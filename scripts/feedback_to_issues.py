#!/usr/bin/env python3
"""Turns new feedback from Firestore into GitHub issues (run by .github/workflows/feedback.yml).

Reads documents of the `feedback` collection with `processed == false` (written by the app's
feedback form), creates an issue labelled `source:app` + `status:triage` for each, then marks the document
`processed = true` with the issue number. An issue is created before marking, so a failure in
between can at worst produce a duplicate issue, never lose a message. Then writes each issue's status
back into its document for the app (feedback_status.py, #157).

Environment:
  FIREBASE_SERVICE_ACCOUNT  service account key JSON (needs role "Cloud Datastore User")
  FIREBASE_PROJECT_ID       optional, defaults to the key's project_id
  GITHUB_TOKEN, GITHUB_REPOSITORY  set by GitHub Actions (issues: write)
"""
import datetime
import json
import os
import sys
import urllib.parse
import urllib.error
import urllib.request

import feedback_status

FIRESTORE = "https://firestore.googleapis.com/v1"
BATCH = 20
TITLE_LENGTH = 70
LABELS = ["source:app", "status:triage"]  # labels: docs/agents/tasks.md; type is set at triage


def decode_value(value):
    """One Firestore REST value ({"stringValue": "x"}) → Python value."""
    if "stringValue" in value:
        return value["stringValue"]
    if "integerValue" in value:
        return int(value["integerValue"])
    if "booleanValue" in value:
        return value["booleanValue"]
    if "timestampValue" in value:
        return value["timestampValue"]
    if "nullValue" in value:
        return None
    return json.dumps(value, ensure_ascii=False)


def decode_document(document):
    fields = {k: decode_value(v) for k, v in document.get("fields", {}).items()}
    fields["_name"] = document["name"]
    fields["_id"] = document["name"].rsplit("/", 1)[-1]
    return fields


# Must match DIAGNOSTICS_MARKER in core/domain/.../feedback/FeedbackText.kt (#216).
DIAGNOSTICS_MARKER = "\n\n--- tech ---\n"


def split_text(text):
    """User's words and the technical lines the app appends after the marker."""
    user, _, tech = text.partition(DIAGNOSTICS_MARKER)
    return user, tech


def tech_details(tech):
    if not tech.strip():
        return ""
    # A tilde fence: a backtick fence in the data cannot close it.
    return f"<details><summary>Технические данные</summary>\n\n~~~~\n{tech}\n~~~~\n\n</details>\n\n"


def issue_title(text):
    first_line = next((line.strip() for line in text.splitlines() if line.strip()), "Без текста")
    if len(first_line) > TITLE_LENGTH:
        first_line = first_line[: TITLE_LENGTH - 1].rstrip() + "…"
    return f"Отзыв: {first_line}"


def issue_body(feedback):
    user_text, tech = split_text(feedback.get("text", ""))
    quoted = "\n".join(f"> {line}" if line else ">" for line in user_text.splitlines())
    created = feedback.get("createdAt", "?").replace("T", " ").split(".")[0].rstrip("Z")
    rows = [
        ("Отправлено (UTC)", created),
        ("Версия", f"{feedback.get('appVersion', '?')} (сборка {feedback.get('versionCode', '?')})"),
        ("Телефон", feedback.get("device", "?")),
        ("Система", feedback.get("os", "?")),
        ("Установка", str(feedback.get("installationId", "?"))[:8]),
        ("Документ", f"`feedback/{feedback['_id']}`"),
    ]
    table = "\n".join(f"| {k} | {v} |" for k, v in rows)
    return (
        f"{quoted}\n\n"
        f"| | |\n|---|---|\n{table}\n\n"
        f"{tech_details(tech)}"
        "_Создано автоматически из формы «Ещё → Обратная связь»._"
    )


def oldest_first(feedback_list):
    return sorted(feedback_list, key=lambda f: f.get("createdAt") or "")


def request(method, url, token, body=None, headers=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Authorization", f"Bearer {token}")
    req.add_header("Content-Type", "application/json")
    for k, v in (headers or {}).items():
        req.add_header(k, v)
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read() or b"null")


def google_token(service_account_info):
    from google.auth.transport.requests import Request
    from google.oauth2 import service_account

    creds = service_account.Credentials.from_service_account_info(
        service_account_info, scopes=["https://www.googleapis.com/auth/datastore"]
    )
    creds.refresh(Request())
    return creds.token


def unprocessed_feedback(project, token):
    url = f"{FIRESTORE}/projects/{project}/databases/(default)/documents:runQuery"
    query = {
        "structuredQuery": {
            "from": [{"collectionId": "feedback"}],
            "where": {
                "fieldFilter": {
                    "field": {"fieldPath": "processed"},
                    "op": "EQUAL",
                    "value": {"booleanValue": False},
                }
            },
            "limit": BATCH,
        }
    }
    rows = request("POST", url, token, query)
    return oldest_first(decode_document(r["document"]) for r in rows if "document" in r)


def create_issue(repo, token, feedback):
    url = f"https://api.github.com/repos/{repo}/issues"
    body = {"title": issue_title(split_text(feedback.get("text", ""))[0]), "body": issue_body(feedback), "labels": LABELS}
    issue = request("POST", url, token, body, {"Accept": "application/vnd.github+json"})
    return issue["number"]


def mark_processed(document_name, token, issue_number):
    mask = urllib.parse.urlencode([("updateMask.fieldPaths", "processed"), ("updateMask.fieldPaths", "issueNumber")])
    body = {"fields": {"processed": {"booleanValue": True}, "issueNumber": {"integerValue": str(issue_number)}}}
    request("PATCH", f"{FIRESTORE}/{document_name}?{mask}", token, body)


def main():
    account = json.loads(os.environ["FIREBASE_SERVICE_ACCOUNT"])
    project = os.environ.get("FIREBASE_PROJECT_ID") or account["project_id"]
    repo = os.environ["GITHUB_REPOSITORY"]
    github_token = os.environ["GITHUB_TOKEN"]

    token = google_token(account)
    try:
        feedback_list = unprocessed_feedback(project, token)
        print(f"New feedback: {len(feedback_list)}")
        for feedback in feedback_list:
            number = create_issue(repo, github_token, feedback)
            mark_processed(feedback["_name"], token, number)
            print(f"feedback/{feedback['_id']} → issue #{number}")
        now = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
        updated = feedback_status.sync_statuses(request, project, token, repo, github_token, decode_document, now)
        print(f"Statuses updated: {updated}")
    except urllib.error.HTTPError as e:
        if e.code != 429:
            raise
        # Daily free quota of Firestore reads is spent; it resets at midnight Pacific time and
        # the next scheduled run picks the feedback up. Not a failure of this workflow.
        print("::warning::Firestore quota exhausted (HTTP 429) — feedback will be fetched on the next run")
    return 0


if __name__ == "__main__":
    sys.exit(main())
