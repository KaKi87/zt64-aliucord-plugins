[![](https://shields.kaki87.net/badge/github.com-main-blue?style=flat&logo=github)](https://github.com/VibedByKaKi/zt64-aliucord-plugins)
[![](https://shields.kaki87.net/github/stars/VibedByKaKi/zt64-aliucord-plugins)](https://github.com/VibedByKaKi/zt64-aliucord-plugins)

[![](https://shields.kaki87.net/badge/git.kaki87.net-mirror-green?style=flat&logo=forgejo)](https://git.kaki87.net/VibedByKaKi/zt64-aliucord-plugins)
[![](https://shields.kaki87.net/gitea/stars/VibedByKaKi/zt64-aliucord-plugins?gitea_url=https%3A%2F%2Fgit.kaki87.net&logo=forgejo)](https://git.kaki87.net/VibedByKaKi/zt64-aliucord-plugins)

# Aliucord Plugins, maintained fork

Original `README.md` :
- [on `main` branch synced with upstream](https://github.com/VibedByKaKi/zt64-aliucord-plugins/blob/main/README.md) ;
- [on `dev` branch at fork time](https://github.com/VibedByKaKi/zt64-aliucord-plugins/blob/e25b9b12c60571e07cb742e52862a1aa247a913a/README.md) ;
- [on upstream `builds` branch](https://github.com/zt64/aliucord-plugins/blob/builds/README.md).

## Exclusive features

### CI & distribution

- **GitHub Actions builds** for every branch (`Build` on feature branches, `Deploy Dev` on `dev`, `Deploy` on `main`).
- **Dev prereleases** published to [GitHub Releases](https://github.com/VibedByKaKi/zt64-aliucord-plugins/releases) instead of pushing artifacts to git branches (`builds`, `dev-builds`).
- **Floating `dev` tag** always points at the latest `dev` build, with an **`updater.json`** for Aliucord plugin auto-updates.
- **Immutable `dev-<run_number>` prereleases** alongside the floating tag, each with its own `updater.json` pointing at that release's assets.
- **Cloud Agent environment** (`.cursor/`) for reproducible plugin builds in Cursor.

### DMCategories enhancements

Available on the `dev` branch only (not yet on `main`) :

- **Pinned DM static order** — pinned DMs keep their user-defined order instead of being sorted by last activity.
- **DM ordering setting** — choose between *Static order* and *Last activity* in plugin settings.
- **Move up / Move down** — reorder DMs inside a category from the channel context menu.

## Installation

### Dev prereleases (recommended for this fork)

From the [latest `dev` prerelease](https://github.com/VibedByKaKi/zt64-aliucord-plugins/releases/tag/dev), download individual `*.zip` plugin files and move them to `/sdcard/Aliucord/plugins` on your device.

For auto-updates, point your plugin updater at the release's `updater.json` :

```
https://github.com/VibedByKaKi/zt64-aliucord-plugins/releases/download/dev/updater.json
```

To pin a specific build, use a versioned prerelease (`dev-<run_number>`) and its matching `updater.json` from the [releases page](https://github.com/VibedByKaKi/zt64-aliucord-plugins/releases).

### Main branch builds

The `Deploy` workflow on `main` publishes build artifacts (plugin zips and `updater.json`) to [GitHub Actions](https://github.com/VibedByKaKi/zt64-aliucord-plugins/actions/workflows/deploy.yml) — download them from the latest successful run's *Artifacts* section.

## Third-party resources

- [Aliucord](https://github.com/Aliucord/Aliucord) — the Discord mobile client mod these plugins target.
- [zt64/aliucord-plugins](https://github.com/zt64/aliucord-plugins) — upstream plugin collection.
- [Aliucord plugin development docs](https://docs.aliucord.com/)
