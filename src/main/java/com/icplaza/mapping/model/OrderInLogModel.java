package com.icplaza.mapping.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@Component
public class OrderInLogModel {
    /**
     * 通道类型
     */
    private Integer channelType;
    /**
     * ID
     */
    private BigInteger id;
    /**
     * 订单号
     */
    private BigInteger orderNo;
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
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
