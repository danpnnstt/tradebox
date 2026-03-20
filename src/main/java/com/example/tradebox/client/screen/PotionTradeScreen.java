package com.example.tradebox.client.screen;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.GlassBottleEntry;
import com.example.tradebox.config.PotionEntry;
import com.example.tradebox.menu.PotionTradeMenu;
import com.example.tradebox.network.BuyPotionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class PotionTradeScreen extends AbstractContainerScreen<PotionTradeMenu> {

    private static final int GUI_WIDTH  = 320;
    private static final int GUI_HEIGHT = 220;
    private static final int LIST_X     = 5;
    private static final int LIST_WIDTH = 155;
    private static final int LIST_TOP   = 18;
    private static final int ROW_H      = 16;
    private static final int MAX_VISIBLE = 11;
    private static final int COST_X     = 168;
    private static final int COST_Y     = 18;

    private static final int COLOR_BG_OUTER   = 0xFF3D3D3D;
    private static final int COLOR_BG_INNER   = 0xFFBBBBBB;
    private static final int COLOR_LIST_BG    = 0xFF8A8A8A;
    private static final int COLOR_SELECTED   = 0xFF4A7ABF;
    private static final int COLOR_HOVERED    = 0xFF6699CC;
    private static final int COLOR_DIVIDER    = 0xFF555555;
    private static final int COLOR_TEXT       = 0xFF1A1A1A;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_HAS_ITEM   = 0xFF44AA44;
    private static final int COLOR_MISSING    = 0xFFAA4444;
    private static final int COLOR_HDR        = 0xFF2A2A2A;

    private final List<PotionEntry> potions;
    private final GlassBottleEntry glassBottles;
    private int selectedIndex = -1;
    private int scrollOffset  = 0;

    private Button buyPotionButton;
    private Button buyBottlesButton;

    public PotionTradeScreen(PotionTradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.potions     = menu.getPotions();
        this.glassBottles = menu.getGlassBottles();
    }

    @Override
    protected void init() {
        super.init();
        int bx = leftPos, by = topPos;

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> scroll(-1))
                .bounds(bx + LIST_X + LIST_WIDTH - 14, by + LIST_TOP, 14, 14).build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> scroll(1))
                .bounds(bx + LIST_X + LIST_WIDTH - 14, by + LIST_TOP + MAX_VISIBLE * ROW_H - 14, 14, 14).build());

        buyPotionButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.tradebox.purchase"),
                b -> onPurchaseClicked()
        ).bounds(bx + COST_X, by + GUI_HEIGHT - 26, 140, 20).build());
        buyPotionButton.active = false;

        buyBottlesButton = addRenderableWidget(Button.builder(
                buildBottleButtonLabel(),
                b -> onBuyBottlesClicked()
        ).bounds(bx + LIST_X, by + GUI_HEIGHT - 26, LIST_WIDTH, 20).build());
        buyBottlesButton.active = canAffordCost(glassBottles.cost());
    }

    private Component buildBottleButtonLabel() {
        StringBuilder sb = new StringBuilder("Buy ");
        sb.append(glassBottles.quantity()).append("x Bottles: ");
        for (int i = 0; i < glassBottles.cost().size(); i++) {
            if (i > 0) sb.append(" + ");
            CostEntry c = glassBottles.cost().get(i);
            sb.append(c.quantity()).append(" ").append(getShortItemName(c.item()));
        }
        return Component.literal(sb.toString());
    }

    private void scroll(int delta) {
        int max = Math.max(0, potions.size() - MAX_VISIBLE);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + delta));
    }

    private void onPurchaseClicked() {
        if (selectedIndex < 0 || selectedIndex >= potions.size()) return;
        PotionEntry entry = potions.get(selectedIndex);
        PacketDistributor.sendToServer(new BuyPotionPacket(entry.potion_id(), entry.type()));
    }

    private void onBuyBottlesClicked() {
        PacketDistributor.sendToServer(new BuyPotionPacket("", "bottle"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int lx = leftPos + LIST_X;
            int ly = topPos + LIST_TOP;
            if (mouseX >= lx && mouseX < lx + LIST_WIDTH - 14) {
                for (int i = 0; i < MAX_VISIBLE; i++) {
                    int idx = scrollOffset + i;
                    if (idx >= potions.size()) break;
                    int ey = ly + i * ROW_H;
                    if (mouseY >= ey && mouseY < ey + ROW_H) {
                        selectedIndex = idx;
                        buyPotionButton.active = canAffordCost(potions.get(idx).cost());
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll((int) -Math.signum(sy));
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = leftPos, y = topPos;

        g.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BG_OUTER);
        g.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + GUI_HEIGHT - 2, COLOR_BG_INNER);
        g.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 16, COLOR_HDR);

        // List pane
        int lx = x + LIST_X, ly = y + LIST_TOP;
        int listH = MAX_VISIBLE * ROW_H;
        g.fill(lx, ly, lx + LIST_WIDTH, ly + listH, COLOR_LIST_BG);

        for (int i = 0; i < MAX_VISIBLE; i++) {
            int idx = scrollOffset + i;
            if (idx >= potions.size()) break;
            PotionEntry entry = potions.get(idx);
            int ey = ly + i * ROW_H;
            boolean sel = idx == selectedIndex;
            boolean hov = mx >= lx && mx < lx + LIST_WIDTH - 14 && my >= ey && my < ey + ROW_H;

            if (sel)       g.fill(lx, ey, lx + LIST_WIDTH - 14, ey + ROW_H, COLOR_SELECTED);
            else if (hov)  g.fill(lx, ey, lx + LIST_WIDTH - 14, ey + ROW_H, COLOR_HOVERED);
            if (i > 0)     g.fill(lx, ey, lx + LIST_WIDTH - 14, ey + 1, COLOR_DIVIDER);

            // Potion icon (16x16, scaled to 12x12 in the row)
            ItemStack icon = buildPotionStack(entry);
            if (!icon.isEmpty()) g.renderItem(icon, lx + 1, ey + 2);

            String label = getPotionDisplayName(entry);
            g.drawString(font, label, lx + 17, ey + 4, sel ? COLOR_TEXT_LIGHT : COLOR_TEXT, false);
        }

        // Scrollbar
        if (potions.size() > MAX_VISIBLE) {
            int sbx = lx + LIST_WIDTH - 3;
            int barH = (int) ((double) MAX_VISIBLE / potions.size() * listH);
            int barTop = ly + (int) ((double) scrollOffset / potions.size() * listH);
            g.fill(sbx, ly, sbx + 3, ly + listH, 0xFF666666);
            g.fill(sbx, barTop, sbx + 3, barTop + barH, 0xFFDDDDDD);
        }

        // Divider
        int divX = x + COST_X - 5;
        g.fill(divX, y + 2, divX + 1, y + GUI_HEIGHT - 2, COLOR_DIVIDER);

        // Cost panel
        int cx = x + COST_X, cy = y + COST_Y;
        g.fill(cx, cy - 2, cx + 144, cy + 10, COLOR_HDR);
        g.drawString(font, Component.translatable("gui.tradebox.cost"), cx + 2, cy, COLOR_TEXT_LIGHT, false);
        cy += 14;

        if (selectedIndex >= 0 && selectedIndex < potions.size()) {
            PotionEntry entry = potions.get(selectedIndex);
            var mc = Minecraft.getInstance();

            for (CostEntry cost : entry.cost()) {
                int have = mc.player != null ? countItemClient(cost.item()) : 0;
                boolean enough = have >= cost.quantity();

                // Item icon
                ResourceLocation rl = ResourceLocation.tryParse(cost.item());
                if (rl != null) {
                    Optional<net.minecraft.core.Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.getHolder(rl);
                    if (opt.isPresent()) g.renderItem(new ItemStack(opt.get().value()), cx, cy - 1);
                }

                g.drawString(font, getItemDisplayName(cost.item()) + " x" + cost.quantity(), cx + 18, cy + 2, COLOR_TEXT, false);
                g.drawString(font, "(" + have + ")", cx + 18, cy + 10, enough ? COLOR_HAS_ITEM : COLOR_MISSING, false);
                cy += 24;
                if (cy > y + GUI_HEIGHT - 30) break;
            }

            boolean affordable = canAffordCost(entry.cost());
            g.drawString(font,
                    affordable ? Component.translatable("gui.tradebox.can_afford")
                               : Component.translatable("gui.tradebox.cannot_afford"),
                    cx, y + GUI_HEIGHT - 30, affordable ? COLOR_HAS_ITEM : COLOR_MISSING, false);
        } else {
            g.drawString(font, Component.translatable("gui.tradebox.select_potion"), cx + 2, cy, COLOR_TEXT, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, this.title, LIST_X + 2, 4, COLOR_TEXT_LIGHT, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        // Refresh button states each frame
        buyBottlesButton.active = canAffordCost(glassBottles.cost());
        if (selectedIndex >= 0 && selectedIndex < potions.size()) {
            buyPotionButton.active = canAffordCost(potions.get(selectedIndex).cost());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ItemStack buildPotionStack(PotionEntry entry) {
        ResourceLocation rl = ResourceLocation.tryParse(entry.potion_id());
        if (rl == null) return ItemStack.EMPTY;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return ItemStack.EMPTY;
        var registry = mc.level.registryAccess().registryOrThrow(Registries.POTION);
        Optional<net.minecraft.core.Holder.Reference<Potion>> opt = registry.getHolder(rl);
        if (opt.isEmpty()) return ItemStack.EMPTY;

        Item item = switch (entry.type()) {
            case "splash"    -> Items.SPLASH_POTION;
            case "lingering" -> Items.LINGERING_POTION;
            default          -> Items.POTION;
        };
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(opt.get()));
        return stack;
    }

    private String getPotionDisplayName(PotionEntry entry) {
        ItemStack stack = buildPotionStack(entry);
        if (!stack.isEmpty()) return stack.getHoverName().getString();
        // Fallback: derive from ID
        String name = entry.potion_id().contains(":") ? entry.potion_id().split(":")[1] : entry.potion_id();
        name = name.replace("_", " ");
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return switch (entry.type()) {
            case "splash"    -> "Splash Potion of " + name;
            case "lingering" -> "Lingering Potion of " + name;
            default          -> "Potion of " + name;
        };
    }

    private String getItemDisplayName(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return itemId;
        Optional<net.minecraft.core.Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.getHolder(rl);
        return opt.map(h -> Component.translatable(h.value().getDescriptionId()).getString()).orElse(itemId);
    }

    private String getShortItemName(String itemId) {
        String full = getItemDisplayName(itemId);
        // Abbreviate long names (Glass Bottle → Bottle, Gold Ingot → Gold, etc.)
        return full.replace("Gold Ingot", "Gold").replace("Glass Bottle", "Bottle");
    }

    private int countItemClient(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return 0;
        Optional<net.minecraft.core.Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.getHolder(rl);
        if (opt.isEmpty()) return 0;
        Item item = opt.get().value();
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        int count = 0;
        for (ItemStack s : player.getInventory().items)   if (s.is(item)) count += s.getCount();
        for (ItemStack s : player.getInventory().offhand) if (s.is(item)) count += s.getCount();
        return count;
    }

    private boolean canAffordCost(List<CostEntry> cost) {
        for (CostEntry c : cost) if (countItemClient(c.item()) < c.quantity()) return false;
        return true;
    }
}
