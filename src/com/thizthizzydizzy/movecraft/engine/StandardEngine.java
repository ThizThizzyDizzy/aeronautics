package com.thizthizzydizzy.movecraft.engine;
import com.thizthizzydizzy.movecraft.Direction;
import com.thizthizzydizzy.movecraft.JSON.JSONArray;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import com.thizthizzydizzy.movecraft.Movecraft;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.craft.CraftEngine;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
public class StandardEngine extends Engine{
    public HashMap<Material, Integer> fuels = new HashMap<>();
    public ArrayList<BlockRequirement> liftBlocks = new ArrayList<>();
    public ArrayList<BlockRequirement> diveBlocks = new ArrayList<>();
    public ArrayList<BlockRequirement> engineBlocks = new ArrayList<>();
    private Integer moveTime;
    private Integer horizMoveDistance;
    private Integer vertMoveDistance;
    private Material maneuverItem;
    private double floatThreshold = .75;
    public StandardEngine(){
        super("movecraft:standard");
    }
    @Override
    protected void load(JSONObject json){
        if(json.hasJSONArray("fuels")){
            JSONArray jsonFuels = json.getJSONArray("fuels");
            for(Object obj : jsonFuels){
                JSONObject jsonFuel = (JSONObject)obj;
                String item = jsonFuel.getString("item");
                int value = jsonFuel.getInt("value");
                if(item.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_ITEMS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(item.substring(1))){
                            for(Material m : tag.getValues()){
                                fuels.put(m, value);
                            }
                            break;
                        }
                    }
                }else{
                    fuels.put(Material.matchMaterial(item), value);
                }
            }
        }
        moveTime = json.getInt("moveTime");
        horizMoveDistance = json.getInt("horizMoveDistance");
        vertMoveDistance = json.getInt("vertMoveDistance");
        if(json.hasJSONArray("lift")){
            JSONArray jsonLiftBlocks = json.getJSONArray("lift");
            for(Object obj : jsonLiftBlocks){
                JSONObject jsonLiftBlock = (JSONObject)obj;
                String block = jsonLiftBlock.getString("block");
                float percent = jsonLiftBlock.getFloat("required");
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            liftBlocks.add(new BlockRequirement(block, tag.getValues(), percent));
                            break;
                        }
                    }
                }else{
                    liftBlocks.add(new BlockRequirement(block, Material.matchMaterial(block), percent));
                }
            }
        }
        if(json.hasJSONArray("dive")){
            JSONArray jsonDiveBlocks = json.getJSONArray("dive");
            for(Object obj : jsonDiveBlocks){
                JSONObject jsonDiveBlock = (JSONObject)obj;
                String block = jsonDiveBlock.getString("block");
                float percent = jsonDiveBlock.getFloat("required");
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            diveBlocks.add(new BlockRequirement(block, tag.getValues(), percent));
                            break;
                        }
                    }
                }else{
                    diveBlocks.add(new BlockRequirement(block, Material.matchMaterial(block), percent));
                }
            }
        }
        if(json.hasJSONArray("engine")){
            JSONArray jsonEngineBlocks = json.getJSONArray("engine");
            for(Object obj : jsonEngineBlocks){
                JSONObject jsonEngineBlock = (JSONObject)obj;
                String block = jsonEngineBlock.getString("block");
                float percent = jsonEngineBlock.getFloat("required");
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            engineBlocks.add(new BlockRequirement(block, tag.getValues(), percent));
                            break;
                        }
                    }
                }else{
                    engineBlocks.add(new BlockRequirement(block, Material.matchMaterial(block), percent));
                }
            }
        }
        maneuverItem = Material.matchMaterial(json.getString("maneuverItem"));
        if(json.hasDouble("floatThreshold"))floatThreshold = json.getDouble("floatThreshold");
    }
    @Override
    protected void createSigns(ArrayList<CraftSign> signs){
        signs.add(new CraftSign(getName()+"/cruise"){
            @Override
            public boolean matches(Craft craft, Sign sign){
                if(craft==null)return false;//no craft can't cruise :3
                if(!craft.hasEngine(StandardEngine.this.getName()))return false;//this engine isn't on that craft
                return sign.getLine(0).trim().toLowerCase().startsWith("cruise:");
            }
            @Override
            public void click(Craft craft, Sign sign, PlayerInteractEvent event){
                if(!craft.isPilot(event.getPlayer()))return;
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                ((StandardEngine)engine.getEngine()).cruise(engine, event.getAction()==Action.LEFT_CLICK_BLOCK?Direction.NONE:Movecraft.getSignRotation(sign.getBlockData()));
                craft.updateSigns();
            }
            @Override
            public void update(Craft craft, Sign sign){
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                Direction cruise = ((StandardEngine)engine.getEngine()).getCruise(engine);
                boolean off = cruise==Direction.UP||cruise==Direction.DOWN||cruise==Direction.NONE;
                sign.setLine(0, "Cruise: "+(off?"OFF":"ON"));
                sign.update();//TODO only if it changes!
            }
            @Override
            public boolean canLeftClick(Craft craft, Sign sign){
                return true;
            }
            @Override
            public boolean canRightClick(Craft craft, Sign sign){
                return true;
            }
        });
        signs.add(new CraftSign(getName()+"/ascend"){
            @Override
            public boolean matches(Craft craft, Sign sign){
                if(craft==null)return false;//no craft can't ascend :3
                if(!craft.hasEngine(StandardEngine.this.getName()))return false;//this engine isn't on that craft
                return sign.getLine(0).trim().toLowerCase().startsWith("ascend:");
            }
            @Override
            public void click(Craft craft, Sign sign, PlayerInteractEvent event){
                if(!craft.isPilot(event.getPlayer()))return;
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                ((StandardEngine)engine.getEngine()).cruise(engine, event.getAction()==Action.LEFT_CLICK_BLOCK?Direction.NONE:Direction.UP);
                craft.updateSigns();
            }
            @Override
            public void update(Craft craft, Sign sign){
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                sign.setLine(0, "Ascend: "+(((StandardEngine)engine.getEngine()).getCruise(engine)==Direction.UP?"ON":"OFF"));
                sign.update();//TODO only if it changes!
            }
            @Override
            public boolean canLeftClick(Craft craft, Sign sign){
                return true;
            }
            @Override
            public boolean canRightClick(Craft craft, Sign sign){
                return true;
            }
        });
        signs.add(new CraftSign(getName()+"/descend"){
            @Override
            public boolean matches(Craft craft, Sign sign){
                if(craft==null)return false;//no craft can't descend :3
                if(!craft.hasEngine(StandardEngine.this.getName()))return false;//this engine isn't on that craft
                return sign.getLine(0).trim().toLowerCase().startsWith("descend:");
            }
            @Override
            public void click(Craft craft, Sign sign, PlayerInteractEvent event){
                if(!craft.isPilot(event.getPlayer()))return;
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                ((StandardEngine)engine.getEngine()).cruise(engine, event.getAction()==Action.LEFT_CLICK_BLOCK?Direction.NONE:Direction.DOWN);
                craft.updateSigns();
            }
            @Override
            public void update(Craft craft, Sign sign){
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                sign.setLine(0, "Descend: "+(((StandardEngine)engine.getEngine()).getCruise(engine)==Direction.DOWN?"ON":"OFF"));
                sign.update();//TODO only if it changes!
            }
            @Override
            public boolean canLeftClick(Craft craft, Sign sign){
                return true;
            }
            @Override
            public boolean canRightClick(Craft craft, Sign sign){
                return true;
            }
        });
        signs.add(new CraftSign(getName()+"/helm"){
            @Override
            public boolean matches(Craft craft, Sign sign){
                if(craft==null)return false;//no craft can't rotate :3
                if(!craft.hasEngine(StandardEngine.this.getName()))return false;//this engine isn't on that craft
                return Movecraft.isHelm(sign.getLines())||sign.getLine(0).trim().equalsIgnoreCase("[helm]");
            }
            @Override
            public void click(Craft craft, Sign sign, PlayerInteractEvent event){
                if(!craft.isPilot(event.getPlayer()))return;
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                int direction = 0;
                if(event.getAction()==Action.RIGHT_CLICK_BLOCK)direction = 1;
                if(event.getAction()==Action.LEFT_CLICK_BLOCK)direction = -1;
                ((StandardEngine)engine.getEngine()).rotate(engine, direction);
                craft.updateSigns();
            }
            @Override
            public void update(Craft craft, Sign sign){
                CraftEngine engine = craft.getEngine(StandardEngine.this.getName());
                for(int i = 0; i<Movecraft.helm.length; i++){
                    sign.setLine(i, Movecraft.helm[i]);
                }
                int rotation = ((StandardEngine)engine.getEngine()).getRotate(engine);
                String rotStr = "";
                while(rotation>=2){
                    rotStr+="»";
                    rotation-=2;
                }
                while(rotation>=1){
                    rotStr+="›";
                    rotation--;
                }
                while(rotation<=-2){
                    rotStr+="«";
                    rotation+=2;
                }
                while(rotation<=-1){
                    rotStr+="‹";
                    rotation++;
                }
                sign.setLine(3, rotStr);
                sign.update();
            }
            @Override
            public boolean canLeftClick(Craft craft, Sign sign){
                return true;
            }
            @Override
            public boolean canRightClick(Craft craft, Sign sign){
                return true;
            }
        });
    }
    @Override
    public Engine newInstance(){
        return new StandardEngine();
    }
    @Override
    public String checkValid(HashSet<Block> craft){
        for(BlockRequirement requirement : liftBlocks){//TODO only if in air!
            float ratio = count(craft, requirement.blocks)/(float)craft.size();
            if(ratio<requirement.ratio){
                return "Not enough lift: "+requirement.name+"! ("+Math.round(ratio*1000)/10f+"%>"+Math.round(requirement.ratio*1000)/10f+"%)";
            }
        }
        for(BlockRequirement requirement : diveBlocks){//TODO only if underwater!
            float ratio = count(craft, requirement.blocks)/(float)craft.size();
            if(ratio<requirement.ratio){
                return "Not enough dive: "+requirement.name+"! ("+Math.round(ratio*1000)/10f+"%>"+Math.round(requirement.ratio*1000)/10f+"%)";
            }
        }
        return null;
    }
    private int count(HashSet<Block> craft, HashSet<Material> materials){
        int c = 0;
        for(Block b : craft){
            if(materials.contains(b.getType()))c++;
        }
        return c;
    }
    
    @Override
    public void init(CraftEngine engine){
        engine.set("cruise", Direction.NONE);
        engine.set("maneuver", new int[3]);
        engine.set("rotate", 0);
        engine.set("timer", 0);
        engine.set("maneuverCooldown", 0);
        engine.set("fuel", 0);
        recalcEngines(engine);
        recalcLift(engine);
        recalcDive(engine);
    }
    @Override
    public void tick(CraftEngine engine){
        if((int)engine.get("maneuverCooldown")>0){
            engine.set("maneuverCooldown", (int)engine.get("maneuverCooldown")-1);
        }
        if(!diveBlocks.isEmpty()&&!canDive(engine)&&engine.getCraft().isUnderwater(true)){
            engine.set("involuntaryTimer", (int)engine.get("involuntaryTimer")+1);
            if((int)engine.get("involuntaryTimer")>=moveTime/Math.max(horizMoveDistance, vertMoveDistance)){
                if(canFly(engine)){
                    move(engine, 0, 1, 0, false);
                }else{
                    if(!move(engine, 0, -1, 0, false))engine.getCraft().startSinking();
                }
                engine.set("involuntaryTimer", 0);
            }
        }
        if(!diveBlocks.isEmpty()&&!canFly(engine)){
            int waterLevel = engine.getCraft().getWaterLevel();
            double percent = (waterLevel-engine.getCraft().getBoundingBox().getMinY())/engine.getCraft().getBoundingBox().getMaxY();
            if(percent<floatThreshold){
                engine.set("involuntaryTimer", (int)engine.get("involuntaryTimer")+1);
                if((int)engine.get("involuntaryTimer")>=moveTime/Math.max(horizMoveDistance, vertMoveDistance)){
                    if(!move(engine, 0, -1, 0, false))engine.getCraft().startSinking();
                    engine.set("involuntaryTimer", 0);
                }
            }
        }
        Direction cruise = getCruise(engine);
        if(cruise==Direction.NONE&&getRotate(engine)==0){
            engine.set("timer", 0);
        }else{
            engine.set("timer", (int)engine.get("timer")+1);
            if((int)engine.get("timer")>=moveTime){
                engine.set("timer", (int)engine.get("timer")-moveTime);
                int rotate = getRotate(engine);
                if(rotate!=0){
                    int direction = rotate/Math.abs(rotate);
                    doRotate(engine, direction);
                }else{
                    move(engine, cruise.x*horizMoveDistance, cruise.y*vertMoveDistance, cruise.z*horizMoveDistance, true);
                }
            }
        }
    }
    private void recalcEngines(CraftEngine engine){
        Craft craft = engine.getCraft();
        float enginePower = 1;
        boolean hasCalculated = false;
        for(BlockRequirement requirement : engineBlocks){
            float power = (count(craft.blocks, requirement.blocks)/(float)craft.blocks.size())/requirement.ratio;
            if(power<enginePower||!hasCalculated){
                enginePower = power;
            }
            hasCalculated = true;
        }
        engine.set("power", enginePower);
    }
    private void recalcLift(CraftEngine engine){
        Craft craft = engine.getCraft();
        float lift = 1;
        boolean hasCalculated = false;
        for(BlockRequirement requirement : liftBlocks){
            float power = (count(craft.blocks, requirement.blocks)/(float)craft.blocks.size())/requirement.ratio;
            if(power<lift||!hasCalculated){
                lift = power;
            }
            hasCalculated = true;
        }
        engine.set("lift", lift);
    }
    private void recalcDive(CraftEngine engine){
        Craft craft = engine.getCraft();
        float dive = 1;
        boolean hasCalculated = false;
        for(BlockRequirement requirement : diveBlocks){
            float power = (count(craft.blocks, requirement.blocks)/(float)craft.blocks.size())/requirement.ratio;
            if(power<dive||!hasCalculated){
                dive = power;
            }
            hasCalculated = true;
        }
        engine.set("dive", dive);
    }
    private void cruise(CraftEngine engine, Direction direction){
        if(engine.get("cruise")==direction){
            engine.set("cruise", Direction.NONE);
        }else{
            engine.set("cruise", direction);
        }
        engine.getCraft().updateSigns();
    }
    private Direction getCruise(CraftEngine engine){
        return (Direction)engine.get("cruise");
    }
    private void rotate(CraftEngine engine, int amount){
        engine.set("rotate", (int)engine.get("rotate")+amount);
        engine.getCraft().updateSigns();
    }
    private int getRotate(CraftEngine engine){
        return (int)engine.get("rotate");
    }
    @Override
    public void event(CraftEngine engine, Event event){
        if(event instanceof PlayerInteractEvent){
            PlayerInteractEvent pie = (PlayerInteractEvent)event;
            Player player = pie.getPlayer();
            if(!engine.getCraft().isPilot(player))return;
            if(pie.getItem()==null||pie.getItem().getType()!=maneuverItem)return;
            if(pie.getAction()==Action.RIGHT_CLICK_AIR||pie.getAction()==Action.RIGHT_CLICK_BLOCK){
                if(!engine.getCraft().isOnBoard(player))return;
                pie.setCancelled(true);
                int x=0,y=0,z=0;
                float pitch = player.getLocation().getPitch();
                float yaw = player.getLocation().getYaw();
                if(pitch>30)y = -1;
                if(pitch<-30)y = 1;
                while(yaw<-180)yaw+=360;
                while(yaw>180)yaw-=360;
                if(pitch<60&&pitch>-60){
                    if(yaw<-120||yaw>120)z = -1;
                    if(yaw>-60&&yaw<60)z = 1;
                    if(yaw>30&&yaw<150)x = -1;
                    if(yaw<-30&&yaw>-150)x = 1;
                }
                maneuver(engine,x,y,z);
            }
        }
        if(event instanceof PlayerQuitEvent){
            PlayerQuitEvent pqe = (PlayerQuitEvent)event;
            if(engine.getCraft().isPilot(pqe.getPlayer()))onUnload(engine);
        }
    }
    private boolean canDive(CraftEngine engine){
        return (float)engine.get("dive")>=1;
    }
    private boolean canFly(CraftEngine engine){
        return (float)engine.get("lift")>=1;
    }
    private boolean move(CraftEngine engine, int x, int y, int z, boolean voluntary){
        if(voluntary){
            if(checkDisabled(engine))return false;
            if(!checkFuel(engine))return false;
        }
        return engine.getCraft().move(x,y,z, (!diveBlocks.isEmpty()&&canDive(engine))||(!diveBlocks.isEmpty()&&!liftBlocks.isEmpty()&&canFly(engine)&&!canDive(engine)));
    }
    /**
     * Checks to see if the craft is disabled, and if so, notifies the pilots.
     * @return <code>true</code> if the craft is disabled
     */
    private boolean checkDisabled(CraftEngine engine){
        boolean disabled = (float)engine.get("power")<1;
        if(disabled){
            engine.getCraft().notifyPilots("Craft is disabled!", Sound.BLOCK_ANVIL_LAND, .4f);
        }
        return disabled;
    }
    /**
     * Checks to see if the craft has fuel, and if not, notifies the crew.
     * @return <code>true</code> if the craft has fuel
     */
    private boolean checkFuel(CraftEngine engine){
        refuel(engine);
        if((int)engine.get("fuel")<=0){
            engine.getCraft().notifyCrew("Out of fuel!");
            return false;
        }
        return true;
    }
    private void refuel(CraftEngine engine){
        if((int)engine.get("fuel")>0)return;
        if(fuels.isEmpty())engine.set("fuel", Integer.MAX_VALUE);
        for(Block block : engine.getCraft().blocks){
            if(block.getType()==Material.FURNACE){
                FurnaceInventory furnace = ((Furnace)block.getState()).getInventory();
                for(Material m : fuels.keySet()){
                    if(furnace.getFuel()!=null&&furnace.getFuel().getType()==m){
                        ItemStack s = furnace.getFuel();
                        if(s.getAmount()==1)furnace.setFuel(new ItemStack(Material.AIR));
                        else{
                            s.setAmount(s.getAmount()-1);
                            furnace.setFuel(s);
                        }
                        engine.set("fuel", (int)engine.get("fuel")+fuels.get(m));
                        return;
                    }
                    if(furnace.getSmelting()!=null&&furnace.getSmelting().getType()==m){
                        ItemStack s = furnace.getSmelting();
                        if(s.getAmount()==1)furnace.setSmelting(new ItemStack(Material.AIR));
                        else{
                            s.setAmount(s.getAmount()-1);
                            furnace.setSmelting(s);
                        }
                        engine.set("fuel", (int)engine.get("fuel")+fuels.get(m));
                        return;
                    }
                    if(furnace.getResult()!=null&&furnace.getResult().getType()==m){
                        ItemStack s = furnace.getResult();
                        if(s.getAmount()==1)furnace.setResult(new ItemStack(Material.AIR));
                        else{
                            s.setAmount(s.getAmount()-1);
                            furnace.setResult(s);
                        }
                        engine.set("fuel", (int)engine.get("fuel")+fuels.get(m));
                        return;
                    }
                }
            }
        }
    }
    @Override
    public void onUnload(CraftEngine engine){
        engine.set("cruise", Direction.NONE);
        engine.set("rotate", 0);
    }
    @Override
    public void onMoved(CraftEngine engine){
        engine.set("fuel", (int)engine.get("fuel")-1);
    }
    private void maneuver(CraftEngine engine, int x, int y, int z){
        if((int)engine.get("maneuverCooldown")>0)return;
        int cooldown = moveTime/Math.min(horizMoveDistance, vertMoveDistance);
        engine.set("maneuverCooldown", cooldown);
        engine.set("timer", 0);
        move(engine, x, y, z, true);
    }
    @Override
    public boolean removeBlock(CraftEngine engine, Player player, int damage, boolean damaged, Location l){
        Craft craft = engine.getCraft();
        for(BlockRequirement requirement : liftBlocks){//TODO only if in air!
            float ratio = count(craft.blocks, requirement.blocks)/(float)craft.blocks.size();
            if(ratio<requirement.ratio){
                craft.notifyBlockChange(player, "Not enough lift: "+requirement.name+"! ("+Math.round(ratio*1000)/10f+"%>"+Math.round(requirement.ratio*1000)/10f+"%)");
                return false;
            }
        }
        for(BlockRequirement requirement : diveBlocks){//TODO only if underwater!
            float ratio = count(craft.blocks, requirement.blocks)/(float)craft.blocks.size();
            if(ratio<requirement.ratio){
                craft.notifyBlockChange(player, "Not enough dive: "+requirement.name+"! ("+Math.round(ratio*1000)/10f+"%>"+Math.round(requirement.ratio*1000)/10f+"%)");
                return false;
            }
        }
        return true;
    }
    @Override
    public void updateHull(CraftEngine engine){
        recalcEngines(engine);
        recalcLift(engine);
        recalcDive(engine);
    }
    @Override
    public boolean addBlock(CraftEngine engine, Player player, Block block, boolean force){
        Craft craft = engine.getCraft();
        for(BlockRequirement requirement : liftBlocks){//TODO only if in air!
            float ratio = count(craft.blocks, requirement.blocks)/(float)craft.blocks.size();
            if(ratio<requirement.ratio){
                craft.notifyBlockChange(player, "Not enough lift: "+requirement.name+"! ("+Math.round(ratio*1000)/10f+"%>"+Math.round(requirement.ratio*1000)/10f+"%)");
                return false;
            }
        }
        for(BlockRequirement requirement : diveBlocks){//TODO only if underwater!
            float ratio = count(craft.blocks, requirement.blocks)/(float)craft.blocks.size();
            if(ratio<requirement.ratio){
                craft.notifyBlockChange(player, "Not enough dive: "+requirement.name+"! ("+Math.round(ratio*1000)/10f+"%>"+Math.round(requirement.ratio*1000)/10f+"%)");
                return false;
            }
        }
        return true;
    }
    private void doRotate(CraftEngine engine, int rotation){
        if(checkDisabled(engine))return;
        if(!checkFuel(engine))return;
        if(engine.getCraft().rotate(engine.getCraft().getOrigin().clone().subtract(.5,.5,.5), rotation, canDive(engine))){
            while(rotation>0){
                rotation--;
                switch(getCruise(engine)){
                    case NORTH:
                        cruise(engine, Direction.EAST);
                        break;
                    case EAST:
                        cruise(engine, Direction.SOUTH);
                        break;
                    case SOUTH:
                        cruise(engine, Direction.WEST);
                        break;
                    case WEST:
                        cruise(engine, Direction.NORTH);
                        break;
                }
            }
            rotate(engine, -rotation);
        }
    }
    @Override
    public void getMessages(CraftEngine engine, ArrayList<Message> messages){
        if(!liftBlocks.isEmpty()){
            float lift = (float)engine.get("lift");
            messages.add(new Message(lift<1?Message.Priority.CRITICAL:Message.Priority.INFO_UNIVERSAL, true, true, "Lift: "+Math.round(lift*100_0)/10d+"%"));
        }
        if(!diveBlocks.isEmpty()){
            float dive = (float)engine.get("dive");
            messages.add(new Message(dive<1?Message.Priority.CRITICAL:Message.Priority.INFO_UNIVERSAL, true, true, "Dive: "+Math.round(dive*100_0)/10d+"%"));
        }
        if(!engineBlocks.isEmpty()){
            float power = (float)engine.get("power");
            messages.add(new Message(power<1?Message.Priority.CRITICAL:Message.Priority.INFO_UNIVERSAL, true, true, "Engines: "+Math.round(power*100_0)/10d+"%"));
        }
    }
    private static class BlockRequirement{
        public HashSet<Material> blocks;
        public String name;
        public float ratio;
        public BlockRequirement(String name, Collection<Material> blocks, float required){
            this.name = name;
            this.blocks = new HashSet<>(blocks);
            this.ratio = required;
        }
        public BlockRequirement(String name, Material block, float required){
            this(name, Arrays.asList(block), required);
        }
    }
}