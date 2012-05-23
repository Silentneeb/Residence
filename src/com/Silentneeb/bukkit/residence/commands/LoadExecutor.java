/**
 * @author keith
 * @filename LoadExecutor.java
 * @date_created 2012-05-17
 * @last_modified 2012-05-17
 * @description 
 *
 *
 */
package com.Silentneeb.bukkit.residence.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.permissions.PermissionManager;

public class LoadExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel,
			String[] args) {
		
		ResidenceCommandEvent cevent = new ResidenceCommandEvent(cmd
				.getName(), args, sender);
		Bukkit.getPluginManager().callEvent(cevent);
		if (cevent.isCancelled())
			return true;
		
		Residence resPlugin = (Residence)Bukkit.getPluginManager().getPlugin("Residence");
		PermissionManager gmanager = Residence.getPermissionManager();
		
		if (cmd.getName().equals("resload")) {
			if (!(sender instanceof Player)
					|| (sender instanceof Player && gmanager
							.isResidenceAdmin((Player) sender))) {
				try {
					resPlugin.loadYml();
					sender.sendMessage("§a[Residence] Reloaded save file...");
				} catch (Exception ex) {
					sender
							.sendMessage("§c[Residence] Unable to reload the save file, exception occured!");
					sender.sendMessage("§c" + ex.getMessage());
					Logger.getLogger(Residence.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
			return true;
		}
		return false;
	}

}
