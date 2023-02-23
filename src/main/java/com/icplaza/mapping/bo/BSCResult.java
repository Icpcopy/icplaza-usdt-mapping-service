package com.icplaza.mapping.bo; /**
  * Copyright 2022 json.cn 
  */

import java.util.List;

/**
 * Auto-generated: 2022-07-24 20:23:10
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class BSCResult {

    private String status;
    private String message;
    private List<Result> result;
    public void setStatus(String status) {
         this.status = status;
     }
     public String getStatus() {
         return status;
     }

    public void setMessage(String message) {
         this.message = message;
     }
     public String getMessage() {
         return message;
     }

    public void setResult(List<Result> result) {
         this.result = result;
     }
     public List<Result> getResult() {
         return result;
     }

}