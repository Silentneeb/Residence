/**
 * @author keith
 * @filename ReloadExecutor.java
 * @date_created 2012-05-17
 * @last_modified 2012-05-17
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

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.permissions.PermissionManager;

public class ReloadExecutor implements CommandExecutor {

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
		
		if (cmd.getName().equals("resreload") && args.length == 0) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (gmanager.isResidenceAdmin(player)) {
					resPlugin.reloadPlugin();
					System.out.println("[Residence] Reloaded by "
							+ player.getName() + ".");
				}
			} else {
				resPlugin.reloadPlugin();
				System.out.println("[Residence] Reloaded by console.");
			}
			return true;
		}
		return false;
	}

}
