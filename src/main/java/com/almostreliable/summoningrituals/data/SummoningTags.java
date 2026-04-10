package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SummoningTags extends BlockTagsProvider {

    public static final TagKey<Block> ALTARS = TagKey.create(Registries.BLOCK, SummoningRituals.getRL("altars"));

    SummoningTags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(Registration.ALTAR_BLOCK.get());
        tag(ALTARS).add(Registration.ALTAR_BLOCK.get(), Registration.INDESTRUCTIBLE_ALTAR_BLOCK.get());
    }
}
