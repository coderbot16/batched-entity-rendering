package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.batchedentityrendering.mixin.RenderLayerAccessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FullyBufferedVertexConsumerProvider extends VertexConsumerProvider.Immediate implements MemoryTrackingBuffer {
	private final SegmentedBufferBuilder builder;
	private int drawCalls;

	public static FullyBufferedVertexConsumerProvider instance;

	public FullyBufferedVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		// 2 MB initial allocation
		this.builder = new SegmentedBufferBuilder();
		this.drawCalls = 0;

		// TODO: Eh
		instance = this;
	}

	@Override
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		return builder.getBuffer(renderLayer);
	}

	@Override
	public void draw() {
		List<BufferSegment> segments = builder.getSegments();

		for (BufferSegment segment : segments) {
			drawCalls += 1;
			draw(segment);
		}
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

	private static void draw(BufferSegment segment) {
		BufferBuilder.DrawArrayParameters parameters = segment.getParameters();

		segment.getRenderLayer().startDrawing();
		draw(segment.getSlice(), parameters.getMode(), parameters.getVertexFormat(), parameters.getCount());
		segment.getRenderLayer().endDrawing();
	}

	private static void draw(ByteBuffer buffer, int mode, VertexFormat vertexFormat, int count) {
		// TODO: This only works on 1.15 and 1.16, not 1.17.
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		buffer.clear();
		if (count > 0) {
			vertexFormat.startDrawing(MemoryUtil.memAddress(buffer));
			GlStateManager.drawArrays(mode, 0, count);
			vertexFormat.endDrawing();
		}
	}

	@Override
	public void draw(RenderLayer layer) {
		// Disable explicit flushing
	}

	@Override
	public int getAllocatedSize() {
		return builder.getAllocatedSize();
	}

	@Override
	public int getUsedSize() {
		return builder.getUsedSize();
	}
}
