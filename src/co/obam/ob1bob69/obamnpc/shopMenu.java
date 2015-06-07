package co.obam.ob1bob69.obamnpc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.obam.ismooch.obamapi.ObamAPI;

import com.google.common.primitives.Ints;

public class shopMenu {

    public static void executeShopMenu(Player player, String packet) {
        String[] NPCPass = packet.split(":");
        String uuidNPC = NPCPass[0];
        String nameNPC = NPCPass[1].replace('_', ' ');
        String shopClass = NPCPass[2];



        List<Integer> slot = new ArrayList<Integer>();
        List<Integer> qty = new ArrayList<Integer>();
        List<String> item = new ArrayList<String>();
        List<String> name = new ArrayList<String>();
        List<String> lore = new ArrayList<String>();
        List<String> command = new ArrayList<String>();
        List<String> subClass = new ArrayList<String>();
        List<Integer> price = new ArrayList<Integer>();
        List<String> postText = new ArrayList<String>();

        int max = 1;

        ObamAPI.openConnection();

        try {
            PreparedStatement ps = ObamAPI.connection
                    .prepareStatement("SELECT * FROM `shopClassJava` WHERE Class=?;");
            ps.setString(1, shopClass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.beforeFirst();

//						player.sendMessage(ChatColor.GREEN
//								+ "In the first if statement");
                int c = 0;
                String commandString = new String();
                HashMap<Integer, String> commandHash = new HashMap<Integer, String>();

                while (rs.next()) {
//							player.sendMessage(ChatColor.GREEN + "In a loop"
//									+ c);


                    slot.add(rs.getInt("Slot"));
                    qty.add(rs.getInt("Qty"));
                    item.add(rs.getString("Item"));
                    name.add(rs.getString("Name"));
                    lore.add(rs.getString("Lore"));
                    command.add(rs.getString("Command"));
                    subClass.add(rs.getString("SubClass"));
                    price.add(rs.getInt("Price"));
                    postText.add(rs.getString("postText"));

                    c++;
                }
/*						player.sendMessage(ChatColor.GREEN
							+ "Counter after loop is  " + ChatColor.WHITE
							+ c);
					player.sendMessage(ChatColor.GREEN
							+ "Max before loop is  " + ChatColor.WHITE
							+ max);*/
                int[] slotA = Ints.toArray(slot);
                int[] qtyA = Ints.toArray(qty);
                Object[] itemA = item.toArray();
                Object[] nameA = name.toArray();
                Object[] loreA = lore.toArray();
                Object[] commandA = command.toArray();
                Object[] subClassA = subClass.toArray();
                int[] priceA = Ints.toArray(price);
                Object[] postTextA = postText.toArray();

/*						player.sendMessage(ChatColor.GREEN + "Slot is  "
							+ ChatColor.WHITE + slotA[2]);
					player.sendMessage(ChatColor.GREEN + "Qty is  "
							+ ChatColor.WHITE + qtyA[2]);
					player.sendMessage(ChatColor.GREEN + "Item is  "
							+ ChatColor.WHITE + itemA[2]);
					player.sendMessage(ChatColor.GREEN + "Name is  "
							+ ChatColor.WHITE + nameA[2]);
					player.sendMessage(ChatColor.GREEN + "Lore is  "
							+ ChatColor.WHITE + loreA[2]);
					player.sendMessage(ChatColor.GREEN + "Command is  "
							+ ChatColor.WHITE + commandA[2]);
					player.sendMessage(ChatColor.GREEN + "subClass is  "
							+ ChatColor.WHITE + subClassA[2]);
					player.sendMessage(ChatColor.GREEN + "Price is  "
							+ ChatColor.WHITE + priceA[2]);
					player.sendMessage(ChatColor.GREEN + "postText is  "
							+ ChatColor.WHITE + postTextA[2]);*/

                for (int i = 1; i < slotA.length; i++) {
                    if (slotA[i] > max) {
                        max = i;
                    }
                }
//						player.sendMessage(ChatColor.GREEN
//								+ "Max after loop is  " + ChatColor.WHITE
//								+ max);

                int rows = (int) Math.ceil(max / 9.0);
//						player.sendMessage(ChatColor.GREEN + "The Number of rows is "
//								+ ChatColor.WHITE + rows);
                int last = rows * 9;

//						player.sendMessage("Last is" + last);

                ps.close();
                rs.close();

                Inventory inv = Bukkit.createInventory(null, last, nameNPC);
                ItemStack is;
                for (int i = 0; i < c; i++) {

                    String itemC = String.valueOf(itemA[i]);
                    String nameC = String.valueOf(nameA[i]);
                    String loreC = String.valueOf(loreA[i]);
                    String commandC = String.valueOf(commandA[i]);
                    String subClassC = String.valueOf(subClassA[i]);
                    String postTextC = String.valueOf(postTextA[i]);
                    String priceC = String.valueOf(priceA[i]);


                    if (itemC.contains(":")) {
                        String[] cut = itemC.split(":");
                        String cut1 = cut[0];
                        String cut2 = cut[1];
                        short id = Short.valueOf(cut2);
//								player.sendMessage(String.valueOf(id));
                        is = new ItemStack(Material.getMaterial(cut1), qtyA[i],
                                (short) id);
                    } else {
                        is = new ItemStack(Material.getMaterial(itemC),qtyA[i]);
                    }

                    ItemMeta isMeta = is.getItemMeta();
                    isMeta.setDisplayName(nameC);
                    loreC = loreC.replace('|', '<');
                    String[] loreCut = loreC.split("<<");

                    ArrayList<String> lf = new ArrayList<String>();

                    for (int i2 = 0; i2 < loreCut.length; i2++){
//								player.sendMessage(ChatColor.GREEN + "r215" + loreCut[i2]);
                        lf.add(loreCut[i2]);
                    }
//							player.sendMessage(String.valueOf(i));
//							player.sendMessage(String.valueOf(slotA[i]));
                    isMeta.setLore(lf);

                    is.setItemMeta(isMeta);

                    inv.setItem(slotA[i], is);

                    if (postTextC.equals("NONE")) {
                        postTextC = NPCPass[3];
                    } else {
                        postTextC = postTextC.replace(' ', '_');
                    }
                    nameC = nameC.replace(' ', '_');
                    commandString = (commandC + " " + uuidNPC + ":" + NPCPass[1] + ":" + subClassC + ":" + priceC + ":" + postTextC + ":" + nameC);
                    commandHash.put(slotA[i],commandString);

                }

                OBAMNPC.NPCCommands.put(player.getUniqueId(), commandHash);

                player.openInventory(inv);


            }
        } catch (SQLException e) {
            player.sendMessage("OBAMNPC.shopMenu error 1. Please report with /bug.");
            e.printStackTrace();
        } finally {
            ObamAPI.closeConnection();
        }


    }





}
