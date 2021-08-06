package com.thizthizzydizzy.aeronautics.craft.special;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
public class DamageReport extends Special{
    private int timeout;
    public DamageReport(){
        super("aeronautics:damage_report");
    }
    @Override
    protected void load(JSON.JSONObject json){
        timeout = json.getInt("timeout");
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Special newInstance(){
        return new DamageReport();
    }
    @Override
    public void init(CraftSpecial special){
        special.set("damage", 0);
        special.set("timer", 0);
    }
    @Override
    public void tick(CraftSpecial special){
        if((int)special.get("damage")>0){
            int timer = (int)special.get("timer");
            timer++;
            if(timer>=timeout){
                special.set("damage", 0);
                timer = 0;
            }
            special.set("timer", timer);
        }
    }
    @Override
    public void event(CraftSpecial special, Event event){}
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special, int damage, boolean damaged){
        if(damaged)special.set("damage", (int)special.get("damage")+damage);
    }
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
    @Override
    public void getMessages(CraftSpecial special, ArrayList<Message> messages){
        int damage = (int)special.get("damage");
        if(damage>0){
            messages.add(new Message(Message.Priority.COMBAT, true, true, ChatColor.RED+"Took "+ChatColor.DARK_RED+damage+ChatColor.RED+" damage!"));
        }
    }
    @Override
    public void getMultiblockTypes(CraftSpecial special, ArrayList<Multiblock> multiblockTypes){}
}