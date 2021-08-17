package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.coderbot.batchedentityrendering.mixin.RenderLayerAccessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FullyBufferedVertexConsumerProvider extends VertexConsumerProvider.Immediate implements MemoryTrackingBuffer {
	private static final int UNASSIGNED_AFFINITY = -1;
	private static final int NUM_BUFFERS = 32;

	private final SegmentedBufferBuilder[] builders;
	private final Object2IntMap<RenderLayer> affinities;
	private int nextAffinity;
	private int drawCalls;

	public static FullyBufferedVertexConsumerProvider instance;

	public FullyBufferedVertexConsumerProvider() {
		super(new BufferBuilder(0), Collections.emptyMap());

		this.builders = new SegmentedBufferBuilder[NUM_BUFFERS];

		for (int i = 0; i < this.builders.length; i++) {
			this.builders[i] = new SegmentedBufferBuilder();
		}

		this.affinities = new Object2IntOpenHashMap<>();
		this.affinities.defaultReturnValue(UNASSIGNED_AFFINITY);
		this.nextAffinity = 0;

		this.drawCalls = 0;

		// TODO: Eh
		instance = this;
	}

	@Override
	public VertexConsumer getBuffer(RenderLayer renderLayer) {
		int affinity = affinities.getInt(renderLayer);

		if (affinity == UNASSIGNED_AFFINITY) {
			if (nextAffinity < builders.length) {
				affinity = nextAffinity;
				nextAffinity += 1;
			} else {
				// TODO: Don't just select a random buffer.
				affinity = ThreadLocalRandom.current().nextInt(builders.length);
			}

			affinities.put(renderLayer, affinity);
		}

		return builders[affinity].getBuffer(renderLayer);
	}

	@Override
	public void draw() {
		// TODO: Proper sorting???
		for (SegmentedBufferBuilder builder : builders) {
			List<BufferSegment> segments = builder.getSegments();

			for (BufferSegment segment : segments) {
				drawCalls += 1;
				draw(segment);
			}
		}

		affinities.clear();
		nextAffinity = 0;
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
		int size = 0;

		for (SegmentedBufferBuilder builder : builders) {
			size += builder.getAllocatedSize();
		}

		return size;
	}

	@Override
	public int getUsedSize() {
		int size = 0;

		for (SegmentedBufferBuilder builder : builders) {
			size += builder.getUsedSize();
		}

		return size;
	}
}
