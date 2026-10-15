# Felix's More Curios Slots

Forge 1.20.1 (47.x) + Curios API 5.x.

Consumable crystals that unlock **+1 permanent Curios slot** per use.
The limit is **per player and per slot type** (default: 2).
Limits can be configured globally or individually for each slot.

## Configuration

**Limit mode** (`limits.mode`) decides how limits are chosen:

| Mode | Slots balanced by the author | All other slots |
|---|---|---|
| `BALANCED` (default) | author's value | default (2) |
| `BALANCED_GLOBAL` | author's value | global value |
| `ALL_GLOBAL` | global value | global value |
| `ALL_DEFAULT` | 2 | 2 |

**Global value** (`limits.global`): a single number you choose, used by the modes above and by any slot set to `global`.

**Per-slot value** (`limits.per_slot.<slot>`) always takes priority over the mode. It accepts:

- a number (`0` disables that crystal),
- `default` (fixed at 2),
- `global` (uses the global value),
- `auto` (follows the selected mode; this is the initial value).

Lowering a limit never removes slots a player already unlocked and never resets their counter; it only blocks further use.
