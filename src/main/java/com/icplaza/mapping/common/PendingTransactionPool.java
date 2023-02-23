package com.icplaza.mapping.common;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 失败的交易数据
 * */
public class PendingTransactionPool {
    private static Queue<JSONObject> queue = new LinkedList<>();
    private static HashMap<JSONObject, Boolean> pending = new HashMap<>();

    public static void add(JSONObject jsonObject) {
        // 限制1000
        if (pending.size() > 1000) {
            return;
        }
        if (pending.get(jsonObject) == null) {
            queue.offer(jsonObject);
        }
    }

    public static synchronized JSONObject getJob() {
        JSONObject ele = queue.poll();
        pending.remove(ele);
        return ele;
    }

    public static int size() {
        return queue.size();
    }
}
