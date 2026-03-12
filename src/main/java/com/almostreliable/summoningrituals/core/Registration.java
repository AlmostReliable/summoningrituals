package com.almostreliable.summoningrituals.core;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import com.almostreliable.summoningrituals.recipe.condition.ConditionRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.function.Function;

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

    // blocks
    public static final DeferredBlock<AltarBlock> ALTAR_BLOCK = registerBlock(Constants.ALTAR, "Summoning Altar", AltarBlock::new, p -> p.strength(2.5f));
    public static final DeferredBlock<AltarBlock> INDESTRUCTIBLE_ALTAR_BLOCK = registerBlock(Constants.INDESTRUCTIBLE_ALTAR, "Indestructible Summoning Altar", AltarBlock::new, p -> p.strength(-1.0f, 3_600_000.0f));

    // block entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_BLOCK_ENTITY = registerBlockEntity(ALTAR_BLOCK.getId(), AltarBlockEntity::new, ALTAR_BLOCK, INDESTRUCTIBLE_ALTAR_BLOCK);

    // creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
        "tab", () -> CreativeModeTab.builder()
            .title(SummoningLang.LangEntry.of("tab", "main", ModConstants.MOD_NAME).get())
            .icon(ALTAR_BLOCK::toStack)
            .noScrollBar()
            .displayItems((features, output) -> output.acceptAll(getKnownItems()))
            .build()
    );

    // @formatter:on

    public static final DeferredHolder<RecipeType<?>, RecipeType<AltarRecipe>> ALTAR_RECIPE_TYPE = RECIPE_TYPES.register(
        Constants.ALTAR, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return Constants.ALTAR;
            }
        }
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AltarRecipe>> ALTAR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
        Constants.ALTAR,
        AltarRecipeSerializer::new
    );

    private Registration() {}

    public static void init(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
        ConditionRegistry.init();

        eventBus.addListener(Registration::registerCapabilities);
    }

    private static Collection<ItemStack> getKnownItems() {
        return ITEMS.getEntries().stream().map(e -> e.value().getDefaultInstance()).toList();
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ALTAR_BLOCK_ENTITY.get(), AltarBlockEntity::getCapability);
    }

    private static <B extends Block> DeferredBlock<B> registerBlock(
        String id, String name, Function<BlockBehaviour.Properties, B> factory,
        Function<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesConfigurator
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
