# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [1.1.2] - 2022-10-02

### Added
- simplified Chinese translation ([#4])

### Fixed
- crash with Blue Skies ([#5], [#6])

<!-- Links -->
[#4]: https://github.com/AlmostReliable/summoningrituals-forge/pull/4
[#5]: https://github.com/AlmostReliable/summoningrituals-forge/issues/5
[#6]: https://github.com/AlmostReliable/summoningrituals-forge/pull/6

## [1.1.1] - 2022-09-25

### Changed
- improved the check for the block right-click event
- block properties are now only read and written when necessary

## [1.1.0] - 2022-09-17

### Added
- an Indestructible Altar ([#1] by [wchen1990])
  - can be used for structures or central places where players have to go for rituals without being able to break it

### Changed
- altar recipes no longer require at least one input item ([#1] by [wchen1990])
  - this allows recipes with only sacrifices
- recipe sacrifices now use the default region if none is specified in the recipe ([#1] by [wchen1990])
- the active texture animation of the altar is now more seamless ([#1] by [wchen1990])

### Fixed
- incompatibility with Chisels and Bits

<!-- Links -->
[wchen1990]: https://github.com/wchen1990
[#1]: https://github.com/AlmostReliable/summoningrituals-forge/pull/1

## [1.0.0] - 2022-09-12

Initial release!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[1.1.2]: https://github.com/AlmostReliable/summoningrituals-forge/releases/tag/v1.18-1.1.2
[1.1.1]: https://github.com/AlmostReliable/summoningrituals-forge/releases/tag/v1.18-1.1.1
[1.1.0]: https://github.com/AlmostReliable/summoningrituals-forge/releases/tag/v1.18-1.1.0
[1.0.0]: https://github.com/AlmostReliable/summoningrituals-forge/releases/tag/v1.18-1.0.0
