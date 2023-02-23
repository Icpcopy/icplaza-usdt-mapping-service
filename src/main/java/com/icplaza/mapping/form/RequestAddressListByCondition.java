package com.icplaza.mapping.form;

import lombok.Data;

@Data
public class RequestAddressListByCondition {
    Integer startPage = 1;
    Integer pageSize = 50;
    Double usdt;
}
