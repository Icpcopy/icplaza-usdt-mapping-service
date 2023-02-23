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
public class Result {

    private String address;
    private List<String> topics;
    private String data;
    private String blockNumber;
    private String timeStamp;
    private String gasPrice;
    private String gasUsed;
    private String logIndex;
    private String transactionHash;
    private String transactionIndex;
    public void setAddress(String address) {
         this.address = address;
     }
     public String getAddress() {
         return address;
     }

    public void setTopics(List<String> topics) {
         this.topics = topics;
     }
     public List<String> getTopics() {
         return topics;
     }

    public void setData(String data) {
         this.data = data;
     }
     public String getData() {
         return data;
     }

    public void setBlockNumber(String blockNumber) {
         this.blockNumber = blockNumber;
     }
     public String getBlockNumber() {
         return blockNumber;
     }

    public void setTimeStamp(String timeStamp) {
         this.timeStamp = timeStamp;
     }
     public String getTimeStamp() {
         return timeStamp;
     }

    public void setGasPrice(String gasPrice) {
         this.gasPrice = gasPrice;
     }
     public String getGasPrice() {
         return gasPrice;
     }

    public void setGasUsed(String gasUsed) {
         this.gasUsed = gasUsed;
     }
     public String getGasUsed() {
         return gasUsed;
     }

    public void setLogIndex(String logIndex) {
         this.logIndex = logIndex;
     }
     public String getLogIndex() {
         return logIndex;
     }

    public void setTransactionHash(String transactionHash) {
         this.transactionHash = transactionHash;
     }
     public String getTransactionHash() {
         return transactionHash;
     }

    public void setTransactionIndex(String transactionIndex) {
         this.transactionIndex = transactionIndex;
     }
     public String getTransactionIndex() {
         return transactionIndex;
     }

}