<h1 align="center">
    <a href="https://github.com/AlmostReliable/summoningrituals"><img src=https://i.imgur.com/NDyQ5LI.png" alt="Preview" width=200></a>
    <p>Summoning Rituals</p>
</h1>

<div align="center">

A [Minecraft] mod to create custom summoning rituals for items and mobs.

[![Workflow Status][workflow_status_badge]][workflow_status_link]
[![License][license_badge]][license]

[![Version][version_badge]][version_link]
[![Total Downloads CF][total_downloads_cf_badge]][curseforge]
[![Total Downloads MR][total_downloads_mr_badge]][modrinth]

[![Discord][discord_badge]][discord]
[![Wiki][wiki_badge]][wiki]

</div>

## **📑 Information**
This is a mod for [Minecraft]-[Forge] and [Fabric].<br>

It allows packmakers to create custom summoning rituals for items and mobs.<br>
This mod does not add any recipes by default.

Summoning Rituals has a native [KubeJS] integration and also supports datapacks.

For an in-depth explanation of the mod and guides on how to create recipes, check out the [wiki].

<details>
  <summary>Preview</summary>

  https://user-images.githubusercontent.com/16513358/189552459-67e2dd38-528a-471a-9325-36b6fe7e83ff.mp4

  ```js
onEvent('recipes', event => {
    event.recipes.summoningrituals
        .altar('iron_ingot')
        .itemOutput('3x gold_ingot')
        .itemOutput('diamond')
        .mobOutput('wolf')
        .mobOutput(
            SummoningOutput.mob('blaze')
                .count(5)
                .offset(0, 3, 0)
                .spread(4, 0, 4)
                .data({ Health: 50, Attributes: [{ Name: 'generic.max_health', Base: 50 }] })
        )
        .input('64x minecraft:stone')
        .input('5x prismarine_shard')
        .input('10x amethyst_shard')
        .input(Ingredient.of('#forge:glass'))
        .sacrifice('pig', 3)
        .sacrifice('sheep')
        .sacrifice('cow')
        .sacrificeRegion(3, 3)
        .recipeTime(200)
        .blockBelow('minecraft:furnace', { lit: true })
        .weather('clear')
        .dayTime('day');
});

onEvent('summoningrituals.start', event => {
    event.level.spawnLightning(event.pos.x, event.pos.y, event.pos.z, true);
});

onEvent('summoningrituals.complete', event => {
    event.player.addXPLevels(10);
});
  ```

</details>

## **🔧 Manual Installation**
1. Download the latest **mod jar** from the [releases], from [CurseForge] or from [Modrinth].
2. Install Minecraft [Forge] or [Fabric].
3. Drop the **jar file** into your mods folder.

## **💚 Credits**
- requested by Saereth from FTB
- altar model and textures by [mo_shark]
- JEI textures by [Ne0kys]

## **⏰ Changelog**
Everything related to versions and their release notes can be found in the [changelog].

## **🎓 License**
This project is licensed under the [GNU Lesser General Public License v3.0][license].

<!-- Badges -->
[workflow_status_badge]: https://img.shields.io/github/actions/workflow/status/AlmostReliable/summoningrituals/build.yml?branch=1.19-forge&style=for-the-badge
[workflow_status_link]: https://github.com/AlmostReliable/summoningrituals/actions
[license_badge]: https://img.shields.io/github/license/AlmostReliable/summoningrituals?style=for-the-badge
[version_badge]: https://img.shields.io/badge/dynamic/json?color=0078FF&label=release&style=for-the-badge&query=name&url=https://api.razonyang.com/v1/github/tag/AlmostReliable/summoningrituals%3Fprefix=v1.19.2-
[version_link]: https://github.com/AlmostReliable/summoningrituals/releases/latest
[total_downloads_cf_badge]: https://img.shields.io/badge/dynamic/json?color=e04e14&label=CurseForge&style=for-the-badge&query=downloads.total&url=https%3A%2F%2Fapi.cfwidget.com%2F671040&logo=curseforge
[total_downloads_mr_badge]: https://img.shields.io/modrinth/dt/19smZ71v?color=5da545&label=Modrinth&style=for-the-badge&logo=modrinth
[discord_badge]: https://img.shields.io/discord/917251858974789693?color=5865f2&label=Discord&logo=discord&style=for-the-badge
[wiki_badge]: https://img.shields.io/badge/Read%20the-Wiki-ba00ff?style=for-the-badge

<!-- Links -->
[minecraft]: https://www.minecraft.net/
[discord]: https://discord.com/invite/ThFnwZCyYY
[wiki]: https://github.com/AlmostReliable/almostunified/wiki
[curseforge]: https://www.curseforge.com/minecraft/mc-mods/summoningrituals
[modrinth]: https://modrinth.com/mod/summoningrituals
[releases]: https://github.com/AlmostReliable/almostunified/releases
[forge]: http://files.minecraftforge.net/
[fabric]: https://fabricmc.net/
[mo_shark]: https://www.curseforge.com/members/mo_shark
[ne0kys]: https://www.curseforge.com/members/ne0kys
[changelog]: CHANGELOG.md
[license]: LICENSE
