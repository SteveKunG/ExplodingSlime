package com.stevekung.exploding_slime.entity.slime_explode;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ExtendedEnergyLayer<T extends Entity, M extends EntityModel<T>> extends LayerRenderer<T, M>
{
    public ExtendedEnergyLayer(IEntityRenderer<T, M> renderer)
    {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (((ISlimeExplode)entitylivingbaseIn).isCharged())
        {
            matrixStackIn.scale(1.25F, 1.25F, 1.25F);
            matrixStackIn.translate(0.0D, -0.25D, 0.0D);
            float f = entitylivingbaseIn.ticksExisted + partialTicks;
            EntityModel<T> entitymodel = this.func_225635_b_();
            entitymodel.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.getEntityModel().copyModelAttributesTo(entitymodel);
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEnergySwirl(this.func_225633_a_(), this.func_225634_a_(f), f * 0.01F));
            entitymodel.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            entitymodel.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
        }
    }

    protected abstract float func_225634_a_(float p_225634_1_);

    protected abstract ResourceLocation func_225633_a_();

    protected abstract EntityModel<T> func_225635_b_();
}