package com.inundated;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Inundated implements ModInitializer {
	public static final String MOD_ID = "inundated";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final int NEAR_DISTANCE = 128;
	private final Queue<WorldChunkTask> nearQueue = new ConcurrentLinkedQueue<>();
	private static final Set<ChunkPos> PROCESSED_CHUNKS = ConcurrentHashMap.newKeySet();
	private static boolean isActive = false;
	private static final int MAX_CHUNKS_PER_TICK = 2;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello kitty");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});

		ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			if (!isActive) return;
			queueChunkIfNearPlayer((ServerWorld) world, chunk);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!isActive || server.isStopping()) return;
			for (int i = 0; i < MAX_CHUNKS_PER_TICK && !nearQueue.isEmpty(); i++) {
				processQueue(nearQueue);
			}
		});
	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("inundated")
				.then(CommandManager.literal("start").executes(context -> {
					isActive = true;
					LOGGER.info("Inundated mod started");
					scanLoadedChunks(context.getSource().getWorld());
					return Command.SINGLE_SUCCESS;
				}))
				.then(CommandManager.literal("stop").executes(context -> {
					isActive = false;
					LOGGER.info("Inundated mod stopped");
					return Command.SINGLE_SUCCESS;
				}))
		);
	}

	private void scanLoadedChunks(ServerWorld world) {
		LOGGER.info("Scanning loaded chunks...");
		for (ServerPlayerEntity player : world.getPlayers()) {
			ChunkPos playerChunkPos = player.getChunkPos();
			for (int dx = -5; dx <= 5; dx++) {
				for (int dz = -5; dz <= 5; dz++) {
					int chunkX = playerChunkPos.x + dx;
					int chunkZ = playerChunkPos.z + dz;
					if (world.isChunkLoaded(chunkX, chunkZ)) {
						WorldChunk chunk = world.getChunk(chunkX, chunkZ);
						queueChunkIfNearPlayer(world, chunk);
					}
				}
			}
		}
	}

	private void queueChunkIfNearPlayer(ServerWorld world, WorldChunk chunk) {
		ChunkPos pos = chunk.getPos();
		if (PROCESSED_CHUNKS.contains(pos) || !world.isChunkLoaded(pos.x, pos.z)) {
			return;
		}
		if (isChunkNearPlayer(world, chunk)) {
			nearQueue.add(new WorldChunkTask(world, chunk));
		}
	}

	private void processQueue(Queue<WorldChunkTask> queue) {
		WorldChunkTask task = queue.poll();
		if (task != null && task.world.isChunkLoaded(task.chunk.getPos().x, task.chunk.getPos().z)) {
			processChunk(task.world, task.chunk);
			PROCESSED_CHUNKS.add(task.chunk.getPos());
		}
	}

	private boolean isChunkNearPlayer(ServerWorld world, WorldChunk chunk) {
		ChunkPos pos = chunk.getPos();
		int centerX = pos.getStartX() + 8;
		int centerZ = pos.getStartZ() + 8;
		for (ServerPlayerEntity player : world.getPlayers()) {
			double dx = player.getX() - centerX;
			double dz = player.getZ() - centerZ;
			if (Math.sqrt(dx * dx + dz * dz) <= NEAR_DISTANCE) {
				return true;
			}
		}
		return false;
	}

	private void processChunk(ServerWorld world, WorldChunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		LOGGER.info("Processing chunk at {}, {}", chunkPos.x, chunkPos.z);

		int replacedBlocks = 0;
		int fixedWaterlogged = 0;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = world.getBottomY(); y < world.getHeight(); y++) {
					BlockPos pos = new BlockPos(chunkPos.getStartX() + x, y, chunkPos.getStartZ() + z);
					var state = world.getBlockState(pos);
					if (state.isAir()) {
						world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
						replacedBlocks++;
					} else if (state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED)) {
						world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), 2);
						fixedWaterlogged++;
					}
				}
			}
		}
		LOGGER.info("Chunk at {}, {} processed: replaced {} air blocks, fixed {} waterlogged blocks", chunkPos.x, chunkPos.z, replacedBlocks, fixedWaterlogged);
	}
	private static class WorldChunkTask {
			public final ServerWorld world;
			public final WorldChunk chunk;

			public WorldChunkTask(ServerWorld world, WorldChunk chunk) {
				this.world = world;
				this.chunk = chunk;
			}
		}
	}
