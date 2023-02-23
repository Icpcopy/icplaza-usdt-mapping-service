package com.icplaza.mapping.controller;

import com.alibaba.fastjson.JSONObject;
import com.icplaza.mapping.common.*;
import com.icplaza.mapping.dao.AddressLogDAO;
import com.icplaza.mapping.dao.BlockDAO;
import com.icplaza.mapping.dao.OrderOutLogDAO;
import com.icplaza.mapping.form.RequestAddressListByCondition;
import com.icplaza.mapping.form.RequestPrivateKeyData;
import com.icplaza.mapping.model.AddressLogModel;
import com.icplaza.mapping.model.BlockModel;
import com.icplaza.mapping.properties.ChainProperty;
import com.icplaza.mapping.properties.ContractProperty;
import com.icplaza.mapping.properties.NodeProperty;
import com.icplaza.mapping.service.*;
import com.icplaza.mapping.utils.AddressUtil;
import com.icplaza.mapping.utils.BSCTool;
import com.icplaza.mapping.utils.RSATool;
import com.icplaza.mapping.utils.TronTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tron.tronj.crypto.SECP256K1;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Api(tags = "test")
@RestController
@RequestMapping("/debug/")
public class TestController {
    @Autowired
    ChainDataService chainDataService;
    @Autowired
    OrderOutLogDAO orderOutLogDAO;
    @Autowired
    BlockDAO blockDAO;
    @Autowired
    AddressLogDAO addressLogDAO;
    @Autowired
    Web3ICPlazaService web3ICPlazaService;
    @Autowired
    Web3TRONService web3TRONService;
    @Autowired
    EVMService evmService;
    @Autowired
    Web3BSCService web3BSCService;
    @Autowired
    ContractProperty contractProperty;
    @Autowired
    NodeProperty nodeProperty;
    @Autowired
    ChainProperty chainProperty;

    /**
     * 链数据高度
     */
    @GetMapping("/chain/height")
    public Long height() {
        return chainDataService.queryBlockHeight();
    }

    /**
     * 高度交易信息
     */
    @GetMapping("/chain/blockData/{height}")
    public JSONObject blockData(@PathVariable Long height) {
        return chainDataService.getBlockDataByBlockNumber(height);
    }

    /**
     * 合约交易
     */
    @GetMapping("/chain/hash/{hash}")
    public JSONObject hash(@PathVariable String hash) {
        return chainDataService.getTransactionContractByHash(hash);
    }

    /**
     * 私钥生成地址
     */
    @GetMapping("/address/getAddress/{key}")
    public String getAddressFromPrivateKey(@PathVariable String key) {
        return TronTool.getAddressByPrivateKey(key);
    }

    /**
     * tron地址数量
     */
    @GetMapping("/address/count")
    public Integer addressCount() {
        return addressLogDAO.rows();
    }

    /**
     * bsc地址数量
     */
    @GetMapping("/address2/count")
    public Integer addressCount2() {
        return addressLogDAO.rows2();
    }

    /**
     * 测试过滤器
     */
    @GetMapping("/bloom/{address}")
    public boolean bloom(@PathVariable String address) {
        return MyBloomFilter.mightContain(address);
    }

    /**
     * 加入过滤器
     */
    @GetMapping("/bloom/add/{address}")
    public boolean bloomAdd(@PathVariable String address) {
        MyBloomFilter.put(address);
        return MyBloomFilter.mightContain(address);
    }

    /**
     * 开关日志
     */
    @GetMapping("/log/open/{open}")
    public String open(@PathVariable boolean open) {
        LogSwitch.setOpen(open);
        return "ok";
    }

    /**
     * 开关日志
     */
    @GetMapping("/log/openPushLog/{open}")
    public String openPushLog(@PathVariable boolean open) {
        LogSwitch.setPushLogOpen(open);
        return "ok";
    }

    /**
     * 定时任务处理高度
     */
    @GetMapping("/job/height")
    public Map jobHeight() {
        BlockModel blockModel = blockDAO.getBlock(ChainType.TRON.ordinal());
        Map map = new HashMap();
        map.put("tron", blockModel.getHeight());
        blockModel = blockDAO.getBlock(ChainType.BSC.ordinal());
        map.put("bsc", blockModel.getHeight());
        blockModel = blockDAO.getBlock(ChainType.ETH.ordinal());
        map.put("evm", blockModel.getHeight());
        return map;
    }

