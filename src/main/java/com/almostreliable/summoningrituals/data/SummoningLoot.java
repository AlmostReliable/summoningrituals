package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class SummoningLoot extends LootTableProvider {

    SummoningLoot(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), registries);
    }

    private static final class BlockLoot extends BlockLootSubProvider {

        private BlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(Registration.ALTAR_BLOCK.get());
        }

        @Override
        protected void generate() {
            getKnownBlocks().forEach(this::dropSelf);
        }
    }
}
