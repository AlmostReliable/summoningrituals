package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningOutputBinding;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.AltarKubeRecipe;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.AltarRecipeSchema;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityInputsComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.ItemOutputComponent;
import com.almostreliable.summoningrituals.core.Registration;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class KubePlugin implements KubeJSPlugin {

    @Override
    public void init() {
        AltarBlockEntity.SUMMONING_START.register((level, pos, recipeInfo, player) ->
            Events.SUMMONING_START.post(new SummoningKubeEvent(level, pos, recipeInfo, player)).interruptFalse());
        AltarBlockEntity.SUMMONING_COMPLETE.register((level, pos, recipeInfo, player) ->
            Events.SUMMONING_COMPLETE.post(new SummoningKubeEvent(level, pos, recipeInfo, player)).interruptFalse());
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(Events.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry registry) {
        if (!registry.type().isServer()) return;
        registry.add("SummoningOutput", SummoningOutputBinding.class);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistry registry) {
        registry.register(EntityInputsComponent.INSTANCE);
        registry.register(ItemOutputComponent.INSTANCE);
        registry.register(EntityOutputComponent.INSTANCE);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(AltarKubeRecipe.FACTORY);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(Registration.ALTAR_RECIPE_TYPE.getId(), AltarRecipeSchema.SCHEMA);
    }

    public interface Events {

        EventGroup GROUP = EventGroup.of(ModConstants.MOD_NAME.replace(" ", ""));
        EventHandler SUMMONING_START = GROUP.server("start", () -> SummoningKubeEvent.class).hasResult();
        EventHandler SUMMONING_COMPLETE = GROUP.server("complete", () -> SummoningKubeEvent.class);
    }
}
