#!/usr/bin/env bash
# Gradle for agents — use instead of ./gradlew (#156). Same arguments as ./gradlew.
# 1. Always adds maven-mirror.init.gradle: dependencies come from Google's Maven Central mirror,
#    so the cloud proxy's 429 «Too Many Requests» from Maven Central does not stop the build.
# 2. If a 429 still happens, downloads the named files with curl (m2-mirror.sh) into ~/.m2 and
#    repeats the build once. Output and exit code are Gradle's.
# Example: scripts/agent/gradle.sh :app:testDebugUnitTest --tests '*UpdateControllerTest'
set -uo pipefail
cd "$(dirname "$0")/../.."
init=scripts/agent/maven-mirror.init.gradle
log=$(mktemp)
trap 'rm -f "$log"' EXIT

./gradlew --init-script "$init" "$@" 2>&1 | tee "$log"
status=${PIPESTATUS[0]}
[ "$status" -eq 0 ] && exit 0
grep -q '429\|Failed to fetch maven artifact' "$log" || exit "$status"

# Coordinates from «Could not GET '…/maven2/<group path>/<artifact>/<version>/<file>'» and Robolectric's
# «Failed to fetch maven artifact <group>:<artifact>:<version>».
coords=$(python3 - "$log" <<'PY'
import re, sys
text = open(sys.argv[1], encoding='utf-8', errors='replace').read()
found = set()
for path in re.findall(r"/maven2/([^'\s]+)/[^/'\s]+\.(?:jar|pom|aar|module)", text):
    parts = path.split('/')
    if len(parts) >= 3:
        found.add('%s:%s:%s' % ('.'.join(parts[:-2]), parts[-2], parts[-1]))
found.update(re.findall(r'Failed to fetch maven artifact ([\w.\-]+:[\w.\-]+:[\w.\-]+)', text))
print(' '.join(sorted(found)))
PY
)
[ -z "$coords" ] && exit "$status"
echo "== 429: fetching with curl into ~/.m2: $coords"
M2_REPO="$HOME/.m2/repository" scripts/agent/m2-mirror.sh "$(dirname "$log")" $coords
echo "== retrying the build"
./gradlew --init-script "$init" "$@"
