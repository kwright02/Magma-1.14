package org.magmafoundation.magma.mixin.core.minecraft.inventory;

import jline.internal.Nullable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.magmafoundation.magma.util.MagmaConversions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(net.minecraft.inventory.Inventory.class)
public abstract class MixinInventory implements Inventory {

    @Shadow public abstract int getSizeInventory();
    @Shadow public abstract net.minecraft.item.ItemStack getStackInSlot(int index);
    @Shadow public abstract void setInventorySlotContents(int index, net.minecraft.item.ItemStack stack);
    @Shadow public abstract net.minecraft.item.ItemStack addItem(net.minecraft.item.ItemStack stack);

    /**
     * @TODO
     * int getMaxStackSize();
     * int setMaxStackSizE();
     */

    @Override
    public int getSize(){
        return getSizeInventory();
    }

    @Override @Nullable
    public ItemStack getItem(int index){
        return MagmaConversions.convertFromNMS(getStackInSlot(index));
    }

    @Override
    public void setItem(int index, @Nullable ItemStack item){
        setInventorySlotContents(index, MagmaConversions.asNMSCopy(item));
    }

    @NotNull
    @Override
    public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> addedList = new HashMap<>(items.length);
        for(int i = 0; i < items.length; i++){
            ItemStack item = items[i];
            addedList.put(i, item);
            addItem(MagmaConversions.asNMSCopy(item));
        }
        return addedList;
    }
}
