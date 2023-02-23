package com.icplaza.mapping.controller;

import com.google.gson.Gson;
import com.icplaza.mapping.common.ChannelType;
import com.icplaza.mapping.common.FeeAndLimit;
import com.icplaza.mapping.common.ResponseCommon;
import com.icplaza.mapping.dao.ConfDAO;
import com.icplaza.mapping.form.RequestConf;
import com.icplaza.mapping.model.ConfModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Api(tags = "配置")
@Slf4j
@RestController
@RequestMapping("/config")
public class ConfController {
    @Autowired
    ConfDAO confDAO;

    @ApiOperation("设置转出手续费-转出限额")
    @PostMapping("/set")
    public ResponseCommon set(@RequestBody RequestConf req) {
        if (req.getChannelType() > 2) {
            return ResponseCommon.fail("通道类型错误");
        }

        if (req.getFee().compareTo(BigDecimal.ZERO) < 0 || req.getMaxLimit().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseCommon.fail("参数错误,不能小于0");
        }

        ConfModel model = confDAO.selectModel(req.getChannelType());
        ConfModel modelOld = model;
        if (model == null) {
            model = new ConfModel();
            model.setChannelType(req.getChannelType());
            model.setFee(req.getFee());
            model.setMaxLimit(req.getMaxLimit());
            confDAO.add(model);
        } else {
            model.setFee(req.getFee());
            model.setMaxLimit(req.getMaxLimit());
            confDAO.update(model);
        }
        log.info("更新配置：from {} to {}", new Gson().toJson(modelOld), new Gson().toJson(model));
        if (model.getChannelType().equals(ChannelType.TRON.ordinal())) {
            FeeAndLimit.TRON_MAX_LIMIT = req.getMaxLimit();
            FeeAndLimit.TRON_FEE = req.getFee();
            log.info("FeeAndLimit.TRON_MAX_LIMIT {}", FeeAndLimit.TRON_MAX_LIMIT);
            log.info("FeeAndLimit.TRON_FEE {}", FeeAndLimit.TRON_FEE);
        } else {
            FeeAndLimit.BSC_MAX_LIMIT = req.getMaxLimit();
            FeeAndLimit.BSC_FEE = req.getFee();
            log.info("FeeAndLimit.BSC_MAX_LIMIT {}", FeeAndLimit.BSC_MAX_LIMIT);
            log.info("FeeAndLimit.BSC_FEE {}", FeeAndLimit.BSC_FEE);
        }
        return ResponseCommon.success("success");
    }

    @ApiOperation("已有配置")
    @GetMapping("/list")
    public ResponseCommon list() {
        return ResponseCommon.success(confDAO.selectModels());
    }

}
