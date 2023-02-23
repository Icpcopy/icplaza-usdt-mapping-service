package com.icplaza.mapping.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RequestOrders {
    @ApiModelProperty(value = "绑定的icplaza地址")
    String icplaza;
    @ApiModelProperty(value = "通道类型：0-TRON;1-BSC;2-ETH;999-不限制；")
    Integer channelType;
    @ApiModelProperty(value = "当前页码，默认1")
    Integer page;
    @ApiModelProperty(value = "每页展示数量，默认20")
    Integer pageSize;
    @ApiModelProperty(value = "处理状态：0-处理中；1-成功；2-失败；3-待审核;999-不限制；")
    Integer status;
}
