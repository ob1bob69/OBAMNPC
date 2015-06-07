package co.obam.ob1bob69.obamnpc;

import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class cmdRouter {
    public static Plugin plugin;
    public static ConsoleCommandSender console;

    public static void getPlugin(Plugin p) {
        console = p.getServer().getConsoleSender();
    }


    public static void route(Player player, String command, String packet) {
        if (player instanceof Player) {

            if (command.equals("shopMenu")) {
                shopMenu.executeShopMenu(player, packet);
            } else if (command.equals("npcsub")) {
                NPCSub.executeNPCSub(player, packet);
            } else {
                player.performCommand("command packet");
                String poop = "test";
            }


        } else {
            console.sendMessage(ChatColor.RED + "Only players can do this.");
        }
    }


}
