# 映射服务
## 注意：任何新的部署都要验证加密后的地址私钥是否能够解密
- 解密测试工具
[https://github.com/Icpcopy/icplaza-usdt-mapping-decrypt-test-tool](https://github.com/Icpcopy/icplaza-usdt-mapping-decrypt-test-tool)
### 用途
- 使用icplaza获取波场和bsc充值地址
- 调用icplaza上的合约铸造usdt
### 安全
- 地址私钥采用RSA公钥加密存储，RSA公钥写在com.ds.tron.common Constant.java
- 请求头部验证project-id,配置写在 com.ds.tron.common Constant.java
### 依赖
- 本服务依赖区块链数据服务
- 推送地址及区块链数据服务信息在 application.properties
### 部署
- 数据库安装 在sql文件夹
- 修改配置文件 application.properties
- 程序和配置文件放在同一目录
- 启动程序
```
nohup java -jar 程序 &
```
### 启动后输入私钥
- 私钥工具
[https://github.com/Icpcopy/icplaza-usdt-mapping-keytool](https://github.com/Icpcopy/icplaza-usdt-mapping-keytool)