package com.thizthizzydizzy.movecraft;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import com.thizthizzydizzy.movecraft.file.FileFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
public class Movecraft extends JavaPlugin{
    public static final String[] helm = {"\\  |  /","-       -","/  |  \\"};
    public final ArrayList<CraftType> craftTypes = new ArrayList<>();
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
        //<editor-fold defaultstate="collapsed" desc="Load craft types">
        File craftsFolder = new File(getDataFolder(), "crafts");
        if(!craftsFolder.exists()){
            craftsFolder.mkdirs();
            //TODO default crafts
        }
        FILE:for(File f : craftsFolder.listFiles()){
            String name = f.getName();
            String extension;
            if(name.contains(".")){
                String[] split = name.split("\\.");
                extension = split[split.length-1];
            }else extension = "";
            for(FileFormat format : FileFormat.getFileFormats()){
                if(format.getFileExtension().equalsIgnoreCase(extension)){
                    CraftType type;
                    try{
                        type = format.load(f);
                    }catch(Exception ex){
                        logger.log(Level.WARNING, "Failed to load CraftType from file "+name+"!", ex);
                        continue FILE;
                    }
                    if(type==null){
                        logger.log(Level.WARNING, "Failed to load CraftType from file {0}!", name);
                    }else{
                        craftTypes.add(type);
                        logger.log(Level.INFO, "Loaded CraftType {0}", type.getName());
                    }
                    continue FILE;
                }
            }
            logger.log(Level.WARNING, "Unrecognized file extension .{0} on file {1}! (Skipping...)", new Object[]{extension, name});
        }
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