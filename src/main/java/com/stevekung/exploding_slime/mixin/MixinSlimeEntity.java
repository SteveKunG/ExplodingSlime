package com.stevekung.exploding_slime.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.exploding_slime.config.ExplodingSlimeConfig;
import com.stevekung.exploding_slime.entity.slime_explode.ISlimeExplode;

import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Explosion;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(SlimeEntity.class)
public abstract class MixinSlimeEntity extends MobEntity implements IMob, ISlimeExplode
{
    private final SlimeEntity that = (SlimeEntity) (Object) this;
    private static final DataParameter<Integer> STATE = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> POWERED = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IGNITED = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.BOOLEAN);
    private int lastActiveTime;
    private int timeSinceIgnited;
    private int fuseTime = 30;
    private int explosionRadius = 3;

    @Shadow
    protected abstract void setSlimeSize(int size, boolean resetHealth);

    @Shadow
    protected abstract boolean canDamagePlayer();

    private MixinSlimeEntity()
    {
        super(null, null);
    }

    @Inject(method = "registerGoals()V", at = @At("RETURN"))
    private void addAttackAll(CallbackInfo info)
    {
        if (ExplodingSlimeConfig.GENERAL.slimeAttackAll.get())
        {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> !(entity instanceof SlimeEntity)));
        }
    }

    @Inject(method = "onInitialSpawn(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/ILivingEntityData;Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/entity/ILivingEntityData;", at = @At("HEAD"))
    private void onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag, CallbackInfoReturnable<ILivingEntityData> info)
    {
        this.getAttributeManager().createInstanceIfAbsent(Attributes.FOLLOW_RANGE).setBaseValue(16 + this.that.getSlimeSize() * 0.25D);

        if (ExplodingSlimeConfig.GENERAL.poweredSpawn.get())
        {
            this.dataManager.set(POWERED, true);
        }
    }

    @Inject(method = "registerData()V", at = @At("RETURN"))
    private void registerData(CallbackInfo info)
    {
        this.dataManager.register(STATE, -1);
        this.dataManager.register(POWERED, false);
        this.dataManager.register(IGNITED, false);
    }

    @Inject(method = "writeAdditional(Lnet/minecraft/nbt/CompoundNBT;)V", at = @At("RETURN"))
    private void writeAdditional(CompoundNBT compound, CallbackInfo info)
    {
        if (this.dataManager.get(POWERED))
        {
            compound.putBoolean("powered", true);
        }
        compound.putShort("Fuse", (short)this.fuseTime);
        compound.putByte("ExplosionRadius", (byte)this.explosionRadius);
        compound.putBoolean("ignited", this.hasIgnited());
    }

    @Inject(method = "readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V", at = @At("RETURN"))
    private void readAdditional(CompoundNBT compound, CallbackInfo info)
    {
        this.dataManager.set(POWERED, compound.getBoolean("powered"));

        if (compound.contains("Fuse", 99))
        {
            this.fuseTime = compound.getShort("Fuse");
        }
        if (compound.contains("ExplosionRadius", 99))
        {
            this.explosionRadius = compound.getByte("ExplosionRadius");
        }
        if (compound.getBoolean("ignited"))
        {
            this.ignite();
        }
    }

    private boolean hasIgnited()
    {
        return this.dataManager.get(IGNITED);
    }

    private void ignite()
    {
        this.dataManager.set(IGNITED, true);
    }

    @Override
    public int getState()
    {
        return this.dataManager.get(STATE);
    }

    @Override
    public void setState(int state)
    {
        this.dataManager.set(STATE, state);
    }

    @Override
    public boolean isCharged()
    {
        return this.dataManager.get(POWERED);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getFlashIntensity(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastActiveTime, this.timeSinceIgnited) / (this.fuseTime - 2);
    }

    private void explode()
    {
        if (!this.world.isRemote)
        {
            Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
            float f = ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get() ? this.isCharged() ? 1.0F * this.that.getSlimeSize() : 0.25F * this.that.getSlimeSize() : this.isCharged() ? 1.0F : 0.25F;
            this.dead = true;
            this.world.createExplosion(this, this.getPosX(), this.getPosY(), this.getPosZ(), this.explosionRadius * f, explosion$mode);

            if (this.world instanceof ServerWorld)
            {
                ((ServerWorld)this.world).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(this.that instanceof MagmaCubeEntity ? Items.MAGMA_CREAM : Items.SLIME_BALL)), this.getPosX(), this.getPosYHeight(0.6666666666666666D), this.getPosZ(), 10, (double)(this.getWidth() / 4.0F), (double)(this.getHeight() / 4.0F), (double)(this.getWidth() / 4.0F), 0.05D);
            }
            this.remove();
        }
    }

    @Override
    public void func_241841_a(ServerWorld p_241841_1_, LightningBoltEntity p_241841_2_)
    {
        super.func_241841_a(p_241841_1_, p_241841_2_);
        this.dataManager.set(POWERED, true);
    }

    @Override
    protected ActionResultType func_230254_b_(PlayerEntity p_230254_1_, Hand p_230254_2_)
    {
        ItemStack itemstack = p_230254_1_.getHeldItem(p_230254_2_);

        if (itemstack.getItem() == Items.FLINT_AND_STEEL && (this.that.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get()))
        {
            this.world.playSound(p_230254_1_, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ITEM_FLINTANDSTEEL_USE, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);

            if (!this.world.isRemote)
            {
                this.ignite();
                itemstack.damageItem(1, p_230254_1_, player -> player.sendBreakAnimation(p_230254_2_));
            }
            return ActionResultType.func_233537_a_(this.world.isRemote);
        }
        else
        {
            return super.func_230254_b_(p_230254_1_, p_230254_2_);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void remove(boolean keepData)
    {
        int i = this.that.getSlimeSize();

        if (!this.world.isRemote && i > 1 && this.getShouldBeDead() && !this.removed)
        {
            ITextComponent itextcomponent = this.getCustomName();
            boolean flag = this.isAIDisabled();
            float f = i / 4.0F;
            int j = i / 2;
            int k = 2 + this.rand.nextInt(3);

            for (int l = 0; l < k; ++l)
            {
                float f1 = (l % 2 - 0.5F) * f;
                float f2 = (l / 2 - 0.5F) * f;
                SlimeEntity slimeentity = this.that.getType().create(this.world);

                if (this.isNoDespawnRequired())
                {
                    slimeentity.enablePersistence();
                }

                slimeentity.setCustomName(itextcomponent);
                slimeentity.setNoAI(flag);
                slimeentity.setInvulnerable(this.isInvulnerable());
                ((ISlimeExplode)slimeentity).setSlimeSize2(j, true);
                slimeentity.getDataManager().set(POWERED, this.dataManager.get(POWERED));
                slimeentity.setLocationAndAngles(this.getPosX() + f1, this.getPosY() + 0.5D, this.getPosZ() + f2, this.rand.nextFloat() * 360.0F, 0.0F);
                this.world.addEntity(slimeentity);
            }
        }
        super.remove(keepData);
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void tick(CallbackInfo info)
    {
        if (this.isAlive() && (this.that.isSmallSlime() || ExplodingSlimeConfig.GENERAL.bigSlimeExplode.get()))
        {
            this.lastActiveTime = this.timeSinceIgnited;

            if (this.hasIgnited())
            {
                this.setState(1);
            }

            int i = this.getState();

            if (i > 0 && this.timeSinceIgnited == 0)
            {
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
            }

            this.timeSinceIgnited += i;

            if (this.timeSinceIgnited < 0)
            {
                this.timeSinceIgnited = 0;
            }
            if (this.timeSinceIgnited >= this.fuseTime)
            {
                this.timeSinceIgnited = this.fuseTime;
                this.explode();
            }
        }
    }

    @Override
    public boolean canDamagePlayer2()
    {
        return this.canDamagePlayer();
    }

    @Override
    public void setSlimeSize2(int size, boolean resetHealth)
    {
        this.setSlimeSize(size, resetHealth);
    }
}