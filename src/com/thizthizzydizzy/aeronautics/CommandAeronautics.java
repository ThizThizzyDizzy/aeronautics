package com.thizthizzydizzy.aeronautics;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
public class CommandAeronautics implements TabExecutor{
    private final Aeronautics plugin;
    public CommandAeronautics(Aeronautics plugin){
        this.plugin = plugin;
    }
    private final ArrayList<AeronauticsCommand> commands = new ArrayList<>();
    {
        commands.add(new AeronauticsCommand("help"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                for(AeronauticsCommand cmd : commands){
                    if(cmd.hasPermission(sender)){
                        String s = cmd.getUsage();
                        if(s!=null)sender.sendMessage(s);
                    }
                }
                return true;
            }
            @Override
            protected String getUsage(){
                return "/aeronautics help";
            }
        });
        AeronauticsCommand debugOn = new AeronauticsCommand("on"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = true;
                sender.sendMessage("Debug mode enabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/aeronautics debug on";
            }
        };
        AeronauticsCommand debugOff = new AeronauticsCommand("off"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = false;
                sender.sendMessage("Debug mode disabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/aeronautics debug off";
            }
        };
        commands.add(new AeronauticsCommand("debug", debugOn, debugOff) {
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = !plugin.debug;
                sender.sendMessage("Debug mode "+(plugin.debug?"enabled":"disabled"));
                return true;
            }
            @Override
            protected String getUsage(){
                return "/aeronautics debug [on|off]";
            }
        });
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length<1){
            sender.sendMessage("Usage: /aeronautics (help|debug [on|off])");
            return true;
        }
        for(AeronauticsCommand cmd : commands){
            if(args[0].equals(cmd.command)){
                return cmd.onCommand(sender, command, label, trim(args, 1), args);
            }
        }
        sender.sendMessage("Usage: /aeronautics (help|debug [on|off])");
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> strs = new ArrayList<>();
        if(args.length==1){
            for(AeronauticsCommand cmd : commands){
                if(cmd.command.substring(0, cmd.command.length()-1).startsWith(args[0])&&cmd.hasPermission(sender))strs.add(cmd.command);
            }
        }
        if(args.length>1){
            for(AeronauticsCommand cmd : commands){
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