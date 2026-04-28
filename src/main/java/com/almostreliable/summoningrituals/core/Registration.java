package com.almostreliable.summoningrituals.core;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.dev.PatternGeneratorItem;
import com.almostreliable.summoningrituals.dev.PatternGeneratorState;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import com.almostreliable.summoningrituals.recipe.condition.ConditionRegistry;
import com.almostreliable.summoningrituals.recipe.condition.check.AltarTagBlockStateCheck;
import com.almostreliable.summoningrituals.recipe.condition.check.MoonPhaseCheck;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@SuppressWarnings("ConstantConditions")
public final class Registration {

    // @formatter:off

    // registries
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModConstants.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModConstants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModConstants.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ModConstants.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ModConstants.MOD_ID);
    private static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ModConstants.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModConstants.MOD_ID);

    // blocks
    public static final DeferredBlock<AltarBlock> ALTAR_BLOCK = registerBlock(Constants.ALTAR, "Summoning Altar", AltarBlock::new, p -> p.strength(2.5f));
    public static final DeferredBlock<AltarBlock> INDESTRUCTIBLE_ALTAR_BLOCK = registerBlock(Constants.INDESTRUCTIBLE_ALTAR, "Indestructible Summoning Altar", AltarBlock::new, p -> p.strength(-1.0f, 3_600_000.0f));

    // items
    public static final DeferredItem<PatternGeneratorItem> PATTERN_GENERATOR_ITEM = ITEMS.registerItem("pattern_generator", PatternGeneratorItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    // block entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_BLOCK_ENTITY = registerBlockEntity(ALTAR_BLOCK.getId(), AltarBlockEntity::new, ALTAR_BLOCK, INDESTRUCTIBLE_ALTAR_BLOCK);

    // recipes
    public static final DeferredHolder<RecipeType<?>, RecipeType<AltarRecipe>> ALTAR_RECIPE_TYPE = RECIPE_TYPES.register(
        Constants.ALTAR, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return Constants.ALTAR;
            }
        }
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AltarRecipe>> ALTAR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(Constants.ALTAR, AltarRecipeSerializer::new);

    // conditions
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> ALTAR_TAG_BLOCK_STATE_CONDITION = LOOT_CONDITIONS.register(Constants.BLOCK_TAG_BLOCK_STATE, () -> new LootItemConditionType(AltarTagBlockStateCheck.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> MOON_PHASE_CONDITION = LOOT_CONDITIONS.register(Constants.MOON_PHASE, () -> new LootItemConditionType(MoonPhaseCheck.CODEC));

    // data components
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PatternGeneratorState>> PATTERN_GENERATOR_COMPONENT = DATA_COMPONENTS.registerComponentType(
        "pattern_generator_state",
        builder -> builder.networkSynchronized(PatternGeneratorState.STREAM_CODEC)
    );

    // creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
        "tab", () -> CreativeModeTab.builder()
            .title(SummoningLang.LangEntry.of("tab", "main", ModConstants.MOD_NAME).get())
            .icon(ALTAR_BLOCK::toStack)
            .noScrollBar()
            .displayItems((features, output) -> output.acceptAll(List.of(ALTAR_BLOCK.toStack(), INDESTRUCTIBLE_ALTAR_BLOCK.toStack())))
            .build()
    );

    // @formatter:on

    private Registration() {}

    public static void init(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
        LOOT_CONDITIONS.register(eventBus);
        DATA_COMPONENTS.register(eventBus);

        eventBus.addListener(Registration::onCommonSetup);
        eventBus.addListener(Registration::registerCapabilities);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // delay condition registry until custom loot conditions are registered
        ConditionRegistry.init();
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ALTAR_BLOCK_ENTITY.get(), AltarBlockEntity::getCapability);
    }

    private static <B extends Block> DeferredBlock<B> registerBlock(
        String id, String name, Function<BlockBehaviour.Properties, B> factory,
        UnaryOperator<BlockBehaviour.Properties> propertiesConfigurator
    ) {
        var block = BLOCKS.registerBlock(
            id,
            factory,
            propertiesConfigurator.apply(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .sound(SoundType.STONE))
        );
        ITEMS.registerSimpleBlockItem(block);

        SummoningLang.LangEntry.of("item", id, name);
        SummoningLang.LangEntry.of("block", id, name);
        return block;
    }

    @SuppressWarnings("DataFlowIssue")
    private static <E extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<E>> registerBlockEntity(
        ResourceLocation id,
        BlockEntityType.BlockEntitySupplier<E> factory, DeferredBlock<?>... blocks
    ) {
        return BLOCK_ENTITIES.register(
            id.getPath(), () -> {
                var blockArray = new Block[blocks.length];
                for (var i = 0; i < blocks.length; i++) {
                    blockArray[i] = blocks[i].get();
                }
                return Builder.of(factory, blockArray).build(null);
            }
        );
    }
}
