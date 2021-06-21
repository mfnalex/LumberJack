# Changelog

## 2.4.1

- Updated config version so the auto config updater can add the new option from 2.4.0 (sorry I forgot that in 2.4.0)

## 2.4.0

- Fixed torch exploit one and for all by adding NBT Tags to falling blocks.
- Added "prevent-torch-exploit-aggressive" that will prevent the logs from breaking even if a player somehow bypasses
  the torch exploit detection.
- Torch exploit prevention works with all other blocks (slabs, snow, carpet, ...) as well!

## 2.3.6

- Updated API to 1.16.5

## 2.3.5

- Fixed nether tree detection

## 2.3.4

- Fixed support for GriefPrevention, WorldGuard and other protection plugins
- Added Dutch translation

## 2.3.3

- Added support for GriefPrevention and other protection plugins

## 2.3.2

- Fixed message "Block type not found: WARPED_STEM" (etc.) spamming console in MC versions < 1.16

## 2.3.1

- Added podzol as valid ground block

## 2.3.0

- Added support for blocks with upper-diagonal blocks like acacia or dark oak
- Added support for crimson and warped fungus trees
- Removed tree-types config option. Let me know if you need this again
- Added automatic config updater
- Improved plugin update checker
- Added Spanish and Turkish translation
- Updated Spigot API to 1.16.1
- Code cleanup