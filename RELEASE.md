# Release Process

A new version of `rethis` is shipped via a single dispatch-driven workflow. You
do all your work on `dev` (including the changelog entry); when you're ready,
trigger the workflow with a version number and it handles the rest:
`dev → master` merge, build, publish to Maven Central, tag, README bump,
benchmarks, GitHub Release, and Dokka deploy.

## Versioning

* Semantic Versioning: `MAJOR.MINOR.PATCH` (e.g. `0.4.0`).
* The version lives only in the git tag — no version files in the repo.
* The library version at runtime comes from the `libVersion` env var, which is
  set by the release workflow from the dispatch input.

## Branches

* `dev` — integration branch. All feature work and changelog edits land here.
* `master` — release branch. Updated by the release workflow only; force-synced
  back to `dev` after each master push by `dev-sync.yml`.

## Required prerequisites

* `CHANGELOG.md` on `dev` must contain a `## x.y.z` heading for the version
  being released. The workflow hard-fails if the heading is missing.
* The following repository secrets must be configured:
  * `ACCESS_TOKEN` — PAT with `repo` scope (and admin if `master` is protected).
    Used for the dev→master push, tag push, README bump commit, and GitHub
    Release creation.
  * `SONATYPE_USERNAME`, `SONATYPE_PASSWORD` — Maven Central credentials.
  * `GPG_KEY_CONTENT`, `GPG_KEY_ID`, `GPG_KEY_PWD` — signing key.
  * `CODECOV_TOKEN` — coverage upload (used by CI, not release).

## How to release

1. **On `dev`**, add a `## x.y.z` section at the top of `CHANGELOG.md` with the
   release notes. Land it via your normal PR flow.
2. **Actions → Release → Run workflow** → enter the version (e.g. `0.4.1`) →
   **Run workflow**.

That's it. The workflow:

1. Validates the version format and the `## x.y.z` heading on `dev`.
2. Refuses if the tag already exists or if `dev` is not ahead of `master`.
3. Builds with `./gradlew koverXmlReport` to verify `dev` is releasable.
4. Fast-forwards `master` to `dev`.
5. Publishes signed artifacts to Maven Central.
6. Tags `master` HEAD with `x.y.z` and pushes the tag.
7. Replaces the version string in `README.md`, runs benchmarks, updates the
   benchmark table, commits the result back to `master`.
8. Extracts the matching CHANGELOG section and creates a GitHub Release.
9. Generates the Dokka site and deploys it to `gh-pages`.
10. `dev-sync.yml` fires automatically on each `master` push and keeps `dev`
    aligned with `master`.

After the run completes, verify on https://central.sonatype.com that the new
version shows up (propagation typically takes a few minutes).

## Rolling back

* **Tag pushed but the workflow is still running**: cancel the run, then delete
  the tag locally and remotely:
  ```bash
  git tag -d X.Y.Z
  git push origin :refs/tags/X.Y.Z
  ```
* **Maven Central publish succeeded**: artifacts are immutable. Bump to the next
  patch and release again.
* **Maven publish failed mid-flight**: re-run the failed step from the Actions
  UI. If the staging repo is in a bad state, drop it from Sonatype Central and
  re-run.
* **Docs deploy failed but everything else succeeded**: re-run only the Dokka
  steps locally with `libVersion=x.y.z ./gradlew :docs:dokkaGenerate`, then
  push the output to `gh-pages` manually — or re-dispatch the workflow with the
  same version (the early "tag already exists" check will block it; a tiny
  `docs-redeploy.yml` workflow can be added if you find this happens often).

## Troubleshooting

* **`CHANGELOG.md on dev must contain '## x.y.z'`** → add the heading to `dev`
  and re-dispatch.
* **`Tag x.y.z already exists`** → the workflow ran (or partially ran) for this
  version. Bump to the next patch.
* **`dev is not ahead of master`** → there's nothing new to release. Land your
  changes on `dev` first.
* **Fast-forward step fails** → `master` has commits not on `dev` (shouldn't
  happen given `dev-sync.yml`, but if it does, manually sync first). Or the
  PAT lacks permission to push to protected `master`.
* **Sonatype publish step fails** → verify `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`,
  and the GPG signing secrets. Sonatype occasionally returns 5xx during high
  traffic; re-running the step is usually enough.
* **README bump commit fails** → branch protection on `master` blocks the bot.
  Either grant `github-actions[bot]` push access (recommended) or push the
  bump commit manually.

## Why a PAT instead of `GITHUB_TOKEN`

The default `GITHUB_TOKEN` cannot push to protected branches in many
configurations, and pushes made with it do not trigger downstream `push`
workflows. The release workflow uses `ACCESS_TOKEN` for the dev→master push,
tag push, README commit, and GitHub Release. If `ACCESS_TOKEN` expires, those
steps fail loudly with auth errors — the workflow won't silently stall.
