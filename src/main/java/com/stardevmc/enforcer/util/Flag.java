package com.stardevmc.enforcer.util;

public enum Flag {
    PUBLIC('p'), SILENT('s'), IGNORE_CONFIRM('c'), IGNORE_TRAINING('t'), WAVE('c'), NORMAL('n');
    
    private char shortHand;
    
    Flag(char shortHand) {
        this.shortHand = shortHand;
    }
    
    public static Flag matchFlag(String arg) {
        for (Flag flag : Flag.values()) {
            if (arg.startsWith("-" + flag.getShortHand())) {
                return flag;
            }
        }
        
        return null;
    }
    
    public char getShortHand() {
        return shortHand;
    }
}