package org.psyrioty.magicpipes.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.psyrioty.magicpipes.Objects.Pipe;
import org.psyrioty.magicpipes.Objects.PipeContainer;
import org.psyrioty.magicpipes.magicpipes;

public class debug implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Bukkit.getScheduler().runTaskAsynchronously(magicpipes.getPlugin(), () -> {
            for(Pipe pipe: magicpipes.getPlugin().getPipes()){
                if(
                        pipe.getWorld().getName().equals(strings[0])
                        && (pipe.getX() + "").equals(strings[1])
                        && (pipe.getY() + "").equals(strings[2])
                        && (pipe.getZ() + "").equals(strings[3])
                ){
                    commandSender.sendMessage("Родительские:");
                    for(Pipe includePipe: pipe.getParentPipes()){
                        commandSender.sendMessage(includePipe.getDbId() + " X: " + includePipe.getX() + " Y: " + includePipe.getY() + " Z: " + includePipe.getZ());
                    }
                    commandSender.sendMessage("Дочерние:");
                    for(Pipe includePipe: pipe.getPipes()){
                        commandSender.sendMessage(includePipe.getDbId() + " X: " + includePipe.getX() + " Y: " + includePipe.getY() + " Z: " + includePipe.getZ());
                    }
                    commandSender.sendMessage("Контейнер взять:");
                    for(PipeContainer includePipe: pipe.getTakePipeContainers()){
                        commandSender.sendMessage(" X: " + includePipe.getX() + " Y: " + includePipe.getY() + " Z: " + includePipe.getZ());
                    }

                    commandSender.sendMessage("Контейнер положить:");
                    for(PipeContainer includePipe: pipe.getPipeContainers()){
                        commandSender.sendMessage(" X: " + includePipe.getX() + " Y: " + includePipe.getY() + " Z: " + includePipe.getZ());
                    }
                }
            }
        });
        return true;
    }
}
