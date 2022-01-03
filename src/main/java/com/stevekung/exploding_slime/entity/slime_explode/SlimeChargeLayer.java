package com.stevekung.exploding_slime.entity.slime_explode;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeChargeLayer<E extends LivingEntity, M extends EntityModel<E>> extends ExtendedEnergyLayer<E, M>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final M model;

    public SlimeChargeLayer(IEntityRenderer<E, M> renderer, M model)
    {
        super(renderer);
        this.model = model;
    }

    @Override
    protected float func_225634_a_(float p_225634_1_)
    {
        return p_225634_1_ * 0.01F;
    }

    @Override
    protected ResourceLocation func_225633_a_()
    {
        return TEXTURE;
    }

    @Override
    protected EntityModel<E> func_225635_b_()
    {
        return this.model;
    }
}