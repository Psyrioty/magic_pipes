package org.psyrioty.magicpipes.Listeners;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.psyrioty.magicpipes.Database.Requests;
import org.psyrioty.magicpipes.Objects.Pipe;
import org.psyrioty.magicpipes.Objects.PipeContainer;
import org.psyrioty.magicpipes.magicpipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContainerEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void blockBreak(BlockBreakEvent event){
        World world = event.getBlock().getWorld();
        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            for(PipeContainer pipeContainer: magicpipes.getPlugin().getActivePipeContainers()){
                if(
                        pipeContainer.getX() == x
                        && pipeContainer.getY() == y
                        && pipeContainer.getZ() == z
                        && pipeContainer.getWorld() == world
                ){
                    magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                    magicpipes.getPlugin().getPipeContainers().remove(pipeContainer);
                    for(Pipe pipe: pipeContainer.getPipeParents()){
                        pipe.getTakePipeContainers().remove(pipeContainer);
                        pipe.getPipeContainers().remove(pipeContainer);
                    }
                    return;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Integer> blockX = new ArrayList<>();
        List<Integer> blockY = new ArrayList<>();
        List<Integer> blockZ = new ArrayList<>();
        List<World> blockWorld = new ArrayList<>();
        for(Block blockEvent: event.getBlocks()){
            blockX.add(blockEvent.getX());
            blockY.add(blockEvent.getY());
            blockZ.add(blockEvent.getZ());
            blockWorld.add(blockEvent.getWorld());
        }
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () ->{
            for(int i = 0; i < blockX.size(); i++){
                for(PipeContainer pipeContainer: new ArrayList<>(magicpipes.getPlugin().getActivePipeContainers())){
                    if(
                            blockX.get(i) == pipeContainer.getX() &&
                            blockY.get(i) == pipeContainer.getY() &&
                            blockZ.get(i) == pipeContainer.getZ() &&
                            blockWorld.get(i) == pipeContainer.getWorld()
                    ){
                        magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                        magicpipes.getPlugin().getPipeContainers().remove(pipeContainer);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        List<Integer> blockX = new ArrayList<>();
        List<Integer> blockY = new ArrayList<>();
        List<Integer> blockZ = new ArrayList<>();
        List<World> blockWorld = new ArrayList<>();
        for(Block blockEvent: event.getBlocks()){
            blockX.add(blockEvent.getX());
            blockY.add(blockEvent.getY());
            blockZ.add(blockEvent.getZ());
            blockWorld.add(blockEvent.getWorld());
        }
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () ->{
            for(int i = 0; i < blockX.size(); i++){
                for(PipeContainer pipeContainer: new ArrayList<>(magicpipes.getPlugin().getActivePipeContainers())){
                    if(
                            blockX.get(i) == pipeContainer.getX() &&
                                    blockY.get(i) == pipeContainer.getY() &&
                                    blockZ.get(i) == pipeContainer.getZ() &&
                                    blockWorld.get(i) == pipeContainer.getWorld()
                    ){
                        magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                        magicpipes.getPlugin().getPipeContainers().remove(pipeContainer);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void ContainerBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        World world = block.getWorld();
        BlockState blockState = block.getState();
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () ->{
            if(blockState instanceof Container container){
                for(PipeContainer pipeContainer: magicpipes.getPlugin().getActivePipeContainers()){
                    if(
                            pipeContainer.getX() == x
                            && pipeContainer.getY() == y
                            && pipeContainer.getZ() == z
                            && pipeContainer.getWorld() == world
                    ){
                        return;
                    }
                }
                for(Pipe pipe: magicpipes.getPlugin().getActivePipe()) {
                    if(
                            checkPipe(
                                    x,
                                    y,
                                    z,
                                    world,
                                    pipe
                            )
                    ) {
                        PipeContainer pipeContainer = new PipeContainer(
                                block,
                                container
                        );

                        magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        magicpipes.getPlugin().getPipeContainers().add(pipeContainer);
                        pipeContainer.setIsActive(true);
                        if(pipe.getType() == 2) {
                            pipe.getPipeContainers().add(pipeContainer);
                            pipeContainer.getPipeParents().add(pipe);
                        }else if(pipe.getType() == 1){
                            pipe.getTakePipeContainers().add(pipeContainer);
                            pipeContainer.getPipeParents().add(pipe);
                        }
                    }
                }
            }
        });
    }

    private boolean checkPipe(int x, int y, int z, World world, Pipe pipe){
        if(
                (
                        x == pipe.getX() + 1
                        && y == pipe.getY()
                        && z == pipe.getZ()
                        && world == pipe.getWorld()
                )
                ||
                (
                        x == pipe.getX() - 1
                        && y == pipe.getY()
                        && z == pipe.getZ()
                        && world == pipe.getWorld()
                )
                ||
                (
                        x == pipe.getX()
                        && y == pipe.getY() + 1
                        && z == pipe.getZ()
                        && world == pipe.getWorld()
                )
                ||
                (
                        x == pipe.getX()
                        && y == pipe.getY() - 1
                        && z == pipe.getZ()
                        && world == pipe.getWorld()
                )
                ||
                (
                        x == pipe.getX()
                        && y == pipe.getY()
                        && z == pipe.getZ() + 1
                        && world == pipe.getWorld()
                )
                ||
                (
                        x == pipe.getX()
                        && y == pipe.getY()
                        && z == pipe.getZ() - 1
                        && world == pipe.getWorld()
                )
        ){
            return true;
        }
        return false;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void containerChunkUnload(ChunkUnloadEvent event){
        if(magicpipes.getPlugin().isStartPipes()) {
            Chunk chunk = event.getChunk();
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            World world = chunk.getWorld();
            //Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                for (PipeContainer pipeContainer : new ArrayList<>(magicpipes.getPlugin().getActivePipeContainers())) {
                    if (pipeContainer != null) {
                        if (
                                pipeContainer.getChunkX() == chunkX
                                && pipeContainer.getChunkZ() == chunkZ
                                && pipeContainer.getWorld() == world
                        ) {
                            magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                            pipeContainer.setIsActive(false);
                        }
                    } else {
                        magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                        magicpipes.getPlugin().getPipes().remove(pipeContainer);
                    }
                }
            //});
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void containerChunkLoad(ChunkLoadEvent event){
        if(magicpipes.getPlugin().isStartPipes()) {
            Chunk chunk = event.getChunk();
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            World world = chunk.getWorld();

            //Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                List<PipeContainer> activePipeContainers = magicpipes.getPlugin().getActivePipeContainers();
                for (PipeContainer pipeContainer : magicpipes.getPlugin().getPipeContainers()) {
                    if (pipeContainer != null) {
                        if (
                            pipeContainer.getChunkX() == chunkX
                            && pipeContainer.getChunkZ() == chunkZ
                            && pipeContainer.getWorld() == world
                        ) {
                            //Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                                Block block = pipeContainer.getWorld().getBlockAt(
                                        pipeContainer.getX(),
                                        pipeContainer.getY(),
                                        pipeContainer.getZ()
                                );
                                if(block.getState() instanceof Container container){
                                    //Bukkit.getLogger().info("Контейнер");
                                    //Bukkit.getLogger().info(pipeContainer.getPipeParents() + "");
                                    //Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                                        pipeContainer.setContainer(container);
                                        if(!activePipeContainers.contains(pipeContainer)) {
                                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                                        }
                                        pipeContainer.setIsActive(true);
                                    //});
                                }else{
                                    //Bukkit.getLogger().info("Не контейнер");
                                    //Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                                        for(Pipe pipe: pipeContainer.getPipeParents()){
                                            pipe.getPipeContainers().remove(pipeContainer);
                                            pipe.getTakePipeContainers().remove(pipeContainer);
                                        }
                                        magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                                        magicpipes.getPlugin().getPipeContainers().remove(pipeContainer);
                                    //});
                                }
                            //});
                        }
                    } else {
                        magicpipes.getPlugin().getActivePipeContainers().remove(pipeContainer);
                        magicpipes.getPlugin().getPipeContainers().remove(pipeContainer);
                    }
                }
            //});
        }
    }
}
