package com.icplaza.mapping.bo;

import lombok.Data;

import java.util.List;

@Data
public class EVMReceiptResult {
    private String blockHash;
    private String blockNumber;
    private String contractAddress;
    private String cumulativeGasUsed;
    private String effectiveGasPrice;
    private String from;
    private String gasUsed;
    private List<EVMReceiptLog> logs;
    private String logsBloom;
    private String status;
    private String to;
    private String transactionHash;
    private String transactionIndex;
    private String type;
}
