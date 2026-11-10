#!/usr/bin/env python3
"""Syncs the GitHub Projects board with issue `status:*` labels (.github/workflows/tasks.yml).

Labels are the source of truth the agent reads and writes; the board is the owner's view.
- every open issue is on the board; its Status column follows its `status:*` label;
- a closed issue goes to «Готово» (closed = merged into dev: `Fixes #N`, dev is the default branch);
- the owner moved a card (the item changed after the issue's last label change) → the label
  follows the board; «Готово» on an open issue closes it;
- field «Тип» (story / feature / bug / question / owner check) follows the type label, one way:
  the owner can group or filter the board by it.

Env: PROJECT_TOKEN (PAT with project access), GITHUB_TOKEN (repo issues), GITHUB_REPOSITORY,
PROJECT_OWNER, PROJECT_NUMBER. Docs: docs/agents/tasks.md.
"""
import json
import os
import sys
import urllib.request

API = "https://api.github.com"
FIELD = "Status"
DONE = "Готово"
TRIAGE = "status:triage"
# label → column, in board order; «Готово» is the last column
COLUMNS = [
    ("status:triage", "Разобрать", "RED"),
    ("status:backlog", "Бэклог", "GRAY"),
    ("status:next", "Очередь", "GREEN"),
    ("status:in-progress", "В работе", "YELLOW"),
    ("status:needs-owner", "Ждёт владельца", "ORANGE"),
]
TYPE_FIELD = "Тип"
# type label → «Тип» option; an issue has exactly one (docs/agents/tasks.md), the first found wins
TYPES = [
    ("type:story", "История", "PURPLE"),
    ("type:feature", "Фича", "BLUE"),
    ("type:bug", "Баг", "RED"),
    ("type:question", "Вопрос", "PINK"),
    ("type:owner", "Владельцу", "ORANGE"),
]
LABEL_TO_COLUMN = {label: column for label, column, _ in COLUMNS}
COLUMN_TO_LABEL = {column: label for label, column, _ in COLUMNS}


def status_label(labels):
    return next((l for l in labels if l.startswith("status:")), None)


def decide(issue, item):
    """What to change for one issue. issue: {open, labels, labels_at}; item: {status, updated_at} or None.

    Returns (column to set on the board or None, label to set on the issue or None, close issue?).
    Timestamps are ISO-8601 strings in UTC, so they compare as strings.
    """
    if not issue["open"]:
        return (DONE if item and item["status"] != DONE else None), None, False
    want = LABEL_TO_COLUMN.get(status_label(issue["labels"]), LABEL_TO_COLUMN[TRIAGE])
    if item is None or item["status"] == want:
        return (want if item is None else None), None, False
    moved_by_owner = item["status"] and (item["updated_at"] or "") > (issue["labels_at"] or "")
    if moved_by_owner and item["status"] == DONE:
        return None, None, True
    if moved_by_owner and item["status"] in COLUMN_TO_LABEL:
        return None, COLUMN_TO_LABEL[item["status"]], False
    return want, None, False


def type_of(labels):
    return next((name for label, name, _ in TYPES if label in labels), None)


def decide_type(issue, current):
    """«Тип» to set for an open issue: (change?, option name or None to clear)."""
    want = type_of(issue["labels"]) if issue["open"] else current
    return want != current, want


