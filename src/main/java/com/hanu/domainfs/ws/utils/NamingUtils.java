package com.hanu.domainfs.ws.utils;

import static java.lang.Character.*;

public final class NamingUtils {
    public static String fieldNameFrom(String typeName, String suffix) {
        return toLowerCase(typeName.charAt(0)) + typeName.substring(1) + suffix;
    }
}
