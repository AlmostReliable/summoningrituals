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
        return Platform.getId(mob.getEntityType());
    }

    @Override
    public boolean isEmpty(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return false;
    }

    @Override
    public MobIngredient copy(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return new MobIngredient(mob.getEntityType(), mob.getCount(), mob.getTag());
    }

    @Override
    public MobIngredient normalize(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return new MobIngredient(mob.getEntityType(), mob.getCount());
    }

    @Override
    public MobIngredient wildcard(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return new MobIngredient(mob.getEntityType(), mob.getCount());
    }

    @Override
    public long hash(EntryStack<MobIngredient> entry, MobIngredient mob, ComparisonContext context) {
        int code = Platform.getId(mob.getEntityType()).hashCode();
        code = 31 * code + mob.getTag().hashCode();
        return code;
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public boolean equals(MobIngredient mob1, MobIngredient mob2, ComparisonContext context) {
        return mob1.getEntityType() == mob2.getEntityType() && mob1.getTag().equals(mob2.getTag());
    }

    @Nullable
    @Override
    public EntrySerializer<MobIngredient> getSerializer() {
        return null;
    }

    @Override
    public Component asFormattedText(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return mob.getDisplayName();
    }

    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<MobIngredient> entry, MobIngredient mob) {
        return Platform.getTagsFor(mob.getEntityType());
    }
}
