package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.block;

import com.almostreliable.summoningrituals.compat.viewer.rei.AlmostREI;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BlockReferenceDefinition implements EntryDefinition<BlockReference> {

    private final REIBlockReferenceRenderer renderer;

    public BlockReferenceDefinition(int size) {
        renderer = new REIBlockReferenceRenderer(size);
    }

    @Override
    public Class<BlockReference> getValueType() {
        return BlockReference.class;
    }

    @Override
    public EntryType<BlockReference> getType() {
        return AlmostREI.BLOCK_REFERENCE;
    }

    @Override
    public EntryRenderer<BlockReference> getRenderer() {
        return renderer;
    }

    @Nullable
    @Override
    public ResourceLocation getIdentifier(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return Platform.getId(blockReference.getDisplayState().getBlock());
    }

    @Override
    public boolean isEmpty(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return false;
    }

    @Override
    public BlockReference copy(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return BlockReference.fromJson(blockReference.toJson());
    }

    @Override
    public BlockReference normalize(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return copy(entry, blockReference);
    }

    @Override
    public BlockReference wildcard(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return copy(entry, blockReference);
    }

    @Override
    public long hash(EntryStack<BlockReference> entry, BlockReference blockReference, ComparisonContext context) {
        int code = Platform.getId(blockReference.getDisplayState().getBlock()).hashCode();
        code = 31 * code + blockReference.getDisplayState().hashCode();
        return code;
    }

    @Override
    public boolean equals(BlockReference blockReference1, BlockReference blockReference2, ComparisonContext context) {
        return blockReference1.test(blockReference2.getDisplayState());
    }

    @Nullable
    @Override
    public EntrySerializer<BlockReference> getSerializer() {
        return null;
    }

    @Override
    public Component asFormattedText(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return blockReference.getDisplayState().getBlock().getName();
    }

    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<BlockReference> entry, BlockReference blockReference) {
        return Platform.getTagsFor(blockReference.getDisplayState().getBlock());
    }
}
