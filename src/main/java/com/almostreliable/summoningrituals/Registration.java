package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.almostreliable.summoningrituals.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;

@SuppressWarnings("ConstantConditions")
public final class Registration {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.Blocks.createBlocks(ModConstants.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(ModConstants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = createRegistry(Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = createRegistry(Registries.RECIPE_TYPE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = createRegistry(Registries.RECIPE_SERIALIZER);

    public static final DeferredBlock<AltarBlock> ALTAR_BLOCK = BLOCKS.registerBlock(
        Constants.ALTAR,
        AltarBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .strength(2.5f)
            .sound(SoundType.STONE)
    );
    public static final DeferredItem<Item> ALTAR_ITEM = ITEMS.register(
        Constants.ALTAR,
        () -> new BlockItem(ALTAR_BLOCK.get(), new Properties())
    );
    public static final DeferredBlock<AltarBlock> INDESTRUCTIBLE_ALTAR_BLOCK = BLOCKS.registerBlock(
        Constants.INDESTRUCTIBLE_ALTAR,
        AltarBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .strength(-1.0f, 3_600_000.0f)
            .sound(SoundType.STONE)
    );
    public static final DeferredItem<Item> INDESTRUCTIBLE_ALTAR_ITEM = ITEMS.register(
        Constants.INDESTRUCTIBLE_ALTAR,
        () -> new BlockItem(INDESTRUCTIBLE_ALTAR_BLOCK.get(), new Properties())
    );
    public static final RecipeEntry<AltarRecipe> ALTAR_RECIPE = RecipeEntry.register(
        Constants.ALTAR,
        AltarRecipeSerializer::new
    );

    private Registration() {}

    static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_ENTITY = BLOCK_ENTITIES.register(
        Constants.ALTAR,
        () -> Builder.of(AltarBlockEntity::new, ALTAR_BLOCK.get(), INDESTRUCTIBLE_ALTAR_BLOCK.get()).build(null)
    );

    private static <T> DeferredRegister<T> createRegistry(ResourceKey<? extends Registry<T>> key) {
        return DeferredRegister.create(key, ModConstants.MOD_ID);
    }

    static final class Tab {
        private static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Utils.getRL("tab")
        );
        private static final CreativeModeTab TAB = CreativeModeTab.builder()
            .title(TextUtils.translate("label", "itemGroup"))
            .icon(() -> new ItemStack(ALTAR_BLOCK.get()))
            .noScrollBar()
            .build();

        private Tab() {}

        static void initContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == TAB_KEY) {
                event.accept(ALTAR_BLOCK);
                event.accept(INDESTRUCTIBLE_ALTAR_BLOCK);
            }
        }

        static void registerTab(RegisterEvent event) {
            event.register(Registries.CREATIVE_MODE_TAB, TAB_KEY.location(), () -> TAB);
        }
    }

    //
    // public record RecipeEntry<T extends Recipe<?>>(RegistryObject<RecipeType<T>> type, RegistryObject<? extends RecipeSerializer<T>> serializer) {
    //     @SuppressWarnings("SameParameterValue")
    //     private static <T extends Recipe<?>> RecipeEntry<T> register(
    //         String id, Supplier<? extends RecipeSerializer<T>> serializer
    //     ) {
    //         RegistryObject<RecipeType<T>> type = RECIPE_TYPES.register(id, () -> new RecipeType<>() {
    //             public String toString() {
    //                 return id;
    //             }
    //         });
    //         return new RecipeEntry<>(type, RECIPE_SERIALIZERS.register(id, serializer));
    //     }
    // }
}
