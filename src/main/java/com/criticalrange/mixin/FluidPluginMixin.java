package com.criticalrange.mixin;

import com.criticalrange.CatalystConfig;
import com.hypixel.hytale.builtin.fluid.FluidPlugin;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for lazy fluid pre-processing.
 *
 * <p>When Hyxin is present, this mixin provides the same optimization as
 * LazyFluidTransformer but using the Mixin framework.</p>
 */
@Mixin(FluidPlugin.class)
public class FluidPluginMixin {

    /**
     * Cancels fluid pre-simulation when lazy loading is enabled.
     */
    @Inject(method = "onChunkPreProcess", at = @At("HEAD"), cancellable = true)
    private static void catalyst$skipFluidPreProcess(ChunkPreLoadProcessEvent event, CallbackInfo ci) {
        if (CatalystConfig.LAZY_FLUID_ENABLED) {
            ci.cancel();
        }
    }
}
