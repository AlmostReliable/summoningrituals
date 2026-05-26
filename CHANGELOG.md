# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

- fixed class loading issues when KubeJS is not installed causing recipe viewers to break

## [3.14.0] - 2026-05-25

- added config option to register block pattern entries as usages in recipe viewers, enabled by default
- fixed recipe matching logic if multiple input items are the same

## [3.13.3] - 2026-05-17

- fixed crash when KubeJS is not installed

## [3.13.2] - 2026-05-04

- added localization support for biomes in biome conditions
- changed animation calculation base from system nano time to Minecraft's DeltaTracker

## [3.13.1] - 2026-04-28

- improved type info for KubeRecipe builder methods returning the same instance
- improved Altar animation smoothness when player moves
- improved Altar item orbit vertical shift smoothness
- improved recipe progress calculation for Altar animations
- replaced functional interface functions with same type parameters with unary operators
- fixed block entity not being removed when breaking the Altar
- fixed players being able to retrieve the catalyst while a ritual is running
- fixed jittery item animation when ritual is in progress
- fixed jittery item orbit animation when client performance is low
- fixed different Altar animations interfering with each other

## [3.13.0] - 2026-04-27

- added event methods to highlight absolute block positions and rotation-aware offsets
- added event to modify the conditions tooltip in recipe viewers

## [3.12.1] - 2026-04-26

