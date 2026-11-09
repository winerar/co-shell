package app.coshell.core.data.remote

import app.coshell.core.domain.family.FamilyRemote
import app.coshell.core.domain.family.normalizeEmail
import app.coshell.core.model.Family
import app.coshell.core.model.Invite
import app.coshell.core.model.Member
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * `families/{id}`: `ownerUid`, `memberUids` (for rules), `members` (map uid → {name, email}), `createdAt`.
 * `invites/{email}`: `familyId`, `invitedBy`, `createdAt`. Layout must match firebase/firestore.rules.
 */
class FirestoreFamilyRemote(private val client: FirestoreClient) : FamilyRemote {

    override suspend fun family(familyId: String): Family? = try {
        client.get("families/$familyId")?.let { parseFamily(familyId, it) }
    } catch (e: ApiException) {
        if (e.isPermissionDenied) null else throw e
    }

    override suspend fun inviteFor(email: String): Invite? = try {
        val id = normalizeEmail(email)
        client.get("invites/$id")?.let { parseInvite(id, it) }
    } catch (e: ApiException) {
        if (e.isPermissionDenied) null else throw e
    }

    override suspend fun create(familyId: String, me: Member): Boolean = try {
        client.commit(listOf(buildJsonObject {
            put("update", buildJsonObject {
                put("name", client.name("families/$familyId"))
                put("fields", JsonObject(mapOf(
                    "ownerUid" to fsString(me.uid),
                    "memberUids" to fsArray(listOf(fsString(me.uid))),
                    "members" to fsMap(mapOf(me.uid to memberValue(me))),
                )))
            })
            put("currentDocument", buildJsonObject { put("exists", false) })
            put("updateTransforms", serverTime("createdAt"))
        }))
        true
    } catch (e: ApiException) {
        // Exists already: the precondition fails, or rules deny it as an update.
        if (e.isAlreadyExists || e.isPermissionDenied) false else throw e
    }

    override suspend fun join(invite: Invite, me: Member) {
        client.commit(listOf(buildJsonObject {
            put("update", buildJsonObject {
                put("name", client.name("families/${invite.familyId}"))
                put("fields", JsonObject(mapOf("members" to fsMap(mapOf(me.uid to memberValue(me))))))
            })
            put("updateMask", buildJsonObject {
                put("fieldPaths", buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive("members.`${me.uid}`")) })
            })
            put("currentDocument", buildJsonObject { put("exists", true) })
            put("updateTransforms", buildJsonArray {
                add(buildJsonObject {
                    put("fieldPath", "memberUids")
                    put("appendMissingElements", buildJsonObject { put("values", buildJsonArray { add(fsString(me.uid)) }) })
                })
            })
        }))
    }

    override suspend fun invite(familyId: String, email: String, invitedBy: String) {
        client.commit(listOf(buildJsonObject {
            put("update", buildJsonObject {
                put("name", client.name("invites/${normalizeEmail(email)}"))
                put("fields", JsonObject(mapOf("familyId" to fsString(familyId), "invitedBy" to fsString(invitedBy))))
            })
            put("updateTransforms", serverTime("createdAt"))
        }))
    }

    override suspend fun invites(familyId: String): List<Invite> {
        val query = buildJsonObject {
            put("from", buildJsonArray { add(buildJsonObject { put("collectionId", "invites") }) })
            put("where", buildJsonObject {
                put("fieldFilter", buildJsonObject {
                    put("field", buildJsonObject { put("fieldPath", "familyId") })
                    put("op", "EQUAL")
                    put("value", fsString(familyId))
                })
            })
        }
        return client.runQuery("", query).mapNotNull { doc ->
            val id = doc["name"]!!.jsonPrimitive.content.substringAfterLast('/')
            parseInvite(id, doc)
        }
    }

    override suspend fun cancelInvite(email: String) {
        client.commit(listOf(buildJsonObject { put("delete", client.name("invites/${normalizeEmail(email)}")) }))
    }

    private fun memberValue(m: Member) = fsMap(mapOf("name" to fsString(m.name), "email" to fsString(normalizeEmail(m.email))))

    private fun serverTime(field: String) = buildJsonArray {
        add(buildJsonObject { put("fieldPath", field); put("setToServerValue", "REQUEST_TIME") })
    }

    private fun parseFamily(id: String, doc: JsonObject): Family? {
        val f = FsFields.of(doc)
        val members = f.map("members") ?: return null
        return Family(
            id = id,
            ownerUid = f.string("ownerUid") ?: return null,
            members = f.strings("memberUids").mapNotNull { uid ->
                members.map(uid)?.let { Member(uid, it.string("name").orEmpty(), it.string("email").orEmpty()) }
            },
        )
    }

    private fun parseInvite(email: String, doc: JsonObject): Invite? {
        val f = FsFields.of(doc)
        return Invite(email, f.string("familyId") ?: return null, f.string("invitedBy").orEmpty())
    }
}
