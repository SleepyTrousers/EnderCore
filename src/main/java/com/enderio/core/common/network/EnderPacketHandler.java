package com.enderio.core.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnderPacketHandler {
  private static SimpleChannel INSTANCE;
  private static int ID = 0;

  public static void init() {
    registerClientMessage(PacketProgress.class, PacketProgress::toBytes, PacketProgress::new, PacketProgress::handle);
    registerServerMessage(PacketGhostSlot.class, PacketGhostSlot::toBytes, PacketGhostSlot::new, PacketGhostSlot::handle);
  }

  protected static <T> void registerClientMessage(Class<T> type, BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer, T> decoder,
                                                                       BiConsumer<T, Supplier<NetworkEvent.Context>> consumer) {
    INSTANCE.registerMessage(ID++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }

  protected static <T> void registerServerMessage(Class<T> type, BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer, T> decoder,
                                                  BiConsumer<T, Supplier<NetworkEvent.Context>> consumer) {
    INSTANCE.registerMessage(ID++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
  }

  public static void sendToAllTracking(IPacket<?> message, TileEntity te) {
    sendToAllTracking(message, te.getWorld(), te.getPos());
  }

  // Credit: https://github.com/mekanism/Mekanism/blob/0287e5fd48a02dd8fe0b7a474c766d6c3a8d3f01/src/main/java/mekanism/common/network/BasePacketHandler.java#L150
  public static void sendToAllTracking(IPacket<?> packet, World world, BlockPos pos) {
    if (world instanceof ServerWorld) {
      ((ServerWorld) world).getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(packet, p));
    } else{
      INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(pos.getX() >> 4, pos.getZ() >> 4)), packet);
    }
  }

  public static <T> void sendTo(T packet, ServerPlayerEntity player) {
    INSTANCE.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
  }

  public static <T> void sendToServer(T packet) {
    INSTANCE.sendToServer(packet);
  }
}
