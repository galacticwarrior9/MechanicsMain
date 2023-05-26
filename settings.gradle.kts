pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "MechanicsMain"

// Include every module
include(":WeaponMechanics")
include(":MechanicsCore")
include(":WeaponMechanicsPlus")
include(":BuildMechanicsCore")
include(":BuildWeaponMechanics")

include(":CoreCompatibility")
include(":WorldGuardV6")
include(":WorldGuardV7")
include(":Core_1_19_R3")

include(":WeaponCompatibility")
include(":Weapon_1_19_R3")


// All projects in the non-root directory need to have their directories updates.
project(":WorldGuardV7").projectDir = file("CoreCompatibility/WorldGuardV7")
project(":WorldGuardV6").projectDir = file("CoreCompatibility/WorldGuardV6")

project(":Core_1_19_R3").projectDir = file("CoreCompatibility/Core_1_19_R3")

project(":Weapon_1_19_R3").projectDir = file("WeaponCompatibility/Weapon_1_19_R3")