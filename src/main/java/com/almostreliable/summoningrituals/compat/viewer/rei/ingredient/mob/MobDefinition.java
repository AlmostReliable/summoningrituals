package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.viewer.common.MobIngredient;
import com.almostreliable.summoningrituals.compat.viewer.rei.AlmostREI;
import com.almostreliable.summoningrituals.platform.Platform;
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

public class MobDefinition implements EntryDefinition<MobIngredient> {

    private final REIMobRenderer renderer;

    public MobDefinition(int size) {
        renderer = new REIMobRenderer(size);
    }

    @Override
    public Class<MobIngredient> getValueType() {
        return MobIngredient.class;
    }

    @Override
    public EntryType<MobIngredient> getType() {
        return AlmostREI.MOB;
    }

    @Override
    public EntryRenderer<MobIngredient> getRenderer() {
        return renderer;
    }

    @Nullable
    @Override
    public ResourceLocation getIdentifier(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return Platform.getId(mob.entityType);
    }

    @Override
    public boolean isEmpty(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return false;
    }

    @Override
    public MobIngredient copy(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return new MobIngredient(mob.entityType, mob.count, mob.tag);
    }

    @Override
    public MobIngredient normalize(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return new MobIngredient(mob.entityType, mob.count);
    }

    @Override
    public MobIngredient wildcard(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return new MobIngredient(mob.entityType, mob.count);
    }

    @Override
    public long hash(EntryStack<MobIngredient> entry, MobIngredient mob, ComparisonContext context) {
        int code = Platform.getId(mob.entityType).hashCode();
        code = 31 * code + mob.tag.hashCode();
        return code;
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public boolean equals(MobIngredient mob1, MobIngredient mob2, ComparisonContext context) {
        return mob1.entityType == mob2.entityType && mob1.tag.equals(mob2.tag);
    }

    @Nullable
    @Override
    public EntrySerializer<MobIngredient> getSerializer() {
        return null;
    }

    @Override
    public Component asFormattedText(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return mob.displayName;
    }

    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return Platform.getTagsFor(mob.entityType);
    }
}
