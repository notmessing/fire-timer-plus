# Fire Timer Plus

A RuneLite plugin that displays an in-game timer over player-made fires **and** Forester's Campfires, so you know how much burn time is left before they go out.

## Features

- **Regular fires** — same behavior as the original [Fire Timer](https://github.com/autumn-smellegy/fire-timer): a countdown over each fire you light, with a color change when the fire enters its "may burn out at any moment" window.
- **Forester's Campfires** — tracks the six campfire object variants (49927–49932) introduced with the Forestry update. Detects which logs you used to light or refuel the fire, infers the remaining burn time, and clamps to the 300-tick (~3 minute) campfire cap.
- **Display toggle** — show the remaining time as raw game ticks (default) or as `m:ss`, configurable per user.

## How the campfire timer works

Forester's Campfires don't expose their remaining lifetime directly to the client. The plugin infers it by:

1. Watching `MenuOptionClicked` for "Use log on fire" actions and remembering the log type involved.
2. On `GameObjectSpawned` of a campfire (initial light) or chat-message confirmation of a successful refuel, applying the per-log-type tick gain (e.g. +3 for regular logs, +17 for willow, +38 for magic, +45 for redwood) up to the 300-tick cap.

If you walk up to a campfire someone else lit, the plugin doesn't know its starting state and falls back to a count-up display from the moment it became visible to you.

## Attribution

This plugin is adapted from [autumn-smellegy/fire-timer](https://github.com/autumn-smellegy/fire-timer), which is itself a fork of [alevine/fire-timer](https://github.com/alevine/fire-timer). The original Fire Timer plugin handles regular player-made fires; Fire Timer Plus extends it with Forester's Campfire support and a configurable display unit.

The original BSD-2-Clause license is preserved in [`LICENSE`](LICENSE).

## License

BSD-2-Clause. See [`LICENSE`](LICENSE).
