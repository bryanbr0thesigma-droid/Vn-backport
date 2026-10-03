package com.backport.worldgen;

import com.backport.Backport;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/** Jigsaw structure with a larger allowed size than vanilla 1.20.1 (trial chambers use depth 20). */
public final class TrialChambersStructure extends Structure {
   public static final Codec<TrialChambersStructure> CODEC = RecordCodecBuilder.<TrialChambersStructure>mapCodec(i -> i.group(
      settingsCodec(i),
      StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
      Codec.intRange(0, 32).fieldOf("size").forGetter(s -> s.maxDepth),
      HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
      Codec.BOOL.fieldOf("use_expansion_hack").forGetter(s -> s.useExpansionHack),
      Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistance)
   ).apply(i, TrialChambersStructure::new)).codec();

   public static StructureType<TrialChambersStructure> TYPE;

   private final Holder<StructureTemplatePool> startPool;
   private final int maxDepth;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final int maxDistance;

   public TrialChambersStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, HeightProvider startHeight, boolean hack, int maxDistance) {
      super(settings);
      this.startPool = startPool;
      this.maxDepth = maxDepth;
      this.startHeight = startHeight;
      this.useExpansionHack = hack;
      this.maxDistance = maxDistance;
   }

   public static void register() {
      TYPE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Backport.id("trial_chambers"), () -> CODEC);
   }

   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext ctx) {
      ChunkPos chunk = ctx.chunkPos();
      int y = this.startHeight.sample(ctx.random(), new WorldGenerationContext(ctx.chunkGenerator(), ctx.heightAccessor()));
      BlockPos pos = new BlockPos(chunk.getMinBlockX(), y, chunk.getMinBlockZ());
      return JigsawPlacement.addPieces(ctx, this.startPool, Optional.empty(), this.maxDepth, pos, this.useExpansionHack, Optional.empty(), this.maxDistance);
   }

   public StructureType<?> type() {
      return TYPE;
   }
}
