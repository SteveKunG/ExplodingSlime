package com.stevekung.exploding_slime.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.stevekung.exploding_slime.entity.slime_explode.ExtendedSlimeRenderer;
import com.stevekung.exploding_slime.entity.slime_explode.ISlimeExplode;
import com.stevekung.exploding_slime.entity.slime_explode.SlimeChargeLayer;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.MagmaCubeModel;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.util.math.MathHelper;

@Mixin(MagmaCubeRenderer.class)
public abstract class MixinMagmaCubeRenderer extends MobRenderer<MagmaCubeEntity, MagmaCubeModel<MagmaCubeEntity>>
{
    private final MagmaCubeRenderer that = (MagmaCubeRenderer)(Object)this;

    private MixinMagmaCubeRenderer()
    {
        super(null, null, 0);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererManager;)V", at = @At("RETURN"))
    private void addChargedSlime(EntityRendererManager renderManagerIn, CallbackInfo info)
    {
        this.addLayer(new SlimeChargeLayer(this.that, this.entityModel));
    }

    @Inject(method = "preRenderCallback(Lnet/minecraft/entity/monster/MagmaCubeEntity;Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At("RETURN"))
    private void preRenderCallback(MagmaCubeEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime, CallbackInfo info)
    {
        ExtendedSlimeRenderer.preRender(entitylivingbaseIn, matrixStackIn, partialTickTime);
    }

    @Override
    protected float getOverlayProgress(MagmaCubeEntity livingEntityIn, float partialTicks)
    {
        float f = ((ISlimeExplode)livingEntityIn).getFlashIntensity(partialTicks);
        return (int)(f * 10.0F) % 2 == 0 ? 0.0F : MathHelper.clamp(f, 0.5F, 1.0F);
    }
}