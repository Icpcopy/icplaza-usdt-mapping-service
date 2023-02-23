package com.icplaza.mapping.bo;

import lombok.Data;

import java.util.List;

@Data
public class EVMBlockTransaction {
    private String blockHash;
    private String blockNumber;
    private String from;
    private String gas;
    private String gasPrice;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String hash;
    private String input;
    private String nonce;
    private String to;
    private String transactionIndex;
    private String value;
    private String type;
    private List<String> accessList;
    private String chainId;
    private String v;
    private String r;
    private String s;
}
