package com.icplaza.mapping.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.icplaza.mapping.common.LogSwitch;
import com.icplaza.mapping.model.OrderInLogModel;
import com.icplaza.mapping.properties.PushProperty;
import com.icplaza.mapping.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushServiceImpl implements PushService {
    @Autowired
    PushProperty pushProperty;

    @Override
    public boolean push(OrderInLogModel orderLogModel) {
        String url = pushProperty.getUrl();
        if (LogSwitch.pushLogOpen) {
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++");
            log.info("postTO=>" + url);
            log.info("postData=>" + JSON.toJSONString(orderLogModel));
        }
        String result = HttpRequest.post(url).body(JSON.toJSONString(orderLogModel)).timeout(20000).execute().body();
        if (LogSwitch.pushLogOpen) {
            log.info("result=>"+result);
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        // php版本使用 jsonObject.getString("result").equals("success") 判断
        // java版本使用 jsonObject.getInteger("code").equals(200) 判断
//        if (jsonObject != null && jsonObject.getString("result").equals("success")) {
        if (jsonObject != null && jsonObject.getInteger("code").equals(200)) {
            return true;
        }
        return false;
    }
}
