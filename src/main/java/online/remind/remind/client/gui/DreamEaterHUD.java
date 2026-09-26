package online.remind.remind.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import online.kingdomkeys.kingdomkeys.client.gui.elements.HUD.HUDElement;
import online.remind.remind.client.ClientUtilsRM;
import online.remind.remind.config.ModConfigs;
import online.remind.remind.dreameater.DreamEaterSummonCooldown;

public class DreamEaterHUD extends OverlayBaseRM {

    public static final DreamEaterHUD INSTANCE = new DreamEaterHUD();

    private static final int BAR_WIDTH = 74;
    private static final int BAR_HEIGHT = 7;
    private static final int MAX_COOLDOWN_TICKS = 20 * 30;

    private DreamEaterHUD() {
        super();
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        super.render(guiGraphics, deltaTracker);

        Player player = minecraft.player;
        if (player == null) return;
        if (!DreamEaterSummonCooldown.isOnCooldown(player)) return;

        int maxCooldownTicks = ModConfigs.summonCooldown * 20;
        int remainingTicks = DreamEaterSummonCooldown.getRemainingTicks(player);

        if (maxCooldownTicks <= 0 || remainingTicks <= 0) return;

        float progress = Math.min(1.0F, remainingTicks / (float) maxCooldownTicks);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        HUDElement element = ClientUtilsRM.DREAM_EATER_ELEMENT;
        element.applyTransform(guiGraphics, screenWidth, screenHeight);

        renderCooldownBar(guiGraphics, progress);

        element.endTransform(guiGraphics);
    }

    private void renderCooldownBar(GuiGraphics gui, float progress) {
        int x = -50;
        int y = 15;

        gui.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xAA160A24);
        gui.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, 0xFF2A1638);

        int fillWidth = Math.round((BAR_WIDTH - 2) * progress);
        if (fillWidth <= 0) return;

        int innerX = x + 1;
        int innerY = y + 1;
        int innerHeight = BAR_HEIGHT - 2;

        for (int i = 0; i < fillWidth; i++) {
            float t = fillWidth <= 1 ? 0.0F : i / (float) (fillWidth - 1);

            int r = (int) (255 + (177 - 255) * t);
            int g = (int) (92 + (66 - 92) * t);
            int b = (int) (190 + (255 - 190) * t);

            int color = 0xFF000000 | (r << 16) | (g << 8) | b;

            gui.fill(
                    innerX + i,
                    innerY,
                    innerX + i + 1,
                    innerY + innerHeight,
                    color
            );
        }
    }
}