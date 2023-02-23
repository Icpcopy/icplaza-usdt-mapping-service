package com.icplaza.mapping.utils;

import com.icplaza.mapping.model.OrderOutLogModel;
import com.icplaza.mapping.vo.OrderOutLogVO;

public class OrderOutLogConverter extends BaseConverter<OrderOutLogModel, OrderOutLogVO> {
    @Override
    protected void convert(OrderOutLogModel from, OrderOutLogVO to) {
        to.setOrderNo(from.getOrderNo().toString());
        super.convert(from, to);
    }
}
