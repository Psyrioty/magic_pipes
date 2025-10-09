package org.psyrioty.magic_pipes.Objects;

import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.psyrioty.magic_pipes.Database.Requests;
import org.psyrioty.magic_pipes.Magic_pipes;
import org.w3c.dom.ls.LSException;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pipe implements InventoryHolder, Listener {
    private final int x;
    private final int y;
    private final int z;
    private boolean isRemove = false;

    //0 - обычная
    //1 - собиратель
    private byte type = 0;

    private final World world;

    private BukkitTask update;
    List <Material> materialList = new ArrayList<>();

    private List<BlockState> inventoryList = new ArrayList<>();
    private List<BlockState> inventoryListTake = new ArrayList<>();
    private List<Pipe> cashPipe = new ArrayList<>();
    private Inventory inventory;
    private ItemStack itemStackForPipeItems;
    private ItemStack pipeItemStackBlock = new ItemStack(Material.PLAYER_HEAD);
    private List<Inventory> whiteListReceiver = new ArrayList<>();

    private List<Block> cashContainersBlock = new ArrayList<>();
    private List<BlockState> cashBlockStateContainers = new ArrayList<>();

    public Pipe(int x, int y, int z, World world, byte type) throws Exception {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.type = type;

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
            Update();
            this.inventory = Bukkit.createInventory(this, 54, "WHITE LIST");
        }else if(type == 2){
            this.inventory = Bukkit.createInventory(this, 54, "WHITE LIST");
        }
        Bukkit.getPluginManager().registerEvents(this, Magic_pipes.getPlugin());


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

        pipeItemStackBlock = Magic_pipes.getPlugin().createCustomHead(value, key, name);

        Bukkit.getScheduler().runTask(Magic_pipes.getPlugin(), () -> {
            Location location = new Location(world, x, y, z);
            location.getBlock().setType(Material.PLAYER_HEAD);
            Bukkit.getScheduler().runTaskLater(Magic_pipes.getPlugin(), () -> {
                Skull skullBlock = (Skull) location.getBlock().getState();
                SkullMeta skullMeta = (SkullMeta) pipeItemStackBlock.getItemMeta();
                skullBlock.setOwnerProfile(skullMeta.getOwnerProfile());
                skullBlock.update();
            }, 1L);
        });



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
    }

    public List<Block> getCashContainersBlock() {
        return cashContainersBlock;
    }

    public List<BlockState> getCashBlockStateContainers() {
        return cashBlockStateContainers;
    }

    private void Update(){
        update = Bukkit.getServer().getScheduler().runTaskTimer(Magic_pipes.getPlugin(), () -> {
            //Bukkit.getLogger().info("Ложим сюда " + this.inventoryList);
            //Bukkit.getLogger().info("Берём от сюда " + this.inventoryListTake);
            Location chunkLocation = new Location(world, x, y, z);
            boolean playerNear = false;
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(chunkLocation) <= 112 * 112) {
                    playerNear = true;
                    break;
                }
            }
            if(playerNear){
                boolean whiteListNull = true;
                for (ItemStack itemStackWhiteList : this.inventory.getContents()) {
                    if (itemStackWhiteList != null) {
                        if (itemStackWhiteList.getType() != Material.BARRIER) {
                            whiteListNull = false;
                        }
                        break;
                    }
                }
                for (BlockState blockStateTake : this.inventoryListTake) {
                    int blockStateGiveIterator = 0;
                    for (BlockState blockStateGive : this.inventoryList) {
                        Container containerTake = (Container) blockStateTake;
                        Container containerGive = (Container) blockStateGive;
                        if (containerTake != null && containerGive != null) {
                            int itemStackSlot = 0;
                            for (ItemStack itemStackTake : containerTake.getInventory()) {
                                if (itemStackTake != null) {
                                    boolean inWhiteList = false;
                                    boolean inBlocked = false;
                                    boolean inBlockedReceiver = false;
                                    int whiteListSlot = 0;
                                    int whiteListReceiverSlot = 0;
                                    boolean inWhiteListReceiver = false;
                                    boolean whiteListReceiverIsNull = true;
                                    Inventory inventoryWhiteListReceiver = null;

                                    for (ItemStack itemStackWhiteList : this.inventory.getContents()) {
                                        if (itemStackWhiteList != null) {
                                            if (itemStackTake.isSimilar(itemStackWhiteList)) {
                                                inWhiteList = true;
                                                break;
                                            }

                                            if (whiteListSlot == itemStackSlot && itemStackWhiteList.getType() == Material.BARRIER) {
                                                inBlocked = true;
                                            }
                                        }
                                        whiteListSlot++;
                                    }

                                    int itemGiveSlot = 0;
                                    List<Integer> blockReceiveSlots = new ArrayList<>();
                                    for (ItemStack itemStack : containerGive.getInventory()) {
                                        if (whiteListReceiver != null) {
                                            if (whiteListReceiver.size() > blockStateGiveIterator) {
                                                inventoryWhiteListReceiver = whiteListReceiver.get(blockStateGiveIterator);
                                            }
                                            if (inventoryWhiteListReceiver != null) {
                                                int whiteListReceiveSlotContainer = 0;
                                                for (ItemStack itemStackWhiteListReceiver : inventoryWhiteListReceiver.getContents()) {
                                                    if (itemStackWhiteListReceiver != null) {
                                                        if (itemStackWhiteListReceiver.isSimilar(itemStackTake)) {
                                                            inWhiteListReceiver = true;
                                                            whiteListReceiverIsNull = false;
                                                            whiteListReceiverSlot = whiteListReceiveSlotContainer;
                                                            //whiteListSlot = whiteListReceiverSlot;
                                                            //break;
                                                        }
                                                        if (itemStackWhiteListReceiver.getType() != Material.BARRIER) {
                                                            whiteListReceiverIsNull = false;
                                                        }
                                                        if (whiteListReceiverSlot == itemGiveSlot && itemStackWhiteListReceiver.getType() == Material.BARRIER) {
                                                            inBlockedReceiver = true;
                                                            blockReceiveSlots.add(whiteListReceiveSlotContainer);
                                                        }
                                                    }
                                                    whiteListReceiveSlotContainer++;
                                                }
                                            }
                                        }
                                        itemGiveSlot++;
                                    }


                                    if (!inBlocked && !inBlockedReceiver) {
                                        if (
                                                (inWhiteList || whiteListNull)
                                                        && (inWhiteListReceiver || whiteListReceiverIsNull)
                                        ) {
                                            boolean isSimilar = false;
                                            if (
                                                    containerGive.getType() != Material.CRAFTER
                                                    && containerGive.getType() != Material.FURNACE
                                                    && containerGive.getType() != Material.BLAST_FURNACE
                                                    && containerGive.getType() != Material.SMOKER
                                            ) {
                                                for (ItemStack itemStackGive : containerGive.getInventory()) {
                                                    if (itemStackGive != null) {
                                                        if (itemStackGive.isSimilar(itemStackTake)) {
                                                            int itemStackGiveAmount = itemStackGive.getAmount();
                                                            int itemStackTakeAmount = itemStackTake.getAmount();
                                                            int itemStackRemainderAmount = 0;
                                                            if (itemStackGive.getMaxStackSize() >= itemStackTakeAmount + itemStackGiveAmount) {
                                                                itemStackGive.setAmount(itemStackGiveAmount + itemStackTakeAmount);
                                                                itemStackTake.setAmount(itemStackRemainderAmount);
                                                            } else {
                                                                itemStackRemainderAmount = (itemStackGive.getMaxStackSize() - itemStackTakeAmount - itemStackGiveAmount) * -1;
                                                                itemStackGive.setAmount(itemStackGive.getMaxStackSize());
                                                                itemStackTake.setAmount(itemStackRemainderAmount);
                                                            }
                                                            if (itemStackGive.getMaxStackSize() == itemStackGive.getAmount()) {
                                                                isSimilar = false;
                                                            } else {
                                                                isSimilar = true;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (!isSimilar) {
                                                boolean isNull = false;
                                                if (
                                                        (
                                                                (inWhiteListReceiver && inWhiteList)
                                                                || (inWhiteListReceiver && whiteListNull)
                                                                || (whiteListReceiverIsNull && inWhiteList)
                                                        )
                                                                && (
                                                                containerGive.getType() == Material.CRAFTER
                                                                || containerGive.getType() == Material.FURNACE
                                                                || containerGive.getType() == Material.BLAST_FURNACE
                                                                || containerGive.getType() == Material.SMOKER
                                                        )
                                                                && whiteListSlot < containerGive.getInventory().getSize()
                                                ) {
                                                    if (whiteListReceiverIsNull) {
                                                        if (containerGive.getInventory().getItem(whiteListSlot) == null) {
                                                            isNull = true;
                                                        }
                                                    } else {
                                                        if (
                                                                containerGive.getInventory().getItem(whiteListReceiverSlot) == null
                                                                && containerGive.getType() == Material.BREWING_STAND
                                                                && containerGive.getType() == Material.CRAFTER
                                                                && containerGive.getType() == Material.FURNACE
                                                                && containerGive.getType() == Material.BLAST_FURNACE
                                                                && containerGive.getType() == Material.SMOKER
                                                        ) {
                                                            isNull = true;
                                                        }/* else {
                                                            for (ItemStack itemStack: inventoryWhiteListReceiver){
                                                                if(itemStack != null) {
                                                                    if (itemStack.isSimilar(itemStackTake)) {
                                                                        isNull = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }*/
                                                    }
                                                } else {
                                                    for (ItemStack itemStackGive : containerGive.getInventory()) {
                                                        if (itemStackGive == null) {
                                                            isNull = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (isNull) {
                                                    if (
                                                            containerGive.getType() != Material.BREWING_STAND
                                                            && containerGive.getType() != Material.CRAFTER
                                                            && containerGive.getType() != Material.FURNACE
                                                            && containerGive.getType() != Material.BLAST_FURNACE
                                                            && containerGive.getType() != Material.SMOKER

                                                    ) {
                                                        containerGive.getInventory().addItem(itemStackTake);
                                                        itemStackTake.setAmount(0);
                                                    } else if (containerGive.getType() == Material.BREWING_STAND) {
                                                        if (
                                                                itemStackTake.getType() == Material.POTION
                                                                        && (
                                                                        containerGive.getInventory().getItem(0) == null
                                                                                || containerGive.getInventory().getItem(1) == null
                                                                                || containerGive.getInventory().getItem(2) == null
                                                                )
                                                        ) {
                                                            containerGive.getInventory().addItem(itemStackTake);
                                                            itemStackTake.setAmount(0);
                                                        } else if (itemStackTake.getType() == Material.BLAZE_POWDER && containerGive.getInventory().getItem(4) == null) {
                                                            containerGive.getInventory().setItem(4, itemStackTake);
                                                            itemStackTake.setAmount(0);
                                                        } else if (
                                                                containerGive.getInventory().getItem(3) == null
                                                                        && itemStackTake.getType() != Material.POTION
                                                                && (inWhiteListReceiver || whiteListReceiverIsNull) && (inWhiteList || whiteListNull) && !inBlockedReceiver && !inBlocked
                                                        ) {
                                                            boolean slotInWhiteList = false;
                                                            boolean slotInWhiteListReceiver = false;
                                                            if(!whiteListNull){
                                                                if(whiteListSlot == 3){
                                                                    slotInWhiteList = true;
                                                                }
                                                            }
                                                            if(!whiteListReceiverIsNull){
                                                                if(whiteListReceiverSlot == 3){
                                                                    slotInWhiteListReceiver = true;
                                                                }
                                                            }
                                                            if(
                                                                    (slotInWhiteList || whiteListNull)
                                                                    && (slotInWhiteListReceiver || whiteListReceiverIsNull)
                                                            ) {
                                                                containerGive.getInventory().setItem(3, itemStackTake);
                                                                itemStackTake.setAmount(0);
                                                            }else if(!whiteListNull && !whiteListReceiverIsNull){
                                                                containerGive.getInventory().setItem(3, itemStackTake);
                                                                itemStackTake.setAmount(0);
                                                            }
                                                        }
                                                    } else if (
                                                            (whiteListSlot < containerGive.getInventory().getSize() && whiteListReceiverIsNull)
                                                    ) {
                                                        int itemStackRemainderAmount = 0;
                                                        ItemStack itemStackGive = containerGive.getInventory().getItem(whiteListSlot);
                                                        if(containerGive.getInventory().getItem(whiteListSlot) == null) {
                                                            containerGive.getInventory().setItem(whiteListSlot, itemStackTake);
                                                            itemStackTake.setAmount(0);
                                                        }else if(containerGive.getInventory().getItem(whiteListSlot).isSimilar(itemStackTake)){
                                                            if (itemStackGive.getMaxStackSize() >= itemStackTake.getAmount() + itemStackGive.getAmount()) {
                                                                itemStackGive.setAmount(itemStackGive.getAmount() + itemStackTake.getAmount());
                                                                itemStackTake.setAmount(itemStackRemainderAmount);
                                                            } else if (itemStackGive.getMaxStackSize() < itemStackTake.getAmount() + itemStackGive.getAmount()){
                                                                itemStackRemainderAmount = (itemStackGive.getMaxStackSize() - itemStackTake.getAmount() - itemStackGive.getAmount()) * -1;
                                                                itemStackGive.setAmount(itemStackGive.getMaxStackSize());
                                                                itemStackTake.setAmount(itemStackRemainderAmount);
                                                            }
                                                        }
                                                    } else if (
                                                            (whiteListReceiverSlot < containerGive.getInventory().getSize())
                                                    ) {
                                                        int itemStackRemainderAmount = 0;
                                                        ItemStack itemStackGive = containerGive.getInventory().getItem(whiteListReceiverSlot);
                                                        if(containerGive.getInventory().getItem(whiteListReceiverSlot) == null) {
                                                            containerGive.getInventory().setItem(whiteListReceiverSlot, itemStackTake);
                                                            itemStackTake.setAmount(0);
                                                        }else if(containerGive.getInventory().getItem(whiteListReceiverSlot).isSimilar(itemStackTake)){
                                                            if (itemStackGive.getMaxStackSize() >= itemStackTake.getAmount() + itemStackGive.getAmount()) {
                                                                itemStackGive.setAmount(itemStackGive.getAmount() + itemStackTake.getAmount());
                                                                itemStackTake.setAmount(itemStackRemainderAmount);
                                                            } else if (itemStackGive.getMaxStackSize() < itemStackTake.getAmount() + itemStackGive.getAmount()){
                                                                itemStackRemainderAmount = (itemStackGive.getMaxStackSize() - itemStackTake.getAmount() - itemStackGive.getAmount()) * -1;
                                                                itemStackGive.setAmount(itemStackGive.getMaxStackSize());
                                                                itemStackTake.setAmount(itemStackRemainderAmount);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (inBlockedReceiver && !inBlocked && (inWhiteList || whiteListNull) && (whiteListReceiverIsNull || inWhiteListReceiver)) {
                                        int slot = 0;
                                        for (ItemStack itemStackGive : containerGive.getInventory()) {
                                            int itemStackRemainderAmount = 0;
                                            boolean isNull = false;
                                            boolean isSimilar = false;
                                            int nullSlotFirst = -1;
                                            int i = 0;
                                            for (ItemStack itemStack : containerGive.getInventory()) {
                                                if (itemStack == null) {
                                                    isNull = true;
                                                    if(nullSlotFirst == -1 && !blockReceiveSlots.contains(i)) {
                                                        nullSlotFirst = i;
                                                    }
                                                }else if (itemStack.isSimilar(itemStackGive)) {
                                                    isSimilar = true;
                                                }
                                                i++;
                                            }
                                            if (!blockReceiveSlots.contains(slot)) {
                                                if (
                                                        containerGive.getType() != Material.BREWING_STAND
                                                        && containerGive.getType() != Material.CRAFTER
                                                        && containerGive.getType() != Material.FURNACE
                                                        && containerGive.getType() != Material.BLAST_FURNACE
                                                        && containerGive.getType() != Material.SMOKER
                                                )
                                                {
                                                    if(isSimilar){
                                                        if (itemStackGive.getMaxStackSize() >= itemStackTake.getAmount() + itemStackGive.getAmount()) {
                                                            itemStackGive.setAmount(itemStackGive.getAmount() + itemStackTake.getAmount());
                                                            itemStackTake.setAmount(itemStackRemainderAmount);
                                                        } else if (itemStackGive.getMaxStackSize() < itemStackTake.getAmount() + itemStackGive.getAmount()){
                                                            itemStackRemainderAmount = (itemStackGive.getMaxStackSize() - itemStackTake.getAmount() - itemStackGive.getAmount()) * -1;
                                                            itemStackGive.setAmount(itemStackGive.getMaxStackSize());
                                                            itemStackTake.setAmount(itemStackRemainderAmount);
                                                        }
                                                    }else if(isNull){
                                                        containerGive.getInventory().setItem(nullSlotFirst, itemStackTake);
                                                        itemStackTake.setAmount(0);
                                                    }
                                                }else if (
                                                        itemStackGive == null
                                                        && slot < containerGive.getInventory().getSize()
                                                ) {
                                                    containerGive.getInventory().setItem(slot, itemStackTake);
                                                    itemStackTake.setAmount(0);
                                                } else if (
                                                        itemStackGive.getMaxStackSize() >= itemStackTake.getAmount() + itemStackGive.getAmount()
                                                                && slot < containerGive.getInventory().getSize()
                                                        && itemStackGive.isSimilar(itemStackTake)
                                                ) {
                                                    itemStackGive.setAmount(itemStackGive.getAmount() + itemStackTake.getAmount());
                                                    itemStackTake.setAmount(itemStackRemainderAmount);
                                                } else if(
                                                        itemStackGive.getMaxStackSize() < itemStackTake.getAmount() + itemStackGive.getAmount()
                                                                && slot < containerGive.getInventory().getSize()
                                                                && itemStackGive.isSimilar(itemStackTake)
                                                ){
                                                    itemStackRemainderAmount = (itemStackGive.getMaxStackSize() - itemStackTake.getAmount() - itemStackGive.getAmount()) * -1;
                                                    itemStackGive.setAmount(itemStackGive.getMaxStackSize());
                                                    itemStackTake.setAmount(itemStackRemainderAmount);
                                                }
                                            }
                                            slot++;
                                        }
                                    }
                                }
                                itemStackSlot++;
                            }
                        }
                        blockStateGiveIterator++;
                    }

                }


                inventoryList.clear();
                inventoryListTake.clear();
                cashPipe.clear();
                whiteListReceiver.clear();

                switch (type) {
                    case 0:
                        break;
                    case 1:
                        Bukkit.getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), () -> {
                            boolean pipeFind = true;
                            List<Pipe> pipes = new ArrayList<>();
                            pipes.add(this);
                            while (pipeFind) {
                                    List<Pipe> newPipes = checkPipe(pipes);
                                pipes.clear();
                                pipes.addAll(newPipes);
                                newPipes.clear();
                                if (pipes.isEmpty()) {
                                    pipeFind = false;
                                }
                            }
                        });

                        Block block = new Location(world, x, y, z).getBlock();
                        for (BlockFace face : BlockFace.values()) {
                            if (face == BlockFace.UP || face == BlockFace.DOWN ||
                                    face == BlockFace.NORTH || face == BlockFace.SOUTH ||
                                    face == BlockFace.EAST || face == BlockFace.WEST) {
                                Block blockFace = block.getRelative(face);
                                if (blockFace.getState() instanceof Container) {
                                    BlockState blockState = blockFace.getState();
                                    this.inventoryListTake.add(blockState);
                                }
                            }
                        }


                        break;
                    }
                }

        }, 20L, 20L);
    }

    private List<Pipe> checkPipe(List<Pipe> oldPipes){
        List<Pipe> newPipe = new ArrayList<>();

        cashPipe.addAll(oldPipes);

        for (Pipe oldPipe: oldPipes){
            Block block = new Location(
                    oldPipe.getWorld(),
                    oldPipe.getX(),
                    oldPipe.getY(),
                    oldPipe.getZ()
            ).getBlock();

            for (BlockFace face : BlockFace.values()) {
                if (face == BlockFace.UP || face == BlockFace.DOWN ||
                        face == BlockFace.NORTH || face == BlockFace.SOUTH ||
                        face == BlockFace.EAST || face == BlockFace.WEST) {
                    Block blockFace = block.getRelative(face);

                    for (Pipe pipe : Magic_pipes.getPlugin().getPipes()) {
                        if (
                                pipe.getWorld() == blockFace.getWorld()
                                        && pipe.getX() == blockFace.getX()
                                        && pipe.getY() == blockFace.getY()
                                        && pipe.getZ() == blockFace.getZ()
                                        && (
                                        pipe.getType() == 0
                                                || pipe.getType() == 1
                                                || pipe.getType() == 2
                                )
                                        && !cashPipe.contains(pipe)
                        ) {
                            newPipe.add(pipe);
                            break;
                        }
                    }
                    if(materialList.contains(blockFace.getType()) && (
                            oldPipe.getType() == 0
                                    || oldPipe.getType() == 2
                    )){
                        int i = 0;
                        boolean blockInCash = false;
                        for(Block cashBlock: cashContainersBlock){
                            if(
                                    cashBlock.equals(blockFace)
                            ){
                                this.inventoryList.add(cashBlockStateContainers.get(i));
                                if (oldPipe.getType() == 2) {
                                    this.whiteListReceiver.add(oldPipe.getInventory());
                                }
                                blockInCash = true;
                                break;
                            }
                            i++;
                        }

                        if(!blockInCash) {
                            Bukkit.getServer().getScheduler().runTask(Magic_pipes.getPlugin(), () -> {
                                BlockState blockState = blockFace.getState();
                                this.inventoryList.add(blockState);
                                cashContainersBlock.add(blockFace);
                                cashBlockStateContainers.add(blockState);
                                if (oldPipe.getType() == 2) {
                                    this.whiteListReceiver.add(oldPipe.getInventory());
                                }
                            });
                        }
                    }
                    /*Bukkit.getServer().getScheduler().runTask(Magic_pipes.getPlugin(), () -> {
                        if (blockFace.getState() instanceof Container
                                && (
                                oldPipe.getType() == 0
                                        || oldPipe.getType() == 2
                        )
                        ) {
                            BlockState blockState = blockFace.getState();
                            this.inventoryList.add(blockState);
                            if (oldPipe.getType() == 2) {
                                this.whiteListReceiver.add(oldPipe.getInventory());
                            }
                        }
                    });

                     */

                }
            }
        }

        return newPipe;
    }

    /**
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void PipeBlockBreak(BlockBreakEvent event){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), () -> {
            if(
                    event.getBlock().getX() == x
                    && event.getBlock().getY() == y
                    && event.getBlock().getZ() == z
                    && event.getBlock().getWorld() == world
            ){
                remove();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getSourceBlock();
        if(
                (
                        x == block.getX()
                        && y == block.getY()
                        && z == block.getZ()
                        && world == block.getWorld()
                    )
                || (
                        x == event.getBlock().getX()
                        && y ==  event.getBlock().getY()
                        && z ==  event.getBlock().getZ()
                        && world == event.getBlock().getWorld()
                    )
        ) {
            if (
                    event.getBlock().getType() == Material.WATER
                            && event.getSourceBlock().getType() == Material.PLAYER_HEAD
                            && x == block.getX()
                            && y == block.getY()
                            && z == block.getZ()
                            && world == block.getWorld()
                            && !isRemove
            ) {
                event.setCancelled(true);
                block.setType(Material.AIR);
                //block.getWorld().dropItemNaturally(block.getLocation(), pipeItemStackBlock);
                Bukkit.getServer().getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), this::remove);
            } else if (
                    event.getSourceBlock().getType() == Material.WATER
                            && event.getBlock().getType() == Material.PLAYER_HEAD
                            && x == event.getBlock().getX()
                            && y == event.getBlock().getY()
                            && z == event.getBlock().getZ()
                            && world == event.getBlock().getWorld()
                            && !isRemove
            ) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), pipeItemStackBlock);

                Bukkit.getServer().getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), this::remove);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onItemDrop(BlockDropItemEvent event){
        Block block = event.getBlock();
            if(
                    world == block.getWorld()
                    && x == block.getX()
                    && y == block.getY()
                    && z == block.getZ()
                    && !event.getItems().isEmpty()
                    && !isRemove
            ){
                //event.getPlayer().getInventory().addItem(pipeItemStackBlock);
                Item item = event.getItems().getFirst();
                item.setItemStack(pipeItemStackBlock);
                event.getItems().set(0, item);
                //Item item = event.getItems().getFirst();
                //item.setItemStack(pipeItemStackBlock);
                //event.getItems().set(0, item);

            }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for(Block block: event.getBlocks()){
            if(
                    block.getWorld() == world
                    && block.getX() == x
                    && block.getY() == y
                    && block.getZ() == z
                    && !isRemove
            ){
                event.setCancelled(true);
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), pipeItemStackBlock);

                Bukkit.getServer().getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), this::remove);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block: event.getBlocks()){
            if(
                    block.getWorld() == world
                    && block.getX() == x
                    && block.getY() == y
                    && block.getZ() == z
                    && !isRemove
            ){
                event.setCancelled(true);
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), pipeItemStackBlock);

                Bukkit.getServer().getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), this::remove);
                break;
            }
        }
    }
    */

    public void remove(){
        Requests.removePipe(this);
        Magic_pipes.getPlugin().getPipes().remove(this);
        if(update != null) {
            update.cancel();
        }

        isRemove = true;
    }

    /**
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) throws IOException {
        if(this.inventory == event.getInventory()){
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
                Bukkit.getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), () -> {
                    Requests.itemsAdd(this, itemBase64List, slot);
                });
            }else{
                Bukkit.getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), () -> {
                    Requests.removeItem(this);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        if(event.getInventory().getHolder() instanceof Pipe) {
            if (this.inventory == event.getClickedInventory() && event.getSlot() > -999) {
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
            } else if (event.isShiftClick() && this.inventory == event.getInventory()) {
                event.setCancelled(true);
            }
        }
    }
    */

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onChunkUnload(ChunkUnloadEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(Magic_pipes.getPlugin(), () -> {
            int i = 0;
            for(Block cashBLock: cashContainersBlock){
                if(cashBLock.getChunk() == event.getChunk())
                {
                    cashContainersBlock.remove(i);
                    cashBlockStateContainers.remove(i);
                }
                i++;
            }
        });
    }
}
