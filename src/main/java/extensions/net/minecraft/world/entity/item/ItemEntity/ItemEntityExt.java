package extensions.net.minecraft.world.entity.item.ItemEntity;

import manifold.ext.rt.api.Extension;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Extension
public final class ItemEntityExt {

    private ItemEntityExt() {}

    @Extension
    public static ItemEntity of(Level level, ItemStack stack) {
        return new ItemEntity(level, 0, 0, 0, stack);
    }
}
