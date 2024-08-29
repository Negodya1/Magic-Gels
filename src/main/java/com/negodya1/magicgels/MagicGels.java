package com.negodya1.magicgels;

import com.mojang.logging.LogUtils;
import com.negodya1.magicgels.item.MagicGelItem;
import com.negodya1.magicgels.item.MagicSpongeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MagicGels.MODID)
public class MagicGels {
    public static final String MODID = "magicgels";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static void logThis(String str) {
        LOGGER.info(str);
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> MAGIC_GEL = ITEMS.register("magic_gel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GEL = ITEMS.register("gel", () -> new MagicGelItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> MAGIC_SPONGE = ITEMS.register("magic_sponge", () -> new MagicSpongeItem(new Item.Properties().stacksTo(1).durability(8)));

    public static final RegistryObject<CreativeModeTab> MAGIC_GELS_TAB = CREATIVE_MODE_TABS.register("magic_gels", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> MAGIC_GEL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(MAGIC_GEL.get());
                output.accept(MAGIC_SPONGE.get());
                parameters.holders().lookup(Registries.POTION).ifPresent((p_269993_) -> {
                    generatePotionEffectTypes(output, p_269993_, GEL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });
            }).build());

    public MagicGels() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);

        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PotionBrewing.addContainer(GEL.get());
        PotionBrewing.addContainerRecipe(Items.POTION, MAGIC_GEL.get(), GEL.get());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    private static void generatePotionEffectTypes(CreativeModeTab.Output p_270129_, HolderLookup<Potion> p_270334_, Item p_270968_, CreativeModeTab.TabVisibility p_270778_) {
        p_270334_.listElements().filter((p_270012_) -> {
            return !p_270012_.is(Potions.EMPTY_ID);
        }).map((p_269986_) -> {
            return PotionUtils.setPotion(new ItemStack(p_270968_), p_269986_.value());
        }).forEach((p_270000_) -> {
            p_270129_.accept(p_270000_, p_270778_);
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
