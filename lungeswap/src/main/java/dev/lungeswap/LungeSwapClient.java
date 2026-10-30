package dev.lungeswap;

import dev.lungeswap.mixin.MinecraftClientInvoker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Lunge-Swap: Taste druecken -> Speer mit hoechstem Lunge-Level waehlen, Jab (Linksklick),
 * dann auf den Slot mit dem geringsten Cooldown zurueckwechseln. Alles im selben Tick.
 * Taste halten = Wiederholung alle 5 Ticks.
 */
public class LungeSwapClient implements ClientModInitializer {
    /** Ticks zwischen zwei Lunges bei gehaltener Taste. */
    private static final int INTERVAL_TICKS = 5;

    private static KeyBinding swapKey;
    private int cooldown = 0;
    private int warnCooldown = 0;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("lungeswap", "main"));
        // Standard: V. In den Steuerungs-Optionen frei aenderbar, auch auf Maustasten (z. B. Seitentaste).
        swapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lungeswap.swap", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, category));

        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (cooldown > 0) cooldown--;
        if (warnCooldown > 0) warnCooldown--;

        boolean wanted = swapKey.isPressed();
        while (swapKey.wasPressed()) wanted = true; // auch kurzes Antippen zaehlt

        ClientPlayerEntity player = client.player;
        if (!wanted || player == null || client.interactionManager == null || client.currentScreen != null) return;
        if (cooldown > 0) return;

        if (lungeSwap(client, player)) {
            cooldown = INTERVAL_TICKS;
        } else if (warnCooldown == 0) {
            player.sendMessage(Text.literal("LungeSwap: kein Speer mit Lunge in der Hotbar"), true);
            warnCooldown = 40;
        }
    }

    private boolean lungeSwap(MinecraftClient client, ClientPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        int origin = inv.getSelectedSlot();
        int spear = findBestSpear(inv);
        if (spear < 0) return false;

        int back = findReturnSlot(player, inv, spear, origin);

        // 1) auf den Speer wechseln (Client + Server)
        inv.setSelectedSlot(spear);
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(spear));

        // 2) Jab ueber die normale Linksklick-Routine
        ((MinecraftClientInvoker) (Object) client).lungeswap$doAttack();

        // 3) sofort zurueck auf den Slot mit dem geringsten Cooldown
        if (back != spear) {
            inv.setSelectedSlot(back);
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(back));
        }
        return true;
    }

    /** Speer in der Hotbar mit dem hoechsten Lunge-Level (mind. 1). */
    private static int findBestSpear(PlayerInventory inv) {
        int bestSlot = -1, bestLevel = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty() || !isSpear(s)) continue;
            int lvl = lungeLevel(s);
            if (lvl > bestLevel) {
                bestLevel = lvl;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    /**
     * Rueckwechsel-Slot: geringster Cooldown. Bei Gleichstand: urspruenglicher Slot,
     * dann leerer Slot, dann Rest. Waffen/Werkzeuge werden uebersprungen (ausser sie waren der Ursprungsslot).
     */
    private static int findReturnSlot(ClientPlayerEntity player, PlayerInventory inv, int spear, int origin) {
        int best = origin == spear ? spear : origin;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < 9; i++) {
            if (i == spear) continue;
            ItemStack s = inv.getStack(i);
            if (i != origin && isWeaponOrTool(s)) continue;
            float progress = s.isEmpty() ? 0f : player.getItemCooldownManager().getCooldownProgress(s, 0f);
            double tie = (i == origin) ? 0.0 : (s.isEmpty() ? 0.1 : 0.2);
            double score = progress * 10.0 + tie;
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private static String path(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).getPath();
    }

    private static boolean isSpear(ItemStack s) {
        return path(s).endsWith("_spear");
    }

    private static boolean isWeaponOrTool(ItemStack s) {
        if (s.isEmpty()) return false;
        String p = path(s);
        return p.endsWith("_spear") || p.endsWith("_sword") || p.endsWith("_axe") || p.endsWith("_pickaxe")
                || p.endsWith("_shovel") || p.endsWith("_hoe") || p.equals("mace") || p.equals("trident");
    }

    private static int lungeLevel(ItemStack s) {
        ItemEnchantmentsComponent e = s.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (RegistryEntry<Enchantment> en : e.getEnchantments()) {
            if (en.matchesKey(Enchantments.LUNGE)) return e.getLevel(en);
        }
        return 0;
    }
}
