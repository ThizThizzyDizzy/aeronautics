package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.JSON.JSONArray;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.craft.BlockCache;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
public class StandardEngine extends Engine{
    private ArrayList<EnergyDistributionSystem> energyDistributionSystems = new ArrayList<>();
    private ArrayList<Generator> generators = new ArrayList<>();
    private ArrayList<SubEngine> subEngines = new ArrayList<>();
    private double gravity;
    private boolean enableBalancing = false;
    private double balancingDriftMultiplier;
    private int defaultBlockMass;
    private int defaultItemMass;
    private HashMap<Material, Integer> blockMass = new HashMap<>();
    private double itemMassMultiplier;
    private double netResolutionMult;
    private double netStepSize;
    private int netBlend;
    private double netInflate;
    private double netInflateNormal;
    private double netFlatnessPow;
    private double netFlatnessPenalty;
    private double netSideWeight;
    private boolean netGeometricNormals;

    private boolean netDebugEnable;
    private double netScale;
    private double netNormalScale;
    private String netColors;
    
    private boolean needsAerodynamicUpdate = true;
    private BukkitTask aerodynamicUpdate = null;
    private AerodynamicNet aerodynamicNet = null;
    public StandardEngine(){
        super("aeronautics:standard_engine");
    }
    @Override
    protected void load(JSONObject json){
        for(Object obj : json.getJSONArray("energy_distribution_systems")){
            energyDistributionSystems.add(EnergyDistributionSystem.loadEnergyDistributionSystem((JSONObject)obj));
        }
        for(Object obj : json.getJSONArray("generators")){
            generators.add(Generator.loadGenerator((JSONObject)obj));
        }
        for(Object obj : json.getJSONArray("engines")){
            subEngines.add(SubEngine.loadEngine((JSONObject)obj));
        }
        JSONObject aerodynamicSettings = json.getJSONObject("aerodynamic_settings");
        JSONObject netSettings = aerodynamicSettings.getJSONObject("aerodynamic_net");
        netResolutionMult = netSettings.getDouble("resolution_mult");
        netStepSize = netSettings.getDouble("step_size");
        netBlend = netSettings.getInt("blend");
        netInflate = netSettings.getDouble("inflate");
        netInflateNormal = netSettings.getDouble("inflate_normal");
        netFlatnessPow = netSettings.getDouble("flatness_power");
        netFlatnessPenalty = netSettings.getDouble("flatness_penalty");
        netSideWeight = netSettings.getDouble("side_weight");
        netGeometricNormals = netSettings.getBoolean("geometric_normals");
        JSONObject netDebug = netSettings.getJSONObject("debug");
        netDebugEnable = netDebug.getBoolean("enabled");
        netScale = netDebug.getDouble("scale");
        netNormalScale = netDebug.getDouble("normal_scale");
        netColors = netDebug.getString("colors");
        gravity = aerodynamicSettings.getDouble("gravity");
        if(aerodynamicSettings.hasJSONObject("balancing")){
            enableBalancing = true;
            JSONObject balancingSettings = aerodynamicSettings.getJSONObject("balancing");
            balancingDriftMultiplier = balancingSettings.getDouble("drift_multiplier");
        }
        defaultBlockMass = aerodynamicSettings.getInt("default_block_mass");
        defaultItemMass = aerodynamicSettings.getInt("default_item_mass");
        for(Object obj : aerodynamicSettings.getJSONArray("mass")){
            JSONObject jobj = (JSONObject)obj;
            JSONArray materials = jobj.getJSONArray("materials");
            ArrayList<Material> mats = new ArrayList<>();
            for(Object o : materials){
                mats.addAll(Vanillify.getBlocks((String)o));
            }
            for(Material m : mats){
                blockMass.put(m, jobj.getInt("mass"));
            }
        }
        itemMassMultiplier = aerodynamicSettings.getDouble("item_mass_multiplier");
    }
    @Override
    protected void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Engine newInstance(){
        return new StandardEngine();
    }
    @Override
    public void init(CraftEngine engine){
        needsAerodynamicUpdate = true;
        for(var gen : generators){
            gen.init(engine, this);
        }
        for(var subEngine : subEngines){
            subEngine.init(engine, this);
        }
        for(var eds : energyDistributionSystems){
            eds.init(engine, this);
        }
        //TODO pass through
        //todo initialization
    }
    @Override
    public void tick(CraftEngine engine){
        if(needsAerodynamicUpdate&&aerodynamicUpdate==null){
            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "Generating block cache");
            final BlockCache cache = engine.getCraft().generateBlockCache();
            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "Starting asynchronous aerodynamic update");
            aerodynamicUpdate = new BukkitRunnable() {
                @Override
                public void run(){
                    double originX = ((cache.minX+cache.maxX)/2d)+.5;
                    double originY = ((cache.minY+cache.maxY)/2d)+.5;
                    double originZ = ((cache.minZ+cache.maxZ)/2d)+.5;
                    cache.calcCOV();
                    double localCovX = cache.covX+.5-originX;
                    double localCovY = cache.covY+.5-originY;
                    double localCovZ = cache.covZ+.5-originZ;
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "COM: "+cache.covX+" "+cache.covY+" "+cache.covZ+" (~"+localCovX+" ~"+localCovY+" ~"+localCovZ+")");
                        }
                    }.runTask(engine.getCraft().getAeronautics());
                    //TODO rather than shrinking towards the center of the ship, try moving the net (for example) from the front of the ship to the back, while shrinking it from double size to 0
                    AerodynamicNet net = new AerodynamicNet(originX, originY, originZ, (int)(netResolutionMult*Math.min(cache.maxX-cache.minX+1, Math.min(cache.maxY-cache.minY+1, cache.maxZ-cache.minZ+1))), (cache.maxX-cache.minX+1)/2d, (cache.maxY-cache.minY+1)/2d, (cache.maxZ-cache.minZ+1)/2d);
                    for(AerodynamicNetSide side : net.sides){
                        for(AerodynamicNetPoint[] points : side.net){
                            for(AerodynamicNetPoint point : points){
                                double dist = Math.sqrt((point.x-localCovX)*(point.x-localCovX)+(point.y-localCovY)*(point.y-localCovY)+(point.z-localCovZ)*(point.z-localCovZ));
                                int steps = (int)(dist/netStepSize);
                                double stepSize = dist/steps;//adjusted so the final step will bring it to the origin
                                double dx = -((point.x-localCovX)/dist)*stepSize;//*(Math.abs(side.direction.x)+1);
                                double dy = -((point.y-localCovY)/dist)*stepSize;//*(Math.abs(side.direction.y)+1);
                                double dz = -((point.z-localCovZ)/dist)*stepSize;//*(Math.abs(side.direction.z)+1);
                                for(int i = 0; i<steps; i++){
                                    if(cache.isSolid(originX+point.x, originY+point.y, originZ+point.z))break;
                                    else{
                                        point.x+=dx;
                                        point.y+=dy;
                                        point.z+=dz;
                                    }
                                }
                                //inflate by a bit, away from the origin (not COM or normals)
                                point.x-=dx/stepSize*netInflate;
                                point.y-=dy/stepSize*netInflate;
                                point.z-=dz/stepSize*netInflate;
                            }
                        }
                    }
                    if(netBlend>0){
                        net.blend(netBlend);
                    }
                    if(netGeometricNormals)net.calculateGeometricNormals();
                    else net.calculateNormals();
                    if(netInflateNormal>0){
                        net.inflate(netInflateNormal);
                        if(netGeometricNormals)net.calculateGeometricNormals();
                        else net.calculateNormals();
                    }
                    net.calculateSharpness(netFlatnessPow);
                    net.calculateAerodynamics(netFlatnessPenalty, netSideWeight);
                    StandardEngine.this.aerodynamicNet = net;
                    aerodynamicUpdate = null;
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "Finished asynchronous aerodynamic update");
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), 
                                    "N "+Math.round(net.aerodynamics.get(Direction.NORTH).aerodynamicness*10000)/100d+"% | "+
                                    "S "+Math.round(net.aerodynamics.get(Direction.SOUTH).aerodynamicness*10000)/100d+"%");
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), 
                                    "E "+Math.round(net.aerodynamics.get(Direction.EAST).aerodynamicness*10000)/100d+"% | "+
                                    "W "+Math.round(net.aerodynamics.get(Direction.WEST).aerodynamicness*10000)/100d+"%");
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), 
                                    "U "+Math.round(net.aerodynamics.get(Direction.UP).aerodynamicness*10000)/100d+"% | "+
                                    "D "+Math.round(net.aerodynamics.get(Direction.DOWN).aerodynamicness*10000)/100d+"%");
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), 
                                    "CW "+Math.round(net.cwAerodynamics.aerodynamicness*10000)/100d+"% | "+
                                    "CCW "+Math.round(net.ccwAerodynamics.aerodynamicness*10000)/100d+"%");
                        }
                    }.runTask(engine.getCraft().getAeronautics());
                }
            }.runTaskAsynchronously(engine.getCraft().getAeronautics());
            needsAerodynamicUpdate = false;
        }
        if(netDebugEnable&&aerodynamicNet!=null){
            iii++;
            switch (iii) {
                case 2 -> drawNet(engine.getCraft().getWorld(), aerodynamicNet, aerodynamicNet.north, Color.RED);
                case 4 -> drawNet(engine.getCraft().getWorld(), aerodynamicNet, aerodynamicNet.east, Color.YELLOW);
                case 6 -> drawNet(engine.getCraft().getWorld(), aerodynamicNet, aerodynamicNet.south, Color.BLUE);
                case 8 -> drawNet(engine.getCraft().getWorld(), aerodynamicNet, aerodynamicNet.west, Color.GREEN);
                case 10 -> drawNet(engine.getCraft().getWorld(), aerodynamicNet, aerodynamicNet.up, Color.ORANGE);
                case 12 -> {
                    drawNet(engine.getCraft().getWorld(), aerodynamicNet, aerodynamicNet.down, Color.PURPLE);
                    iii = 0;
                }
            }
        }
        for(var eds : energyDistributionSystems){//because they don't use multiblocks; what about generators/engines though?
            eds.tick(engine, this);
        }
        //TODO pass through
        //TODO all the movement and stuff
    }
    int iii = 0;
    private void drawNet(World world, AerodynamicNet net, AerodynamicNetSide side, Color color){
        for(int x = 0; x<side.net.length; x++){
            for(int y = 0; y<side.net[x].length; y++){
                var point = side.net[x][y];
                color = switch(netColors){
                    case "face" -> color;
                    case "xy" -> Color.fromRGB(x*255/side.net.length, y*255/side.net[x].length, 0);
                    case "sharpness" -> Color.fromRGB((int)(Math.max(0, Math.min(1, -point.sharpness))*255), (int)(Math.max(0, Math.min(1, point.flatness))*255), (int)(Math.max(0, Math.min(1, point.sharpness))*255));
                    case "aerodynamic_components_north" -> Color.fromRGB((int)(Math.max(0, Math.min(1, point.angle.get(Direction.NORTH)))*255), (int)(Math.max(0, Math.min(1, point.sideness.get(Direction.NORTH)))*255), (int)(Math.max(0, Math.min(1, point.forwardness.get(Direction.NORTH)))*255));
                    case "aerodynamic_components_up" -> Color.fromRGB((int)(Math.max(0, Math.min(1, point.angle.get(Direction.UP)))*255), (int)(Math.max(0, Math.min(1, point.sideness.get(Direction.UP)))*255), (int)(Math.max(0, Math.min(1, point.forwardness.get(Direction.UP)))*255));
                    case "aerodynamic_factors_north" -> Color.fromRGB((int)(Math.max(0, Math.min(1, point.aerodynamicness.get(Direction.NORTH)))*255), (int)(Math.max(0, Math.min(1, point.sideFactor.get(Direction.NORTH)))*255), (int)(Math.max(0, Math.min(1, point.forwardFactor.get(Direction.NORTH)))*255));
                    case "aerodynamic_factors_up" -> Color.fromRGB((int)(Math.max(0, Math.min(1, point.aerodynamicness.get(Direction.UP)))*255), (int)(Math.max(0, Math.min(1, point.sideFactor.get(Direction.UP)))*255), (int)(Math.max(0, Math.min(1, point.forwardFactor.get(Direction.UP)))*255));
                    case "aerodynamics_north" -> Color.fromRGB((int)(Math.max(0, Math.min(1, 1-(point.aerodynamicness.get(Direction.NORTH)/2+.5)))*255), (int)(Math.max(0, Math.min(1, point.aerodynamicness.get(Direction.NORTH)/2+.5))*255), (int)(Math.max(0, Math.min(1, Math.abs(point.sharpness)))*255));
                    case "aerodynamics_up" -> Color.fromRGB((int)(Math.max(0, Math.min(1, 1-(point.aerodynamicness.get(Direction.UP)/2+.5)))*255), (int)(Math.max(0, Math.min(1, point.aerodynamicness.get(Direction.UP)/2+.5))*255), (int)(Math.max(0, Math.min(1, Math.abs(point.sharpness)))*255));
                    default -> Color.fromRGB(0, 0, 0);
                };
                world.spawnParticle(Particle.REDSTONE, new Location(world, net.originX+point.x*netScale, net.originY+point.y*netScale, net.originZ+point.z*netScale), 1, 0, 0, 0, new Particle.DustOptions(color, 2));
                if(netNormalScale>0)drawNormal(world, net, point, color, .5);
                if(x>0)drawLine(world, net, point, side.net[x-1][y], color, .5);
                if(y>0)drawLine(world, net, point, side.net[x][y-1], color, .5);
            }
        }
    }
    private void drawNormal(World world, AerodynamicNet net, AerodynamicNetPoint point, Color color, double step){
        double dist = point.normal.length()*netNormalScale;
        int steps = (int)Math.ceil(dist/step)+1;
        for(int i = 1; i<steps; i++){
            double percent = i/(double)steps;
            double x1 = net.originX+point.x*netScale;
            double y1 = net.originY+point.y*netScale;
            double z1 = net.originZ+point.z*netScale;
            double x2 = x1+point.normal.getX()*netScale*netNormalScale;
            double y2 = y1+point.normal.getY()*netScale*netNormalScale;
            double z2 = z1+point.normal.getZ()*netScale*netNormalScale;
            world.spawnParticle(Particle.REDSTONE, new Location(world, x1+(x2-x1)*percent, y1+(y2-y1)*percent, z1+(z2-z1)*percent), 1, 0, 0, 0, new Particle.DustOptions(color, 1));
        }
    }
    private void drawLine(World world, AerodynamicNet net, AerodynamicNetPoint p1, AerodynamicNetPoint p2, Color color, double step){
        double dist = Math.sqrt(Math.pow(p2.x-p1.x,2)+Math.pow(p2.y-p1.y,2)+Math.pow(p2.z-p1.z,2));
        int steps = (int)Math.ceil(dist/step)+1;
        for(int i = 1; i<steps; i++){
            double percent = i/(double)steps;
            double x1 = net.originX+p1.x*netScale;
            double y1 = net.originY+p1.y*netScale;
            double z1 = net.originZ+p1.z*netScale;
            double x2 = net.originX+p2.x*netScale;
            double y2 = net.originY+p2.y*netScale;
            double z2 = net.originZ+p2.z*netScale;
            world.spawnParticle(Particle.REDSTONE, new Location(world, x1+(x2-x1)*percent, y1+(y2-y1)*percent, z1+(z2-z1)*percent), 1, 0, 0, 0, new Particle.DustOptions(color, 1));
        }
    }
    @Override
    public void event(CraftEngine engine, Event event){
        needsAerodynamicUpdate = true;
        //TODO pass through
    }
    @Override
    public void onUnload(CraftEngine engine){
        //TODO stop?
    }
    @Override
    public void onMoved(CraftEngine engine){}
    @Override
    public boolean canRemoveBlock(CraftEngine engine, Player player, int damage, boolean damaged, Location l){
        needsAerodynamicUpdate = true;
        return true;
    }
    @Override
    public void updateHull(CraftEngine engine, int damage, boolean damaged){
        needsAerodynamicUpdate = true;
        for(var gen : generators){
            gen.updateHull(engine, this, damage, damaged);
        }
        for(var subEngine : subEngines){
            subEngine.updateHull(engine, this, damage, damaged);
        }
        for(var eds : energyDistributionSystems){
            eds.updateHull(engine, this, damage, damaged);
        }
    }
    @Override
    public boolean canAddBlock(CraftEngine engine, Player player, Block block, boolean force){
        needsAerodynamicUpdate = true;
        return true;
    }
    @Override
    public void getMessages(CraftEngine engine, ArrayList<Message> messages){
        for(var gen : generators){
            gen.getMessages(engine, this, messages);
        }
        for(var subEngine : subEngines){
            subEngine.getMessages(engine, this, messages);
        }
        for(var eds : energyDistributionSystems){
            eds.getMessages(engine, this, messages);
        }
    }
    @Override
    public void getMultiblockTypes(CraftEngine engine, ArrayList<Multiblock> multiblockTypes){
        for(var gen : generators){
            gen.getMultiblockTypes(engine, this, multiblockTypes);
        }
        for(var subEngine : subEngines){
            subEngine.getMultiblockTypes(engine, this, multiblockTypes);
        }
        for(var eds : energyDistributionSystems){//these go last so they can't claim things like turbine shafts
            eds.getMultiblockTypes(engine, this, multiblockTypes);
        }
    }
    public int getMass(Material m){
        if(blockMass.containsKey(m))return blockMass.get(m);
        return m.isBlock()?defaultBlockMass:defaultItemMass;
    }
}