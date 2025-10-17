/*
SQLyog v10.2 
MySQL - 5.5.40 : Database - el-admin
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`el-admin` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `eladmin`;

/*Table structure for table `permissions` */

DROP TABLE IF EXISTS `permissions`;

CREATE TABLE `permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

/*Data for the table `permissions` */

insert  into `permissions`(`id`,`name`,`description`,`created_at`) values (1,'user:read','查看用户信息','2025-03-22 15:21:50'),(2,'user:insert','添加用户','2025-03-22 15:21:50'),(3,'user:update','修改用户','2025-03-22 15:21:50'),(4,'user:delete','删除用户','2025-03-22 15:21:50'),(8,'user:read:all','查询所有用户','2025-04-10 13:25:15'),(9,'role:read','查看角色和权限','2025-04-10 15:00:00'),(10,'role:insert','添加角色','2025-04-10 15:00:00'),(11,'role:update','修改角色和权限','2025-04-10 15:00:00'),(12,'role:delete','删除角色','2025-04-10 15:00:00'),(13,'permission:read','查看权限','2025-04-10 15:00:00'),(14,'permission:insert','添加权限','2025-04-10 15:00:00'),(15,'permission:update','修改权限','2025-04-10 15:00:00'),(16,'permission:delete','删除权限','2025-04-10 15:00:00'),(17,'log:query','操作日志查看','2025-04-10 16:24:28'),(18,'modules','公共模块查看','2025-09-05 14:54:00'),(19,'modules.add','添加公共模块','2025-09-05 14:54:19'),(20,'modules.update','更新模块','2025-09-05 15:54:06'),(21,'modules.delete','删除公共模块','2025-09-05 15:54:21'),(22,'project.read','获取项目列表','2025-09-05 18:05:17'),(23,'project.add','创建项目','2025-09-05 18:05:29'),(24,'删除单个项目','根据ID删除单个项目','2025-09-05 18:05:41');



/*Table structure for table `role_permissions` */

DROP TABLE IF EXISTS `role_permissions`;

CREATE TABLE `role_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8;

/*Data for the table `role_permissions` */

insert  into `role_permissions`(`id`,`role_id`,`permission_id`,`created_at`) values (48,1,9,'2025-04-10 17:06:51'),(49,1,10,'2025-04-10 17:07:06'),(50,1,11,'2025-04-10 17:07:11'),(51,1,13,'2025-04-10 17:07:41'),(52,1,14,'2025-04-10 17:07:42'),(53,1,15,'2025-04-10 17:07:44'),(55,1,17,'2025-04-10 17:07:54'),(56,1,1,'2025-04-10 17:08:12'),(57,1,2,'2025-04-10 17:08:12'),(59,1,3,'2025-04-10 17:08:12'),(62,1,8,'2025-04-10 17:08:12'),(68,2,1,'2025-04-10 18:11:48'),(69,2,2,'2025-04-10 18:11:48'),(74,1,4,'2025-04-11 14:31:33'),(75,2,17,'2025-07-16 17:00:50'),(76,1,16,'2025-09-04 16:31:33'),(78,3,16,'2025-09-05 14:57:26'),(79,3,17,'2025-09-05 14:57:26'),(80,3,18,'2025-09-05 14:57:26'),(81,1,19,'2025-09-05 15:08:54'),(82,1,18,'2025-09-05 15:08:54'),(83,1,20,'2025-09-05 15:55:02'),(84,1,21,'2025-09-05 15:55:02'),(85,1,22,'2025-09-05 18:05:57'),(86,1,23,'2025-09-05 18:05:57'),(87,1,24,'2025-09-05 18:05:57'),(88,3,23,'2025-09-05 18:14:48'),(89,3,22,'2025-09-05 18:14:48'),(90,3,19,'2025-09-05 18:16:44'),(91,3,20,'2025-09-05 18:16:44'),(92,3,21,'2025-09-05 18:16:44'),(93,3,24,'2025-09-05 18:16:44');

/*Table structure for table `roles` */

DROP TABLE IF EXISTS `roles`;

CREATE TABLE `roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

/*Data for the table `roles` */

insert  into `roles`(`id`,`name`,`description`,`created_at`) values (1,'root','超级管理员','2025-04-10 17:05:27'),(2,'admin','管理员','2025-04-10 17:05:27'),(3,'user','普通用户','2025-04-10 17:05:27');

/*Table structure for table `user_disable_logs` */

DROP TABLE IF EXISTS `user_disable_logs`;

CREATE TABLE `user_disable_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `service` varchar(50) NOT NULL COMMENT '服务标识（如 user-freeze）',
  `is_disabled` tinyint(4) DEFAULT '0' COMMENT '是否封禁（0:未封禁, 1:已封禁）',
  `disable_until` timestamp NULL DEFAULT NULL COMMENT '封禁截止时间（NULL表示永久封禁）',
  `reason` varchar(255) DEFAULT NULL COMMENT '封禁原因',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_service` (`user_id`,`service`),
  CONSTRAINT `user_disable_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8 COMMENT='用户封禁信息表';

