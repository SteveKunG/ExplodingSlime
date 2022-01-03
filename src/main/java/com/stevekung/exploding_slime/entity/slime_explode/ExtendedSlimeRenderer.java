package com.stevekung.exploding_slime.entity.slime_explode;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ExtendedSlimeRenderer
{
    public static void preRender(LivingEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime)
    {
        float f = ((ISlimeExplode)entitylivingbaseIn).getFlashIntensity(partialTickTime);
        float f11 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.05F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = f * f;
        f = f * f;
        float f22 = (1.0F + f * 0.8F) * f11;
        float f33 = (1.0F + f * 0.4F) / f11;
        matrixStackIn.scale(f22, f33, f22);
    }
}