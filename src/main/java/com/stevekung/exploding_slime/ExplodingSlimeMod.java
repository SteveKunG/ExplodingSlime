package com.stevekung.exploding_slime;

import com.stevekung.exploding_slime.config.ExplodingSlimeConfig;
import com.stevekung.stevekungslib.utils.CommonUtils;
import com.stevekung.stevekungslib.utils.LoggerBase;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ExplodingSlimeMod.MOD_ID)
public class ExplodingSlimeMod
{
    public static final String MOD_ID = "exploding_slime";
    public static final LoggerBase LOGGER = new LoggerBase("ExplodingSlime");

    public ExplodingSlimeMod()
    {
        CommonUtils.registerConfig(ModConfig.Type.COMMON, ExplodingSlimeConfig.GENERAL_BUILDER);
        CommonUtils.registerModEventBus(ExplodingSlimeConfig.class);
    }
}