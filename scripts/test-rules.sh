#!/usr/bin/env bash
# Firestore and Realtime Database rules tests (firebase/rules-test) and the Kotlin sync test against the emulator.
# Needs Node 20+ and Java 11+. The emulator jar is downloaded by firebase-tools on first run.
#   scripts/test-rules.sh          rules tests only
#   scripts/test-rules.sh --kotlin also FirestoreEmulatorTest (real REST sync of two phones)
set -euo pipefail
cd "$(dirname "$0")/../firebase"
(cd rules-test && npm install --silent --no-audit --no-fund)
cmd="cd rules-test && node --test"
if [[ "${1:-}" == "--kotlin" ]]; then
  cmd="$cmd && cd ../.. && ./gradlew -q :core:data:jvmTest --tests '*FirestoreEmulatorTest*' --rerun"
fi
npx --yes firebase-tools@14.11.0 emulators:exec --only firestore,auth,database --project demo-coshell "$cmd"
