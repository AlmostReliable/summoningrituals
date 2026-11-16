package com.almostreliable.summoningrituals.compat.kubegroovy;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.compat.kubegroovy.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubegroovy.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubegroovy.event.KubeEvents;
import com.almostreliable.summoningrituals.compat.kubegroovy.event.SummoningKubeEvent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.AltarKubeRecipe;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.AltarRecipeSchema;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.BlockPatternConditionComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.BlockPosComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.CommandOutputComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.EntityInputComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.EntityOutputComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.FakeEntityInputComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.ItemOutputComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.LootItemConditionComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.wrapper.CommandOutputTypeWrapper;
import com.almostreliable.summoningrituals.compat.kubegroovy.wrapper.EntityInfoTypeWrapper;
import com.almostreliable.summoningrituals.core.ModRegistration;
import com.almostreliable.summoningrituals.recipe.condition.handler.TimeCondition;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import net.minecraft.world.level.MoonPhase;

import dev.uncandango.kubegroovy.event.EventGroupRegistry;
import dev.uncandango.kubegroovy.plugin.KubeGroovyPlugin;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponentTypeRegistry;
import dev.uncandango.kubegroovy.recipe.schema.RecipeFactoryRegistry;
import dev.uncandango.kubegroovy.recipe.schema.RecipeSchemaRegistry;
import dev.uncandango.kubegroovy.script.BindingRegistry;
import dev.uncandango.kubegroovy.script.TypeWrapperRegistry;

public class KubePlugin implements KubeGroovyPlugin {

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
        registry.register(CommandOutput.class, CommandOutputTypeWrapper.INSTANCE, true);
        registry.register(EntityInfo.class, EntityInfoTypeWrapper.INSTANCE, true);
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
