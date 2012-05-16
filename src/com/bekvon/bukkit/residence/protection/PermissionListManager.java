/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class PermissionListManager {

    private Map<String,Map<String,FlagPermissions>> lists;

    public PermissionListManager()
    {
        lists = Collections.synchronizedMap(new HashMap<String,Map<String,FlagPermissions>>());
    }
    
    public FlagPermissions getList(String player, String listname)
    {
        Map<String, FlagPermissions> get = lists.get(player);
        if(get==null)
        {
            return null;
        }
        return get.get(listname);
    }
    
    public void makeList(Player player, String listname)
    {
        Map<String, FlagPermissions> get = lists.get(player.getName());
        if(get==null)
        {
            get=new HashMap<String,FlagPermissions>();
            lists.put(player.getName(), get);
        }
        FlagPermissions perms = get.get(listname);
        if(perms == null)
        {
            perms = new FlagPermissions();
            get.put(listname, perms);
            player.sendMessage("§a"+Residence.getLanguage().getPhrase("ListCreate", listname));
        }
        else
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("ListExists"));
        }
    }

    public void removeList(Player player, String listname)
    {
        Map<String, FlagPermissions> get = lists.get(player.getName());
        if(get==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidList"));
            return;
        }
        FlagPermissions list = get.get(listname);
        if(list==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidList"));
            return;
        }
        get.remove(listname);
        player.sendMessage("§c"+Residence.getLanguage().getPhrase("ListRemoved"));
    }
    
    public void applyListToResidence(Player player, String listname, String areaname, boolean resadmin)
    {
        FlagPermissions list = this.getList(player.getName(), listname);
        if(list == null)
        {
             player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidList"));
             return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByName(areaname);
        if(res == null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        res.getPermissions().applyTemplate(player, list, resadmin);
    }

    public void printList(Player player, String listname)
    {
        FlagPermissions list = this.getList(player.getName(), listname);
        if(list==null)
        {
            player.sendMessage("§c"+Residence.getLanguage().getPhrase("InvalidList"));
            return;
        }
        player.sendMessage("§d------Permission Template------");
        player.sendMessage("§"+Residence.getLanguage().getPhrase("Name")+": §a" + listname);
        list.printFlags(player);
    }

    public Map<String,Object> save()
    {
        Map<String, Object> root = new LinkedHashMap<String,Object>();
        for(Entry<String, Map<String, FlagPermissions>> players : lists.entrySet())
        {
            Map<String, Object> saveMap = new LinkedHashMap<String,Object>();
            Map<String, FlagPermissions> map = players.getValue();
            for(Entry<String, FlagPermissions> list : map.entrySet())
            {
                saveMap.put(list.getKey(), list.getValue().save());
            }
            root.put(players.getKey(), saveMap);
        }
        return root;
    }
    @SuppressWarnings("unchecked")
	public static PermissionListManager load(Map<String, Object> root) {
        
        PermissionListManager p = new PermissionListManager();
        if(root != null)
        {
            for (Entry<String, Object> players : root.entrySet()) {
                try {
                    Map<String, Object> value = (Map<String, Object>) players.getValue();
                    Map<String, FlagPermissions> loadedMap = Collections.synchronizedMap(new HashMap<String, FlagPermissions>());
                    for (Entry<String, Object> list : value.entrySet()) {
                        loadedMap.put(list.getKey(), FlagPermissions.load((Map<String, Object>) list.getValue()));
                    }
                    p.lists.put(players.getKey(), loadedMap);
                } catch (Exception ex) {
                    System.out.println("[Residence] - Failed to load permission lists for player: " + players.getKey());
                }
            }
        }
        return p;
    }

    public void printLists(Player player)
    {
        StringBuilder sbuild = new StringBuilder();
        Map<String, FlagPermissions> get = lists.get(player.getName());
        sbuild.append("§e"+Residence.getLanguage().getPhrase("Lists")+":§3 ");
        if(get!=null)
        {
            for( Entry<String, FlagPermissions> thislist : get.entrySet())
            {
            sbuild.append(thislist.getKey()).append(" ");
            }
        }
        player.sendMessage(sbuild.toString());
    }
}