/*Data for the table `user_disable_logs` */

insert  into `user_disable_logs`(`id`,`user_id`,`service`,`is_disabled`,`disable_until`,`reason`,`created_at`,`updated_at`) values (29,29,'user-freeze',0,NULL,'123','2025-04-11 17:47:03','2025-09-04 16:06:14'),(30,38,'user-freeze',0,'2025-04-13 18:46:02','88','2025-04-12 18:46:02','2025-04-12 18:46:02'),(31,39,'user-freeze',0,'2025-07-17 16:57:17','测试','2025-07-16 16:57:17','2025-07-16 16:57:17');

/*Table structure for table `user_roles` */

DROP TABLE IF EXISTS `user_roles`;

CREATE TABLE `user_roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8;

/*Data for the table `user_roles` */

insert  into `user_roles`(`id`,`user_id`,`role_id`,`created_at`) values (28,17,1,'2025-04-10 17:06:02'),(53,29,2,'2025-04-10 20:43:42'),(61,37,3,'2025-04-11 17:33:05'),(62,38,3,'2025-04-11 17:56:29'),(63,39,3,'2025-07-16 16:50:27'),(64,40,3,'2025-07-16 17:09:18'),(65,41,3,'2025-09-01 14:14:22'),(66,42,3,'2025-09-04 16:27:53'),(67,43,3,'2025-09-05 15:51:17');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(200) NOT NULL,
  `email` varchar(100) NOT NULL,
  `google_secret_key` varchar(255) DEFAULT NULL,
  `last_login_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `registration_ip` varchar(50) DEFAULT NULL,
  `last_login_ip` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `username_2` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8;

/*Data for the table `users` */

insert  into `users`(`id`,`username`,`password`,`email`,`google_secret_key`,`last_login_at`,`created_at`,`registration_ip`,`last_login_ip`) values (17,'admin','AIyxhTzc7ICpm9yjy2UhLQ==','adminadmin@gmai.com','LXWXG2AZMLF5AW6VHR22IZLS73YJ5NIV','2025-10-17 15:22:11','2025-04-07 14:39:47',NULL,'127.0.0.1'),(29,'gggggggg','CBiQenq6U1rJz2YNeHpgnQ==','gggggggg@gmail.com',NULL,'2025-04-11 14:26:31','2025-04-10 18:12:05','127.0.0.1','127.0.0.1'),(37,'benxiaojie','PZaCLp7q9SBxiMyhCQG57Q==','benxiaojie@qq.com',NULL,'2025-09-05 15:07:04','2025-04-11 17:33:05',NULL,'127.0.0.1'),(38,'xiaodingdang','TkXfOO8UH9ala0UYhD9Psw==','xiaodingdang','FIEK6WUFZXBIAJ2MPDJZD3XR2D44JYBN','2025-04-11 18:31:15','2025-04-11 17:56:29','127.0.0.1','127.0.0.1'),(39,'admintest','bhqTeHmeP5SYmQ/2Pd1l7w==','admintest@gmail.com','EFJH45BCIKNQ2XRMD7QVK3UTXBFB74TI','2025-07-16 16:50:33','2025-07-16 16:50:27','127.0.0.1','127.0.0.1'),(40,'duobaoceshi','FZuCFcmo0rIET/B6wyOY6Q==','duobaoceshi@gmail.com','JFHQFSSJVCR6K4SDNNP635AAMVOMIG22','2025-07-16 17:09:22','2025-07-16 17:09:18','127.0.0.1','127.0.0.1'),(41,'ooasda','F9lloiakpz0tMYzyjeCN+Q==','ooasda@gmail.com',NULL,'2025-09-01 14:14:26','2025-09-01 14:14:22','127.0.0.1','127.0.0.1'),(42,'xiaodingdang2','CJQJIbAElQk2ArhRgLNy/w==','xiaodingdang@gmai.com','VB5JK7DPNRQDQAKAXWIVNQCKNGMSY7NT','2025-09-04 16:27:58','2025-09-04 16:27:53','127.0.0.1','127.0.0.1'),(43,'7777777777','7bjk7X9htJoR/bXwxOPxRw==','77777@gmail.com',NULL,'2025-09-05 18:15:02','2025-09-05 15:51:17','127.0.0.1','127.0.0.1');

/* Trigger structure for table `users` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `prevent_created_at_update` */$$

/*!50003 CREATE */  /*!50003 TRIGGER `prevent_created_at_update` BEFORE UPDATE ON `users` FOR EACH ROW BEGIN
    IF NEW.created_at <> OLD.created_at THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '字段 created_at 是只读字段，不能修改';
    END IF;
END */$$


DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
