package co.obam.ob1bob69.obamnpc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import co.obam.ismooch.obamapi.ObamAPI;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public class OBAMNPC extends JavaPlugin implements Listener {

    public static HashMap<UUID, Location> NPCList = new HashMap<UUID, Location>();
    public static HashMap<Location, UUID> NPCList2 = new HashMap<Location, UUID>();
    public static HashMap<UUID, Timestamp> NPCTimeCheck = new HashMap<UUID, Timestamp>();
    public static HashMap<UUID, HashMap<Integer, String>> NPCCommands = new HashMap<UUID, HashMap<Integer, String>>();
    ConsoleCommandSender console = getServer().getConsoleSender();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        cmdRouter.getPlugin(this);
        ObamAPI.openConnection();
        try {
            List<String> uuid = new ArrayList<String>();
            List<Integer> x = new ArrayList<Integer>();
            List<Integer> y = new ArrayList<Integer>();
            List<Integer> z = new ArrayList<Integer>();
            List<String> world = new ArrayList<String>();

            PreparedStatement ps = ObamAPI.connection
                    .prepareStatement("SELECT * FROM `NPCJava`;");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.beforeFirst();
                int c = 0;
                while (rs.next()) {
                    uuid.add(rs.getString("UUID"));
                    x.add(rs.getInt("xCord"));
                    y.add(rs.getInt("yCord"));
                    z.add(rs.getInt("zCord"));
                    world.add(rs.getString("world"));
                    c++;
                }

                Object[] uuidC = uuid.toArray();
                int[] xC = Ints.toArray(x);
                int[] yC = Ints.toArray(y);
                int[] zC = Ints.toArray(z);
                Object[] worldC = world.toArray();

                for (int i = 0; i < c; i++) {

                    UUID uuidF = UUID.fromString(String.valueOf(uuidC[i]));
                    World worldF = getServer().getWorld(
                            String.valueOf(worldC[i]));
                    Location locF = new Location(worldF, xC[i], yC[i], zC[i]);

                    NPCList.put(uuidF, locF);
                    NPCList2.put(locF, uuidF);

                }
                console.sendMessage(ChatColor.GREEN
                        + "NPCList is constructed with " + ChatColor.WHITE + c
                        + ChatColor.GREEN + " entries. OBAMNPC");

            } else {

                console.sendMessage(ChatColor.RED
                        + "Unable to pull NPCList from Database. OBAMNPC");
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ObamAPI.closeConnection();
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {

                for (World w : getServer().getWorlds()) {
                    for (Entity e : w.getEntities()) {
                        if (NPCList.containsKey(e.getUniqueId())) {
                            Location loc = NPCList.get(e.getUniqueId());
                            e.teleport(loc);
                            ((LivingEntity) e).addPotionEffect(
                                    PotionEffectType.SLOW.createEffect(
                                            99999999, 1000), true);
                            //remove when live
                        } else if (e instanceof Player) {
                            e.sendMessage(ChatColor.GREEN
                                    + "NPC teleport loop.");
                        }
                    }

                }
            }
        }, 60L, 12000L);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label,
                             String[] args) {

        if (cmd.getName().equalsIgnoreCase("forceport")) {
            Player player = (Player) sender;
            for (World w : getServer().getWorlds()) {
                for (Entity e : w.getEntities()) {
                    if (NPCList.containsKey(e.getUniqueId())) {
                        Location loc = NPCList.get(e.getUniqueId());
                        e.teleport(loc);
                        player.sendMessage(ChatColor.GREEN + "Teleporting "
                                + ChatColor.WHITE + e.getCustomName());
                    } else if (e instanceof Player) {
                        e.sendMessage(ChatColor.GREEN + "NPC teleport loop.");
                    }
                }
            }
        }


        if (cmd.getName().equalsIgnoreCase("testitem")) {
            Player player = (Player) sender;
            ItemStack is = player.getItemInHand();
            int check = is.getTypeId();
            player.sendMessage(String.valueOf(Material.getMaterial(check))
                    + ":" + String.valueOf(is.getData()));
        }


        if (cmd.getName().equalsIgnoreCase("npc")) {
            Player player = (Player) sender;
            if (!player.hasPermission("obam.smod")) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to do this.");
                return true;
            } else if (args.length < 2) {
                if (args[0].equalsIgnoreCase("reload")) {
                    NPCList.clear();
                    NPCList2.clear();
                    ObamAPI.openConnection();
                    try {
                        List<String> uuid = new ArrayList<String>();
                        List<Integer> x = new ArrayList<Integer>();
                        List<Integer> y = new ArrayList<Integer>();
                        List<Integer> z = new ArrayList<Integer>();
                        List<String> world = new ArrayList<String>();

                        PreparedStatement ps = ObamAPI.connection
                                .prepareStatement("SELECT * FROM `NPCJava`;");
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            rs.beforeFirst();
                            int c = 0;
                            while (rs.next()) {
                                uuid.add(rs.getString("UUID"));
                                x.add(rs.getInt("xCord"));
                                y.add(rs.getInt("yCord"));
                                z.add(rs.getInt("zCord"));
                                world.add(rs.getString("world"));
                                c++;
                            }

                            Object[] uuidC = uuid.toArray();
                            int[] xC = Ints.toArray(x);
                            int[] yC = Ints.toArray(y);
                            int[] zC = Ints.toArray(z);
                            Object[] worldC = world.toArray();

                            for (int i = 0; i < c; i++) {

                                UUID uuidF = UUID.fromString(String
                                        .valueOf(uuidC[i]));
                                World worldF = getServer().getWorld(
                                        String.valueOf(worldC[i]));
                                Location locF = new Location(worldF, xC[i],
                                        yC[i], zC[i]);

                                NPCList.put(uuidF, locF);
                                NPCList2.put(locF, uuidF);

                            }
                            console.sendMessage(ChatColor.GREEN
                                    + "NPCList is constructed with "
                                    + ChatColor.WHITE + c + ChatColor.GREEN
                                    + " entries. OBAMNPC");

                        } else {

                            console.sendMessage(ChatColor.RED
                                    + "Unable to pull NPCList from Database. OBAMNPC");
                        }
                        rs.close();
                        ps.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        ObamAPI.closeConnection();
                    }
                    return true;
                }
                player.sendMessage(ChatColor.RED + "[type] [name]");
                return true;
            } else if (!args[0].equals("farmer") && !args[0].equals("priest")
                    && !args[0].equals("butcher")
                    && !args[0].equals("blacksmith")
                    && !args[0].equals("librarian")) {
                player.sendMessage(ChatColor.RED
                        + "That is not a valid Villager Type.");
                return true;
            } else {
                Location loc = player.getLocation();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();
                String world = loc.getWorld().getName();
                player.sendMessage("x-" + String.valueOf(x) + " y-"
                        + String.valueOf(y) + " z-" + String.valueOf(z)
                        + " of world " + String.valueOf(world));
                String name1 = args[1].replace('&', '§');
                String name = name1.replace('_', ' ');
                Villager npc = (Villager) loc.getWorld().spawn(loc,
                        Villager.class);
                npc.setCustomName(name);
                if (args[0].equalsIgnoreCase("farmer")) {
                    npc.setProfession(Profession.FARMER);
                } else if (args[0].equalsIgnoreCase("priest")) {
                    npc.setProfession(Profession.PRIEST);
                } else if (args[0].equalsIgnoreCase("butcher")) {
                    npc.setProfession(Profession.BUTCHER);
                } else if (args[0].equalsIgnoreCase("blacksmith")) {
                    npc.setProfession(Profession.BLACKSMITH);
                } else if (args[0].equalsIgnoreCase("librarian")) {
                    npc.setProfession(Profession.LIBRARIAN);
                }
                npc.addPotionEffect(
                        PotionEffectType.SLOW.createEffect(99999999, 1000),
                        true);
                UUID u = npc.getUniqueId();
                String uuid = u.toString();
                NPCList.put(u, loc);
                NPCList2.put(loc, u);
                ObamAPI.openConnection();
                try {
                    PreparedStatement ps = ObamAPI.connection
                            .prepareStatement("INSERT INTO NPCJava (UUID , name , type , xCord , yCord , zCord , world , shopPre ,shopPost , shopCommand , shopClass , questPre ,questPost , questCommand , questClass) VALUES (? , ? , ? , ? , ? , ? , ? , 'Hello' , 'Good bye' , 'NONE' , 'NONE' , 'Hello' , 'Good bye' , 'NONE' , 'NONE');");
                    ps.setString(1, uuid);
                    ps.setString(2, name);
                    ps.setString(3, args[0]);
                    ps.setInt(4, x);
                    ps.setInt(5, y);
                    ps.setInt(6, z);
                    ps.setString(7, world);

                    ps.executeUpdate();
                    ps.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ObamAPI.closeConnection();
                }
                return true;
            }
        }


        if (cmd.getName().equalsIgnoreCase("invtest")) {
            Player player = (Player) sender;
            Inventory inv = player.getInventory();
            HashMap<Integer, ItemStack> iC = new HashMap<Integer, ItemStack>();

            ItemStack air = new ItemStack(Material.STICK);
            ItemMeta isMeta = air.getItemMeta();
            isMeta.setDisplayName("§aBlank");
            air.setItemMeta(isMeta);

            for(int i = 0 ; i < inv.getSize() ; i++) {
                if (inv.getItem(i) == null) {
                    iC.put(i, air);
                } else {
                    iC.put(i, inv.getItem(i));
                }
            }
            for (Map.Entry<Integer, ItemStack> entry : iC.entrySet()) {
                if (entry.getValue().getItemMeta().getDisplayName() == null) {
                    player.sendMessage("Slot " + entry.getKey() + " is " + entry.getValue().getType().name());
                } else {
                    player.sendMessage("D Slot " + entry.getKey() + " is " + entry.getValue().getItemMeta().getDisplayName());
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("listtest")) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.GREEN + "List Test starting!");
            int c = 0;
            for (Map.Entry<UUID, HashMap<Integer, String>> entry : NPCCommands.entrySet()) {
                player.sendMessage("Looping entry set " + c);
                c++;
                HashMap<Integer, String> commands = NPCCommands.get(entry.getKey());
                for (Map.Entry<Integer, String> entry2 : commands.entrySet()) {
                    player.sendMessage("Slot is " + entry2.getKey());
                    player.sendMessage("Command is " + entry2.getValue());
                }
            }
            player.sendMessage(ChatColor.GREEN + "Done");
            return true;
        }


        if (cmd.getName().equalsIgnoreCase("listrun")) {
            for (World w : getServer().getWorlds()) {
                for (Entity e : w.getEntities()) {
                    if (NPCList.containsKey(e.getUniqueId())) {
                        e.remove();

                        // NPCList.remove(e.getUniqueId());

                    }
                }
            }
        }

        // remove later

        if (cmd.getName().equalsIgnoreCase("giveStick")) {
            Player player = (Player) sender;
            Inventory inv = player.getInventory();
            if (args[0].equalsIgnoreCase("magic")) {
                ItemStack item = new ItemStack(Material.STICK);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Magic Stick");
                itemMeta.setLore(Arrays.asList(
                        (ChatColor.YELLOW + "Use this amazing stick"),
                        (ChatColor.YELLOW + "to " + ChatColor.RED + "copy "
                                + ChatColor.YELLOW + "all the"),
                        (ChatColor.YELLOW + "things!")));
                item.setItemMeta(itemMeta);
                inv.addItem(item);
            }
            return true;
        }




        if (cmd.getName().equalsIgnoreCase("npcsub")) {
            Player player = (Player) sender;
            player.sendMessage("It got to npcSub Command.");
            return true;
        }

        // end of command
        return true;

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (NPCCommands.containsKey(event.getPlayer().getUniqueId())) {
            NPCCommands.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        if (NPCCommands.containsKey(uuid)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            HashMap<Integer, String> commands = NPCCommands.get(uuid);
            if (commands.containsKey(slot)) {
                player.closeInventory();
                String[] split = commands.get(slot).split(" ");
                cmdRouter.route(player, split[0], split[1]);
                NPCCommands.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity a = event.getDamager();
        UUID u = event.getEntity().getUniqueId();

        if (NPCList.containsKey(u)) {
            event.setCancelled(true);
            if (a instanceof Player) {
                a.sendMessage(ChatColor.RED + "You can not hurt NPCs.");
                console.sendMessage(ChatColor.GREEN + "UUID is " + u
                        + "    OBAMNPC");
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            if (NPCList.containsKey(event.getRightClicked().getUniqueId())) {
                event.setCancelled(true);
                Entity e = event.getRightClicked();
                UUID u = e.getUniqueId();
                String uuid = u.toString();
                Player p = event.getPlayer();
                long ts = System.currentTimeMillis();
                Timestamp now = new Timestamp(ts);
                boolean ok = false;
                if (NPCTimeCheck.containsKey(p.getUniqueId())) {
                    if (now.after(NPCTimeCheck.get(p.getUniqueId()))) {
                        ok = true;
                        NPCTimeCheck.remove(p.getUniqueId());
                    } else {
                        p.sendMessage(e.getCustomName() + ChatColor.WHITE
                                + ": Stop it, that tickles!");
                        return;
                    }
                } else {
                    ok = true;
                }
                if (ok == true) {
                    Timestamp later = new Timestamp(now.getTime() + (3 * 1000L));
                    later.setNanos(now.getNanos());
                    NPCTimeCheck.put(p.getUniqueId(), later);

                    String shopPre = new String();
                    String shopPost = new String();
                    String shopCommand = new String();
                    String shopClass = new String();

                    ObamAPI.openConnection();

                    try {
                        PreparedStatement ps = ObamAPI.connection
                                .prepareStatement("SELECT * FROM `NPCJava` WHERE UUID=?;");

                        ps.setString(1, uuid);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            shopPre = rs.getString("shopPre");
                            shopPost = rs.getString("shopPost");
                            shopCommand = rs.getString("shopCommand");
                            shopClass = rs.getString("shopClass");

                        }
                        if (shopCommand.equals("NONE")) {
                            p.sendMessage(e.getCustomName() + ChatColor.WHITE
                                    + ": " + shopPre);
                        } else {
                            p.sendMessage(e.getCustomName() + ChatColor.WHITE
                                    + ": " + shopPre);
                            String nameN = e.getCustomName().replace(' ', '_');
                            shopPost = shopPost.replace(' ', '_');
                            String passInfo = new String(uuid + ":" + nameN + ":" + shopClass + ":" + shopPost);
                            cmdRouter.route(p, shopCommand, passInfo);
                        }
                        ps.close();
                        rs.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } finally {
                        ObamAPI.closeConnection();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void missingNPC(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock().getType() == Material.WOOD_BUTTON) {
                console.sendMessage(ChatColor.GREEN
                        + "Clicked a button. OBAMNPC");
                Location loc = event.getClickedBlock().getLocation();
                int r = 1;
                int cx = loc.getBlockX();
                int cy = loc.getBlockY();
                int cz = loc.getBlockZ();
                for (int x = cx - r; x <= cx + r; x++) {
                    for (int z = cz - r; z <= cz + r; z++) {
                        for (int y = cy - r; y < cy; y++) {
                            Location nLoc = new Location(loc.getWorld(), x, y,
                                    z);
                            console.sendMessage(ChatColor.GREEN + "A "
                                    + nLoc.getBlock().getType() + "    OBAMNPC");
                            if (nLoc.getBlock().getType()
                                    .equals(Material.WALL_SIGN)) {
                                console.sendMessage(ChatColor.GREEN
                                        + "Found a Sign. OBAMNPC");
                                Sign sign = (Sign) nLoc.getBlock().getState();
                                if (sign.getLine(0).equals("Press")
                                        && sign.getLine(1).equals("Button")
                                        && sign.getLine(2).equals("For")
                                        && sign.getLine(3).equals("Service")) {
                                    console.sendMessage(ChatColor.GREEN
                                            + "Sign meets reqs. OBAMNPC");
                                    int r2 = 2;
                                    int cx2 = nLoc.getBlockX();
                                    int cy2 = nLoc.getBlockY();
                                    int cz2 = nLoc.getBlockZ();
                                    for (int x2 = cx2 - r2; x2 <= cx2 + r2; x2++) {
                                        for (int z2 = cz2 - r2; z2 <= cz2 + r2; z2++) {
                                            for (int y2 = cy2 - r2; y2 <= cy2
                                                    + r2; y2++) {
                                                Location nLoc2 = new Location(
                                                        loc.getWorld(), x2, y2,
                                                        z2);
                                                // console.sendMessage(ChatColor.GREEN
                                                // + "Loc is " + nLoc2 +
                                                // "    OBAMNPC");

                                                if (NPCList2.containsKey(nLoc2)) {
                                                    console.sendMessage(ChatColor.GREEN
                                                            + "Found key "
                                                            + NPCList2
                                                            .get(nLoc2)
                                                            + " OBAMNPC");
                                                    UUID uuid = NPCList2
                                                            .get(nLoc2);

                                                    World world = event
                                                            .getPlayer()
                                                            .getWorld();
                                                    boolean found = false;
                                                    for (Entity e : world
                                                            .getEntities()) {
                                                        if (e instanceof Villager) {
                                                            console.sendMessage(ChatColor.LIGHT_PURPLE
                                                                    + "E is "
                                                                    + e.getUniqueId()
                                                                    + "    OBAMNPC");
                                                            if (e.getUniqueId()
                                                                    .equals(uuid)) {
                                                                console.sendMessage(ChatColor.GREEN
                                                                        + "Found the NPC. OBAMNPC");
                                                                found = true;
                                                            }
                                                        }
                                                    }
                                                    if (found == false) {
                                                        console.sendMessage(ChatColor.RED
                                                                + "Didn't find the NPC. OBAMNPC");
                                                        ObamAPI.openConnection();
                                                        String u = uuid
                                                                .toString();
                                                        try {
                                                            PreparedStatement ps = ObamAPI.connection
                                                                    .prepareStatement("SELECT name , type FROM `NPCJava` WHERE UUID = ?;");
                                                            ps.setString(1, u);
                                                            ResultSet rs = ps
                                                                    .executeQuery();
                                                            if (rs.next()) {

                                                                String name = rs
                                                                        .getString("name");
                                                                String type = rs
                                                                        .getString("type");

                                                                Villager npc = (Villager) nLoc2
                                                                        .getWorld()
                                                                        .spawn(nLoc2,
                                                                                Villager.class);
                                                                npc.setCustomName(name);
                                                                if (type.equalsIgnoreCase("farmer")) {
                                                                    npc.setProfession(Profession.FARMER);
                                                                } else if (type
                                                                        .equalsIgnoreCase("priest")) {
                                                                    npc.setProfession(Profession.PRIEST);
                                                                } else if (type
                                                                        .equalsIgnoreCase("butcher")) {
                                                                    npc.setProfession(Profession.BUTCHER);
                                                                } else if (type
                                                                        .equalsIgnoreCase("blacksmith")) {
                                                                    npc.setProfession(Profession.BLACKSMITH);
                                                                } else if (type
                                                                        .equalsIgnoreCase("librarian")) {
                                                                    npc.setProfession(Profession.LIBRARIAN);
                                                                }
                                                                npc.addPotionEffect(
                                                                        PotionEffectType.SLOW
                                                                                .createEffect(
                                                                                        99999999,
                                                                                        1000),
                                                                        true);
                                                                UUID u2 = npc
                                                                        .getUniqueId();
                                                                String uuid2 = u2
                                                                        .toString();
                                                                NPCList.remove(uuid);
                                                                NPCList2.remove(nLoc2);
                                                                NPCList.put(u2,
                                                                        nLoc2);
                                                                NPCList2.put(
                                                                        nLoc2,
                                                                        u2);
                                                                try {
                                                                    PreparedStatement ps2 = ObamAPI.connection
                                                                            .prepareStatement("UPDATE `NPCJava` SET UUID=? WHERE UUID=?;");

                                                                    ps2.setString(
                                                                            1,
                                                                            uuid2);
                                                                    ps2.setString(
                                                                            2,
                                                                            u);

                                                                    ps2.executeUpdate();
                                                                    ps2.close();

                                                                } catch (SQLException e2) {
                                                                    e2.printStackTrace();
                                                                    console.sendMessage(ChatColor.RED
                                                                            + "2nd Query - OBAMNPC");
                                                                }

                                                            }
                                                            ps.close();
                                                            rs.close();

                                                        } catch (SQLException e) {
                                                            e.printStackTrace();
                                                            console.sendMessage(ChatColor.RED
                                                                    + "1st Query - OBAMNPC");

                                                        } finally {
                                                            ObamAPI.closeConnection();
                                                            console.sendMessage(ChatColor.GREEN
                                                                    + "Should have respawned. OBAMNPC");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void clearFlags(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        NPCTimeCheck.remove(uuid);
        NPCCommands.remove(uuid);
    }

}
