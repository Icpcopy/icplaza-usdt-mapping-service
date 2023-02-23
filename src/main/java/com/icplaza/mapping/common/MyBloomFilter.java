package com.icplaza.mapping.common;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;

public class MyBloomFilter {
    private static int expectedInsertions = 1000000;
    private static double fpp = 0.01;
    private static boolean ready = false;
    private static BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, fpp);

    public static boolean ready() {
        return ready;
    }

    public static void setReady(boolean bool) {
        ready = bool;
    }

    public static void put(String str) {
        bloomFilter.put(str);
    }

    public static boolean mightContain(String str) {
        return bloomFilter.mightContain(str);
    }
}
