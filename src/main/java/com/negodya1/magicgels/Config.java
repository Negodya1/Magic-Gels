package com.negodya1.magicgels;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MagicGels.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue STACKING_EFFECTS = BUILDER
            .comment("Effect from magic gel will extend exist effect")
            .define("stackingEffects", true);

    private static final ForgeConfigSpec.IntValue EFFECT_DURATION_PER_HIT = BUILDER
            .comment("Effect duration per hit. Potion duration / effect duration = magic gel uses")
            .defineInRange("effectDurationPerHit", 5, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MAGIC_GELS_LIMIT = BUILDER
            .comment("Max amount of magic gels applied for single item")
            .defineInRange("magicGelsLimit", 2, 1, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean stackingEffects;
    public static int effectDurationPerHit;
    public static int magicGelsLimit;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        stackingEffects = STACKING_EFFECTS.get();
        effectDurationPerHit = EFFECT_DURATION_PER_HIT.get();
        magicGelsLimit = MAGIC_GELS_LIMIT.get();
    }
}
