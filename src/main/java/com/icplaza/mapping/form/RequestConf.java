package com.icplaza.mapping.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestConf {
    @ApiModelProperty(value = "通道类型：0-TRON;1-BSC")
    private Integer channelType;
    @ApiModelProperty(value = "通道手续费")
    private BigDecimal fee;
    @ApiModelProperty(value = "审核限额")
    private BigDecimal maxLimit;
}