- fixed exception when trying to find suitable recipe when interacting with the Altar ([ATM10#543](https://github.com/AllTheMods/All-the-Mons/issues/543))

## [3.12.0] - 2026-04-19

- added Pattern Generator dev item to automatically generate patterns from block selections
- added Portuguese localization ([#36](https://github.com/AlmostReliable/summoningrituals/pull/36))

## [3.11.1] - 2026-04-18

- added utility method for obtaining the position in summoning KubeJS events
- fixed level property not being accessible in summoning KubeJS events

## [3.11.0] - 2026-04-17

- added Altar block entity property to summoning KubeJS events
- added utility functions for block patterns in summoning KubeJS events
- fixed functions with query strings in block pattern builders not being callable
- fixed no easy way of obtaining the transformed block pattern
- changed function names of query string functions to be prefixed with `queryable`

## [3.10.0] - 2026-04-15

Note: This release reworks the block pattern condition. It's no longer part of the main condition system. Refer to the wiki for an updated syntax.

- added rotation awareness to the block pattern condition
- added option to add optional tooltip lines to the block pattern condition
- added block pattern preview cancellation when player is too far away from the Altar
- added a dedicated failure message if the block pattern is not correct when starting the ritual
- added highlights for wrong blocks when trying to start a ritual
- added option to add an optional block pattern extension that can be used to modify logic on additional blocks not required for the ritual to work
- add config option to make block pattern preview checks block state aware
- improved type info for KubeJS builder methods returning the same instance
- moved recipe inputs and outputs to container classes for better maintainability
- changed the component column count in block pattern tooltip from 4 to 8
- changed inner key of moon phase condition from `phase` to `moon_phase`
- changed some recipe component key to optionals, so they can be omitted from the JSON
- decoupled the block pattern condition from the loot condition system
- removed the utility methods for obtaining the block pattern condition in favor of the dedicated field in the recipe

## [3.9.0] - 2026-04-13

- added utility method to recipe and recipe container to expose block pattern condition
- added optional query id to block pattern entries
- added block pattern preview rendering support for block entities using the `ENTITYBLOCK_ANIMATED` render type
- added early block pattern preview cancellation when the Altar is destroyed or on reload
- fixed block pattern preview task leaking memory when player leaves the world while it's still running
- fixed block pattern preview not working on the indestructible Altar
- replaced internal AnyOf loot condition with a proper custom condition to check for Altar properties

## [3.8.0] - 2026-04-09

- added block pattern xray highlights for incorrect blocks
- added feedback message for wrong blocks in patterns
- added feedback message and early preview cancellation if the pattern is complete
- added new config options to customize the block pattern preview duration
- added option to add a custom name to the block pattern condition
- fixed log spam when a custom ritual renderer is erroring
- fixed NPE when using entity inputs and fake entity inputs with custom validators
- changed pattern preview icon from Jigsaw to Structure Block

## [3.7.1] - 2026-04-09

- fixed fake entity inputs not supporting wildcard counts

## [3.7.0] - 2026-04-09

- added client config option to disable candle particle rendering (useful for custom models)
- added support for registering custom ritual renderers
- added support for adding fake entity inputs with custom predicates
- added support for adding display outputs if the outputs are handled by events
- fixed input item stacks being modified in the JEI recipe category

## [3.6.0] - 2026-04-04

- added small indent to condition values in condition display
- added block pattern condition with in-world preview
- removed block below condition in favor of block pattern condition

## [3.5.0] - 2026-04-01

- added moon phase condition ([#26](https://github.com/AlmostReliable/summoningrituals/issues/26))
- fixed item names being untranslated
- moved lang entries to central file

## [3.4.1] - 2026-03-18

- added type wrapper for `EntityInfo` to allow for easier entity definition syntax in bindings
- added source line reporting for `CommandOutput` type wrapper

## [3.4.0] - 2026-03-17

- added option to add custom data validators to entity inputs to manually check NBT
- added aliases to recipe components
- added `SummoningTime` binding to use pre-defined time values as conditions
- fixed recipe info container properties not being exposed as beans in events

## [3.3.0] - 2026-03-12

- added German translation
- added block below condition
- added facing condition
- added light level condition
- added smoked condition
- added waterlogged condition
- fixed item and block names being untranslated
- fixed recipe not rendering in recipe viewers if an entity can't be measured ([#35](https://github.com/AlmostReliable/summoningrituals/issues/35))

## [3.2.1] - 2025-11-16

- fixed startup crashing due to accessing a private method

## [3.2.0] - 2025-11-16

- added support for KubeJS 7.2
- added support for EMI
- added overload in item binding to specify count directly
- added particle effect when entity input is sacrificed
- improved entity wrapper error message when non-existing id is provided
- improved entity ingredient and renderer
  - bookmark renderer no longer displays count or entity data
  - bookmark serialization now omits values not needed for persisting
  - renderer now measures the entity bounds in the origin position allowing for much better y offset calculation
  - measurements now span across 40 render ticks to track height changes in animations
  - bookmark and output renderers now use scissor mask to avoid clipping
  - fixed rotation of entities using an inverted model (ender dragon, bats, etc.)
- renamed catalyst to initiator

## [3.1.0] - 2025-11-08

- added a condition display to the recipe viewer integration
- added custom condition builder to KubeJS integration to avoid exposing loot condition builders
- added height condition
- added localization for specific condition values
- added ability to add data to input entities for rendering purposes
- added ability to add custom tooltips to entities to display in recipe viewers
- added command outputs with custom tooltips, player context and recipe viewer integration
- added logic to make condition ids more readable
- improved error codes in KubeJS log and add source lines
- improved entity size calculation to improve display in recipe viewers
- fixed recipe viewer integration not showing accepted tags for ingredients
- switched back to NeoForge config system
- refactored condition system to be more modular

## [3.0.0] - 2025-10-27

Initial 1.21.1 release!

This version is highly experimental and should not be used in production. This alpha release was published for internal testing. The wiki
is still in the works until the final release because many things are subject to change.

<!-- Versions -->
[3.14.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.14.0
[3.13.3]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.13.3
[3.13.2]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.13.2
[3.13.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.13.1
[3.13.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.13.0
[3.12.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.12.1
[3.12.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.12.0
[3.11.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.11.1
[3.11.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.11.0
[3.10.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.10.0
[3.9.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.9.0
[3.8.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.8.0
[3.7.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.7.1
[3.7.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.7.0
[3.6.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.6.0
[3.5.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.5.0
[3.4.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.4.1
[3.4.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.4.0
[3.3.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.3.0
[3.2.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.2.1
[3.2.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.2.0
[3.1.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.1.0
[3.0.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.0.0
