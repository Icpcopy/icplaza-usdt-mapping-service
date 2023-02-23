package com.icplaza.mapping.vo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@Component
public class OrderOutLogVO {
    /**
     * ID
     */
    private BigInteger id;
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 绑定的icplaza地址
     */
    private String icplaza;
    /**
     * 通道类型
     */
    private Integer channelType;
    /**
     * 转出地址
     */
    private String from_0;
    /**
     * 转入地址0x0
     */
    private String to_0;
    /**
     * 资产数量
     */
    private BigDecimal value_0;
    /**
     * 手续费
     */
    private BigDecimal fee_1;
    /**
     * 哈希
     */
    private String hash_0;
    /**
     * 哈希状态
     */
    private Integer hashStatus_0;
    /**
     * 接收地址
     */
    private String receiver;
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
    /**
     * 销毁操作-地址
     */
    private String from_2;
    /**
     * 销毁操作-0x0
     */
    private String to_2;
    /**
     * 销毁操作-资产数量
     */
    private BigDecimal value_2;
    /**
     * 销毁操作-销毁哈希
     */
    private String hash_2;
    /**
     * 销毁操作-哈希状态
     */
    private Integer hashStatus_2;
    /**
     * 销毁时间
     */
    private Date burnTime;
    /**
     * 0-默认；1-回退；
     */
    private Integer refund;
}
