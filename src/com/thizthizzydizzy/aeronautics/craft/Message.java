package com.thizthizzydizzy.aeronautics.craft;
import org.bukkit.entity.Player;
public class Message{
    public Player player;
    public final String text;
    public final Priority priority;
    public final boolean pilot;
    public final boolean crew;
    public final boolean raw;
    public Message(Priority priority, boolean pilot, boolean crew, String text, boolean raw){
        this.text = text;
        this.priority = priority;
        this.pilot = pilot;
        this.crew = crew;
        this.raw = raw;
    }
    public Message(Priority priority, boolean pilot, boolean crew, String text){
        this(priority, pilot, crew, text, false);
    }
    public Message(Priority priority, boolean pilot, boolean crew, Player player, String text, boolean raw){
        this(priority, pilot, crew, text, raw);
        this.player = player;
    }
    public static enum Priority{
        /**
         * Will display if the ship is neither in combat nor being modified
         */
        INFO(true, false, false),
        /**
         * Will display if the ship is not in combat.
         */
        INFO_CONSTRUCTION(true, true, false),
        /**
         * Will display if the ship is not being modified.
         */
        INFO_COMBAT(true, false, true),
        /**
         * Will always display.
         */
        INFO_UNIVERSAL(true, true, true),
        /**
         * Will display if blocks are modified on the craft.
         */
        CONSTRUCTION(false, true, false),
        /**
         * Will display if the craft is damaged.
         */
        COMBAT(false, false, true),
        /**
         * Overrides ALL messages, used for sinking info, etc.
         */
        CRITICAL(true, true, true);
        public final boolean info;
        public final boolean construction;
        public final boolean combat;
        private Priority(boolean info, boolean construction, boolean combat){
            this.info = info;
            this.construction = construction;
            this.combat = combat;
        }
        public boolean shouldDisplay(Craft.Mode mode){
            switch(mode){
                case COMBAT:
                    return combat;
                case CONSTRUCTION:
                    return construction;
                case IDLE:
                    return info;
            }
            throw new IllegalArgumentException("Unknown mode "+mode.toString()+"!");
        }
    }
}