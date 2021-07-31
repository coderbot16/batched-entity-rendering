package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.ExtendedBufferStorage;
import net.coderbot.batchedentityrendering.impl.FlushableVertexConsumerProvider;
import net.coderbot.batchedentityrendering.impl.FullyBufferedVertexConsumerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Shadow
	@Final
	private BufferBuilderStorage bufferBuilders;

	@Inject(method = "render", at = @At("HEAD"))
	private void batchedentityrendering$beginWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		if (FullyBufferedVertexConsumerProvider.instance != null) {
			FullyBufferedVertexConsumerProvider.instance.resetDrawCalls();
		}

		((ExtendedBufferStorage) bufferBuilders).beginWorldRendering();
	}

	@Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void batchedentityrendering$beginTranslucents(MatrixStack matrices, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
										CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f,
										Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										VertexConsumerProvider.Immediate immediate) {
		profiler.swap("entity_draws");
		immediate.draw();
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void batchedentityrendering$fantastic$endWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		((ExtendedBufferStorage) bufferBuilders).endWorldRendering();
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/world/ClientWorld.getEntities ()Ljava/lang/Iterable;"))
	private Iterable<Entity> batchedentityrendering$sortEntityList(ClientWorld world) {
		world.getProfiler().push("sortEntityList");

		// Sort the entity list first in order to allow vanilla's entity batching code to work better.
		Iterable<Entity> entityIterable = world.getEntities();

		Map<EntityType<?>, List<Entity>> sortedEntities = new HashMap<>();

		List<Entity> entities = new ArrayList<>();
		entityIterable.forEach(entity -> {
			sortedEntities.computeIfAbsent(entity.getType(), entityType -> new ArrayList<>(32)).add(entity);
		});

		sortedEntities.values().forEach(entities::addAll);

		world.getProfiler().pop();

		return entities;
	}
}
