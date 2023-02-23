package com.icplaza.mapping.common;


import java.util.LinkedList;
import java.util.Queue;

/**
 * 任务池
 */
public class EVMJobPool {
    private static Queue<Long> queue = new LinkedList<Long>();
    private static int jobMinSize = 0;

    public static void add(Long height) {
        if (height != null && height > 0) {
            queue.offer(height);
        }
    }

    public static void add(Long startHeight, Long endHeight) {
        for (long i = startHeight; i <= endHeight; i++) {
            add(i);
        }
    }

    public static synchronized Long getJob() {
        Long ele = queue.poll();
        return ele == null ? 0L : ele;
    }

    public static int jobSize() {
        return queue.size();
    }

    public static int jobMinSize() {
        return jobMinSize;
    }

    public static void setJobMinSize(int size) {
        jobMinSize = size;
    }
}
