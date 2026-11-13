#!/usr/bin/env bash
# Publish firebase/firestore.rules and database.rules.json to the owner's Firebase project.
# ci.yml runs it on dev after scripts/test-rules.sh when the rules changed (docs/agents/ci.md).
# Env: FIREBASE_SERVICE_ACCOUNT  key JSON (roles: Firebase Rules Admin, Firebase Realtime Database Admin)
#      FIREBASE_PROJECT_ID       optional, defaults to the key's project_id
# Firestore must deploy. Realtime Database is optional (docs/setup/sync.md §6): when it is not created
# yet, its deploy fails with a warning instead of a red CI.
set -euo pipefail
[ -n "${FIREBASE_SERVICE_ACCOUNT:-}" ] || { echo "::warning::FIREBASE_SERVICE_ACCOUNT is not set — rules not deployed"; exit 0; }
key=$(mktemp)
trap 'rm -f "$key"' EXIT
printf '%s' "$FIREBASE_SERVICE_ACCOUNT" > "$key"
export GOOGLE_APPLICATION_CREDENTIALS="$key"
project=${FIREBASE_PROJECT_ID:-$(jq -r .project_id "$key")}
# 403 "caller does not have permission" → give this account the roles above in this project (docs/setup/sync.md §4).
echo "Service account: $(jq -r .client_email "$key"), project: $project"
cd "$(dirname "$0")/../../firebase"
deploy() { npx --yes firebase-tools@14.11.0 deploy --only "$1" --project "$project" --non-interactive; }
deploy firestore:rules
echo "Firestore rules deployed to $project"
if deploy database; then echo "Realtime Database rules deployed to $project"
else echo "::warning::Realtime Database rules not deployed (database not created? docs/setup/sync.md §6)"; fi
