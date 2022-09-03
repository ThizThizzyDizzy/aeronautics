package com.thizthizzydizzy.aeronautics.craft.multiblock;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.special.PointDefenseCannon;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chain;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
public class PDCMultiblock extends Multiblock{
    /**
     * Currently facing (Cardinal)
     */
    private Direction facing;
    private final CraftSpecial special;
    private final PointDefenseCannon pdc;
    private PDCTarget target = null;
    private Vector initialFacing;
    /**
     * Turing to face
     */
    private Vector targetVector;
    /**
     * Currently facing
     */
    private Vector vector;
    private int targetingTimeout = 0;
    private HashMap<Entity, Location> trackedProjectiles = new HashMap<>();
    public PDCMultiblock(CraftSpecial special, PointDefenseCannon pdc){
        this(special, pdc, null, null, null);
    }
    private PDCMultiblock(CraftSpecial special, PointDefenseCannon pdc, Craft craft, Block origin, Direction facing){
        super("aeronautics:pdc", craft, origin);
        this.facing = facing;
        this.special = special;
        this.pdc = pdc;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(origin.getType()!=Material.DISPENSER)return null;//quick fail to make it faster
        for(Direction d : Direction.LATERAL){
            Multiblock m = detect(craft, origin, d);
            if(m!=null)return m;
        }
        return null;
    }
    private Multiblock detect(Craft craft, Block origin, Direction d){
        if(scan(craft, origin, d))return new PDCMultiblock(special, pdc, craft, origin, d);
        return null;
    }
    private boolean scan(Craft craft, Block origin, Direction facing){
        BlockData data = origin.getBlockData();
        if(data instanceof Dispenser){
            if(!facing.matches(((Dispenser)data).getFacing()))return false;//dispenser's facing the wrong way
        }else return false;//not a dispenser
        Block chain = origin.getRelative(facing.x, facing.y, facing.z);
        if(chain.getType()!=Material.CHAIN)return false;//not chain
        data = chain.getBlockData();
        if(data instanceof Chain){
            if(!facing.matches(((Chain)data).getAxis()))return false;//chain's facing the wrong way
        }//not chain
        Block wall,button1,button2;
        if((wall = origin.getRelative(0,-1,0)).getType()!=Material.STONE_BRICK_WALL)return false;//not wall
        if((button1 = origin.getRelative(facing.z, facing.y, facing.x)).getType()!=Material.STONE_BUTTON)return false;//one of them
        if((button2 = origin.getRelative(-facing.z, facing.y, -facing.x)).getType()!=Material.STONE_BUTTON)return false;//the other one
        Block stairs = origin.getRelative(-facing.x,facing.y,-facing.z);
        if(stairs.getType()!=Material.STONE_BRICK_STAIRS)return false;//not stairs
        data = stairs.getBlockData();
        if(data instanceof Stairs){
            if(((Stairs)data).getShape()!=Stairs.Shape.STRAIGHT)return false;//wrong shape
            if(!facing.matches(((Stairs)data).getFacing()));//wrong direction
            if(((Stairs)data).getHalf()!=Bisected.Half.TOP)return false;//wrong half
        }
        Block stairSlab = stairs.getRelative(0,1,0);
        if(stairSlab.getType()!=Material.STONE_BRICK_SLAB)return false;//not stairs
        data = stairSlab.getBlockData();
        if(data instanceof Slab){
            if(((Slab)data).getType()!=Slab.Type.BOTTOM)return false;//wrong type
        }
        Block topSlab = origin.getRelative(0,1,0);
        if(topSlab.getType()!=Material.STONE_BRICK_SLAB)return false;//not stairs
        data = topSlab.getBlockData();
        if(data instanceof Slab){
            if(((Slab)data).getType()!=Slab.Type.BOTTOM)return false;//wrong type
        }
        return craft==null||craft.contains(origin, chain, wall, button1, button2, stairs, stairSlab, topSlab);
    }
    @Override
    public void init(){
        initialFacing = new Vector(facing.x, facing.y, facing.z).normalize();
        vector = initialFacing.clone();
        targetVector = initialFacing.clone();
        craft.aeronautics.debug(craft.getCrew(), "InitialFacing initialized to "+initialFacing.toString());
    }
    @Override
    public void tick(){
        if(targetingTimeout>=pdc.minTargetTimeout)target = null;
        if(target==null){
            PDCTarget closest = null;
            float closestAngle = 0;
            HashSet<PDCTarget> targets = pdc.getTargets(special);
            for(PDCTarget target : targets){
                for(int i = 0; i<pdc.targetingAttempts; i++){
                    Location targ = target.getRandomLocation();
                    Location barrel = getIdealBarrelLocation(targ);
                    if(targ.distance(barrel)>pdc.targetingRange){
                        continue;
                    }
                    Vector direction = targ.toVector().subtract(barrel.toVector()).normalize();
                    RayTraceResult result = craft.getWorld().rayTraceBlocks(barrel, direction, pdc.targetingRange, FluidCollisionMode.NEVER, true);
                    if(result==null||result.getHitBlock()==null||target.isTarget(result.getHitBlock())){
                        float angle = direction.angle(vector);
                        if(closest==null||angle<closestAngle){
                            closest = target;
                            closestAngle = angle;
                        }
                        break;
                    }
                }
            }
            if(closest==null){
                targetVector = initialFacing.clone();
            }else{
                target = closest;
            }
        }
        if(target!=null){
            craft.aeronautics.debug(craft.getCrew(), "Targeting");
            if(targetVector.equals(initialFacing)||Math.toDegrees(targetVector.angle(vector))<=pdc.firingAngle){//arrived at target, change target
                craft.aeronautics.debug(craft.getCrew(), "Retargeting");
                boolean foundTarget = false;
                for(int i = 0; i<pdc.targetingAttempts; i++){
                    Location targ = target.getRandomLocation();
                    Location barrel = getIdealBarrelLocation(targ);
                    if(targ.distance(barrel)>pdc.targetingRange)continue;
                    Vector direction = targ.toVector().subtract(barrel.toVector()).normalize();
                    double angle = Math.toDegrees(direction.angle(new Vector(0, 1, 0)))-90;
                    if(angle<-pdc.verticalAngle||angle>pdc.verticalAngle)continue;//too far up/down!
                    RayTraceResult result = craft.getWorld().rayTraceBlocks(barrel, direction, pdc.targetingRange, FluidCollisionMode.NEVER, true);
                    if(result==null||result.getHitBlock()==null||target.isTarget(result.getHitBlock())){
                        targetVector = direction;
                        craft.aeronautics.debug(craft.getCrew(), "Target set!");
                        foundTarget = true;
                        break;
                    }
                }
                if(!foundTarget){
                    targetingTimeout++;
                }
            }
        }
        craft.aeronautics.debug(craft.getCrew(), "Trying to face "+targetVector.toString());
        craft.aeronautics.debug(craft.getCrew(), "Vector is "+vector.toString());
        Vector axis = vector.getCrossProduct(targetVector).normalize();
        double angle = 0;
        if(!Double.isNaN(axis.getX())){
            craft.aeronautics.debug(craft.getCrew(), "Axis is "+axis.toString());
            angle = Math.min(Math.toDegrees(vector.angle(targetVector)), pdc.trackingSpeed);
            craft.aeronautics.debug(craft.getCrew(), "Angle is "+angle);
            vector.rotateAroundAxis(axis, Math.toRadians(angle));
            craft.aeronautics.debug(craft.getCrew(), "Vector is now "+vector);
        }else{
            craft.aeronautics.debug(craft.getCrew(), "Axis is NaN; Angle is assumed to be 0");
        }
        Direction closest = null;
        float dirAngle = 0;
        craft.aeronautics.debug(craft.getCrew(), "Finding closest direction to "+vector.toString());
        for(Direction dir : Direction.LATERAL){
            float dirAng = new Vector(dir.x,dir.y,dir.z).angle(vector);
            if(closest==null||dirAng<dirAngle){
                dirAngle = dirAng;
                closest = dir;
            }
        }
        if(closest!=facing){
            craft.aeronautics.debug(craft.getCrew(), "Rotating from "+facing.toString()+" to "+closest);
            rotateTo(closest);
        }
        if(target!=null){
            Location barrel = getBarrelLocation();
            Vector direction = vector.clone().normalize();
            RayTraceResult result = craft.getWorld().rayTraceBlocks(barrel, direction, pdc.targetingRange, FluidCollisionMode.NEVER, true);
            boolean firing;
            if(result==null||result.getHitBlock()==null){
                firing = angle<=pdc.firingAngle;
            }else{
                firing = target.isTarget(result.getHitBlock());
            }
            Random rand = new Random();
            direction.add(new Vector((rand.nextDouble()-.5)*(1-pdc.accuracy), (rand.nextDouble()-.5)*(1-pdc.accuracy), (rand.nextDouble()-.5)*(1-pdc.accuracy)));
            if(firing){
                BlockState state = origin.getState();
                if(state instanceof org.bukkit.block.Dispenser){
                    Inventory inv = ((org.bukkit.block.Dispenser)state).getInventory();
                    ArrayList<ItemStack> possibleProjectiles = new ArrayList<>();
                    HashMap<ItemStack, Integer> indicies = new HashMap<>();
                    for(int i = 0; i<inv.getContents().length; i++){
                        ItemStack content = inv.getContents()[i];
                        if(content==null)continue;
                        if(isProjectile(content.getType())){
                            possibleProjectiles.add(content);
                            indicies.put(content, i);
                        }
                    }
                    if(!possibleProjectiles.isEmpty()){
                        ItemStack stack = possibleProjectiles.get(rand.nextInt(possibleProjectiles.size()));
                        EntityType type = getProjectile(stack.getType());
                        Projectile projectile = Vanillify.summon(type, barrel);
                        if(pdc.noGravity)projectile.setGravity(false);
                        craft.aeronautics.debug(craft.getCrew(), "Shooting at "+direction.toString()+" "+pdc.muzzleVelocity+" "+pdc.acceleration);
                        Vanillify.setVelocity(projectile, direction.clone().normalize().multiply(pdc.muzzleVelocity));
                        if(projectile instanceof Fireball)Vanillify.setPower((Fireball)projectile, direction.clone().normalize().multiply(pdc.acceleration));
                        if(stack.getType()==Material.TIPPED_ARROW){
                            ItemMeta meta = stack.getItemMeta();
                            if(meta instanceof PotionMeta){
                                PotionMeta pmeta = (PotionMeta)meta;
                                ((Arrow)projectile).setBasePotionData(pmeta.getBasePotionData());
                                for(PotionEffect effect : pmeta.getCustomEffects()){
                                    ((Arrow)projectile).addCustomEffect(effect, false);
                                }
                            }
                        }
                        stack.setAmount(stack.getAmount()-1);
//                        inv.setItem(indicies.get(stack), stack);
                        trackedProjectiles.put(projectile, barrel);
                    }
                }
            }
        }
        for(Iterator<Entity> it = trackedProjectiles.keySet().iterator(); it.hasNext();){
            Entity e = it.next();
            if(e.getLocation().distance(trackedProjectiles.get(e))>pdc.range){
                e.remove();
                it.remove();
            }
        }
    }
    @Override
    public boolean rescan(){
        craft.aeronautics.debug(craft.getCrew(), "rescanning!");
        return scan(craft, origin, facing);
    }
    @Override
    public void onDestroy(){
        craft.aeronautics.debug(craft.getCrew(), "Destroyed multiblock "+toString()+" at "+origin.toString()+"!");
    }
    private Location getBarrelLocation(){
        return origin.getLocation().add(.5,.5,.5).add(facing.x*2,facing.y*2,facing.z*2);
    }
    private Location getIdealBarrelLocation(Location target){
        Vector v = target.toVector().subtract(origin.getLocation().add(.5,.5,.5).toVector());
        return origin.getLocation().add(v.normalize().multiply(2));
    }
    private void rotateTo(Direction newFacing){
        craft.aeronautics.debug(craft.getCrew(), "rotating!");
        int rotation = facing.getRotation(newFacing);
        if(rotate(getBlocks(), rotation)){
            craft.aeronautics.debug(craft.getCrew(), "success!");
            facing = newFacing;
        }
        else destroy();
    }
    private HashSet<Block> getBlocks(){
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(origin);
        blocks.add(origin.getRelative(facing.x, facing.y, facing.z));//chain
        blocks.add(origin.getRelative(0,-1,0));//wall
        blocks.add(origin.getRelative(facing.z, facing.y, facing.x));//button1
        blocks.add(origin.getRelative(-facing.z, facing.y, -facing.x));//button2
        blocks.add(origin.getRelative(-facing.x,facing.y,-facing.z));//stairs
        blocks.add(origin.getRelative(-facing.x,facing.y,-facing.z).getRelative(0,1,0));//stairSlab
        blocks.add(origin.getRelative(0,1,0));//topSlab
        return blocks;
    }
    private boolean isProjectile(Material type){
        return type==Material.FIRE_CHARGE
                ||type==Material.ARROW
                ||type==Material.SPECTRAL_ARROW
                ||type==Material.TIPPED_ARROW
                ||type==Material.SNOWBALL
                ||type==Material.EGG;
    }
    private EntityType getProjectile(Material type){
        switch(type){
            case FIRE_CHARGE:
                return EntityType.SMALL_FIREBALL;
            case ARROW:
                return EntityType.ARROW;
            case SPECTRAL_ARROW:
                return EntityType.SPECTRAL_ARROW;
            case TIPPED_ARROW:
                return EntityType.ARROW;
            case SNOWBALL:
                return EntityType.SNOWBALL;
            case EGG:
                return EntityType.EGG;
        }
        return null;
    }
    @Override
    public void onRotated(int rotation){
        while(rotation>0){
            rotation--;
            initialFacing.rotateAroundY(90);
            switch(facing){
                case NORTH:
                    facing = Direction.EAST;
                    break;
                case EAST:
                    facing = Direction.SOUTH;
                    break;
                case SOUTH:
                    facing = Direction.WEST;
                    break;
                case WEST:
                    facing = Direction.NORTH;
                    break;
            }
        }
    }
    @Override
    public String[] getBlockStats(boolean onSign){
        return null;
    }
    @Override
    public boolean contains(Block block){
        return false;
        //TODO actually check
    }
}