package com.icplaza.mapping.service;

import com.icplaza.mapping.common.Constant;
import com.icplaza.mapping.common.Status;
import com.icplaza.mapping.model.OrderOutLogModel;
import com.icplaza.mapping.properties.ContractProperty;
import com.icplaza.mapping.properties.NodeProperty;
import com.icplaza.mapping.utils.AddressUtil;
import com.icplaza.mapping.utils.RSATool;
import com.icplaza.mapping.utils.TronTool;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Bytes32;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
// import org.tron.tronj.abi.TypeReference;
// import org.tron.tronj.abi.datatypes.Address;
// import org.tron.tronj.abi.datatypes.Function;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Numeric;
// import org.tron.tronj.abi.datatypes.Type;
// import org.tron.tronj.abi.datatypes.generated.Bytes32;
// import org.tron.tronj.abi.datatypes.generated.Uint256;
// import org.tron.tronj.client.TronClient;
// import org.tron.tronj.client.transaction.TransactionBuilder;
// import org.tron.tronj.proto.Chain;
// import org.tron.tronj.proto.Response;
// import org.tron.tronj.utils.Numeric;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * 波场上的合约调用
 */
@Slf4j
@Service
public class Web3TRONService {
    private final String FUNC_TRANSFER = "transfer";
    public static final String FUNC_BALANCE = "balance";
    private static final String FUNC_OUTAMT = "outAmt";
    public static final String FUNC__OUTACCOUNT = "_outAccount";
    // private TronClient web3j = null;
    private ApiWrapper web3j = null;
    @Autowired
    NodeProperty nodeProperty;
    @Autowired
    ContractProperty contractProperty;

    // @PostConstruct
    public void init() {
        if (RSATool.getSK() == null) {
            log.error("私钥不存在，无法初始化");
            return;
        }
        // web3j = getClient(RSATool.getSK());
        if (nodeProperty.getTron().equals("grpc.shasta.trongrid.io")) {
            log.info("连接波场测试网络");
            web3j = ApiWrapper.ofShasta(RSATool.getSK());
        } else {
            log.info("连接波场主网");
            web3j = ApiWrapper.ofMainnet(RSATool.getSK(), Constant.TRON_API_KEY);
        }
    }

    /**
     * 查询实例
     * */
    private ApiWrapper getCaller(){
        String privateKey = "111198e99f26ab40ab1de2affad70e6a4de2861fc1b1ef5d7873d527a8d20b9f";
        if (nodeProperty.getTron().equals("grpc.shasta.trongrid.io")) {
            log.info("连接波场测试网络");
            return ApiWrapper.ofShasta(privateKey);
        } else {
            log.info("连接波场主网");
            return ApiWrapper.ofMainnet(privateKey, Constant.TRON_API_KEY);
        }
    }


    // private TronClient getClient(String privateKey) {
    //     String[] list = nodeProperty.getTron().split(",");
    //     int m = 0;
    //     int n = list.length - 1;
    //     int temp = m + (int) (Math.random() * (n + 1 - m));
    //     String ip = "";
    //     if (list[temp] != null) {
    //         ip = list[temp];
    //     }
    //     if (ip.equals("grpc.shasta.trongrid.io")) {
    //         log.info("使用Shasta测试网络");
    //         return TronClient.ofShasta(privateKey);
    //     }
    //     return new TronClient(ip + ":50051", ip + ":50052", privateKey);
    // }

    /**
     * 转出USDT
     * 1.查询可转出余额-节省gas
     * 2.转出
     */
    public synchronized String transfer(OrderOutLogModel model) {

        //1.查询合约可用余额
        try {
            BigDecimal b = balance();
            // 余额不足
            if (b.compareTo(model.getValue_1()) < 0) {
                log.error("余额不足 {} < {}", b, model.getValue_1());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }

        //2.转出

        if (web3j == null) {
            init();
        }
        if (web3j == null) {
            log.error("web3j is null");
            return null;
        }


        String to = model.getReceiver();
        BigInteger amount = model.getValue_1().multiply(new BigDecimal(BigInteger.TEN.pow(6))).toBigInteger();
        String hash_0 = model.getHash_0();
        if (hash_0.startsWith("0x")) {
            hash_0 = hash_0.substring(2);
        }
        byte[] hash = Hex.decode(hash_0);

        Function function = new Function(
                FUNC_TRANSFER,
                Arrays.asList(new Address(to),
                        new Uint256(amount),
                        new Bytes32(hash)),
                Collections.emptyList());

        TransactionBuilder builder = web3j.triggerCall(TronTool.getAddressByPrivateKey(RSATool.getSK()), contractProperty.getOUTOnTRON(), function);
        builder.setFeeLimit(100000000L);
        builder.setMemo("");

        Chain.Transaction signedTxn = web3j.signTransaction(builder.build());

        // String txHash = getTxHash(signedTxn);

        return web3j.broadcastTransaction(signedTxn);


    }


    // private static String getTxHash(Chain.Transaction tx) {
    //     SHA256.Digest digest = new SHA256.Digest();
    //     digest.update(tx.getRawData().toByteArray());
    //     byte[] txid = digest.digest();
    //     return TronClient.toHex(txid);
    // }

    /**
     * 哈希状态
     */
    public synchronized int hashStatus(String hash) throws Exception {
        if (web3j == null) {
            init();
        }

        Response.TransactionInfo result = web3j.getTransactionInfoById(hash);
        Response.TransactionInfo.code code = result.getResult();

        // if (web3j != null) {
        //     web3j.close();
        // }

        if (code == null) {
            return Status.PENDING.ordinal();
        }

        if (code == Response.TransactionInfo.code.SUCESS) {
            return Status.SUCCESS.ordinal();
        }
        return Status.FAIL.ordinal();
    }


    public BigDecimal balance() {
        ApiWrapper caller = getCaller();
        Function function = new Function(FUNC_BALANCE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        Response.TransactionExtention txnExt = caller.constantCall("TU9sZ6GqBuPPGH2HdUKsJHv3demAJda4ZW", contractProperty.getOUTOnTRON(), function);
        caller.close();
        String result = Numeric.toHexString(txnExt.getConstantResult(0).toByteArray());
        if (result.startsWith("0x")) {
            result = result.substring(2);
        }
        BigInteger b = new BigInteger(result, 16);
        // web3j.close();
        return new BigDecimal(b).divide(new BigDecimal(BigInteger.TEN.pow(6)));
    }

    //
    public BigDecimal channelBalance() {
        if (web3j == null) {
            init();
        }
        Function function = new Function(FUNC_BALANCE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        Response.TransactionExtention txnExt = web3j.constantCall("TU9sZ6GqBuPPGH2HdUKsJHv3demAJda4ZW", contractProperty.getTRONChannel(), function);
        String result = Numeric.toHexString(txnExt.getConstantResult(0).toByteArray());
        if (result.startsWith("0x")) {
            result = result.substring(2);
        }
        BigInteger b = new BigInteger(result, 16);
        return new BigDecimal(b).divide(new BigDecimal(BigInteger.TEN.pow(6)));
    }
    /**
     * 绑定的地址
     */
    public String outAccount() {
        ApiWrapper caller = getCaller();
        final Function function = new Function(FUNC__OUTACCOUNT,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {
                }));
        Response.TransactionExtention txnExt = caller.constantCall("TU9sZ6GqBuPPGH2HdUKsJHv3demAJda4ZW", contractProperty.getOUTOnTRON(), function);
        caller.close();
        String result = Numeric.toHexString(txnExt.getConstantResult(0).toByteArray());
        return AddressUtil.from0xToTron(result.substring(26));
    }
}
