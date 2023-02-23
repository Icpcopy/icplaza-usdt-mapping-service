package com.icplaza.mapping.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
public class Condition {
    /**
     * 通道类型
     */
    private Integer channelType;
    /**
     * 绑定的icplaza地址
     */
    private String icplaza;
    /**
     * 转出地址
     */
    private String from_0;
    /**
     * 转入地址
     */
    private String to_0;
    /**
     * 哈希
     */
    private String hash_0;
    /**
     * 哈希状态
     */
    private Integer hashStatus_0;
    /**
     * 资产数量
     */
    private BigDecimal value_0;
    /**
     * 转出地址
     */
    private String from_1;
    /**
     * 转入地址
     */
    private String to_1;
    /**
     * 资产数量
     */
    private BigDecimal value_1;
    /**
     * 手续费
     */
    private BigDecimal fee_1;
    /**
     * 哈希
     */
    private String hash_1;
    /**
     * 哈希状态
     */
    private Integer hashStatus_1;
    private Integer page;
    private Integer pageSize;
}
