package com.stevekung.exploding_slime.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.exploding_slime.config.ExplodingSlimeConfig;
import com.stevekung.exploding_slime.entity.slime_explode.ISlimeExplode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(targets = "net.minecraft.entity.monster.SlimeEntity$AttackGoal")
public abstract class MixinSlimeEntity_AttackGoal extends Goal
{
    private LivingEntity attackTarget;

    @Shadow
    private SlimeEntity slime;

    @Shadow
    private int growTieredTimer;

    @Inject(method = "shouldExecute()Z", at = @At("RETURN"), cancellable = true)
    private void shouldExecute(CallbackInfoReturnable info)
    {
        if (this.slime.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get())
        {
            LivingEntity livingentity = this.slime.getAttackTarget();

            if (livingentity == null)
            {
                info.setReturnValue(false);
            }
            else if (!livingentity.isAlive())
            {
                info.setReturnValue(false);
            }
            else
            {
                info.setReturnValue((((ISlimeExplode)this.slime).getState() > 0 || livingentity != null && !livingentity.isSneaking() && this.slime.getDistanceSq(livingentity) < 128.0D) && this.slime.getMoveHelper() instanceof SlimeEntity.MoveHelperController);
            }
        }
    }

    @Inject(method = "startExecuting()V", at = @At("HEAD"))
    private void startExecuting(CallbackInfo info)
    {
        if (this.slime.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get())
        {
            this.growTieredTimer = 300;
            this.slime.getNavigator().clearPath();
            this.attackTarget = this.slime.getAttackTarget();
        }
    }

    @Inject(method = "shouldContinueExecuting()Z", at = @At("HEAD"), cancellable = true)
    private void shouldContinueExecuting(CallbackInfoReturnable info)
    {
        LivingEntity livingentity = this.slime.getAttackTarget();

        if (this.slime.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get())
        {
            if (livingentity == null)
            {
                info.setReturnValue(false);
            }
            else if (!livingentity.isAlive())
            {
                info.setReturnValue(false);
            }
            else if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).abilities.disableDamage)
            {
                info.setReturnValue(false);
            }
            else
            {
                info.setReturnValue(--this.growTieredTimer > 0);
            }
        }
    }

    @Override
    public void resetTask()
    {
        if (this.slime.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get())
        {
            this.attackTarget = null;
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void tick(CallbackInfo info)
    {
        if (this.slime.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get())
        {
            if (this.attackTarget == null)
            {
                ((ISlimeExplode)this.slime).setState(-1);
            }
            else if (this.slime.getDistanceSq(this.attackTarget) > 12.0D * this.slime.getSlimeSize())
            {
                ((ISlimeExplode)this.slime).setState(-1);
            }
            else if (!this.slime.getEntitySenses().canSee(this.attackTarget))
            {
                ((ISlimeExplode)this.slime).setState(-1);
            }
            else
            {
                this.slime.faceEntity(this.slime.getAttackTarget(), 10.0F, 10.0F);
                ((SlimeEntity.MoveHelperController)this.slime.getMoveHelper()).setDirection(this.slime.rotationYaw, ((ISlimeExplode)this.slime).canDamagePlayer2());

                if (!this.attackTarget.isSneaking())
                {
                    ((ISlimeExplode)this.slime).setState(1);
                }
                else
                {
                    ((ISlimeExplode)this.slime).setState(-1);
                }
            }
        }
    }
}