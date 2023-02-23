package com.icplaza.mapping.jobs;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.icplaza.mapping.bo.EVMBlockJSON;
import com.icplaza.mapping.bo.EVMBlockTransaction;
import com.icplaza.mapping.bo.EVMReceiptJSON;
import com.icplaza.mapping.bo.EVMReceiptLog;
import com.icplaza.mapping.common.*;
import com.icplaza.mapping.dao.*;
import com.icplaza.mapping.model.*;
import com.icplaza.mapping.properties.ChainProperty;
import com.icplaza.mapping.properties.ContractProperty;
import com.icplaza.mapping.properties.NodeProperty;
import com.icplaza.mapping.service.*;
import com.icplaza.mapping.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.icplaza.mapping.common.FeeAndLimit.TRON_FEE;


/**
 * 区块扫描
 */
@Service
@EnableScheduling
@Slf4j
public class Job {
    @Autowired
    AddressLogDAO addressLogDAO;
    @Autowired
    BlockDAO blockDAO;
    @Autowired
    ChainDataService chainDataService;
    @Autowired
    ChainProperty chainProperty;
    @Autowired
    OrderInLogModel orderInLogModel;
    @Autowired
    OrderOutLogDAO orderOutLogDAO;
    @Autowired
    OrderInLogDAO orderInLogDao;
    @Autowired
    PushService pushService;
    @Autowired
    Web3ICPlazaService web3ICPlazaService;
    @Autowired
    Web3TRONService web3TRONService;
    @Autowired
    Web3BSCService web3BSCService;
    @Autowired
    EVMService evmService;
    @Autowired
    NodeProperty nodeProperty;
    @Autowired
    ContractProperty contractProperty;
    @Autowired
    ConfDAO confDAO;
    // 放到线程池避免影响后续定时任务
    int nThreads = 25;
    ExecutorService service = Executors.newFixedThreadPool(nThreads);

    private static boolean PENDING_TRANSACTION_START = false;
    private static boolean INIT_BLOOM = false;
    private static boolean IN_SCAN = false;
    private static boolean IN_EVM_SCAN = false;
    private static boolean IN_BSC = false;
    private static boolean IN_MINT = false;
    private static boolean IN_SEND = false;
    private static boolean IN_BURNLOCK = false;
    private static boolean IN_MANUAL = false;
    private static boolean ORDER_IN_STEP2_HASH_STATUS = false;
    private static boolean ORDER_OUT_STEP2_HASH_STATUS = false;
    private static boolean ORDER_OUT_STEP3_HASH_STATUS = false;
    private static Map<String, Boolean> WORKER = new HashMap<>();
    private static Map<String, Boolean> EVM_WORKER = new HashMap<>();

