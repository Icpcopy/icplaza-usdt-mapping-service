package com.icplaza.mapping.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "node")
public class NodeProperty {
    private String icplaza;
    private String tron;
    private String bsc;
    private Long chainId;
}
