/**
 * @author Keith
 * @filename HomeExecutor.java
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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;

public class HomeExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel,
			String[] args) {
		
		ResidenceCommandEvent cevent = new ResidenceCommandEvent(cmd
				.getName(), args, sender);
		Bukkit.getPluginManager().callEvent(cevent);
		if (cevent.isCancelled())
			return true;
		
		Player player = null;
		if(sender instanceof Player)
		{
			//TODO: finish, will need some API work first.
			player = (Player)sender;
			Residence.getResidenceManager().getByOwner(player.getName()).tpToResidence(player, player, true);
			
			return true;
		}
		else if(sender instanceof ConsoleCommandSender){
			sender.sendMessage("Ingame only");
			return true;
		}
		return false;
	}

}
