package extensions.net.minecraft.world.item.ItemStack;

import com.almostreliable.summoningrituals.platform.Platform;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Extension
public final class ItemStackExt {

    private ItemStackExt() {}

    public static ItemStack withCount(@This ItemStack thiz, long count) {
        if (count > thiz.getMaxStackSize()) {
            throw new IllegalArgumentException("Count cannot be greater than max stack size");
        }
        thiz.setCount((int) count);
        return thiz;
    }

    public static ItemStack copyWithCount(@This ItemStack thiz, int count) {
        return thiz.copy().withCount(count);
    }

    public static boolean canStack(@This ItemStack thiz, ItemStack other) {
        if (thiz.isEmpty() || !thiz.sameItem(other) || thiz.hasTag() != other.hasTag()) return false;
        if (!thiz.hasTag()) return true;
        assert thiz.getTag() != null;
        return thiz.getTag().equals(other.getTag());
    }

    public static CompoundTag serialize(@This ItemStack thiz) {
        return Platform.serializeItemStack(thiz);
    }
}
