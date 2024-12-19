package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.recipe.component.EntityOutput;
import com.almostreliable.summoningrituals.recipe.component.ItemOutput;
import com.almostreliable.summoningrituals.util.Utils;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class AlmostKube implements KubeJSPlugin {

    private static final EventGroup GROUP = EventGroup.of(ModConstants.MOD_NAME.replace(" ", ""));
    private static final EventHandler START = GROUP.server("start", () -> SummoningEventJS.class).hasResult();
    private static final EventHandler COMPLETE = GROUP.server("complete", () -> SummoningEventJS.class);

    @Override
    public void init() {
        AltarBlockEntity.SUMMONING_START.register((level, pos, recipe, player) ->
            START.post(new SummoningEventJS(level, pos, recipe, player)).interruptFalse());
        AltarBlockEntity.SUMMONING_COMPLETE.register((level, pos, recipe, player) ->
            COMPLETE.post(new SummoningEventJS(level, pos, recipe, player)).interruptFalse());
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (bindings.type() != ScriptType.SERVER) return;
        bindings.add("SummoningOutput", OutputWrapper.class);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        if (registry.scriptType() != ScriptType.SERVER) return;
        registry.register(ItemOutput.class, OutputWrapper::ofItem);
        registry.register(EntityOutput.class, OutputWrapper::ofEntity);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(Utils.getRL(Constants.ALTAR), AltarRecipeSchema.SCHEMA);
    }

    @SuppressWarnings("WeakerAccess")
    public static final class OutputWrapper {

        private OutputWrapper() {}

        private static ItemOutput ofItem(RegistryAccessContainer registries, @Nullable Object o) {
            if (o instanceof ItemOutput.Builder iob) return iob.build();
            if (o instanceof ItemOutput io) return io;

            ItemStack stack = ItemStackJS.wrap(registries, o);
            if (stack.isEmpty()) {
                ConsoleJS.SERVER.error("Empty or null ItemStack specified for SummoningOutput.item");
            }

            return new ItemOutput.Builder(stack).build();
        }

        private static EntityOutput ofEntity(@Nullable Object o) {
            if (o instanceof EntityOutput.Builder mob) return mob.build();
            if (o instanceof EntityOutput entity) return entity;
            if (o instanceof CharSequence || o instanceof ResourceLocation) {
                ResourceLocation id = ResourceLocation.parse(o.toString());
                var mob = BuiltInRegistries.ENTITY_TYPE.getHolder(id).orElseThrow();
                return new EntityOutput.Builder(mob).build();
            }

            ConsoleJS.SERVER.error("Missing or invalid entity specified for SummoningOutput.mob");
            return new EntityOutput.Builder(EntityType.ITEM.builtInRegistryHolder()).build();
        }

        public static ItemOutput.Builder item(ItemStack item) {
            return new ItemOutput.Builder(item);
        }

        public static ItemOutput.Builder item(ItemOutput item, int count) {
            return new ItemOutput.Builder(item.item().copyWithCount(count));
        }

        public static EntityOutput.Builder entity(Holder<EntityType<?>> entity) {
            return new EntityOutput.Builder(entity);
        }

        public static EntityOutput.Builder entity(Holder<EntityType<?>> entity, int count) {
            return new EntityOutput.Builder(entity, count);
        }
    }
}
