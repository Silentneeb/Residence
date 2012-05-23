/**
 * @author keith
 * @filename MarketExecutor.java
 * @date_created 2012-05-22
 * @last_modified 2012-05-22
 * @description 
 *
 *
 */
package com.Silentneeb.bukkit.residence.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.text.Language;

public class MarketExecutor implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
			String[] args) {
		ResidenceCommandEvent cevent = new ResidenceCommandEvent(cmd
				.getName(), args, sender);
		Bukkit.getPluginManager().callEvent(cevent);
		if (cevent.isCancelled())
			return true;
		
		ConfigManager cmanager = Residence.getConfigManager();
		Language language = Residence.getLanguage();
		PermissionManager gmanager = Residence.getPermissionManager();
		ResidenceManager rmanager = Residence.getResidenceManager();
		TransactionManager tmanager = Residence.getTransactionManager();
		RentManager rentmanager = Residence.getRentManager();
		
		Player player;
		boolean resadmin = false;
		String pname;
		
		if (sender instanceof Player) {
			player = (Player) sender;
			pname = player.getName();
			if (cmd.getName().equals("resadmin")) {
				resadmin = gmanager.isResidenceAdmin(player);
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("NonAdmin"));
					return true;
				}
			}
			if (cmanager.allowAdminsOnly()) {
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("AdminOnly"));
					return true;
				}
			}
		}
		else
			return false;
		
		if (args.length == 0)
			return false;
		if (args[1].equals("list")) {
			if (!cmanager.enableEconomy()) {
				player.sendMessage("§c"
						+ language.getPhrase("MarketDisabled"));
				return true;
			}
			player.sendMessage("§9---"
					+ language.getPhrase("MarketList") + "---");
			tmanager.printForSaleResidences(player);
			if (cmanager.enabledRentSystem()) {
				rentmanager.printRentableResidences(player);
			}
			return true;
		} else if (args[1].equals("autorenew")) {
			if (!cmanager.enableEconomy()) {
				player.sendMessage("§c"
						+ language.getPhrase("MarketDisabled"));
				return true;
			}
			if (args.length != 4) {
				return false;
			}
			boolean value;
			if (args[3].equalsIgnoreCase("true")
					|| args[3].equalsIgnoreCase("t")) {
				value = true;
			} else if (args[3].equalsIgnoreCase("false")
					|| args[3].equalsIgnoreCase("f")) {
				value = false;
			} else {
				player.sendMessage("§c"
						+ language.getPhrase("InvalidBoolean"));
				return true;
			}
			if (rentmanager.isRented(args[2])
					&& rentmanager.getRentingPlayer(args[2])
							.equalsIgnoreCase(pname)) {
				rentmanager.setRentedRepeatable(player, args[2],
						value, resadmin);
			} else if (rentmanager.isForRent(args[2])) {
				rentmanager.setRentRepeatable(player, args[2],
						value, resadmin);
			} else {
				player.sendMessage("§c"
						+ language.getPhrase("RentReleaseInvalid",
								"§e" + args[2] + "§c"));
			}
			return true;
		} else if (args[1].equals("rentable")) {
			if (args.length < 5 || args.length > 6) {
				return false;
			}
			if (!cmanager.enabledRentSystem()) {
				player.sendMessage("§c"
						+ language.getPhrase("RentDisabled"));
				return true;
			}
			int days;
			int cost;
			try {
				cost = Integer.parseInt(args[3]);
			} catch (Exception ex) {
				player.sendMessage("§c"
						+ language.getPhrase("InvalidCost"));
				return true;
			}
			try {
				days = Integer.parseInt(args[4]);
			} catch (Exception ex) {
				player.sendMessage("§c"
						+ language.getPhrase("InvalidDays"));
				return true;
			}
			boolean repeat = false;
			if (args.length == 6) {
				if (args[5].equalsIgnoreCase("t")
						|| args[5].equalsIgnoreCase("true")) {
					repeat = true;
				} else if (!args[5].equalsIgnoreCase("f")
						&& !args[5].equalsIgnoreCase("false")) {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidBoolean"));
					return true;
				}
			}
			rentmanager.setForRent(player, args[2], cost, days,
					repeat, resadmin);
			return true;
		} else if (args[1].equals("rent")) {
			if (args.length < 3 || args.length > 4)
				return false;
			boolean repeat = false;
			if (args.length == 4) {
				if (args[3].equalsIgnoreCase("t")
						|| args[3].equalsIgnoreCase("true")) {
					repeat = true;
				} else if (!args[3].equalsIgnoreCase("f")
						&& !args[3].equalsIgnoreCase("false")) {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidBoolean"));
					return true;
				}
			}
			rentmanager.rent(player, args[2], repeat, resadmin);
			return true;
		} else if (args[1].equals("release")) {
			if (args.length != 3)
				return false;
			if (rentmanager.isRented(args[2])) {
				rentmanager.removeFromForRent(player, args[2],
						resadmin);
			} else {
				rentmanager.unrent(player, args[2], resadmin);
			}
			return true;
		} else if (args.length == 2) {
			if (args[1].equals("info")) {
				String areaname = rmanager.getNameByLoc(player
						.getLocation());
				tmanager.viewSaleInfo(areaname, player);
				if (cmanager.enabledRentSystem()
						&& rentmanager.isForRent(areaname)) {
					rentmanager.printRentInfo(player, areaname);
				}
				return true;
			}
		} else if (args.length == 3) {
			if (args[1].equals("buy")) {
				tmanager.buyPlot(args[2], player, resadmin);
				return true;
			} else if (args[1].equals("info")) {
				tmanager.viewSaleInfo(args[2], player);
				if (cmanager.enabledRentSystem()
						&& rentmanager.isForRent(args[2])) {
					rentmanager.printRentInfo(player, args[2]);
				}
				return true;
			} else if (args[1].equals("unsell")) {
				tmanager.removeFromSale(player, args[2], resadmin);
				return true;
			}
		} else if (args.length == 4) {
			if (args[1].equals("sell")) {
				int amount;
				try {
					amount = Integer.parseInt(args[3]);
				} catch (Exception ex) {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidAmount"));
					return true;
				}
				tmanager.putForSale(args[2], player, amount,
						resadmin);
				return true;
			}
		}
		return false;
	}

}
