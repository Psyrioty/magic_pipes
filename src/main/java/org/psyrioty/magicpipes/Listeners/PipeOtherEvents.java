package org.psyrioty.magicpipes.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.psyrioty.magicpipes.Database.Requests;
import org.psyrioty.magicpipes.magicpipes;
import org.psyrioty.magicpipes.Objects.Pipe;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PipeOtherEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void PipeBlockBreak(BlockBreakEvent event){

        Bukkit.getServer().getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            Block block = event.getBlock();
            Pipe pipe = magicpipes.getPlugin().findPipeForXYZWorld(block.getX(), block.getY(), block.getZ(), block.getWorld());
            if(pipe != null){
                pipe.remove();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getSourceBlock();
        Pipe pipe = magicpipes.getPlugin().findPipeForXYZWorld(block.getX(), block.getY(), block.getZ(), block.getWorld());
        if(pipe == null){
            pipe = magicpipes.getPlugin().findPipeForXYZWorld(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getWorld());
        }
        if(pipe != null) {
            if (
                    event.getBlock().getType() == Material.WATER
                            && event.getSourceBlock().getType() == Material.PLAYER_HEAD
                            && pipe.getX() == block.getX()
                            && pipe.getY() == block.getY()
                            && pipe.getZ() == block.getZ()
                            && pipe.getWorld() == block.getWorld()
                            && !pipe.getIsRemove()
            ) {
                event.setCancelled(true);
                block.setType(Material.AIR);
                //block.getWorld().dropItemNaturally(block.getLocation(), pipeItemStackBlock);
                Bukkit.getServer().getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), pipe::remove);
            } else if (
                    event.getSourceBlock().getType() == Material.WATER
                            && event.getBlock().getType() == Material.PLAYER_HEAD
                            && pipe.getX() == event.getBlock().getX()
                            && pipe.getY() == event.getBlock().getY()
                            && pipe.getZ() == event.getBlock().getZ()
                            && pipe.getWorld() == event.getBlock().getWorld()
                            && !pipe.getIsRemove()
            ) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), pipe.getPipeItemStackBlock());

                Bukkit.getServer().getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), pipe::remove);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onItemDrop(BlockDropItemEvent event){
        Block block = event.getBlock();
        Pipe pipe = magicpipes.getPlugin().findPipeForXYZWorld(block.getX(), block.getY(), block.getZ(), block.getWorld());
        if(pipe == null){
            return;
        }
        if(
                        !event.getItems().isEmpty()
                        && !pipe.getIsRemove()
        ){
                /*ItemStack itemStack = event.getItems().getFirst().getItemStack();
                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();


                if(pipe.getType() == 0){
                    pdc.set(
                            new NamespacedKey(MagicPipes.getPlugin(), "id"),
                            PersistentDataType.STRING,
                            "pipe"
                    );
                }else {
                    pdc.set(
                            new NamespacedKey(MagicPipes.getPlugin(), "id"),
                            PersistentDataType.STRING,
                            "pipe_funnel"
                    );
                }

                itemStack.setItemMeta(meta);*/
            //event.getPlayer().getInventory().addItem(pipeItemStackBlock);
            Item item = event.getItems().getFirst();
            item.setItemStack(pipe.getPipeItemStackBlock());
            event.getItems().set(0, item);
            //Item item = event.getItems().getFirst();
            //item.setItemStack(pipeItemStackBlock);
            //event.getItems().set(0, item);

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for(Block block: event.getBlocks()){
            Pipe pipe = magicpipes.getPlugin().findPipeForXYZWorld(block.getX(), block.getY(), block.getZ(), block.getWorld());
            if(pipe != null) {
                if (!pipe.getIsRemove()) {
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), pipe.getPipeItemStackBlock());

                    Bukkit.getServer().getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), pipe::remove);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block: event.getBlocks()){
            Pipe pipe = magicpipes.getPlugin().findPipeForXYZWorld(block.getX(), block.getY(), block.getZ(), block.getWorld());
            if(pipe != null) {
                if (!pipe.getIsRemove()) {
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), pipe.getPipeItemStackBlock());

                    Bukkit.getServer().getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), pipe::remove);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) throws IOException {
        Inventory pipeInventory = magicpipes.getPlugin().getPipeInventoryForInventory(event.getInventory());
        Pipe pipe;
        if (pipeInventory == null){
            return;
        }

        pipe = magicpipes.getPlugin().getPipes().stream().filter(pipe1 -> pipeInventory == pipe1.getInventory()).findFirst().orElse(null);
        if(pipe == null){
            return;
        }

        List<String> itemBase64List = new ArrayList<>();
        int i = 0;
        List<Integer> slot = new ArrayList<>();
        for (ItemStack itemStack: event.getInventory().getContents()){
            if(itemStack != null){
                itemBase64List.add(serializeItems(itemStack));
                slot.add(i);
            }
            i++;
        }
        if(!itemBase64List.isEmpty()){
            Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                Requests.itemsAdd(pipe, itemBase64List, slot);
            });
        }else{
            Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                Requests.removeItem(pipe);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        if(event.getInventory().getHolder() instanceof Pipe) {
            Inventory pipeInventory = magicpipes.getPlugin().getPipeInventoryForInventory(event.getInventory());
            Pipe pipe;
            if (pipeInventory == null){
                return;
            }

            pipe = magicpipes.getPlugin().getPipes().stream().filter(pipe1 -> pipeInventory == pipe1.getInventory()).findFirst().orElse(null);
            if(pipe == null){
                return;
            }
            if (pipeInventory == event.getClickedInventory() && event.getSlot() > -999) {
                event.setCancelled(true);
                if (event.getCursor() != null && event.getSlot() > -999 && event.getCursor().getType() != Material.AIR) {
                    ItemStack cursor = event.getCursor().clone();
                    cursor.setAmount(1);
                    event.getInventory().setItem(event.getSlot(), cursor);
                } else if (event.getClickedInventory().getItem(event.getSlot()) != null && event.getSlot() > -999) {
                    event.getClickedInventory().getItem(event.getSlot()).setAmount(0);
                } else if (event.getClickedInventory().getItem(event.getSlot()) == null && event.getSlot() > -999) {
                    ItemStack itemStack = new ItemStack(Material.BARRIER);
                    itemStack.setAmount(1);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("ЗАБЛОКИРОВАНО");
                    itemStack.setItemMeta(meta);
                    event.getClickedInventory().setItem(event.getSlot(), itemStack);
                }
            } else if ((event.isShiftClick()) && pipeInventory == event.getInventory()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event){
        if(event.getInventory().getHolder() instanceof Pipe){
            event.setCancelled(true);
        }
    }

    public static String serializeItems(ItemStack itemStack) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(itemStack);

        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onChunkUnload(ChunkUnloadEvent event) {
        if(magicpipes.getPlugin().isStartPipes()) {
            Chunk chunk = event.getChunk();
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            World world = event.getWorld();
            Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                for (Pipe pipe : new ArrayList<>(magicpipes.getPlugin().getActivePipe())) {
                    if (pipe != null) {
                        if (
                                pipe.getChunkX() == chunkX
                                        && pipe.getChunkZ() == chunkZ
                                        && pipe.getWorld() == world
                        ) {
                            magicpipes.getPlugin().getActivePipe().remove(pipe);
                            pipe.setIsActive(false);
                        }
                    } else {
                        magicpipes.getPlugin().getActivePipe().remove(pipe);
                    }
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onChunkLoad(ChunkLoadEvent event) {
        if(magicpipes.getPlugin().isStartPipes()) {
            Chunk chunk = event.getChunk();
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            World world = event.getWorld();
            Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                for (Pipe pipe : magicpipes.getPlugin().getPipes()) {
                    if (
                            pipe.getChunkX() == chunkX
                                    && pipe.getChunkZ() == chunkZ
                                    && pipe.getWorld() == world
                    ) {
                        magicpipes.getPlugin().getActivePipe().add(pipe);
                        pipe.setIsActive(true);
                    }
                }
            });
        }
    }
}
