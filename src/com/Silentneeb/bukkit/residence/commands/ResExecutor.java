/**
 * @author keith
 * @filename ResExecutor.java
 * @date_created 2012-05-16
 * @last_modified 2012-05-16
 * @description 
 *
 *TODO break market out into its own command
 *
 */
package com.Silentneeb.bukkit.residence.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.spout.ResidenceSpout;
import com.bekvon.bukkit.residence.spout.ResidenceSpoutListener;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;

public class ResExecutor implements CommandExecutor {

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
		
		//TODO
		HelpEntry helppages = Residence.getHelppages();
		ConfigManager cmanager = Residence.getConfigManager();
		Language language = Residence.getLanguage();
		PermissionManager gmanager = Residence.getPermissionManager();
		SelectionManager smanager = Residence.getSelectionManager();
		ResidenceManager rmanager = Residence.getResidenceManager();
		ResidencePlayerListener plistener = Residence.getPlayerListener();
		ResidenceSpoutListener slistener = Residence.getSlistener();
		TransactionManager tmanager = Residence.getTransactionManager();
		PermissionListManager pmanager = Residence.getPmanager();
		LeaseManager leasemanager = Residence.getLeaseManager();
		RentManager rentmanager = Residence.getRentManager();
		Map<String, String> deleteConfirm = new HashMap<String, String>();
		
		Residence resPlugin = (Residence)Bukkit.getPluginManager().getPlugin("Residence");

