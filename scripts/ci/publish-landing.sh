#!/usr/bin/env bash
# Builds the landing page from a release manifest and pushes it to the gh-pages branch of the releases repo (GitHub Pages).
#   scripts/ci/publish-landing.sh <version.json> <label>
# Env: GH_TOKEN (RELEASES_TOKEN: Contents write on the releases repo), RELEASES_REPO (owner/co-shell-releases).
# Used by ci.yml after a release (fresh version.json) and on dev when only the page changed (the latest release's version.json).
set -euo pipefail
manifest=${1:?usage: publish-landing.sh <version.json> <label>}
label=${2:-update}
: "${RELEASES_REPO:?}"
if [ -z "${GH_TOKEN:-}" ]; then echo "No RELEASES_TOKEN: landing not published"; exit 0; fi
work=$(mktemp -d)
python3 scripts/make_landing.py "$manifest" "$work/out"
remote="https://x-access-token:${GH_TOKEN}@github.com/$RELEASES_REPO.git"
git clone --quiet --depth 1 --branch gh-pages "$remote" "$work/repo" 2>/dev/null \
    || { git init --quiet "$work/repo" && git -C "$work/repo" checkout --quiet -b gh-pages && git -C "$work/repo" remote add origin "$remote"; }
find "$work/repo" -mindepth 1 -maxdepth 1 ! -name .git -exec rm -rf {} +
cp -r "$work/out/." "$work/repo/"
git -C "$work/repo" add -A
if git -C "$work/repo" -c user.name="co-shell bot" -c user.email="bot@users.noreply.github.com" commit --quiet -m "Landing: $label"; then
    git -C "$work/repo" push --quiet origin gh-pages
    echo "Landing published ($label)"
else
    echo "Landing unchanged"
fi
