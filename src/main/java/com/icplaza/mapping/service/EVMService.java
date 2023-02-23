package com.icplaza.mapping.service;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.icplaza.mapping.bo.EVMBlockNumber;
import com.icplaza.mapping.properties.NodeProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

/**
 * emv json-rpc
 */
@Service
public class EVMService {
    @Autowired
    NodeProperty nodeProperty;

    public String eth_getBlockByNumber(BigInteger number) {
        String body = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByNumber\",\"params\":[\"0x" + number.toString(16) + "\", true],\"id\":1}";
        try {
            return HttpUtil.post(nodeProperty.getIcplaza(), body, 10000);
        } catch (Exception e) {
            return null;
        }
    }

    public String eth_getTransactionReceipt(String hash) {
        String body = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":[\"" + hash + "\"],\"id\":1}";
        try {
            return HttpUtil.post(nodeProperty.getIcplaza(), body, 10000);
        } catch (Exception e) {
            return null;
        }
    }

    public BigInteger eth_blockNumber() {
        String body = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\",\"params\":[],\"id\":83}";
        try {
            String rs = HttpUtil.post(nodeProperty.getIcplaza(), body, 10000);
            if (rs == null) {
                return null;
            }
            EVMBlockNumber b = new Gson().fromJson(rs, EVMBlockNumber.class);
            if (b == null || b.getResult() == null) {
                return null;
            }
            return new BigInteger(b.getResult().substring(2), 16);
        } catch (Exception e) {
            return null;
        }
    }
}