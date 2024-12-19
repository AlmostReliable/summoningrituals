package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
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

public class MobDefinition implements EntryDefinition<EntityIngredient> {

    private final REIMobRenderer renderer;

    public MobDefinition(int size) {
        renderer = new REIMobRenderer(size);
    }

    @Override
    public Class<EntityIngredient> getValueType() {
        return EntityIngredient.class;
    }

    @Override
    public EntryType<EntityIngredient> getType() {
        return AlmostREI.MOB;
    }

    @Override
    public EntryRenderer<EntityIngredient> getRenderer() {
        return renderer;
    }

    @Nullable
    @Override
    public ResourceLocation getIdentifier(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return Platform.getId(mob.getEntityType());
    }

    @Override
    public boolean isEmpty(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return false;
    }

    @Override
    public EntityIngredient copy(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return new EntityIngredient(mob.getEntityType(), mob.getCount(), mob.getTag());
    }

    @Override
    public EntityIngredient normalize(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return new EntityIngredient(mob.getEntityType(), mob.getCount());
    }

    @Override
    public EntityIngredient wildcard(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return new EntityIngredient(mob.getEntityType(), mob.getCount());
    }

    @Override
    public long hash(EntryStack<EntityIngredient> entry, EntityIngredient mob, ComparisonContext context) {
        int code = Platform.getId(mob.getEntityType()).hashCode();
        code = 31 * code + mob.getTag().hashCode();
        return code;
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public boolean equals(EntityIngredient mob1, EntityIngredient mob2, ComparisonContext context) {
        return mob1.getEntityType() == mob2.getEntityType() && mob1.getTag().equals(mob2.getTag());
    }

    @Nullable
    @Override
    public EntrySerializer<EntityIngredient> getSerializer() {
        return null;
    }

    @Override
    public Component asFormattedText(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return mob.getDisplayName();
    }

    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<EntityIngredient> entry, EntityIngredient mob) {
        return Platform.getTagsFor(mob.getEntityType());
    }
}
