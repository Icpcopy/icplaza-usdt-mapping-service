package com.icplaza.mapping.controller;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.icplaza.mapping.bo.Condition;
import com.icplaza.mapping.bo.EVMReceiptJSON;
import com.icplaza.mapping.bo.EVMReceiptLog;
import com.icplaza.mapping.bo.EVMReceiptResult;
import com.icplaza.mapping.common.*;
import com.icplaza.mapping.dao.AddressLogDAO;
import com.icplaza.mapping.dao.OrderInLogDAO;
import com.icplaza.mapping.dao.OrderOutLogDAO;
import com.icplaza.mapping.form.RequestOrders;
import com.icplaza.mapping.model.AddressLogModel;
import com.icplaza.mapping.model.OrderInLogModel;
import com.icplaza.mapping.model.OrderOutLogModel;
import com.icplaza.mapping.properties.ContractProperty;
import com.icplaza.mapping.service.Web3BSCService;
import com.icplaza.mapping.service.Web3ICPlazaService;
import com.icplaza.mapping.service.Web3TRONService;
import com.icplaza.mapping.utils.BSCTool;
import com.icplaza.mapping.utils.OrderInLogConverter;
import com.icplaza.mapping.utils.OrderOutLogConverter;
import com.icplaza.mapping.utils.OrderUtils;
import com.icplaza.mapping.vo.OrderInLogVO;
import com.icplaza.mapping.vo.OrderOutLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.icplaza.mapping.common.BSC.startBlockNumber;

@Api(tags = "订单")
@Slf4j
@RestController
public class OrderController {
    private final Integer PAGE = 1;
    private final Integer PAGE_SIZE = 20;
    @Autowired
    OrderInLogDAO orderInLogDao;
    @Autowired
    OrderOutLogDAO orderOutLogDao;
    @Autowired
    Web3TRONService web3TRONService;
    @Autowired
    Web3BSCService web3BSCService;
    @Autowired
    Web3ICPlazaService web3ICPlazaService;
    @Autowired
    ContractProperty contractProperty;
    @Autowired
    AddressLogDAO addressLogDAO;

    @ApiOperation("映射入订单")
    @PostMapping("/getOrdersIn")
    public ResponseCommon getOrdersIn(@RequestBody RequestOrders req) {
        if (req.getPage() == null || req.getPage() <= 0) {
            req.setPage(PAGE);
        }
        if (req.getPageSize() == null || req.getPageSize() <= 0) {
            req.setPageSize(PAGE_SIZE);
        }
        Integer start = (req.getPage() - 1) * req.getPageSize();
        Integer pageSize = req.getPageSize();

        log.info("{}", new Gson().toJson(req));

        Condition condition = calCondition(req);

        log.info("{}", new Gson().toJson(condition));

        List<OrderInLogModel> list = orderInLogDao.selectWithCondition(condition.getIcplaza(), condition.getChannelType(), condition.getHashStatus_1(), start, pageSize);

        BigInteger count = orderInLogDao.selectCountWithCondition(condition.getIcplaza(), condition.getChannelType(), condition.getHashStatus_1(), start, pageSize);

        OrderInLogConverter converter = new OrderInLogConverter();


        if (list.size() == 0) {
            return ResponseCommon.success(new PageResult<>(count.intValue(), list));
        }

        return ResponseCommon.success(new PageResult<>(count.intValue(), converter.convert(list, OrderInLogVO.class)));
    }

    private Condition calCondition(RequestOrders req) {
        Condition condition = new Condition();
        if (StrUtil.isNotBlank(req.getIcplaza())) {
            condition.setIcplaza(req.getIcplaza());
        }

        if (req.getChannelType() != null && !req.getChannelType().equals(Integer.valueOf("999"))) {
            condition.setChannelType(req.getChannelType());
        }
        if (req.getStatus() != null && !req.getStatus().equals(Integer.valueOf("999"))) {
            condition.setHashStatus_1(req.getStatus());
        }
        return condition;
    }

    @ApiOperation("映射出订单")
    @PostMapping("/getOrdersOut")
    public ResponseCommon getOrdersOut(@RequestBody RequestOrders req) {
        if (req.getPage() == null || req.getPage() <= 0) {
            req.setPage(PAGE);
        }
        if (req.getPageSize() == null || req.getPageSize() <= 0) {
            req.setPageSize(PAGE_SIZE);
        }
        Integer start = (req.getPage() - 1) * req.getPageSize();
        Integer pageSize = req.getPageSize();

        log.info("{}", new Gson().toJson(req));

        Condition condition = calCondition(req);

        log.info("{}", new Gson().toJson(condition));

        List<OrderOutLogModel> list = orderOutLogDao.selectWithCondition(condition.getIcplaza(), condition.getChannelType(), condition.getHashStatus_1(), start, pageSize);

        BigInteger count = orderOutLogDao.selectCountWithCondition(condition.getIcplaza(), condition.getChannelType(), condition.getHashStatus_1(), start, pageSize);

        OrderOutLogConverter converter = new OrderOutLogConverter();


        if (list.size() == 0) {
            return ResponseCommon.success(new PageResult<>(count.intValue(), list));
        }

        return ResponseCommon.success(new PageResult<>(count.intValue(), converter.convert(list, OrderOutLogVO.class)));
    }

