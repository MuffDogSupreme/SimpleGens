# Upgrade Plan: SimpleGens (20260608023918)

- **Generated**: 2026-06-08 12:40:49
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 21: not available (baseline will be skipped)
- JDK 25: C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot (available, used by steps 1, 3, 4, 5)

**Build Tools**
- Maven 3.9.11: C:\Program Files\Apache\Maven\apache-maven-3.9.11\bin\mvn
- Maven Wrapper: absent

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260608023918
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java runtime target to the latest LTS version: Java 25

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- | 
| Java | 21 | 25 | User requested latest LTS runtime | 
| Maven | 3.9.11 | 3.9.11 | Supports Java 25 and current project build | 
| maven-compiler-plugin | 3.11.0 | 3.11.0 | Already compatible with JDK 25 | 
| paper-api | 1.21.1-R0.1-SNAPSHOT | N/A | Direct API dependency for Minecraft plugin; no upgrade required for JDK change | 
| worldedit-bukkit | 7.3.0 | N/A | Direct plugin dependency; no upgrade required for JDK change |

## Derived Upgrades

- Java 21 → Java 25: latest LTS and installed runtime available.
- No Spring Boot / Jakarta / Kotlin upgrades are required for this Maven plugin project.
- Maven 3.9.11 is sufficient; no build tool or wrapper upgrade is needed.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| pom.xml | java.version | 21 | upgrade | 25 | Align project source/target to latest LTS | 
| pom.xml | maven.compiler.source | 21 | upgrade | 25 | Align source compatibility to JDK 25 | 
| pom.xml | maven.compiler.target | 21 | upgrade | 25 | Align target compatibility to JDK 25 | 
| pom.xml | maven.compiler.release | 21 | upgrade | 25 | Ensure compiler release mode targets JDK 25 |

### Source Code Changes

No source code changes are required at plan time. If compilation issues appear during execution, fix them in the corresponding Java files.

### Configuration Changes

No application configuration changes are required for the Java runtime upgrade.

### CI/CD Changes

No CI/CD files were detected that require changes in this workspace.

### Risks & Warnings

- **Minecraft/Paper runtime compatibility**: The plugin is being compiled for Java 25, but the actual server runtime may still be constrained by the Paper/Minecraft version. Verify the target deployment environment supports Java 25.
- **Build tool detection mismatch**: `#appmod-list-jdks` did not discover JDK installations, but local `java -version` confirmed JDK 25. Use the confirmed JDK path for execution.
- **No Maven wrapper**: The project depends on system Maven 3.9.11. Keep this in mind for environment consistency.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Ensure Java 25 and Maven 3.9.11 are available for the upgrade and final validation.
  - **Changes to Make**: None.
  - **Verification**: `java -version && mvn -version` using the local JDK 25 and Maven 3.9.11.

- Step 2: Setup Baseline
  - **Rationale**: A baseline build with the original Java 21 target would be ideal, but JDK 21 is not available in the environment.
  - **Changes to Make**: None.
  - **Verification**: Skipped because JDK 21 is unavailable.

- Step 3: Upgrade Java target to 25
  - **Rationale**: Update the project POM to compile against the latest LTS runtime.
  - **Changes to Make**: Apply all `pom.xml` property updates from Impact Analysis.
  - **Verification**: `mvn clean test-compile -q` using JDK 25.

- Step 4: CVE Validation & Fix
  - **Rationale**: Scan direct dependencies for known CVEs and fix any found.
  - **Changes to Make**: Update direct dependency versions only if a CVE scan reports an affected artifact.
  - **Verification**: `mvn dependency:list -DexcludeTransitive=true -q` plus `#appmod-validate-cves-for-java` scan and resolution.

- Step 5: Final Validation
  - **Rationale**: Confirm the upgraded Java runtime build and test suite succeed on Java 25.
  - **Changes to Make**: None additional beyond Step 3 and Step 4.
  - **Verification**: `mvn clean test -q` using JDK 25.
