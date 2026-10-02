# Villager News Addon Port — Fabric 1.20.1 backport

Personal backport of **Villager News Addon Port 1.3.6** (originally built for Minecraft 26.3 / Java 25)
to **Fabric 1.20.1** (Java 17).

The mod id (`villager-news-addon-port`), package (`com.vnap`) and all assets are unchanged.
The Java source was reconstructed by decompiling the release jar and then ported to the 1.20.1 APIs.

## Requirements

- Minecraft 1.20.1, Fabric Loader >= 0.14.21, Fabric API
- Entity Model Features >= 3.3.5, Entity Texture Features >= 7.2.1, Entity Sound Features >= 0.8.2
- Mod Menu (optional, for the settings screen)

## Building

Loom 1.15 needs Gradle 9.2+ (the wrapper is configured for it) and a JDK 21+; the mod itself targets Java 17.

```
./gradlew build
```

The jar is written to `build/libs/`.

## What changed in the port

- Mojang mappings, Fabric API 0.92.x networking (`FabricPacket`) instead of payload codecs.
- NBT save/load instead of `ValueInput`/`ValueOutput`; scoreboard, spawn-reason and villager-data APIs adapted.
- No render state in 1.20.1: the sign layer is a normal `RenderLayer<Villager, VillagerModel<Villager>>`.
- EMF model-part mixin adapted to the 1.20.1 `render`/`compile` signatures (float colour channels).
- Item definitions with display-context selectors do not exist in 1.20.1; held/worn models are swapped in by an `ItemRenderer` mixin.
- `handbook_held` model had free rotations (not valid in 1.20.1); they are baked into the element geometry.
- Recipe moved to `data/.../recipes/` in the 1.20.1 format.
- No "after damage" event in Fabric API 1.20.1: a `LivingEntity#hurt` mixin feeds the dialogue controller.
- Pale oak sign (not in 1.20.1) falls back to birch.

## Licence

The Villager News models, textures, sounds, dialogue and other converted assets are **not** covered by CC0 and
remain the property of their respective owners (Oreville Studios Ltd, Element Animation). This backport is for
personal use only.
