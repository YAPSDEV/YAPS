/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.stutiguias.yaps.commands;

import me.stutiguias.yaps.init.Yaps;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Daniel
 */
public class Help extends CommandHandler {

    public Help(Yaps plugin) {
        super(plugin);
    }

    @Override
    protected Boolean OnCommand(CommandSender sender, String[] args) {
        this.sender = sender;
        
        SendMessage(MsgHr);
        SendMessage(" &7Yet Another Protect System ");
        
        SendMessage(MsgHr);
        
        if(plugin.hasPermission(sender.getName(),"yaps.define")){
            SendMessage("&6/yaps <d or define> <areaName> &e| &7Save Select area");
            SendMessage("&6/yaps <d or define> <areaName> <owner> &e| &7Save Select area");
        }
        
        if(plugin.hasPermission(sender.getName(),"yaps.listprotectionblocks")){
            SendMessage("&6/yaps <listp>  &e| &7List Protected Blocks");
        }     
        
        if(plugin.hasPermission(sender.getName(),"yaps.onoff")){
            SendMessage("&6/yaps <on|off>  &e| &7Enable or Disable Protect Place Block");
        }       
        
        if(plugin.hasPermission(sender.getName(),"yaps.wand")){
            SendMessage("&6/yaps <w or wand>  &e| &7Get Special Wand to make area");
        }
      
        if(plugin.hasPermission(sender.getName(),"yaps.list")){
            SendMessage("&6/yaps <l or list> &e| &7List all areas");
        }   
                
        if(plugin.hasPermission(sender.getName(),"yaps.info")){
            SendMessage("&6/yaps <i or info> &e| &7info about area you are");
        }   
        
        if(plugin.hasPermission(sender.getName(),"yaps.purge")){
            SendMessage("&6/yaps purge &e| &7Delete Old Records");
        }
        
        if(plugin.hasPermission(sender.getName(),"yaps.delete")){
            SendMessage("&6/yaps <dl or delete> <areaName> &e| &7Delete an area");
        }
        
        if(plugin.hasPermission(sender.getName(),"yaps.tp")){
            SendMessage("&6/yaps <tp or teleport> <areaName> &e| &7Teleport to an area");
        }
  
        if(plugin.hasPermission(sender.getName(),"yaps.update")){
            SendMessage("&6/yaps update &e| &7 Update the plugin");
        }
        
        if(plugin.hasPermission(sender.getName(),"yaps.reload")){
            SendMessage("&6/yaps reload &e| &7Reload the plugin");
        }
        
        SendMessage(MsgHr);
        
        return true;
    }

    @Override
    protected Boolean isInvalid(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
