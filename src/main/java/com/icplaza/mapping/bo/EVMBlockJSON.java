package com.icplaza.mapping.bo;

import lombok.Data;

@Data
public class EVMBlockJSON {
    private String jsonrpc;
    private int id;
    private EVMBlockResult result;
}
