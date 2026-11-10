#!/usr/bin/env bash
# Works around Maven Central answering Gradle with 429 in cloud sessions (docs/agents/notes.md).
# Usage: scripts/agent/m2-mirror.sh <dir> group:artifact:version [...]
#   Downloads jar+pom of each artifact with curl (Maven Central, then the Google mirror) into <dir>/m2
#   and writes <dir>/m2-init.gradle, which puts that local repo first.
#   Then: scripts/agent/check.sh --init-script <dir>/m2-init.gradle
# Robolectric's own download («Failed to fetch maven artifact …android-all-instrumented…»):
#   M2_REPO=~/.m2/repository scripts/agent/m2-mirror.sh <dir> org.robolectric:android-all-instrumented:<ver>
set -uo pipefail
dir=${1:?usage: m2-mirror.sh <dir> group:artifact:version ...}; shift
repo="${M2_REPO:-$dir/m2}"
mkdir -p "$dir"
mirrors=(https://repo1.maven.org/maven2 https://maven-central.storage-download.googleapis.com/maven2)
for coord in "$@"; do
    IFS=: read -r group artifact version <<<"$coord"
    path="${group//.//}/$artifact/$version"
    mkdir -p "$repo/$path"
    for ext in pom jar; do
        file="$artifact-$version.$ext"
        [ -s "$repo/$path/$file" ] && continue
        ok=0
        for m in "${mirrors[@]}" "${mirrors[@]}"; do
            if curl -sSf -o "$repo/$path/$file" "$m/$path/$file"; then ok=1; break; fi
            sleep 4
        done
        [ $ok -eq 1 ] && echo "ok   $path/$file" || echo "FAIL $path/$file"
    done
done
cat >"$dir/m2-init.gradle" <<GRADLE
settingsEvaluated { s ->
    def add = { r -> def m = r.maven { url = uri('$repo') }; r.remove(m); r.addFirst(m) }
    s.pluginManagement.repositories(add)
    s.dependencyResolutionManagement.repositories(add)
}
GRADLE
echo "init script: $dir/m2-init.gradle"
