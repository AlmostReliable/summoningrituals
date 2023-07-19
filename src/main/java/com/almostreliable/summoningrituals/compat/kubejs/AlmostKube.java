package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.BuildConfig;
import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.MobOutputBuilder;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class AlmostKube extends KubeJSPlugin {

    private static final EventGroup GROUP = EventGroup.of(BuildConfig.MOD_NAME.replace(" ", ""));
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
    public void registerEvents() {
        GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.getType() != ScriptType.SERVER) return;
        event.add("SummoningOutput", OutputWrapper.class);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        if (type != ScriptType.SERVER) return;
        typeWrappers.registerSimple(ItemOutputBuilder.class, OutputWrapper::item);
        typeWrappers.registerSimple(MobOutputBuilder.class, OutputWrapper::mob);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace(BuildConfig.MOD_ID).register(Constants.ALTAR, AltarRecipeSchema.SCHEMA);
    }

    @SuppressWarnings("WeakerAccess")
    public static final class OutputWrapper {

        private OutputWrapper() {}

        public static ItemOutputBuilder item(@Nullable Object o) {
            if (o instanceof ItemOutputBuilder iob) return iob;
            ItemStack stack = ItemStackJS.of(o);
            if (stack.isEmpty()) {
                ConsoleJS.SERVER.error("Empty or null ItemStack specified for SummoningOutput.item");
            }
            return new ItemOutputBuilder(stack);
        }

        public static MobOutputBuilder mob(@Nullable Object o) {
            if (o instanceof MobOutputBuilder mob) return mob;
            if (o instanceof CharSequence || o instanceof ResourceLocation) {
                ResourceLocation id = ResourceLocation.tryParse(o.toString());
                var mob = Platform.mobFromId(id);
                return new MobOutputBuilder(mob);
            }
            ConsoleJS.SERVER.error("Missing or invalid entity specified for SummoningOutput.mob");
            return new MobOutputBuilder(EntityType.ITEM);
        }
    }
}
