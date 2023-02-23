package com.icplaza.mapping.bo;

import lombok.Data;

@Data
public class EVMReceiptJSON {
    private String jsonrpc;
    private int id;
    private EVMReceiptResult result;
}
