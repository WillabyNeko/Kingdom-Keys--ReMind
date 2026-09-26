package online.remind.remind.network.cts;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncPlayerData;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.dreameater.DreamEaterSummonCooldown;
import online.remind.remind.network.PacketHandlerRM;

import java.util.Objects;
import java.util.UUID;

public class CSChangeSpiritPacket implements CustomPacketPayload {

    public static final Type<CSChangeSpiritPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            KingdomKeysReMind.MODID,
                            "cs_change_spirit"
                    )
            );

    public static final StreamCodec<FriendlyByteBuf, CSChangeSpiritPacket> STREAM_CODEC =
            StreamCodec.of(
                    CSChangeSpiritPacket::encode,
                    CSChangeSpiritPacket::decode
            );

    private String rl;

    public CSChangeSpiritPacket() {
    }

    public CSChangeSpiritPacket(String id) {
        this.rl = id;
    }

    public static void encode(
            FriendlyByteBuf buffer,
            CSChangeSpiritPacket message
    ) {
        buffer.writeUtf(message.rl, 100);
    }

    public static CSChangeSpiritPacket decode(
            FriendlyByteBuf buffer
    ) {
        CSChangeSpiritPacket msg =
                new CSChangeSpiritPacket();

        msg.rl =
                buffer.readUtf(100);

        return msg;
    }

    public static void handle(
            final CSChangeSpiritPacket message,
            final IPayloadContext ctx
    ) {
        ctx.enqueueWork(() -> {

            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }

            GlobalDataRM globalData =
                    ModDataRM.getGlobal(player);

            if (globalData == null) {
                return;
            }

            String oldSpiritRL =
                    globalData.getDreamEaterRL();

            boolean changedSpirit =
                    !Objects.equals(
                            oldSpiritRL,
                            message.rl
                    );

            /*
             * If the player switches away from the currently selected
             * Dream Eater while it is summoned, remove that physical
             * summon and its Kingdom Keys party entry.
             */
            if (changedSpirit
                    && globalData.hasDreamEaterSummoned()) {

                UUID spiritUUID =
                        globalData.getDreamEaterUUID();

                if (spiritUUID != null) {

                    // Remove old Spirit from the Kingdom Keys party.
                    CSSummonSpiritPacket.removeSpiritFromParty(player, spiritUUID);

                    // Remove the actual summoned entity,
                    // regardless of which dimension it is currently in.
                    if (player.getServer() != null) {

                        for (ServerLevel level :
                                player.getServer().getAllLevels()) {

                            Entity spirit = level.getEntity(spiritUUID);

                            if (spirit != null) {
                                spirit.discard();
                                break;
                            }
                        }
                    }
                }

                /*
                 * Important:
                 * clear these before changing the selected Spirit so the
                 * DreamEaterFollowManager does not recreate anything.
                 */
                globalData.setDreamEaterUUID(null);
                globalData.setHasDreamEaterSummoned(false);
            }

            // Now change the selected Dream Eater.
            globalData.setDreamEaterRL(
                    message.rl
            );

            DreamEaterSummonCooldown.start(player);

            PacketHandler.sendTo(
                    new SCSyncPlayerData(player),
                    player
            );

            PacketHandlerRM.syncGlobalToAllAround(
                    player,
                    globalData
            );
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}