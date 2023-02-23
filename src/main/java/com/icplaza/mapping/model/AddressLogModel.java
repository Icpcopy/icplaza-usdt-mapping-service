package com.icplaza.mapping.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
public class AddressLogModel {
    /**
     * ID
     */
    private Integer id;
    /**
     * 绑定的icplaza地址
     */
    private String icplaza;
    /**
     * 地址
     */
    private String address;
    /**
     * 地址类型
     */
    private Integer addressType;
    /**
     * 加密后的私钥
     */
    private String privateKey;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * usdt余额
     */
    private Double usdt;
    /**
     * 最后扫描时间
     */
    private Integer lastScan;
}
