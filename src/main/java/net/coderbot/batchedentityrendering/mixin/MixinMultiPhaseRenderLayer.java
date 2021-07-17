package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/render/RenderLayer$MultiPhase")
public abstract class MixinMultiPhaseRenderLayer extends RenderLayer implements BlendingStateHolder {
	@Unique
	private boolean hasBlending;

	private MixinMultiPhaseRenderLayer(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void batchedentityrendering$onMultiPhaseInit(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize,
									   boolean hasCrumbling, boolean translucent, MultiPhaseParameters phases,
									   CallbackInfo ci) {
		Transparency transparency = ((MultiPhaseParametersAccessor) (Object) phases).getTransparency();
		hasBlending = transparency != RenderPhaseAccessor.getNO_TRANSPARENCY();
	}

	@Override
	public boolean hasBlend() {
		return hasBlending;
	}
}
