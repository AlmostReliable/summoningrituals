# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [2.0.4] - 2023-02-02

### Changed
- inventory size of the Altar is now limited to the recipe with the highest input size
  - this prevents the Altar from acting as a mass storage for items
  - the maximum input size of recipes is 64 item stacks

### Fixed
- insertion sound is no longer played when no item could be inserted into the Altar

## [2.0.3] - 2023-01-29

### Fixed
- items with secondary use in off-hand preventing item withdrawal
- duplication glitches with automation ([#14])

<!-- Links -->
[#14]: https://github.com/AlmostReliable/summoningrituals/issues/14

## [2.0.2] - 2023-01-15

### Added
- maximum input validation to recipe serializer (64 items)
- empty tag validation to recipe serializer

### Fixed
- NBT not being applied to output and input items

## [2.0.1] - 2023-01-13

### Added
- ability to waterlog the Altar blocks ([#12])

### Fixed
- offset and spread not being applied to single outputs ([#13])
- dedicated server networking crash ([#11])

<!-- Links -->
[#11]: https://github.com/AlmostReliable/summoningrituals/issues/11
[#12]: https://github.com/AlmostReliable/summoningrituals/issues/12
[#13]: https://github.com/AlmostReliable/summoningrituals/issues/13

## [2.0.0] - 2022-12-20

Initial release of the Fabric port!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[2.0.4]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-fabric-2.0.4
[2.0.3]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-fabric-2.0.3
[2.0.2]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-fabric-2.0.2
[2.0.1]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-fabric-2.0.1
[2.0.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-fabric-2.0.0
