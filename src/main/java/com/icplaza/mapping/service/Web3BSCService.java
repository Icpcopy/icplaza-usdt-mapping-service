package com.icplaza.mapping.service;

import com.google.gson.Gson;
import com.icplaza.mapping.common.Status;
import com.icplaza.mapping.model.OrderOutLogModel;
import com.icplaza.mapping.properties.ContractProperty;
import com.icplaza.mapping.properties.NodeProperty;
import com.icplaza.mapping.utils.BSCTool;
import com.icplaza.mapping.utils.RSATool;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.web3j.tx.gas.DefaultGasProvider.GAS_LIMIT;
import static org.web3j.tx.gas.DefaultGasProvider.GAS_PRICE;

@Slf4j
@Service
public class Web3BSCService {
    private final String FUNC_TRANSFER = "transfer";
    public static final String FUNC_BALANCE = "balance";
    public static final String FUNC_LOCK = "lock";
    public static final String FUNC__OUTACCOUNT = "_outAccount";
    private long CHAIN_ID = 56;
    private Web3j web3j = null;
    @Autowired
    NodeProperty nodeProperty;
    @Autowired
    ContractProperty contractProperty;

    @PostConstruct
    public void init() {
        web3j = Web3j.build(new HttpService(nodeProperty.getBsc()));
        if (nodeProperty.getChainId() != null) {
            CHAIN_ID = nodeProperty.getChainId();
        }
        log.info("初始化网络 node {} chainId {}", nodeProperty.getBsc(), nodeProperty.getChainId());
    }

    /**
     * 转出USDT
     * 1.检查合约可用余额-节省gas
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
        String to = model.getReceiver();
        BigInteger amount = model.getValue_1().multiply(new BigDecimal(BigInteger.TEN.pow(18))).toBigInteger();
        String hash_0 = model.getHash_0();
        if (hash_0.startsWith("0x")) {
            hash_0 = hash_0.substring(2);
        }
        byte[] hash = Hex.decode(hash_0);

        Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFER,
                Arrays.asList(new org.web3j.abi.datatypes.Address(to),
                        new org.web3j.abi.datatypes.generated.Uint256(amount),
                        new org.web3j.abi.datatypes.generated.Bytes32(hash)),
                Collections.emptyList());

        String encodedFunction = FunctionEncoder.encode(function);
        // 转出合约
        String contractAddress = contractProperty.getOUTOnBSC();
        return signAndBroadcast(CHAIN_ID, RSATool.getSK(), contractAddress, encodedFunction);
    }

    /**
     * 转出USDT
     */
    public String lock(String receiver, BigDecimal value, String privateKey) {
        BigInteger amount = value.multiply(new BigDecimal(BigInteger.TEN.pow(18))).toBigInteger();

        Function function = new org.web3j.abi.datatypes.Function(
                FUNC_LOCK,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(receiver),
                        new org.web3j.abi.datatypes.generated.Uint256(amount)),
                Collections.emptyList());

        String encodedFunction = FunctionEncoder.encode(function);
        // 转出合约
        String contractAddress = contractProperty.getBSCChannel();
        return signAndBroadcast(CHAIN_ID, privateKey, contractAddress, encodedFunction);
    }


    private String signAndBroadcast(long chainId, String privateKey, String contractAddress, String encodedFunction) {
        if (web3j == null) {
            init();
        }
        BigInteger gasPrice;
        BigInteger nonce;
        String from_1 = BSCTool.getAddressByPrivateKey(privateKey);
        try {
            nonce = getNonce(from_1);
        } catch (Exception e) {
            return null;
        }
        try {
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            gasPrice = ethGasPrice.getGasPrice();
        } catch (Exception e) {
            gasPrice = GAS_PRICE;
        }
        BigInteger gasLimit = GAS_LIMIT;

        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, encodedFunction);
        Credentials credentials = Credentials.create(privateKey);
        //签名
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        try {
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            if (null == ethSendTransaction.getError()) {
                return ethSendTransaction.getTransactionHash();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取nonce
     */
    private BigInteger getNonce(String from) throws ExecutionException, InterruptedException {
        EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = transactionCount.getTransactionCount();
        return nonce;
    }

    /**
     * 哈希状态
     */
    public int hashStatus(String hash) throws ExecutionException, InterruptedException {
        if (web3j == null) {
            init();
        }
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(hash).sendAsync().get();
        if (transactionReceipt.getError() != null) {
            return Status.PENDING.ordinal();
        }
        if (transactionReceipt.getResult() == null) {
            return Status.PENDING.ordinal();
        }
        if (transactionReceipt.getResult().getStatus().equals("0x1")) {
            return Status.SUCCESS.ordinal();
        } else if (transactionReceipt.getResult().getStatus().equals("")) {
            return Status.PENDING.ordinal();
        } else {
            return Status.FAIL.ordinal();
        }
    }

    /**
     * 哈希状态
     */
    public String hashStatus2(String hash) throws ExecutionException, InterruptedException {
        if (web3j == null) {
            init();
        }
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(hash).sendAsync().get();
        return new Gson().toJson(transactionReceipt);
    }


    public BigDecimal balance() {
        if (web3j == null) {
            init();
        }
        Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000001000", contractProperty.getOUTOnBSC(), data);
        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BigInteger value = new BigInteger(ethCall.getValue().substring(2), 16);
        return new BigDecimal(value).divide(new BigDecimal(BigInteger.TEN.pow(18)), 6, RoundingMode.HALF_DOWN);
    }

    /**
     * 通道合约流量
     */
    public BigDecimal channelBalance() {
        if (web3j == null) {
            init();
        }
        Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000001000", contractProperty.getBSCChannel(), data);
        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BigInteger value = new BigInteger(ethCall.getValue().substring(2), 16);
        return new BigDecimal(value).divide(new BigDecimal(BigInteger.TEN.pow(18)), 6, RoundingMode.HALF_DOWN);
    }

    /**
     * 绑定的地址
     */
    public String outAccount() {
        if (web3j == null) {
            init();
        }
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC__OUTACCOUNT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000001000", contractProperty.getOUTOnBSC(), data);
        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "0x"+ethCall.getValue().substring(26);
    }

}
