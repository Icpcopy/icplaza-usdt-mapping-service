package com.icplaza.mapping.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "contract")
public class ContractProperty {
    /**
     * 波场上的USDT合约
     * */
    private String USDTOnTron;
    /**
     * BSC上的USDT合约
     * */
    private String USDTOnBSC;
    /**
     * tron上的转出合约
     * */
    private String OUTOnTRON;
    /**
     * bsc上的转出合约
     * */
    private String OUTOnBSC;
    /**
     * BSC通道合约
     * */
    private String BSCChannel;
    /**
     * 波场通道合约
     * */
    private String TRONChannel;
    /**
     * icplaza usdt contract address
     * */
    private String usdt;
}
