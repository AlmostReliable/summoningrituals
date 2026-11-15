package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Optional;

class SummoningModels extends ModelProvider {

    private static final TextureSlot TEXTURE = TextureSlot.create("texture");

    SummoningModels(PackOutput output) {
        super(output, ModConstants.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var altarBlock = Registration.ALTAR_BLOCK;
        var altarBlockId = altarBlock.getId();
        var altarBlockModelPath = SummoningRituals.getRL("block/" + altarBlockId.getPath());
        var altarBlockModelPathActive = altarBlockModelPath.withSuffix("_active");
        new ModelTemplate(Optional.of(altarBlockModelPath), Optional.empty(), TEXTURE).create(
            altarBlockModelPathActive,
            new TextureMapping().put(TEXTURE, new Material(altarBlockModelPathActive)),
            blockModels.modelOutput
        );

        altarBlockState(blockModels, altarBlock, altarBlockModelPath, altarBlockModelPathActive);
        itemModels.itemModelOutput.accept(altarBlock.asItem(), ItemModelUtils.plainModel(altarBlockModelPath));

        var indestructibleAltarBlock = Registration.INDESTRUCTIBLE_ALTAR_BLOCK;

        altarBlockState(blockModels, indestructibleAltarBlock, altarBlockModelPath, altarBlockModelPathActive);
        itemModels.itemModelOutput.accept(indestructibleAltarBlock.asItem(), ItemModelUtils.plainModel(altarBlockModelPath));

        var bambooModelPath = ModelLocationUtils.getModelLocation(Items.BAMBOO);
        itemModels.itemModelOutput.accept(Registration.PATTERN_GENERATOR_ITEM.get(), ItemModelUtils.plainModel(bambooModelPath));
    }

    private static void altarBlockState(BlockModelGenerators blockModels, DeferredBlock<?> block, Identifier model, Identifier modelActive) {
        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.dispatch(block.get())
                .with(PropertyDispatch.initial(AltarBlock.ACTIVE)
                    .generate(active -> BlockModelGenerators.plainVariant(active ? modelActive : model)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
        );
    }
}
