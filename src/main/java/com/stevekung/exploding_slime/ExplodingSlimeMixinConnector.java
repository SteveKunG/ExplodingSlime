package com.stevekung.exploding_slime;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class ExplodingSlimeMixinConnector implements IMixinConnector
{
    @Override
    public void connect()
    {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.exploding_slime.json");
    }
}