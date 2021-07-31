package net.coderbot.batchedentityrendering.impl;

import net.coderbot.batchedentityrendering.mixin.RenderLayerAccessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FullyBufferedVertexConsumerProvider extends VertexConsumerProvider.Immediate implements MemoryTrackingBuffer {
	private final BufferBuilder buffer;
	private final List<RenderLayer> usedLayers;

	private RenderLayer currentLayer;

	private int drawCalls;

	public static FullyBufferedVertexConsumerProvider instance;

	public FullyBufferedVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		// 2 MB initial allocation
		this.buffer = new BufferBuilder(512 * 1024);
		this.usedLayers = new ArrayList<>(256);

		this.currentLayer = null;
		this.drawCalls = 0;

		// TODO: Eh
		instance = this;
	}

	@Override
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		if (!Objects.equals(currentLayer, renderLayer)) {
			if (currentLayer != null) {
				if (isTranslucent(currentLayer)) {
					buffer.sortQuads(0, 0, 0);
				}

				buffer.end();
				usedLayers.add(currentLayer);
			}

			buffer.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());

			currentLayer = renderLayer;
		}

		return buffer;
	}

	@Override
	public void draw() {
		if (currentLayer == null) {
			return;
		}

		usedLayers.add(currentLayer);

		if (isTranslucent(currentLayer)) {
			buffer.sortQuads(0, 0, 0);
		}

		buffer.end();
		currentLayer = null;

		for (RenderLayer layer : usedLayers) {
			layer.startDrawing();
			drawCalls += 1;
			BufferRenderer.draw(buffer);
			layer.endDrawing();
		}

		usedLayers.clear();
	}

	private static boolean isTranslucent(RenderLayer layer) {
		return ((RenderLayerAccessor) layer).isTranslucent();
	}

	public int getDrawCalls() {
		return drawCalls;
	}

	public void resetDrawCalls() {
		drawCalls = 0;
	}

	@Override
	public void draw(RenderLayer layer) {
		// Disable explicit flushing
	}

	@Override
	public int getAllocatedSize() {
		return ((MemoryTrackingBuffer) buffer).getAllocatedSize();
	}

	@Override
	public int getUsedSize() {
		return ((MemoryTrackingBuffer) buffer).getUsedSize();
	}
}
