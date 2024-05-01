package net.kyrptonaught.diggusmaximus;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class StartExcavatePacket {
    private static final CustomPayload.Id<StartExcavatePacketPayload> START_EXCAVATE_PACKET = new CustomPayload.Id<>(new Identifier(DiggusMaximusMod.MOD_ID, "start_excavate_packet"));

    static void registerReceivePacket() {
        PayloadTypeRegistry.playC2S().register(START_EXCAVATE_PACKET, new PacketCodec<RegistryByteBuf, StartExcavatePacketPayload>() {
            @Override
            public StartExcavatePacketPayload decode(RegistryByteBuf buf) {
                return new StartExcavatePacketPayload(buf.readBlockPos(), buf.readIdentifier(), buf.readInt(), buf.readInt());
            }

            @Override
            public void encode(RegistryByteBuf buf, StartExcavatePacketPayload value) {
                buf.writeBlockPos(value.getBlockPos());
                buf.writeIdentifier(value.getBlockID());
                buf.writeInt(value.getFacingId());
                buf.writeInt(value.getShapeSelection());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(START_EXCAVATE_PACKET, new ServerPlayNetworking.PlayPayloadHandler<>() {
            @Override
            public void receive(StartExcavatePacketPayload payload, ServerPlayNetworking.Context context) {
                BlockPos blockPos = payload.getBlockPos();
                Identifier blockID = payload.getBlockID();
                int facingID = payload.getFacingId();
                Direction facing = facingID == -1 ? null : Direction.byId(facingID);
                int shapeKey = payload.getShapeSelection();
                context.player().server.execute(() -> {
                    if (DiggusMaximusMod.getOptions().enabled) {
                        if (blockPos.isWithinDistance(context.player().getPos(), 10)) {
                            new Excavate(blockPos, blockID, context.player(), facing).startExcavate(shapeKey);
                        }
                    }
                });
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static void sendExcavatePacket(BlockPos blockPos, Identifier blockID, Direction facing, int shapeSelection) {
        ClientPlayNetworking.send(new StartExcavatePacketPayload(blockPos, blockID, facing == null ? -1 : facing.getId(), shapeSelection));
    }

    public static class StartExcavatePacketPayload implements CustomPayload {

        private final BlockPos blockPos;
        private final Identifier blockID;
        private final int facingId;
        private final int shapeSelection;


        public StartExcavatePacketPayload(BlockPos blockPos, Identifier blockID, int facingId, int shapeSelection) {
            this.blockPos = blockPos;
            this.blockID = blockID;
            this.facingId = facingId;
            this.shapeSelection = shapeSelection;

        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public Identifier getBlockID() {
            return blockID;
        }

        public int getShapeSelection() {
            return shapeSelection;
        }

        public int getFacingId() {
            return facingId;
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return START_EXCAVATE_PACKET;
        }
    }
}