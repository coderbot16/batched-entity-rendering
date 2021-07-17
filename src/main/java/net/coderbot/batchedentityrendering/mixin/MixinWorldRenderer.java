package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.ExtendedBufferStorage;
import net.coderbot.batchedentityrendering.impl.FlushableVertexConsumerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Shadow
	@Final
	private BufferBuilderStorage bufferBuilders;

	@Inject(method = "render", at = @At("HEAD"))
	private void batchedentityrendering$beginWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		((ExtendedBufferStorage) bufferBuilders).beginWorldRendering();
	}

	@Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void batchedentityrendering$beginTranslucents(MatrixStack matrices, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
										CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f,
										Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										VertexConsumerProvider.Immediate immediate) {
		profiler.swap("opaque_entity_draws");

		if (immediate instanceof FlushableVertexConsumerProvider) {
			((FlushableVertexConsumerProvider) immediate).flushNonTranslucentContent();
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/render/RenderLayer.getTranslucent ()Lnet/minecraft/client/render/RenderLayer;"))
	private void batchedentityrendering$preRenderTranslucentTerrain(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		VertexConsumerProvider.Immediate vertexConsumers = bufferBuilders.getEntityVertexConsumers();

		if (vertexConsumers instanceof FlushableVertexConsumerProvider) {
			MinecraftClient.getInstance().getProfiler().swap("translucent_entity_draws");
			((FlushableVertexConsumerProvider) vertexConsumers).flushTranslucentContent();
			MinecraftClient.getInstance().getProfiler().swap("translucent");
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void batchedentityrendering$fantastic$endWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		((ExtendedBufferStorage) bufferBuilders).endWorldRendering();
	}
}
