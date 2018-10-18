--分布式锁myql实现的sql
drop table if exists `methodLock`;
CREATE TABLE `methodLock` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lock_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '锁定的方法名',
  `lock_desc` VARCHAR(1024) NOT NULL DEFAULT '备注信息',
  `expire_time`  bigint(22) NOT NULL DEFAULT '0' COMMENT '锁的过期时间（单位毫秒）',
  `lock_status` TINYINT NOT NULL DEFAULT 1 COMMENT '记录状态，0：无效，1：有效',
  `create_time`  DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '锁创建时间',
  `update_time`  DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '锁更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_lock_name` (`lock_name`) USING BTREE
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='myql分布式锁';