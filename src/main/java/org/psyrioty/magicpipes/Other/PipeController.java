package org.psyrioty.magicpipes.Other;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.psyrioty.magicpipes.Objects.Pipe;
import org.psyrioty.magicpipes.magicpipes;

import java.util.List;

public class PipeController {
    private BukkitTask updateTask;
    private List<Pipe> activePipes;

    public PipeController(){
        activePipes = magicpipes.getPlugin().getActivePipe();
        update();
    }

    private void update(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(magicpipes.getPlugin(), () -> {
            if(!magicpipes.getPlugin().isStartPipes()){
                return;
            }
            //Bukkit.getLogger().info("Активные трубы: " + magicpipes.getPlugin().getActivePipe().size() + "/" + magicpipes.getPlugin().getPipes().size());
            //Bukkit.getLogger().info("Активные контейнеры: " + magicpipes.getPlugin().getActivePipeContainers().size() + "/" + magicpipes.getPlugin().getPipeContainers().size());
            for(Pipe pipe: activePipes){
                if(pipe.getIsActive()){
                    pipe.doTask();
                }
            }
        },100L, 100L);
    }

    public void stop(){
        updateTask.cancel();
    }
}
