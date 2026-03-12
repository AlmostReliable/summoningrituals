# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

- added German translation
- added block below condition
- added facing condition
- added light level condition
- added smoked condition
- added waterlogged condition
- fixed item and block names being untranslated

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
[3.2.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.2.1
[3.2.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.2.0
[3.1.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.1.0
[3.0.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.21.1-neoforge-3.0.0
