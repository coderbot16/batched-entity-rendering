package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.FullyBufferedVertexConsumerProvider;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBufferBuilderStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    @Inject(method = "getRightText", at = @At("RETURN"))
    private void appendShaderPackText(CallbackInfoReturnable<List<String>> cir) {
        List<String> messages = cir.getReturnValue();

		MemoryTrackingBufferBuilderStorage memoryTracker = (MemoryTrackingBufferBuilderStorage) MinecraftClient.getInstance().getBufferBuilders();
		messages.add(5, "[Entity Batching] Misc Buffers Allocated Size: " + memoryTracker.getMiscBufferAllocatedSize());
		messages.add(5, "[Entity Batching] Entity Buffers Size: " + memoryTracker.getEntityBufferAllocatedSize());
        messages.add(5, "[Entity Batching] WorldRenderer recursion depth (shouldn't go beyond 1): " + memoryTracker.getMaxBegins());
        messages.add(5, "[Entity Batching] Entity Buffer Draw Calls: " + FullyBufferedVertexConsumerProvider.instance.getDrawCalls());
    }
}
