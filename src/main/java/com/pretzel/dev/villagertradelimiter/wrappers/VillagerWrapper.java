package com.pretzel.dev.villagertradelimiter.wrappers;

import com.pretzel.dev.villagertradelimiter.lib.Debug;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class VillagerWrapper {
    private final Villager villager;
    private final ItemStack[] contents;
    private final ReadWriteNBT nbtVillager;
    private ArrayList<ReadWriteNBT> originalRecipes = new ArrayList<>();

    /** @param villager The Villager to store in this wrapper */
    public VillagerWrapper(final Villager villager, ReadWriteNBT nbtVillager) {
        this.villager = villager;
        this.nbtVillager = nbtVillager;
        this.contents = new ItemStack[villager.getInventory().getContents().length];
        for(int i = 0; i < this.contents.length; i++) {
            ItemStack item = villager.getInventory().getItem(i);
            this.contents[i] = (item == null ? null : item.clone());
        }
    }

    /** @return a list of wrapped recipes for the villager */
    public List<RecipeWrapper> getRecipes() {
        final List<RecipeWrapper> recipes = new ArrayList<>();

        //Add the recipes from the villager's NBT data into a list of wrapped recipes
        final ReadWriteNBT offers = nbtVillager.getCompound("Offers");
        if(offers == null) return recipes;
        final ReadWriteNBTCompoundList nbtRecipes = offers.getCompoundList("Recipes");
        for(ReadWriteNBT nbtRecipe : nbtRecipes) {
            recipes.add(new RecipeWrapper(nbtRecipe));
            ReadWriteNBT newRecipe = NBT.createNBTObject();
            newRecipe.mergeCompound(nbtRecipe);
            originalRecipes.add(newRecipe);
        }
        return recipes;
    }

    /** @return A list of wrapped gossips for the villager */
    private List<GossipWrapper> getGossips() {
        final List<GossipWrapper> gossips = new ArrayList<>();
        if(!nbtVillager.hasTag("Gossips")) return gossips;

        //Add the gossips from the villager's NBT data into a list of wrapped gossips
        final ReadWriteNBTCompoundList nbtGossips = nbtVillager.getCompoundList("Gossips");
        for(ReadWriteNBT nbtGossip : nbtGossips) {
            gossips.add(new GossipWrapper(nbtGossip));
        }
        return gossips;
    }

    /**
     * @param villager The wrapped villager that contains the gossips
     * @param player The wrapped player that the gossips are about
     * @param isOld Whether the server is older than 1.16 or not. Minecraft changed how UUID's are represented in 1.16
     * @return the total reputation (from gossips) for a player
     */
    public int getTotalReputation(@NonNull final VillagerWrapper villager, @NonNull final PlayerWrapper player, final boolean isOld) {
        int totalReputation = 0;

        final String playerUUID = player.getUUID(isOld);
        final List<GossipWrapper> gossips = villager.getGossips();
        for(GossipWrapper gossip : gossips) {
            final GossipWrapper.GossipType type = gossip.getType();
            if(type == null || type == GossipWrapper.GossipType.OTHER) continue;

            final String targetUUID = gossip.getTargetUUID(isOld);
            if(targetUUID.equals(playerUUID)) {
                totalReputation += gossip.getValue() * type.getWeight();
            }
        }
        return totalReputation;
    }

    /** Resets the villager's NBT data to default */
    public void reset() {
        // Reset the recipes back to their default ingredients, MaxUses, and discounts
        NBT.modify(villager, nbtVillager -> {
            ReadWriteNBT offers = nbtVillager.getCompound("Offers");
            ReadWriteNBTCompoundList recipes = offers.getCompoundList("Recipes");

            // Log to track recipe reset progress
            Debug.log("Resetting villager recipes...");

            // Loop through the original recipes and selectively update the ones that need resetting
            for (int i = 0; i < recipes.size(); i++) {
                ReadWriteNBT currentRecipe = recipes.get(i);
                ReadWriteNBT originalRecipe = originalRecipes.get(i);

                // Only reset the ingredients and MaxUses of the recipe, leave other properties intact
                currentRecipe.setItemStack("buy", originalRecipe.getItemStack("buy"));
                if (originalRecipe.getItemStack("buyB") != null) {
                    currentRecipe.setItemStack("buyB", originalRecipe.getItemStack("buyB"));
                }
                currentRecipe.setItemStack("sell", originalRecipe.getItemStack("sell"));
                currentRecipe.setInteger("maxUses", originalRecipe.getInteger("maxUses"));
                currentRecipe.setInteger("specialPrice", originalRecipe.getInteger("specialPrice"));

                // Log recipe data for debugging
                Debug.log("Reset recipe " + i + ": " + currentRecipe);
            }
        });

        // Reset inventory items
        this.villager.getInventory().clear();
        this.villager.getInventory().setContents(this.contents);

        // Log inventory reset
        Debug.log("Villager inventory has been reset.");
    }

}
