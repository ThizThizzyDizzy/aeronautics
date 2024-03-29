package com.thizthizzydizzy.aeronautics.craft;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.special.Special;
import com.thizthizzydizzy.aeronautics.event.BlockMoveEvent;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Campfire;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.BoundingBox;
public class Craft{
    public final Aeronautics aeronautics;
    private final World world;
    public final CraftType type;
    public final HashSet<Block> blocks;
    private HashSet<Sign> signs;
    private final ArrayList<CraftEngine> engines = new ArrayList<>();
    private final ArrayList<CraftSpecial> specials = new ArrayList<>();
    private HashSet<Player> pilots = new HashSet<>();
    private HashSet<String> allowedPilots = new HashSet<>();
    private HashSet<String> crew = new HashSet<>();
    private BoundingBox bbox;
    private boolean sinking;
    private boolean moving;
    public boolean dead = false;
    private Mode mode = Mode.IDLE;
    private int modeTimer;
    public Random rand = new Random();
    public HashMap<Block, BlockState> was = new HashMap<>();//the list of blocks this ship is currently displacing (ex. water blocks when submerged, air blocks when in air, etc.)
    /**
     * All multiblocks that *can* exist on this craft
     */
    private ArrayList<Multiblock> multiblockTypes = new ArrayList<>();
    private ArrayList<Multiblock> multiblocks = new ArrayList<>();
    private MediumCache cachedCurrentMediums;
    public Craft(Aeronautics aeronautics, World world, CraftType type, HashSet<Block> blocks){
        this.aeronautics = aeronautics;
        this.world = world;
        this.type = type;
        this.blocks = blocks;
    }
    public void addPilot(Player player){
        pilots.add(player);
    }
    public void init(){
        for(Engine engine : type.engines){
            CraftEngine craftEngine = new CraftEngine(this, engine);
            engines.add(craftEngine);
            NEWTYPE:for(Multiblock newType : engine.getMultiblockTypes(craftEngine)){
                for(Multiblock type : multiblockTypes){
                    if(newType.name.equals(type.name)){
                        continue NEWTYPE;
                    }
                }
                multiblockTypes.add(newType);
            }
        }
        for(Special special : type.specials){
            CraftSpecial craftSpecial = new CraftSpecial(this, special);
            specials.add(craftSpecial);
            NEWTYPE:for(Multiblock newType : special.getMultiblockTypes(craftSpecial)){
                for(Multiblock type : multiblockTypes){
                    if(newType.name.equals(type.name)){
                        continue NEWTYPE;
                    }
                }
                multiblockTypes.add(newType);
            }
        }
        getSigns();
        scanForNewMultiblocks();
    }
    public void tick(){
        if(sinking){
            type.sinkHandler.tick(this);
        }
        modeTimer++;
        setMode(Mode.IDLE);
        if(type.hasConstructionMode&&type.constructionPilots>0&&pilots.size()>=type.constructionPilots)setMode(Mode.CONSTRUCTION);
        if(type.hasConstructionMode&&type.constructionCrew>0&&getCrew().size()>=type.constructionCrew)setMode(Mode.CONSTRUCTION);
        if(type.hasCombatMode&&type.combatPilots>0&&pilots.size()>=type.combatPilots)setMode(Mode.COMBAT);
        if(type.hasCombatMode&&type.combatCrew>0&&getCrew().size()>=type.combatCrew)setMode(Mode.COMBAT);
        for(CraftEngine engine : engines){
            engine.getEngine().tick(engine);
        }
        for(CraftSpecial special : specials){
            special.getSpecial().tick(special);
        }
        for(Iterator<Multiblock> it = multiblocks.iterator(); it.hasNext();){
            Multiblock multiblock = it.next();
            multiblock.tick();
            if(multiblock.dead){
                multiblock.onDestroy();
                it.remove();
            }
        }
        ArrayList<Message> messages = new ArrayList<>();
        ArrayList<Message> criticalMessages = new ArrayList<>();
        ENGINE:for(CraftEngine engine : engines){
            for(Message message : engine.getEngine().getMessages(engine)){
                if(message.priority==Message.Priority.CRITICAL)criticalMessages.add(message);
                else messages.add(message);
            }
        }
        SPECIAL:for(CraftSpecial special : specials){
            for(Message message : special.getSpecial().getMessages(special)){
                if(message.priority==Message.Priority.CRITICAL)criticalMessages.add(message);
                else messages.add(message);
            }
        }
        if(!criticalMessages.isEmpty()){
            messages.clear();
            messages.addAll(criticalMessages);
        }
        HashSet<Player> hadPilotMessage = new HashSet<>();
        for(Player player : pilots){
            ArrayList<String> strs = new ArrayList<>();
            for(Message m : messages){
                if(m.priority.shouldDisplay(mode)){
                    if(m.pilot&&(m.player==null||m.player==player)){
                        if(m.raw){
                            if(!strs.isEmpty())strs.add(" | ");
                            strs.add(m.text);
                        }else strs.add(strs.isEmpty()?m.text:(" | "+m.text));
                    }
                }
            }
            if(!strs.isEmpty())hadPilotMessage.add(player);
            String raw = "";
            String str = "";
            for(String s : strs){
                if(s.startsWith("[{")||s.startsWith("{")){
                    raw+=",{\"font\":\"minecraft:default\",\"text\":\""+str+"\"}";
                    str = "";
                    if(s.startsWith("["))raw+=","+s.substring(1, s.length()-1);
                    else raw+=","+s;
                }else str+=s;
            }
            raw+=",{\"font\":\"minecraft:default\",\"text\":\""+str+"\"}";
            actionbarRawIfOnBoard(player, "["+raw.substring(1)+"]");
        }
        for(Player player : getCrew()){
            if(hadPilotMessage.contains(player))continue;
            ArrayList<String> strs = new ArrayList<>();
            for(Message m : messages){
                if(m.priority.shouldDisplay(mode)){
                    if(m.crew&&(m.player==null||m.player==player)){
                        if(m.raw){
                            if(!strs.isEmpty())strs.add(" | ");
                            strs.add(m.text);
                        }else strs.add(strs.isEmpty()?m.text:(" | "+m.text));
                    }
                }
            }
            if(!strs.isEmpty())hadPilotMessage.add(player);
            String raw = "";
            String str = "";
            for(String s : strs){
                if(s.startsWith("[{")||s.startsWith("{")){
                    raw+=",{\"font\":\"minecraft:default\",\"text\":\""+str+"\"}";
                    str = "";
                    if(s.startsWith("["))raw+=","+s.substring(1, s.length()-1);
                    else raw+=","+s;
                }else str+=s;
            }
            raw+=",{\"font\":\"minecraft:default\",\"text\":\""+str+"\"}";
            actionbarRawIfOnBoard(player, "["+raw.substring(1)+"]");
        }
    }
    public boolean hasEngine(String engine){
        return getEngine(engine)!=null;
    }
    public CraftEngine getEngine(String name){
        for(CraftEngine engine : engines){
            if(engine.getEngine().getName().equals(name))return engine;
        }
        return null;
    }
    public boolean hasSpecial(String special){
        return getSpecial(special)!=null;
    }
    public CraftSpecial getSpecial(String name){
        for(CraftSpecial special : specials){
            if(special.getSpecial().getName().equals(name))return special;
        }
        return null;
    }
    public boolean isPilot(Player player){
        return pilots.contains(player);
    }
    public boolean isCrew(Player player){
        if(pilots.isEmpty()&&allowedPilots.isEmpty())return true;
        if(isPilot(player))return true;
        for(String s : crew){
            if(s.equalsIgnoreCase(player.getName())){
                return true;
            }
        }
        return false;
    }
    public World getWorld(){
        return world;
    }
    private void rescanSigns(){
        signs = null;
    }
    private HashSet<Sign> getSigns(){
        if(signs==null){
            signs = new HashSet<>();
            for(Block b : blocks){
                BlockState state = b.getState();
                if(state instanceof Sign){
                    signs.add((Sign)state);
                }
            }
            recalcCrew();
        }
        return signs;
    }
    private void recalcCrew(){
        allowedPilots.clear();
        crew.clear();
        for(Sign sign : getSigns()){
            String line = sign.getLine(0).trim();
            if(line.equalsIgnoreCase("pilot:")||line.equalsIgnoreCase("pilots:")){
                allowedPilots.add(sign.getLine(1).trim());
                allowedPilots.add(sign.getLine(2).trim());
                allowedPilots.add(sign.getLine(3).trim());
            }
            if(line.equalsIgnoreCase("pilot:")||line.equalsIgnoreCase("pilots:")||line.equalsIgnoreCase("crew:")){
                crew.add(sign.getLine(1).trim());
                crew.add(sign.getLine(2).trim());
                crew.add(sign.getLine(3).trim());
            }
        }
    }
    public boolean canPilot(Player player){
        if(allowedPilots.isEmpty())return pilots.isEmpty();
        for(String s : allowedPilots){
            if(s.equalsIgnoreCase(player.getName())){
                return true;
            }
        }
        return false;
    }
    public void updateSigns(){
        for(Sign sign : getSigns()){
            CraftSign cs = CraftSign.getSign(this, sign);
            if(cs!=null)cs.update(this, sign);
        }
    }
    public boolean isUnderwater(boolean truly){
        Set<Block> outsideBlocks = new HashSet<>();
        Set<Block> outerHull = new HashSet<>();
        Set<Block> innerShip = new HashSet<>();
        scanHull(outsideBlocks, outerHull, innerShip);
        boolean foundOne = false;
        for(Block b : outsideBlocks){
            if(truly){
                if(!getBoundingBox().contains(b.getX()+.5,b.getY()+.5,b.getZ()+.5))continue;
            }
            foundOne = true;
            if(b.getType()==Material.WATER||((b.getBlockData() instanceof Waterlogged)&&((Waterlogged)b.getBlockData()).isWaterlogged()))return true;
        }
        if(truly&&!foundOne)return isUnderwater(false);
        return false;
    }
    /**
     * Scans the ship hull for airtightness. Feed in empty lists to get filled
     * @param outsideBlocks All blocks that are outside of the ship within one block of its bounding box. This includes ship hull blocks that are exterior-waterloggable
     * @param outerHull All blocks that are part of the outer hull of the ship
     * @param innerShip All that are part of the ship's interior (including air blocks) and/or are not on the outside of the ship
     */
    private void scanHull(Set<Block> outsideBlocks, Set<Block> outerHull, Set<Block> innerShip){
        Set<Block> allBlocks = new HashSet<>();
        Set<Block> nextLayer = new HashSet<>();
        int x1,y1,z1,x2,y2,z2;
        x1 = y1 = z1 = Integer.MAX_VALUE;
        x2 = y2 = z2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            x1 = Math.min(x1, block.getX());
            y1 = Math.min(y1, block.getY());
            z1 = Math.min(z1, block.getZ());
            x2 = Math.max(x2, block.getX());
            y2 = Math.max(y2, block.getY());
            z2 = Math.max(z2, block.getZ());
        }
        for(int x = x1-1; x<=x2+1; x++){
            for(int y = y1-1; y<=y2+1; y++){
                for(int z = z1-1; z<=z2+1; z++){
                    allBlocks.add(world.getBlockAt(x,y,z));
                    if(x==x1-1||y==y1-1||z==z1-1||x==x2+1||y==y2+1||z==z2+1){
                        nextLayer.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        while(!nextLayer.isEmpty()){
            Set<Block> thisLayer = new HashSet<>(nextLayer);
            outsideBlocks.addAll(nextLayer);
            nextLayer.clear();
            for(Block b : thisLayer){
                for(int x = -1; x<=1; x++){
                    for(int y = -1; y<=1; y++){
                        for(int z = -1; z<=1; z++){
                            if(Math.abs(x)+Math.abs(y)+Math.abs(z)>1)continue;
                            Block newBlock = b.getRelative(x,y,z);
                            if(thisLayer.contains(newBlock)||outsideBlocks.contains(newBlock)||(newBlock.getX()<x1||newBlock.getY()<y1||newBlock.getZ()<z1||newBlock.getX()>x2||newBlock.getY()>y2||newBlock.getZ()>z2)||nextLayer.contains(newBlock)){
                                continue;
                            }
                            if(blocks.contains(b)||blocks.contains(newBlock)){
                                if(isFluidConnected(b,newBlock)){
                                    nextLayer.add(newBlock);
                                    if(blocks.contains(b))outerHull.add(newBlock);
                                }
                                if(!blocks.contains(b))outerHull.add(newBlock);
                            }else{
                                nextLayer.add(newBlock);
                            }
                        }
                    }
                }
            }
        }
        innerShip.addAll(allBlocks);
        innerShip.removeAll(outsideBlocks);
        innerShip.removeAll(outerHull);
    }
    /**
     * Checks if fluid can flow from one block to another based on block shape (stairs, slabs, etc.)
     * @param a one block
     * @param b another
     * @return true if fluid can flow from one block to another
     */
    private boolean isFluidConnected(Block a, Block b){
        BlockFace face = a.getFace(b);
        int A = isFluidConnected(a, face);
        int B = isFluidConnected(b, face.getOppositeFace());
        if(A+B>=3)return true;
        if(A==0||B==0)return false;
        boolean anw = false;
        boolean ane = false;
        boolean asw = false;
        boolean ase = false;
        boolean bnw = false;
        boolean bne = false;
        boolean bsw = false;
        boolean bse = false;
        if(a.getBlockData() instanceof Slab){
            Slab s = (Slab) a.getBlockData();
            anw = ane = s.getType()==Slab.Type.TOP;
            asw = ase = s.getType()==Slab.Type.BOTTOM;
        }
        if(b.getBlockData() instanceof Slab){
            Slab s = (Slab) b.getBlockData();
            bnw = bne = s.getType()==Slab.Type.TOP;
            bsw = bse = s.getType()==Slab.Type.BOTTOM;
        }
        if(a.getBlockData() instanceof Stairs){
            Stairs s = (Stairs)a.getBlockData();
            if(face==BlockFace.UP||face==BlockFace.DOWN){
                switch(s.getFacing()){
                    case NORTH:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                anw = ane = asw = true;
                                break;
                            case INNER_RIGHT:
                                anw = ane = ase = true;
                                break;
                            case OUTER_LEFT:
                                anw = true;
                                break;
                            case OUTER_RIGHT:
                                ane = true;
                                break;
                            case STRAIGHT:
                                anw = ane = true;
                                break;
                        }
                        break;
                    case SOUTH:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                asw = ase = ane = true;
                                break;
                            case INNER_RIGHT:
                                anw = asw = ase = true;
                                break;
                            case OUTER_LEFT:
                                ase = true;
                                break;
                            case OUTER_RIGHT:
                                asw = true;
                                break;
                            case STRAIGHT:
                                asw = ase = true;
                                break;
                        }
                        break;
                    case EAST:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                anw = ane = ase = true;
                                break;
                            case INNER_RIGHT:
                                asw = ase = ane = true;
                                break;
                            case OUTER_LEFT:
                                ane = true;
                                break;
                            case OUTER_RIGHT:
                                ase = true;
                                break;
                            case STRAIGHT:
                                ane = ase = true;
                                break;
                        }
                        break;
                    case WEST:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                anw = asw = ase = true;
                                break;
                            case INNER_RIGHT:
                                ane = anw = asw = true;
                                break;
                            case OUTER_LEFT:
                                asw = true;
                                break;
                            case OUTER_RIGHT:
                                anw = true;
                                break;
                            case STRAIGHT:
                                anw = asw = true;
                                break;
                        }
                        break;
                }
            }else{
                if(s.getHalf()==Bisected.Half.TOP){
                    anw = ane = true;
                }
                if(s.getHalf()==Bisected.Half.TOP){
                    asw = ase = true;
                }
                int side = 0;//0 is same, then clockwise
                switch(face){
                    case NORTH:
                        side = 0;
                        break;
                    case EAST:
                        side = 1;
                        break;
                    case SOUTH:
                        side = 2;
                        break;
                    case WEST:
                        side = 3;
                        break;
                }
                switch(s.getFacing()){
                    case NORTH:
                        break;
                    case EAST:
                        side--;
                        break;
                    case WEST:
                        side--;
                        break;
                    case SOUTH:
                        side--;
                        break;
                }
                if(side<0)side+=4;
                switch(s.getShape()){
                    case INNER_LEFT:
                        if(side==1)anw=true;
                        if(side==2)ane=true;
                        break;
                    case INNER_RIGHT:
                        if(side==2)anw=true;
                        if(side==3)ane=true;
                        break;
                    case OUTER_LEFT:
                        if(side==0)anw=true;
                        if(side==3)ane=true;
                        break;
                    case OUTER_RIGHT:
                        if(side==1)anw=true;
                        if(side==0)ane=true;
                        break;
                    case STRAIGHT:
                        if(side==1)anw=true;
                        if(side==3)ane=true;    
                        break;
                }
            }
        }
        return !((anw||bnw)&&(ane||bne)&&(asw||bsw)&&(ase||bse));
    }
    /**
     * Checks if fluid can flow from a block in a certain direction based on block shape (stairs, slabs, etc.)
     * @param a one block
     * @param face the direction fluid is trying to flow
     * @return no clue honestly
     */
    private int isFluidConnected(Block b, BlockFace face){
        switch(b.getType()){
            case BUBBLE_COLUMN:
            case CHEST:
            case TRAPPED_CHEST:
            case CONDUIT:
            case TUBE_CORAL:
            case TUBE_CORAL_FAN:
            case TUBE_CORAL_WALL_FAN:
            case BRAIN_CORAL:
            case BRAIN_CORAL_FAN:
            case BRAIN_CORAL_WALL_FAN:
            case BUBBLE_CORAL:
            case BUBBLE_CORAL_FAN:
            case BUBBLE_CORAL_WALL_FAN:
            case FIRE_CORAL:
            case FIRE_CORAL_FAN:
            case FIRE_CORAL_WALL_FAN:
            case HORN_CORAL:
            case HORN_CORAL_FAN:
            case HORN_CORAL_WALL_FAN:
            case DEAD_TUBE_CORAL:
            case DEAD_TUBE_CORAL_FAN:
            case DEAD_TUBE_CORAL_WALL_FAN:
            case DEAD_BRAIN_CORAL:
            case DEAD_BRAIN_CORAL_FAN:
            case DEAD_BRAIN_CORAL_WALL_FAN:
            case DEAD_BUBBLE_CORAL:
            case DEAD_BUBBLE_CORAL_FAN:
            case DEAD_BUBBLE_CORAL_WALL_FAN:
            case DEAD_FIRE_CORAL:
            case DEAD_FIRE_CORAL_FAN:
            case DEAD_FIRE_CORAL_WALL_FAN:
            case DEAD_HORN_CORAL:
            case DEAD_HORN_CORAL_FAN:
            case DEAD_HORN_CORAL_WALL_FAN:
            case ENDER_CHEST:
            case OAK_FENCE:
            case BIRCH_FENCE:
            case SPRUCE_FENCE:
            case JUNGLE_FENCE:
            case ACACIA_FENCE:
            case DARK_OAK_FENCE:
            case NETHER_BRICK_FENCE:
            case IRON_BARS:
            case KELP:
            case LADDER:
            case SEAGRASS:
            case OAK_SIGN:
            case OAK_WALL_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_WALL_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_SIGN:
            case ACACIA_SIGN:
            case ACACIA_WALL_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_SIGN:
            case TALL_SEAGRASS:
            case COBBLESTONE_WALL:
            case MOSSY_COBBLESTONE_WALL:
            case BRICK_WALL:
            case PRISMARINE_WALL:
            case RED_SANDSTONE_WALL:
            case MOSSY_STONE_BRICK_WALL:
            case GRANITE_WALL:
            case STONE_BRICK_WALL:
            case NETHER_BRICK_WALL:
            case ANDESITE_WALL:
            case RED_NETHER_BRICK_WALL:
            case SANDSTONE_WALL:
            case END_STONE_BRICK_WALL:
            case DIORITE_WALL:
                return 2;
            case CAMPFIRE://insulated on the bottom only
                if(face==BlockFace.DOWN)return 0;
                return 2;
            case GLASS_PANE://depends on blockstate..???
                MultipleFacing pane = (MultipleFacing)b.getBlockData();
                switch(face){
                    case EAST:
                    case WEST:
                        if(pane.hasFace(BlockFace.NORTH)||pane.hasFace(BlockFace.SOUTH))return 2;
                        return 0;
                    case NORTH:
                    case SOUTH:
                        if(pane.hasFace(BlockFace.EAST)||pane.hasFace(BlockFace.WEST))return 2;
                        return 0;
                }
                return 0;
            case OAK_SLAB:
            case SPRUCE_SLAB:
            case BIRCH_SLAB:
            case JUNGLE_SLAB:
            case ACACIA_SLAB:
            case DARK_OAK_SLAB:
            case STONE_SLAB:
            case SMOOTH_STONE_SLAB:
            case SANDSTONE_SLAB:
            case CUT_SANDSTONE_SLAB:
            case PETRIFIED_OAK_SLAB:
            case COBBLESTONE_SLAB:
            case BRICK_SLAB:
            case STONE_BRICK_SLAB:
            case NETHER_BRICK_SLAB:
            case QUARTZ_SLAB:
            case RED_SANDSTONE_SLAB:
            case CUT_RED_SANDSTONE_SLAB:
            case PURPUR_SLAB:
            case PRISMARINE_SLAB:
            case PRISMARINE_BRICK_SLAB:
            case DARK_PRISMARINE_SLAB:
            case POLISHED_GRANITE_SLAB:
            case SMOOTH_RED_SANDSTONE_SLAB:
            case MOSSY_STONE_BRICK_SLAB:
            case POLISHED_DIORITE_SLAB:
            case MOSSY_COBBLESTONE_SLAB:
            case END_STONE_BRICK_SLAB:
            case SMOOTH_SANDSTONE_SLAB:
            case SMOOTH_QUARTZ_SLAB:
            case GRANITE_SLAB:
            case ANDESITE_SLAB:
            case RED_NETHER_BRICK_SLAB:
            case POLISHED_ANDESITE_SLAB:
            case DIORITE_SLAB:
                Slab slab = (Slab)b.getBlockData();
                if(slab.getType()==Slab.Type.DOUBLE)return 0;
                if(face==BlockFace.UP){
                    if(slab.getType()==Slab.Type.TOP)return 0;
                    if(slab.getType()==Slab.Type.BOTTOM)return 2;
                }
                if(face==BlockFace.DOWN){
                    if(slab.getType()==Slab.Type.BOTTOM)return 0;
                    if(slab.getType()==Slab.Type.TOP)return 2;
                }
                return 1;
            case PURPUR_STAIRS:
            case OAK_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case STONE_BRICK_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_STAIRS:
            case BIRCH_STAIRS:
            case JUNGLE_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case PRISMARINE_STAIRS:
            case PRISMARINE_BRICK_STAIRS:
            case DARK_PRISMARINE_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case POLISHED_GRANITE_STAIRS:
            case SMOOTH_RED_SANDSTONE_STAIRS:
            case MOSSY_STONE_BRICK_STAIRS:
            case POLISHED_DIORITE_STAIRS:
            case MOSSY_COBBLESTONE_STAIRS:
            case END_STONE_BRICK_STAIRS:
            case STONE_STAIRS:
            case SMOOTH_SANDSTONE_STAIRS:
            case SMOOTH_QUARTZ_STAIRS:
            case GRANITE_STAIRS:
            case ANDESITE_STAIRS:
            case RED_NETHER_BRICK_STAIRS:
            case POLISHED_ANDESITE_STAIRS:
            case DIORITE_STAIRS:
                Stairs stairs = (Stairs)b.getBlockData();
                if(face==BlockFace.UP){
                    return stairs.getHalf()==Bisected.Half.TOP?0:1;
                }
                if(face==BlockFace.DOWN){
                    return stairs.getHalf()==Bisected.Half.BOTTOM?0:1;
                }
                if(stairs.getShape()==Stairs.Shape.OUTER_LEFT||stairs.getShape()==Stairs.Shape.OUTER_RIGHT)return 1;
                if(stairs.getShape()==Stairs.Shape.STRAIGHT)return stairs.getFacing()==face?0:1;
                if(stairs.getShape()==Stairs.Shape.INNER_LEFT){
                    switch(stairs.getFacing()){
                        case NORTH:
                            return face==BlockFace.NORTH||face==BlockFace.WEST?0:1;
                        case EAST:
                            return face==BlockFace.EAST||face==BlockFace.NORTH?0:1;
                        case SOUTH:
                            return face==BlockFace.SOUTH||face==BlockFace.EAST?0:1;
                        case WEST:
                            return face==BlockFace.WEST||face==BlockFace.SOUTH?0:1;
                    }
                }
                if(stairs.getShape()==Stairs.Shape.INNER_RIGHT){
                    switch(stairs.getFacing()){
                        case NORTH:
                            return face==BlockFace.NORTH||face==BlockFace.EAST?0:1;
                        case EAST:
                            return face==BlockFace.EAST||face==BlockFace.SOUTH?0:1;
                        case SOUTH:
                            return face==BlockFace.SOUTH||face==BlockFace.WEST?0:1;
                        case WEST:
                            return face==BlockFace.WEST||face==BlockFace.NORTH?0:1;
                    }
                }
                return 1;
            case OAK_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case IRON_TRAPDOOR:
                TrapDoor door = (TrapDoor)b.getBlockData();
                if(face==BlockFace.UP&&!door.isOpen()&&door.getHalf()==Bisected.Half.TOP)return 0;
                if(face==BlockFace.DOWN&&!door.isOpen()&&door.getHalf()==Bisected.Half.BOTTOM)return 0;
                if(door.isOpen()&&face==door.getFacing().getOppositeFace())return 0;
                return 2;
        }
        return 0;
    }
    public Location getOrigin(){
        return getBoundingBox().getCenter().toLocation(world);
    }
    public BoundingBox getBoundingBox(){
        if(bbox==null)calculateBoundingBox();
        return new BoundingBox().copy(bbox);
    }
    private void calculateBoundingBox(){
        if(blocks.isEmpty())bbox = new BoundingBox(0, 0, 0, 0, 0, 0);
        int x1,y1,z1,x2,y2,z2;
        x1 = y1 = z1 = Integer.MAX_VALUE;
        x2 = y2 = z2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            x1 = Math.min(x1, block.getX());
            y1 = Math.min(y1, block.getY());
            z1 = Math.min(z1, block.getZ());
            x2 = Math.max(x2, block.getX());
            y2 = Math.max(y2, block.getY());
            z2 = Math.max(z2, block.getZ());
        }
        bbox =  new BoundingBox(x1, y1, z1, x2+1, y2+1, z2+1);
    }
    public boolean move(int x, int y, int z, List<Medium> mediums){
        aeronautics.debug(pilots, "Moving "+x+" "+y+" "+z);
        ArrayList<BlockMovement> movements = new ArrayList<>();
        aeronautics.debug(pilots, "Compiling BlockMovements");
        for(Block block : getMovableBlocks()){
            movements.add(new BlockMovement(block.getLocation(), block.getRelative(x,y,z).getLocation()));
        }
        aeronautics.debug(pilots, "Compiled BlockMovements");
        return move(blocks, movements, mediums)!=null;
    }
    public Iterable<Entity> move(Collection<Block> blocks, Collection<BlockMovement> movements, List<Medium> mediums){//TODO mediums
        clearMediumsCache();
        if(blocks.isEmpty())return null;
        boolean allowUnderwater = false;
        for(Medium medium : mediums)if(medium.blocks.contains(Material.WATER))allowUnderwater = true;
        boolean underwaterMove = isUnderwater(false)&&allowUnderwater;//used for waterlogged calculations
        int waterLevel = 0;//used for waterlogged calculations
        if(underwaterMove)waterLevel = getWaterLevel();
        HashMap<Entity, Location> entityMovements = new HashMap<>();
        HashMap<OfflinePlayer, Location> spawnMovements = new HashMap<>();
        int entityRotation = 0;
        for(Entity entity : world.getNearbyEntities(getBoundingBox().expand(BlockFace.UP, type.onBoardThreshold))){
            Block b = world.getBlockAt(entity.getLocation());
            for(int i = 0; i<2; i++){
                if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN){
                    b = b.getRelative(BlockFace.DOWN);
                }
            }
            if(blocks.contains(b)){
                for(BlockMovement m : movements){
                    if(m.from.equals(b.getLocation())){
                        Location diff = m.to.clone().subtract(m.from.clone());
                        entityMovements.put(entity, rotate(entity.getLocation().clone().add(diff), m.to.clone().add(.5, 0, .5), m.rotation));
                        entityRotation = m.rotation;
                        break;
                    }
                }
            }
        }
        for(OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()){
            if(p.getBedSpawnLocation()==null)continue;
            if(getBoundingBox().expand(BlockFace.UP, 2).contains(p.getBedSpawnLocation().toVector())){
                Block b = world.getBlockAt(p.getBedSpawnLocation());
                for(int i = 0; i<2; i++){
                    if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN){
                        b = b.getRelative(BlockFace.DOWN);
                    }
                }
                if(blocks.contains(b)){
                    for(BlockMovement m : movements){ 
                       if(m.from.equals(b.getLocation())){
                            Location diff = m.to.clone().subtract(m.from.clone());
                            spawnMovements.put(p, rotate(p.getBedSpawnLocation().clone().add(diff), m.to.clone().add(.5, 0, .5), m.rotation));
                            entityRotation = m.rotation;
                            break;
                        }
                    }
                }
            }
        }
        aeronautics.debug(pilots, "Checking Collisions");
        for(Iterator<BlockMovement> it = movements.iterator(); it.hasNext();){
            BlockMovement movement = it.next();
            Block newLocation = world.getBlockAt(movement.to);
            if(!world.getBlockAt(movement.from).getChunk().isLoaded()||!newLocation.getChunk().isLoaded()){
                notifyPilots("Ship not loaded! ("+newLocation.getX()+","+newLocation.getY()+","+newLocation.getZ()+")", newLocation.getLocation(), Sound.ENTITY_WITHER_AMBIENT, .5f);
                for(CraftEngine e : engines){
                    e.getEngine().onUnload(e);
                }
                return null;
            }
            if(!blocks.contains(newLocation)){
                boolean collide = true;
                var type = newLocation.getType();
                for(Medium medium : mediums){
                    if(medium.blocks.contains(type)){
                        collide = false;
                        break;
                    }
                }
                if(collide){
                    switch(this.type.collisionHandler.collide(this, blocks, movements, movement, mediums)){
                        case CANCEL -> {
                            return null;
                        }
                        case BREAK -> {
                            removeBlock(null, newLocation, true);
                            it.remove();
                        }
                        case BREAK_OTHER -> {
                            newLocation.setType(Material.AIR);//TODO what about other crafts?
                        }
                    }
                }
            }
        }
        for(CraftEngine e : engines){
            e.getEngine().onMoved(e);
        }
        aeronautics.debug(pilots, "Prepared Move");
        ArrayList<BlockChange> changes = new ArrayList<>();
        ArrayList<Block> newBlocks = new ArrayList<>();
        HashSet<Block> blox = new HashSet<>(blocks);//blocks that will be reset //TODO what about the interior?
        for(BlockMovement movement : movements){ 
            Block movesFrom = world.getBlockAt(movement.from);
            Block movesTo = world.getBlockAt(movement.to);
            if(movesTo.getType()!=movesFrom.getType()||!Aeronautics.isInert(movesFrom.getType())){
                changes.add(new BlockChange(movesTo, movesFrom, movement.rotation));
            }
            newBlocks.add(movesTo);
            blox.remove(movesTo);
        }
        aeronautics.debug(pilots, "Mid-compiled BlockChanges");
        finishCompilingBlockChanges(mediums, changes, blox);
        Collections.sort(changes);
        aeronautics.debug(pilots, "Sorted BlockChanges");
