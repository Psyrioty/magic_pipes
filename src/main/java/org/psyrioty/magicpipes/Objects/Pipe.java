package org.psyrioty.magicpipes.Objects;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.psyrioty.magicpipes.Database.Requests;
import org.psyrioty.magicpipes.magicpipes;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Pipe implements InventoryHolder{
    private final int x;
    private final int y;
    private final int z;
    private boolean isRemove = false;
    private int dbId;

    //0 - обычная
    //1 - собиратель
    private byte type = 0;

    private final World world;
    int chunkX, chunkZ;

    List <Material> materialList = new ArrayList<>();

    private Inventory inventory; //whitelist
    private ItemStack pipeItemStackBlock = new ItemStack(Material.PLAYER_HEAD);


    private List<PipeContainer> takePipeContainers = new ArrayList<>();
    private List<PipeContainer> pipeContainers = new ArrayList<>();
    private List<Pipe> pipes = new ArrayList<>();
    private List<Pipe> parentPipes = new ArrayList<>();
    private boolean isActive = false;

    public Pipe(int x, int y, int z, World world, byte type, int dbId) throws Exception {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.type = type;
        this.dbId = dbId;

        Chunk chunk = world.getChunkAt(new Location(world, x, y, z));
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();

        materialList.add(Material.CHEST);
        materialList.add(Material.SHULKER_BOX);
        materialList.add(Material.BLACK_SHULKER_BOX);
        materialList.add(Material.BLUE_SHULKER_BOX);
        materialList.add(Material.CYAN_SHULKER_BOX);
        materialList.add(Material.BROWN_SHULKER_BOX);
        materialList.add(Material.GRAY_SHULKER_BOX);
        materialList.add(Material.GREEN_SHULKER_BOX);
        materialList.add(Material.LIGHT_BLUE_SHULKER_BOX);
        materialList.add(Material.LIGHT_GRAY_SHULKER_BOX);
        materialList.add(Material.LIME_SHULKER_BOX);
        materialList.add(Material.MAGENTA_SHULKER_BOX);
        materialList.add(Material.ORANGE_SHULKER_BOX);
        materialList.add(Material.WHITE_SHULKER_BOX);
        materialList.add(Material.RED_SHULKER_BOX);
        materialList.add(Material.YELLOW_SHULKER_BOX);
        materialList.add(Material.PINK_SHULKER_BOX);
        materialList.add(Material.PURPLE_SHULKER_BOX);
        materialList.add(Material.HOPPER);
        materialList.add(Material.DISPENSER);
        materialList.add(Material.DROPPER);
        materialList.add(Material.CRAFTER);
        materialList.add(Material.BARREL);
        materialList.add(Material.FURNACE);
        materialList.add(Material.BLAST_FURNACE);
        materialList.add(Material.BREWING_STAND);
        materialList.add(Material.SMOKER);

        if(type == 1) {
            this.inventory = Bukkit.createInventory(this, 54, "WHITE LIST");
        }else if(type == 2){
            this.inventory = Bukkit.createInventory(this, 54, "WHITE LIST");
        }


        String value = "";
        String name = "";
        String key = "";
        if(type == 1){
            value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRhMmViMmIxMDUzOWNlYzIwMmUxODY3ZjI2MWE2ODBkODcyOWFlNGFiYmE5NTdjZGZhMjQ1MzgxNTJmZGM3MSJ9fX0=";
            name = "Собирательная труба";
            key = "pipe_funnel";
        }else if (type == 0){
            value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY3NTVhYzZjMDc4ZDAwODJmNjg3MTUzOWY4YzhlMDM3M2IwMDgyMTRjYWNkYjRjZGZmZmM4ODY2ZGYxZDJlNiJ9fX0=";
            name = "Труба";
            key = "pipe";
        }else{
            value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBlZjQzOWIxOTdjZmQ4ZThkMWJhZDNhODIwY2Q5NmViZjI0YTQ0MjY2YmUwOTJjOTkzMWE0NGZiMzE1OWQzOCJ9fX0=";
            name = "Труба приёмник";
            key = "pipe_receiver";
        }

        pipeItemStackBlock = magicpipes.getPlugin().createCustomHead(value, key, name);

        Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
            Location location = new Location(world, x, y, z);
            location.getBlock().setType(Material.PLAYER_HEAD);
            Bukkit.getScheduler().runTaskLater(magicpipes.getPlugin(), () -> {
                Skull skullBlock = (Skull) location.getBlock().getState();
                SkullMeta skullMeta = (SkullMeta) pipeItemStackBlock.getItemMeta();
                skullBlock.setOwnerProfile(skullMeta.getOwnerProfile());
                skullBlock.update();
            }, 1L);
        });

        checkContainersPlaced(world.getBlockAt(x, y, z));



        List<String> allItems = Requests.getAllItems(this);
        if(allItems != null) {
            for (String base64 : allItems) {
                int slot = Integer.parseInt(base64.substring(0, base64.indexOf(" ")));
                String newBase64 = base64.substring(base64.indexOf(" ") + 1);
                ItemStack itemStack = null;
                try {
                    itemStack = deserializeItems(newBase64);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                if (itemStack != null) {
                    inventory.setItem(slot, itemStack);
                    //inventory.addItem(itemStack);
                }
            }
        }
        isActive = magicpipes.getPlugin().isStartPipes();

        if(magicpipes.getPlugin().isStartPipes()) {
            for (Pipe activePipe : magicpipes.getPlugin().getActivePipe()) {
                if (checkPipe(this, activePipe)) {
                    switch (type) {
                        case 0:
                            if (activePipe.getType() == 1) {
                                if (!activePipe.getPipes().contains(this)) {
                                    activePipe.getPipes().add(this);
                                }
                                if (!parentPipes.contains(activePipe)) {
                                    parentPipes.add(activePipe);
                                }
                            } else if (activePipe.getType() == 2) {
                                if (!activePipe.getParentPipes().contains(this)) {
                                    activePipe.getParentPipes().add(this);
                                }
                                if (!pipes.contains(activePipe)) {
                                    pipes.add(activePipe);
                                }
                            } else if (activePipe.getType() == 0) {
                                if (!activePipe.getParentPipes().contains(this)) {
                                    activePipe.getParentPipes().add(this);
                                }
                                if (!activePipe.getPipes().contains(this)) {
                                    activePipe.getPipes().add(this);
                                }
                                if (!parentPipes.contains(activePipe)) {
                                    parentPipes.add(activePipe);
                                }
                                if (!pipes.contains(activePipe)) {
                                    pipes.add(activePipe);
                                }
                            }
                            break;
                        case 1:
                            if (activePipe.getType() == 2 || activePipe.getType() == 0) {
                                if (!activePipe.getParentPipes().contains(this)) {
                                    activePipe.getParentPipes().add(this);
                                }
                                if (!pipes.contains(activePipe)) {
                                    pipes.add(activePipe);
                                }
                            }
                            break;
                        case 2:
                            if (activePipe.getType() == 1 || activePipe.getType() == 0) {
                                if (!parentPipes.contains(activePipe)) {
                                    parentPipes.add(activePipe);
                                }
                                if (!activePipe.getPipes().contains(this)) {
                                    activePipe.getPipes().add(this);
                                }
                            }
                            break;
                    }
                }
            }
        }else{
            for (Pipe activePipe : magicpipes.getPlugin().getPipes()) {
                if (checkPipe(this, activePipe)) {
                    switch (type) {
                        case 0:
                            if (activePipe.getType() == 1) {
                                if (!activePipe.getPipes().contains(this)) {
                                    activePipe.getPipes().add(this);
                                }
                                if (!parentPipes.contains(activePipe)) {
                                    parentPipes.add(activePipe);
                                }
                            } else if (activePipe.getType() == 2) {
                                if (!activePipe.getParentPipes().contains(this)) {
                                    activePipe.getParentPipes().add(this);
                                }
                                if (!pipes.contains(activePipe)) {
                                    pipes.add(activePipe);
                                }
                            } else if (activePipe.getType() == 0) {
                                if (!activePipe.getParentPipes().contains(this)) {
                                    activePipe.getParentPipes().add(this);
                                }
                                if (!activePipe.getPipes().contains(this)) {
                                    activePipe.getPipes().add(this);
                                }
                                if (!parentPipes.contains(activePipe)) {
                                    parentPipes.add(activePipe);
                                }
                                if (!pipes.contains(activePipe)) {
                                    pipes.add(activePipe);
                                }
                            }
                            break;
                        case 1:
                            if (activePipe.getType() == 2 || activePipe.getType() == 0) {
                                if (!activePipe.getParentPipes().contains(this)) {
                                    activePipe.getParentPipes().add(this);
                                }
                                if (!pipes.contains(activePipe)) {
                                    pipes.add(activePipe);
                                }
                            }
                            break;
                        case 2:
                            if (activePipe.getType() == 1 || activePipe.getType() == 0) {
                                if (!parentPipes.contains(activePipe)) {
                                    parentPipes.add(activePipe);
                                }
                                if (!activePipe.getPipes().contains(this)) {
                                    activePipe.getPipes().add(this);
                                }
                            }
                            break;
                    }
                }
            }
        }
        for(PipeContainer pipeContainer: magicpipes.getPlugin().getActivePipeContainers()){
            if(checkPipeContainer(this, pipeContainer)){
                if(this.getType() == 2) {
                    if(!pipeContainers.contains(pipeContainer)) {
                        pipeContainers.add(pipeContainer);
                    }
                    if(!pipeContainer.getPipeParents().contains(this)) {
                        pipeContainer.getPipeParents().add(this);
                    }
                }else if(this.getType() == 1){
                    if(!takePipeContainers.contains(pipeContainer)) {
                        takePipeContainers.add(pipeContainer);
                    }
                    if(pipeContainer.getPipeParents().contains(this)) {
                        pipeContainer.getPipeParents().add(this);
                    }

                }
            }
        }
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public int getDbId() {
        return dbId;
    }

    private boolean checkPipeContainer(Pipe pipe, PipeContainer pipeContainer){
        if(
                (
                        pipe.getWorld() == pipeContainer.getWorld()
                                && pipe.getX() == pipeContainer.getX() + 1
                                && pipe.getY() == pipeContainer.getY()
                                && pipe.getZ() == pipeContainer.getZ()
                )
                        ||
                        (
                                pipe.getWorld() == pipeContainer.getWorld()
                                        && pipe.getX() == pipeContainer.getX()
                                        && pipe.getY() == pipeContainer.getY() + 1
                                        && pipe.getZ() == pipeContainer.getZ()
                        )
                        ||
                        (
                                pipe.getWorld() == pipeContainer.getWorld()
                                        && pipe.getX() == pipeContainer.getX()
                                        && pipe.getY() == pipeContainer.getY()
                                        && pipe.getZ() == pipeContainer.getZ() + 1
                        )
                        ||
                        (
                                pipe.getWorld() == pipeContainer.getWorld()
                                        && pipe.getX() == pipeContainer.getX() - 1
                                        && pipe.getY() == pipeContainer.getY()
                                        && pipe.getZ() == pipeContainer.getZ()
                        )
                        ||
                        (
                                pipe.getWorld() == pipeContainer.getWorld()
                                        && pipe.getX() == pipeContainer.getX()
                                        && pipe.getY() == pipeContainer.getY() - 1
                                        && pipe.getZ() == pipeContainer.getZ()
                        )
                        ||
                        (
                                pipe.getWorld() == pipeContainer.getWorld()
                                        && pipe.getX() == pipeContainer.getX()
                                        && pipe.getY() == pipeContainer.getY()
                                        && pipe.getZ() == pipeContainer.getZ() - 1
                        )
        ){
            return true;
        }
        return false;
    }

    private boolean checkPipe(Pipe pipe, Pipe activePipe){
        if(
                (
                        pipe.getWorld() == activePipe.getWorld()
                                && pipe.getX() == activePipe.getX() + 1
                                && pipe.getY() == activePipe.getY()
                                && pipe.getZ() == activePipe.getZ()
                )
                        ||
                        (
                                pipe.getWorld() == activePipe.getWorld()
                                        && pipe.getX() == activePipe.getX()
                                        && pipe.getY() == activePipe.getY() + 1
                                        && pipe.getZ() == activePipe.getZ()
                        )
                        ||
                        (
                                pipe.getWorld() == activePipe.getWorld()
                                        && pipe.getX() == activePipe.getX()
                                        && pipe.getY() == activePipe.getY()
                                        && pipe.getZ() == activePipe.getZ() + 1
                        )
                        ||
                        (
                                pipe.getWorld() == activePipe.getWorld()
                                        && pipe.getX() == activePipe.getX() - 1
                                        && pipe.getY() == activePipe.getY()
                                        && pipe.getZ() == activePipe.getZ()
                        )
                        ||
                        (
                                pipe.getWorld() == activePipe.getWorld()
                                        && pipe.getX() == activePipe.getX()
                                        && pipe.getY() == activePipe.getY() - 1
                                        && pipe.getZ() == activePipe.getZ()
                        )
                        ||
                        (
                                pipe.getWorld() == activePipe.getWorld()
                                        && pipe.getX() == activePipe.getX()
                                        && pipe.getY() == activePipe.getY()
                                        && pipe.getZ() == activePipe.getZ() - 1
                        )
        ){
            return true;
        }
        return false;
    }

    private void checkContainersPlaced(Block block){
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        World world = block.getWorld();
        List<PipeContainer> pipeContainersCheck = new ArrayList<>();
        Block block1 = world.getBlockAt(x + 1, y, z);
        if(materialList.contains(block1.getType())) {
            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if (
                        block1.getState() instanceof Container container
                ) {
                    Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                        PipeContainer pipeContainer = new PipeContainer(
                                block1,
                                container
                        );
                        pipeContainer.getPipeParents().add(this);

                        if (magicpipes.getPlugin().isStartPipes()) {
                            pipeContainer.setIsActive(true);
                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        } else {
                            pipeContainer.setIsActive(false);
                        }
                        if(type == 1) {
                            takePipeContainers.add(pipeContainer);
                        }else if(type == 2){
                            pipeContainers.add(pipeContainer);
                        }
                        addPipeContainerInList(pipeContainer);
                    });
                }
            });
        }

        Block block2 = world.getBlockAt(x - 1, y, z);
        if(materialList.contains(block2.getType())) {
            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if (
                        block2.getState() instanceof Container container
                ) {
                    Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                        PipeContainer pipeContainer = new PipeContainer(
                                block2,
                                container
                        );
                        pipeContainer.getPipeParents().add(this);
                        if (magicpipes.getPlugin().isStartPipes()) {
                            pipeContainer.setIsActive(true);
                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        } else {
                            pipeContainer.setIsActive(false);
                        }
                        if(type == 1) {
                            takePipeContainers.add(pipeContainer);
                        }else if(type == 2){
                            pipeContainers.add(pipeContainer);
                        }
                        addPipeContainerInList(pipeContainer);
                    });
                }
            });
        }

        Block block3 = world.getBlockAt(x, y + 1, z);
        if(materialList.contains(block3.getType())) {
            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if (
                        block3.getState() instanceof Container container
                ) {
                    Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                        PipeContainer pipeContainer = new PipeContainer(
                                block3,
                                container
                        );
                        pipeContainer.getPipeParents().add(this);
                        if (magicpipes.getPlugin().isStartPipes()) {
                            pipeContainer.setIsActive(true);
                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        } else {
                            pipeContainer.setIsActive(false);
                        }
                        if(type == 1) {
                            takePipeContainers.add(pipeContainer);
                        }else if(type == 2){
                            pipeContainers.add(pipeContainer);
                        }
                        addPipeContainerInList(pipeContainer);
                    });
                }
            });
        }


        Block block4 = world.getBlockAt(x, y - 1, z);
        if(materialList.contains(block4.getType())) {
            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if (
                        block4.getState() instanceof Container container
                ) {
                    Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                        PipeContainer pipeContainer = new PipeContainer(
                                block4,
                                container
                        );
                        pipeContainer.getPipeParents().add(this);
                        if (magicpipes.getPlugin().isStartPipes()) {
                            pipeContainer.setIsActive(true);
                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        } else {
                            pipeContainer.setIsActive(false);
                        }
                        if(type == 1) {
                            takePipeContainers.add(pipeContainer);
                        }else if(type == 2){
                            pipeContainers.add(pipeContainer);
                        }
                        addPipeContainerInList(pipeContainer);
                    });
                }
            });
        }


        Block block5 = world.getBlockAt(x, y, z + 1);
        if(materialList.contains(block5.getType())) {
            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if (
                        block5.getState() instanceof Container container
                ) {
                    Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                        PipeContainer pipeContainer = new PipeContainer(
                                block5,
                                container
                        );
                        pipeContainer.getPipeParents().add(this);
                        if (magicpipes.getPlugin().isStartPipes()) {
                            pipeContainer.setIsActive(true);
                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        } else {
                            pipeContainer.setIsActive(false);
                        }
                        if(type == 1) {
                            takePipeContainers.add(pipeContainer);
                        }else if(type == 2){
                            pipeContainers.add(pipeContainer);
                        }
                        addPipeContainerInList(pipeContainer);
                    });
                }
            });
        }

        Block block6 = world.getBlockAt(x, y, z - 1);
        if(materialList.contains(block6.getType())) {
            Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if (
                        block6.getState() instanceof Container container
                ) {
                    Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
                        PipeContainer pipeContainer = new PipeContainer(
                                block6,
                                container
                        );
                        pipeContainer.getPipeParents().add(this);
                        if (magicpipes.getPlugin().isStartPipes()) {
                            pipeContainer.setIsActive(true);
                            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
                        } else {
                            pipeContainer.setIsActive(false);
                        }
                        if(type == 1) {
                            takePipeContainers.add(pipeContainer);
                        }else if(type == 2){
                            pipeContainers.add(pipeContainer);
                        }
                        addPipeContainerInList(pipeContainer);
                    });
                }
            });
        }
    }

    private void addPipeContainerInList(PipeContainer pipeContainer){
        magicpipes.getPlugin().getPipeContainers().add(pipeContainer);
        if(magicpipes.getPlugin().isStartPipes()) {
            magicpipes.getPlugin().getActivePipeContainers().add(pipeContainer);
        }
    }

    public List<PipeContainer> getTakePipeContainers() {
        return takePipeContainers;
    }

    public void doTask(){
        boolean inDistance = false;
        for(Player player: Bukkit.getServer().getOnlinePlayers()){
            if(player.getWorld() == world) {
                if (calculateDistance(
                        player.getLocation().getBlockX(), player.getLocation().getBlockZ()
                        , x, z
                ) < 100) {
                    inDistance = true;
                    break;
                }
            }
        }
        if(type != 1 || !isActive || takePipeContainers.isEmpty() || !inDistance){
            return;
        }

        checkTakeInventoriesAsync(result -> {
            //Bukkit.getLogger().info("1");
            if(result){
                return;
            }
            for(Pipe pipe: pipes){
                if(pipe.getIsActive()){
                    List<Pipe> oldPipes = new ArrayList<>();
                    oldPipes.add(pipe);
                    checkContainers(pipe, oldPipes);
                }
            }
        });
    }

    private double calculateDistance(double x1, double z1, double x2, double z2) {
        double deltaX = x2 - x1;
        double deltaZ = z2 - z1;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    private void checkContainers(Pipe pipe, List<Pipe> oldPipes){
        checkTakeInventoriesAsync(result -> {
            oldPipes.add(pipe);
            //Bukkit.getLogger().info("2");
            if(result || !isActive){
                return;
            }
            if(!pipe.getPipeContainers().isEmpty()){
                giveItems(pipe);
            }
            for(Pipe pipeChildren: pipe.getPipes()){
                if(pipeChildren != pipe && !oldPipes.contains(pipeChildren)){
                    checkContainers(pipeChildren, oldPipes);
                }
            }
        });
    }

    private void checkTakeInventoriesAsync(Consumer<Boolean> callback){
        Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
            for(PipeContainer pipeContainer: takePipeContainers){
                if(!pipeContainer.getContainer().getInventory().isEmpty()){
                    callback.accept(false);
                    return;
                }
            }
            callback.accept(true);
        });
    }



    private void giveItems(Pipe pipe){
        //Bukkit.getLogger().info("3");
        for(PipeContainer takePipeContainer: takePipeContainers){
            checkOneTakeInventoriesAsync(result -> {
                if(result){
                    //тут будет основной код
                    takeInventoryTasks(pipe, takePipeContainer);
                }
            }, takePipeContainer);
        }
    }

    private void takeInventoryTasks(Pipe pipe, PipeContainer takePipeContainer){
        Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
            //Bukkit.getLogger().info("4");
            if(takePipeContainer.getContainer().getInventory().getSize() < 27){
                takeInventoryToolTasks(pipe, takePipeContainer);
            }else{
                takeInventoryChestTasks(pipe, takePipeContainer);
            }
        });
    }

    private void takeInventoryToolTasks(Pipe pipe, PipeContainer takePipeContainer){
        //Bukkit.getLogger().info("5");
        int takeInventoryCount = 0;
        Inventory takeInventory = takePipeContainer.getContainer().getInventory();
        for(ItemStack takeItem: takeInventory.getContents()){
            if(inventory.isEmpty()){
                //идём дальше
                giveInventoriesCheck(
                        pipe,
                        takePipeContainer,
                        takeInventory,
                        takeInventoryCount,
                        takeItem
                );
            }else{
                ItemStack itemWhiteList = inventory.getItem(takeInventoryCount);
                if(itemWhiteList == null){
                    //идём дальше
                    giveInventoriesCheck(
                            pipe,
                            takePipeContainer,
                            takeInventory,
                            takeInventoryCount,
                            takeItem
                    );
                }else{
                    if(itemWhiteList.isSimilar(takeItem)){
                        //идём дальше
                        giveInventoriesCheck(
                                pipe,
                                takePipeContainer,
                                takeInventory,
                                takeInventoryCount,
                                takeItem
                        );
                    }
                }
            }
            takeInventoryCount++;
        }
    }

    private void takeInventoryChestTasks(Pipe pipe, PipeContainer takePipeContainer){
        int takeInventoryCount = 0;
        Inventory takeInventory = takePipeContainer.getContainer().getInventory();
        for(ItemStack takeItem: takeInventory.getContents()){
            if(inventory.isEmpty()){
                //идём дальше
                giveInventoriesCheck(
                        pipe,
                        takePipeContainer,
                        takeInventory,
                        takeInventoryCount,
                        takeItem
                );
            }else{
                ItemStack itemWhiteList = inventory.getItem(takeInventoryCount);
                boolean isSimilar = false;
                for(ItemStack itemWhiteListAll: inventory.getContents()){
                    if(itemWhiteListAll != null){
                        if(itemWhiteListAll.isSimilar(takeItem)){
                            isSimilar = true;
                            break;
                        }
                    }
                }
                if(itemWhiteList != null && isSimilar){
                    if(itemWhiteList.getType() != Material.BARRIER){
                        //идём дальше
                        giveInventoriesCheck(
                                pipe,
                                takePipeContainer,
                                takeInventory,
                                takeInventoryCount,
                                takeItem
                                );
                    }
                }else if(isSimilar){
                    //идём дальше
                    giveInventoriesCheck(
                            pipe,
                            takePipeContainer,
                            takeInventory,
                            takeInventoryCount,
                            takeItem
                    );
                }
            }
            takeInventoryCount++;
        }
    }

    //чекаем получателей
    private void giveInventoriesCheck(
            Pipe pipe,
            PipeContainer takePipeContainer,
            Inventory takeInventory,
            int takeInventoryCount,
            ItemStack takeItem
    ){
        //Bukkit.getLogger().info("6");
        for(PipeContainer givePipeContainer: pipe.getPipeContainers()){
            if(takeItem != null && givePipeContainer.isActive) {
                Inventory giveInventory = givePipeContainer.getContainer().getInventory();
                if (giveInventory.getSize() < 27) {
                    giveInventoryCheckTool(
                            pipe,
                            takePipeContainer,
                            takeInventory,
                            takeInventoryCount,
                            givePipeContainer,
                            giveInventory,
                            takeItem
                    );
                } else {
                    giveInventoryCheckChest(
                            pipe,
                            takePipeContainer,
                            takeInventory,
                            takeInventoryCount,
                            givePipeContainer,
                            giveInventory,
                            takeItem
                    );
                }
            }
        }
    }


    //чекаем вайт листы инструмента получателя
    private void giveInventoryCheckTool(
            Pipe pipe,
            PipeContainer takePipeContainer,
            Inventory takeInventory,
            int takeInventoryCount,
            PipeContainer givePipeContainer,
            Inventory giveInventory,
            ItemStack takeItem
    ){
        //Bukkit.getLogger().info("7");
        Inventory whiteList = pipe.getInventory();
        if(whiteList.isEmpty()){
            //код дальше
            List<Integer> trueWhiteListSlots = new ArrayList<>();
            trueWhiteListSlots.add(-1);
            preCheckTransportItem(
                    pipe,
                    takePipeContainer,
                    takeInventory,
                    takeInventoryCount,
                    givePipeContainer,
                    giveInventory,
                    takeItem,
                    trueWhiteListSlots
            );
        }else{
            List<Integer> trueWhiteListSlots = new ArrayList<>();
            int giveInventorySlot = 0;
            for(ItemStack whiteListItemStack: whiteList.getContents()){
                if(
                        takeItem.isSimilar(whiteListItemStack)
                ){
                    trueWhiteListSlots.add(giveInventorySlot);
                }
                giveInventorySlot++;
            }
            if(!trueWhiteListSlots.isEmpty()){
                //код дальше
                preCheckTransportItem(
                        pipe,
                        takePipeContainer,
                        takeInventory,
                        takeInventoryCount,
                        givePipeContainer,
                        giveInventory,
                        takeItem,
                        trueWhiteListSlots
                );
            }
        }
    }

    //чекаем вайт листы сундука получателя
    private void giveInventoryCheckChest(
            Pipe pipe,
            PipeContainer takePipeContainer,
            Inventory takeInventory,
            int takeInventoryCount,
            PipeContainer givePipeContainer,
            Inventory giveInventory,
            ItemStack takeItem
    ){
        //Bukkit.getLogger().info("8");
        Inventory whiteList = pipe.getInventory();
        if(whiteList.isEmpty()){
            //код дальше
            List<Integer> trueWhiteListSlots = new ArrayList<>();
            trueWhiteListSlots.add(-1);
            preCheckTransportItem(
                    pipe,
                    takePipeContainer,
                    takeInventory,
                    takeInventoryCount,
                    givePipeContainer,
                    giveInventory,
                    takeItem,
                    trueWhiteListSlots
            );
        }else{
            List<Integer> trueWhiteListSlots = new ArrayList<>();
            int giveInventorySlot = 0;
            boolean isSimilar = false;
            for(ItemStack itemWhiteList: pipe.getInventory().getContents()){
                if(itemWhiteList != null || itemWhiteList.getType() != Material.BARRIER){
                    trueWhiteListSlots.add(giveInventorySlot);
                }
                if(takeItem.isSimilar(itemWhiteList)){
                    isSimilar = true;
                }
                giveInventorySlot++;
            }
            if(isSimilar){
                //код дальше
                preCheckTransportItem(
                        pipe,
                        takePipeContainer,
                        takeInventory,
                        takeInventoryCount,
                        givePipeContainer,
                        giveInventory,
                        takeItem,
                        trueWhiteListSlots
                );
            }
        }
    }

    //последние проверки перед перемещением
    private void preCheckTransportItem(
            Pipe pipe,
            PipeContainer takePipeContainer,
            Inventory takeInventory,
            int takeInventoryCount,
            PipeContainer givePipeContainer,
            Inventory giveInventory,
            ItemStack takeItem,
            List<Integer> trueWhiteListSlots
    ){
        if(trueWhiteListSlots.isEmpty()){
            return;
        }
        //Bukkit.getLogger().info("9");

        if(giveInventory.getSize() < 27){
            if(trueWhiteListSlots.getFirst() == -1){
                int slot = 0;
                for(ItemStack giveItem: giveInventory.getContents()){
                    if(giveItem == null){
                        ItemStack newItemCopy = takeItem.clone();
                        takeItem.setAmount(0);
                        giveInventory.setItem(slot, newItemCopy);
                        return;
                    }else if(takeItem.isSimilar(giveItem) && giveItem.getAmount() < giveItem.getMaxStackSize()){
                        int maxAddAmount = giveItem.getMaxStackSize() - giveItem.getAmount();
                        int takeAmount = takeItem.getAmount();
                        if(maxAddAmount <= takeAmount) {
                            int newTakeAmount = takeAmount - maxAddAmount;
                            takeItem.setAmount(newTakeAmount);
                            giveItem.setAmount(giveItem.getMaxStackSize());
                            if (newTakeAmount <= 0) {
                                return;
                            }
                        }else{
                            takeItem.setAmount(0);
                            int giveAmount = giveItem.getAmount();
                            giveItem.setAmount(giveAmount + takeAmount);
                            return;
                        }
                    }
                    slot ++;
                }
            }else {
                for (int slot : trueWhiteListSlots) {
                    if (slot < giveInventory.getSize()) {
                        ItemStack giveItem = giveInventory.getItem(slot);
                        if(giveItem == null){
                            ItemStack newItem = takeItem.clone();
                            takeItem.setAmount(0);
                            giveInventory.setItem(slot, newItem);
                            return;
                        }else if(giveItem.isSimilar(takeItem) && giveItem.getAmount() < giveItem.getMaxStackSize()){
                            int maxAddAmount = giveItem.getMaxStackSize() - giveItem.getAmount();
                            int takeAmount = takeItem.getAmount();
                            if(maxAddAmount <= takeAmount) {
                                int newTakeAmount = takeAmount - maxAddAmount;
                                takeItem.setAmount(newTakeAmount);
                                giveItem.setAmount(giveItem.getMaxStackSize());
                                if (newTakeAmount <= 0) {
                                    return;
                                }
                            }else{
                                takeItem.setAmount(0);
                                int giveAmount = giveItem.getAmount();
                                giveItem.setAmount(giveAmount + takeAmount);
                                return;
                            }
                        }
                    }
                }
            }
        }else {
            if(trueWhiteListSlots.getFirst() == -1){
                int slot = 0;
                for(ItemStack giveItem: giveInventory.getContents()){
                    if(giveItem == null){
                        ItemStack newItemCopy = takeItem.clone();
                        takeItem.setAmount(0);
                        giveInventory.setItem(slot, newItemCopy);
                        return;
                    }else if(takeItem.isSimilar(giveItem) && giveItem.getAmount() < giveItem.getMaxStackSize()){
                        int maxAddAmount = giveItem.getMaxStackSize() - giveItem.getAmount();
                        int takeAmount = takeItem.getAmount();
                        if(maxAddAmount <= takeAmount) {
                            int newTakeAmount = takeAmount - maxAddAmount;
                            takeItem.setAmount(newTakeAmount);
                            giveItem.setAmount(giveItem.getMaxStackSize());
                            if (newTakeAmount <= 0) {
                                return;
                            }
                        }else{
                            takeItem.setAmount(0);
                            int giveAmount = giveItem.getAmount();
                            giveItem.setAmount(giveAmount + takeAmount);
                            return;
                        }
                    }
                    slot++;
                }
            }else{
                for (int slot : trueWhiteListSlots) {
                    if (slot < giveInventory.getSize()) {
                        ItemStack giveItem = giveInventory.getItem(slot);
                        if(giveItem == null){
                            ItemStack newItem = takeItem.clone();
                            takeItem.setAmount(0);
                            giveInventory.setItem(slot, newItem);
                            return;
                        }else if(giveItem.isSimilar(takeItem) && giveItem.getAmount() < giveItem.getMaxStackSize()){
                            int maxAddAmount = giveItem.getMaxStackSize() - giveItem.getAmount();
                            int takeAmount = takeItem.getAmount();
                            if(maxAddAmount <= takeAmount) {
                                int newTakeAmount = takeAmount - maxAddAmount;
                                takeItem.setAmount(newTakeAmount);
                                giveItem.setAmount(giveItem.getMaxStackSize());
                                if (newTakeAmount <= 0) {
                                    return;
                                }
                            }else{
                                takeItem.setAmount(0);
                                int giveAmount = giveItem.getAmount();
                                giveItem.setAmount(giveAmount + takeAmount);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkOneTakeInventoriesAsync(Consumer<Boolean> callback, PipeContainer takePipeContainer){
        Bukkit.getScheduler().runTask(magicpipes.getPlugin(), () -> {
                if(!takePipeContainer.getContainer().getInventory().isEmpty()){
                    callback.accept(true);
                    return;
                }
            callback.accept(false);
        });
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }

    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }

    public List<Pipe> getParentPipes() {
        return parentPipes;
    }

    public void setParentPipes(List<Pipe> parentPipes) {
        this.parentPipes = parentPipes;
    }

    public List<Pipe> getPipes() {
        return pipes;
    }

    public boolean getIsActive(){
        return isActive;
    }

    public void setIsActive(boolean isActive){
        this.isActive = isActive;
    }


    public List<PipeContainer> getPipeContainers() {
        return pipeContainers;
    }

    public void remove(){
        Requests.removePipe(this);
        magicpipes.getPlugin().getPipes().remove(this);
        magicpipes.getPlugin().getActivePipe().remove(this);
        for(PipeContainer pipeContainer: pipeContainers){
            pipeContainer.getPipeParents().remove(this);
            if(pipeContainer.getPipeParents().isEmpty()){
                pipeContainer.remove();
            }
        }

        isRemove = true;
    }

    public static String serializeItems(ItemStack itemStack) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(itemStack);

        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    public static ItemStack deserializeItems(String string) throws IOException, ClassNotFoundException {
        ItemStack item;
        if (string.isEmpty()) return null;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        item = ((ItemStack) dataInput.readObject());
        dataInput.close();
        return item;
    }

    public ItemStack getPipeItemStackBlock() {
        return pipeItemStackBlock;
    }

    public boolean getIsRemove(){return isRemove;}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public byte getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
