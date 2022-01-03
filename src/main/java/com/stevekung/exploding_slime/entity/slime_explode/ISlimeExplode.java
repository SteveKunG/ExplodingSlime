package com.stevekung.exploding_slime.entity.slime_explode;

import net.minecraft.entity.IChargeableMob;

public interface ISlimeExplode extends IChargeableMob
{
    int getState();
    void setState(int state);
    boolean canDamagePlayer2();
    float getFlashIntensity(float partialTicks);
    void setSlimeSize2(int size, boolean resetHealth);
}