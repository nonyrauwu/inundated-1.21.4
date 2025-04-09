package com.inundated.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.StructureAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.class)
public class FloodedChunkGeneratorMixin {

    @Inject(method = "populateEntities", at = @At("TAIL"))
    private void onPopulateEntities(ChunkRegion region, CallbackInfo ci) {
        Chunk centerChunk = region.getChunk(region.getCenterPos().x, region.getCenterPos().z);
        floodChunk(centerChunk);
    }

    @Inject(method = "buildSurface", at = @At("HEAD"))
    private void onBuildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk, CallbackInfo ci) {
        if (region.toServerWorld().getRegistryKey() == World.END) {
            floodChunk(chunk);
        }
    }

    private void floodChunk(Chunk chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int minY = chunk.getBottomY();
        int maxY = chunk.getHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.isAir()) {
                        chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                    } else if (state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED)) {
                        chunk.setBlockState(pos, state.with(Properties.WATERLOGGED, true), false);
                    }
                }
            }
        }
    }
}
