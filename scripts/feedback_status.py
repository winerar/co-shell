"""Writes the status of each feedback's GitHub issue back into its Firestore document (#157).

The app reads its own sent messages by document id and shows «Получен → В работе → Сделано в сборке N».
Run from feedback_to_issues.py after new issues are created. Fields (read by the app's
FirestoreFeedbackStatusSource; keep names in sync):
  status        received | in_progress | done | later | closed
  issueAt       when the issue was created
  inProgressAt  when this robot first saw the issue in work (`status:in-progress`)
  doneAt        when the issue was closed
  doneBuild     first release (tag build-N) published after the issue was closed
"""
import re
import urllib.parse

FIRESTORE = "https://firestore.googleapis.com/v1"
LIMIT = 300


def issue_status(issue):
    """GitHub issue → status string (labels: docs/agents/tasks.md)."""
    if issue.get("state") == "closed":
        return "done" if issue.get("state_reason") in (None, "completed") else "closed"
    labels = {label["name"] for label in issue.get("labels", [])}
    if "status:in-progress" in labels:
        return "in_progress"
    if "status:backlog" in labels:
        return "later"
    return "received"


def release_build(closed_at, releases):
    """The first build released at or after [closed_at]: releases are cut from dev, so it carries the fix."""
    builds = []
    for release in releases:
        match = re.fullmatch(r"build-(\d+)", release.get("tag_name", ""))
        published = release.get("published_at")
        if match and published and not release.get("draft") and published >= closed_at:
            builds.append((published, int(match.group(1))))
    return min(builds)[1] if builds else None


def status_fields(issue, releases, previous, now):
    """Fields to store for [issue]; [previous] — the document's current fields (decoded)."""
    status = issue_status(issue)
    fields = {"status": status, "issueAt": issue["created_at"]}
    in_progress_at = previous.get("inProgressAt") or (now if status == "in_progress" else None)
    if in_progress_at:
        fields["inProgressAt"] = in_progress_at
    if status in ("done", "closed") and issue.get("closed_at"):
        fields["doneAt"] = issue["closed_at"]
        if status == "done":
            build = release_build(issue["closed_at"], releases)
            if build is not None:
                fields["doneBuild"] = build
    return fields


def changed(previous, fields):
    return {k: v for k, v in fields.items() if previous.get(k) != v}


def settled(previous):
    """Nothing will change: closed, or done and released."""
    return previous.get("status") == "closed" or (previous.get("status") == "done" and previous.get("doneBuild"))


def encode_value(name, value):
    if name in ("issueAt", "inProgressAt", "doneAt"):
        return {"timestampValue": value}
    if isinstance(value, int):
        return {"integerValue": str(value)}
    return {"stringValue": value}


def processed_feedback(request, project, token, decode_document):
    url = f"{FIRESTORE}/projects/{project}/databases/(default)/documents:runQuery"
    query = {
        "structuredQuery": {
            "from": [{"collectionId": "feedback"}],
            "where": {"fieldFilter": {"field": {"fieldPath": "processed"}, "op": "EQUAL", "value": {"booleanValue": True}}},
            "limit": LIMIT,
        }
    }
    return [decode_document(r["document"]) for r in request("POST", url, token, query) if "document" in r]


def sync_statuses(request, project, token, repo, github_token, decode_document, now):
    """Updates every processed, not yet settled feedback document; returns how many changed."""
    github = {"Accept": "application/vnd.github+json"}
    releases = request("GET", f"https://api.github.com/repos/{repo}/releases?per_page=100", github_token, headers=github)
    updated = 0
    for doc in processed_feedback(request, project, token, decode_document):
        number = doc.get("issueNumber")
        if not number or settled(doc):
            continue
        issue = request("GET", f"https://api.github.com/repos/{repo}/issues/{number}", github_token, headers=github)
        diff = changed(doc, status_fields(issue, releases, doc, now))
        if not diff:
            continue
        mask = urllib.parse.urlencode([("updateMask.fieldPaths", k) for k in diff])
        body = {"fields": {k: encode_value(k, v) for k, v in diff.items()}}
        request("PATCH", f"{FIRESTORE}/{doc['_name']}?{mask}", token, body)
        updated += 1
        print(f"feedback/{doc['_id']} (#{number}): {', '.join(f'{k}={v}' for k, v in diff.items())}")
    return updated
