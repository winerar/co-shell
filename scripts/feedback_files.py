"""Files attached to feedback (FeedbackAttachment in the app): glued from base64 parts and kept on a branch.

The app writes each file as parts in the Firestore collection `feedback_files` and lists their ids in the message's
`attachments`. Here the parts are read with the service account, glued back, committed to the branch `feedback-files`
(`feedback/<message id>/<n>-<name>`) and linked from the issue; then the parts are deleted (Firestore free quota, privacy).
"""
import base64
import re
import urllib.error
import urllib.parse

FIRESTORE = "https://firestore.googleapis.com/v1"
BRANCH = "feedback-files"
GITHUB = "https://api.github.com"
GH_HEADERS = {"Accept": "application/vnd.github+json"}


def safe_name(name):
    """Letters, digits, dot, dash, underscore; no path; never empty."""
    cleaned = re.sub(r"[^\w.\-]", "_", name.rsplit("/", 1)[-1], flags=re.UNICODE).strip("._") or "file"
    return cleaned[:80]


def glue(parts):
    """Parts (decoded documents) → [(name, mime, bytes)], in file order. A file with missing parts is skipped."""
    files = {}
    for part in parts:
        files.setdefault(part.get("file", 0), []).append(part)
    out = []
    for index in sorted(files):
        chunks = sorted(files[index], key=lambda p: p.get("part", 0))
        expected = chunks[0].get("parts", len(chunks))
        if [p.get("part") for p in chunks] != list(range(expected)):
            continue
        data = base64.b64decode("".join(p.get("data", "") for p in chunks))
        out.append((chunks[0].get("name", "file"), chunks[0].get("mime", ""), data))
    return out


def read_parts(request, decode_document, project, token, ids):
    base = f"{FIRESTORE}/projects/{project}/databases/(default)/documents/feedback_files"
    parts = []
    for part_id in ids:
        try:
            parts.append(decode_document(request("GET", f"{base}/{part_id}", token)))
        except urllib.error.HTTPError as e:
            if e.code != 404:
                raise
    return parts


def delete_parts(request, parts, token):
    for part in parts:
        try:
            request("DELETE", f"{FIRESTORE}/{part['_name']}", token)
        except urllib.error.HTTPError as e:
            if e.code != 404:
                raise


def ensure_branch(request, repo, gh_token):
    try:
        request("GET", f"{GITHUB}/repos/{repo}/git/ref/heads/{BRANCH}", gh_token, headers=GH_HEADERS)
        return
    except urllib.error.HTTPError as e:
        if e.code != 404:
            raise
    default = request("GET", f"{GITHUB}/repos/{repo}", gh_token, headers=GH_HEADERS)["default_branch"]
    head = request("GET", f"{GITHUB}/repos/{repo}/git/ref/heads/{default}", gh_token, headers=GH_HEADERS)["object"]["sha"]
    request("POST", f"{GITHUB}/repos/{repo}/git/refs", gh_token, {"ref": f"refs/heads/{BRANCH}", "sha": head}, GH_HEADERS)


def upload(request, repo, gh_token, message_id, files):
    """Commits files to the branch; returns [(name, size, url)]. Re-running overwrites the same paths."""
    if not files:
        return []
    ensure_branch(request, repo, gh_token)
    links = []
    for n, (name, _mime, data) in enumerate(files, start=1):
        path = f"feedback/{message_id}/{n}-{safe_name(name)}"
        url = f"{GITHUB}/repos/{repo}/contents/{urllib.parse.quote(path)}"
        body = {"message": f"Feedback file {path}", "content": base64.b64encode(data).decode(), "branch": BRANCH}
        try:
            existing = request("GET", f"{url}?ref={BRANCH}", gh_token, headers=GH_HEADERS)
            body["sha"] = existing["sha"]
        except urllib.error.HTTPError as e:
            if e.code != 404:
                raise
        request("PUT", url, gh_token, body, GH_HEADERS)
        links.append((name, len(data), f"https://github.com/{repo}/blob/{BRANCH}/{urllib.parse.quote(path)}"))
    return links


def links_section(links):
    if not links:
        return ""
    rows = "\n".join(f"- [{name}]({url}) · {size // 1024 or 1} КБ" for name, size, url in links)
    return f"**Вложения** (ветка `{BRANCH}`):\n{rows}\n\n"
