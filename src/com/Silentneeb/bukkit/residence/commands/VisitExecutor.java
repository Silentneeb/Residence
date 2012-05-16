/**
 * @author keith
 * @filename VisitExecutor.java
 * @date_created 2012-05-16
 * @last_modified 2012-05-16
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

import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;

public class VisitExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel,
			String[] args) {

		ResidenceCommandEvent cevent = new ResidenceCommandEvent(cmd
				.getName(), args, sender);
		Bukkit.getPluginManager().callEvent(cevent);
		if (cevent.isCancelled())
			return true;
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (player == null){
			sender.sendMessage("Ingame only");
			return true;
		}
		
		if(args.length != 1){
			return false;
		}
		
		//TODO
		player.sendMessage("TODO");
		
		return true;
	}

}
