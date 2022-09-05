package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.altar.AltarEntity;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.MobOutputBuilder;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class AlmostKube extends KubeJSPlugin {

    @Override
    public void init() {
        AltarEntity.SUMMONING_START.register((level, pos, recipe, player) -> {
            var event = new SummoningEventJS(true, level, pos, recipe, player);
            event.post(ScriptType.SERVER, "summoningrituals.start");
            return !event.isCancelled();
        });
        AltarEntity.SUMMONING_COMPLETE.register((level, pos, recipe, player) -> {
            new SummoningEventJS(false, level, pos, recipe, player).post(
                ScriptType.SERVER,
                "summoningrituals.complete"
            );
            return true;
        });
    }

    @Override
    public void addBindings(BindingsEvent event) {
        if (event.type != ScriptType.SERVER) return;
        event.add("SummoningOutput", OutputWrapper.class);
    }

    @Override
    public void addTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        if (type != ScriptType.SERVER) return;
        typeWrappers.register(ItemOutputBuilder.class, OutputWrapper::item);
        typeWrappers.register(MobOutputBuilder.class, OutputWrapper::mob);
    }

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(TextUtils.getRL(Constants.ALTAR), AltarRecipeJS::new);
    }

    @SuppressWarnings("WeakerAccess")
    public static final class OutputWrapper {

        private OutputWrapper() {}

        public static ItemOutputBuilder item(@Nullable Object o) {
            if (o instanceof ItemOutputBuilder iob) return iob;
            ItemStackJS stack = ItemStackJS.of(o);
            if (stack.isEmpty()) {
                ConsoleJS.SERVER.error("ItemStack is empty or null");
            }
            return new ItemOutputBuilder(stack.getItemStack());
        }

        public static MobOutputBuilder mob(@Nullable Object o) {
            if (o instanceof MobOutputBuilder mob) return mob;
            if (o instanceof CharSequence || o instanceof ResourceLocation) {
                ResourceLocation id = ResourceLocation.tryParse(o.toString());
                var mob = SerializeUtils.mobFromId(id);
                return new MobOutputBuilder(mob);
            }
            ConsoleJS.SERVER.error("Missing or invalid entity given for SummoningEntityOutput");
            return new MobOutputBuilder(EntityType.ITEM);
        }
    }
}
