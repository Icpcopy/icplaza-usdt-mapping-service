package com.icplaza.mapping.controller;

import com.icplaza.mapping.common.AddressType;
import com.icplaza.mapping.common.MyBloomFilter;
import com.icplaza.mapping.common.ResponseCommon;
import com.icplaza.mapping.dao.AddressLogDAO;
import com.icplaza.mapping.form.RequestCreateAddress;
import com.icplaza.mapping.form.ResponseCreateAddress;
import com.icplaza.mapping.model.AddressLogModel;
import com.icplaza.mapping.utils.BSCTool;
import com.icplaza.mapping.utils.Bech32;
import com.icplaza.mapping.utils.RSATool;
import com.icplaza.mapping.utils.TronTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@Api(tags = "地址")
@Slf4j
@RestController
public class AddressController {
    @Autowired
    AddressLogDAO addressLogDAO;

    @ApiOperation("获取地址")
    @PostMapping("/getAddress")
    public ResponseCommon getAddress(@RequestBody RequestCreateAddress req) {
        // 检查地址类型
        try {
            Bech32.decode(req.getIcplaza());
        } catch (Exception e) {
            return ResponseCommon.fail(String.format("invalid address %s", req.getIcplaza()));
        }
        AddressLogModel model = addressLogDAO.selectModelByICPlazaAndType(req.getIcplaza(), req.getAddressType());
        if (model != null) {
            ResponseCreateAddress responseCreateAddress = new ResponseCreateAddress();
            responseCreateAddress.setIcplaza(req.getIcplaza());
            responseCreateAddress.setAddress(model.getAddress());
            responseCreateAddress.setAddressType(model.getAddressType());
            return ResponseCommon.success(responseCreateAddress);
        } else {
            try {
                Map<String, String> addressInfo = createAddressByType(req.getAddressType());
                // db
                AddressLogModel addressLogModel = new AddressLogModel();
                addressLogModel.setIcplaza(req.getIcplaza());
                addressLogModel.setAddressType(req.getAddressType());
                addressLogModel.setAddress(addressInfo.get("address"));
                // RSA加密存储
                String encryptPrivateKey = RSATool.encryptStr(addressInfo.get("privateKey"));
                if (encryptPrivateKey == null) {
                    return ResponseCommon.fail("创建地址失败,公钥加密失败！！！");
                }
                addressLogModel.setPrivateKey(encryptPrivateKey);
                addressLogModel.setCreateTime(new Date());
                // 保存地址
                addressLogDAO.insert(addressLogModel);
                // response
                ResponseCreateAddress res = new ResponseCreateAddress();
                res.setIcplaza(req.getIcplaza());
                res.setAddress(addressInfo.get("address"));
                res.setAddressType(req.getAddressType());
                // 加入布隆过滤器
                try {
                    MyBloomFilter.put(addressInfo.get("address"));
                } catch (Exception e) {
                    log.error("加入布隆过滤器,失败了：" + addressInfo.get("address"));
                }
                return ResponseCommon.success(res);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseCommon.fail("创建地址失败");
            }
        }
    }

    /**
     * 根据类型创建地址
     */
    private Map<String, String> createAddressByType(Integer addressType) throws Exception {
        if (addressType == AddressType.TRON.ordinal()) {
            // 波场
            return TronTool.createAddress();
        } else if (addressType == AddressType.BSC.ordinal() || addressType == AddressType.ETH.ordinal()) {
            // bsc或者以太坊
            return BSCTool.createAddress();
        }
        return null;
    }
}
