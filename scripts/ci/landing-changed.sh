#!/usr/bin/env bash
# Did a merge into dev touch the landing page (texts, template, generator)? Then CI republishes it at once, without a release
# (ci.yml «Publish landing (dev)»: the page is rebuilt from the version.json of the latest published release).
#   scripts/ci/landing-changed.sh <base>   base = "HEAD^1" (merge into dev)
# Exit 0: changed, 1: not.
set -uo pipefail
base=${1:?usage: landing-changed.sh <base>}
changed=$(git diff --name-only "$base" HEAD) || exit 1
grep -E '^site/|^scripts/(landing_texts|make_landing|privacy_pages|qr_code)\.py$|^scripts/ci/publish-landing\.sh$' \
    <<<"$changed" || { echo "Landing unchanged"; exit 1; }
