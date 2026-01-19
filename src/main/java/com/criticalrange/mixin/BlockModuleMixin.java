package com.criticalrange.mixin;

import com.criticalrange.CatalystConfig;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for lazy block entity initialization.
 *
 * <p>When Hyxin is present, this mixin provides the same optimization as
 * LazyBlockEntityTransformer but using the Mixin framework.</p>
 */
@Mixin(BlockModule.class)
public class BlockModuleMixin {

    /**
     * Cancels block entity pre-loading when lazy loading is enabled.
     */
    @Inject(method = "onChunkPreLoadProcessEnsureBlockEntity", at = @At("HEAD"), cancellable = true)
    private static void catalyst$skipBlockEntityPreLoad(ChunkPreLoadProcessEvent event, CallbackInfo ci) {
        if (CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED) {
            ci.cancel();
        }
    }
}
