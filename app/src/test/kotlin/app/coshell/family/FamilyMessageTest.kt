package app.coshell.family

import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.sync.SyncIssueKind
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class FamilyMessageTest {
    @Test
    fun `a failed action names its cause instead of «no connection»`() {
        assertEquals(FamilyMessage.ActionFailed(SyncIssueKind.OFFLINE), FamilyMessage.failed(IOException("timeout")))
        assertEquals(
            FamilyMessage.ActionFailed(SyncIssueKind.NO_ACCESS, "PERMISSION_DENIED"),
            FamilyMessage.failed(ApiException(403, "PERMISSION_DENIED", "rules")),
        )
        assertEquals(SyncIssueKind.SIGNED_OUT, FamilyMessage.failed(ApiException(401, null, "")).kind)
        assertEquals(SyncIssueKind.QUOTA, FamilyMessage.failed(ApiException(429, null, "")).kind)
        assertEquals(FamilyMessage.ActionFailed(SyncIssueKind.SERVER, "HTTP 500"), FamilyMessage.failed(ApiException(500, null, "")))
    }
}
