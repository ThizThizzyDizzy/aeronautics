package com.thizthizzydizzy.aeronautics.craft.sink_handler;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public class FallSinkHandler extends SinkHandler{
    private final Aeronautics aeronautics;
    private int sinkMoveTime;
    private int sinkTimer;
    public FallSinkHandler(Aeronautics aeronautics){
        super("aeronautics:fall");
        this.aeronautics = aeronautics;
    }
    @Override
    public SinkHandler newInstance(){
        return new FallSinkHandler(aeronautics);
    }
    @Override
    public void load(JSON.JSONObject json){
        sinkMoveTime = json.getInt("sink_move_time");
    }
    @Override
    public void onStartSinking(Craft craft){
        craft.notifyCrew(ChatColor.RED+"This craft has been destroyed! ABANDON SHIP!");
    }
    @Override
    public void tick(Craft craft) {
        sinkTimer++;
        if(sinkTimer>=sinkMoveTime){
            sinkMoveTime-=sinkTimer;
            ArrayList<Craft.BlockMovement> movements = new ArrayList<>();
            boolean somethingChanged = false;
            do{
                somethingChanged = false;
                for(Iterator<Block> it = craft.blocks.iterator(); it.hasNext();){
                    Block block = it.next();
                    Block down = block.getRelative(BlockFace.DOWN);
                    if(down.getType().isAir()||down.isLiquid()||craft.blocks.contains(down));
                    else{
                        it.remove();
                        somethingChanged = true;
                    }
                }
            }while(somethingChanged);
            if(craft.blocks.isEmpty()){
                craft.dead = true;
                return;
            }
            for(Block block : craft.blocks){
                movements.add(new Craft.BlockMovement(block, block.getRelative(BlockFace.DOWN)));
            }
            craft.move(craft.blocks, movements, craft.type.mediums);
        }
    }
    
}