package com.thizthizzydizzy.movecraft;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
//I could probably just use MC's JSON system, but I already have one ¯\_(ツ)_/¯
public class JSON{
    public static boolean debug = false;
    public static JSONObject parse(String str) throws IOException{
        ArrayList<Character> json = new ArrayList<>();
        for(char c : str.toCharArray())json.add(c);
        return new JSONObject(json);
    }
    public static JSONObject parse(File file){
        try{
            return parse(new FileInputStream(file));
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
    public static JSONObject parse(InputStream stream){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
            ArrayList<Character> json = new ArrayList<>();
            int c;
            if(debug)System.out.print("Reading JSON file...");
            while((c = reader.read())!=-1){
                json.add((char)c);
            }
            if(debug)System.out.println("Done");
            if(debug)System.out.println("Parsing file...");
            JSONObject obj = new JSONObject(json);
            if(debug)System.out.println("Finished Parsing!");
            return obj;
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
    public static class JSONObject extends HashMap<String, Object>{
        public JSONObject(){}
        private JSONObject(ArrayList<Character> json) throws IOException{
            parse(json);
        }
        private void parse(ArrayList<Character> json) throws IOException{
            trim(json);
            if(json.remove(0)!='{')throw new IOException("'{' expected!");
            while(!json.isEmpty()){
                trim(json, ',');
                if(json.get(0)=='}'){
                    if(debug)System.out.println("--Reached End of object");
                    json.remove(0);
                    break;
                }
                if(json.get(0)=='\"'){
                    json.remove(0);
                    String key = "";
                    while(true){
                        while(json.get(0)=='\\'){
                            key+=json.remove(0);
                            key+=json.remove(0);
                        }
                        if(json.get(0)=='\"'){
                            json.remove(0);
                            break;
                        }
                        key+=json.remove(0);
                    }
                    trim(json);
                    if(json.get(0)!=':')throw new IOException("':' expected");
                    json.remove(0);//remove the :
                    trim(json);
                    //what's this new entry?
                    if(json.get(0)=='\"'){//it's a string!
                        json.remove(0);
                        String value = "";
                        while(true){
                            while(json.get(0)=='\\'){
                                value+=json.remove(0);
                                value+=json.remove(0);
                            }
                            if(json.get(0)=='\"'){
                                json.remove(0);
                                break;
                            }
                            value+=json.remove(0);
                        }
                        put(key.replace("\\\"", "\""), value.replace("\\\"", "\""));
                        if(debug)System.out.println("Found new entry: \""+key+"\": \""+value+"\"");
                    }else if(json.get(0)=='{'){
                        //it's an object!
                        if(debug)System.out.println("Found new object: \""+key+"\"");
                        JSONObject newObject = new JSONObject(json);
                        put(key.replace("\\\"", "\""), newObject);
                    }else if(Character.isDigit(json.get(0))||json.get(0)=='-'||json.get(0)=='.'){
                        //it's a number!
                        String num = "";
                        while(Character.isDigit(json.get(0))||json.get(0)=='.'||json.get(0)=='-'){
                            num+=json.remove(0);
                        }
                        if(num.startsWith("-."))throw new IOException("Numbers must contain at least one digit before the decimal point!");
                        if(json.get(0)=='e'||json.get(0)=='E'){
                            //exponent, gosh darnit!
                            num+=Character.toUpperCase(json.remove(0));
                            while(Character.isDigit(json.get(0))||json.get(0)=='.'||json.get(0)=='-'||json.get(0)=='+'){
                                num+=json.remove(0);
                            }
                        }
                        Object value;
                        if(json.get(0)=='f'||json.get(0)=='F'){
                            value = Float.parseFloat(num);
                            json.remove(0);
                        }else if(json.get(0)=='d'||json.get(0)=='D'){
                            value = Double.parseDouble(num);
                            json.remove(0);
                        }else if(json.get(0)=='b'||json.get(0)=='B'){
                            value = Byte.parseByte(num);
                            json.remove(0);
                        }else if(json.get(0)=='s'||json.get(0)=='S'){
                            value = Short.parseShort(num);
                            json.remove(0);
                        }else if(json.get(0)=='l'||json.get(0)=='L'){
                            value = Long.parseLong(num);
                            json.remove(0);
                        }else if(json.get(0)=='i'||json.get(0)=='I'){
                            value = Integer.parseInt(num);
                            json.remove(0);
                        }else if(num.contains(".")){
                            value = Double.parseDouble(num);
                        }else{
                            value = Integer.parseInt(num);
                        }
                        put(key.replace("\\\"", "\""), value);
                        if(debug)System.out.println("Found new entry: \""+key+"\": "+value);
                    }else if(json.get(0)=='['){
                        //it's an array!
                        if(debug)System.out.println("Found new array: \""+key+"\"");
                        JSONArray newArray = new JSONArray(json);
                        put(key.replace("\\\"", "\""), newArray);
                    }else if(json.get(0)=='t'||json.get(0)=='T'){
                        boolean yay = false;
                        json.remove(0);
                        if(json.get(0)=='r'||json.get(0)=='R'){
                            json.remove(0);
                            if(json.get(0)=='u'||json.get(0)=='U'){
                                json.remove(0);
                                if(json.get(0)=='e'||json.get(0)=='E'){
                                    json.remove(0);
                                    yay = true;
                                }
                            }
                        }
                        if(!yay)throw new IOException("Failed to parse JSON file: Unknown entry - "+sub(json, 25)+"...");
                        put(key.replace("\\\"", "\""), true);
                    }else if(json.get(0)=='f'||json.get(0)=='F'){
                        boolean yay = false;
                        json.remove(0);
                        if(json.get(0)=='a'||json.get(0)=='A'){
                            json.remove(0);
                            if(json.get(0)=='l'||json.get(0)=='L'){
                                json.remove(0);
                                if(json.get(0)=='s'||json.get(0)=='S'){
                                    json.remove(0);
                                    if(json.get(0)=='e'||json.get(0)=='E'){
                                        json.remove(0);
                                        yay = true;
                                    }
                                }
                            }
                        }
                        if(!yay)throw new IOException("Failed to parse JSON file: Unknown entry - "+sub(json, 25)+"...");
                        put(key.replace("\\\"", "\""), false);
                    }else{
                        throw new IOException("Failed to parse JSON file: Unknown entry - "+sub(json,25)+"...");
                    }
                    continue;
                }
                throw new IOException("Failed to parse JSON file: I don't know what this is! - "+sub(json,25)+"...");
            }
        }
        public void write(File file) throws IOException{
            write(new FileOutputStream(file));
        }
        public void write(OutputStream stream) throws IOException{
            try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))){
                writer.write(write());
            }
        }
        private String write(){
            if(isEmpty()){
                return "{}";
            }
            String json = "{";
            for(String str : keySet()){
                json+="\""+str+"\":";
                Object o = get(str);
                if(o instanceof String){
                    json+="\""+(String)o+"\"";
                }else if(o instanceof Boolean){
                    json+=(Boolean)o;
                }else if(o instanceof Double){
                    json+=(Double)o;
                }else if(o instanceof Integer){
                    json+=(Integer)o;
                }else if(o instanceof Long){
                    json+=(Long)o;
                }else if(o instanceof Float){
                    json+=(Float)o;
                }else if(o instanceof JSONObject){
                    json+=((JSONObject)o).write();
                }else if(o instanceof JSONArray){
                    json+=((JSONArray)o).write();
                }
                json+=",";
            }
            json = json.substring(0, json.length()-1)+"}";
            return json;
        }
        public JSONObject getJSONObject(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof JSONObject)return (JSONObject)o;
            return null;
        }
        public JSONArray getJSONArray(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof JSONArray)return (JSONArray)o;
            return null;
        }
        public String getString(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof String)return (String)o;
            return null;
        }
        public Boolean getBoolean(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof Boolean)return (Boolean)o;
            return null;
        }
        public Double getDouble(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof Number)return ((Number)o).doubleValue();
            return null;
        }
        public Float getFloat(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof Number)return ((Number)o).floatValue();
            return null;
        }
        public Integer getInt(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof Number)return ((Number)o).intValue();
            return null;
        }
        public Long getLong(String key){
            Object o = get(key);
            if(o==null)return null;
            if(o instanceof Number)return ((Number)o).longValue();
            return null;
        }
        
        public boolean hasJSONObject(String key){
            return getJSONObject(key)!=null;
        }
        public boolean hasJSONArray(String key){
            return getJSONArray(key)!=null;
        }
        public boolean hasString(String key){
            return getString(key)!=null;
        }
        public boolean hasBoolean(String key){
            return getBoolean(key)!=null;
        }
        public boolean hasDouble(String key){
            return getDouble(key)!=null;
        }
        public boolean hasFloat(String key){
            return getFloat(key)!=null;
        }
        public boolean hasInt(String key){
            return getInt(key)!=null;
        }
        public boolean hasLong(String key){
            return getLong(key)!=null;
        }
        //because I like set instead of put
        public Object set(String key, Object value){
            return put(key, value);
        }
        @Override
        public String toString(){
            return write();
        }
    }
    public static class JSONArray extends ArrayList<Object>{
        public JSONArray(){}
        private JSONArray(ArrayList<Character> json) throws IOException{
            parse(json);
        }
        private void parse(ArrayList<Character> json) throws IOException{
            if(json.remove(0)!='[')throw new IOException("'[' expected!");
            while(!json.isEmpty()){
                trim(json, ',');
                if(json.get(0)=='['){
                    //it's an array!
                    if(debug)System.out.println("Found new array!");
                    json.remove(0);
                    JSONArray newArray = new JSONArray(json);
                    add(newArray);
                }else if(json.get(0)==']'){
                    if(debug)System.out.println("-Reached End of array");
                    json.remove(0);
                    break;
                }else if(json.get(0)=='\"'){
                    //it's a string!
                    json.remove(0);
                    String value = "";
                    while(true){
                        while(json.get(0)=='\\'){
                            value+=json.remove(0);
                            value+=json.remove(0);
                        }
                        if(json.get(0)=='\"'){
                            json.remove(0);
                            break;
                        }
                        value+=json.remove(0);
                    }
                    add(value.replace("\\\"", "\""));
                    if(debug)System.out.println("Found new item: \""+value+"\"");
                }else if(json.get(0)=='{'){
                    //it's an object!
                    if(debug)System.out.println("Found new object!");
                    JSONObject newObject = new JSONObject(json);
                    add(newObject);
                }else if(Character.isDigit(json.get(0))||json.get(0)=='-'||json.get(0)=='.'){
                    //it's a number!
                    String num = "";
                    while(Character.isDigit(json.get(0))||json.get(0)=='.'||json.get(0)=='-'){
                        num+=json.remove(0);
                    }
                    if(num.startsWith("-."))throw new IOException("Numbers must contain at least one digit before the decimal point!");
                    if(json.get(0)=='e'||json.get(0)=='E'){
                        //exponent, gosh darnit!
                        num+=Character.toUpperCase(json.remove(0));
                        while(Character.isDigit(json.get(0))||json.get(0)=='.'||json.get(0)=='-'||json.get(0)=='+'){
                            num+=json.remove(0);
                        }
                    }
                    Object value;
                    if(json.get(0)=='f'||json.get(0)=='F'){
                        value = Float.parseFloat(num);
                        json.remove(0);
                    }else if(json.get(0)=='d'||json.get(0)=='D'){
                        value = Double.parseDouble(num);
                        json.remove(0);
                    }else if(json.get(0)=='b'||json.get(0)=='B'){
                        value = Byte.parseByte(num);
                        json.remove(0);
                    }else if(json.get(0)=='s'||json.get(0)=='S'){
                        value = Short.parseShort(num);
                        json.remove(0);
                    }else if(json.get(0)=='l'||json.get(0)=='L'){
                        value = Long.parseLong(num);
                        json.remove(0);
                    }else if(json.get(0)=='i'||json.get(0)=='I'){
                        value = Integer.parseInt(num);
                        json.remove(0);
                    }else if(num.contains(".")){
                        value = Double.parseDouble(num);
                    }else{
                        value = Integer.parseInt(num);
                    }
                    add(value);
                    if(debug)System.out.println("Found new item: "+value);
                }else if(json.get(0)=='t'||json.get(0)=='T'){
                    boolean yay = false;
                    json.remove(0);
                    if(json.get(0)=='r'||json.get(0)=='R'){
                        json.remove(0);
                        if(json.get(0)=='u'||json.get(0)=='U'){
                            json.remove(0);
                            if(json.get(0)=='e'||json.get(0)=='E'){
                                json.remove(0);
                                yay = true;
                            }
                        }
                    }
                    if(!yay)throw new IOException("Failed to parse JSON file: Unknown entry - "+sub(json, 25)+"...");
                    add(true);
                    if(debug)System.out.println("Found new item: true");
                }else if(json.get(0)=='f'||json.get(0)=='F'){
                    boolean yay = false;
                    json.remove(0);
                    if(json.get(0)=='a'||json.get(0)=='A'){
                        json.remove(0);
                        if(json.get(0)=='l'||json.get(0)=='L'){
                            json.remove(0);
                            if(json.get(0)=='s'||json.get(0)=='S'){
                                json.remove(0);
                                if(json.get(0)=='e'||json.get(0)=='E'){
                                    json.remove(0);
                                    yay = true;
                                }
                            }
                        }
                    }
                    if(!yay)throw new IOException("Failed to parse JSON file: Unknown entry - "+sub(json, 25)+"...");
                    add(false);
                    if(debug)System.out.println("Found new item: false");
                }else{
                    throw new IOException("Failed to parse JSON file: Unknown entry - "+sub(json,25)+"...");
                }
            }
        }
        private String write(){
            String json = "[";
            for(Object o : this){
                if(o instanceof String){
                    json+="\""+(String)o+"\"";
                }else if(o instanceof Boolean){
                    json+=(Boolean)o;
                }else if(o instanceof Double){
                    json+=(Double)o;
                }else if(o instanceof Integer){
                    json+=(Integer)o;
                }else if(o instanceof Long){
                    json+=(Long)o;
                }else if(o instanceof Float){
                    json+=(Float)o;
                }else if(o instanceof JSONObject){
                    json+=((JSONObject)o).write();
                }else if(o instanceof JSONArray){
                    json+=((JSONArray)o).write();
                }
                json+=",";
            }
            if(json.contains(","))json = json.substring(0, json.length()-1);
            json = json+"]";
            return json;
        }
        @Override
        public String toString(){
            return write();
        }
    }
    private static String sub(ArrayList<Character> json, int limit){
        if(json.isEmpty())return "";
        String s = "";
        limit = Math.min(limit, json.size());
        for(int i = 0; i<limit; i++){
            s+=json.remove(0);
        }
        return s;
    }
    private static void trim(ArrayList<Character> json){
        char c = json.get(0);
        while(c==' '||c=='\n'||c=='\r'||c=='\t'){
            json.remove(0);
            c = json.get(0);
        }
    }
    private static void trim(ArrayList<Character> str, char... chars){
        WHILE:while(true){
            char c = str.get(0); 
            if(Character.isWhitespace(c)){
                str.remove(0);
                continue;
            }
            for(char ch : chars){
                if(c==ch){
                    str.remove(0);
                    continue WHILE;
                }
            }
            break;
        }
    }
}