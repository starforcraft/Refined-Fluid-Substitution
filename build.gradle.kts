import me.modmuss50.mpp.PublishModTask

plugins {
    id("com.refinedmods.refinedarchitect.root")
    id("com.refinedmods.refinedarchitect.base")
    id("me.modmuss50.mod-publish-plugin") version "2.1.1"
}

val modVersion: String by project
val currentChangelog: String by project
val minecraftVersion: String by project

version = modVersion

val runRequiredTests by tasks.registering {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs tests required before building or publishing."

    dependsOn(":common:test")
}

tasks.withType<PublishModTask>().configureEach {
    dependsOn(runRequiredTests)
}

tasks.named("build") {
    dependsOn(runRequiredTests)
}

publishMods {
    changelog = currentChangelog
    type = STABLE

    val cfOptions = curseforgeOptions {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1654826"
        minecraftVersions.add(minecraftVersion)
        client = true
        server = true
    }

    val mrOptions = modrinthOptions {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "d0GLcSU4"
        minecraftVersions.add(minecraftVersion)
    }

    curseforge("curseforgeFabric") {
        from(cfOptions)
        file(project(":fabric"))
        modLoaders.add("fabric")
        displayName = file.map { it.asFile.name }
        requires("refined-storage", "fabric-api")
    }

    curseforge("curseforgeNeoforge") {
        from(cfOptions)
        file(project(":neoforge"))
        modLoaders.add("neoforge")
        displayName = file.map { it.asFile.name }
        requires("refined-storage")
    }

    modrinth("modrinthFabric") {
        from(mrOptions)
        file(project(":fabric"))
        modLoaders.add("fabric")
        requires("refined-storage", "fabric-api")
    }

    modrinth("modrinthNeoforge") {
        from(mrOptions)
        file(project(":neoforge"))
        modLoaders.add("neoforge")
        requires("refined-storage")
    }
}

subprojects {
    group = "com.ultramega.refinedfluidsubstitution"
}