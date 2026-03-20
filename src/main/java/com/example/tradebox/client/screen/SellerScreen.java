package com.example.tradebox.client.screen;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentEntry;
import com.example.tradebox.config.PotionEntry;
import com.example.tradebox.menu.SellerMenu;
import com.example.tradebox.network.SellEnchantmentPacket;
import com.example.tradebox.network.SellPotionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SellerScreen extends AbstractContainerScreen<SellerMenu> {

    // Layout
    private static final int GUI_WIDTH    = 320;
    private static final int GUI_HEIGHT   = 210;
    private static final int LIST_X       = 5;
    private static final int LIST_WIDTH   = 155;
    private static final int LIST_ITEM_H  = 14;
    private static final int LIST_TOP     = 18;
    private static final int MAX_VISIBLE  = 11;
    private static final int DETAIL_X     = 168;
    private static final int DETAIL_Y     = 18;

    // Colors
    private static final int COL_BG_OUTER   = 0xFF2E2E2E;
    private static final int COL_BG_INNER   = 0xFF9E9E9E;
    private static final int COL_LIST_BG    = 0xFF6A6A6A;
    private static final int COL_SELECTED   = 0xFF7A4ABF;
    private static final int COL_HOVERED    = 0xFF9966CC;
    private static final int COL_DIVIDER    = 0xFF444444;
    private static final int COL_TEXT       = 0xFF1A1A1A;
    private static final int COL_TEXT_LIGHT = 0xFFFFFFFF;
    private static final int COL_HAS_ITEM   = 0xFF44AA44;
    private static final int COL_MISSING    = 0xFFAA4444;
    private static final int COL_HEADER     = 0xFF1A1A2E;
    private static final int COL_BADGE_E    = 0xFF3A5FA0;
    private static final int COL_BADGE_P    = 0xFF6A3A9A;

    /** A unified list entry for the scrollable list. */
    private record ListEntry(boolean isEnchant, EnchantmentEntry enchant, PotionEntry potion) {
        static ListEntry ofEnchant(EnchantmentEntry e) { return new ListEntry(true,  e, null); }
        static ListEntry ofPotion (PotionEntry p)      { return new ListEntry(false, null, p); }
    }

    private final List<ListEntry> listEntries = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset  = 0;
    private Button sellButton;

    public SellerScreen(SellerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        for (EnchantmentEntry e : menu.getRefundableEnchantments()) listEntries.add(ListEntry.ofEnchant(e));
        for (PotionEntry p : menu.getRefundablePotions())           listEntries.add(ListEntry.ofPotion(p));
    }

    @Override
    protected void init() {
        super.init();
        int bx = leftPos, by = topPos;

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> scroll(-1))
                .bounds(bx + LIST_X + LIST_WIDTH - 14, by + LIST_TOP, 14, 14).build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> scroll(1))
                .bounds(bx + LIST_X + LIST_WIDTH - 14, by + LIST_TOP + MAX_VISIBLE * LIST_ITEM_H - 14, 14, 14).build());

        sellButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.tradebox.sell"), b -> onSellClicked()
        ).bounds(bx + DETAIL_X, by + GUI_HEIGHT - 26, 140, 20).build());
        sellButton.active = false;
    }

    private void scroll(int d) {
        int max = Math.max(0, listEntries.size() - MAX_VISIBLE);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + d));
    }

    private void onSellClicked() {
        if (selectedIndex < 0 || selectedIndex >= listEntries.size()) return;
        ListEntry entry = listEntries.get(selectedIndex);
        if (entry.isEnchant()) {
            EnchantmentEntry e = entry.enchant();
            ResourceLocation id = ResourceLocation.tryParse(e.enchant_id());
            if (id != null) PacketDistributor.sendToServer(new SellEnchantmentPacket(id, e.level()));
        } else {
            PotionEntry p = entry.potion();
            PacketDistributor.sendToServer(new SellPotionPacket(p.potion_id(), p.type()));
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int lx = leftPos + LIST_X, ly = topPos + LIST_TOP, lw = LIST_WIDTH - 14;
            if (mx >= lx && mx < lx + lw) {
                for (int i = 0; i < MAX_VISIBLE; i++) {
                    int idx = scrollOffset + i;
                    if (idx >= listEntries.size()) break;
                    int ey = ly + i * LIST_ITEM_H;
                    if (my >= ey && my < ey + LIST_ITEM_H) {
                        selectedIndex = idx;
                        sellButton.active = hasRequiredItem(listEntries.get(idx));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll((int) -Math.signum(sy));
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COL_BG_OUTER);
        g.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + GUI_HEIGHT - 2, COL_BG_INNER);
        g.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 16, COL_HEADER);

        // List pane
        int lx = x + LIST_X, ly = y + LIST_TOP, listH = MAX_VISIBLE * LIST_ITEM_H;
        g.fill(lx, ly, lx + LIST_WIDTH, ly + listH, COL_LIST_BG);

        for (int i = 0; i < MAX_VISIBLE; i++) {
            int idx = scrollOffset + i;
            if (idx >= listEntries.size()) break;
            ListEntry entry = listEntries.get(idx);
            int ey = ly + i * LIST_ITEM_H;
            boolean sel = idx == selectedIndex;
            boolean hov = mx >= lx && mx < lx + LIST_WIDTH - 14 && my >= ey && my < ey + LIST_ITEM_H;
            if (sel) g.fill(lx, ey, lx + LIST_WIDTH - 14, ey + LIST_ITEM_H, COL_SELECTED);
            else if (hov) g.fill(lx, ey, lx + LIST_WIDTH - 14, ey + LIST_ITEM_H, COL_HOVERED);
            if (i > 0) g.fill(lx, ey, lx + LIST_WIDTH - 14, ey + 1, COL_DIVIDER);

            // Type badge
            int badgeX = lx + 2;
            String badge = entry.isEnchant() ? "E" : "P";
            int badgeCol = entry.isEnchant() ? COL_BADGE_E : COL_BADGE_P;
            g.fill(badgeX, ey + 2, badgeX + 9, ey + 12, badgeCol);
            g.drawString(font, badge, badgeX + 2, ey + 3, COL_TEXT_LIGHT, false);

            String label = entry.isEnchant() ? buildEnchantLabel(entry.enchant()) : buildPotionLabel(entry.potion());
            g.drawString(font, label, badgeX + 11, ey + 3, sel ? COL_TEXT_LIGHT : COL_TEXT, false);
        }

        // Divider
        int dvX = x + DETAIL_X - 5;
        g.fill(dvX, y + 2, dvX + 1, y + GUI_HEIGHT - 2, COL_DIVIDER);

        // Detail pane
        int cx = x + DETAIL_X, cy = y + DETAIL_Y;
        int refundPct = menu.getRefundPercentage();
        g.fill(cx, cy - 2, cx + 144, cy + 10, COL_HEADER);
        g.drawString(font, Component.translatable("gui.tradebox.refund_pct", refundPct), cx + 2, cy, COL_TEXT_LIGHT, false);
        cy += 14;

        if (selectedIndex >= 0 && selectedIndex < listEntries.size()) {
            ListEntry entry = listEntries.get(selectedIndex);

            // Has item indicator
            boolean has = hasRequiredItem(entry);
            String hasLabel = has ? "\u2713 " + Component.translatable("gui.tradebox.have_item").getString()
                                  : "\u2717 " + Component.translatable("gui.tradebox.no_item").getString();
            g.drawString(font, hasLabel, cx + 2, cy, has ? COL_HAS_ITEM : COL_MISSING, false);
            cy += 12;

            // "You'll receive:" header
            g.drawString(font, Component.translatable("gui.tradebox.you_receive"), cx + 2, cy, COL_TEXT, false);
            cy += 10;

            List<CostEntry> costs = entry.isEnchant() ? entry.enchant().cost() : entry.potion().cost();
            for (CostEntry cost : costs) {
                int refundQty = Math.max(0, (int) Math.round(cost.quantity() * refundPct / 100.0));

                ResourceLocation itemRl = ResourceLocation.tryParse(cost.item());
                if (itemRl != null) {
                    Optional<net.minecraft.core.Holder.Reference<Item>> itemOpt = BuiltInRegistries.ITEM.getHolder(itemRl);
                    if (itemOpt.isPresent()) g.renderItem(new ItemStack(itemOpt.get().value()), cx, cy - 1);
                }

                String name = getItemDisplayName(cost.item());
                g.drawString(font, refundQty + "x " + name, cx + 18, cy + 2,
                        refundQty > 0 ? COL_TEXT : COL_MISSING, false);
                cy += 18;
                if (cy > y + GUI_HEIGHT - 32) break;
            }
        } else {
            g.drawString(font, Component.translatable("gui.tradebox.select_item"), cx + 2, cy, COL_TEXT, false);
        }

        // Scrollbar
        if (listEntries.size() > MAX_VISIBLE) {
            int total = listEntries.size(), barH = (int)((double) MAX_VISIBLE / total * listH);
            int barTop = ly + (int)((double) scrollOffset / total * listH);
            int sbX = lx + LIST_WIDTH - 3;
            g.fill(sbX, ly, sbX + 3, ly + listH, 0xFF666666);
            g.fill(sbX, barTop, sbX + 3, barTop + barH, 0xFFDDDDDD);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, this.title, LIST_X + 2, 4, COL_TEXT_LIGHT, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        if (selectedIndex >= 0 && selectedIndex < listEntries.size()) {
            sellButton.active = hasRequiredItem(listEntries.get(selectedIndex));
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String buildEnchantLabel(EnchantmentEntry e) {
        String key = "enchantment." + ResourceLocation.tryParse(e.enchant_id()).getNamespace()
                + "." + ResourceLocation.tryParse(e.enchant_id()).getPath();
        return Component.translatable(key).getString() + " " + getRoman(e.level());
    }

    private String buildPotionLabel(PotionEntry p) {
        String local = p.potion_id().contains(":") ? p.potion_id().split(":")[1] : p.potion_id();
        String suffix = "";
        if (local.startsWith("long_"))   { local = local.substring(5);  suffix = " (Ext)"; }
        else if (local.startsWith("strong_")) { local = local.substring(7); suffix = " (Enh)"; }
        String base = toTitle(local) + suffix;
        return switch (p.type()) { case "splash" -> "Splash " + base; case "lingering" -> "Ling. " + base; default -> base; };
    }

    private static String toTitle(String s) {
        String[] w = s.replace('_', ' ').split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : w) { if (!sb.isEmpty()) sb.append(' '); sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)); }
        return sb.toString();
    }

    private String getItemDisplayName(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return itemId;
        Optional<net.minecraft.core.Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.getHolder(rl);
        return opt.map(h -> Component.translatable(h.value().getDescriptionId()).getString()).orElse(itemId);
    }

    private static String getRoman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> String.valueOf(n);
        };
    }

    private boolean hasRequiredItem(ListEntry entry) {
        return entry.isEnchant()
                ? hasEnchantedBook(entry.enchant().enchant_id(), entry.enchant().level())
                : hasPotion(entry.potion().potion_id(), entry.potion().type());
    }

    private boolean hasEnchantedBook(String enchantId, int level) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        var regOpt = mc.level.registryAccess().lookup(Registries.ENCHANTMENT);
        if (regOpt.isEmpty()) return false;
        ResourceLocation rl = ResourceLocation.tryParse(enchantId);
        if (rl == null) return false;
        var holderOpt = regOpt.get().get(ResourceKey.create(Registries.ENCHANTMENT, rl));
        if (holderOpt.isEmpty()) return false;
        var holder = holderOpt.get();
        for (ItemStack stack : mc.player.getInventory().items) {
            if (!stack.is(Items.ENCHANTED_BOOK)) continue;
            var stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (stored.getLevel(holder) == level) return true;
        }
        return false;
    }

    private boolean hasPotion(String potionId, String potionType) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        Item target = switch (potionType) {
            case "splash" -> Items.SPLASH_POTION; case "lingering" -> Items.LINGERING_POTION; default -> Items.POTION;
        };
        ResourceLocation potionRl = ResourceLocation.tryParse(potionId);
        if (potionRl == null) return false;
        for (ItemStack stack : mc.player.getInventory().items) {
            if (!stack.is(target)) continue;
            var contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (contents.potion().isPresent()
                    && contents.potion().get().unwrapKey()
                        .map(k -> k.location().equals(potionRl)).orElse(false)) return true;
        }
        return false;
    }
}
