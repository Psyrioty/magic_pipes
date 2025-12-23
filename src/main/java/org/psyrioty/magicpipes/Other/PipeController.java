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
            for(Pipe pipe: activePipes){
                if(pipe.getIsActive()){
                    pipe.doTask();
                }
            }
        },20L, 20L);
    }

    public void stop(){
        updateTask.cancel();
    }
}
