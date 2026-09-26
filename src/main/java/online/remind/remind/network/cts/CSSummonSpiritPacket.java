package online.remind.remind.network.cts;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.data.WorldData;
import online.kingdomkeys.kingdomkeys.lib.Party;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncWorldData;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.dreameater.DreamEater;
import online.remind.remind.dreameater.DreamEaterSummonCooldown;
import online.remind.remind.dreameater.ModDreamEaters;
import online.remind.remind.entity.spirits.CactuarSpiritEntity;
import online.remind.remind.entity.spirits.ChirithyEntity;
import online.remind.remind.entity.spirits.KomoryBatEntity;
import online.remind.remind.entity.spirits.MeowWowEntity;
import online.remind.remind.entity.spirits.TonberrySpiritEntity;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.network.PacketHandlerRM;

import java.util.UUID;

public class CSSummonSpiritPacket implements CustomPacketPayload {

    public static final Type<CSSummonSpiritPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    KingdomKeysReMind.MODID,
                    "cs_summon_spirit"
            ));

    public static final StreamCodec<FriendlyByteBuf, CSSummonSpiritPacket> STREAM_CODEC =
            StreamCodec.of(
                    CSSummonSpiritPacket::encode,
                    CSSummonSpiritPacket::decode
            );

    public CSSummonSpiritPacket() {
    }

    public static void encode(
            FriendlyByteBuf buffer,
            CSSummonSpiritPacket message
    ) {
    }

    public static CSSummonSpiritPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new CSSummonSpiritPacket();
    }


    // Summon visual effects
    private static void spawnArmorParticles(Entity spirit) {

        if (!(spirit.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 spiritPos =
                new Vec3(
                        spirit.getX(),
                        spirit.getY() + 3.5D,
                        spirit.getZ()
                );

        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                spiritPos.x,
                spiritPos.y,
                spiritPos.z,
                150,
                0.0D,
                0.0D,
                0.0D,
                0.2D
        );

        serverLevel.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                spiritPos.x,
                spiritPos.y,
                spiritPos.z,
                150,
                0.0D,
                0.0D,
                0.0D,
                0.2D
        );
    }


    // Packet handling
    public static void handle(
            final CSSummonSpiritPacket message,
            IPayloadContext ctx
    ) {

        ctx.enqueueWork(() -> {

            if (!(ctx.player() instanceof ServerPlayer owner)) {
                return;
            }

            GlobalDataRM globalData =
                    ModDataRM.getGlobal(owner);

            PlayerData kkData =
                    PlayerData.get(owner);

            if (kkData == null || globalData == null) {
                return;
            }

            if (!globalData.hasDreamEaterSummoned()
                    && globalData.getDreamEaterUUID() == null) {

                if (DreamEaterSummonCooldown.isOnCooldown(owner)) {

                    long seconds =
                            DreamEaterSummonCooldown.getRemainingSeconds(owner);

                    owner.displayClientMessage(
                            Component.literal(
                                    "You can summon your Dream Eater again in "
                                            + seconds
                                            + "s."
                            ),
                            true
                    );

                    return;
                }

                handleSummon(
                        owner,
                        kkData,
                        globalData
                );

            } else {

                handleDesummon(
                        owner,
                        globalData
                );
            }

            PacketHandlerRM.syncGlobalToAllAround(
                    owner,
                    globalData
            );
        });
    }


    // Dream Eater summoning
    private static void handleSummon(
            ServerPlayer owner,
            PlayerData kkData,
            GlobalDataRM globalData
    ) {

        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        String dreamEaterRL =
                globalData.getDreamEaterRL();

        DreamEater dreamEater;

        try {

            dreamEater =
                    ModDreamEaters.registry.get(
                            ResourceLocation.parse(
                                    dreamEaterRL
                            )
                    );

        } catch (Exception e) {

            owner.displayClientMessage(
                    Component.literal(
                            "Dream Eater data could not be found!"
                    ),
                    true
            );

            return;
        }

        if (dreamEater == null) {

            owner.displayClientMessage(
                    Component.literal(
                            "Dream Eater data could not be found!"
                    ),
                    true
            );

            return;
        }

        if (StringsRM.none.equals(
                dreamEater.getName()
        )) {

            owner.displayClientMessage(
                    Component.literal(
                            "You don't have a Dream Eater Equipped!"
                    ),
                    true
            );

            return;
        }

        if (!globalData.hasDreamEaterUnlocked(
                dreamEaterRL
        )) {

            owner.displayClientMessage(
                    Component.literal(
                            "You have not unlocked this Dream Eater yet."
                    ),
                    true
            );

            return;
        }

        LivingEntity summonedDreamEater = null;

        ChirithyEntity.removeExistingChirithy(
                serverLevel,
                owner.getUUID()
        );

        MeowWowEntity.removeExistingMeowWow(
                serverLevel,
                owner.getUUID()
        );

        KomoryBatEntity.removeExistingKomoryBat(
                serverLevel,
                owner.getUUID()
        );

        CactuarSpiritEntity.removeExistingCactuarSpirit(
                serverLevel,
                owner.getUUID()
        );

        TonberrySpiritEntity.removeExistingTonberrySpirit(
                serverLevel,
                owner.getUUID()
        );

        switch (dreamEater.getName()) {

            case StringsRM.chirithy: {

                ChirithyEntity chirithy =
                        new ChirithyEntity(
                                owner.level(),
                                owner
                        );

                chirithy.setOwnerUUID(
                        owner.getUUID()
                );

                chirithy.setPos(
                        owner.getX(),
                        owner.getY() + 2.0D,
                        owner.getZ()
                );

                int variant =
                        kkData.getAlignment()
                                != Utils.OrgMember.NONE
                                ? 0
                                : 1;

                chirithy.setVariant(
                        variant
                );

                owner.level().addFreshEntity(
                        chirithy
                );

                summonedDreamEater =
                        chirithy;

                break;
            }

            case StringsRM.meowWow: {

                MeowWowEntity meowWow =
                        new MeowWowEntity(
                                owner.level(),
                                owner
                        );

                meowWow.setOwnerUUID(
                        owner.getUUID()
                );

                meowWow.setPos(
                        owner.getX(),
                        owner.getY() + 2.0D,
                        owner.getZ()
                );

                int variant =
                        kkData.getAlignment()
                                != Utils.OrgMember.NONE
                                ? MeowWowEntity.VARIANT_ORG
                                : MeowWowEntity.VARIANT_NORMAL;

                meowWow.setVariant(
                        variant
                );

                owner.level().addFreshEntity(
                        meowWow
                );

                summonedDreamEater =
                        meowWow;

                break;
            }

            case StringsRM.komoryBat: {

                KomoryBatEntity komoryBat =
                        new KomoryBatEntity(
                                owner.level(),
                                owner
                        );

                komoryBat.setOwnerUUID(
                        owner.getUUID()
                );

                komoryBat.setPos(
                        owner.getX(),
                        owner.getY() + 2.4D,
                        owner.getZ()
                );

                int variant =
                        kkData.getAlignment()
                                != Utils.OrgMember.NONE
                                ? KomoryBatEntity.VARIANT_ORG
                                : KomoryBatEntity.VARIANT_NORMAL;

                komoryBat.setVariant(
                        variant
                );

                owner.level().addFreshEntity(
                        komoryBat
                );

                summonedDreamEater =
                        komoryBat;

                break;
            }

            case "dreameater_cactuar":
            case "cactuar": {

                CactuarSpiritEntity cactuar =
                        new CactuarSpiritEntity(
                                owner.level(),
                                owner
                        );

                cactuar.setOwnerUUID(
                        owner.getUUID()
                );

                cactuar.setPos(
                        owner.getX(),
                        owner.getY() + 1.0D,
                        owner.getZ()
                );

                owner.level().addFreshEntity(
                        cactuar
                );

                summonedDreamEater =
                        cactuar;

                break;
            }

            case "dreameater_tonberry":
            case "tonberry": {

                TonberrySpiritEntity tonberry =
                        new TonberrySpiritEntity(
                                owner.level(),
                                owner
                        );

                tonberry.setOwnerUUID(
                        owner.getUUID()
                );

                tonberry.setPos(
                        owner.getX(),
                        owner.getY() + 0.1D,
                        owner.getZ()
                );

                owner.level().addFreshEntity(
                        tonberry
                );

                summonedDreamEater =
                        tonberry;

                break;
            }

            default: {

                owner.displayClientMessage(
                        Component.literal(
                                "Unknown Dream Eater: "
                                        + dreamEater.getName()
                        ),
                        true
                );

                return;
            }
        }

        if (summonedDreamEater == null) {
            return;
        }

        globalData.setDreamEaterUUID(
                summonedDreamEater.getUUID()
        );

        globalData.setHasDreamEaterSummoned(
                true
        );

        // Register the summoned Dream Eater as a real Kingdom Keys party member.
        // This lets KK's party/friendly-fire systems recognize the Spirit.
        addSpiritToParty(owner, summonedDreamEater);

        owner.level().playSound(
                null,
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                ModSoundsRM.SPIRIT_SUMMON.get(),
                SoundSource.PLAYERS,
                0.2F,
                1.0F
        );

        spawnArmorParticles(
                summonedDreamEater
        );
    }


    // Manual Dream Eater desummoning
    private static void handleDesummon(
            ServerPlayer owner,
            GlobalDataRM globalData
    ) {

        UUID dreamEaterUUID =
                globalData.getDreamEaterUUID();

        if (dreamEaterUUID != null) {

            // Remove the summoned Spirit from the Kingdom Keys party first.
            // Do this by UUID so cleanup still works even if the entity is no longer loaded.
            removeSpiritFromParty(
                    owner,
                    dreamEaterUUID
            );

            if (owner.getServer() != null) {
                for (ServerLevel serverLevel : owner.getServer().getAllLevels()) {
                    Entity entity = serverLevel.getEntity(dreamEaterUUID);

                    if (entity != null) {
                        entity.discard();
                        break;
                    }
                }
            }
        }

        DreamEaterSummonCooldown.start(
                owner
        );

        owner.level().playSound(
                null,
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                ModSoundsRM.SPIRIT_DESUMMON.get(),
                SoundSource.PLAYERS,
                0.2F,
                1.0F
        );

        globalData.setDreamEaterUUID(
                null
        );

        globalData.setHasDreamEaterSummoned(
                false
        );
    }

    public static void removeSpiritFromParty(ServerPlayer player, UUID spiritUUID) {
        if (player == null || spiritUUID == null || player.getServer() == null) {
            return;
        }

        WorldData worldData = WorldData.get(player.getServer());
        if (worldData == null) {
            return;
        }

        Party party = worldData.getPartyFromMember(player.getUUID());
        if (party == null) {
            return;
        }

        var spiritMember = party.getMember(spiritUUID);
        if (spiritMember == null) {
            return;
        }

        // Remove the exact Party member object returned by KK.
        // This avoids needing to know the concrete Party member type here.
        if (party.getMembers().remove(spiritMember)) {
            worldData.setDirty();

            PacketHandler.sendToAll(
                    new SCSyncWorldData(player.getServer())
            );
        }
    }


    public static void addSpiritToParty(ServerPlayer player, LivingEntity spirit) {
        if (player == null || spirit == null || player.getServer() == null) {
            return;
        }

        WorldData worldData = WorldData.get(player.getServer());
        if (worldData == null) {
            return;
        }

        Party party = worldData.getPartyFromMember(player.getUUID());


        if (party == null) {
            String partyName = player.getName().getString() + "'s Party";


            party = new Party(
                    partyName,
                    player.getUUID(),
                    player.getGameProfile().getName(),
                    true,
                    (byte) 3
            );

            worldData.addParty(party);
        }

        // Prevent duplicate party entries if the packet is triggered more than once.
        if (party.getMember(spirit.getUUID()) != null) {
            return;
        }

        worldData.addPartyMember(party, spirit);

        // WorldData is server-side state, so immediately refresh it for clients.
        PacketHandler.sendToAll(
                new SCSyncWorldData(player.getServer())
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}