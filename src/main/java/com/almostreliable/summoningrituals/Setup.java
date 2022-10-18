package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.altar.AltarEntity;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public final class Setup {

    private static final DeferredRegister<Block> BLOCKS = createRegistry(ForgeRegistries.BLOCKS);
    private static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = createRegistry(ForgeRegistries.BLOCK_ENTITY_TYPES);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(
        Registry.RECIPE_TYPE_REGISTRY,
        BuildConfig.MOD_ID
    );
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = createRegistry(ForgeRegistries.RECIPE_SERIALIZERS);

    private static final Tab TAB = new Tab(BuildConfig.MOD_ID);
    public static final RegistryObject<AltarBlock> ALTAR_BLOCK = BLOCKS.register(
        Constants.ALTAR,
        () -> new AltarBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2.5f).sound(SoundType.STONE))
    );
    public static final RegistryObject<AltarBlock> INDESTRUCTIBLE_ALTAR_BLOCK = BLOCKS.register(
        Constants.INDESTRUCTIBLE_ALTAR,
        () -> new AltarBlock(BlockBehaviour.Properties.of(Material.STONE)
            .strength(-1.0f, 3_600_000.0f)
            .sound(SoundType.STONE))
    );
    public static final RegistryObject<Item> ALTAR_ITEM = ITEMS.register(
        Constants.ALTAR,
        () -> new BlockItem(ALTAR_BLOCK.get(), new Properties().tab(TAB))
    );
    public static final RegistryObject<Item> INDESTRUCTIBLE_ALTAR_ITEM = ITEMS.register(
        Constants.INDESTRUCTIBLE_ALTAR,
        () -> new BlockItem(INDESTRUCTIBLE_ALTAR_BLOCK.get(), new Properties().tab(TAB))
    );
    public static final RegistryObject<BlockEntityType<AltarEntity>> ALTAR_ENTITY = BLOCK_ENTITIES.register(
        Constants.ALTAR,
        () -> Builder.of(AltarEntity::new, ALTAR_BLOCK.get(), INDESTRUCTIBLE_ALTAR_BLOCK.get()).build(null)
    );
    public static final RecipeEntry<AltarRecipe> ALTAR_RECIPE = RecipeEntry.register(
        Constants.ALTAR,
        AltarRecipeSerializer::new
    );

    private Setup() {}

    static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    private static <T> DeferredRegister<T> createRegistry(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, BuildConfig.MOD_ID);
    }

    private static final class Tab extends CreativeModeTab {

        private Tab(String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ALTAR_ITEM.get());
        }
    }

    public record RecipeEntry<T extends Recipe<?>>(RegistryObject<RecipeType<T>> type, RegistryObject<RecipeSerializer<T>> serializer) {
        @SuppressWarnings("SameParameterValue")
        private static <T extends Recipe<?>> RecipeEntry<T> register(
            String id, Supplier<? extends RecipeSerializer<T>> serializer
        ) {
            RegistryObject<RecipeType<T>> type = RECIPE_TYPES.register(id, () -> new RecipeType<>() {
                public String toString() {
                    return id;
                }
            });
            return new RecipeEntry<>(type, RECIPE_SERIALIZERS.register(id, serializer));
        }
    }
}
