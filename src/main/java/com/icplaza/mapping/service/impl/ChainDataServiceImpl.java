package com.icplaza.mapping.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icplaza.mapping.common.LogSwitch;
import com.icplaza.mapping.properties.ChainProperty;
import com.icplaza.mapping.service.ChainDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ChainDataServiceImpl implements ChainDataService {
    @Autowired
    ChainProperty chainProperty;

    /**
     * 根据区块高度获取全部交易
     */
    @Override
    public JSONObject getBlockDataByBlockNumber(Long height) {
        String url = chainProperty.getBaseUrl() + chainProperty.getBlockDataByBlockNumber() + height;
        String result = request(url);
        if (result == null) {
            return null;
        }

        try {
            return JSON.parseObject(result);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据哈希获取合约交易
     *
     * @param hash
     */
    @Override
    public JSONObject getTransactionContractByHash(String hash) {
        String url = chainProperty.getBaseUrl() + chainProperty.getTransactionContractByHash() + hash;
        String result = request(url);
        if (result == null) {
            return null;
        }
        try {
            return JSON.parseObject(result);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 区块最新高度
     */
    @Override
    public Long queryBlockHeight() {
        String url = chainProperty.getBaseUrl() + chainProperty.getBlockNumber();
        String result = request(url);
        if (result == null) {
            return null;
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(result);
        } catch (Exception e) {
            return null;
        }
        return Long.valueOf(jsonObject.getInteger("result"));
    }

    /**
     * 网络请求
     */
    private String request(String url) {
        if (LogSwitch.getOpen()) {
            log.info("请求地址==>" + url);
        }
        try {
            String result = HttpRequest.get(url).setMaxRedirectCount(2).header(chainProperty.getHeaderName(), chainProperty.getHeaderValue()).timeout(20000).execute().body();
            if (LogSwitch.getOpen()) {
                log.info("请求结果==>" + result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取账户余额
     *
     * @param account
     */
    @Override
    public Double getBalance(String account) {
        String url = chainProperty.getBaseUrl() + String.format(chainProperty.getBalance(), account, chainProperty.getContract());
        String result = request(url);
        if (result != null) {
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") == 200) {
                String balance = jsonObject.getJSONObject("result").getString("balance");
                if (balance != null && !balance.equals("")) {
                    try {
                        return Double.valueOf(balance);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
                return 0.0;
            }
        }
        return 0.0;
    }

    @Override
    public Map<String, Double> getBalanceAll(String account) {
        Map<String, Double> map = new HashMap<>();
        map.put("trx", 0.0);
        map.put("usdt", 0.0);

        String url = chainProperty.getBaseUrl() + chainProperty.getBalanceAll() + account;
        String jsonStr = request(url);
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        JSONObject result = jsonObject.getJSONObject("result");
        if (result != null) {
            JSONArray balances = result.getJSONArray("balances");
            if (balances != null) {
                for (int i = 0; i < balances.size(); i++) {
                    String coinType = balances.getJSONObject(i).getString("coinType");
                    String currentValueStr = balances.getJSONObject(i).getString("balance");
                    if (coinType.equalsIgnoreCase("trx")) {
                        map.put("trx", Double.valueOf(currentValueStr));
                    }
                    if (coinType.equalsIgnoreCase("usdt")) {
                        map.put("usdt", Double.valueOf(currentValueStr));
                    }
                }
            }
        }
        return map;
    }
}