def graphql(token, query, **variables):
    body = json.dumps({"query": query, "variables": variables}).encode()
    req = urllib.request.Request(f"{API}/graphql", body, {"Authorization": f"bearer {token}"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.load(resp)
    if data.get("errors"):
        sys.exit(f"GraphQL error: {data['errors']}")
    return data["data"]


def rest(token, method, path, payload=None):
    req = urllib.request.Request(f"{API}{path}", json.dumps(payload).encode() if payload else None,
                                 {"Authorization": f"bearer {token}", "Accept": "application/vnd.github+json"},
                                 method=method)
    with urllib.request.urlopen(req, timeout=30) as resp:
        return resp.read()


def drop_status(token, repo, number, labels):
    old = status_label(labels)
    if old:
        rest(token, "DELETE", f"/repos/{repo}/issues/{number}/labels/{old.replace(':', '%3A')}")


PROJECT_Q = """query($owner:String!,$number:Int!,$after:String){user(login:$owner){projectV2(number:$number){
 id field(name:"Status"){... on ProjectV2SingleSelectField{id options{id name}}}
 kind:field(name:"Тип"){... on ProjectV2SingleSelectField{id options{id name}}}
 items(first:100,after:$after){pageInfo{hasNextPage endCursor} nodes{id updatedAt
  fieldValueByName(name:"Status"){... on ProjectV2ItemFieldSingleSelectValue{name}}
  kind:fieldValueByName(name:"Тип"){... on ProjectV2ItemFieldSingleSelectValue{name}}
  content{... on Issue{number repository{nameWithOwner}}}}}}}}"""

ISSUES_Q = """query($owner:String!,$name:String!,$after:String){repository(owner:$owner,name:$name){
 issues(first:100,after:$after,states:OPEN){pageInfo{hasNextPage endCursor} nodes{id number
  labels(first:20){nodes{name}}
  timelineItems(last:1,itemTypes:[LABELED_EVENT,UNLABELED_EVENT]){nodes{
   ... on LabeledEvent{createdAt} ... on UnlabeledEvent{createdAt}}}}}}}"""

SET_OPTIONS_M = """mutation($field:ID!,$options:[ProjectV2SingleSelectFieldOptionInput!]!){
 updateProjectV2Field(input:{fieldId:$field,singleSelectOptions:$options}){
  projectV2Field{... on ProjectV2SingleSelectField{id options{id name}}}}}"""

CREATE_FIELD_M = """mutation($project:ID!,$name:String!,$options:[ProjectV2SingleSelectFieldOptionInput!]!){
 createProjectV2Field(input:{projectId:$project,dataType:SINGLE_SELECT,name:$name,singleSelectOptions:$options}){
  projectV2Field{... on ProjectV2SingleSelectField{id options{id name}}}}}"""

CLEAR_M = """mutation($project:ID!,$item:ID!,$field:ID!){clearProjectV2ItemFieldValue(input:{
 projectId:$project,itemId:$item,fieldId:$field}){projectV2Item{id}}}"""

ADD_M = """mutation($project:ID!,$content:ID!){addProjectV2ItemById(input:{projectId:$project,contentId:$content}){
 item{id}}}"""

SET_M = """mutation($project:ID!,$item:ID!,$field:ID!,$option:String!){updateProjectV2ItemFieldValue(input:{
 projectId:$project,itemId:$item,fieldId:$field,value:{singleSelectOptionId:$option}}){projectV2Item{id}}}"""


def paged(token, query, path, **variables):
    after = None
    while True:
        node = graphql(token, query, after=after, **variables)
        for key in path:
            node = node[key]
        yield node
        conn = node["items"] if "items" in node else node["issues"]
        if not conn["pageInfo"]["hasNextPage"]:
            return
        after = conn["pageInfo"]["endCursor"]


def ensure_options(token, project, field, name, wanted):
    """Creates the single-select field or resets its options. Returns (field, values were reset?)."""
    names = [n for n, _ in wanted]
    if field and [o["name"] for o in field["options"]] == names:
        return field, False
    opts = [{"name": n, "color": c, "description": ""} for n, c in wanted]
    if field:  # recreating options clears values; they are set again below
        field = graphql(token, SET_OPTIONS_M, field=field["id"], options=opts)["updateProjectV2Field"]["projectV2Field"]
    else:
        field = graphql(token, CREATE_FIELD_M, project=project, name=name,
                        options=opts)["createProjectV2Field"]["projectV2Field"]
    print(f"{name} options reset:", ", ".join(names))
    return field, True


def main():
    ptoken, rtoken = os.environ["PROJECT_TOKEN"], os.environ["GITHUB_TOKEN"]
    repo = os.environ["GITHUB_REPOSITORY"]
    owner, name = repo.split("/")
    pages = list(paged(ptoken, PROJECT_Q, ["user", "projectV2"],
                       owner=os.environ["PROJECT_OWNER"], number=int(os.environ["PROJECT_NUMBER"])))
    project = pages[0]["id"]
    field, reset = ensure_options(ptoken, project, pages[0]["field"], FIELD,
                                  [(c, color) for _, c, color in COLUMNS] + [(DONE, "BLUE")])
    kind, kind_reset = ensure_options(ptoken, project, pages[0]["kind"], TYPE_FIELD,
                                      [(n, color) for _, n, color in TYPES])
    option = {o["name"]: o["id"] for o in field["options"]}
    kind_option = {o["name"]: o["id"] for o in kind["options"]}

    items = {}
    for page in pages:
        for n in page["items"]["nodes"]:
            c = n["content"] or {}
            if c.get("repository", {}).get("nameWithOwner") == repo:
                items[c["number"]] = {"id": n["id"], "updated_at": n["updatedAt"],
                                      "status": None if reset else (n["fieldValueByName"] or {}).get("name"),
                                      "kind": None if kind_reset else (n["kind"] or {}).get("name")}
    issues = {}
    for page in paged(rtoken, ISSUES_Q, ["repository"], owner=owner, name=name):
        for n in page["issues"]["nodes"]:
            events = n["timelineItems"]["nodes"]
            issues[n["number"]] = {"id": n["id"], "open": True, "labels": [l["name"] for l in n["labels"]["nodes"]],
                                   "labels_at": events[0]["createdAt"] if events else None}
    for number in items.keys() - issues.keys():  # on the board but not open: closed (or moved)
        issues[number] = {"open": False, "labels": [], "labels_at": None}

    for number, issue in sorted(issues.items()):
        item = items.get(number)
        column, label, close = decide(issue, item)
        if item is None and column:
            item = {"id": graphql(ptoken, ADD_M, project=project, content=issue["id"])["addProjectV2ItemById"]["item"]["id"]}
        if column:
            graphql(ptoken, SET_M, project=project, item=item["id"], field=field["id"], option=option[column])
            print(f"#{number} → {column}")
        change, want = decide_type(issue, item.get("kind") if item else None)
        if change and item:
            if want:
                graphql(ptoken, SET_M, project=project, item=item["id"], field=kind["id"], option=kind_option[want])
            else:
                graphql(ptoken, CLEAR_M, project=project, item=item["id"], field=kind["id"])
            print(f"#{number}: {TYPE_FIELD} → {want or '—'}")
        if label:
            drop_status(rtoken, repo, number, issue["labels"])
            rest(rtoken, "POST", f"/repos/{repo}/issues/{number}/labels", {"labels": [label]})
            print(f"#{number}: board → {label}")
        if close:
            drop_status(rtoken, repo, number, issue["labels"])
            rest(rtoken, "PATCH", f"/repos/{repo}/issues/{number}", {"state": "closed", "state_reason": "completed"})
            print(f"#{number}: «{DONE}» on the board → closed")


if __name__ == "__main__":
    main()
