package online.remind.remind.network.stc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SCSyncGlobalCapabilityToAllPacketRM implements CustomPacketPayload {

    public static final Type<SCSyncGlobalCapabilityToAllPacketRM> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "sc_sync_global_capability_to_all"));

    public static final StreamCodec<FriendlyByteBuf, SCSyncGlobalCapabilityToAllPacketRM> STREAM_CODEC =
            StreamCodec.of(SCSyncGlobalCapabilityToAllPacketRM::encode, SCSyncGlobalCapabilityToAllPacketRM::decode);

    public int id;

    public int berserkLvl;
    public int berserkTicks;
    public int prestige;
    public int strBonus;
    public int magBonus;
    public int defBonus;
    public int NGPlusWarriorCount;
    public int NGPlusMysticCount;
    public int NGPlusGuardianCount;
    public int stepTicks;
    public int riskchargeCount;
    public int autoLife;
    public int rcCooldown;
    public int sCooldown;
    public int styleTicks;
    public int strPanel;
    public int magPanel;
    public int defPanel;
    public int panelsStatus;
    public int ngpStatus;
    public int dreamEaterSummonCooldown;

    public String dreamEaterRL = "";
    public String style = "";

    public boolean donorGiven;
    public boolean darkMode;
    public boolean dreamEaterSummoned;

    public byte stepType;

    public UUID dreamEaterUUID;

    public double situationValue;

    public LinkedHashMap<String, Integer> learnedMagics = new LinkedHashMap<>();
    public List<String> unlockedDreamEaters = new ArrayList<>();

    public LinkedHashMap<String, Integer> dreamEaterLevels = new LinkedHashMap<>();
    public LinkedHashMap<String, Integer> dreamEaterExp = new LinkedHashMap<>();

    public SCSyncGlobalCapabilityToAllPacketRM() {
    }

    public SCSyncGlobalCapabilityToAllPacketRM(int id, GlobalDataRM capability) {
        this.id = id;

        this.berserkLvl = capability.getBerserkLevel();
        this.berserkTicks = capability.getBerserkTicks();

        this.prestige = capability.getPrestigeLvl();
        this.strBonus = capability.getSTRBonus();
        this.magBonus = capability.getMAGBonus();
        this.defBonus = capability.getDEFBonus();

        this.NGPlusWarriorCount = capability.getNGPWarriorCount();
        this.NGPlusMysticCount = capability.getNGPMysticCount();
        this.NGPlusGuardianCount = capability.getNGPGuardianCount();

        this.stepTicks = capability.getStepTicks();
        this.stepType = capability.getStepType();

        this.riskchargeCount = capability.getRiskchargeCount();
        this.autoLife = capability.getAutoLifeActive();

        this.rcCooldown = capability.getRCCooldownTicks();
        this.sCooldown = capability.getSCooldownTicks();

        this.strPanel = capability.getSTRPanel();
        this.magPanel = capability.getMAGPanel();
        this.defPanel = capability.getDEFPanel();
        this.panelsStatus = capability.getPanelsEnabled();
        this.ngpStatus = capability.getNGPEnabled();

        this.donorGiven = capability.getDonorGiven();
        this.darkMode = capability.isDarkMode();

        this.dreamEaterSummoned = capability.hasDreamEaterSummoned();
        this.dreamEaterUUID = capability.getDreamEaterUUID();
        this.dreamEaterRL = capability.getDreamEaterRL();
        this.dreamEaterSummonCooldown = capability.getDreamEaterSummonCooldownTicks();

        this.style = capability.getStyle();
        this.situationValue = capability.getSituationValue();
        this.styleTicks = capability.getStyleTicks();

        this.learnedMagics = new LinkedHashMap<>(capability.getLearndedMagics());
        this.unlockedDreamEaters = new ArrayList<>(capability.getUnlockedDreamEaters());

        this.dreamEaterLevels = new LinkedHashMap<>(capability.getDreamEaterLevels());
        this.dreamEaterExp = new LinkedHashMap<>(capability.getDreamEaterExpMap());
    }

    public static void encode(FriendlyByteBuf buffer, SCSyncGlobalCapabilityToAllPacketRM message) {
        buffer.writeInt(message.id);

        buffer.writeInt(message.berserkLvl);
        buffer.writeInt(message.berserkTicks);

        buffer.writeInt(message.prestige);
        buffer.writeInt(message.strBonus);
        buffer.writeInt(message.magBonus);
        buffer.writeInt(message.defBonus);

        buffer.writeInt(message.NGPlusWarriorCount);
        buffer.writeInt(message.NGPlusMysticCount);
        buffer.writeInt(message.NGPlusGuardianCount);

        buffer.writeInt(message.stepTicks);
        buffer.writeByte(message.stepType);

        buffer.writeInt(message.riskchargeCount);
        buffer.writeInt(message.autoLife);

        buffer.writeInt(message.rcCooldown);
        buffer.writeInt(message.sCooldown);

        buffer.writeInt(message.strPanel);
        buffer.writeInt(message.magPanel);
        buffer.writeInt(message.defPanel);
        buffer.writeInt(message.panelsStatus);
        buffer.writeInt(message.ngpStatus);

        buffer.writeBoolean(message.donorGiven);
        buffer.writeBoolean(message.darkMode);

        buffer.writeBoolean(message.dreamEaterSummoned);
        buffer.writeUtf(safeString(message.dreamEaterRL), 512);

        if (message.dreamEaterUUID != null) {
            buffer.writeBoolean(true);
            buffer.writeUUID(message.dreamEaterUUID);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeInt(message.dreamEaterSummonCooldown);

        buffer.writeUtf(safeString(message.style), 512);
        buffer.writeDouble(message.situationValue);
        buffer.writeInt(message.styleTicks);

        writeStringIntMap(buffer, message.learnedMagics, 512, 512);
        writeStringList(buffer, message.unlockedDreamEaters, 64, 128);

        writeStringIntMap(buffer, message.dreamEaterLevels, 128, 128);
        writeStringIntMap(buffer, message.dreamEaterExp, 128, 128);
    }

    public static SCSyncGlobalCapabilityToAllPacketRM decode(FriendlyByteBuf buffer) {
        SCSyncGlobalCapabilityToAllPacketRM msg = new SCSyncGlobalCapabilityToAllPacketRM();

        msg.id = buffer.readInt();

        msg.berserkLvl = buffer.readInt();
        msg.berserkTicks = buffer.readInt();

        msg.prestige = buffer.readInt();
        msg.strBonus = buffer.readInt();
        msg.magBonus = buffer.readInt();
        msg.defBonus = buffer.readInt();

        msg.NGPlusWarriorCount = buffer.readInt();
        msg.NGPlusMysticCount = buffer.readInt();
        msg.NGPlusGuardianCount = buffer.readInt();

        msg.stepTicks = buffer.readInt();
        msg.stepType = buffer.readByte();

        msg.riskchargeCount = buffer.readInt();
        msg.autoLife = buffer.readInt();

        msg.rcCooldown = buffer.readInt();
        msg.sCooldown = buffer.readInt();

        msg.strPanel = buffer.readInt();
        msg.magPanel = buffer.readInt();
        msg.defPanel = buffer.readInt();
        msg.panelsStatus = buffer.readInt();
        msg.ngpStatus = buffer.readInt();

        msg.donorGiven = buffer.readBoolean();
        msg.darkMode = buffer.readBoolean();

        msg.dreamEaterSummoned = buffer.readBoolean();
        msg.dreamEaterRL = buffer.readUtf(512);

        if (buffer.readBoolean()) {
            msg.dreamEaterUUID = buffer.readUUID();
        } else {
            msg.dreamEaterUUID = null;
        }
        msg.dreamEaterSummonCooldown = buffer.readInt();

        msg.style = buffer.readUtf(512);
        msg.situationValue = buffer.readDouble();
        msg.styleTicks = buffer.readInt();

        msg.learnedMagics = readStringIntMap(buffer, 512, 512);

        msg.unlockedDreamEaters = new ArrayList<>();
        if (buffer.readableBytes() > 0) {
            msg.unlockedDreamEaters = readStringList(buffer, 64, 128);
        }

        msg.dreamEaterLevels = new LinkedHashMap<>();
        if (buffer.readableBytes() > 0) {
            msg.dreamEaterLevels = readStringIntMap(buffer, 128, 128);
        }

        msg.dreamEaterExp = new LinkedHashMap<>();
        if (buffer.readableBytes() > 0) {
            msg.dreamEaterExp = readStringIntMap(buffer, 128, 128);
        }

        return msg;
    }

    public static void handle(final SCSyncGlobalCapabilityToAllPacketRM message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() == null || ctx.player().level() == null) {
                return;
            }

            Entity entity = ctx.player().level().getEntity(message.id);

            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            GlobalDataRM globalData = ModDataRM.getGlobal(livingEntity);

            if (globalData == null) {
                return;
            }

            globalData.setBerserkTicks(message.berserkTicks, message.berserkLvl);

            globalData.setPrestigeLvl(message.prestige);
            globalData.setSTRBonus(message.strBonus);
            globalData.setMAGBonus(message.magBonus);
            globalData.setDEFBonus(message.defBonus);

            globalData.setNGPWarriorCount(message.NGPlusWarriorCount);
            globalData.setNGPMysticCount(message.NGPlusMysticCount);
            globalData.setNGPGuardianCount(message.NGPlusGuardianCount);

            globalData.setStepTicks(message.stepTicks, message.stepType);
            globalData.setRiskchargeCount(message.riskchargeCount);
            globalData.setAutoLifeActive(message.autoLife);

            globalData.setRCCooldownTicks(message.rcCooldown);
            globalData.setSCooldownTicks(message.sCooldown);

            globalData.setSTRPanel(message.strPanel);
            globalData.setMAGPanel(message.magPanel);
            globalData.setDEFPanel(message.defPanel);
            globalData.setPanelsEnabled(message.panelsStatus);
            globalData.setNGPEnabled(message.ngpStatus);

            globalData.setDonorGiven(message.donorGiven);
            globalData.setDarkMode(message.darkMode);

            globalData.setHasDreamEaterSummoned(message.dreamEaterSummoned);
            globalData.setDreamEaterUUID(message.dreamEaterUUID);
            globalData.setDreamEaterRL(message.dreamEaterRL);

            globalData.setStyle(message.style);
            globalData.setSituationValue(message.situationValue);
            globalData.setStyleTicks(message.styleTicks);

            globalData.setLearnedMagics(message.learnedMagics);
            globalData.setUnlockedDreamEaters(message.unlockedDreamEaters);
            globalData.setDreamEaterProgress(message.dreamEaterLevels, message.dreamEaterExp);
            globalData.setDreamEaterSummonCooldownTicks(message.dreamEaterSummonCooldown);
        });
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    private static void writeStringList(
            FriendlyByteBuf buffer,
            List<String> list,
            int maxEntries,
            int maxStringLength
    ) {
        if (list == null || list.isEmpty()) {
            buffer.writeInt(0);
            return;
        }

        List<String> cleaned = new ArrayList<>();

        for (String value : list) {
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (value.length() > maxStringLength) {
                continue;
            }

            cleaned.add(value);

            if (cleaned.size() >= maxEntries) {
                break;
            }
        }

        buffer.writeInt(cleaned.size());

        for (String value : cleaned) {
            buffer.writeUtf(value, maxStringLength);
        }
    }

    private static List<String> readStringList(
            FriendlyByteBuf buffer,
            int maxEntries,
            int maxStringLength
    ) {
        List<String> list = new ArrayList<>();

        int count = Math.min(buffer.readInt(), maxEntries);

        for (int i = 0; i < count; i++) {
            String value = buffer.readUtf(maxStringLength);

            if (value != null && !value.isEmpty()) {
                list.add(value);
            }
        }

        return list;
    }

    private static void writeStringIntMap(
            FriendlyByteBuf buffer,
            LinkedHashMap<String, Integer> map,
            int maxEntries,
            int maxStringLength
    ) {
        if (map == null || map.isEmpty()) {
            buffer.writeInt(0);
            return;
        }

        LinkedHashMap<String, Integer> cleaned = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();

            if (key == null || key.isEmpty()) {
                continue;
            }

            if (key.length() > maxStringLength) {
                continue;
            }

            cleaned.put(key, entry.getValue());

            if (cleaned.size() >= maxEntries) {
                break;
            }
        }

        buffer.writeInt(cleaned.size());

        for (Map.Entry<String, Integer> entry : cleaned.entrySet()) {
            buffer.writeUtf(entry.getKey(), maxStringLength);
            buffer.writeInt(entry.getValue());
        }
    }

    private static LinkedHashMap<String, Integer> readStringIntMap(
            FriendlyByteBuf buffer,
            int maxEntries,
            int maxStringLength
    ) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

        int count = Math.min(buffer.readInt(), maxEntries);

        for (int i = 0; i < count; i++) {
            String key = buffer.readUtf(maxStringLength);
            int value = buffer.readInt();

            if (key != null && !key.isEmpty()) {
                map.put(key, value);
            }
        }

        return map;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}