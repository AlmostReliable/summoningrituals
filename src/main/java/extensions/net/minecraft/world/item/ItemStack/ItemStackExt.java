package extensions.net.minecraft.world.item.ItemStack;

import com.almostreliable.summoningrituals.platform.Platform;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Extension
public final class ItemStackExt {

    private ItemStackExt() {}

    public static boolean canStack(@This ItemStack thiz, ItemStack other) {
        if (thiz.isEmpty() || !ItemStack.isSameItem(thiz, other) || thiz.hasTag() != other.hasTag()) return false;
        if (!thiz.hasTag()) return true;
        assert thiz.getTag() != null;
        return thiz.getTag().equals(other.getTag());
    }

    public static CompoundTag serialize(@This ItemStack thiz) {
        return Platform.serializeItemStack(thiz);
    }
}
