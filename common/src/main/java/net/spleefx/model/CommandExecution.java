package net.spleefx.model;

import com.google.gson.annotations.SerializedName;
import net.spleefx.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandExecution {

    @SerializedName(value = "Console", alternate = "CONSOLE")
    private List<String> console;

    @SerializedName(value = "Player", alternate = "PLAYER")
    private List<String> player;

    public CommandExecution(List<String> console, List<String> player) {
        this.console = console;
        this.player = player;
    }

    public void execute(@NotNull Player sender, Object... placeholders) {
        console.forEach(p -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.on(p, placeholders, sender)));
        player.forEach(p -> sender.performCommand(Placeholders.on(p, placeholders, sender)));
    }

}
