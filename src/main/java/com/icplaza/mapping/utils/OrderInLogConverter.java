package com.icplaza.mapping.utils;

import com.icplaza.mapping.model.OrderInLogModel;
import com.icplaza.mapping.vo.OrderInLogVO;

public class OrderInLogConverter extends BaseConverter<OrderInLogModel, OrderInLogVO> {
    @Override
    protected void convert(OrderInLogModel from, OrderInLogVO to) {
        to.setOrderNo(from.getOrderNo().toString());
        super.convert(from, to);
    }
}
