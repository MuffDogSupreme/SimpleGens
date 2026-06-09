# SimpleGens

A high-performance, enterprise-ready, interactive visual generator management plugin for modern Minecraft server software. Engineered explicitly to handle ultra-dense GenPVP mine assets and heavy block regeneration patterns without causing main-thread Milliseconds Per Tick (MSPT) spikes or degrading tick-rate consistency (20.0 TPS).

---

## 🔗 Project Resources

[![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/plugin/simplegens)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-E9967A?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.spigotmc.org/resources/simplegens.135986/)
[![BuyMeACoffee](https://img.shields.io/badge/Buy_Me_A_Coffee-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/muffdogsupreme)

---

## 🛠️ Feature Architecture

* **Anti-MSPT Spike Partition Engine (`SimpleGensRegenTask.java`):** Large-scale structural zone resets are partitioned across consecutive server ticks. For regions exceeding 150 blocks, block placement is strictly chunked at a maximum threshold of 100 blocks per tick to stay securely within the server's 50ms tick budget.
* **Safe Player Un-Stucking Logic:** During active regeneration sequences, the engine reads the maximum elevation profile from the blueprint data stream (`highestRegionY`). Any players colliding with the reappearing block volumes are safely moved to the highest regional ceiling layer plus clearance (`highestRegionY + 1.0`).
* **Exploitation Patches & Inventory Controls (`SimpleGensBlockBreakListener.java`):** Features a configurable break-interception matrix. If `prevent-break-when-full: true` is enabled in `config.yml`, players with zero open inventory slots are blocked from mining static generator lines, preventing despawn item-drop vulnerabilities. Under static mode (< 20 ticks), item drops drop straight into inventories and scale mathematically with `FORTUNE` enchantments.
* **Intrusion Prevention (`SimpleGensBlockPlaceListener.java`):** Instantly intercepts and cancels unauthorised block placements inside active generator zones unless the operator holds explicit bypass permissions.
* **Visual Wireframe Boundaries (`ParticleManager.java`):** Real-time spatial boundaries can be rendered via client-side particle tracking structures (`Particle.DUST` or `Particle.HAPPY_VILLAGER`). Computes the precise 12 spatial edge paths of a generator's cuboid box via dedicated drawing threads, ensuring clean memory lifecycles that auto-terminate on operator disconnect or asset removal.
* **Decoupled GUI Configuration Dashboards:** A fully responsive inventory UI layer (`GeneratorIndexGUI`, `GeneratorConfigGUI`, `BlueprintViewGUI`, `ConfirmationGUI`) managed by an empty-constructor base architecture to ensure zero initialization-order crashes. Supports nested MiniMessage tag translation (`Placeholder.parsed()`) for rendering complete, styled text previews on hover.
* **Asynchronous Chat Capture Escape Trigger (`PlayerInputManager.java`):** Captures real-time text entry over target configurations using low-priority chat event loops. Operators can type the uppercase word `CANCEL` at any moment to cleanly kill the listener session, flush reference tracking maps, and safely drop back to the dashboard layout.
* **Automatic Time Unit Simplification:** The formatting subsystem automatically simplifies raw tick metrics in menu panels into clean, readable labels (`100t` auto-parses to `5s`, `2400t` parses to `2m`) when they divide perfectly, dynamically cleaning up trailing menu layout suffixes.

---

## ⚙️ Environment Specs

### Compatible Loaders
* **PaperMC (1.21+)** — *Highly Recommended for advanced asynchronous performance and modern event handling.*
* **Spigot (1.21+)**
* **Bukkit (1.21+)**

### Core Dependencies
* **WorldEdit** — *Required for region selection and structural blueprint generation.*

---

## 🔑 Permissions Reference Hierarchy

| Node | Description | Default Level |
| :--- | :--- | :--- |
| `simplegens.*` | Complete wildcard administrative bypass to all properties. | OP |
| `simplegens.help` | Grants access to display the command layout directory. | OP |
| `simplegens.reload` | Forces an immediate file reload of active data maps and assets. | OP |
| `simplegens.particle` | Toggles the active wireframe particle tracking loops. | OP |
| `simplegens.blockplace.bypass` | Authorizes placing blocks inside protected generator zones. | OP |
| `simplegens.gens` | Parent shorthand node granting all sub-permissions listed below. | False |
| `simplegens.gens.create` | Authorizes WorldEdit region definition conversions into templates. | OP |
| `simplegens.gens.remove` | Authorizes permanent deletion of generator infrastructure assets. | OP |
| `simplegens.gens.list` | Authorizes console and chat outputs of runtime performance telemetry. | OP |
| `simplegens.gens.gui` | Authorizes interactive inventory grid configuration dashboard access. | OP |
| `simplegens.gens.setmessage` | Authorizes editing real-time command-line broadcast variables. | OP |
| `simplegens.gens.togglebroadcast` | Authorizes toggling live global broadcast flags. | OP |

---

## 📦 Installation Workflow

1. Download the latest compiled production asset: `SimpleGens-1.0.1-Release.jar`.
2. Ensure you have a matching version of **WorldEdit** loaded in your environment.
3. Place the `.jar` asset directly inside your network file structure under the `/plugins/` directory.
4. Restart your server instance to instantiate default configuration profiles and unpack the externalized `messages.yml` localization file.
5. Customise your localization metrics inside `messages.yml` utilizing modern Kyori MiniMessage formatting nodes.

---

## 📊 Analytics & Telemetry

* **Current Status:** Initial Stable Release Track
* **Metric Framework:** Built on lightweight tracking components designed for maximum code visibility without degrading performance profiles. All threads run asynchronously or use ticking queues to maintain zero main-thread impact.

---

## 🔄 Version Changelog History

### [v1.0.1-Release]
#### 🚀 Added
* **Chat Session Cancel Escape Trigger:** Integrated an uppercase `CANCEL` monitoring hook in `PlayerInputManager.java` to abort active text tracking safely.
* **Automatic Time Unit Simplification:** Added a time formatter to convert clean intervals into human-readable notation (`100t` -> `5s`), eliminating visual clutter in configuration panels.
* **Full-Inventory Safeguard Interception:** Implemented a storage capacity check inside `SimpleGensBlockBreakListener.java` to halt mining when a player's inventory fills up, preventing despawn item loss.

#### 🐛 Fixed
* **Scheduler Double-Loop Interval Execution:** Patched a core scheduling bug in `SimpleGensRegenTask.java` that forced block restoration loops to run at exactly half-speed, restoring accurate tick synchronization.
* **Nested Hover Lore Tag Rendering:** Swapped hover processing configurations in `GeneratorConfigGUI.java` to use modern `Placeholder.parsed()` tags, allowing color nodes to render properly inside text boxes.
* **Global Variable Matrix Reconciliation:** Fixed broken context keys (`<broadcast_status>` vs `status`) within `GeneratorIndexGUI.java` to restore correct dynamic live tracking displays.
* **YAML Scanner Escape Violations:** Cleaned up layout double-backslash instances inside `messages.yml` to satisfy strict SnakeYAML parsing boundaries and avoid console boot errors.

### [v1.0.0-Release]
* Core initial release tracking infrastructure, including chunked generation loops, base configuration models, permissions layout tree, inventory protection systems, and basic interactive editing menus.
