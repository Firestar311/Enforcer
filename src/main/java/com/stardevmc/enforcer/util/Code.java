package com.stardevmc.enforcer.util;

import java.util.Random;

public class Code {
    
    private static final char[] CODE_CHARS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    public static String generateNewCode(int amount) {
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            char c = CODE_CHARS[random.nextInt(CODE_CHARS.length - 1)];
            if (random.nextInt(100) < 50) {
                c = Character.toUpperCase(c);
            }
            codeBuilder.append(c);
        }
    
        return codeBuilder.toString();
    }
}