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
public class MultiblockTooltip extends Special{
    public MultiblockTooltip(){
        super("aeronautics:multiblock_tooltip");
    }
    @Override
    protected void load(JSON.JSONObject json){}
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Special newInstance(){
        return new MultiblockTooltip();
    }
    @Override
    public void init(CraftSpecial special){}
    @Override
    public void tick(CraftSpecial special){}
    private int getWidth(String str){
        int width = 0;
        boolean skip = false;
        for(char c : str.toCharArray()){
            if(skip){
                skip = false;
                continue;
            }
            if(c==ChatColor.COLOR_CHAR){
                skip = true;
                continue;
            }
            width+=
            switch(c){
                case ' ' -> 4;
                case '!' -> 2;
                case '"' -> 5;
                case '#' -> 6;
                case '$' -> 6;
                case '%' -> 6;
                case '&' -> 6;
                case '\'' -> 3;
                case '(' -> 5;
                case ')' -> 5;
                case '*' -> 5;
                case '+' -> 6;
                case ',' -> 2;
                case '-' -> 6;
                case '.' -> 2;
                case '/' -> 6;
                case '0' -> 6;
                case '1' -> 6;
                case '2' -> 6;
                case '3' -> 6;
                case '4' -> 6;
                case '5' -> 6;
                case '6' -> 6;
                case '7' -> 6;
                case '8' -> 6;
                case '9' -> 6;
                case ':' -> 2;
                case ';' -> 2;
                case '<' -> 5;
                case '=' -> 6;
                case '>' -> 5;
                case '?' -> 6;
                case '@' -> 7;
                case 'A' -> 6;
                case 'B' -> 6;
                case 'C' -> 6;
                case 'D' -> 6;
                case 'E' -> 6;
                case 'F' -> 6;
                case 'G' -> 6;
                case 'H' -> 6;
                case 'I' -> 4;
                case 'J' -> 6;
                case 'K' -> 6;
                case 'L' -> 6;
                case 'M' -> 6;
                case 'N' -> 6;
                case 'O' -> 6;
                case 'P' -> 6;
                case 'Q' -> 6;
                case 'R' -> 6;
                case 'S' -> 6;
                case 'T' -> 6;
                case 'U' -> 6;
                case 'V' -> 6;
                case 'W' -> 6;
                case 'X' -> 6;
                case 'Y' -> 6;
                case 'Z' -> 6;
                case '[' -> 4;
                case '\\' -> 6;
                case ']' -> 4;
                case '^' -> 6;
                case '_' -> 6;
                case '`' -> 3;
                case 'a' -> 6;
                case 'b' -> 6;
                case 'c' -> 6;
                case 'd' -> 6;
                case 'e' -> 6;
                case 'f' -> 5;
                case 'g' -> 6;
                case 'h' -> 6;
                case 'i' -> 2;
                case 'j' -> 6;
                case 'k' -> 5;
                case 'l' -> 3;
                case 'm' -> 6;
                case 'n' -> 6;
                case 'o' -> 6;
                case 'p' -> 6;
                case 'q' -> 6;
                case 'r' -> 6;
                case 's' -> 6;
                case 't' -> 4;
                case 'u' -> 6;
                case 'v' -> 6;
                case 'w' -> 6;
                case 'x' -> 6;
                case 'y' -> 6;
                case 'z' -> 6;
                case '{' -> 5;
                case '|' -> 2;
                case '}' -> 5;
                case '~' -> 7;
                default -> 0;
            };
        }
        return width;
    }
    private String space(int len){
        if(len<0)return negSpace(-len);
        if(len==0)return "";
        if(len>255)return space(255)+space(len-255);
        String sp = Integer.toHexString(len);
        if(sp.length()<2)sp = "0"+sp;
        return "\\uE1"+sp;
    }
    private String negSpace(int len){
        if(len<0)throw new IllegalArgumentException("Cannot make negative negative space!");
        if(len==0)return "";
        if(len>256)return negSpace(256)+negSpace(len-256);
        String sp = Integer.toHexString(len-1);
        if(sp.length()<2)sp = "0"+sp;
        return "\\uE0"+sp;
    }
    @Override
    public void event(CraftSpecial special, Event event){}
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special, int damage, boolean damaged){}
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
    @Override
    public void getMessages(CraftSpecial special, ArrayList<Message> messages){
        for(var player : special.getCraft().getCrew()){
            Block block = player.getTargetBlockExact(3);
            if(special.getCraft().contains(block)){
                Multiblock mb = special.getCraft().getMultiblock(block);
                if(mb!=null){
                    String[] stats = mb.getBlockStats(false);
                    int[] lens = new int[stats.length];
                    int maxLen = 0;
                    for(int i = 0; i<stats.length; i++){
                        lens[i] = getWidth(stats[i]);
                        if(lens[i]>maxLen)maxLen = lens[i];
                    }
                    int[] spaces = new int[stats.length];
                    int totalLen = maxLen+getWidth("][");
                    for(int i = 0; i<stats.length; i++){
                        spaces[i] = maxLen-lens[i];
                    }
                    int numSquigglies = totalLen/getWidth("-");
                    String squigglies = "";
                    for(int i = 0; i<numSquigglies; i++)squigglies+="-";
                    int squigLen = getWidth(squigglies);
                    int squigSpacing = (totalLen-squigLen)/2;
                    String json = "";
                    json+="{\"font\":\"aeronautics:space\",\"text\":\""+negSpace(totalLen/2)+space(squigSpacing)+"\"}";
                    json+=",{\"font\":\"aeronautics:ln1\",\"text\":\""+squigglies+"\"}";
                    json+=",{\"font\":\"aeronautics:space\",\"text\":\""+negSpace(squigLen+squigSpacing)+"\"}";
                    for(int i = 0; i<stats.length; i++){
                        String mainFont = "aeronautics:ln"+(i+2);
                        json+=",{\"font\":\""+mainFont+"\",\"text\":\"["+stats[i]+"\"}";
                        json+=",{\"font\":\"aeronautics:space\",\"text\":\""+space(spaces[i])+"\"}";
                        json+=",{\"font\":\""+mainFont+"\",\"text\":\"]\"}";
                        json+=",{\"font\":\"aeronautics:space\",\"text\":\""+negSpace(totalLen)+"\"}";
                    }
                    json+=",{\"font\":\"aeronautics:space\",\"text\":\""+space(squigSpacing)+"\"}";
                    json+=",{\"font\":\"aeronautics:ln"+(stats.length+2)+"\",\"text\":\""+squigglies+"\"}";
                    json+=",{\"font\":\"aeronautics:space\",\"text\":\""+negSpace(squigLen+squigSpacing)+"\"}";
                    json = "["+json+"]";
                    messages.add(new Message(Message.Priority.INFO_UNIVERSAL, true, true, player, json, true));
                }
            }
        }
    }
    @Override
    public void getMultiblockTypes(CraftSpecial special, ArrayList<Multiblock> multiblockTypes){}
}