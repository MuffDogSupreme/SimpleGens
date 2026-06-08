# SimpleGens v1.0.0-RELEASE

A high-performance, enterprise-ready, interactive visual generator management plugin for Paper/Spigot 1.21+. Designed explicitly to handle ultra-dense GenPVP mine assets without degrading main thread tick matrices.

### 🛠️ Feature Matrix
* **Partitioned Block Restorations:** Processes large zone resets across consecutive tick loops (capped at 100 blocks/tick) to keep server MSPT perfectly low and TPS highly stable.
* **Granular Permission Tree:** Includes robust parent-child authorization nodes (`simplegens.*`, `simplegens.gens`, etc.) for strict access control.
* **Full Inventory Protection:** Implements toggleable break listeners that prevent players with full inventories from destroying static generators.
* **Interactive Dashboard Editing:** Dynamic, fully decoupled GUI panels for real-time config updates (Icons, Delays, Broadcasts).
* **Escape Input Guard:** Type `CANCEL` in uppercase inside chat sessions at any point to break out of configuration menus safely.
* **Safe Player Un-Stucking:** Uses physical blueprint tracking matrices to teleport players cleanly to regional high-ceilings during regeneration phases.

### 📦 Installation
1. Download `SimpleGens-1.0.0-RELEASE.jar` below.
2. Drop it directly into your server's `plugins/` directory.
3. Restart the network instance to generate the fully externalized `messages.yml`.
