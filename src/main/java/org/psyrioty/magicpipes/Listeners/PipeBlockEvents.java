package org.psyrioty.magicpipes.Listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.psyrioty.magicpipes.Database.Requests;
import org.psyrioty.magicpipes.magicpipes;
import org.psyrioty.magicpipes.Objects.Pipe;

import java.util.List;

public class PipeBlockEvents implements Listener {
    @EventHandler(ignoreCancelled = true)
    private void PipeBlockPlace(BlockPlaceEvent event){
        BlockData blockData = event.getBlock().getBlockData();
        BlockState blockState = event.getBlock().getState();
        ItemStack itemStack = event.getItemInHand();
        if(blockData.getMaterial() == Material.PLAYER_HEAD || blockData.getMaterial() == Material.PLAYER_WALL_HEAD) {
            if(
                    event.getPlayer().getInventory().getItemInOffHand().getType() == Material.PLAYER_HEAD
                    && event.getItemInHand().getType() != Material.PLAYER_HEAD
            ){
                itemStack = event.getPlayer().getInventory().getItemInOffHand();
            }
            if (!itemStack.getItemMeta().getPersistentDataContainer().isEmpty()) {
                PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
                for (NamespacedKey key : pdc.getKeys()) {
                    if (key.toString().equalsIgnoreCase("magicpipes:id")) {
                        byte type = 0;
                        switch (pdc.get(key, PersistentDataType.STRING)) {
                            case "pipe_funnel":
                                type = 1;
                                break;
                            case "pipe":
                                type = 0;
                                break;
                            case "pipe_receiver":
                                type = 2;
                                break;
                        }

                        Pipe pipe = null;
                        try {
                            pipe = new Pipe(
                                    blockState.getX(),
                                    blockState.getY(),
                                    blockState.getZ(),
                                    blockState.getWorld(),
                                    type
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        Pipe finalPipe = pipe;
                        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                                    Requests.createPipe(finalPipe);
                        });
                        magicpipes.getPlugin().getPipes().add(pipe);

                        //Skull skull = (Skull) blockState;
                        //skull.setRotation(BlockFace.UP);
                        //skull.update(true, false);
                        break;
                    }
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void blockBreak(BlockBreakEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            List<Pipe> pipeList = magicpipes.getPlugin().getPipes();

            for(Pipe pipe: pipeList){
                int i = 0;
                List<Block> blocks = pipe.getCashContainersBlock();
                List<BlockState> blockStates = pipe.getCashBlockStateContainers();
                for(Block block: blocks){
                    if(block.equals(event.getBlock())){
                        blocks.remove(i);
                        blockStates.remove(i);
                        break;
                    }
                    i++;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            List<Pipe> pipeList = magicpipes.getPlugin().getPipes();

            for(Pipe pipe: pipeList){
                int i = 0;
                List<Block> blocks = pipe.getCashContainersBlock();
                List<BlockState> blockStates = pipe.getCashBlockStateContainers();
                for(Block block: blocks){
                    for(Block blockEvent: event.getBlocks()){
                        if(blockEvent.equals(block)){
                            blocks.remove(i);
                            blockStates.remove(i);
                        }
                    }
                    i++;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            List<Pipe> pipeList = magicpipes.getPlugin().getPipes();

            for(Pipe pipe: pipeList){
                int i = 0;
                List<Block> blocks = pipe.getCashContainersBlock();
                List<BlockState> blockStates = pipe.getCashBlockStateContainers();
                for(Block block: blocks){
                    for(Block blockEvent: event.getBlocks()){
                        if(blockEvent.equals(block)){
                            blocks.remove(i);
                            blockStates.remove(i);
                        }
                    }
                    i++;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onHeadInteract(PlayerInteractEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                for (Pipe pipe : magicpipes.getPlugin().getPipes()) {
                    if (
                            block.getWorld() == pipe.getWorld()
                                    && block.getX() == pipe.getX()
                                    && block.getY() == pipe.getY()
                                    && block.getZ() == pipe.getZ()
                                    && (
                                            pipe.getType() == 1
                                            || pipe.getType() == 2
                                    )
                                    && !pipe.getIsRemove()
                    ) {
                        Player player = event.getPlayer();

                        if (event.getClickedBlock() == null) return;

                        org.bukkit.Location bukkitLoc = block.getLocation();

                        com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(bukkitLoc);
                        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitLoc.getWorld());

                        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        RegionQuery query = container.createQuery();

                        ApplicableRegionSet regions = query.getApplicableRegions(weLocation);

                        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

                        for (ProtectedRegion region : regions) {
                            if (region.isOwner(localPlayer)) {
                                Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                                    event.getPlayer().openInventory(pipe.getInventory());
                                    event.setCancelled(true);
                                });
                            } else if (region.isMember(localPlayer)) {
                                Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                                    event.getPlayer().openInventory(pipe.getInventory());
                                    event.setCancelled(true);
                                });
                            }
                        }

                        if (regions.size() == 0) {
                            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                                event.getPlayer().openInventory(pipe.getInventory());
                                event.setCancelled(true);
                            });
                        }
                    }
                }
            }
        });
    }
}
