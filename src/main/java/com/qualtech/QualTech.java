package com.qualtech;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import java.util.function.Supplier;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(QualTech.MODID)
public class QualTech {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "qualtech";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "qualtech" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "qualtech" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "qualtech" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Create a Deferred Register to hold BlockEntityTypes which will all be registered under the "qualtech" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    // Create a Deferred Register to hold MenuTypes which will all be registered under the "qualtech" namespace
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    // Creates a new Block with the id "qualtech:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "qualtech:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new Block with the id "qualtech:qualtech_block", combining the namespace and path
    public static final DeferredBlock<Block> QUALTECH_BLOCK = BLOCKS.registerSimpleBlock("qualtech_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.5f));
    // Creates a new BlockItem with the id "qualtech:qualtech_block", combining the namespace and path
    public static final DeferredItem<BlockItem> QUALTECH_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("qualtech_block", QUALTECH_BLOCK);

    // Creates a new RF/FE-powered machine block with the id "qualtech:energy_cell"
    public static final DeferredBlock<EnergyCellBlock> ENERGY_CELL = BLOCKS.register("energy_cell",
            () -> new EnergyCellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()));
    // Creates a new BlockItem with the id "qualtech:energy_cell", combining the namespace and path
    public static final DeferredItem<BlockItem> ENERGY_CELL_ITEM = ITEMS.registerSimpleBlockItem("energy_cell", ENERGY_CELL);
    // Creates the BlockEntityType backing the energy cell's block entity
    public static final Supplier<BlockEntityType<EnergyCellBlockEntity>> ENERGY_CELL_BE = BLOCK_ENTITY_TYPES.register("energy_cell",
            () -> BlockEntityType.Builder.of(EnergyCellBlockEntity::new, ENERGY_CELL.get()).build(null));

    // Creates a new ore-grinding RF/FE-powered machine block with the id "qualtech:ore_grinder"
    public static final DeferredBlock<GrinderBlock> ORE_GRINDER = BLOCKS.register("ore_grinder",
            () -> new GrinderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(4.0f).requiresCorrectToolForDrops()));
    // Creates a new BlockItem with the id "qualtech:ore_grinder", combining the namespace and path
    public static final DeferredItem<BlockItem> ORE_GRINDER_ITEM = ITEMS.registerSimpleBlockItem("ore_grinder", ORE_GRINDER);
    // Creates the BlockEntityType backing the ore grinder's block entity
    public static final Supplier<BlockEntityType<GrinderBlockEntity>> ORE_GRINDER_BE = BLOCK_ENTITY_TYPES.register("ore_grinder",
            () -> BlockEntityType.Builder.of(GrinderBlockEntity::new, ORE_GRINDER.get()).build(null));
    // Creates the MenuType used to open the ore grinder's screen
    public static final Supplier<MenuType<GrinderMenu>> ORE_GRINDER_MENU = MENUS.register("ore_grinder",
            () -> IMenuTypeExtension.create(GrinderMenu::new));

    // Dust items produced by grinding raw ore in the Ore Grinder, smeltable back into their ingot
    public static final DeferredItem<Item> IRON_DUST = ITEMS.registerSimpleItem("iron_dust", new Item.Properties());
    public static final DeferredItem<Item> GOLD_DUST = ITEMS.registerSimpleItem("gold_dust", new Item.Properties());
    public static final DeferredItem<Item> COPPER_DUST = ITEMS.registerSimpleItem("copper_dust", new Item.Properties());

    // Creates a new food item with the id "qualtech:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "qualtech:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.qualtech")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());// Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public QualTech(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so menu types get registered
        MENUS.register(modEventBus);

        // Expose the energy cell's RF/FE storage as a capability, so cables from other mods (e.g.
        // Mekanism, Immersive Engineering) can charge/discharge it when placed next to it
        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (QualTech) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
            event.accept(QUALTECH_BLOCK_ITEM);
            event.accept(ENERGY_CELL_ITEM);
            event.accept(ORE_GRINDER_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(IRON_DUST);
            event.accept(GOLD_DUST);
            event.accept(COPPER_DUST);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ENERGY_CELL_BE.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ORE_GRINDER_BE.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ORE_GRINDER_BE.get(),
                (blockEntity, side) -> blockEntity.getItemHandler());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
