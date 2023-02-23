package com.icplaza.mapping.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;

@ApiModel("分页模型")
public class PageResult<T> implements Serializable {
    @ApiModelProperty("总条数")
    private int total;
    @ApiModelProperty("当前页数据")
    private T items;

    public PageResult() {
    }

    public PageResult(int total, T items) {
        this.total = total;
        this.items = items;
    }

    public static PageResult empty() {
        return new PageResult(0, new ArrayList());
    }

    public static int getTotalPage(int count, int rows) {
        int totalPage = 0;
        if (count <= 0) {
            return totalPage;
        } else {
            if (count <= rows) {
                totalPage = 1;
            } else if (count % rows == 0) {
                totalPage = count / rows;
            } else {
                totalPage = count / rows + 1;
            }

            return totalPage;
        }
    }

    public int getTotal() {
        return this.total;
    }



    public T getItems() {
        return this.items;
    }

    public void setTotal(final int total) {
        this.total = total;
    }



    public void setItems(final T items) {
        this.items = items;
    }


}