    @ApiOperation("转账")
    @PostMapping("/out/{id}")
    public ResponseCommon out(@PathVariable BigInteger id) {
        // 审核中的设置为成功之后，会自动处理
        orderOutLogDao.setSuccess(id, Status.WAITING.ordinal());
        return ResponseCommon.success("ok");
    }

    @ApiOperation("资产监控")
    @PostMapping("/monitor")
    public ResponseCommon balance() {
        ArrayList list = new ArrayList();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            BigDecimal tronBalance = web3TRONService.balance();
            String account = web3TRONService.outAccount();
            // 填写绑定的账号地址，防止往合约转账
            map.put("contract", account);
            map.put("balance", tronBalance);
            map.put("channelType", ChannelType.TRON.ordinal());
            list.add(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BigDecimal bscBalance = web3BSCService.balance();
            String account = web3BSCService.outAccount();
            Map<String, Object> map2 = new HashMap<String, Object>();
            // 填写绑定的账号地址，防止往合约转账
            map2.put("contract", account);
            map2.put("balance", bscBalance);
            map2.put("channelType", ChannelType.BSC.ordinal());
            list.add(map2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseCommon.success(list);
    }

    @ApiOperation("通道剩余流量")
    @PostMapping("/channel")
    public ResponseCommon channel() {
        ArrayList list = new ArrayList();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            BigDecimal tronBalance = web3ICPlazaService.channelBalance(contractProperty.getTRONChannel());
            map.put("contract", contractProperty.getTRONChannel());
            map.put("balance", tronBalance);
            map.put("channelType", ChannelType.TRON.ordinal());
            list.add(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BigDecimal bscBalance = web3ICPlazaService.channelBalance(contractProperty.getBSCChannel());
            Map<String, Object> map2 = new HashMap<String, Object>();
            map2.put("contract", contractProperty.getBSCChannel());
            map2.put("balance", bscBalance);
            map2.put("channelType", ChannelType.BSC.ordinal());
            list.add(map2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseCommon.success(list);
    }

    /**
     * 映射入总量
     * 映射出总量
     * 映射出手续费
     * 销毁总量
     * 合约发行量
     */
    @ApiOperation("映射统计")
    @GetMapping("/statistics")
    public ResponseCommon statistics() {
        Map map = new HashMap();
        //映射入总量
        map.put("in", orderInLogDao.sum() == null ? 0 : orderInLogDao.sum());
        //映射出总量
        map.put("out", orderOutLogDao.sum() == null ? 0 : orderOutLogDao.sum());
        //映射出手续费
        map.put("fee", orderOutLogDao.fee() == null ? 0 : orderOutLogDao.fee());
        //销毁总量
        map.put("burn", orderOutLogDao.burn() == null ? 0 : orderOutLogDao.burn());
        //合约发行量
        map.put("totalSupply", web3ICPlazaService.totalSupply());

        return ResponseCommon.success(map);
    }

    /**
     * 通过哈希导入订单
     * */
    @GetMapping("/fromHash/{hash}")
    public ResponseCommon fromHash(@PathVariable String hash)  {
        try {
            String rs = web3BSCService.hashStatus2(hash);
            if (rs != null) {
                EVMReceiptJSON receipt = new Gson().fromJson(rs, EVMReceiptJSON.class);
                EVMReceiptResult result = receipt.getResult();
                if (result.getStatus().equals("0x1") && result.getTo().equalsIgnoreCase("0x55d398326f99059ff775485246999027b3197955")) {
                    List<EVMReceiptLog> logs = result.getLogs();
                    for (EVMReceiptLog logx : logs) {
                        List<String> topics = logx.getTopics();
                        if (topics.size() == 3 && topics.get(0).equalsIgnoreCase("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")) {
                            String toAddress = "0x"+topics.get(2).substring(26);
                            if (!MyBloomFilter.mightContain(toAddress)) {
                                log.info("to地址（{}）不在过滤器",toAddress);
                                continue;
                            }
                            String fromAddress = "0x"+topics.get(1).substring(26);
                            BigDecimal valueH = new BigDecimal(new BigInteger(logx.getData().substring(2), 16)).divide(new BigDecimal(BigInteger.TEN.pow(18)));
                            if (valueH.compareTo(BigDecimal.ZERO) == 1) {
                                OrderInLogModel model = new OrderInLogModel();
                                model.setFrom_0(fromAddress);
                                model.setTo_0(toAddress);
                                model.setHash_0(hash);
                                model.setValue_0(valueH);
                                model.setHashStatus_0(Status.SUCCESS.ordinal());
                                AddressLogModel address = addressLogDAO.selectModelByAddress(model.getTo_0());
                                if (address == null) {
                                    log.info("to地址不存在于数据库无法录入 {}", new Gson().toJson(model));
                                    continue;
                                }
                                model.setOrderNo(new BigInteger(OrderUtils.getOrderNo()));
                                model.setIcplaza(address.getIcplaza());
                                model.setCreateTime(new Date());
                                model.setChannelType(ChannelType.BSC.ordinal());
                                orderInLogDao.addStep1(model);
                            }
                        }
                    }
                } else {
                    return ResponseCommon.fail("failed tx"+result.getStatus());
                }
            } else {
                return ResponseCommon.fail("response null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseCommon.fail(e.getMessage());
        }
        return ResponseCommon.success("");
    }
}
