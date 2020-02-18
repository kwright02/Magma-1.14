package org.magmafoundation.magma.util;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.magmafoundation.magma.util.item.MagmaItemFactory;
import org.magmafoundation.magma.util.item.MagmaItemMeta;

@DelegateDeserialization(ItemStack.class)
public abstract class MagmaConversions {

    public static ItemStack convertFromNMS(net.minecraft.item.ItemStack original){
        if(original.isEmpty()) return new ItemStack(Material.AIR);
        ItemStack stack = new ItemStack(MagmaUnsafeValues.getInstance().getMaterial(original.getItem().getRegistryName().getPath(), 0), original.getCount());
//        if(hasItemMeta(original)) stack.setItemMeta(getItemMeta(original)); @TODO
        return stack;
    }

    public static net.minecraft.item.ItemStack asNMSCopy(ItemStack original) {
        Item item = Item.getItemById(original.getType().getId());

        if (item == null) {
            return net.minecraft.item.ItemStack.EMPTY;
        }

        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(item, original.getAmount());
        if (original.hasItemMeta()) {
            setItemMeta(stack, original.getItemMeta());
        }
        return stack;
    }

    static boolean hasItemMeta(net.minecraft.item.ItemStack item) {
        return !(item == null || item.getTag() == null || item.getTag().isEmpty());
    }

    static Material getType(net.minecraft.item.ItemStack item) {
        return item == null ? Material.AIR : MagmaUnsafeValues.getInstance().getMaterial(item.getItem().getRegistryName().getPath(), 0);
    }

    public static boolean setItemMeta(net.minecraft.item.ItemStack item, ItemMeta itemMeta) {
        if (item == null) {
            return false;
        }
        if (MagmaItemFactory.instance().equals(itemMeta, null)) {
            item.setTag(null);
            return true;
        }
        if (!MagmaItemFactory.instance().isApplicable(itemMeta, getType(item))) {
            return false;
        }

        itemMeta = MagmaItemFactory.instance().asMetaFor(itemMeta, getType(item));
        if (itemMeta == null) return true;

        Item oldItem = item.getItem();
        Item newItem = MagmaUnsafeValues.getInstance().getItem(MagmaItemFactory.instance().updateMaterial(itemMeta, MagmaUnsafeValues.getInstance().getMaterial(oldItem.getRegistryName().getPath(), 0)));
        if (oldItem != newItem) {
            item.setItem(newItem);
        }

        CompoundNBT tag = new CompoundNBT();
        item.setTag(tag);

        ((MagmaItemMeta) itemMeta).applyToItem(tag);
        item.convertStack(((MagmaItemMeta) itemMeta).getVersion());
        // SpigotCraft#463 this is required now by the Vanilla client, so mimic ItemStack constructor in ensuring it
        if (item.getItem() != null && item.getItem().isDamageable()) {
            item.setDamage(item.getDamage());
        }

        return true;
    }


