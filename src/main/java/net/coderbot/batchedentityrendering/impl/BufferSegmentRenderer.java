package net.coderbot.batchedentityrendering.impl;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;

import java.nio.ByteBuffer;

public class BufferSegmentRenderer {
    private final BufferBuilder fakeBufferBuilder;
    private final BufferBuilderExt fakeBufferBuilderExt;

    public BufferSegmentRenderer() {
        this.fakeBufferBuilder = new BufferBuilder(0);
        this.fakeBufferBuilderExt = (BufferBuilderExt) this.fakeBufferBuilder;
    }

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
        fakeBufferBuilderExt.setupBufferSlice(segment.getSlice(), segment.getParameters());
        BufferRenderer.draw(fakeBufferBuilder);
        fakeBufferBuilderExt.teardownBufferSlice();
    }

    private static void setInternalBuffer(BufferBuilder target, ByteBuffer buffer, BufferBuilder.DrawArrayParameters parameters) {
        // target.buffer = buffer;
        // target.parameters = new ArrayList<>(1);
        // target.parameters.add(parameters);

        // target.lastParameterIndex does not need modification.
        // target.buildStart = parameters.getCount() * parameters.getVertexFormat().getVertexSize();
        // target.elementOffset = target.buildStart;
        // target.nextDrawStart is zero as it should be.

        // target.vertexCount is never nonzero in this process.
        // target.currentElement is never non-null in this process.
        // target.currentElementId is never nonzero.
        // target.drawMode is irrelevant.
        // target.format is irrelevant.
        // The final 3 booleans are also irrelvant.

        // TODO
    }

    private static void unsetInternalBuffer(BufferBuilder target) {
        // target.buffer = null;
        // target.parameters gets reset.
        // target.lastParameterIndex gets reset.
        // target.buildStart gets reset.
        // target.elementOffset gets reset.
        // target.nextDrawStart gets reset.

        // target.vertexCount is never nonzero in this process.
        // target.currentElement is never non-null in this process.
        // target.currentElementId is never nonzero.
        // target.drawMode is irrelevant.
        // target.format is irrelevant.
        // The final 3 booleans are also irrelvant.

        // TODO
    }
}