    /**
     * 当前任务数量
     */
    @GetMapping("/job/size")
    public Integer jobSize() {
        return JobPool.jobSize();
    }

    /**
     * 当前任务数量
     */
    @GetMapping("/job/transactionSize")
    public Integer transactionSize() {
        return PendingTransactionPool.size();
    }

    /**
     * 当前任务数量
     */
    @GetMapping("/job/minSize")
    public Integer jobMinSize() {
        return JobPool.jobMinSize();
    }

    /**
     * 当前任务数量
     */
    @GetMapping("/job/minSize/{size}")
    public Integer setJobMinSize(@PathVariable Integer size) {
        JobPool.setJobMinSize(size);
        return JobPool.jobMinSize();
    }

    /**
     * 添加一个任务高度
     */
    @GetMapping("/job/add/{height}")
    public Boolean jobAdd(@PathVariable Long height) {
        if (height != null) {
            JobPool.add(height);
            return true;
        }
        return false;
    }

    /**
     * 开关余额扫描
     */
    @GetMapping("/balance/open/{open}")
    public Boolean balanceSwitch(@PathVariable boolean open) {
        BalanceSwitch.setOpen(open);
        return open;
    }

    /**
     * 开关余额扫描
     */
    @GetMapping("/balance2/open/{open}")
    public Boolean balanceSwitch2(@PathVariable boolean open) {
        BalanceSwitch.setOpen(open);
        return open;
    }

    /**
     * 查询所有TRON地址
     */
    @PostMapping("/address/list")
    public List<AddressLogModel> getAddressListWithCondition(@RequestBody RequestAddressListByCondition requestAddressListByCondition) {
        Integer startPage = requestAddressListByCondition.getStartPage() > 0 ? requestAddressListByCondition.getStartPage() : 1;
        Integer pageSize = requestAddressListByCondition.getPageSize() > 0 ? requestAddressListByCondition.getPageSize() : 10;
        Integer start = (startPage - 1) * pageSize;
        return addressLogDAO.selectModelsWithCondition(start, pageSize, requestAddressListByCondition.getUsdt(), AddressType.TRON.ordinal());
    }

    /**
     * 查询所有BSC地址
     */
    @PostMapping("/address2/list")
    public List<AddressLogModel> getAddressListWithCondition2(@RequestBody RequestAddressListByCondition requestAddressListByCondition) {
        Integer startPage = requestAddressListByCondition.getStartPage() > 0 ? requestAddressListByCondition.getStartPage() : 1;
        Integer pageSize = requestAddressListByCondition.getPageSize() > 0 ? requestAddressListByCondition.getPageSize() : 10;
        Integer start = (startPage - 1) * pageSize;
        return addressLogDAO.selectModelsWithCondition(start, pageSize, requestAddressListByCondition.getUsdt(), AddressType.BSC.ordinal());
    }

    /**
     * 查询所有TRON地址
     */
    @GetMapping("/address/list/{startPage}/{pageSize}")
    public List<AddressLogModel> getAddressList(@PathVariable Integer startPage, @PathVariable Integer pageSize) {
        startPage = startPage > 0 ? startPage : 1;
        pageSize = pageSize > 0 ? pageSize : 10;
        Integer start = (startPage) * pageSize;
        return addressLogDAO.selectModels(start, pageSize, AddressType.TRON.ordinal());
    }

    /**
     * 查询所有BSC地址
     */
    @GetMapping("/address2/list/{startPage}/{pageSize}")
    public List<AddressLogModel> getAddressList2(@PathVariable Integer startPage, @PathVariable Integer pageSize) {
        startPage = startPage > 0 ? startPage : 1;
        pageSize = pageSize > 0 ? pageSize : 10;
        Integer start = (startPage) * pageSize;
        return addressLogDAO.selectModels(start, pageSize, AddressType.BSC.ordinal());
    }

    @GetMapping("/getAddress/{address}")
    public AddressLogModel getAddress(@PathVariable String address) {
        return addressLogDAO.selectModelByAddress(address);
    }

    /**
     * 获取公钥
     */
    @GetMapping("/getPublicKey")
    public ResponseCommon getPublicKey() {
        return ResponseCommon.success(RSATool.getPublicKeyMap());
    }

