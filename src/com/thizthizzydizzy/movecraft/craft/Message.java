package com.thizthizzydizzy.movecraft.craft;
public class Message{
    public final String text;
    private final Priority priority;
    public final boolean pilot;
    public final boolean crew;
    public Message(String text, Priority priority, boolean pilot, boolean crew){
        this.text = text;
        this.priority = priority;
        this.pilot = pilot;
        this.crew = crew;
    }
    public boolean overrides(Message other){
        return priority.overrides(other.priority);
    }
    public static enum Priority{
        /**
         * Will display unless overridden by a higher priority.
         */
        INFO,
        /**
         * Will display if blocks are modified on the craft.
         */
        CONSTRUCTION,
        /**
         * Will display if the craft is damaged.
         */
        COMBAT,
        /**
         * Overrides ALL messages, used for sinking info, etc.
         */
        CRITICAL;
        private boolean overrides(Priority priority){
            return ordinal()>priority.ordinal();
        }
    }
}