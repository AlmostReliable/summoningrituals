# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## Unreleased
- fixed offset and spread not being applied to single ouputs
- made Altar block waterloggable

## [2.0.0] - 2022-12-20

### Added
- native REI support
- invisible mob eggs to item lookup in recipe viewers

### Changed
- lots of internal changes to make Fabric support possible
- improved performance when interacting with the altar
- improved inventory handling

### Fixed
- some bugs with automation of rituals

## [1.1.7] - 2022-11-19

### Added
- German translation

### Fixed
- crash with latest KubeJS version ([#10])
  - this bumps the minimum KubeJS version to 1902.6.0-build.119

<!-- Links -->
[#10]: https://github.com/AlmostReliable/summoningrituals/pull/10

## [1.1.6] - 2022-11-02

### Fixed
- a rare crash when an item was not resolved correctly from a recipe
- some entities being not correctly rendered in recipe lookup

## [1.1.5] - 2022-11-01

### Changed
- bumped version to the same one as the 1.18 branch to keep sync

### Fixed
- an extraction dupe bug when taking out items from the altar

## [1.0.0] - 2022-10-18

1.19 port with KubeJS v6 integration!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[2.0.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-forge-2.0.0
[1.1.7]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-1.1.7
[1.1.6]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-1.1.6
[1.1.5]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-1.1.5
[1.0.0]: https://github.com/AlmostReliable/summoningrituals/releases/tag/v1.19-1.0.0
