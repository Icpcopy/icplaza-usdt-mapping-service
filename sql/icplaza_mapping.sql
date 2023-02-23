/*
Navicat MySQL Data Transfer

Source Server         : 192.168.239.130
Source Server Version : 50733
Source Host           : 192.168.239.130:3306
Source Database       : icplaza_mapping

Target Server Type    : MYSQL
Target Server Version : 50733
File Encoding         : 65001

Date: 2022-10-18 15:14:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for address_log
-- ----------------------------
DROP TABLE IF EXISTS `address_log`;
CREATE TABLE `address_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `icplaza` varchar(46) NOT NULL COMMENT '绑定的icplaza地址',
  `address` varchar(50) NOT NULL COMMENT '生成的地址',
  `address_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '生成的地址类型',
  `private_key` tinytext NOT NULL COMMENT '地址的私钥',
  `usdt` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT 'usdt余额',
  `last_scan` int(11) NOT NULL DEFAULT '0' COMMENT '扫描时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for block
-- ----------------------------
DROP TABLE IF EXISTS `block`;
CREATE TABLE `block` (
  `height` bigint(20) NOT NULL DEFAULT '0' COMMENT '区块高度',
  `code` tinyint(1) NOT NULL DEFAULT '0' COMMENT '代码',
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of block
-- ----------------------------
INSERT INTO `block` VALUES ('0', '0');
INSERT INTO `block` VALUES ('0', '1');
INSERT INTO `block` VALUES ('0', '2');
INSERT INTO `block` VALUES ('0', '3');

-- ----------------------------
-- Table structure for conf
-- ----------------------------
DROP TABLE IF EXISTS `conf`;
CREATE TABLE `conf` (
  `channel_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '通道：0-TRON;1-BSC;2-ETH',
  `fee` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '转出手续费',
  `max_limit` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '审核限额'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of conf
-- ----------------------------
INSERT INTO `conf` VALUES ('0', '1.000000', '1000.000000');
INSERT INTO `conf` VALUES ('1', '2.000000', '200.000000');

-- ----------------------------
-- Table structure for order_in_log
-- ----------------------------
DROP TABLE IF EXISTS `order_in_log`;
CREATE TABLE `order_in_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_type` tinyint(1) DEFAULT '0' COMMENT '0-TRON;1-BSC;2-ETH',
  `order_no` bigint(20) NOT NULL,
  `icplaza` varchar(46) NOT NULL,
  `from_0` varchar(50) NOT NULL,
  `to_0` varchar(50) NOT NULL,
  `value_0` decimal(20,6) NOT NULL,
  `hash_0` varchar(66) NOT NULL,
  `hash_status_0` tinyint(1) NOT NULL DEFAULT '0' COMMENT '哈希状态：0-处理中；1-成功；2-失败',
  `from_1` varchar(50) DEFAULT NULL,
  `to_1` varchar(50) DEFAULT NULL,
  `value_1` decimal(20,6) NOT NULL DEFAULT '0.000000',
  `fee_1` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '手续费',
  `hash_1` varchar(66) DEFAULT NULL,
  `hash_status_1` tinyint(1) NOT NULL DEFAULT '0' COMMENT '哈希状态：0-处理中；1-成功；2-失败',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  UNIQUE KEY `hash_0` (`hash_0`),
  UNIQUE KEY `hash_1` (`hash_1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for order_out_log
-- ----------------------------
DROP TABLE IF EXISTS `order_out_log`;
CREATE TABLE `order_out_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_type` tinyint(1) DEFAULT '0' COMMENT '0-TRON;1-BSC;2-ETH',
  `order_no` bigint(20) NOT NULL,
  `icplaza` varchar(46) NOT NULL,
  `from_0` varchar(50) NOT NULL,
  `to_0` varchar(50) NOT NULL,
  `value_0` decimal(20,6) NOT NULL,
  `receiver` varchar(255) NOT NULL COMMENT '接收地址',
  `hash_0` varchar(66) NOT NULL,
  `hash_status_0` tinyint(1) NOT NULL DEFAULT '0' COMMENT '哈希状态：0-处理中；1-成功；2-失败',
  `from_1` varchar(50) DEFAULT NULL,
  `to_1` varchar(50) DEFAULT NULL,
  `value_1` decimal(20,6) NOT NULL DEFAULT '0.000000',
  `fee_1` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '手续费',
  `hash_1` varchar(66) DEFAULT NULL,
  `hash_status_1` tinyint(1) NOT NULL DEFAULT '0' COMMENT '哈希状态：0-处理中；1-成功；2-失败',
  `from_2` varchar(50) DEFAULT NULL,
  `to_2` varchar(50) DEFAULT NULL,
  `value_2` decimal(20,6) NOT NULL DEFAULT '0.000000',
  `fee_2` decimal(20,6) NOT NULL DEFAULT '0.000000' COMMENT '手续费',
  `hash_2` varchar(66) DEFAULT NULL,
  `hash_status_2` tinyint(1) NOT NULL DEFAULT '0' COMMENT '哈希状态：0-处理中；1-成功；2-失败',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `burn_time` datetime DEFAULT NULL,
  `refund` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1=回退',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  UNIQUE KEY `hash_0` (`hash_0`),
  UNIQUE KEY `hash_1` (`hash_1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