    /**
     * 设置私钥
     */
    @PostMapping("/setPrivateKey")
    public ResponseCommon setPrivateKey(@RequestBody RequestPrivateKeyData r) {
        try {
            String data = r.getData();
            String pk = RSATool.decrypt(data);
            if (pk.length() != 64) {
                return ResponseCommon.fail("invalid data");
            }
            Hex.decode(pk);
            RSATool.setPK(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseCommon.fail("invalid data");
        }
        return ResponseCommon.success("success");
    }

    @GetMapping("/bsc/{hash}")
    public ResponseCommon hash2(@PathVariable String hash) {
        try {
            String r = web3BSCService.hashStatus2(hash);
            return ResponseCommon.success(r);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("/icplaza/{hash}")
    public ResponseCommon hash3(@PathVariable String hash) {
        try {
            String r = web3ICPlazaService.hashStatus2(hash);
            return ResponseCommon.success(r);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/tron/{hash}")
    public ResponseCommon hash4(@PathVariable String hash) {
        try {
            int r = web3TRONService.hashStatus(hash);
            return ResponseCommon.success(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/block/{height}")
    public ResponseCommon block(@PathVariable BigInteger height) {
        return ResponseCommon.success(evmService.eth_getBlockByNumber(height));
    }

    @ApiOperation("大账户列表-用于合约调用及部署")
    @GetMapping("/accounts")
    public Map account() throws Exception {
        String key = RSATool.getSK();
        if (key == null) {
            return null;
        }
        Map map = new HashMap();
        map.put("TRON", TronTool.getAddressByPrivateKey(key));
        map.put("BSC", BSCTool.getAddressByPrivateKey(key));
        map.put("ICPLAZA", AddressUtil.convertEthAddressToCosmos((String) map.get("BSC"), "icplaza"));
        return map;
    }


    @ApiOperation("测试私钥-生成多种地址")
    @GetMapping("/keys/{key}")
    public Map keys(@PathVariable String key) throws Exception {
        if (key == null) {
            return null;
        }
        Map map = new HashMap();
        map.put("TRON", TronTool.getAddressByPrivateKey(key));
        map.put("BSC", BSCTool.getAddressByPrivateKey(key));
        map.put("ICPLAZA", AddressUtil.convertEthAddressToCosmos((String) map.get("BSC"), "icplaza"));
        return map;
    }

    @ApiOperation("配置")
    @GetMapping("/configs")
    public Map configs() {
        Map map = new HashMap();

        map.put("contract.USDTOnTron", contractProperty.getUSDTOnTron());
        map.put("contract.USDTOnBSC", contractProperty.getUSDTOnBSC());
        map.put("contract.OUTOnBSC", contractProperty.getOUTOnBSC());
        map.put("contract.OUTOnTRON", contractProperty.getOUTOnTRON());
        map.put("contract.TRONChannel", contractProperty.getTRONChannel());
        map.put("contract.BSCChannel", contractProperty.getBSCChannel());
        map.put("contract.usdt", contractProperty.getUsdt());

        map.put("node.bsc", nodeProperty.getBsc());
        map.put("node.chainId", nodeProperty.getChainId());
        map.put("node.tron", nodeProperty.getTron());
        map.put("node.icplaza", nodeProperty.getIcplaza());


        map.put("chain.baseUrl", chainProperty.getBaseUrl());


        return map;
    }

    @ApiOperation("随机私钥")
    @GetMapping("/randomKey")
    public String randomKey() {
        SECP256K1.KeyPair kp = SECP256K1.KeyPair.generate();
        return Hex.toHexString(kp.getPrivateKey().getEncoded());
    }

    @ApiOperation("销毁状态")
    @GetMapping("/burn/{id}/{status}")
    public boolean burn(@PathVariable BigInteger id, @PathVariable Integer status) {
        orderOutLogDAO.updateStep3StatusByAPI(id, status);
        return true;
    }

    @GetMapping("/bscScan/{start}/{end}")
    public Map bscScan(@PathVariable BigInteger start, @PathVariable BigInteger end) {
        Map map = new HashMap();
        if (BSC.start == false && end.compareTo(start) == 1 && start.compareTo(BigInteger.ZERO) == 1 && end.compareTo(BigInteger.ZERO) == 1) {
            BSC.setBlockNumber(start, end);
            map.put("start", start);
            map.put("end", end);
            return map;
        }
        map.put("start", BSC.startBlockNumber);
        map.put("end", BSC.endBlockNumber);
        return map;
    }

    @GetMapping("/bscScanStatus/")
    public boolean bscScanStatus() {
        return BSC.start;
    }

    @GetMapping("/version")
    public String version() {
        return Constant.VERSION;
    }
}
