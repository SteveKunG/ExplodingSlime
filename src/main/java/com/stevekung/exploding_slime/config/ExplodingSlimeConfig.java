package com.stevekung.exploding_slime.config;

import com.stevekung.exploding_slime.ExplodingSlimeMod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class ExplodingSlimeConfig
{
    public static final ForgeConfigSpec.Builder GENERAL_BUILDER = new ForgeConfigSpec.Builder();
    public static final ExplodingSlimeConfig.General GENERAL = new ExplodingSlimeConfig.General(ExplodingSlimeConfig.GENERAL_BUILDER);

    public static class General
    {
        public final ForgeConfigSpec.BooleanValue bigSlimeExplode;
        public final ForgeConfigSpec.BooleanValue slimeAttackAll;
        public final ForgeConfigSpec.BooleanValue poweredSpawn;

        private General(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General settings")
            .push("general");

            this.bigSlimeExplode = builder
                    .translation("Big Slime can be exploded.")
                    .define("bigSlimeExplode", false);
            this.slimeAttackAll = builder
                    .translation("Make Slime attacks all entities.")
                    .define("slimeAttackAll", false);
            this.poweredSpawn = builder
                    .translation("Initial spawn with powered state.")
                    .define("poweredSpawn", false);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onLoad(ModConfig.Loading event)
    {
        ExplodingSlimeMod.LOGGER.info("Loaded config file {}", event.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event)
    {
        ExplodingSlimeMod.LOGGER.info("BestKunG config just got changed on the file system");
    }
}