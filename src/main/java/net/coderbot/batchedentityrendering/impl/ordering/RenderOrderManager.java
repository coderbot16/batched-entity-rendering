package net.coderbot.batchedentityrendering.impl.ordering;

import net.minecraft.client.render.RenderLayer;

public interface RenderOrderManager {
    void begin(RenderLayer layer);
    void startGroup();
    void endGroup();
    void reset();
    Iterable<RenderLayer> getRenderOrder();
}