//        int created = 0;
//        int destroyed = 0;
//        for(BlockChange change : changes){
//            if(change.type==Material.AIR&&change.block.getType()!=Material.AIR)destroyed++;
//            if(change.type!=Material.AIR&&change.block.getType()==Material.AIR)created++;
//            if(change.type==Material.WATER&&change.block.getType()!=Material.WATER)destroyed++;
//            if(change.type!=Material.WATER&&change.block.getType()==Material.WATER)created++;
//        }
//        if(created-destroyed!=0){
////            criticalError(81037, "NET CHANGE IS NOT EQUAL TO 0!"); 
////            return null;
//        }
        ArrayList<Block> replacingBlocks = new ArrayList<>(newBlocks);
        replacingBlocks.removeAll(this.blocks);
        aeronautics.debug(pilots, "Prepared WAS blocks");
        moving = true;
        replacingBlocks.forEach((t)->{
            was.put(t, t.getState());
        });
        aeronautics.debug(pilots, "Stored WAS blocks");
        for(Block block : blocks){
            if(Aeronautics.blocksThatPop.contains(block.getType())){
                block.setType(Material.AIR, false);
            }
        }
        aeronautics.debug(pilots, "Airified popping blocks");
        for(BlockChange change : changes){
            if(!Aeronautics.blocksThatPop.contains(change.type))change.change();
        }
        aeronautics.debug(pilots, "Changed non-popping blocks");
        for(BlockChange change : changes){
            if(Aeronautics.blocksThatPop.contains(change.type)){
                change.change();
            }
        }
        aeronautics.debug(pilots, "Changed popping blocks");
        for(BlockMovement movement : movements){
            movement.move1(this);
        }
        for(BlockMovement movement : movements){
            movement.move2(this);
        }
        HashMap<Multiblock, Block> moveMultiblocks = new HashMap<>();
        for(BlockMovement movement : movements){
            for(Multiblock m : multiblocks){
                if(m.origin.equals(world.getBlockAt(movement.from))){
                    aeronautics.debug(pilots, "Moving multiblock");
                    moveMultiblocks.put(m, world.getBlockAt(movement.to));
                }//TODO optimize a bit?
            }
        }
        for(Multiblock multiblock : moveMultiblocks.keySet()){
            multiblock.origin = moveMultiblocks.get(multiblock);
        }
        for(BlockMovement movement : movements){
            movement.callEvent(this);
        }
        aeronautics.debug(pilots, "Moved Ship Blocks");
        moving = false;
        this.blocks.removeAll(blocks);
        this.blocks.addAll(newBlocks);
        if(underwaterMove){
            Set<Block> outsideBlocks = new HashSet<>();
            Set<Block> outerHull = new HashSet<>();
            Set<Block> innerShip = new HashSet<>();
            scanHull(outsideBlocks, outerHull, innerShip);
            for(Block b : outerHull){
                if(b.getBlockData() instanceof Waterlogged){
                    Waterlogged l = (Waterlogged) b.getBlockData();
                    l.setWaterlogged(b.getY()<=waterLevel);
                    b.setBlockData(l);
                }
            }
            innerShip.removeAll(newBlocks);
            for(Block b : innerShip){
                if(b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN)b.setType(Material.AIR);
                if(b.getBlockData() instanceof Waterlogged){
                    Waterlogged l = (Waterlogged) b.getBlockData();
                    l.setWaterlogged(false);
                    b.setBlockData(l);
                }
            }
            aeronautics.debug(pilots, "Recalculated Waterlogged blocks");
        }
        calculateBoundingBox();
        for(Entity e : entityMovements.keySet()){
            Vanillify.smoothTeleport(e, entityMovements.get(e));
            if(e instanceof Hanging){
                Hanging f = (Hanging) e;
                int amount = entityRotation;
                while(amount>0){
                    amount--;
                    switch(f.getFacing()){
                        case NORTH:
                            f.setFacingDirection(BlockFace.EAST);
                            break;
                        case EAST:
                            f.setFacingDirection(BlockFace.SOUTH);
                            break;
                        case SOUTH:
                            f.setFacingDirection(BlockFace.WEST);
                            break;
                        case WEST:
                            f.setFacingDirection(BlockFace.UP);
                            break;
                    }
                }
            }
        }
        for(OfflinePlayer p : spawnMovements.keySet()){
            p.getPlayer().setBedSpawnLocation(spawnMovements.get(p));
        }
        aeronautics.debug(pilots, "Moved Players");
        for(Player player : pilots){
            if(player.getLocation().distance(getOrigin())>500){
                actionbarIfOnBoard(player, "Ship moved to ("+getOrigin().getBlockX()+", "+getOrigin().getBlockY()+", "+getOrigin().getBlockZ()+")");
            }
        }
        rescanSigns();
        updateSigns();
        return entityMovements.keySet();
    }
    public Location rotate(Location location, Location origin){
        location.subtract(origin);
        double x = -location.getZ();
        double z = location.getX();
        location.setX(x);
        location.setZ(z);
        location.add(origin);
        return location;
    }
    public Location rotate(Location location, Location origin, int amount){
        while(amount>=4)amount-=4;
        while(amount<0)amount+=4;
        if(amount==0)return location;
        for(int i = 0; i<amount; i++){
            location = rotate(location, origin);
        }
        return location;
    }
    public static void rotateBlock(BlockData data, int rotation){
        while(rotation>=4)rotation-=4;
        while(rotation<0)rotation+=4;
        if(rotation==0)return;
        for(int i = 0; i<rotation; i++)rotateBlock(data);
    }
    public static void rotateBlock(BlockData data){
        if(data instanceof RedstoneWire){
            RedstoneWire wire = (RedstoneWire)data;
            RedstoneWire.Connection n = wire.getFace(BlockFace.NORTH);
            RedstoneWire.Connection e = wire.getFace(BlockFace.EAST);
            RedstoneWire.Connection s = wire.getFace(BlockFace.SOUTH);
            RedstoneWire.Connection w = wire.getFace(BlockFace.WEST);
            wire.setFace(BlockFace.NORTH, w);
            wire.setFace(BlockFace.EAST, n);
            wire.setFace(BlockFace.SOUTH, e);
            wire.setFace(BlockFace.WEST, s);
        }
        if(data instanceof Directional){
            Directional d = (Directional) data;
            switch(d.getFacing()){
                case NORTH:
                    d.setFacing(BlockFace.EAST);
                    break;
                case EAST:
                    d.setFacing(BlockFace.SOUTH);
                    break;
                case SOUTH:
                    d.setFacing(BlockFace.WEST);
                    break;
                case WEST:
                    d.setFacing(BlockFace.NORTH);
                    break;
            }
        }
        if(data instanceof MultipleFacing){
            MultipleFacing m = (MultipleFacing) data;
            boolean n = m.hasFace(BlockFace.NORTH);
            boolean e = m.hasFace(BlockFace.EAST);
            boolean s = m.hasFace(BlockFace.SOUTH);
            boolean w = m.hasFace(BlockFace.WEST);
            m.setFace(BlockFace.NORTH, w);
            m.setFace(BlockFace.EAST, n);
            m.setFace(BlockFace.SOUTH, e);
            m.setFace(BlockFace.WEST, s);
        }
        if(data instanceof Orientable){
            Orientable o = (Orientable) data;
            switch(o.getAxis()){
                case X:
                    o.setAxis(Axis.Z);
                    break;
                case Z:
                    o.setAxis(Axis.X);
                    break;
            }
        }
        if(data instanceof Rail){
            Rail r = (Rail) data;
            switch(r.getShape()){
                case ASCENDING_EAST:
                    r.setShape(Rail.Shape.ASCENDING_SOUTH);
                    break;
                case ASCENDING_NORTH:
                    r.setShape(Rail.Shape.ASCENDING_EAST);
                    break;
                case ASCENDING_SOUTH:
                    r.setShape(Rail.Shape.ASCENDING_WEST);
                    break;
                case ASCENDING_WEST:
                    r.setShape(Rail.Shape.ASCENDING_NORTH);
                    break;
                case EAST_WEST:
                    r.setShape(Rail.Shape.NORTH_SOUTH);
                    break;
                case NORTH_EAST:
                    r.setShape(Rail.Shape.SOUTH_EAST);
                    break;
                case NORTH_SOUTH:
                    r.setShape(Rail.Shape.EAST_WEST);
                    break;
                case NORTH_WEST:
                    r.setShape(Rail.Shape.NORTH_EAST);
                    break;
                case SOUTH_EAST:
                    r.setShape(Rail.Shape.SOUTH_WEST);
                    break;
                case SOUTH_WEST:
                    r.setShape(Rail.Shape.NORTH_WEST);
                    break;
            }
        }
        if(data instanceof Rotatable){
            Rotatable r = (Rotatable) data;
            switch(r.getRotation()){
                case NORTH:
                    r.setRotation(BlockFace.EAST);
                    break;
                case EAST:
                    r.setRotation(BlockFace.SOUTH);
                    break;
                case SOUTH:
                    r.setRotation(BlockFace.WEST);
                    break;
                case WEST:
                    r.setRotation(BlockFace.NORTH);
                    break;
                case EAST_NORTH_EAST:
                    r.setRotation(BlockFace.SOUTH_SOUTH_EAST);
                    break;
                case EAST_SOUTH_EAST:
                    r.setRotation(BlockFace.SOUTH_SOUTH_WEST);
                    break;
                case NORTH_EAST:
                    r.setRotation(BlockFace.SOUTH_EAST);
                    break;
                case NORTH_NORTH_EAST:
                    r.setRotation(BlockFace.EAST_SOUTH_EAST);
                    break;
                case NORTH_NORTH_WEST:
                    r.setRotation(BlockFace.EAST_NORTH_EAST);
                    break;
                case NORTH_WEST:
                    r.setRotation(BlockFace.NORTH_EAST);
                    break;
                case SOUTH_EAST:
                    r.setRotation(BlockFace.SOUTH_WEST);
                    break;
                case SOUTH_SOUTH_EAST:
                    r.setRotation(BlockFace.WEST_SOUTH_WEST);
                    break;
                case SOUTH_SOUTH_WEST:
                    r.setRotation(BlockFace.WEST_NORTH_WEST);
                    break;
                case SOUTH_WEST:
                    r.setRotation(BlockFace.NORTH_WEST);
                    break;
                case WEST_NORTH_WEST:
                    r.setRotation(BlockFace.NORTH_NORTH_EAST);
                    break;
                case WEST_SOUTH_WEST:
                    r.setRotation(BlockFace.NORTH_NORTH_WEST);
                    break;
            }
        }
    }
    public void startSinking(){
        type.sinkHandler.onStartSinking(this);
        sinking = true;
        pilots.clear();
    }
    public void notify(Player player, String message, Location loc, Sound sound, float pitch){
        if(player==null)return;
        if(!isOnBoard(player))return;
        if(message!=null)player.sendMessage(message);
        if(sound!=null)player.playSound(loc==null?player.getLocation():loc, sound, SoundCategory.MASTER, 100, pitch);
    }
    public void notify(Iterable<Player> players, String message, Location loc, Sound sound, float pitch){
        for(Player p : players)notify(p, message, loc, sound, pitch);
    }
    public void notifyPilots(String message, Location loc, Sound sound, float pitch){
        notify(pilots, message, loc, sound, pitch);
    }
    public void notifyPilots(String message, Sound sound, float pitch){
        notifyPilots(message, null, sound, pitch);
    }
    public void notifyPilots(String message){
        notifyPilots(message, null, 0);
    }
    public void notifyCrew(String message, Location loc, Sound sound, float pitch){
        notify(getCrew(), message, loc, sound, pitch);
    }
    public void notifyCrew(String message, Sound sound, float pitch){
        notifyCrew(message, null, sound, pitch);
    }
    public void notifyCrew(String message){
        notifyCrew(message, null, 0);
    }
    public HashSet<Player> getCrew(){
        HashSet<Player> crew = new HashSet<>();
        crew.addAll(pilots);
        for(String s : this.crew){
            for(Player p : world.getPlayers()){
                if(p.getName().equals(s)){
                    if(isOnBoard(p))crew.add(p);
                }
            }
        }
        return crew;
    }
    public boolean isOnBoard(Entity entity){
        Block b = world.getBlockAt(entity.getLocation());
        for(int i = 0; i<type.onBoardThreshold; i++){
            if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN){
                b = b.getRelative(0, -1, 0);
            }
        }
        if(contains(b)){
            return true;
        }
        return false;
    }
    private HashSet<Block> getMovableBlocks(){
        HashSet<Block> movable = new HashSet<>(blocks);
        aeronautics.debug(pilots, "Getting movable blocks");
        //TODO moving other ships too
        aeronautics.debug(pilots, "Got Movable blocks");
        return movable;
    }
    public int getWaterLevel(){
        Set<Block> outsideBlocks = new HashSet<>();
        Set<Block> outerHull = new HashSet<>();
        Set<Block> innerShip = new HashSet<>();
        scanHull(outsideBlocks, outerHull, innerShip);
        int max = 0;
        for(Block b : outsideBlocks){
            if(b.getType()==Material.WATER||((b.getBlockData() instanceof Waterlogged)&&((Waterlogged)b.getBlockData()).isWaterlogged()))max = Math.max(b.getY(), max);
        }
        return max;
    }
    /**
     * Adds block changes to reset blocks to the medium material (where the ship is moving out of)
     * @param mediums a list of mediums the ship may pass through
     * @param changes a list to add block changes to
     * @param blox a list of blocks that are to be reset
     */
    private void finishCompilingBlockChanges(List<Medium> mediums, ArrayList<BlockChange> changes, Set<Block> blox){
        int A = 0;
        int B = 0;
        for(Block block : blox){
            if(was.containsKey(block)){
                var itWas = was.get(block);
                changes.add(new BlockChange(block, itWas.getType(), itWas.getBlockData(), itWas));
                was.remove(block);
                A++;
            }else{
                changes.add(new BlockChange(block, Material.AIR, null, null).causePhysicsUpdate());
                B++;
            }
        }
        aeronautics.debug(pilots, "Compiled BlockChanges "+A+" "+B);
    }
    public boolean contains(Sign sign){
        return getSigns().contains(sign);
    }
    public boolean contains(Block block){
        if(block==null)return false;
        if(block.getState() instanceof Sign)return contains((Sign)block.getState());
        return blocks.contains(block);
    }
    public void event(Event event){
        for(CraftEngine engine : engines){
            engine.getEngine().event(engine, event);
        }
        for(CraftSpecial special : specials){
            special.getSpecial().event(special, event);
        }
    }
    public boolean removeBlock(Player player, Block block, boolean damage){
        if(moving)return false;
        if(damage)setMode(Mode.COMBAT);
        else setMode(Mode.CONSTRUCTION);
        if(updateHull(player, blocks.remove(block)?1:0, damage, block.getLocation())){
            rescanMultiblocks();
            return true;
        }else{
            blocks.add(block);
            return false;
        }
    }
    public boolean updateHull(Player player, int damage, boolean damaged, Location l){
        clearMediumsCache();
        rescanSigns();
        aeronautics.debug(pilots, "Updating hull; damage: "+damage+" "+damaged);
        for(Iterator<Block> it = blocks.iterator(); it.hasNext();){
            Block block = it.next();
            if(type.bannedBlocks.contains(block.getType())){
                it.remove();
                damage++;
            }
        }
        //TODO damage report?
        if(blocks.size()<type.minSize){
            if(damaged)startSinking();//TODO make this a special; move min size to the craft detector
            else{
                notifyBlockChange(player, "Craft too small!");
                return false;
            }
        }
        for(CraftEngine engine : engines){
            if(!engine.getEngine().canRemoveBlock(engine, player, damage, damaged, l))return false;
        }
        for(CraftSpecial special : specials){
            if(!special.getSpecial().removeBlock(special, player, damage, damaged, l))return false;
        }
        for(CraftEngine engine : engines){
            engine.getEngine().updateHull(engine, damage, damaged);
        }
        for(CraftSpecial special : specials){
            special.getSpecial().updateHull(special, damage, damaged);
        }
        if(l!=null&&damaged){
            notify(player, null, l, Sound.ENTITY_GENERIC_EXPLODE, 2);
        }
        bbox = null;
        rescanSigns();
        updateSigns();
        return true;
    }
    public boolean addBlock(Player player, Block block, boolean force){
        if(moving){
            return false;
        }
        clearMediumsCache();
        setMode(Mode.CONSTRUCTION);
        if(type.bannedBlocks.contains(block.getType())||!type.allowedBlocks.contains(block.getType())){
            notifyBlockChange(player, block.getType()+" is not allowed on this craft!");
            return false;
        }
        HashSet<Block> craft = new HashSet<>(blocks);
        craft.add(block);
        if(craft.size()>type.maxSize){
            notifyBlockChange(player, "Craft too large!");
            return false;
        }
        for(CraftEngine engine : engines){
            if(!engine.getEngine().canAddBlock(engine, player, block, force))return false;
        }
        for(CraftSpecial special : specials){
            if(!special.getSpecial().addBlock(special, player, block, force))return false;
        }
        blocks.add(block);
        for(CraftEngine engine : engines){
            engine.getEngine().updateHull(engine, -1, false);
        }
        for(CraftSpecial special : specials){
            special.getSpecial().updateHull(special, -1, false);
        }
        bbox = null;
        rescanSigns();
        updateSigns();
        scanForNewMultiblocks();
        return true;
    }
    public void notifyBlockChange(Player player, String message){
        if(player==null)notifyCrew(message);
        else player.sendMessage(message);
    }
    public boolean rotate(Location origin, int rotation, List<Medium> mediums){
        origin.setX(Math.round(origin.getX()));
        origin.setY(Math.round(origin.getY()));
        origin.setZ(Math.round(origin.getZ()));
        while(rotation>=4)rotation-=4;
        while(rotation<0)rotation+=4;
        ArrayList<BlockMovement> movements = new ArrayList<>();
        for(Block block : getMovableBlocks()){
            movements.add(new BlockMovement(block.getLocation(), rotate(block.getLocation(), origin, rotation), rotation));
        }
        Iterable<Entity> entities = move(blocks, movements, mediums);
        for(Multiblock multiblock : multiblocks){
            multiblock.onRotated(rotation);
        }
        if(entities==null)return false;
        for(Entity e : entities){
            Location l = e.getLocation();
            l.setYaw(l.getYaw()+90*rotation);
            Vanillify.smoothTeleport(e, l);
        }
        return true;
    }
    private void setMode(Mode mode){
        if(mode==Mode.CONSTRUCTION&&!type.hasConstructionMode)return;
        if(mode==Mode.COMBAT&&!type.hasCombatMode)return;
        if(mode.ordinal()>this.mode.ordinal())this.mode = mode;
        if(mode==this.mode)modeTimer = 0;
        if(mode.ordinal()<this.mode.ordinal()){
            switch(this.mode){
                case CONSTRUCTION:
                    if(modeTimer>type.constructionTimeout){
                        this.mode = mode;
                    }
                    break;
                case COMBAT:
                    if(modeTimer>type.combatTimeout){
                        this.mode = mode;
                    }
                    break;
            }
        }
    }
    private void rescanMultiblocks(){
        for(Iterator<Multiblock> it = multiblocks.iterator(); it.hasNext();){
            Multiblock multi = it.next();
            if(!multi.rescan()){
                multi.onDestroy();
                it.remove();
            }
        }
    }
    private void scanForNewMultiblocks(){
        BLOCKS:for(Block b : blocks){
            for(Multiblock m : multiblocks){
                if(m.origin.equals(b))continue BLOCKS;
            }
            for(Multiblock type : multiblockTypes){
                Multiblock multiblock = type.detect(this, b);
                if(multiblock!=null){
                    notifyPilots("Found new multiblock "+multiblock.toString()+" at "+multiblock.origin.toString()+"!");
                    aeronautics.debug(pilots, "Found new multiblock "+multiblock.toString()+" at "+multiblock.origin.toString()+"!");
                    multiblocks.add(multiblock);
                    multiblock.init();
                }
            }
        }
    }
    public boolean contains(Block... blocks){
        return contains(Arrays.asList(blocks));
    }
    public boolean contains(Collection<Block> blocks){
//        ArrayList<Block> list = new ArrayList<>(blocks);
//        list.removeAll(this.blocks);//is this actually faster than a bunch of .contains()?
//        return list.isEmpty();
        return this.blocks.containsAll(blocks);//y'know this is probably faster than that anyway
    }
    private void actionbarIfOnBoard(Player player, String string){
        if(isOnBoard(player))Vanillify.actionbar(player, string);
    }
    private void actionbarRawIfOnBoard(Player player, String string){
        if(isOnBoard(player))Vanillify.actionbarRaw(player, string);
    }
    public Aeronautics getAeronautics(){
        return aeronautics;
    }
    public BlockCache generateBlockCache(){
        return generateBlockCache(new BlockCache());
    }
    public <T extends BlockCache> T generateBlockCache(T cache){
        for(Block b : blocks){
            BlockState state = b.getState();
            if(state instanceof Container){
                cache.add(b, ((Container)state).getInventory().getContents());
            }else cache.add(b);
        }
        return cache;
    }
    public ArrayList<Multiblock> getMultiblocks(){
        return new ArrayList<>(multiblocks);//so you can't modify the list
    }
    //TODO calculate mediums based on where the actual volume of the ship is, rather than assuming uniform distribution based on surface area
    public MediumCache getCurrentMediums(){
        if(cachedCurrentMediums!=null)return cachedCurrentMediums;
        Set<Block> outsideBlocks = new HashSet<>();
        Set<Block> outerHull = new HashSet<>();
        Set<Block> innerShip = new HashSet<>();
        scanHull(outsideBlocks, outerHull, innerShip);
        MediumCache cache = new MediumCache();
        for(Block b : outsideBlocks){
            for(Medium m : type.mediums){
                if(m.blocks.contains(b.getType())){
                    cache.addBlock(m, b.getY());
                }
            }
        }
        cache.shipVolume = outerHull.size()+innerShip.size();
        return cachedCurrentMediums = cache.calculate();
    }
    public void clearMediumsCache(){
        cachedCurrentMediums = null;
    }
    public Multiblock getMultiblock(Block block){
        for(var mb : getMultiblocks()){
            if(mb.contains(block))return mb;
        }
        return null;
    }
    public static class BlockChange implements Comparable<BlockChange>{
        private final Material type;
        private final BlockData data;
        private final BlockState state;
        private final Block block;
        private boolean causesPhysicsUpdate = false;
        public BlockChange(Block block, Material type, BlockData data, BlockState state, int rotation){
            this.block = block;
            this.type = type;
            this.data = data;
            this.state = state;
            rotateBlock(data, rotation);
        }
        public BlockChange(Block block, Material type, BlockData data, BlockState state){
            this(block, type, data, state, 0);
        }
        public BlockChange(Block block, Block to){
            this(block, to.getType(), to.getBlockData(), to.getState(), 0);
        }
        public BlockChange(Block block, Block to, int rotation){
            this(block, to.getType(), to.getBlockData(), to.getState(), rotation);
        }
        public void change(){
            BlockState st = block.getState();
            if(st instanceof Container){
                ((Container)st).getSnapshotInventory().clear();
                st.update(false, causesPhysicsUpdate);
            }
            if(block.getType()!=type){
                block.setType(type, causesPhysicsUpdate);
            }
            if(Aeronautics.isInert(type))return;
            if(data!=null)block.setBlockData(data);
            BlockState newState = block.getState();
            if(state!=null){
                if(state instanceof org.bukkit.block.Beehive){
                    Beehive theNew = (Beehive)newState;
                    Beehive theOld = (Beehive)state;
//                    theNew.
                }
                if(state instanceof Container){
                    Container newContainer = (Container)newState;
                    Container oldContainer = (Container)state;
                    newContainer.getSnapshotInventory().setContents(oldContainer.getSnapshotInventory().getContents());
                }
                if(state instanceof Structure){
                    Structure newStructure = (Structure)newState;
                    Structure oldStructure = (Structure)state;
                    newStructure.setAuthor(oldStructure.getAuthor());
                    newStructure.setBoundingBoxVisible(oldStructure.isBoundingBoxVisible());
                    newStructure.setIgnoreEntities(oldStructure.isIgnoreEntities());
                    newStructure.setIntegrity(oldStructure.getIntegrity());
                    newStructure.setMetadata(oldStructure.getMetadata());
                    newStructure.setMirror(oldStructure.getMirror());
                    newStructure.setRelativePosition(oldStructure.getRelativePosition());
                    newStructure.setRotation(oldStructure.getRotation());
                    newStructure.setSeed(oldStructure.getSeed());
                    newStructure.setShowAir(oldStructure.isShowAir());
                    newStructure.setStructureName(oldStructure.getStructureName());
                    newStructure.setStructureSize(oldStructure.getStructureSize());
                    newStructure.setUsageMode(oldStructure.getUsageMode());
                }
                if(state instanceof Skull){
                    Skull newSkull = (Skull)newState;
                    Skull oldSkull = (Skull)state;
                    if(oldSkull.hasOwner())newSkull.setOwningPlayer(oldSkull.getOwningPlayer());
                }
                if(state instanceof Sign){
                    Sign newSign = (Sign)newState;
                    Sign oldSign = (Sign)state;
                    for(int i = 0; i<oldSign.getLines().length; i++){
                        newSign.setLine(i, oldSign.getLine(i));
                    }
                    newSign.setEditable(oldSign.isEditable());
                }
                if(state instanceof Lockable){
                    Lockable newLockable = (Lockable)newState;
                    Lockable oldLockable = (Lockable)state;
                    newLockable.setLock(oldLockable.getLock());
                }
                if(state instanceof Jukebox){
                    Jukebox newJukebox = (Jukebox)newState;
                    Jukebox oldJukebox = (Jukebox)state;
                    newJukebox.setPlaying(oldJukebox.getPlaying());
                    newJukebox.setRecord(oldJukebox.getRecord());
                }
                if(state instanceof Lectern){
                    Lectern newLectern = (Lectern)newState;
                    Lectern oldLectern = (Lectern)state;
                    newLectern.setPage(oldLectern.getPage());
                }
                if(state instanceof EndGateway){
                    EndGateway newEndGateway = (EndGateway)newState;
                    EndGateway oldEndGateway = (EndGateway)state;
                    newEndGateway.setAge(oldEndGateway.getAge());;
                    newEndGateway.setExactTeleport(oldEndGateway.isExactTeleport());
                    newEndGateway.setExitLocation(oldEndGateway.getExitLocation());
                }
                if(state instanceof Furnace){
                    Furnace newFurnace = (Furnace)newState;
                    Furnace oldFurnace = (Furnace)state;
                    newFurnace.setBurnTime(oldFurnace.getBurnTime());
                    newFurnace.setCookTime(oldFurnace.getCookTime());
                    newFurnace.setCookTimeTotal(oldFurnace.getCookTimeTotal());
                }
                if(state instanceof CreatureSpawner){
                    CreatureSpawner newCreatureSpawner = (CreatureSpawner)newState;
                    CreatureSpawner oldCreatureSpawner = (CreatureSpawner)state;
                    newCreatureSpawner.setDelay(oldCreatureSpawner.getDelay());
                    newCreatureSpawner.setMaxNearbyEntities(oldCreatureSpawner.getMaxNearbyEntities());
                    newCreatureSpawner.setMaxSpawnDelay(oldCreatureSpawner.getMaxSpawnDelay());
                    newCreatureSpawner.setMinSpawnDelay(oldCreatureSpawner.getMinSpawnDelay());
                    newCreatureSpawner.setRequiredPlayerRange(oldCreatureSpawner.getRequiredPlayerRange());
                    newCreatureSpawner.setSpawnCount(oldCreatureSpawner.getSpawnCount());
                    newCreatureSpawner.setSpawnRange(oldCreatureSpawner.getSpawnRange());
                    newCreatureSpawner.setSpawnedType(oldCreatureSpawner.getSpawnedType());
                }
                if(state instanceof Banner){
                    Banner newBanner = (Banner)newState;
                    Banner oldBanner = (Banner)state;
                    newBanner.setBaseColor(oldBanner.getBaseColor());
                    newBanner.setPatterns(oldBanner.getPatterns());
                }
                if(state instanceof Beacon){
                    Beacon newBeacon = (Beacon)newState;
                    Beacon oldBeacon = (Beacon)state;
                    newBeacon.setPrimaryEffect(oldBeacon.getPrimaryEffect().getType());
                    newBeacon.setSecondaryEffect(oldBeacon.getSecondaryEffect().getType());
                }
                if(state instanceof BrewingStand){
                    BrewingStand newBrewingStand = (BrewingStand)newState;
                    BrewingStand oldBrewingStand = (BrewingStand)state;
                    newBrewingStand.setBrewingTime(oldBrewingStand.getBrewingTime());
                    newBrewingStand.setFuelLevel(oldBrewingStand.getFuelLevel());
                }
                if(state instanceof Campfire){
                    Campfire newCampfire = (Campfire)newState;
                    Campfire oldCampfire = (Campfire)state;
                    for(int i = 0; i<oldCampfire.getSize(); i++){
                        newCampfire.setItem(i, oldCampfire.getItem(i));
                        newCampfire.setCookTime(i, oldCampfire.getCookTime(i));
                        newCampfire.setCookTimeTotal(i, oldCampfire.getCookTimeTotal(i));
                    }
                }
                if(state instanceof CommandBlock){
                    CommandBlock newCommandBlock = (CommandBlock)newState;
                    CommandBlock oldCommandBlock = (CommandBlock)state;
                    newCommandBlock.setCommand(oldCommandBlock.getCommand());
                    newCommandBlock.setName(oldCommandBlock.getName());
                }
                try{
                    newState.update(false, causesPhysicsUpdate);
                }catch(NullPointerException ex){
                    Bukkit.getServer().broadcastMessage("Failed to copy block state: "+block.getType().name());
                }
            }
        }
        @Override
        public int compareTo(BlockChange o){
            boolean a = block.getType().isSolid();
            boolean b = o.block.getType().isSolid();
            if(a&&b)return 0;
            if(a)return -1;
            if(b)return 1;
            return 0;
        }
        private BlockChange causePhysicsUpdate(){
            causesPhysicsUpdate = true;
            return this;
        }
    }
    public static class BlockMovement{
        public final Location from;
        public final Location to;
        public final int rotation;
        public BlockMovement(Location from, Location to){
            this(from, to, 0);
        }
        public BlockMovement(Block from, Block to){
            this(from.getLocation(), to.getLocation());
        }
        public BlockMovement(Block from, Location to){
            this(from.getLocation(), to);
        }
        public BlockMovement(Location from, Location to, int rotation){
            this.from = from.clone();
            this.to = to.clone();
            this.rotation = rotation;
        }
        public BlockMovement(Block from, Block to, int rotation){
            this(from.getLocation(), to.getLocation(), rotation);
        }
        public BlockMovement(Block from, Location to, int rotation){
            this(from.getLocation(), to, rotation);
        }
        public void move1(Craft parent){
            //TODO moving other crafts' blocks
//            Block block = parent.world.getBlockAt(from);
//            if(parent.blocks.contains(block))return;
//            Craft craft = parent.aeronautics.getCraft(from);
//            if(parent.type.children.contains(craft.type)){
//                craft.blocks.remove(block);
//            }
        }
        public void move2(Craft parent){
            //TODO moving other crafts' blocks
//            Block block = parent.world.getBlockAt(from);
//            Block t = parent.world.getBlockAt(to);
//            if(parent.blocks.contains(block))return;
//            Craft craft = parent.aeronautics.getCraft(from);
//            if(parent.type.children.contains(craft.type)){
//                craft.blocks.add(t);
//            }
        }
        private void callEvent(Craft craft){
            Bukkit.getServer().getPluginManager().callEvent(new BlockMoveEvent(craft, this));
        }
    }
    /**
     * @deprecated currently no alternative
     */
    @Deprecated
    public static enum Mode{
        IDLE,
        CONSTRUCTION,
        COMBAT;
    }//TODO remove hard-coded modes; make it more configurable
}