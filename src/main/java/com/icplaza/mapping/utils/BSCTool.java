package com.icplaza.mapping.utils;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.icplaza.mapping.bo.BSCResult;
import com.icplaza.mapping.bo.Result;
import com.icplaza.mapping.common.Constant;
import com.icplaza.mapping.common.MyBloomFilter;
import com.icplaza.mapping.common.Status;
import com.icplaza.mapping.model.OrderInLogModel;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.tronj.crypto.SECP256K1;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class BSCTool {
    @Autowired

    public static Map<String, String> createAddress() throws Exception {
        Map<String, String> result = new HashMap<>();
        SECP256K1.KeyPair kp = SECP256K1.KeyPair.generate();
        String privateKey = Hex.toHexString(kp.getPrivateKey().getEncoded());
        String address = getAddressByPrivateKey(privateKey);
        result.put("address", address);
        result.put("privateKey", privateKey);
        return result;
    }

    public static String getAddressByPrivateKey(String privateKey) {
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        BigInteger bigInteger = new BigInteger(privateKey, 16);
        ECKeyPair ecKeyPair = ECKeyPair.create(bigInteger);
        return "0x" + Keys.getAddress(ecKeyPair);
    }

    /**
     * 解析数据，更新高度，添加交易
     */
    public static List<Object> parseTransactions(BigInteger fromBlock, BigInteger toBlock, String contractAddress, boolean incoming) throws Exception {
        List<Object> listObjs = new ArrayList<>();
        String url = "https://api.bscscan.com/api?module=logs&action=getLogs&fromBlock=" + fromBlock + "&toBlock=" + toBlock + "&address=" + contractAddress + "&topic0=0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef&apikey=" + Constant.API_KEY;
        String rs = HttpUtil.get(url, 10000);
        log.info("url {}", url);
        log.info("from {} to {}",fromBlock,toBlock);
        BSCResult result = new Gson().fromJson(rs, BSCResult.class);
        if (result.getStatus().equals("1")) {
            List<Result> list = result.getResult();
            log.info("数据量： {}", list.size());
            if (list.size() > 1000) {
                log.error("{} ~ {} 可能存在数据丢失", fromBlock, toBlock);
            }
            for (Result r : list) {
                String hash = r.getTransactionHash();
                List<String> topics = r.getTopics();
                String fromAddress = "0x" + topics.get(1).substring(26);
                String toAddress = "0x" + topics.get(2).substring(26);

                // 过滤器
                if (!MyBloomFilter.mightContain(toAddress)) {
                    continue;
                }

                String value = r.getData().substring(2);
                BigInteger valueB = new BigInteger(value, 16);
                BigDecimal valueH = new BigDecimal(valueB.toString()).divide(new BigDecimal("1000000000000000000"));

                if (hashStatus(hash)) {
                    if (incoming) {
                        // 入
                        OrderInLogModel model = new OrderInLogModel();
                        model.setFrom_0(fromAddress);
                        model.setTo_0(toAddress);
                        model.setHash_0(hash);
                        model.setValue_0(valueH);
                        model.setHashStatus_0(Status.SUCCESS.ordinal());
                        listObjs.add(model);
                    } else {
                        // 出
                    }
                } else {
                    // 只记录成功
                    // if (incoming) {
                    //     // 入
                    //     OrderInLogModel model = new OrderInLogModel();
                    //     model.setFrom_0(fromAddress);
                    //     model.setTo_0(toAddress);
                    //     model.setHash_0(hash);
                    //     model.setValue_0(valueH);
                    //     model.setHashStatus_0(Status.FAIL.ordinal());
                    //     listObjs.add(model);
                    // } else {
                    //     // 出
                    // }
                }
            }
        } else {
            log.error("{} ~ {} 没有交易?", fromBlock, toBlock);
        }
        return listObjs;
    }

    /**
     * 最新区块高度10s左右
     */
    public static BigInteger getLatestBlockNumber() throws Exception {
        long seconds = System.currentTimeMillis() / 1000;
        String timestamp = String.valueOf(seconds);
        String url = "https://api.bscscan.com/api?module=block&action=getblocknobytime&timestamp=" + timestamp + "&closest=before&apikey=" + Constant.API_KEY;
        String rs = HttpUtil.get(url, 10000);
        Map v = new Gson().fromJson(rs, Map.class);
        String no = v.get("result").toString();
        if (no == null || no.contains("Error")) {
            return null;
        }
        return new BigInteger(no);
    }

    /**
     * BSC链上哈希状态查询
     */
    public static boolean hashStatus(String hash) throws Exception {
        Thread.sleep(100);
        String url = "https://api.bscscan.com/api?module=transaction&action=gettxreceiptstatus&txhash=" + hash + "&apikey=" + Constant.API_KEY;
        String rs = HttpUtil.get(url, 10000);
        System.out.println(rs);
        Map map = new Gson().fromJson(rs, Map.class);
        Map result = (Map) map.get("result");
        if (result.get("status").equals("1")) {
            return true;
        }
        return false;
    }

}
