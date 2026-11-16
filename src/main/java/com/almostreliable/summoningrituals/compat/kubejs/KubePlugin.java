package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubejs.event.KubeEvents;
import com.almostreliable.summoningrituals.compat.kubejs.event.SummoningKubeEvent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.AltarKubeRecipe;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.AltarRecipeSchema;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.BlockPatternConditionComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.BlockPosComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.CommandOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityInputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.FakeEntityInputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.ItemOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.LootItemConditionComponent;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.CommandOutputTypeWrapper;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityInfoTypeWrapper;
import com.almostreliable.summoningrituals.core.ModRegistration;
import com.almostreliable.summoningrituals.recipe.condition.handler.TimeCondition;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import net.minecraft.world.level.MoonPhase;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;

public class KubePlugin implements KubeJSPlugin {

    @Override
    public void init() {
        AltarBlockEntity.SUMMONING_START.register((altar, recipeInfo, player) ->
            KubeEvents.SUMMONING_START.post(new SummoningKubeEvent(altar, recipeInfo, player)).interruptFalse());
        AltarBlockEntity.SUMMONING_COMPLETE.register((altar, recipeInfo, player) ->
            KubeEvents.SUMMONING_COMPLETE.post(new SummoningKubeEvent(altar, recipeInfo, player)).interruptFalse());
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(KubeEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry registry) {
        if (!registry.type().isServer()) return;
        registry.add("SummoningItem", SummoningItemBinding.class);
        registry.add("SummoningEntity", SummoningEntityBinding.class);
        registry.add("SummoningMoonPhase", MoonPhase.class);
        registry.add("SummoningTime", TimeCondition.TimeType.class);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.unit(BlockPatternConditionComponent.INSTANCE);
        registry.unit(BlockPosComponent.INSTANCE);
        registry.unit(CommandOutputComponent.INSTANCE);
        registry.unit(EntityInputComponent.INSTANCE);
        registry.unit(FakeEntityInputComponent.INSTANCE);
        registry.unit(EntityOutputComponent.INSTANCE);
        registry.unit(ItemOutputComponent.INSTANCE);
        registry.unit(LootItemConditionComponent.INSTANCE);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        registry.register(CommandOutput.class, CommandOutputTypeWrapper.INSTANCE);
        registry.register(EntityInfo.class, EntityInfoTypeWrapper.INSTANCE);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(AltarKubeRecipe.FACTORY);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(ModRegistration.ALTAR_RECIPE_TYPE.getId(), AltarRecipeSchema.SCHEMA);
    }
}
