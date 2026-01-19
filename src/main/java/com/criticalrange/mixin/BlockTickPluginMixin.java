package com.criticalrange.mixin;

import com.criticalrange.CatalystConfig;
import com.hypixel.hytale.builtin.blocktick.BlockTickPlugin;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for lazy block tick discovery.
 *
 * <p>When Hyxin is present, this mixin provides the same optimization as
 * LazyBlockTickTransformer but using the Mixin framework.</p>
 */
@Mixin(BlockTickPlugin.class)
public class BlockTickPluginMixin {

    /**
     * Cancels ticking block discovery when lazy loading is enabled.
     * Returns 0 (no blocks discovered) to match expected return type.
     */
    @Inject(method = "discoverTickingBlocks(Lcom/hypixel/hytale/component/Holder;Lcom/hypixel/hytale/server/core/universe/world/chunk/WorldChunk;)I", at = @At("HEAD"), cancellable = true)
    private void catalyst$skipTickDiscovery(Holder<ChunkStore> holder, WorldChunk chunk, CallbackInfoReturnable<Integer> cir) {
        if (CatalystConfig.LAZY_BLOCK_TICK_ENABLED) {
            cir.setReturnValue(0);
        }
    }
}
