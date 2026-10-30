package dev.lungeswap.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Macht die private Linksklick-Routine des Clients aufrufbar (derselbe Weg wie ein echter Klick). */
@Mixin(MinecraftClient.class)
public interface MinecraftClientInvoker {
    @Invoker("doAttack")
    boolean lungeswap$doAttack();
}
