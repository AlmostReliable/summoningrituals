package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.SummoningRitualsConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

@SuppressWarnings("ALL")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    private LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "shouldDropLoot", at = @At("HEAD"), cancellable = true)
    private void summoning$shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        if (getTags().contains(f("{}_sacrificed", SummoningRitualsConstants.MOD_ID))) {
            cir.setReturnValue(false);
        }
    }
}
