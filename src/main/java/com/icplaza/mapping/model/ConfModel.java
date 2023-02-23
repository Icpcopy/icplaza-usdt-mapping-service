package com.icplaza.mapping.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConfModel {
    private Integer channelType;
    private BigDecimal fee;
    private BigDecimal maxLimit;
}
