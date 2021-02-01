package com.thizthizzydizzy.movecraft;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
public class Movecraft extends JavaPlugin{
    public static final String[] helm = {"\\  |  /","-       -","/  |  \\"};
    public void onEnable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        PluginManager pm = getServer().getPluginManager();
//        pm.registerEvents(new EventListener(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
//        getCommand("movecraft").setExecutor(new CommandMovecraft(this));
        logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public void onDisable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public static boolean isHelm(String... lines){
        return (lines.length>=1&&lines[0].equalsIgnoreCase("[helm]"))||lines.length>=3&&lines[0].equals(helm[0])&&lines[1].equals(helm[1])&&lines[2].equals(helm[2]);
    }
}