    /**
     * @TODO
     * This is for future implementation of the getItemMeta for NMS itemstack conversion
     */

//    public static ItemMeta getItemMeta(net.minecraft.item.ItemStack item) {
//        if (!hasItemMeta(item)) {
//            return null;
//        }
//        switch (getType(item)) {
//            case WRITTEN_BOOK:
//                return new CraftMetaBookSigned(item.getTag());
//            case WRITABLE_BOOK:
//                return new CraftMetaBook(item.getTag());
//            case CREEPER_HEAD:
//            case CREEPER_WALL_HEAD:
//            case DRAGON_HEAD:
//            case DRAGON_WALL_HEAD:
//            case PLAYER_HEAD:
//            case PLAYER_WALL_HEAD:
//            case SKELETON_SKULL:
//            case SKELETON_WALL_SKULL:
//            case WITHER_SKELETON_SKULL:
//            case WITHER_SKELETON_WALL_SKULL:
//            case ZOMBIE_HEAD:
//            case ZOMBIE_WALL_HEAD:
//                return new CraftMetaSkull(item.getTag());
//            case LEATHER_HELMET:
//            case LEATHER_HORSE_ARMOR:
//            case LEATHER_CHESTPLATE:
//            case LEATHER_LEGGINGS:
//            case LEATHER_BOOTS:
//                return new CraftMetaLeatherArmor(item.getTag());
//            case POTION:
//            case SPLASH_POTION:
//            case LINGERING_POTION:
//            case TIPPED_ARROW:
//                return new CraftMetaPotion(item.getTag());
//            case FILLED_MAP:
//                return new CraftMetaMap(item.getTag());
//            case FIREWORK_ROCKET:
//                return new CraftMetaFirework(item.getTag());
//            case FIREWORK_STAR:
//                return new CraftMetaCharge(item.getTag());
//            case ENCHANTED_BOOK:
//                return new CraftMetaEnchantedBook(item.getTag());
//            case BLACK_BANNER:
//            case BLACK_WALL_BANNER:
//            case BLUE_BANNER:
//            case BLUE_WALL_BANNER:
//            case BROWN_BANNER:
//            case BROWN_WALL_BANNER:
//            case CYAN_BANNER:
//            case CYAN_WALL_BANNER:
//            case GRAY_BANNER:
//            case GRAY_WALL_BANNER:
//            case GREEN_BANNER:
//            case GREEN_WALL_BANNER:
//            case LIGHT_BLUE_BANNER:
//            case LIGHT_BLUE_WALL_BANNER:
//            case LIGHT_GRAY_BANNER:
//            case LIGHT_GRAY_WALL_BANNER:
//            case LIME_BANNER:
//            case LIME_WALL_BANNER:
//            case MAGENTA_BANNER:
//            case MAGENTA_WALL_BANNER:
//            case ORANGE_BANNER:
//            case ORANGE_WALL_BANNER:
//            case PINK_BANNER:
//            case PINK_WALL_BANNER:
//            case PURPLE_BANNER:
//            case PURPLE_WALL_BANNER:
//            case RED_BANNER:
//            case RED_WALL_BANNER:
//            case WHITE_BANNER:
//            case WHITE_WALL_BANNER:
//            case YELLOW_BANNER:
//            case YELLOW_WALL_BANNER:
//                return new CraftMetaBanner(item.getTag());
//            case BAT_SPAWN_EGG:
//            case BLAZE_SPAWN_EGG:
//            case CAT_SPAWN_EGG:
//            case CAVE_SPIDER_SPAWN_EGG:
//            case CHICKEN_SPAWN_EGG:
//            case COD_SPAWN_EGG:
//            case COW_SPAWN_EGG:
//            case CREEPER_SPAWN_EGG:
//            case DOLPHIN_SPAWN_EGG:
//            case DONKEY_SPAWN_EGG:
//            case DROWNED_SPAWN_EGG:
//            case ELDER_GUARDIAN_SPAWN_EGG:
//            case ENDERMAN_SPAWN_EGG:
//            case ENDERMITE_SPAWN_EGG:
//            case EVOKER_SPAWN_EGG:
//            case FOX_SPAWN_EGG:
//            case GHAST_SPAWN_EGG:
//            case GUARDIAN_SPAWN_EGG:
//            case HORSE_SPAWN_EGG:
//            case HUSK_SPAWN_EGG:
//            case LLAMA_SPAWN_EGG:
//            case MAGMA_CUBE_SPAWN_EGG:
//            case MOOSHROOM_SPAWN_EGG:
//            case MULE_SPAWN_EGG:
//            case OCELOT_SPAWN_EGG:
//            case PANDA_SPAWN_EGG:
//            case PARROT_SPAWN_EGG:
//            case PHANTOM_SPAWN_EGG:
//            case PIG_SPAWN_EGG:
//            case PILLAGER_SPAWN_EGG:
//            case POLAR_BEAR_SPAWN_EGG:
//            case PUFFERFISH_SPAWN_EGG:
//            case RABBIT_SPAWN_EGG:
//            case RAVAGER_SPAWN_EGG:
//            case SALMON_SPAWN_EGG:
//            case SHEEP_SPAWN_EGG:
//            case SHULKER_SPAWN_EGG:
//            case SILVERFISH_SPAWN_EGG:
//            case SKELETON_HORSE_SPAWN_EGG:
//            case SKELETON_SPAWN_EGG:
//            case SLIME_SPAWN_EGG:
//            case SPIDER_SPAWN_EGG:
//            case SQUID_SPAWN_EGG:
//            case STRAY_SPAWN_EGG:
//            case TRADER_LLAMA_SPAWN_EGG:
//            case TROPICAL_FISH_SPAWN_EGG:
//            case TURTLE_SPAWN_EGG:
//            case VEX_SPAWN_EGG:
//            case VILLAGER_SPAWN_EGG:
//            case VINDICATOR_SPAWN_EGG:
//            case WANDERING_TRADER_SPAWN_EGG:
//            case WITCH_SPAWN_EGG:
//            case WITHER_SKELETON_SPAWN_EGG:
//            case WOLF_SPAWN_EGG:
//            case ZOMBIE_HORSE_SPAWN_EGG:
//            case ZOMBIE_PIGMAN_SPAWN_EGG:
//            case ZOMBIE_SPAWN_EGG:
//            case ZOMBIE_VILLAGER_SPAWN_EGG:
//                return new CraftMetaSpawnEgg(item.getTag());
//            case ARMOR_STAND:
//                return new CraftMetaArmorStand(item.getTag());
//            case KNOWLEDGE_BOOK:
//                return new CraftMetaKnowledgeBook(item.getTag());
//            case FURNACE:
//            case CHEST:
//            case TRAPPED_CHEST:
//            case JUKEBOX:
//            case DISPENSER:
//            case DROPPER:
//            case ACACIA_SIGN:
//            case ACACIA_WALL_SIGN:
//            case BIRCH_SIGN:
//            case BIRCH_WALL_SIGN:
//            case DARK_OAK_SIGN:
//            case DARK_OAK_WALL_SIGN:
//            case JUNGLE_SIGN:
//            case JUNGLE_WALL_SIGN:
//            case OAK_SIGN:
//            case OAK_WALL_SIGN:
//            case SPRUCE_SIGN:
//            case SPRUCE_WALL_SIGN:
//            case SPAWNER:
//            case BREWING_STAND:
//            case ENCHANTING_TABLE:
//            case COMMAND_BLOCK:
//            case REPEATING_COMMAND_BLOCK:
//            case CHAIN_COMMAND_BLOCK:
//            case BEACON:
//            case DAYLIGHT_DETECTOR:
//            case HOPPER:
//            case COMPARATOR:
//            case SHIELD:
//            case STRUCTURE_BLOCK:
//            case SHULKER_BOX:
//            case WHITE_SHULKER_BOX:
//            case ORANGE_SHULKER_BOX:
//            case MAGENTA_SHULKER_BOX:
//            case LIGHT_BLUE_SHULKER_BOX:
//            case YELLOW_SHULKER_BOX:
//            case LIME_SHULKER_BOX:
//            case PINK_SHULKER_BOX:
//            case GRAY_SHULKER_BOX:
//            case LIGHT_GRAY_SHULKER_BOX:
//            case CYAN_SHULKER_BOX:
//            case PURPLE_SHULKER_BOX:
//            case BLUE_SHULKER_BOX:
//            case BROWN_SHULKER_BOX:
//            case GREEN_SHULKER_BOX:
//            case RED_SHULKER_BOX:
//            case BLACK_SHULKER_BOX:
//            case ENDER_CHEST:
//            case BARREL:
//            case BELL:
//            case BLAST_FURNACE:
//            case CAMPFIRE:
//            case JIGSAW:
//            case LECTERN:
//            case SMOKER:
//                return new CraftMetaTropicalFishBucket(item.getTag());
//            case CROSSBOW:
//                return new CraftMetaCrossbow(item.getTag());
//            case SUSPICIOUS_STEW:
//                return new CraftMetaSuspiciousStew(item.getTag());
//            default:
//                return new CraftMetaItem(item.getTag());
//        }
//    }

}
