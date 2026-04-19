package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

class SummoningModels extends BlockStateProvider {

    SummoningModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var altarBlock = Registration.ALTAR_BLOCK;
        var altarBlockId = altarBlock.getId();
        var altarBlockModelPath = SummoningRituals.getRL("block/" + altarBlockId.getPath());

        altarBlockStateAndModel(altarBlock, altarBlockModelPath);
        itemModels().simpleBlockItem(altarBlock.get());

        var indestructibleAltarBlock = Registration.INDESTRUCTIBLE_ALTAR_BLOCK;

        altarBlockStateAndModel(indestructibleAltarBlock, altarBlockModelPath);
        itemModels().withExistingParent(indestructibleAltarBlock.getId().toString(), altarBlockModelPath);

        var patternGeneratorId = Registration.PATTERN_GENERATOR_ITEM.getId();
        var bambooId = BuiltInRegistries.ITEM.getKey(Items.BAMBOO);
        itemModels().withExistingParent(patternGeneratorId.getPath(), bambooId);
    }

    private void altarBlockStateAndModel(DeferredBlock<?> block, ResourceLocation modelPath) {
        var model = models().getExistingFile(modelPath);
        var modelPathActive = modelPath.getPath() + "_active";
        var modelActive = models().getBuilder(modelPathActive).parent(model).texture("texture", modelPathActive);

        getVariantBuilder(block.get()).forAllStatesExcept(
            state -> {
                var facing = state.getValue(AltarBlock.FACING);
                boolean active = state.getValue(AltarBlock.ACTIVE);
                return ConfiguredModel.builder()
                    .modelFile(active ? modelActive : model)
                    .rotationY(((int) facing.toYRot() + 180) % 360)
                    .build();
            }, AltarBlock.WATERLOGGED
        );
    }
}
