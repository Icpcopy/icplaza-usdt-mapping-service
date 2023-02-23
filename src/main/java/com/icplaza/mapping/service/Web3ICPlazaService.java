package com.icplaza.mapping.service;

import com.google.gson.Gson;
import com.icplaza.mapping.common.ChainType;
import com.icplaza.mapping.common.Status;
import com.icplaza.mapping.model.OrderInLogModel;
import com.icplaza.mapping.model.OrderOutLogModel;
import com.icplaza.mapping.properties.ContractProperty;
import com.icplaza.mapping.properties.NodeProperty;
import com.icplaza.mapping.utils.BSCTool;
import com.icplaza.mapping.utils.RSATool;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
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

@Service
public class Web3ICPlazaService {
    private final String FUNC_MINT = "mint";
    private final String FUNC_BURNLOCK = "burnLock";
    private final String FUNC_FREELOCK = "freeLock";
    private final String FUNC_TOTALSUPPLY = "totalSupply";
    private final String FUNC_BALANCE = "balance";

    private final long CHAIN_ID = 9000;
    private Web3j web3j = null;
    @Autowired
    NodeProperty nodeProperty;
    @Autowired
    ContractProperty contractProperty;

    @PostConstruct
    public void init() {
        web3j = Web3j.build(new HttpService(nodeProperty.getIcplaza()));
    }

    /**
     * 铸造USDT
     */
    public synchronized String mint(OrderInLogModel model) {
        if (web3j == null) {
            init();
        }
        String to_1 = model.getTo_1();
        BigInteger amount = model.getValue_1().multiply(new BigDecimal(BigInteger.TEN.pow(18))).toBigInteger();
        String hash_0 = model.getHash_0();
        if (hash_0.startsWith("0x")) {
            hash_0 = hash_0.substring(2);
        }
        byte[] hash = Hex.decode(hash_0);

        Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MINT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(to_1),
                        new org.web3j.abi.datatypes.generated.Uint256(amount),
                        new org.web3j.abi.datatypes.generated.Bytes32(hash)),
                Collections.<TypeReference<?>>emptyList());
        String encodedFunction = FunctionEncoder.encode(function);
        // 确定通道合约
        String contractAddress = contractProperty.getBSCChannel();
        if (model.getChannelType().equals(ChainType.TRON.ordinal())) {
            contractAddress = contractProperty.getTRONChannel();
        }

        return signAndBroadcast(CHAIN_ID, RSATool.getSK(), contractAddress, encodedFunction);
    }

    /**
     * 销毁锁定中的USDT
     */
    public synchronized String burnLock(OrderOutLogModel model) {
        if (web3j == null) {
            init();
        }
        String account = model.getFrom_2();
        BigInteger amount = model.getValue_0().multiply(new BigDecimal(BigInteger.TEN.pow(18))).toBigInteger();
        String hash_0 = model.getHash_1();
        if (hash_0.startsWith("0x")) {
            hash_0 = hash_0.substring(2);
        }
        byte[] hash = Hex.decode(hash_0);

        Function function = new org.web3j.abi.datatypes.Function(
                FUNC_BURNLOCK,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(account),
                        new org.web3j.abi.datatypes.generated.Uint256(amount),
                        new org.web3j.abi.datatypes.generated.Bytes32(hash)),
                Collections.<TypeReference<?>>emptyList());

        String encodedFunction = FunctionEncoder.encode(function);
        // 确定通道合约
        String contractAddress = contractProperty.getBSCChannel();
        if (model.getChannelType().equals(ChainType.TRON.ordinal())) {
            contractAddress = contractProperty.getTRONChannel();
        }
        return signAndBroadcast(CHAIN_ID, RSATool.getSK(), contractAddress, encodedFunction);
    }

    /**
     * 销毁锁定中的USDT
     */
    public synchronized String freeLock(OrderOutLogModel model) {
        if (web3j == null) {
            init();
        }
        String account = model.getFrom_0();
        BigInteger amount = model.getValue_0().multiply(new BigDecimal(BigInteger.TEN.pow(18))).toBigInteger();
        String hash_0 = model.getHash_0();
        if (hash_0.startsWith("0x")) {
            hash_0 = hash_0.substring(2);
        }
        byte[] hash = Hex.decode(hash_0);

        Function function = new org.web3j.abi.datatypes.Function(
                FUNC_FREELOCK,
                Arrays.asList(new org.web3j.abi.datatypes.Address(account),
                        new org.web3j.abi.datatypes.generated.Uint256(amount)),
                Collections.emptyList());

        String encodedFunction = FunctionEncoder.encode(function);
        // 确定通道合约
        String contractAddress = contractProperty.getBSCChannel();
        if (model.getChannelType().equals(ChainType.TRON.ordinal())) {
            contractAddress = contractProperty.getTRONChannel();
        }
        return signAndBroadcast(CHAIN_ID, RSATool.getSK(), contractAddress, encodedFunction);
    }


    public synchronized String lock(String receiver, BigInteger amount) {
        String encodedFunction = getFunction(receiver, amount);
        String contractAddress = contractProperty.getTRONChannel();
        return signAndBroadcast(CHAIN_ID, RSATool.getSK(), contractAddress, encodedFunction);
    }


    public String getFunction(String receiver, BigInteger amount) {
        Function function = new org.web3j.abi.datatypes.Function(
                "lock",
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(receiver),
                        new org.web3j.abi.datatypes.generated.Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());


        String encodedFunction = FunctionEncoder.encode(function);
        return encodedFunction;

    }

    public BigDecimal totalSupply() {
        if (web3j == null) {
            init();
        }
        Function function = new Function(FUNC_TOTALSUPPLY,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        String encodedFunction = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000001000", contractProperty.getUsdt(), encodedFunction);
        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BigInteger value = new BigInteger(ethCall.getValue().substring(2), 16);
        return new BigDecimal(value).divide(new BigDecimal(BigInteger.TEN.pow(18)), 6, RoundingMode.HALF_DOWN);
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
            e.printStackTrace();
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
            System.out.println(ethSendTransaction.getError().getMessage());
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

    /**
     * channel balance
     */
    public BigDecimal channelBalance(String channelContract) {
        if (web3j == null) {
            init();
        }
        Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000001000", channelContract, data);
        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BigInteger value = new BigInteger(ethCall.getValue().substring(2), 16);
        return new BigDecimal(value).divide(new BigDecimal(BigInteger.TEN.pow(18)), 6, RoundingMode.HALF_DOWN);
    }

}
