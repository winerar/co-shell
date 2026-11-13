#!/usr/bin/env bash
# Flows: routines that always run one after another, as one command with short output (agent.sh calls these).
# Each step prints one block; the flow stops at the first failure and says which step failed.
#   flow.sh ship -m "Add X" [-m "trailer…"] [--quick|--affected|--no-check] [--recheck]
#       check (full by default) → git add -A → commit → push with retries
#   flow.sh update
#       merge origin/dev (sync-dev.sh) → tests of touched modules (affected.sh) → push
#   flow.sh finish [-m "Docs: STATUS after X"] [--no-check] [--recheck]
#       doc rules (session-end.sh --pre) → STATUS date and test count (stamp-status.sh) → full check
#       → commit → push → session-end.sh
# check/quick are skipped when the same files (or the same plus docs only) already passed (check-cache.sh);
# --recheck forces the run.
# Commit messages: English imperative (workflow.md «Git»); pass attribution trailers as extra -m.
set -uo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
cd "$here/../.."

step() { echo "── $*"; }
die() { echo "✗ flow stopped at: $*"; exit 1; }

# Parse -m messages and the check mode shared by ship/finish.
msgs=(); mode=full; recheck=0
parse() {
    while [ $# -gt 0 ]; do
        case "$1" in
            -m) msgs+=(-m "${2:?текст после -m}"); shift 2 ;;
            --quick) mode=quick; shift ;;
            --affected) mode=affected; shift ;;
            --no-check) mode=none; shift ;;
            --recheck) recheck=1; shift ;;
            *) die "unknown argument $1" ;;
        esac
    done
}

run_check() {
    local why
    if [ "$recheck" -eq 0 ] && { [ "$mode" = full ] || [ "$mode" = quick ]; } &&
        why=$("$here/check-cache.sh" fresh "$mode"); then
        step "check skipped: $why (--recheck to run)"; return
    fi
    case "$mode" in
        full) step "check (lint + tests + APK)"; "$here/check.sh" || die "check" ;;
        quick) step "check --quick (unit tests)"; "$here/check.sh" --quick || die "check --quick" ;;
        affected) step "tests of touched modules"; "$here/affected.sh" || die "affected tests" ;;
        none) step "check skipped (--no-check)" ;;
    esac
}

commit_all() {
    if [ -z "$(git status --porcelain)" ]; then step "nothing to commit"; return; fi
    [ ${#msgs[@]} -gt 0 ] || die "commit needs -m \"message\""
    step "commit: $(git status --porcelain | wc -l) files"
    git add -A && git commit -q "${msgs[@]}" || die "commit"
    git log -1 --format='   %h %s'
}

cmd="${1:-}"; [ $# -gt 0 ] && shift
case "$cmd" in
    ship)
        parse "$@"
        run_check
        commit_all
        step "push"; "$here/push.sh" || die "push" ;;
    update)
        step "merge origin/dev"; "$here/sync-dev.sh" || die "merge (resolve conflicts, then: agent.sh update)"
        step "tests of touched modules"; "$here/affected.sh" || die "affected tests"
        step "push"; "$here/push.sh" || die "push" ;;
    finish)
        parse "$@"
        [ ${#msgs[@]} -gt 0 ] || msgs=(-m "Docs: STATUS for $(date +%F)")
        step "STATUS facts"; "$here/stamp-status.sh"
        step "doc rules"; "$here/session-end.sh" --pre || die "doc rules (fix ✗, then: agent.sh finish)"
        run_check
        "$here/stamp-status.sh" >/dev/null # the check may have changed the test count
        commit_all
        step "push"; "$here/push.sh" || die "push"
        step "session-end checklist"; "$here/session-end.sh" || die "session-end (fix ✗, then: agent.sh finish)" ;;
    *) sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'; exit 2 ;;
esac
