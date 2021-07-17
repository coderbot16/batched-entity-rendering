package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.MemoryTrackingBuffer;
import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder implements MemoryTrackingBuffer {
	@Shadow
	private ByteBuffer buffer;

	@Overwrite
	private static int roundBufferSize(int amount) {
		int i = 2048 * 2048;
		if (amount == 0) {
			return i;
		} else {
			if (amount < 0) {
				i *= -1;
			}

			int j = amount % i;
			return j == 0 ? amount : amount + i - j;
		}
	}

	@Override
	public int getAllocatedSize() {
		return buffer.capacity();
	}

	@Override
	public int getUsedSize() {
		return buffer.position();
	}
}
