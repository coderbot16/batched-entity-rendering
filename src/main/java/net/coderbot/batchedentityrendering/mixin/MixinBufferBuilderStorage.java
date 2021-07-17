package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.ExtendedBufferStorage;
import net.coderbot.batchedentityrendering.impl.FantasticVertexConsumerProvider;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
import net.coderbot.batchedentityrendering.impl.MemoryTrackingBufferBuilderStorage;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage implements ExtendedBufferStorage, MemoryTrackingBufferBuilderStorage {
	@Unique
	private final VertexConsumerProvider.Immediate buffered = new FantasticVertexConsumerProvider();

	@Unique
	private int begins = 0;

	@Unique
	private int maxBegins = 0;

	@Unique
	private final OutlineVertexConsumerProvider outlineVertexConsumers = new OutlineVertexConsumerProvider(buffered);

	@Shadow
	@Final
	private VertexConsumerProvider.Immediate entityVertexConsumers;

	@Inject(method = "getEntityVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceEntityVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(buffered);
	}

	@Inject(method = "getOutlineVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void batchedentityrendering$replaceOutlineVertexConsumers(CallbackInfoReturnable<OutlineVertexConsumerProvider> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(outlineVertexConsumers);
	}

	@Override
	public void beginWorldRendering() {
		begins += 1;

		maxBegins = Math.max(begins, maxBegins);
	}

	@Override
	public void endWorldRendering() {
		begins -= 1;
	}

	@Override
	public int getEntityBufferAllocatedSize() {
		return ((MemoryTrackingBuffer) buffered).getAllocatedSize();
	}

	@Override
	public int getMiscBufferAllocatedSize() {
		return ((MemoryTrackingBuffer) entityVertexConsumers).getAllocatedSize();
	}

	@Override
	public int getMaxBegins() {
		return maxBegins;
	}
}
