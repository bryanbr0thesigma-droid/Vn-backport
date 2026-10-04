# Installing Vanilla Extended (Villager News + Backport + Pale Garden + 26.x content)

For **Fabric 1.20.1**. One mod jar; it adds the Villager News addon, the Pale Garden, and the 26.x backport content.

## What you need

| File | Where from |
| --- | --- |
| `villager-news-addon-port-1.3.6-fabric-1.20.1.jar` | this repo, `dist/` |
| Fabric API 0.92.x for 1.20.1 | https://modrinth.com/mod/fabric-api |
| Entity Model Features 3.3.9 | https://modrinth.com/mod/entity-model-features |
| Entity Texture Features 7.2.4 | https://modrinth.com/mod/entitytexturefeatures |
| Entity Sound Features 0.8.2 | https://modrinth.com/mod/esf |
| *(optional)* Fresh Animations | https://modrinth.com/resourcepack/fresh-animations |

Entity Model/Texture/Sound Features are **required**, on clients and on servers. The mod will not start without them.
Use exactly the versions in the table: newer releases (for example Entity Model Features 3.3.10) exist but have not been tested with this mod.
The `.mrpack` files below pin these exact versions for you.

## One-file install

`dist/friends-pack-pc-and-server.mrpack` and `dist/friends-pack-questcraft.mrpack` list everything above (Fabric API is
left out of the QuestCraft one, which ships its own). Import one in a launcher that supports `.mrpack` files
(Modrinth App, Prism Launcher, ATLauncher). Whether QuestCraft can import `.mrpack` files itself has not been tested; if it cannot,
copy the files by hand as below. Regenerate the packs with `python3 tools/make-mrpack.py` whenever the jar changes.

## By hand

* **PC:** put the five jars in the `mods` folder of a Fabric 1.20.1 instance.
* **QuestCraft:** put the mod jar and the three Entity Features jars in the instance's `mods` folder
  (`.../instances/1.20.1/mods`). QuestCraft already supplies Fabric API.
* **Server:** put the same five jars in the server's `mods` folder, and have every player install the same set.
* **Fresh Animations (optional):** put the `.zip` in `resourcepacks` and enable it under Options → Resource Packs.
  It is heavy; leave it off on QuestCraft if frame rate suffers. It is never switched on for you.

## Server notes

* `/gamerule locatorBar false` turns the locator bar off for everyone.
* Back up the world before adding other biome mods: the new underground biomes hook into vanilla's biome builder.
* Spawning a mannequin: `/summon backport:mannequin ~ ~ ~ {profile:{name:"SomeName"}}`.
