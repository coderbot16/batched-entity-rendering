package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferSegmentRenderer {
    /**
     * Sets up the render layer, draws the buffer, and then tears down the render layer.
     */
    public void draw(BufferSegment segment) {
        segment.getRenderLayer().startDrawing();
        drawInner(segment);
        segment.getRenderLayer().endDrawing();
    }

    /**
     * Like draw(), but it doesn't setup / tear down the render layer.
     */
    public void drawInner(BufferSegment segment) {
        BufferBuilder.DrawArrayParameters parameters = segment.getParameters();

        draw(segment.getSlice(), parameters.getMode(), parameters.getVertexFormat(), parameters.getCount());
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
}
