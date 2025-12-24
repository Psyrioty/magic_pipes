package org.psyrioty.magicpipes.Objects;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.psyrioty.magicpipes.magicpipes;

import java.util.ArrayList;
import java.util.List;

public class PipeContainer {
    World world;
    int x,y,z;
    int chunkX, chunkZ;
    Container container;
    Material material;
    boolean isActive = false;
    List<Pipe> pipeParents = new ArrayList<>();

    public PipeContainer(Block block, Container container){
        world = block.getWorld();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        material = block.getType();
        Chunk chunk = block.getChunk();
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
        this.container = container;


    }

    public void remove(){
        isActive = false;
        pipeParents.clear();
        magicpipes.getPlugin().getPipeContainers().remove(this);
        magicpipes.getPlugin().getActivePipeContainers().remove(this);
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public World getWorld() {
        return world;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getZ() {
        return z;
    }

    public List<Pipe> getPipeParents() {
        return pipeParents;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public Material getMaterial() {
        return material;
    }

    public Container getContainer() {
        return container;
    }

    public boolean getIsActive(){
        return isActive;
    }

    public void setIsActive(boolean isActive){
        this.isActive = isActive;
    }
}
