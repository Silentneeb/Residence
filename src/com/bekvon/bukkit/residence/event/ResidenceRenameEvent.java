/**
 * @author keith
 * @filename ResidenceRenameEvent.java
 * @date_created 2012-05-16
 * @last_modified 2012-05-16
 * @description 
 *
 *
 */
package com.bekvon.bukkit.residence.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceRenameEvent extends CancellableResidencePlayerEvent {

	private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    
    protected String oldResName;
    protected String newResName;
    
    
	public ResidenceRenameEvent(Player player, String oldname, String newname, ClaimedResidence resref) {
		
		super("RESIDENCE_RENAME", resref, player);
		oldResName = oldname;
		newResName = newname;
	}

	public String getOldResName() {
		return oldResName;
	}

	public void setOldResName(String oldResName) {
		this.oldResName = oldResName;
	}

	public String getNewResName() {
		return newResName;
	}

	public void setNewResName(String newResName) {
		this.newResName = newResName;
	}

	

}
