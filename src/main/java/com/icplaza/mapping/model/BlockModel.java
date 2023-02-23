package com.icplaza.mapping.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class BlockModel {
    Long height;
    Integer code;
}
