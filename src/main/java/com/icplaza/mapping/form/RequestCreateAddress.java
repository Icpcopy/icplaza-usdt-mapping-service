package com.icplaza.mapping.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

@Data
@ApiModel(value = "创建地址", description = "创建地址")
public class RequestCreateAddress {
    @ApiModelProperty(value = "绑定的icplaza地址")
    String icplaza;
    @ApiModelProperty(value = "创建的地址类型：0-波场；1-BSC;2-以太坊")
    Integer addressType;
}
