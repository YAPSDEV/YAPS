package me.stutiguias.yaps.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.stutiguias.yaps.commands.YAPSCommands;
import me.stutiguias.yaps.configs.Config;
import me.stutiguias.yaps.db.IDataQueries;
import me.stutiguias.yaps.db.MySQLDataQueries;
import me.stutiguias.yaps.db.SqliteDataQueries;
import me.stutiguias.yaps.listener.PlayerListener;
import me.stutiguias.yaps.listener.SignListener;
import me.stutiguias.yaps.metrics.Metrics;
import me.stutiguias.yaps.model.Area;
import me.stutiguias.yaps.model.BlockProtected;
import me.stutiguias.yaps.model.Save;
import me.stutiguias.yaps.model.YAPSPlayer;
import me.stutiguias.yaps.task.PurgeOldRecordsTask;
import me.stutiguias.yaps.task.SaveTask;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Yaps extends JavaPlugin {
    
    public String prefix = "[YAPS] ";
    public static final Logger logger = Logger.getLogger("Minecraft");

    private final PlayerListener playerListener = new PlayerListener(this);
    private final SignListener SignListener = new SignListener(this);

    public static Queue<Save> SaveList = new LinkedList<>();
    
    public static HashMap<Player,Area> AreaCreating;
    public static HashMap<String,YAPSPlayer> PlayerProfiles;
    public static List<String> notProtectPlace;
    
    public static List<Area> Areas;
    
    public static HashMap<Location,BlockProtected> Protected;
    
    public Permission permission = null;
    public Economy economy = null;

    public static boolean protectStatus;
    
    public static Config config;
    
    public static IDataQueries db;
    
    public static boolean update = false;
    public static String name = "";
    public static String type = "";
    public static String version = "";
    public static String link = "";

    @Override
    public void onEnable() {
               
        File dir = getDataFolder();
        if (!dir.exists()) {
          dir.mkdirs();
        }
        
        AreaCreating = new HashMap<>();
        PlayerProfiles = new HashMap<>();
        notProtectPlace = new ArrayList<>();
        
        config = new Config(this);
        
        protectStatus = true;
        
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvents(playerListener, this);
        pm.registerEvents(SignListener,this);
        
        setupPermissions();
        setupEconomy();
        
        if(config.DataBaseType.equalsIgnoreCase("mysql")) {
            db = new MySQLDataQueries(this,config.Host , config.Port, config.Username,config.Password,config.Database);
        }else{
            db = new SqliteDataQueries(this);
        }
        
        Areas = db.getAreas();
        
        if(config.SearchAgainstMemory) Protected = db.GetAllProtect();
        
        if(config.SaveQueue) getServer().getScheduler().runTaskTimerAsynchronously(this,new SaveTask(this),config.RunTaskSeconds * 20L,config.RunTaskSeconds * 20L);
        
        if(config.AutoPurge) getServer().getScheduler().runTaskTimerAsynchronously(this, new PurgeOldRecordsTask(this), config.PurgeOldRecordsTaskTimer * 20L,config.PurgeOldRecordsTaskTimer * 20L);
        
        // Metrics 
        try {
         logger.log(Level.INFO, "{0} {1} - Sending Metrics, Thank You!", new Object[]{prefix, "[Metrics]"});
         Metrics metrics = new Metrics(this);
         metrics.start();
        } catch (IOException e) {
         logger.log(Level.WARNING, "{0} {1} !! Failed to submit the stats !! ", new Object[]{prefix, "[Metrics]"});
        }
        
        getCommand("yaps").setExecutor(new YAPSCommands(this));
    }

    @Override
    public void onDisable() {
        
        for(Save save:SaveList) {
           if(save.getFunction().contains("add")) Yaps.db.InsertProtect(save.getBlockProtected());
           if(save.getFunction().contains("remove")) Yaps.db.RemoveProtect(save.getBlockProtected().getLocation());
        }
        SaveList.clear();
        
        getServer().getPluginManager().disablePlugin(this);
    }
    
    public void OnReload() {
        config.reloadConfig();
        getServer().getPluginManager().disablePlugin(this);
        getServer().getPluginManager().enablePlugin(this);
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
                economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public boolean hasPermission(String PlayerName,UUID PlayerUUID,String Permission) {
       return permission.has(getServer().getPlayer(PlayerUUID).getWorld(),PlayerName,Permission);
    }
    
    public boolean hasPermission(Player player, String Permission) {
        return permission.has(player.getWorld(), player.getName(), Permission.toLowerCase());
    }        
    
    public long getCurrentMilli() {
            return System.currentTimeMillis();
    }
    
    public int getAreaIndex(Location location) {
        for (int i = 0; i < Yaps.Areas.size(); i++) {
            
            Area area = Yaps.Areas.get(i);
            
            double fx = area.getFirstSpot().getX();
            double fz = area.getFirstSpot().getZ();
            
            double sx = area.getSecondSpot().getX();
            double sz = area.getSecondSpot().getZ();

            double x,x2,z,z2;

            if(fx > sx) {
                 x = fx;
                 x2 = sx;
            } else {
                 x = sx;
                 x2 = fx;    
            }

            if(sz > fz) {
               z = sz;
               z2 = fz;
            } else {
               z = fz; 
               z2 = sz;
            }

            if(isInsideProtection(x, x2, z, z2, location)) {
                return i;
            }                
        }
        return -1;
    }

    public Area getArea(Location location) {
         int index = getAreaIndex(location);
         if(index == -1) return null;
         return Areas.get(index);
    }
    
    public Area getArea(String name) {
        for(Area area:Areas) {
            if(area.getName().equalsIgnoreCase(name))
                return area;
        }
        return null;
    }
    
    public boolean isInsideProtection(double x,double x2,double z,double z2,Location location) {
       return location.getX() <= x && location.getX() >= x2 && location.getZ() <= z && location.getZ() >= z2;
    }

}