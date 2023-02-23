package com.icplaza.mapping.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public interface ChainDataService {
    /**
     * 根据区块高度获取全部交易
     * */
    JSONObject getBlockDataByBlockNumber(Long height);
    /**
     * 根据哈希获取合约交易
     * */
    JSONObject getTransactionContractByHash(String hash);
    /**
     * 区块最新高度
     * */
    Long queryBlockHeight();
    /**
     * 获取账户余额
     * */
    Double getBalance(String account);
    Map<String, Double> getBalanceAll(String account);
}
