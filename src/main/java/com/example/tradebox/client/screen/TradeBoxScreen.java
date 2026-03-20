package com.example.tradebox.client.screen;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentEntry;
import com.example.tradebox.menu.TradeBoxMenu;
import com.example.tradebox.network.BuyEnchantmentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class TradeBoxScreen extends AbstractContainerScreen<TradeBoxMenu> {

    // Layout constants
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 210;
    private static final int LIST_X_OFFSET = 5;
    private static final int LIST_WIDTH = 155;
    private static final int LIST_ITEM_HEIGHT = 14;
    private static final int LIST_TOP_OFFSET = 18;
    private static final int MAX_VISIBLE = 11; // entries visible at once
    private static final int COST_X_OFFSET = 168;
    private static final int COST_Y_OFFSET = 18;

    // Colors
    private static final int COLOR_BG_OUTER    = 0xFF3D3D3D;
    private static final int COLOR_BG_INNER    = 0xFFBBBBBB;
    private static final int COLOR_LIST_BG     = 0xFF8A8A8A;
    private static final int COLOR_SELECTED    = 0xFF4A7ABF;
    private static final int COLOR_HOVERED     = 0xFF6699CC;
    private static final int COLOR_DIVIDER     = 0xFF555555;
    private static final int COLOR_TEXT        = 0xFF1A1A1A;
    private static final int COLOR_TEXT_LIGHT  = 0xFFFFFFFF;
    private static final int COLOR_HAS_ITEM    = 0xFF44AA44;
    private static final int COLOR_MISSING     = 0xFFAA4444;
    private static final int COLOR_SECTION_HDR = 0xFF2A2A2A;

    private List<EnchantmentEntry> enchantments;
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    private Button buyButton;

    public TradeBoxScreen(TradeBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.enchantments = menu.getEnchantments();
    }

    @Override
    protected void init() {
        super.init();
        int bx = leftPos;
        int by = topPos;

        // Scroll up/down buttons
        addRenderableWidget(Button.builder(Component.literal("▲"), b -> scroll(-1))
                .bounds(bx + LIST_X_OFFSET + LIST_WIDTH - 14, by + LIST_TOP_OFFSET, 14, 14)
                .build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> scroll(1))
                .bounds(bx + LIST_X_OFFSET + LIST_WIDTH - 14, by + LIST_TOP_OFFSET + MAX_VISIBLE * LIST_ITEM_HEIGHT - 14, 14, 14)
                .build());

        // Purchase button (bottom-right area)
        buyButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.tradebox.purchase"),
                b -> onPurchaseClicked()
        ).bounds(bx + COST_X_OFFSET, by + GUI_HEIGHT - 26, 140, 20).build());
        buyButton.active = false;
    }

    private void scroll(int delta) {
        int max = Math.max(0, enchantments.size() - MAX_VISIBLE);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + delta));
    }

    private void onPurchaseClicked() {
        if (selectedIndex < 0 || selectedIndex >= enchantments.size()) return;
        EnchantmentEntry entry = enchantments.get(selectedIndex);
        ResourceLocation enchantId = ResourceLocation.tryParse(entry.enchant_id());
        if (enchantId == null) return;
        PacketDistributor.sendToServer(new BuyEnchantmentPacket(enchantId, entry.level()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int lx = leftPos + LIST_X_OFFSET;
            int ly = topPos + LIST_TOP_OFFSET;
            int listW = LIST_WIDTH - 14; // subtract scrollbar width

            if (mouseX >= lx && mouseX < lx + listW) {
                for (int i = 0; i < MAX_VISIBLE; i++) {
                    int entryIndex = scrollOffset + i;
                    if (entryIndex >= enchantments.size()) break;
                    int ey = ly + i * LIST_ITEM_HEIGHT;
                    if (mouseY >= ey && mouseY < ey + LIST_ITEM_HEIGHT) {
                        selectedIndex = entryIndex;
                        buyButton.active = canAfford(enchantments.get(selectedIndex));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll((int) -Math.signum(scrollY));
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Outer border
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BG_OUTER);
        // Inner fill
        graphics.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + GUI_HEIGHT - 2, COLOR_BG_INNER);

        // Title bar
        graphics.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 16, COLOR_SECTION_HDR);

        // List pane background
        int lx = x + LIST_X_OFFSET;
        int ly = y + LIST_TOP_OFFSET;
        int listH = MAX_VISIBLE * LIST_ITEM_HEIGHT;
        graphics.fill(lx, ly, lx + LIST_WIDTH, ly + listH, COLOR_LIST_BG);

        // Render list entries
        for (int i = 0; i < MAX_VISIBLE; i++) {
            int entryIndex = scrollOffset + i;
            if (entryIndex >= enchantments.size()) break;

            EnchantmentEntry entry = enchantments.get(entryIndex);
            int ey = ly + i * LIST_ITEM_HEIGHT;
            boolean isSelected = entryIndex == selectedIndex;
            boolean isHovered = mouseX >= lx && mouseX < lx + LIST_WIDTH - 14
                    && mouseY >= ey && mouseY < ey + LIST_ITEM_HEIGHT;

            if (isSelected) {
                graphics.fill(lx, ey, lx + LIST_WIDTH - 14, ey + LIST_ITEM_HEIGHT, COLOR_SELECTED);
            } else if (isHovered) {
                graphics.fill(lx, ey, lx + LIST_WIDTH - 14, ey + LIST_ITEM_HEIGHT, COLOR_HOVERED);
            }

            // Row divider
            if (i > 0) {
                graphics.fill(lx, ey, lx + LIST_WIDTH - 14, ey + 1, COLOR_DIVIDER);
            }

            String label = buildEnchantmentLabel(entry);
            graphics.drawString(font, label, lx + 3, ey + 3,
                    isSelected ? COLOR_TEXT_LIGHT : COLOR_TEXT, false);
        }

        // Divider between list and detail pane
        int divX = x + COST_X_OFFSET - 5;
        graphics.fill(divX, y + 2, divX + 1, y + GUI_HEIGHT - 2, COLOR_DIVIDER);

        // Detail pane — cost header
        int cx = x + COST_X_OFFSET;
        int cy = y + COST_Y_OFFSET;
        graphics.fill(cx, cy - 2, cx + 144, cy + 10, COLOR_SECTION_HDR);
        graphics.drawString(font,
                Component.translatable("gui.tradebox.cost"),
                cx + 2, cy, COLOR_TEXT_LIGHT, false);
        cy += 14;

        if (selectedIndex >= 0 && selectedIndex < enchantments.size()) {
            EnchantmentEntry entry = enchantments.get(selectedIndex);
            var player = Minecraft.getInstance().player;

            for (CostEntry cost : entry.cost()) {
                int available = player != null ? countItemInInventory(cost.item()) : 0;
                boolean enough = available >= cost.quantity();

                // Item icon
                ResourceLocation itemRL = ResourceLocation.tryParse(cost.item());
                if (itemRL != null) {
                    Optional<net.minecraft.core.Holder.Reference<Item>> itemOpt =
                            BuiltInRegistries.ITEM.getHolder(itemRL);
                    if (itemOpt.isPresent()) {
                        ItemStack icon = new ItemStack(itemOpt.get().value());
                        graphics.renderItem(icon, cx, cy - 1);
                    }
                }

                // Item name and quantity
                String itemName = getItemDisplayName(cost.item());
                String line = itemName + " x" + cost.quantity();
                String haveStr = "(" + available + ")";

                graphics.drawString(font, line, cx + 18, cy + 2, COLOR_TEXT, false);
                graphics.drawString(font, haveStr, cx + 18, cy + 10,
                        enough ? COLOR_HAS_ITEM : COLOR_MISSING, false);

                cy += 24;
                if (cy > y + GUI_HEIGHT - 30) break; // safety clamp
            }

            // Affordable indicator
            boolean affordable = canAfford(entry);
            Component statusMsg = affordable
                    ? Component.translatable("gui.tradebox.can_afford")
                    : Component.translatable("gui.tradebox.cannot_afford");
            graphics.drawString(font, statusMsg, cx, y + GUI_HEIGHT - 30,
                    affordable ? COLOR_HAS_ITEM : COLOR_MISSING, false);
        } else {
            graphics.drawString(font,
                    Component.translatable("gui.tradebox.select_enchantment"),
                    cx + 2, cy, COLOR_TEXT, false);
        }

        // Scroll position indicator
        if (enchantments.size() > MAX_VISIBLE) {
            int totalEntries = enchantments.size();
            int barHeight = (int) ((double) MAX_VISIBLE / totalEntries * (MAX_VISIBLE * LIST_ITEM_HEIGHT));
            int barTop = ly + (int) ((double) scrollOffset / totalEntries * (MAX_VISIBLE * LIST_ITEM_HEIGHT));
            int sbx = lx + LIST_WIDTH - 3;
            graphics.fill(sbx, ly, sbx + 3, ly + MAX_VISIBLE * LIST_ITEM_HEIGHT, 0xFF666666);
            graphics.fill(sbx, barTop, sbx + 3, barTop + barHeight, 0xFFDDDDDD);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Render title manually
        graphics.drawString(font, this.title, LIST_X_OFFSET + 2, 4, COLOR_TEXT_LIGHT, false);
        // Skip vanilla inventory label (no player inventory shown)
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        // Update buy button state on each frame (inventory may change)
        if (selectedIndex >= 0 && selectedIndex < enchantments.size()) {
            buyButton.active = canAfford(enchantments.get(selectedIndex));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildEnchantmentLabel(EnchantmentEntry entry) {
        String name = getEnchantmentDisplayName(entry.enchant_id());
        String levelStr = getLevelRomanNumeral(entry.level());
        return name + " " + levelStr;
    }

    private String getEnchantmentDisplayName(String enchantId) {
        ResourceLocation rl = ResourceLocation.tryParse(enchantId);
        if (rl == null) return enchantId;
        // Use the standard translation key: enchantment.namespace.path
        String key = "enchantment." + rl.getNamespace() + "." + rl.getPath();
        Component comp = Component.translatable(key);
        return comp.getString();
    }

    private String getItemDisplayName(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return itemId;
        Optional<net.minecraft.core.Holder.Reference<Item>> itemOpt =
                BuiltInRegistries.ITEM.getHolder(rl);
        if (itemOpt.isEmpty()) return itemId;
        return Component.translatable(itemOpt.get().value().getDescriptionId()).getString();
    }

    private static String getLevelRomanNumeral(int level) {
        return switch (level) {
            case 1  -> "I";
            case 2  -> "II";
            case 3  -> "III";
            case 4  -> "IV";
            case 5  -> "V";
            case 6  -> "VI";
            case 7  -> "VII";
            case 8  -> "VIII";
            case 9  -> "IX";
            case 10 -> "X";
            default -> String.valueOf(level);
        };
    }

    private int countItemInInventory(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return 0;
        Optional<net.minecraft.core.Holder.Reference<Item>> itemOpt =
                BuiltInRegistries.ITEM.getHolder(rl);
        if (itemOpt.isEmpty()) return 0;

        Item item = itemOpt.get().value();
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;

        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) count += stack.getCount();
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private boolean canAfford(EnchantmentEntry entry) {
        for (CostEntry cost : entry.cost()) {
            if (countItemInInventory(cost.item()) < cost.quantity()) return false;
        }
        return true;
    }
}