    @Scheduled(fixedRate = 1000)
    private void initBloomFilter() {
        if (MyBloomFilter.ready() || INIT_BLOOM) {
            return;
        }
        log.info("初始化过滤器");
        Integer pageSize = 1000;
        Integer rows = addressLogDAO.total();
        try {
            long totalPage = 0;
            if (rows > 0) {
                totalPage = rows / pageSize + 1;
            }
            for (int i = 1; i <= totalPage; i++) {
                Integer start = (i - 1) * pageSize;
                List<AddressLogModel> list = addressLogDAO.selectAllModels(start, pageSize);
                if (list.size() > 0) {
                    for (AddressLogModel addressLogModel : list) {
                        if (LogSwitch.getOpen()) {
                            log.info("加入布隆过滤器：" + addressLogModel.getAddress());
                        }
                        MyBloomFilter.put(addressLogModel.getAddress());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        MyBloomFilter.setReady(true);
        log.info("初始化布隆过滤器ok=" + rows);
        log.info("读取并设置手续费...");
        ConfModel conf = confDAO.selectModel(ChainType.TRON.ordinal());
        if (conf != null) {
            TRON_FEE = conf.getFee();
            FeeAndLimit.TRON_MAX_LIMIT = conf.getMaxLimit();
        }
        conf = confDAO.selectModel(ChainType.BSC.ordinal());
        if (conf != null) {
            FeeAndLimit.BSC_FEE = conf.getFee();
            FeeAndLimit.BSC_MAX_LIMIT = conf.getMaxLimit();
        }
        log.info("手续费设置为 TRON_FEE {}, TRON_MAX_LIMIT {}, BSC_FEE {}, BSC_MAX_LIMIT {}", TRON_FEE, FeeAndLimit.TRON_MAX_LIMIT, FeeAndLimit.BSC_FEE, FeeAndLimit.BSC_MAX_LIMIT);
    }

    /**
     * 更新波场扫描高度
     */
    private void updateTRONBlockHeight(BlockModel model) {
        try {
            model.setCode(HeightCodeType.TRON.ordinal());
            blockDAO.update(model);
        } catch (Exception e) {
            e.printStackTrace();
            blockDAO.add(model);
        }
    }

    // 区块扫描
    @Scheduled(fixedRate = 1000)
    public void scan() {
        if (!IN_SCAN && MyBloomFilter.ready()) {
            log.info("TRON区块高度扫描...");
            IN_SCAN = true;
            service.execute(() -> {
                Long lastUpdateHeight = 0L;
                // 无限循环
                while (true) {
                    try {
                        if (JobPool.jobSize() <= JobPool.jobMinSize()) {
                            BlockModel blockModel = blockDAO.getBlock(ChannelType.TRON.ordinal());
                            long height = blockModel.getHeight();
                            // 不要追随太近，防止数据服务没有数据
                            Long blockHeight = chainDataService.queryBlockHeight();
                            blockHeight = blockHeight == null ? 0 : blockHeight;
                            long currentHeight = blockHeight - 3;
                            height = height == 0 ? currentHeight - 1 : (lastUpdateHeight > 0 ? lastUpdateHeight : height);
                            if (currentHeight > height) {
                                if (currentHeight - height > 50) {
                                    lastUpdateHeight = height + 51;
                                } else {
                                    lastUpdateHeight = currentHeight;
                                }
                                JobPool.add(height + 1, lastUpdateHeight);
                            }
                        } else {
                            // sleep 10s
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        log.error("区块高度任务失败");
                        log.error(e.getMessage());
                    }
                }
            });
        }
    }

    // 启用10个服务
    @Scheduled(fixedRate = 1000)
    public void callWorkers() {
        callWorker(10);
    }

    public void callWorker(Integer count) {
        for (int i = 0; i < count; i++) {
            String label = "worker_" + i;
            if (WORKER.get(label) == null && MyBloomFilter.ready()) {
                WORKER.put(label, true);
                log.info(label + " start work!");
                service.execute(() -> {
                    // work for death
                    while (true) {
                        try {
                            worker();
                        } catch (Exception e) {
                            log.error(label + " error");
                            log.error(e.getMessage());
                        }
                    }
                });
            }
            //
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新处理未处理的交易
     */
    @Scheduled(fixedRate = 1000)
    private void pendingTransactions() {
        if (PENDING_TRANSACTION_START == false) {
            log.info("deal pending transactions");
            PENDING_TRANSACTION_START = true;
            try {
                while (PendingTransactionPool.size() > 0) {
                    Thread.sleep(1000);
                    dealTRC20Transaction(PendingTransactionPool.getJob());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            PENDING_TRANSACTION_START = false;
        }
    }

    /**
     * 区块任务处理器
     */
    private void worker() {
        long height = JobPool.getJob();
        if (height > 0) {
            try {
                dealTronHeight(height);
                BlockModel model = new BlockModel();
                model.setHeight(height);
                model.setCode(ChannelType.TRON.ordinal());
                updateTRONBlockHeight(model);
            } catch (Exception e) {
                log.error("处理波场区块失败");
                e.printStackTrace();
                log.error("worker failed at " + height);
                log.error(e.getMessage());
            }
        } else {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理区块
     */
    private void dealTronHeight(long height) {
        log.info("deal height=>" + height);
        // 处理区块信息
        JSONObject jsonObject = chainDataService.getBlockDataByBlockNumber(height);
        // 高度重新放回队列
        if (jsonObject == null || jsonObject.getJSONObject("result") == null || jsonObject.getJSONObject("result").getJSONObject("block") == null || jsonObject.getJSONObject("result").getJSONObject("block").getInteger("blockNumber") == null) {
            log.error("重新放回队列：" + height);
            JobPool.add(height);
            return;
        }
        // 包含指定地址交易
        if (jsonObject.toJSONString().contains(chainProperty.getContract())) {
            jsonObject = jsonObject.getJSONObject("result");
            if (jsonObject != null) {
                // 处理交易，创建订单
                JSONArray transactions = jsonObject.getJSONArray("transaction");
                for (int k = 0; k < transactions.size(); k++) {
                    if (LogSwitch.getOpen()) {
                        log.info("开始处理交易：" + (k) + "/" + transactions.size());
                    }
                    JSONObject transaction = (JSONObject) transactions.get(k);
                    try {
                        this.dealTRC20Transaction(transaction);
                    } catch (Exception e) {
                        log.error("交易查询失败:" + height);
                        log.error("error:" + e.getMessage());
                        // 重新放回队列并停止处理
                        log.error("重新放回队列：" + height);
                        JobPool.add(height);
                        return;
                    }
                    if (LogSwitch.getOpen()) {
                        log.info("处理结束");
                    }
                }
            }
            if (LogSwitch.getOpen()) {
                log.info("高度" + height + "处理完成");
            }
        } else if (jsonObject.getJSONObject("result").getJSONObject("block") == null) {
            log.error("无交易数据：height=" + height);
        }
    }

    /**
     * 处理交易
     */
    private void dealTRC20Transaction(JSONObject transaction) {
        if (transaction == null) {
            log.error("transaction null");
            return;
        }
        String fromAddress = transaction.getString("from");
        String toAddress = transaction.getString("to");
        double amount = transaction.getDoubleValue("amount");
        String txhash = transaction.getString("txHash");
        Integer transactionStatus = transaction.getInteger("status");
        if (transactionStatus.equals(1) && toAddress.equalsIgnoreCase(chainProperty.getContract())) {
            JSONObject hashInfo = chainDataService.getTransactionContractByHash(txhash);
            if (hashInfo == null) {
                // log.error("链服务查询结果：交易信息不存在");
                log.info("error: " + txhash);
                PendingTransactionPool.add(transaction);
                return;
            }
            // hashInfo是个数组，里边可能存在多个交易
            JSONArray jsonArray = hashInfo.getJSONArray("result");
            if (jsonArray.size() == 0) {
                // log.error("链服务查询结果：交易信息不存在");
                log.info("空交易: " + txhash);
                return;
            }
            // 循环查询
            for (int i = 0; i < jsonArray.size(); i++) {
                hashInfo = jsonArray.getJSONObject(i);
                String to = hashInfo.getString("to");
                if (!MyBloomFilter.mightContain(to)) {
                    if (LogSwitch.getOpen()) {
                        log.error("不在过滤器：" + to);
                    }
                    continue;
                }
                // 查询确实在
                AddressLogModel addressLogModel = addressLogDAO.selectModelByAddress(to);
                if (addressLogModel != null) {
                    // 确认hash没有被记录
                    amount = hashInfo.getDoubleValue("amount");
                    OrderInLogModel order = orderInLogDao.selectModelByHash0(txhash);
                    if (order == null) {
                        // save
                        String orderNo = OrderUtils.getOrderNo();
                        order = new OrderInLogModel();
                        order.setOrderNo(new BigInteger(orderNo));
                        order.setChannelType(ChannelType.TRON.ordinal());
                        order.setIcplaza(addressLogModel.getIcplaza());
                        order.setFrom_0(fromAddress);
                        order.setTo_0(to);
                        order.setValue_0(new BigDecimal(amount));
                        order.setHash_0(txhash);
                        order.setHashStatus_0(Status.SUCCESS.ordinal());
                        order.setCreateTime(new Date());
                        order.setUpdateTime(new Date());
                        orderInLogDao.addStep1(order);
                    }
                }
            }
        }
    }


    // ====================================EVM 数据扫描====================================
    @Scheduled(fixedRate = 1000)
    public void EVMScan() {
        if (!IN_EVM_SCAN && MyBloomFilter.ready()) {
            log.info("EVM区块高度扫描...");
            IN_EVM_SCAN = true;
            service.execute(() -> {
                Long lastUpdateHeight = 0L;
                // 无限循环
                while (true) {
                    try {
                        if (EVMJobPool.jobSize() <= EVMJobPool.jobMinSize()) {
                            BlockModel blockModel = blockDAO.getBlock(HeightCodeType.EVM_TRON_CHANNEL.ordinal());
                            long height = blockModel.getHeight();
                            // 不要追随太近，防止数据服务没有数据
                            BigInteger blockHeightOnLine = evmService.eth_blockNumber();
                            log.info("blockHeightOnLine {}", blockHeightOnLine);
                            while (blockHeightOnLine == null) {
                                Thread.sleep(20000);
                                blockHeightOnLine = evmService.eth_blockNumber();
                            }
                            Long blockHeight = blockHeightOnLine == null ? 0 : blockHeightOnLine.longValue();
                            long currentHeight = blockHeight - 3;
                            height = height == 0 ? currentHeight - 1 : (lastUpdateHeight > 0 ? lastUpdateHeight : height);
                            if (currentHeight > height) {
                                if (currentHeight - height > 50) {
                                    lastUpdateHeight = height + 51;
                                } else {
                                    lastUpdateHeight = currentHeight;
                                }
                                EVMJobPool.add(height + 1, lastUpdateHeight);
                            }
                        }
                        Thread.sleep(10000);
                    } catch (Exception e) {
                        log.error("区块高度任务失败");
                        log.error(e.getMessage());
                    }
                }
            });
        }
    }

    @Scheduled(fixedRate = 1000)
    public void callEVMWorkers() {
        callEVMWorker(5);
    }

    public void callEVMWorker(Integer count) {
        for (int i = 0; i < count; i++) {
            String label = "evm worker_" + i;
            if (EVM_WORKER.get(label) == null && MyBloomFilter.ready()) {
                EVM_WORKER.put(label, true);
                log.info(label + " start work!");
                service.execute(() -> {
                    // work for death
                    while (true) {
                        try {
                            EVMWorker();
                        } catch (Exception e) {
                            log.error(label + " error");
                            log.error(e.getMessage());
                        }
                    }
                });
            }
        }
    }

    /**
     * 区块任务处理器
     */
    private void EVMWorker() {
        long height = EVMJobPool.getJob();
        if (height > 0) {
            try {
                dealEVMHeight(new BigInteger(String.valueOf(height)));
            } catch (Exception e) {
                log.error("处理EVM区块失败");
                e.printStackTrace();
                log.error("worker failed at " + height);
                log.error(e.getMessage());
            }
        } else {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dealEVMHeight(BigInteger height) {
        log.info("deal evm height {}", height);
        String block = evmService.eth_getBlockByNumber(height);
        String tronChannel = contractProperty.getTRONChannel();
        String bscChannel = contractProperty.getBSCChannel();

        if (block.contains(tronChannel.toLowerCase()) || block.contains(bscChannel.toLowerCase())) {
            // 查询转出限制
            // BigDecimal tronLimit = BigDecimal.ZERO;
            // BigDecimal bscLimit = BigDecimal.ZERO;
            // ConfModel tronConf = confDAO.selectModel(ChainType.TRON.ordinal());
            // if (tronConf != null) {
            //     tronLimit = tronConf.getMaxLimit();
            // }
            // ConfModel bscConf = confDAO.selectModel(ChainType.BSC.ordinal());
            // if (bscConf != null) {
            //     bscLimit = bscConf.getMaxLimit();
            // }

            EVMBlockJSON blockJson = new Gson().fromJson(block, EVMBlockJSON.class);
            for (EVMBlockTransaction transaction : blockJson.getResult().getTransactions()) {
                // 指定合约
                log.info("{}", transaction.getTo());
                if (transaction.getTo().equalsIgnoreCase(tronChannel) || transaction.getTo().equalsIgnoreCase(bscChannel)) {
                    String hash = transaction.getHash();
                    // 查询凭证
                    String rs = evmService.eth_getTransactionReceipt(hash);
                    if (rs == null) {
                        log.error("获取凭证失败 {}", hash);
                        continue;
                    }
                    EVMReceiptJSON receiptJSON = new Gson().fromJson(rs, EVMReceiptJSON.class);
                    // 只处理成功记录
                    if (receiptJSON.getResult().getStatus().equals("0x1")) {
                        String from = receiptJSON.getResult().getFrom();
                        // 合约地址
                        String to = receiptJSON.getResult().getTo();
                        // log
                        for (EVMReceiptLog log : receiptJSON.getResult().getLogs()) {
                            // 本合约的log
                            if (log.getAddress().equals(to)) {
                                List<String> topics = log.getTopics();
                                if (topics.get(0).equals("0xfd726b3c66354a0801cf61e13f1282806917ff4f06bf73781d47043677f64a53")) {
                                    BigInteger a = new BigInteger(topics.get(2).substring(2), 16);
                                    BigDecimal amount = new BigDecimal(a).divide(new BigDecimal(BigInteger.TEN.pow(18)));
                                    // save to db
                                    int channelType = ChannelType.TRON.ordinal();

                                    Integer status = Status.SUCCESS.ordinal();
                                    BigDecimal maxLimit;
                                    if (to.equalsIgnoreCase(bscChannel)) {
                                        maxLimit = FeeAndLimit.BSC_MAX_LIMIT;
                                        channelType = ChannelType.BSC.ordinal();
                                        if (amount.compareTo(maxLimit) > 0) {
                                            status = Status.WAITING.ordinal();
                                        }
                                    } else {
                                        maxLimit = FeeAndLimit.TRON_MAX_LIMIT;
                                        if (amount.compareTo(maxLimit) > 0) {
                                            status = Status.WAITING.ordinal();
                                        }
                                    }


                                    String receiver = parseData(log.getData().substring(2));
                                    String icplaza = null;
                                    try {
                                        icplaza = AddressUtil.convertEthAddressToCosmos(from, "icplaza");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    String orderNo = OrderUtils.getOrderNo();
                                    OrderOutLogModel order = new OrderOutLogModel();
                                    order.setOrderNo(new BigInteger(orderNo));
                                    order.setChannelType(channelType);
                                    order.setIcplaza(icplaza);
                                    order.setFrom_0(from);
                                    order.setTo_0("0x0");
                                    order.setValue_0(amount);
                                    order.setHash_0(hash);
                                    order.setHashStatus_0(status);
                                    order.setReceiver(receiver);
                                    order.setCreateTime(new Date());

                                    orderOutLogDAO.addStep1(order);
                                }
                            }
                        }
                    }
                } else {
                    log.info("no data");
                }

            }

        }
        //update block
        BlockModel blockModel = new BlockModel();
        blockModel.setHeight(height.longValue());
        blockModel.setCode(HeightCodeType.EVM_TRON_CHANNEL.ordinal());
        blockDAO.update(blockModel);
    }

    // =====================================================BSC usdt扫描======================================================
    //充值
    @Scheduled(fixedRate = 1000)
    private void bsc() {
        if (!IN_BSC) {
            IN_BSC = true;
            log.info("BSC链上扫描...");
            service.execute(() -> {
                BigInteger lastBlockNumber = BigInteger.ZERO;
                // 每次只能返回1000条数据，step不能太大
                BigInteger step = new BigInteger("20");
                while (true) {
                    try {
                        Thread.sleep(1000);
                        BigInteger blockNumber = BSCTool.getLatestBlockNumber();
                        log.info("bsc 最新高度 {}", blockNumber);
                        if (lastBlockNumber.equals(BigInteger.ZERO)) {
                            BlockModel blockModel = blockDAO.getBlock(ChannelType.BSC.ordinal());
                            lastBlockNumber = BigInteger.valueOf(blockModel.getHeight());
                        }
                        if (blockNumber.subtract(lastBlockNumber).compareTo(step) > 0) {
                            blockNumber = lastBlockNumber.add(step);
                            log.info("修正bsc扫描范围：{} {}", lastBlockNumber, blockNumber);
                        }
                        log.info("scan bsc from {} to {}", lastBlockNumber, blockNumber);
                        List<Object> list = BSCTool.parseTransactions(lastBlockNumber, blockNumber, contractProperty.getUSDTOnBSC(), true);

                        for (Object order : list) {
                            OrderInLogModel model = (OrderInLogModel) order;
                            if (orderInLogDao.selectModelByHash0(model.getHash_0()) == null) {
                                AddressLogModel address = addressLogDAO.selectModelByAddress(model.getTo_0());
                                if (address == null || address.getAddress().equalsIgnoreCase(AddressUtil.zero())) {
                                    log.info("to地址不存在于数据库无法录入 {}", new Gson().toJson(model));
                                    continue;
                                }
                                model.setOrderNo(new BigInteger(OrderUtils.getOrderNo()));
                                model.setIcplaza(address.getIcplaza());
                                model.setCreateTime(new Date());
                                model.setChannelType(ChannelType.BSC.ordinal());
                                orderInLogDao.addStep1(model);
                            } else {
                                log.error("bsc数据存在于数据库 {}", new Gson().toJson(model));
                            }
                        }

                        // 更新高度信息
                        if (blockNumber.compareTo(lastBlockNumber) > 0) {
                            BlockModel blockModel = new BlockModel();
                            blockModel.setCode(ChannelType.BSC.ordinal());
                            blockModel.setHeight(blockNumber.longValue());
                            blockDAO.update(blockModel);
                            log.info("update bsc blockHeight to {}", blockNumber);
                            // 设置最后扫描高度
                            lastBlockNumber = blockNumber;
                        }

                    } catch (Exception e) {
                        log.error("bsc fail {}", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * BSC人工加入
     */
    @Scheduled(fixedRate = 1000)
    private void manualBSC() {
        if (!IN_MANUAL) {
            IN_MANUAL = true;
            log.info("开启BSC手动扫描任务...");
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(20000);
                        if (BSC.start) {
                            BigInteger startBlockNumber = BSC.startBlockNumber;
                            BigInteger endBlockNumber = BSC.endBlockNumber;
                            log.info("有 BSC 手动扫描任务 scan bsc from {} to{}", startBlockNumber, endBlockNumber);
                            BigInteger step = new BigInteger("20");

                            while (startBlockNumber.compareTo(endBlockNumber) == -1) {
                                BigInteger lastBlockNumber = startBlockNumber;
                                BigInteger blockNumber = startBlockNumber.add(step);
                                log.info("手动任务 scan bsc from {} to {}", lastBlockNumber, blockNumber);
                                try {

                                    List<Object> list = BSCTool.parseTransactions(lastBlockNumber, blockNumber, contractProperty.getUSDTOnBSC(), true);
                                    for (Object order : list) {
                                        OrderInLogModel model = (OrderInLogModel) order;
                                        if (orderInLogDao.selectModelByHash0(model.getHash_0()) == null) {
                                            AddressLogModel address = addressLogDAO.selectModelByAddress(model.getTo_0());
                                            if (address == null || address.getAddress().equalsIgnoreCase(AddressUtil.zero())) {
                                                log.info("to地址不存在于数据库无法录入 {}", new Gson().toJson(model));
                                                continue;
                                            }
                                            model.setOrderNo(new BigInteger(OrderUtils.getOrderNo()));
                                            model.setIcplaza(address.getIcplaza());
                                            model.setCreateTime(new Date());
                                            model.setChannelType(ChannelType.BSC.ordinal());
                                            orderInLogDao.addStep1(model);
                                        } else {
                                            log.error("bsc数据存在于数据库 {}", new Gson().toJson(model));
                                        }
                                    }
                                    startBlockNumber = startBlockNumber.add(step);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            log.info("手动任务 BSC链上扫描结束");
                            BSC.stop();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }

    /**
     * BSC根据哈希
     * */


    /**
     * 映射入自动铸造
     */
    @Scheduled(fixedRate = 1000)
    private void autoMint() {
        if (!IN_MINT) {
            log.info("开启自动铸造...");
            IN_MINT = true;
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        List<OrderInLogModel> list = orderInLogDao.step2List();
                        if (list.size() > 0) {
                            log.info("铸造条数 {}", list.size());
                        }
                        for (OrderInLogModel model : list) {
                            if (RSATool.getSK() == null) {
                                log.error("需要设置转账私钥");
                                continue;
                            }
                            String from_1 = "0x0";
                            String to_1;
                            try {
                                to_1 = AddressUtil.convertICPlazaToEth(model.getIcplaza());
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                            model.setFrom_1(from_1);
                            model.setTo_1(to_1);
                            model.setValue_1(model.getValue_0());
                            model.setUpdateTime(new Date());

                            if (model.getHash_1() != null && web3ICPlazaService.hashStatus(model.getHash_1()) == Status.SUCCESS.ordinal()) {
                                log.error("成功的哈希 {} ，禁止重新铸造", model.getHash_1());
                                continue;
                            }

                            String hash = web3ICPlazaService.mint(model);
                            if (hash != null) {
                                model.setHash_1(hash);
                                model.setUpdateTime(new Date());
                                model.setHashStatus_1(Status.PENDING.ordinal());
                                orderInLogDao.addStep2(model);
                                log.info("铸造成功： {} {} {} {}", model.getFrom_1(), model.getTo_1(), model.getValue_1(), hash);
                            } else {
                                log.error("铸造失败");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 自动转账 映射出第二步
     */
    @Scheduled(fixedRate = 1000)
    private void autoSend() {
        if (!IN_SEND) {
            log.info("开启映射出第二步：自动转账...");
            IN_SEND = true;
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(20000);

                        List<OrderOutLogModel> list = orderOutLogDAO.step2List();
                        if (list.size() > 0) {
                            log.info("映射出第二步 size {}", list.size());
                        }
                        for (OrderOutLogModel model : list) {
                            Thread.sleep(5000);
                            if (RSATool.getSK() == null) {
                                log.error("需要设置转账私钥");
                                continue;
                            }
                            String from_1 = "0x0";
                            String receiver = model.getReceiver();
                            String hash;
                            BigDecimal fee = BigDecimal.ZERO;
                            log.info("id = {}", model.getId());
                            if (model.getChannelType() == ChannelType.TRON.ordinal()) {
                                fee = FeeAndLimit.TRON_FEE;
                                if (AddressUtil.validTRONAddress(receiver)) {
                                    // 波场转账
                                    model.setFrom_1(TronTool.getAddressByPrivateKey(RSATool.getSK()));
                                    model.setTo_1(receiver);

                                    BigDecimal v = model.getValue_0().subtract(fee);
                                    if (v.compareTo(BigDecimal.ZERO) < 0) {
                                        v = BigDecimal.ZERO;
                                    }
                                    model.setValue_1(v);
                                    if (model.getValue_1().compareTo(BigDecimal.ZERO) > 0) {
                                        try {
                                            hash = web3TRONService.transfer(model);
                                            if (hash != null) {
                                                model.setHash_1(hash);
                                                model.setFee_1(fee);
                                                model.setUpdateTime(new Date());
                                                orderOutLogDAO.addStep2(model);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            continue;
                                        }
                                    } else {
                                        // 设置成为成功
                                        model.setHash_1(String.valueOf(System.currentTimeMillis()));
                                        model.setFee_1(fee);
                                        model.setUpdateTime(new Date());
                                        orderOutLogDAO.addStep2Success(model);
                                    }
                                } else {
                                    // 退回
                                    log.error("接收地址错误，应该是tron地址，调用回退");
                                    model.setFrom_1("0x0");
                                    // from0地址为to1
                                    model.setTo_1(model.getFrom_0());
                                    model.setValue_1(model.getValue_0());
                                    try {

                                        hash = web3ICPlazaService.freeLock(model);
                                        if (hash != null) {
                                            model.setHash_1(hash);
                                            model.setUpdateTime(new Date());
                                            orderOutLogDAO.addStep2Refund(model);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        continue;
                                    }
                                }
                            } else if (model.getChannelType() == ChannelType.BSC.ordinal()) {
                                fee = FeeAndLimit.BSC_FEE;
                                if (AddressUtil.validBSCAddress(receiver)) {
                                    // 转账
                                    model.setFrom_1(BSCTool.getAddressByPrivateKey(RSATool.getSK()));
                                    model.setTo_1(receiver);
                                    BigDecimal v = model.getValue_0().subtract(fee);
                                    if (v.compareTo(BigDecimal.ZERO) < 0) {
                                        v = BigDecimal.ZERO;
                                    }
                                    model.setValue_1(v);
                                    if (model.getValue_1().compareTo(BigDecimal.ZERO) > 0) {
                                        hash = web3BSCService.transfer(model);
                                        if (hash != null) {
                                            model.setHash_1(hash);
                                            model.setFee_1(fee);
                                            model.setUpdateTime(new Date());
                                            orderOutLogDAO.addStep2(model);
                                        }
                                    } else {
                                        // 设置成为成功
                                        model.setHash_1(String.valueOf(System.currentTimeMillis()));
                                        model.setFee_1(fee);
                                        model.setUpdateTime(new Date());
                                        orderOutLogDAO.addStep2Success(model);
                                    }
                                } else {
                                    // 退回
                                    log.error("接收地址错误，应该是bsc地址，调用回退");
                                    model.setFrom_1("0x0");
                                    // from0地址为to1
                                    model.setTo_1(model.getFrom_0());
                                    model.setValue_1(model.getValue_0());
                                    hash = web3ICPlazaService.freeLock(model);
                                    if (hash != null) {
                                        model.setHash_1(hash);
                                        model.setUpdateTime(new Date());
                                        orderOutLogDAO.addStep2Refund(model);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * 销毁锁定资产
     */
    @Scheduled(fixedRate = 10000)
    private void autoBurnLock() {
        if (!IN_BURNLOCK) {
            log.info("开启自动销毁...");
            IN_BURNLOCK = true;
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        List<OrderOutLogModel> models = orderOutLogDAO.getBurnLockPending();
                        log.info("待销毁 {}", models.size());
                        for (OrderOutLogModel model : models) {
                            if (RSATool.getSK() == null) {
                                log.error("需要设置转账私钥");
                                continue;
                            }
                            //忽略处理中
                            if (model.getHashStatus_2() == Status.PENDING.ordinal() && model.getHash_2() != null) {
                                continue;
                            }
                            model.setFrom_2(model.getFrom_0());
                            model.setTo_2("0x0");
                            model.setValue_2(model.getValue_0());
                            log.info("model {}", new Gson().toJson(model));
                            String hash_2 = web3ICPlazaService.burnLock(model);
                            if (hash_2 != null) {
                                model.setHash_2(hash_2);
                                model.setBurnTime(new Date());
                                orderOutLogDAO.addStep3(model);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * data数据解析为string
     */
    private String parseData(String data) {
        BigInteger len = new BigInteger(data.substring(64, 128), 16);
        String receiver = data.substring(128, len.intValue() * 2 + 128);
        return new String(Hex.decode(receiver));
    }

    /**
     * evm hash status
     * 映射入订单第二步状态
     */
    @Scheduled(fixedRate = 1000)
    private void orderInStep2HashStatus() {
        if (!ORDER_IN_STEP2_HASH_STATUS) {
            ORDER_IN_STEP2_HASH_STATUS = true;
            log.info("映射入订单第二步状态...");
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        List<OrderInLogModel> list = orderInLogDao.step2PendingList();
                        if (list.size() == 0) {
                            Thread.sleep(10000);
                        }
                        for (OrderInLogModel model : list) {
                            int status = web3ICPlazaService.hashStatus(model.getHash_1());
                            if (status != Status.PENDING.ordinal()) {
                                model.setHashStatus_1(status);
                                orderInLogDao.updateStep2Status(model);
                            } else {
                                log.info("PENDING {}", new Gson().toJson(model));
                                // 超过30分钟设置为失败
                                long betweenMinutes = DateUtil.between(new Date(), model.getUpdateTime(), DateUnit.MINUTE);
                                if (betweenMinutes > Constant.REPLAY_PERIOD) {
                                    model.setHashStatus_1(Status.FAIL.ordinal());
                                    orderInLogDao.updateStep2Status(model);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 映射出订单第二步状态
     */
    @Scheduled(fixedRate = 1000)
    private void orderOutStep2HashStatus() {
        if (!ORDER_OUT_STEP2_HASH_STATUS) {
            ORDER_OUT_STEP2_HASH_STATUS = true;
            log.info("映射出订单第二步状态...");
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        List<OrderOutLogModel> list = orderOutLogDAO.step2PendingList();
                        if (list.size() == 0) {
                            Thread.sleep(10000);
                        }
                        for (OrderOutLogModel model : list) {
                            //波场
                            if (model.getChannelType() == ChannelType.TRON.ordinal()) {
                                if (model.getRefund() == 1) {
                                    int status = web3ICPlazaService.hashStatus(model.getHash_1());
                                    if (status == Status.PENDING.ordinal()) {
                                        log.info("model {}", new Gson().toJson(model));
                                        long betweenMinutes = DateUtil.between(new Date(), model.getUpdateTime(), DateUnit.MINUTE);
                                        if (betweenMinutes > Constant.REPLAY_PERIOD) {
                                            model.setHashStatus_1(Status.FAIL.ordinal());
                                            orderOutLogDAO.updateStep2Status(model);
                                        }
                                    } else {
                                        model.setHashStatus_1(status);
                                        orderOutLogDAO.updateStep2Status(model);
                                    }
                                } else {
                                    int status = web3TRONService.hashStatus(model.getHash_1());
                                    if (status == Status.PENDING.ordinal()) {
                                        log.info("PENDING {}", new Gson().toJson(model));
                                        long betweenMinutes = DateUtil.between(new Date(), model.getUpdateTime(), DateUnit.MINUTE);
                                        if (betweenMinutes > Constant.REPLAY_PERIOD) {
                                            model.setHashStatus_1(Status.FAIL.ordinal());
                                            orderOutLogDAO.updateStep2Status(model);
                                        }
                                    } else {
                                        model.setHashStatus_1(status);
                                        orderOutLogDAO.updateStep2Status(model);
                                    }
                                }
                            }
                            // BSC
                            if (model.getChannelType() == ChannelType.BSC.ordinal()) {
                                if (model.getRefund() == 1) {
                                    int status = web3ICPlazaService.hashStatus(model.getHash_1());
                                    if (status == Status.PENDING.ordinal()) {
                                        log.info("model {}", new Gson().toJson(model));
                                        long betweenMinutes = DateUtil.between(new Date(), model.getUpdateTime(), DateUnit.MINUTE);
                                        if (betweenMinutes > Constant.REPLAY_PERIOD) {
                                            model.setHashStatus_1(Status.FAIL.ordinal());
                                            orderOutLogDAO.updateStep2Status(model);
                                        }
                                    } else {
                                        model.setHashStatus_1(status);
                                        orderOutLogDAO.updateStep2Status(model);
                                    }
                                } else {
                                    int status = web3BSCService.hashStatus(model.getHash_1());
                                    if (status == Status.PENDING.ordinal()) {
                                        log.info("model {}", new Gson().toJson(model));
                                        long betweenMinutes = DateUtil.between(new Date(), model.getUpdateTime(), DateUnit.MINUTE);
                                        if (betweenMinutes > Constant.REPLAY_PERIOD) {
                                            model.setHashStatus_1(Status.FAIL.ordinal());
                                            orderOutLogDAO.updateStep2Status(model);
                                        }
                                    } else {
                                        model.setHashStatus_1(status);
                                        orderOutLogDAO.updateStep2Status(model);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 映射出订单第三步状态
     */
    @Scheduled(fixedRate = 1000)
    private void orderOutStep3HashStatus() {
        if (!ORDER_OUT_STEP3_HASH_STATUS) {
            ORDER_OUT_STEP3_HASH_STATUS = true;
            log.info("映射出订单第三步状态...");
            service.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        List<OrderOutLogModel> list = orderOutLogDAO.step3PendingList();
                        if (list.size() == 0) {
                            Thread.sleep(10000);
                        }
                        for (OrderOutLogModel model : list) {
                            int status = web3ICPlazaService.hashStatus(model.getHash_2());
                            if (status == Status.PENDING.ordinal()) {
                                log.info("model {}", new Gson().toJson(model));
                                long betweenMinutes = DateUtil.between(new Date(), model.getBurnTime(), DateUnit.MINUTE);
                                if (betweenMinutes > Constant.REPLAY_PERIOD) {
                                    model.setHashStatus_2(Status.FAIL.ordinal());
                                    orderOutLogDAO.updateStep3Status(model);
                                }
                            } else {
                                model.setHashStatus_2(status);
                                orderOutLogDAO.updateStep3Status(model);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


}
