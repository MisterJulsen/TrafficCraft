package de.mrjulsen.trafficcraft.config;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<ERenderType> GLOW_RENDER_EFFECT;

    static {
        BUILDER.push(ModMain.MOD_ID + "_client_config");
        
        GLOW_RENDER_EFFECT = BUILDER.comment("Implementation of the glow effect. Changing this option may fix compatibility issues with other mods. Restart is required!")
            .defineEnum("glow_shader_renderer", ERenderType.DEFAULT);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
