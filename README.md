# Bamboo Farm Architect — Fabric 26.1 source project

**Target:** Minecraft Java 26.1, Fabric Loader 0.18.4, Fabric Loom 1.15, Fabric API 0.145.0+26.1, Gradle 9.4.0, Java 25.

## Current features
- `/bfa start` records your current position as the excavation origin.
- `/bfa size <width 3-512> <length 3-512> <depth 1-128>` sets the excavation dimensions.
- `/bfa plan` estimates volume and the number of 3x3 drill positions.
- `/bfa next` records a completed drill-position checkpoint.
- `/bfa pause`, `/bfa resume`, `/bfa status`, `/bfa help`.
- Reminds you to inspect sand, red sand, gravel, anvils, water, lava, powder snow, caves and unsupported ceilings.

## Important limitations
This is a client-side planning/checkpoint assistant, not an autonomous miner or farm builder. It does not automatically break/place blocks, scan every block for falling/fluid hazards, operate a server-specific drill, or construct the bamboo farm. Those features depend on the drill mod's API and server rules.

## Build locally
Use a Java 25 JDK. From this folder run `gradle build` with Gradle 9.4.0, or use the included GitHub Actions workflow to build on a hosted runner. The 26.1 toolchain uses Mojang's official names; Yarn mappings are intentionally not configured.

## Build using GitHub Actions
1. Create a GitHub repository and upload the contents of this ZIP.
2. Open the repository's **Actions** tab and enable workflows if prompted.
3. Run the **Build Fabric mod** workflow, or push a commit.
4. Open the completed workflow run and download the `bamboo-farm-architect` artifact. The JAR will be inside that ZIP.

The project is configured against versions listed by Fabric's official Maven repository and 26.1 guidance, but it has not been compiled or runtime-tested in this environment.
