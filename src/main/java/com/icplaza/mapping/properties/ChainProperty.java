package com.icplaza.mapping.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chain")
public class ChainProperty {
    String baseUrl;
    String headerName;
    String headerValue;
    String blockNumber;
    String blockDataByBlockNumber;
    String transactionByHash;
    String transactionContractByHash;
    String contract;
    String balanceAll;
    String balance;
}