		if ((args.length > 0 && args[args.length - 1].equalsIgnoreCase("?"))
				|| (args.length > 1 && args[args.length - 2].equals("?"))) {
			if (helppages != null) {
				String helppath = "res";
				for (int i = 0; i < args.length; i++) {
					if (args[i].equalsIgnoreCase("?")) {
						break;
					}
					helppath = helppath + "." + args[i];
				}
				int page = 1;
				if (!args[args.length - 1].equalsIgnoreCase("?")) {
					try {
						page = Integer.parseInt(args[args.length - 1]);
					} catch (Exception ex) {
						sender.sendMessage("§c"
								+ language.getPhrase("InvalidHelp"));
					}
				}
				if (helppages.containesEntry(helppath)) {
					helppages.printHelp(sender, page, helppath);
					return true;
				}
			}
		}
		int page = 1;
		try {
			if (args.length > 0)
				page = Integer.parseInt(args[args.length - 1]);
		} catch (Exception ex) {
		}
		if (sender instanceof Player) {
			Player player = (Player) sender;
			PermissionGroup group = Residence.getPermissionManager()
					.getGroup(player);
			String pname = player.getName();
			boolean resadmin = false;
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
			if (args.length == 0)
				return false;
			if (args.length == 0) {
				args = new String[1];
				args[0] = "?";
			}
			if (args[0].equals("select")) {
				if (!group.selectCommandAccess() && !resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("SelectDiabled"));
					return true;
				}
				if (!group.canCreateResidences()
						&& group.getMaxSubzoneDepth() <= 0 && !resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("SelectDiabled"));
					return true;
				}
				if (args.length == 2) {
					if (args[1].equals("size") || args[1].equals("cost")) {
						if (smanager.hasPlacedBoth(pname)) {
							try {
								smanager.showSelectionInfo(player);
								return true;
							} catch (Exception ex) {
								Logger.getLogger(Residence.class.getName())
										.log(Level.SEVERE, null, ex);
								return true;
							}
						}
					} else if (args[1].equals("vert")) {
						smanager.vert(player, resadmin);
						return true;
					} else if (args[1].equals("sky")) {
						smanager.sky(player, resadmin);
						return true;
					} else if (args[1].equals("bedrock")) {
						smanager.bedrock(player, resadmin);
						return true;
					} else if (args[1].equals("coords")) {
						Location playerLoc1 = smanager.getPlayerLoc1(pname);
						if (playerLoc1 != null) {
							player.sendMessage("§a"
									+ language
											.getPhrase("Primary.Selection")
									+ ":§b (" + playerLoc1.getBlockX()
									+ ", " + playerLoc1.getBlockY() + ", "
									+ playerLoc1.getBlockZ() + ")");
						}
						Location playerLoc2 = smanager.getPlayerLoc2(pname);
						if (playerLoc2 != null) {
							player
									.sendMessage("§a"
											+ language
													.getPhrase("Secondary.Selection")
											+ ":§b ("
											+ playerLoc2.getBlockX() + ", "
											+ playerLoc2.getBlockY() + ", "
											+ playerLoc2.getBlockZ() + ")");
						}
						return true;
					} else if (args[1].equals("chunk")) {
						smanager.selectChunk(player);
						return true;
					} else if (args[1].equals("worldedit")) {
						smanager.worldEdit(player);
						return true;
					}
				} else if (args.length == 3) {
					if (args[1].equals("expand")) {
						int amount;
						try {
							amount = Integer.parseInt(args[2]);
						} catch (Exception ex) {
							player.sendMessage("§c"
									+ language.getPhrase("InvalidAmount"));
							return true;
						}
						smanager.modify(player, false, amount);
						return true;
					} else if (args[1].equals("shift")) {
						int amount;
						try {
							amount = Integer.parseInt(args[2]);
						} catch (Exception ex) {
							player.sendMessage("§c"
									+ language.getPhrase("InvalidAmount"));
							return true;
						}
						smanager.modify(player, true, amount);
						return true;
					}
				}
				if (args.length > 1 && args[1].equals("residence")) {
					ClaimedResidence res = rmanager.getByName(args[2]);
					if (res == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
						return true;
					}
					CuboidArea area = res.getArea(args[3]);
					if (area != null) {
						smanager.placeLoc1(pname, area.getHighLoc());
						smanager.placeLoc2(pname, area.getLowLoc());
						player.sendMessage("§a"
								+ language.getPhrase("SelectionArea", "§6"
										+ args[3] + "§a.§6" + args[2]
										+ "§a"));
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("AreaNonExist"));
					}
					return true;
				} else {
					try {
						smanager.selectBySize(player, Integer
								.parseInt(args[1]), Integer
								.parseInt(args[2]), Integer
								.parseInt(args[3]));
						return true;
					} catch (Exception ex) {
						player.sendMessage("§c"
								+ language.getPhrase("SelectionFail"));
						return true;
					}
				}
			} else if (args[0].equals("create")) {
				if (args.length != 2) {
					return false;
				}
				if (smanager.hasPlacedBoth(pname)) {
					rmanager.addResidence(player, args[1], smanager
							.getPlayerLoc1(pname), smanager
							.getPlayerLoc2(pname), resadmin);
					return true;
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("SelectPoints"));
					return true;
				}
			} else if (args[0].equals("subzone") || args[0].equals("sz")) {
				if (args.length != 2 && args.length != 3) {
					return false;
				}
				String zname;
				String parent;
				if (args.length == 2) {
					parent = rmanager.getNameByLoc(player.getLocation());
					zname = args[1];
				} else {
					parent = args[1];
					zname = args[2];
				}
				if (smanager.hasPlacedBoth(pname)) {
					ClaimedResidence res = rmanager.getByName(parent);
					if (res == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
						return true;
					}
					res.addSubzone(player, smanager.getPlayerLoc1(pname),
							smanager.getPlayerLoc2(pname), zname, resadmin);
					return true;
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("SelectPoints"));
					return true;
				}
			} else if (args[0].equals("gui")) {
				if (slistener != null) {
					if (args.length == 1)
						ResidenceSpout.showResidenceFlagGUI(SpoutManager
								.getPlayer(player), resPlugin, rmanager
								.getNameByLoc(player.getLocation()),
								resadmin);
					else if (args.length == 2)
						ResidenceSpout
								.showResidenceFlagGUI(SpoutManager
										.getPlayer(player), resPlugin, args[1],
										resadmin);
				}
				return true;
			} else if (args[0].equals("sublist")) {
				if (args.length == 1 || args.length == 2
						|| args.length == 3) {
					ClaimedResidence res;
					if (args.length == 1)
						res = rmanager.getByLoc(player.getLocation());
					else
						res = rmanager.getByName(args[1]);
					if (res != null)
						res.printSubzoneList(player, page);
					else
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					return true;
				}
			} else if (args[0].equals("remove") || args[0].equals("delete")) {
				if (args.length == 1) {
					String area = rmanager.getNameByLoc(player
							.getLocation());
					if (area != null) {
						if (!deleteConfirm.containsKey(player.getName())
								|| !area.equalsIgnoreCase(deleteConfirm
										.get(player.getName()))) {
							player.sendMessage("§c"
									+ language.getPhrase("DeleteConfirm",
											"§e" + area + "§c"));
							deleteConfirm.put(player.getName(), area);
						} else {
							rmanager
									.removeResidence(player, area, resadmin);
						}
						return true;
					}
					return false;
				}
				if (args.length != 2) {
					return false;
				}
				if (!deleteConfirm.containsKey(player.getName())
						|| !args[1].equalsIgnoreCase(deleteConfirm
								.get(player.getName()))) {
					player.sendMessage("§c"
							+ language.getPhrase("DeleteConfirm", "§e"
									+ args[1] + "§c"));
					deleteConfirm.put(player.getName(), args[1]);
				} else {
					rmanager.removeResidence(player, args[1], resadmin);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("confirm")) {
				if (args.length == 1) {
					String area = deleteConfirm.get(player.getName());
					if (area == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					} else {
						rmanager.removeResidence(player, area, resadmin);
						deleteConfirm.remove(player.getName());
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("removeall")) {
				if (args.length != 2)
					return false;
				if (resadmin || args[1].endsWith(pname)) {
					rmanager.removeAllByOwner(args[1]);
					player.sendMessage("§a"
							+ language.getPhrase("RemovePlayersResidences",
									"§e" + args[1] + "§a"));
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
				}
				return true;
			} else if (args[0].equals("area")) {
				if (args.length == 4) {
					if (args[1].equals("remove")) {
						ClaimedResidence res = rmanager.getByName(args[2]);
						if (res != null)
							res.removeArea(player, args[3], resadmin);
						else
							player.sendMessage("§c"
									+ language
											.getPhrase("InvalidResidence"));
						return true;
					} else if (args[1].equals("add")) {
						if (smanager.hasPlacedBoth(pname)) {
							ClaimedResidence res = rmanager
									.getByName(args[2]);
							if (res != null)
								res.addArea(player, new CuboidArea(smanager
										.getPlayerLoc1(pname), smanager
										.getPlayerLoc2(pname)), args[3],
										resadmin);
							else
								player
										.sendMessage("§c"
												+ language
														.getPhrase("InvalidResidence"));
						} else {
							player.sendMessage("§c"
									+ language.getPhrase("SelectPoints"));
						}
						return true;
					} else if (args[1].equals("replace")) {
						if (smanager.hasPlacedBoth(pname)) {
							ClaimedResidence res = rmanager
									.getByName(args[2]);
							if (res != null)
								res.replaceArea(player, new CuboidArea(
										smanager.getPlayerLoc1(pname),
										smanager.getPlayerLoc2(pname)),
										args[3], resadmin);
							else
								player
										.sendMessage("§c"
												+ language
														.getPhrase("InvalidResidence"));
						} else {
							player.sendMessage("§c"
									+ language.getPhrase("SelectPoints"));
						}
						return true;
					}
				}
				if ((args.length == 3 || args.length == 4)
						&& args[1].equals("list")) {
					ClaimedResidence res = rmanager.getByName(args[2]);
					if (res != null) {
						res.printAreaList(player, page);
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					}
					return true;
				} else if ((args.length == 3 || args.length == 4)
						&& args[1].equals("listall")) {
					ClaimedResidence res = rmanager.getByName(args[2]);
					if (res != null) {
						res.printAdvancedAreaList(player, page);
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					}
					return true;
				}
			} else if (args[0].equals("lists")) {
				if (args.length == 2) {
					if (args[1].equals("list")) {
						pmanager.printLists(player);
						return true;
					}
				} else if (args.length == 3) {
					if (args[1].equals("view")) {
						pmanager.printList(player, args[2]);
						return true;
					} else if (args[1].equals("remove")) {
						pmanager.removeList(player, args[2]);
						return true;
					} else if (args[1].equals("add")) {
						pmanager.makeList(player, args[2]);
						return true;
					}
				} else if (args.length == 4) {
					if (args[1].equals("apply")) {
						pmanager.applyListToResidence(player, args[2],
								args[3], resadmin);
						return true;
					}
				} else if (args.length == 5) {
					if (args[1].equals("set")) {
						pmanager.getList(pname, args[2]).setFlag(args[3],
								FlagPermissions.stringToFlagState(args[4]));
						player.sendMessage("§a"
								+ language.getPhrase("FlagSet"));
						return true;
					}
				} else if (args.length == 6) {
					if (args[1].equals("gset")) {
						pmanager.getList(pname, args[2]).setGroupFlag(
								args[3], args[4],
								FlagPermissions.stringToFlagState(args[5]));
						player.sendMessage("§a"
								+ language.getPhrase("FlagSet"));
						return true;
					} else if (args[1].equals("pset")) {
						pmanager.getList(pname, args[2]).setPlayerFlag(
								args[3], args[4],
								FlagPermissions.stringToFlagState(args[5]));
						player.sendMessage("§a"
								+ language.getPhrase("FlagSet"));
						return true;
					}
				}
			} else if (args[0].equals("default")) {
				if (args.length == 2) {
					ClaimedResidence res = rmanager.getByName(args[1]);
					res.getPermissions()
							.applyDefaultFlags(player, resadmin);
					return true;
				}
			} else if (args[0].equals("limits")) {
				if (args.length == 1) {
					gmanager.getGroup(player).printLimits(player);
					return true;
				}
			} else if (args[0].equals("info")) {
				if (args.length == 1) {
					String area = rmanager.getNameByLoc(player
							.getLocation());
					if (area != null) {
						rmanager.printAreaInfo(area, player);
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					}
					return true;
				} else if (args.length == 2) {
					rmanager.printAreaInfo(args[1], player);
					return true;
				}
			} else if (args[0].equals("check")) {
				if (args.length == 3 || args.length == 4) {
					if (args.length == 4) {
						pname = args[3];
					}
					ClaimedResidence res = rmanager.getByName(args[1]);
					if (res == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
						return true;
					}
					if (!res.getPermissions().hasApplicableFlag(pname,
							args[2]))
						player
								.sendMessage(language.getPhrase(
										"FlagCheckFalse", "§e" + args[2]
												+ "§c.§e" + pname + "§c.§e"
												+ args[1] + "§c"));
					else
						player.sendMessage(language.getPhrase(
								"FlagCheckTrue", "§a"
										+ args[2]
										+ "§e.§a"
										+ pname
										+ "§e.§e"
										+ args[1]
										+ "§c."
										+ (res.getPermissions().playerHas(
												pname,
												res.getPermissions()
														.getWorld(),
												args[2], false) ? "§aTRUE"
												: "§cFALSE")));
					return true;
				}
			} else if (args[0].equals("current")) {
				if (args.length != 1)
					return false;
				String res = rmanager.getNameByLoc(player.getLocation());
				if (res == null) {
					player.sendMessage("§c"
							+ language.getPhrase("NotInResidence"));
				} else {
					player.sendMessage("§a"
							+ language.getPhrase("InResidence", "§e" + res
									+ "§a"));
				}
				return true;
			} else if (args[0].equals("set")) {
				if (args.length == 3) {
					String area = rmanager.getNameByLoc(player
							.getLocation());
					if (area != null) {
						rmanager.getByName(area).getPermissions().setFlag(
								player, args[1], args[2], resadmin);
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					}
					return true;
				} else if (args.length == 4) {
					ClaimedResidence area = rmanager.getByName(args[1]);
					if (area != null) {
						area.getPermissions().setFlag(player, args[2],
								args[3], resadmin);
					} else
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					return true;
				}
			} else if (args[0].equals("pset")) {
				if (args.length == 3
						&& args[2].equalsIgnoreCase("removeall")) {
					ClaimedResidence area = rmanager.getByLoc(player
							.getLocation());
					if (area != null)
						area.getPermissions().removeAllPlayerFlags(player,
								args[1], resadmin);
					else
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					return true;
				} else if (args.length == 4
						&& args[3].equalsIgnoreCase("removeall")) {
					ClaimedResidence area = rmanager.getByName(args[1]);
					if (area != null) {
						area.getPermissions().removeAllPlayerFlags(player,
								args[2], resadmin);
					} else
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					return true;
				} else if (args.length == 4) {
					ClaimedResidence area = rmanager.getByLoc(player
							.getLocation());
					if (area != null) {
						area.getPermissions().setPlayerFlag(player,
								args[1], args[2], args[3], resadmin);
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					}
					return true;
				} else if (args.length == 5) {
					ClaimedResidence area = rmanager.getByName(args[1]);
					if (area != null) {
						area.getPermissions().setPlayerFlag(player,
								args[2], args[3], args[4], resadmin);
					} else
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					return true;
				}
			} else if (args[0].equals("gset")) {
				if (args.length == 4) {
					ClaimedResidence area = rmanager.getByLoc(player
							.getLocation());
					if (area != null) {
						area.getPermissions().setGroupFlag(player, args[1],
								args[2], args[3], resadmin);

					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidArea"));
					}
					return true;
				} else if (args.length == 5) {
					ClaimedResidence area = rmanager.getByName(args[1]);
					if (area != null) {
						area.getPermissions().setGroupFlag(player, args[2],
								args[3], args[4], resadmin);
					} else
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
					return true;
				}
			} else if (args[0].equals("lset")) {
				ClaimedResidence res = null;
				Material mat = null;
				String listtype = null;
				boolean showinfo = false;
				if (args.length == 2 && args[1].equals("info")) {
					res = rmanager.getByLoc(player.getLocation());
					showinfo = true;
				} else if (args.length == 3 && args[2].equals("info")) {
					res = rmanager.getByName(args[1]);
					showinfo = true;
				}
				if (showinfo) {
					if (res == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
						return true;
					}
					player.sendMessage("§cBlacklist:");
					res.getItemBlacklist().printList(player);
					player.sendMessage("§aIgnorelist:");
					res.getItemIgnoreList().printList(player);
					return true;
				} else if (args.length == 4) {
					res = rmanager.getByName(args[1]);
					listtype = args[2];
					try {
						mat = Material.valueOf(args[3].toUpperCase());
					} catch (Exception ex) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidMaterial"));
						return true;
					}
				} else if (args.length == 3) {
					res = rmanager.getByLoc(player.getLocation());
					listtype = args[1];
					try {
						mat = Material.valueOf(args[2].toUpperCase());
					} catch (Exception ex) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidMaterial"));
						return true;
					}
				}
				if (res != null) {
					if (listtype.equalsIgnoreCase("blacklist")) {
						res.getItemBlacklist().playerListChange(player,
								mat, resadmin);
					} else if (listtype.equalsIgnoreCase("ignorelist")) {
						res.getItemIgnoreList().playerListChange(player,
								mat, resadmin);
					} else {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidList"));
					}
					return true;
				} else
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
			} else if (args[0].equals("list")) {
				if (args.length == 1) {
					rmanager.listResidences(player);
					return true;
				} else if (args.length == 2) {
					try {
						Integer.parseInt(args[1]);
						rmanager.listResidences(player, page);
					} catch (Exception ex) {
						rmanager.listResidences(player, args[1]);
					}
					return true;
				} else if (args.length == 3) {
					rmanager.listResidences(player, args[1], page);
					return true;
				}
			} else if (args[0].equals("listhidden")) {
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
					return true;
				}
				if (args.length == 1) {
					rmanager.listResidences(player, 1, true);
					return true;
				} else if (args.length == 2) {
					try {
						Integer.parseInt(args[1]);
						rmanager.listResidences(player, page, true);
					} catch (Exception ex) {
						rmanager.listResidences(player, args[1], 1, true);
					}
					return true;
				} else if (args.length == 3) {
					rmanager.listResidences(player, args[1], page, true);
					return true;
				}
			} else if (args[0].equals("rename")) {
				if (args.length == 3) {
					rmanager.renameResidence(player, args[1], args[2],
							resadmin);
					return true;
				}
			} else if (args[0].equals("renamearea")) {
				if (args.length == 4) {
					ClaimedResidence res = rmanager.getByName(args[1]);
					if (res == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
						return true;
					}
					res.renameArea(player, args[2], args[3], resadmin);
					return true;
				}
			} else if (args[0].equals("unstuck")) {
				if (args.length != 1) {
					return false;
				}
				group = gmanager.getGroup(player);
				if (!group.hasUnstuckAccess()) {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
					return true;
				}
				ClaimedResidence res = rmanager.getByLoc(player
						.getLocation());
				if (res == null) {
					player.sendMessage("§c"
							+ language.getPhrase("NotInResidence"));
				} else {
					player.sendMessage("§e" + language.getPhrase("Moved")
							+ "...");
					player.teleport(res.getOutsideFreeLoc(player
							.getLocation()));
				}
				return true;
			} else if (args[0].equals("mirror")) {
				if (args.length != 3) {
					return false;
				}
				rmanager.mirrorPerms(player, args[2], args[1], resadmin);
				return true;
			} else if (args[0].equals("listall")) {
				if (args.length == 1) {
					rmanager.listAllResidences(player, 1);
				} else if (args.length == 2) {
					try {
						rmanager.listAllResidences(player, page);
					} catch (Exception ex) {
					}
				} else {
					return false;
				}
				return true;
			} else if (args[0].equals("listallhidden")) {
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
					return true;
				}
				if (args.length == 1) {
					rmanager.listAllResidences(player, 1, true);
				} else if (args.length == 2) {
					try {
						rmanager.listAllResidences(player, page, true);
					} catch (Exception ex) {
					}
				} else {
					return false;
				}
				return true;
			} else if (args[0].equals("version")) {
				player.sendMessage("§7------------------------------------");
				player.sendMessage("§cThis server running §6Residence§c version: §9"
								+ resPlugin.getDescription().getVersion());
				player.sendMessage("§aCreated by: §ebekvon");
				player.sendMessage("§aModified for use one Silent Pro. by: Silentneeb");
				player.sendMessage("§3For a command list, and help, see the wiki:");
				player.sendMessage("§ahttp://residencebukkitmod.wikispaces.com/");
				player.sendMessage("§bVisit the Residence thread at:");
				player.sendMessage("§9http://forums.bukkit.org/");
				player.sendMessage("§7------------------------------------");
				return true;
			} else if (args[0].equals("material")) {
				if (args.length != 2)
					return false;
				try {
					player.sendMessage("§a"
							+ language.getPhrase("GetMaterial", "§6"
									+ args[1]
									+ "§a.§c"
									+ Material.getMaterial(
											Integer.parseInt(args[1]))
											.name() + "§a"));
				} catch (Exception ex) {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidMaterial"));
				}
				return true;
			} else if (args[0].equals("tpset")) {
				ClaimedResidence res = rmanager.getByLoc(player
						.getLocation());
				if (res != null) {
					res.setTpLoc(player, resadmin);
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
				}
				return true;
			} else if (args[0].equals("tp")) {
				if (args.length != 2) {
					return false;
				}
				ClaimedResidence res = rmanager.getByName(args[1]);
				if (res == null) {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
					return true;
				}
				res.tpToResidence(player, player, resadmin);
				return true;
			} else if (args[0].equals("lease")) {
				if (args.length == 2 || args.length == 3) {
					if (args[1].equals("renew")) {
						if (args.length == 3) {
							leasemanager.renewArea(args[2], player);
						} else {
							leasemanager.renewArea(rmanager
									.getNameByLoc(player.getLocation()),
									player);
						}
						return true;
					} else if (args[1].equals("cost")) {
						if (args.length == 3) {
							ClaimedResidence res = Residence
									.getResidenceManager().getByName(
											args[2]);
							if (res == null
									|| leasemanager.leaseExpires(args[2])) {
								int cost = leasemanager.getRenewCost(res);
								player.sendMessage("§e"
										+ language.getPhrase(
												"LeaseRenewalCost", "§c"
														+ args[2] + "§e.§c"
														+ cost + "§e"));
							} else {
								player
										.sendMessage("§c"
												+ language
														.getPhrase("LeaseNotExpire"));
							}
							return true;
						} else {
							String area = rmanager.getNameByLoc(player
									.getLocation());
							ClaimedResidence res = rmanager.getByName(area);
							if (area == null || res == null) {
								player
										.sendMessage("§c"
												+ language
														.getPhrase("InvalidArea"));
								return true;
							}
							if (leasemanager.leaseExpires(area)) {
								int cost = leasemanager.getRenewCost(res);
								player.sendMessage("§e"
										+ language.getPhrase(
												"LeaseRenewalCost", "§c"
														+ area + "§e.§c"
														+ cost + "§e"));
							} else {
								player
										.sendMessage("§c"
												+ language
														.getPhrase("LeaseNotExpire"));
							}
							return true;
						}
					}
				} else if (args.length == 4) {
					if (args[1].equals("set")) {
						if (!resadmin) {
							player.sendMessage("§c"
									+ language.getPhrase("NoPermission"));
							return true;
						}
						if (args[3].equals("infinite")) {
							if (leasemanager.leaseExpires(args[2])) {
								leasemanager.removeExpireTime(args[2]);
								player
										.sendMessage("§a"
												+ language
														.getPhrase("LeaseInfinite"));
							} else {
								player
										.sendMessage("§c"
												+ language
														.getPhrase("LeaseNotExpire"));
							}
							return true;
						} else {
							int days;
							try {
								days = Integer.parseInt(args[3]);
							} catch (Exception ex) {
								player
										.sendMessage("§c"
												+ language
														.getPhrase("InvalidDays"));
								return true;
							}
							leasemanager.setExpireTime(player, args[2],
									days);
							return true;
						}
					}
				}
				return false;
			} else if (args[0].equals("bank")) {
				if (args.length != 3)
					return false;
				ClaimedResidence res = rmanager.getByName(plistener
						.getLastAreaName(pname));
				if (res == null) {
					player.sendMessage("§c"
							+ language.getPhrase("NotInResidence"));
					return true;
				}
				int amount = 0;
				try {
					amount = Integer.parseInt(args[2]);
				} catch (Exception ex) {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidAmount"));
					return true;
				}
				if (args[1].equals("deposit")) {
					res.getBank().deposit(player, amount, resadmin);
				} else if (args[1].equals("withdraw")) {
					res.getBank().withdraw(player, amount, resadmin);
				} else
					return false;
				return true;
			} else if (args[0].equals("market")) {
				if (args.length == 1)
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
			} else if (args[0].equals("message")) {
				ClaimedResidence res = null;
				int start = 0;
				boolean enter = false;
				if (args.length < 2)
					return false;
				if (args[1].equals("enter")) {
					enter = true;
					res = rmanager.getByLoc(player.getLocation());
					start = 2;
				} else if (args[1].equals("leave")) {
					res = rmanager.getByLoc(player.getLocation());
					start = 2;
				} else if (args[1].equals("remove")) {
					if (args.length > 2 && args[2].equals("enter")) {
						res = rmanager.getByLoc(player.getLocation());
						if (res != null) {
							res.setEnterLeaveMessage(player, null, true,
									resadmin);
						} else {
							player.sendMessage("§c"
									+ language
											.getPhrase("InvalidResidence"));
						}
						return true;
					} else if (args.length > 2 && args[2].equals("leave")) {
						res = rmanager.getByLoc(player.getLocation());
						if (res != null) {
							res.setEnterLeaveMessage(player, null, false,
									resadmin);
						} else {
							player.sendMessage("§c"
									+ language
											.getPhrase("InvalidResidence"));
						}
						return true;
					}
					player.sendMessage("§c"
							+ language.getPhrase("InvalidMessageType"));
					return true;
				} else if (args.length > 2 && args[2].equals("enter")) {
					enter = true;
					res = rmanager.getByName(args[1]);
					start = 3;
				} else if (args.length > 2 && args[2].equals("leave")) {
					res = rmanager.getByName(args[1]);
					start = 3;
				} else if (args.length > 2 && args[2].equals("remove")) {
					res = rmanager.getByName(args[1]);
					if (args.length != 4) {
						return false;
					}
					if (args[3].equals("enter")) {
						if (res != null) {
							res.setEnterLeaveMessage(player, null, true,
									resadmin);
						}
						return true;
					} else if (args[3].equals("leave")) {
						if (res != null) {
							res.setEnterLeaveMessage(player, null, false,
									resadmin);
						}
						return true;
					}
					player.sendMessage("§c"
							+ language.getPhrase("InvalidMessageType"));
					return true;
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidMessageType"));
					return true;
				}
				if (start == 0)
					return false;
				String message = "";
				for (int i = start; i < args.length; i++) {
					message = message + args[i] + " ";
				}
				if (res != null) {
					res.setEnterLeaveMessage(player, message, enter,
							resadmin);
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
				}
				return true;
			} else if (args[0].equals("give")) {
				rmanager.giveResidence(player, args[2], args[1], resadmin);
				return true;
			} else if (args[0].equals("setowner")) {
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
					return true;
				}
				ClaimedResidence area = rmanager.getByName(args[1]);
				if (area != null) {
					area.getPermissions().setOwner(args[2], true);
					player.sendMessage("§a"
							+ language.getPhrase("ResidenceOwnerChange",
									"§e " + args[1] + " §a.§e" + args[2]
											+ "§a"));
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
				}
				return true;
			} else if (args[0].equals("server")) {
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
					return true;
				}
				if (args.length == 2) {
					ClaimedResidence res = rmanager.getByName(args[1]);
					if (res == null) {
						player.sendMessage("§c"
								+ language.getPhrase("InvalidResidence"));
						return true;
					}
					res.getPermissions().setOwner("Server Land", false);
					player.sendMessage("§a"
							+ language.getPhrase("ResidenceOwnerChange",
									"§e" + args[1] + "§a.§eServer Land§a"));
					return true;
				} else
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
			} else if (args[0].equals("clearflags")) {
				if (!resadmin) {
					player.sendMessage("§c"
							+ language.getPhrase("NoPermission"));
					return true;
				}
				ClaimedResidence area = rmanager.getByName(args[1]);
				if (area != null) {
					area.getPermissions().clearFlags();
					player.sendMessage("§a"
							+ language.getPhrase("FlagsCleared"));
				} else {
					player.sendMessage("§c"
							+ language.getPhrase("InvalidResidence"));
				}
				return true;
			} else if (args[0].equals("tool")) {
				player.sendMessage("§e"
						+ language.getPhrase("SelectionTool")
						+ ":§a"
						+ Material.getMaterial(cmanager
								.getSelectionTooldID()));
				player.sendMessage("§e" + language.getPhrase("InfoTool")
						+ ": §a"
						+ Material.getMaterial(cmanager.getInfoToolID()));
				return true;
			}
		}
		return false;
	}

}
