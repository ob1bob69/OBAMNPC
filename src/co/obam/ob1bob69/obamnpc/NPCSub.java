package co.obam.ob1bob69.obamnpc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.primitives.Ints;

import co.obam.ismooch.obamapi.ObamAPI;

public class NPCSub {

    public static void executeNPCSub(Player player, String packet) {
        player.sendMessage("Got to execute npcSub");

        String[] NPCPass = packet.split(":");
        String uuidNPC = NPCPass[0];
        String nameNPC = NPCPass[1].replace('_', ' ');
        String subClass = NPCPass[2];
        Integer price = Integer.parseInt(NPCPass[3]);
        String postText = NPCPass[4].replace('_', ' ');
        String nameItem = NPCPass[5].replace('_', ' ');

        List<String> action = new ArrayList<String>();
        List<Integer> qty = new ArrayList<Integer>();
        List<String> item = new ArrayList<String>();
        List<String> name = new ArrayList<String>();
        List<String> lore = new ArrayList<String>();
        List<String> command = new ArrayList<String>();

        boolean cont = true;
        double tickets = 0;
        int gC = 0;

        if (price != 0) {
            tickets = ObamAPI.getTickets(player.getUniqueId());
            player.sendMessage("Tickets are " + tickets);
            if (tickets < price) {
                player.sendMessage(ChatColor.GREEN
                        + "[F] "
                        + nameNPC
                        + ChatColor.WHITE
                        + ": I'm sorry my friend, but you do not have enough tickets for this.");
                player.sendMessage(ChatColor.RED
                        + "Your current ticket balance is " + ChatColor.WHITE
                        + tickets + ChatColor.RED + ".");
                cont = false;
            }
        }
        if (cont == true) {
            ObamAPI.openConnection();

            try {
                PreparedStatement ps = ObamAPI.connection
                        .prepareStatement("SELECT * FROM `shopSubClassJava` WHERE SubClass=?;");
                ps.setString(1, subClass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    rs.beforeFirst();

                    while (rs.next()) {

                        action.add(rs.getString("Action"));
                        qty.add(rs.getInt("Qty"));
                        item.add(rs.getString("Item"));
                        name.add(rs.getString("Name"));
                        lore.add(rs.getString("Lore"));
                        command.add(rs.getString("Command"));

                    }

                    Object[] actionA = action.toArray();
                    int[] qtyA = Ints.toArray(qty);
                    Object[] itemA = item.toArray();
                    Object[] nameA = name.toArray();
                    Object[] loreA = lore.toArray();
                    Object[] commandA = command.toArray();

                    ps.close();
                    rs.close();

                    for (int i = 0; i < actionA.length; i++) {
                        if (actionA[i].equals("give")) {
                            gC++;
                        }
                    }

                    player.sendMessage(ChatColor.GREEN + "[F] " + nameNPC
                            + ChatColor.WHITE + ": Give Count is " + gC);

                    if (gC > 0) {
                        boolean invCont = true;
                        HashMap<Integer, ItemStack> iC = new HashMap<Integer, ItemStack>();
                        Inventory inv = player.getInventory();
                        ItemStack blank = new ItemStack(Material.STICK);
                        ItemMeta isMeta = blank.getItemMeta();
                        isMeta.setDisplayName("§aBlank");
                        blank.setItemMeta(isMeta);

                        for (int i2 = 0; i2 < inv.getSize(); i2++) {
                            if (inv.getItem(i2) == null) {
                                iC.put(i2, blank);
                            } else {
                                iC.put(i2, inv.getItem(i2));
                            }
                        }

                        for (int i3 = 0; i3 < actionA.length; i3++) {
                            if (invCont == true) {
                                if (actionA[i3].equals("give")) {
                                    int newQty = qtyA[i3];
                                    player.sendMessage("Qty to give " + newQty);
                                    ItemStack newItem;
                                    for (Map.Entry<Integer, ItemStack> entry : iC
                                            .entrySet()) {


                                        //remove after debug
/*										if (entry.getValue().getItemMeta().getDisplayName() == null) {
											player.sendMessage("Slot " + entry.getKey() + " is " + entry.getValue().getType().name());
										} else {
											player.sendMessage("D Slot " + entry.getKey() + " is " + entry.getValue().getItemMeta().getDisplayName());
										}*/

                                        //	§a§b§k§a§3Timber Axe

                                        if (newQty > 0) {
                                            if (entry.getValue().getItemMeta()
                                                    .hasDisplayName()) {
                                                if (entry.getValue()
                                                        .getItemMeta()
                                                        .getDisplayName()
                                                        .equals(nameA[i3])) {
                                                    int space = entry
                                                            .getValue()
                                                            .getMaxStackSize()
                                                            - entry.getValue()
                                                            .getAmount();
                                                    if (space > 0) {
                                                        if (space >= newQty) {
                                                            newItem = new ItemStack(entry
                                                                    .getValue());
                                                            newItem.setAmount(entry
                                                                    .getValue()
                                                                    .getAmount()
                                                                    + newQty);
                                                            iC.put(entry
                                                                            .getKey(),
                                                                    newItem);
                                                            newQty = 0;
                                                        } else {
                                                            newItem = new ItemStack(entry
                                                                    .getValue());
                                                            newItem.setAmount(entry
                                                                    .getValue()
                                                                    .getMaxStackSize());
                                                            iC.put(entry
                                                                            .getKey(),
                                                                    newItem);
                                                            newQty = newQty
                                                                    - space;
                                                        }
                                                    }
                                                } else if (entry.getValue()
                                                        .getItemMeta()
                                                        .getDisplayName()
                                                        .equals("§aBlank")) {
                                                    ItemStack is;
                                                    String itemC = String
                                                            .valueOf(itemA[i3]);
                                                    String nameC = String
                                                            .valueOf(nameA[i3]);

                                                    if (itemC.contains(":")) {
                                                        String[] cut = itemC
                                                                .split(":");
                                                        String cut1 = cut[0];
                                                        String cut2 = cut[1];
                                                        short id = Short
                                                                .valueOf(cut2);
                                                        is = new ItemStack(
                                                                Material.getMaterial(cut1),
                                                                1, (short) id);
                                                    } else {
                                                        is = new ItemStack(
                                                                Material.getMaterial(itemC),
                                                                1);
                                                    }

                                                    ItemMeta isMeta2 = is
                                                            .getItemMeta();
                                                    isMeta2.setDisplayName(nameC);

                                                    is.setItemMeta(isMeta2);
                                                    int space = is
                                                            .getMaxStackSize();
                                                    if (space >= newQty) {
                                                        is.setAmount(newQty);
                                                        iC.put(entry.getKey(), is);
                                                        newQty = 0;
                                                    } else {
                                                        is.setAmount(space);
                                                        iC.put(entry.getKey(), is);
                                                        newQty = newQty - space;
                                                    }
                                                }
                                            } else if (entry.getValue()
                                                    .getType().name() == itemA[i3]) {
                                                int space = entry.getValue()
                                                        .getMaxStackSize()
                                                        - entry.getValue()
                                                        .getAmount();
                                                if (space > 0) {
                                                    if (space >= newQty) {
                                                        newItem = new ItemStack(entry
                                                                .getValue());
                                                        newItem.setAmount(entry
                                                                .getValue()
                                                                .getAmount()
                                                                + newQty);
                                                        iC.put(entry.getKey(),
                                                                newItem);
                                                        newQty = 0;
                                                    } else {
                                                        newItem = new ItemStack(entry
                                                                .getValue());
                                                        newItem.setAmount(entry
                                                                .getValue()
                                                                .getMaxStackSize());
                                                        iC.put(entry.getKey(),
                                                                newItem);
                                                        newQty = newQty - space;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    player.sendMessage(ChatColor.GREEN + "newQty is " + newQty);
                                    if (newQty > 0) {
                                        player.sendMessage(ChatColor.RED + "should set to false");
                                        invCont = false;
                                    }
                                }
                            }
                        }
                        if (invCont == false) {
                            player.sendMessage(ChatColor.GREEN
                                    + "[F] "
                                    + nameNPC
                                    + ChatColor.WHITE
                                    + ": I'm sorry my friend, but you do not have room to carry all of this.");
                            cont = false;
                        }
                    }

                    if (cont == true) {
                        player.sendMessage("Got through inventory check.");
                        if (price != 0) {
                            player.sendMessage(ChatColor.GREEN + "[F] "
                                    + nameNPC + ChatColor.WHITE
                                    + ": It has a price!");

                            if (price > 0) {
                                double nT = tickets - price;
//								ObamAPI.removeTickets(player.getUniqueId(),
                                //									price, nameNPC, nameItem);
                                player.sendMessage(ChatColor.GREEN
                                        + "You have purchased "
                                        + ChatColor.WHITE + nameItem
                                        + ChatColor.GREEN + " for "
                                        + ChatColor.WHITE + price
                                        + ChatColor.GREEN + " tickets.");
                                player.sendMessage(ChatColor.GREEN
                                        + "Your new balance is "
                                        + ChatColor.WHITE + nT);
                            } else if (price < 0) {
                                double nP = price * -1;
                                double nT = tickets - price;
                                //							ObamAPI.addTickets(player.getUniqueId(), nP,
                                //								nameNPC, nameItem);
                                player.sendMessage(ChatColor.GREEN + "[F] "
                                        + nameNPC + ChatColor.WHITE
                                        + ": Here, have some tickets buddy.");
                                player.sendMessage(ChatColor.GREEN
                                        + "Your new balance is "
                                        + ChatColor.WHITE + nT);
                            }
                        } else {
                            player.sendMessage(ChatColor.GREEN + "[F] "
                                    + nameNPC + ChatColor.WHITE
                                    + ": Must of been free!");
                        }
                        player.sendMessage(ChatColor.GREEN + "[F] " + nameNPC
                                + ChatColor.WHITE
                                + ": Now i should give stuff?");

                        // give stuff
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ObamAPI.closeConnection();
            }

        }
    }
}
