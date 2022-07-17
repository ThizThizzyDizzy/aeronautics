package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.JSON.JSONArray;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.StandardEngineInitializationEvent;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.MediumCache;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
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
    
    private int minMoveInterval;
    private int minMoveDistance;
    
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
        minMoveInterval = json.getInt("min_move_interval");
        minMoveDistance = json.getInt("min_move_distance");
    }
    @Override
    protected void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Engine newInstance(){
        return new StandardEngine();
    }
    @Override
    public void init(CraftEngine engine){
        engine.set("moveDelay", 0);
        engine.set("velocity", new Vector());
        engine.set("pendingTravel", new Vector());
        engine.setLong("mass", 0);
        engine.setBoolean("needsAerodynamicUpdate", true);
        for(var gen : generators){
            gen.init(engine, this);
        }
        for(var subEngine : subEngines){
            subEngine.init(engine, this);
        }
        for(var eds : energyDistributionSystems){
            eds.init(engine, this);
        }
        //todo initialization
    }
    @Override
    public void tick(CraftEngine engine){
        if(engine.getBoolean("needsAerodynamicUpdate")&&engine.get("aerodynamicUpdate")==null){
            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "Generating block cache");
            final StandardEngineBlockCache cache = engine.getCraft().generateBlockCache(new StandardEngineBlockCache(this));
            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "Starting asynchronous aerodynamic update");
            engine.set("aerodynamicUpdate", new BukkitRunnable() {
                @Override
                public void run(){
                    double originX = ((cache.minX+cache.maxX)/2d)+.5;
                    double originY = ((cache.minY+cache.maxY)/2d)+.5;
                    double originZ = ((cache.minZ+cache.maxZ)/2d)+.5;
                    cache.calcCenters();
                    engine.setLong("mass", cache.mass);
                    double localCovX = cache.covX+.5-originX;
                    double localCovY = cache.covY+.5-originY;
                    double localCovZ = cache.covZ+.5-originZ;
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            engine.getCraft().getAeronautics().debug(engine.getCraft().getCrew(), "COM: "+cache.covX+" "+cache.covY+" "+cache.covZ+" (~"+localCovX+" ~"+localCovY+" ~"+localCovZ+")");
                        }
                    }.runTask(engine.getCraft().getAeronautics());
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
                    engine.set("aerodynamicNet", net);
                    engine.set("aerodynamicUpdate", null);
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
            }.runTaskAsynchronously(engine.getCraft().getAeronautics()));
            engine.setBoolean("needsAerodynamicUpdate", false);
        }
        AerodynamicNet aerodynamicNet = engine.get("aerodynamicNet");
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
        for(var gen : generators)gen.tick(engine, this);
        for(var eds : energyDistributionSystems){
            eds.tick(engine, this);
        }
        for(var eng : subEngines)eng.tick(engine, this);
        Vector velocity = engine.get("velocity");
        MediumCache mediums = engine.getCraft().getCurrentMediums();
        double buoyantForce = mediums.shipVolume*mediums.buoyancy*gravity;
        double dragMult = mediums.drag;
        long mass = engine.getLong("mass");
        if(mass>0){//only move if mass is non-zero
            velocity.add(getGravityVector());
            velocity.add(new Vector(0, buoyantForce/mass, 0));
            for(var eng : subEngines){
                velocity.add(eng.getCurrentThrust(engine, this).multiply(1d/mass));
            }
        }
        if(aerodynamicNet!=null){
            for(Direction d : Direction.NONZERO){
                AerodynamicSettings settings = aerodynamicNet.aerodynamics.get(d);
                if(settings!=null){//no drag if aerodynamics haven't calculated yet
                    Vector filteredVector = new Vector(velocity.getX()*d.x, velocity.getY()*d.y, velocity.getZ()*d.z);
                    if(Math.signum(filteredVector.getX())==Math.signum(velocity.getX())&&Math.signum(filteredVector.getY())==Math.signum(velocity.getY())&&Math.signum(filteredVector.getZ())==Math.signum(velocity.getZ())){
                        //vector is pointing the right direction
                        velocity.add(filteredVector.multiply(-1+Math.max(settings.aerodynamicness, 1-dragMult*(1-settings.aerodynamicness))/Math.max(1, dragMult)));
                    }
                }
            }
        }
        Vector pendingTravel = engine.get("pendingTravel");
        pendingTravel.add(velocity);
        int delay = engine.get("moveDelay");
        if(delay<=0){
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Mass: "+mass);
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Pending travel: "+pendingTravel.toString());
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Velocity: "+velocity.toString());
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Buoyant force: "+buoyantForce);
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Gravity force: "+gravity*mass);
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Drag multiplier: "+dragMult);
            int len = Math.min((int)pendingTravel.length(), Math.max((int)Math.abs(pendingTravel.getX()), Math.max((int)Math.abs(pendingTravel.getY()), (int)Math.abs(pendingTravel.getZ()))));
            engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Travel len: "+len+"/"+minMoveDistance);
            if(len>=minMoveDistance){
                int dx = (int)pendingTravel.getX();
                int dy = (int)pendingTravel.getY();
                int dz = (int)pendingTravel.getZ();
                if(engine.getCraft().move(dx, dy, dz, engine.getCraft().type.mediums)){
                    engine.getCraft().aeronautics.debug(engine.getCraft().getCrew(), "Moved by "+dx+" "+dy+" "+dz);
                    pendingTravel.subtract(new Vector(dx,dy,dz));
                }else{
                    velocity.multiply(0);
                    pendingTravel.multiply(0);
                }
                engine.set("moveDelay", minMoveInterval);
            }
        }else engine.set("moveDelay", delay-1);
        //TODO rotation
        //TODO holding steady vertically
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
        engine.setBoolean("needsAerodynamicUpdate", true);
        //TODO pass through
    }
    @Override
    public void onUnload(CraftEngine engine){
        for(SubEngine e : subEngines){
            for(Direction d : Direction.LATERAL)e.setThrottle(engine, this, d, 0);
        }
        //TODO stop?
    }
    @Override
    public void onMoved(CraftEngine engine){}
    @Override
    public boolean canRemoveBlock(CraftEngine engine, Player player, int damage, boolean damaged, Location l){
        engine.setBoolean("needsAerodynamicUpdate", true);
        return true;
    }
    @Override
    public void updateHull(CraftEngine engine, int damage, boolean damaged){
        engine.setBoolean("needsAerodynamicUpdate", true);
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
        engine.setBoolean("needsAerodynamicUpdate", true);
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
        messages.add(new Message(Message.Priority.INFO_UNIVERSAL, true, true, "pos "+engine.getCraft().getOrigin().toVector().toString()));
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
    public int getItemMass(Material m, int count){
        return (int)(getMass(m)*count*itemMassMultiplier);
    }
    @Override
    public void onRegister(){
        EnergyDistributionSystem.init();
        Generator.init();
        SubEngine.init();
        Bukkit.getServer().getPluginManager().callEvent(new StandardEngineInitializationEvent(this));
    }
    private Vector getGravityVector(){
        return new Vector(0, -gravity, 0);
    }
}