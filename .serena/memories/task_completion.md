# keyfc-api task completion checklist
- Run `./gradlew test` to exercise the multiplatform tests (currently limited to the stub under `library/src/commonTest`).
- Run `./gradlew :library:build` (and `:example:build` if the changes touch the sample) to make sure all targets compile; build failures often indicate missing platform-specific dependencies.
- If packaging a release or verifying integration, run `./gradlew :library:publishToMavenLocal` so downstream consumers can pull the artifact locally.
- When modifying any example, rebuild the JVM jar (`./gradlew :example:build`) and run the intended `main` via `java -cp example/build/libs/example.jar <package>.<ExampleKt>` to double-check the workflow.
- Finish with `git status`/`git diff` to confirm only intended files changed, and consider running `rg`, `sed`, or `cat` to inspect generated outputs if needed.
