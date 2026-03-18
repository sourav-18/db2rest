package com.homihq.db2rest.jdbc.util;

import java.util.UUID;

public class
AliasGenerator {
    private static final int UUID_LENGTH = 36;
    private static final int UUID_NUM_CHARS = 12;
    private static final int PREFIX_LENGTH = 4;


    public static String getAlias(String sqlIdentifier) {
        return
                sqlIdentifier.length() > PREFIX_LENGTH ?
                        sqlIdentifier.substring(0, PREFIX_LENGTH) + "_" + generateUUID()
                        : sqlIdentifier + "_" + generateUUID();

    }

    private static String generateUUID (){
        int startIndex = UUID_LENGTH - UUID_NUM_CHARS;
        return UUID.randomUUID().toString().substring(startIndex,UUID_LENGTH);
    }
}
