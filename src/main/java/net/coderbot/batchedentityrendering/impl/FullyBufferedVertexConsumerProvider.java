package net.coderbot.batchedentityrendering.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FullyBufferedVertexConsumerProvider extends VertexConsumerProvider.Immediate implements MemoryTrackingBuffer {
	private final Map<RenderLayer, BufferBuilder> bufferBuilders;
	private final Object2IntMap<RenderLayer> unused;
	private final Set<BufferBuilder> activeBuffers;
	private boolean flushed;

	public FullyBufferedVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.bufferBuilders = new HashMap<>();
		this.unused = new Object2IntOpenHashMap<>();
		this.activeBuffers = new HashSet<>();
		this.flushed = false;
	}

	@Override
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		flushed = false;

		BufferBuilder buffer = bufferBuilders.computeIfAbsent(renderLayer, layer -> new BufferBuilder(2048));

		if (activeBuffers.add(buffer)) {
			buffer.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
		}

		// If this buffer is scheduled to be removed, unschedule it since it's now being used.
		unused.removeInt(renderLayer);

		return buffer;
	}

	@Override
	public void draw() {
		if (flushed) {
			return;
		}

		List<RenderLayer> removedLayers = new ArrayList<>();

		unused.forEach((unusedLayer, unusedCount) -> {
			if (unusedCount < 10) {
				// Removed after 10 frames of not being used
				return;
			}

			BufferBuilder buffer = bufferBuilders.remove(unusedLayer);
			removedLayers.add(unusedLayer);

			if (activeBuffers.contains(buffer)) {
				throw new IllegalStateException(
						"A buffer was simultaneously marked as inactive and as active, something is very wrong...");
			}
		});

		for (RenderLayer removed : removedLayers) {
			unused.removeInt(removed);
		}

		bufferBuilders.keySet().forEach(this::drawInternal);

		flushed = true;
	}

	@Override
	public void draw(RenderLayer layer) {
		// Disable explicit flushing
	}

	private void drawInternal(RenderLayer layer) {
		BufferBuilder buffer = bufferBuilders.get(layer);

		if (buffer == null) {
			return;
		}

		if (activeBuffers.remove(buffer)) {
			layer.draw(buffer, 0, 0, 0);
			buffer.reset();
		} else {
			// Schedule the buffer for removal next frame if it isn't used this frame.
			int unusedCount = unused.getOrDefault(layer, 0);

			unusedCount += 1;

			unused.put(layer, unusedCount);
		}
	}

	@Override
	public int getAllocatedSize() {
		int allocatedSize = 0;

		for (BufferBuilder builder : bufferBuilders.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getAllocatedSize();
		}

		return allocatedSize;
	}

	@Override
	public int getUsedSize() {
		int allocatedSize = 0;

		for (BufferBuilder builder : bufferBuilders.values()) {
			allocatedSize += ((MemoryTrackingBuffer) builder).getUsedSize();
		}

		return allocatedSize;
	}
}
