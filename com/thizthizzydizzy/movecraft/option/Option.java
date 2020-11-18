package com.thizthizzydizzy.movecraft.option;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
public abstract class Option<E>{
    public static ArrayList<Option> options = new ArrayList<>();
    public static Option<Integer> CONSTRUCTION_TIMEOUT = new Option<Integer>("Construction Timeout", true, true, 1200){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "How long (in ticks) after the last construction event should construction mode end?";
        }
    };
    public static Option<Integer> COMBAT_TIMEOUT = new Option<Integer>("Combat Timeout", true, true, 6000){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "How long (in ticks) after the last combat event should combat mode end?";
        }
    };
    public static Option<Integer> COMBAT_PILOTS = new Option<Integer>("Combat Pilots", true, true, 2){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "How many pilots should be required to force combat mode?";
        }
    };
    public static Option<Integer> COMBAT_CREW = new Option<Integer>("Combat Crew", true, true, 3){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "How many crew should be required to force combat mode?";
        }
    };
    public static OptionBoolean COMBAT_BOSSBAR = new OptionBoolean("Combat Bossbar", true, true, true){
        @Override
        public String getDesc(){
            return "Should a ship health bossbar be visible during combat?";
        }
    };
    public static Option<Integer> DAMAGE_REPORT_TIMEOUT = new Option<Integer>("Damage Report Timeout", true, true, 20){
        @Override
        public String getDesc(){
            return "How long should it take for a damage report to time out?";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
    };
    public static Option<Double> YELLOW_THRESHOLD = new Option<Double>("Yellow Threshold", true, true, .1){
        @Override
        public String getDesc(){
            return "What percentage difference should be considered in the yellow zone? (used for HUD block percentages)";
        }
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
    };
    public static Option<Double> RED_THRESHOLD = new Option<Double>("Red Threshold", true, true, .05){
        @Override
        public String getDesc(){
            return "What percentage difference should be considered in the red zone? (used for HUD block percentages)";
        }
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
    };
    public static Option<Integer> FIREBALL_LIFESPAN = new Option<Integer>("Fireball Lifespan", true, true, 100){
        @Override
        public String getDesc(){
            return "How long should fireballs live before despawning?";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
    };
    public static Option<Double> FIREBALL_ANGLE = new Option<Double>("Fireball Angle", true, true, .5){
        @Override
        public String getDesc(){
            return "How far should fireballs be able to be directed? (measure unknown)";
        }
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
    };
    public static Option<Double> TNT_ANGLE = new Option<Double>("TNT Angle", true, true, .7){
        @Override
        public String getDesc(){
            return "How far should TNT be able to be directed? (measure unknown)";
        }
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
    };
    public static Option<Integer> DIRECTOR_TARGET_RANGE = new Option<Integer>("Director Target Range", true, true, 192){
        @Override
        public String getDesc(){
            return "From How far away should you be able to target directors?";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
    };
    public static Option<HashMap<Material, Float>> BLOCK_RESISTANCE_OVERRIDE = new Option<HashMap<Material, Float>>("Block Resistance Override", true, false, new HashMap<>()){
        @Override
        public String getDesc(){
            return "Any values set here will override the vanilla block resistance values\nA restart is required for these changes to take effect";
        }
        @Override
        public HashMap<Material, Float> load(Object o){
            if(o instanceof Map){
                HashMap<Material, Float> materials = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    Material material = loadMaterial(obj);
                    if(material==null)continue;
                    Float resistance = loadFloat(m.get(obj));
                    if(resistance==null)continue;
                    if(materials.containsKey(material)){
                        materials.put(material, Math.max(materials.get(material), resistance));
                    }else{
                        materials.put(material, resistance);
                    }
                }
                return materials;
            }
            return null;
        }
    };
    public static Option<HashMap<Material, Float>> BLOCK_RESISTANCE = new Option<HashMap<Material, Float>>("Block Resistance", true, true, new HashMap<>()){
        @Override
        public String getDesc(){
            return "This controls the movecraft resistance system, separate from the vanilla system";
        }
        @Override
        public HashMap<Material, Float> load(Object o){
            if(o instanceof Map){
                HashMap<Material, Float> materials = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    Material material = loadMaterial(obj);
                    if(material==null)continue;
                    Float resistance = loadFloat(m.get(obj));
                    if(resistance==null)continue;
                    if(materials.containsKey(material)){
                        materials.put(material, Math.max(materials.get(material), resistance));
                    }else{
                        materials.put(material, resistance);
                    }
                }
                return materials;
            }
            return null;
        }
    };
    public static Option<Material> TRACER_STREAM = new Option<Material>("Tracer Stream", true, true, Material.COBWEB, "Cobweb"){
        @Override
        public String getDesc(){
            return "What block should be used for tracers?";
        }
        @Override
        public Material load(Object o){
            return loadMaterial(o);
        }
    };
    public static Option<Integer> TRACER_STREAM_TIME = new Option<Integer>("Tracer Stream Time", true, true, 50){
        @Override
        public String getDesc(){
            return "How long should the tracer stream ghost blocks last?";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
    };
    public static Option<Material> TRACER_EXPLOSION = new Option<Material>("Tracer Explosion", true, true, Material.GLOWSTONE, "Glowstone"){
        @Override
        public String getDesc(){
            return "What block should be used for tracer explosions?";
        }
        @Override
        public Material load(Object o){
            return loadMaterial(o);
        }
    };
    public static Option<Integer> TRACER_EXPLOSION_TIME = new Option<Integer>("Tracer Explosion Time", true, true, 50){
        @Override
        public String getDesc(){
            return "How long should the tracer explosion ghost blocks last?";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
    };
    public static Option<Integer> TRACER_INTERVAL = new Option<Integer>("Tracer Interval", true, true, 5){
        @Override
        public String getDesc(){
            return "How often (in ticks) should tracer ghost blocks be created?";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
    };
    public static Option<Double> TRACER_VELOCITY_THRESHOLD = new Option<Double>("Tracer Velocity Threshold", true, true, .6d){
        @Override
        public String getDesc(){
            return "How fast should TNT be moving in order to get a tracer?";
        }
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
    };
    protected final String name;
    public final boolean global;
    public final boolean craft;
    /**
     * The default or global value of this Option.
     */
    public final E defaultValue;
    public E globalValue;
    public HashMap<CraftType, E> craftValues = new HashMap<>();
    private final Object defaultConfigValue;
    protected Option(String name, boolean global, boolean craft, E defaultValue){
        this(name, global, craft, defaultValue, defaultValue);
    }
    protected Option(String name, boolean global, boolean craft, E defaultValue, Object defaultConfigValue){
        this.name = name;
        this.global = global;
        this.craft = craft;
        this.defaultValue = defaultValue;
        options.add(this);
        this.defaultConfigValue = defaultConfigValue;
    }
    public String getFriendlyName(){
        return name;
    }
    public ArrayList<String> getDescription(){
        ArrayList<String> description = new ArrayList<>();
        String s = getDesc();
        if(s==null)return description;
        if(s.contains("\n")){
            for(String str : s.split("\n")){
                description.add(str);
            }
        }else description.add(s);
        return description;
    }
    public abstract String getDesc();
    /**
     * @return the name in-this-format
     */
    public String getGlobalName(){
        return name.replace(" ", "-").toLowerCase();
    }
    /**
     * @return the name inthisformat
     */
    public String getLocalName(){
        return name.replace(" ", "").toLowerCase();
    }
    /**
     * Loads the option from an object.<br>
     * This object is the result of <code>getConfig().get(getGlobalName())</code> for global options, or <code>list.get(getLocalName()) for local options</code>
     * @param o the object to load from
     * @return the loaded value
     */
    public abstract E load(Object o);
    public E loadFromConfig(FileConfiguration config){
        return load(config.get(getGlobalName()));
    }
    public static Material loadMaterial(Object o){
        if(o instanceof Material)return (Material)o;
        if(o instanceof String)return Material.matchMaterial((String)o);
        return null;
    }
    public ArrayList<E> loadList(Object o){
        if(o instanceof Iterable){
            ArrayList<E> list = new ArrayList<>();
            for(Object ob : (Iterable)o){
                E item = load(ob);
                if(item!=null)list.add(item);
            }
            return list;
        }
        return null;
    }
    public static String loadString(Object o){
        if(o==null)return null;
        return o.toString();
    }
    public static Integer loadInt(Object o){
        if(o instanceof Number){
            return ((Number)o).intValue();
        }
        if(o instanceof String){
            try{
                return Integer.parseInt((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Short loadShort(Object o){
        if(o instanceof Number){
            return ((Number)o).shortValue();
        }
        if(o instanceof String){
            try{
                return Short.parseShort((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Long loadLong(Object o){
        if(o instanceof Number){
            return ((Number)o).longValue();
        }
        if(o instanceof String){
            try{
                return Long.parseLong((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Float loadFloat(Object o){
        if(o instanceof Number){
            return ((Number)o).floatValue();
        }
        if(o instanceof String){
            try{
                return Float.parseFloat((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Double loadDouble(Object o){
        if(o instanceof Number){
            return ((Number)o).doubleValue();
        }
        if(o instanceof String){
            try{
                return Double.parseDouble((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    protected String makeReadable(E value){
        return value.toString();
    }
    public void setValue(E value){
        globalValue = value;
    }
    public void setValue(CraftType craft, E value){
        craftValues.put(craft, value);
    }
    public E getValue(){
        return globalValue;
    }
    public E getValue(CraftType craft){
        return craftValues.get(craft);
    }
    public E get(CraftType craft){
        if(craftValues.containsKey(craft))return craftValues.get(craft);
        return globalValue;
    }
    public E get(Craft craft){
        return get(craft.type);
    }
    public String getDefaultConfigValue(){
        if(defaultConfigValue==null)return null;
        if(defaultConfigValue instanceof HashSet){
            String s = "";
            for(Object o : (HashSet)defaultConfigValue){
                s+="\n    - "+o;
            }
            return s;
        }
        return defaultConfigValue.toString();
    }
}