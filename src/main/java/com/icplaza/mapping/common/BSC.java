package com.icplaza.mapping.common;

import java.math.BigInteger;

public class BSC {
    public static BigInteger startBlockNumber;
    public static BigInteger endBlockNumber;
    public static boolean start = false;

    public static void setBlockNumber(BigInteger _startBlockNumber, BigInteger _endBlockNumber) {
        start = true;
        startBlockNumber = _startBlockNumber;
        endBlockNumber = _endBlockNumber;
    }

    public static void stop() {
        start = false;
    }

}
