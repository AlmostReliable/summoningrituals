package com.almostreliable.summoningrituals.compat.viewer.rei;

import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.SummoningRitualsConstants;
import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.block.BlockReferenceDefinition;
import com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.mob.MobDefinition;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.util.Utils;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class AlmostREI implements REIClientPlugin {

    public static final EntryType<EntityIngredient> MOB = EntryType.deferred(Utils.getRL("mob"));
    public static final EntryType<BlockReference> BLOCK_REFERENCE = EntryType.deferred(Utils.getRL("block_reference"));

    @Override
    public String getPluginProviderName() {
        return SummoningRitualsConstants.MOD_NAME.replace(" ", "");
    }

    @Override
    public void registerEntryTypes(EntryTypeRegistry registry) {
        registry.register(MOB, new MobDefinition(16));
        registry.register(BLOCK_REFERENCE, new BlockReferenceDefinition(20));
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new AltarCategoryREI());

        registry.addWorkstations(AltarCategoryREI.ID, EntryStacks.of(Registration.ALTAR_ITEM.get()));
        registry.addWorkstations(AltarCategoryREI.ID, EntryStacks.of(Registration.INDESTRUCTIBLE_ALTAR_ITEM.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(
            AltarRecipe.class,
            Registration.ALTAR_RECIPE.type().get(),
            AltarCategoryREI.AltarDisplay::new
        );
    }
}
