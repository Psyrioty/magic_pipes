package org.psyrioty.magicpipes.Listeners;

import org.bukkit.event.Listener;

public class PipeItemEvents implements Listener {
    /*@EventHandler(priority = EventPriority.HIGHEST)
    private void PipeItemDrop(PlayerDropItemEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(MagicPipes.getPlugin(), () -> {
            ItemStack item = event.getItemDrop().getItemStack();
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            for(NamespacedKey key: container.getKeys()) {
                if(key.toString().equals("magicpipes:id")){

                }
            }
        });
    }*/
}
