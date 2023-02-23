package com.icplaza.mapping.bo;

import lombok.Data;

import java.util.List;

@Data
public class EVMReceiptLog {
    private String address;
    private List<String> topics;
    private String data;
    private String blockNumber;
    private String transactionHash;
    private String transactionIndex;
    private String blockHash;
    private String logIndex;
    private boolean removed;
}
