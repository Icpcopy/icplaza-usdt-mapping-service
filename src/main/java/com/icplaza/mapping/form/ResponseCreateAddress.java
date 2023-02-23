package com.icplaza.mapping.form;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class ResponseCreateAddress {
    String icplaza;
    String address;
    Integer addressType;
}
