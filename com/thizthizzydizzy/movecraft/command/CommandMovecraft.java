package com.thizthizzydizzy.movecraft.command;
import com.thizthizzydizzy.movecraft.Movecraft;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
public class CommandMovecraft implements TabExecutor{
    private final Movecraft plugin;
    public CommandMovecraft(Movecraft plugin){
        this.plugin = plugin;
    }
    private final ArrayList<MovecraftCommand> commands = new ArrayList<>();
    {
        commands.add(new MovecraftCommand("reload"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.reloadConfig();
                plugin.reload();
                sender.sendMessage("Movecraft reloaded!");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/movecraft reload";
            }
        });
        commands.add(new MovecraftCommand("help"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                for(MovecraftCommand cmd : commands){
                    if(cmd.hasPermission(sender)){
                        String s = cmd.getUsage();
                        if(s!=null)sender.sendMessage(s);
                    }
                }
                return true;
            }
            @Override
            protected String getUsage(){
                return "/movecraft help";
            }
        });
        MovecraftCommand debugOn = new MovecraftCommand("on"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = true;
                sender.sendMessage("Debug mode enabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/movecraft debug on";
            }
        };
        MovecraftCommand debugOff = new MovecraftCommand("off"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = false;
                sender.sendMessage("Debug mode disabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/movecraft debug off";
            }
        };
        commands.add(new MovecraftCommand("debug", debugOn, debugOff) {
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = !plugin.debug;
                sender.sendMessage("Debug mode "+(plugin.debug?"enabled":"disabled"));
                return true;
            }
            @Override
            protected String getUsage(){
                return "/movecraft debug [on|off]";
            }
        });
    }
    private String getFullUsage(){
        return "/movecraft help|reload|debug [on|off]";
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length<1){
            sender.sendMessage("Usage: "+getFullUsage());
            return true;
        }
        for(MovecraftCommand cmd : commands){
            if(args[0].equals(cmd.command)){
                return cmd.onCommand(sender, command, label, trim(args, 1), args);
            }
        }
        sender.sendMessage("Usage: "+getFullUsage());
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> strs = new ArrayList<>();
        if(args.length==1){
            for(MovecraftCommand cmd : commands){
                if(cmd.command.substring(0, cmd.command.length()-1).startsWith(args[0])&&cmd.hasPermission(sender))strs.add(cmd.command);
            }
        }
        if(args.length>1){
            for(MovecraftCommand cmd : commands){
                if(args[0].equals(cmd.command))return cmd.onTabComplete(sender, command, label, trim(args, 1));
            }
        }
        return strs;
    }
    public String[] trim(String[] data, int beginning){
        if(data==null)return null;
        String[] newData = new String[Math.max(0,data.length-beginning)];
        for(int i = 0; i<newData.length; i++){
            newData[i] = data[i+beginning];
        }
        return newData;
    }
}