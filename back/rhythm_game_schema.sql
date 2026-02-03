-- MySQL dump 10.13  Distrib 9.0.1, for macos15.1 (arm64)
--
-- Host: localhost    Database: rhythm_game
-- ------------------------------------------------------
-- Server version	8.0.29
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;
/*!50503 SET NAMES utf8mb4 */
;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */
;
/*!40103 SET TIME_ZONE='+00:00' */
;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */
;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */
;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */
;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */
;
--
-- Table structure for table `best_record`
--

DROP TABLE IF EXISTS `best_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `best_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲ID',
  `user_id` varchar(255) NOT NULL COMMENT '玩家ID',
  `score` int NOT NULL COMMENT '游玩分数',
  `pure` int NOT NULL COMMENT 'pure数',
  `far` int NOT NULL COMMENT 'far数',
  `lost` int NOT NULL COMMENT 'lost数',
  `combo` int NOT NULL COMMENT 'combo数',
  `potential` float NOT NULL COMMENT '单次成绩潜力值',
  `time` datetime NOT NULL COMMENT '游玩的时间',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 19 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `change_background_operation`
--

DROP TABLE IF EXISTS `change_background_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `change_background_operation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲ID',
  `start_timing` int NOT NULL COMMENT '操作开始时间',
  `background` varchar(255) NOT NULL COMMENT '存放需要更换的背景的路径',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 488 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `change_color_operation`
--

DROP TABLE IF EXISTS `change_color_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `change_color_operation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲id',
  `based_track` int NOT NULL COMMENT '所存在的轨道的编号',
  `start_timing` int NOT NULL COMMENT '操作开始时间',
  `end_timing` int NOT NULL COMMENT '操作结束时间',
  `start_r` int NOT NULL COMMENT '操作的起始R',
  `start_g` int NOT NULL COMMENT '操作的起始G',
  `start_b` int NOT NULL COMMENT '操作的起始B',
  `end_r` int NOT NULL COMMENT '操作的目的R',
  `end_g` int NOT NULL COMMENT '操作的目的G',
  `end_b` int NOT NULL COMMENT '操作的目的B',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 522 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `change_width_operation`
--

DROP TABLE IF EXISTS `change_width_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `change_width_operation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲id',
  `based_track` int NOT NULL COMMENT '所存在的轨道的编号',
  `start_timing` int NOT NULL COMMENT '操作开始时间',
  `end_timing` int NOT NULL COMMENT '操作结束时间',
  `start_width` float NOT NULL COMMENT '操作的起始宽度',
  `end_width` float NOT NULL COMMENT '操作的目的宽度',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 323 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `move_operation`
--

DROP TABLE IF EXISTS `move_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `move_operation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲ID',
  `based_track` int NOT NULL COMMENT '所存在的轨道的编号',
  `start_timing` int NOT NULL COMMENT '操作开始时间',
  `end_timing` int NOT NULL COMMENT '操作结束时间',
  `start_x` float NOT NULL COMMENT '操作的起始坐标',
  `end_x` float NOT NULL COMMENT '操作的目的坐标',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1224 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `note`
--

DROP TABLE IF EXISTS `note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `note` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲ID',
  `based_track` int NOT NULL COMMENT '音符所存在的轨道的编号',
  `note_type` int NOT NULL COMMENT '0-hit 1-hold 2-slide',
  `key_x` varchar(10) NOT NULL COMMENT '音符对应的判定按键',
  `timing` int NOT NULL COMMENT '音符击打的时间点',
  `end_timing` int DEFAULT NULL,
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 3741 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `recent_record`
--

DROP TABLE IF EXISTS `recent_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `recent_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲ID',
  `user_id` varchar(255) NOT NULL COMMENT '玩家ID',
  `score` int NOT NULL COMMENT '游玩分数',
  `pure` int NOT NULL COMMENT 'pure数',
  `far` int NOT NULL COMMENT 'far数',
  `lost` int NOT NULL COMMENT 'lost数',
  `combo` int NOT NULL COMMENT 'combo数',
  `potential` float NOT NULL COMMENT '单次成绩潜力值',
  `time` datetime NOT NULL COMMENT '游玩的时间',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 50 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `song`
--

DROP TABLE IF EXISTS `song`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `song` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '歌曲id',
  `song_name` varchar(255) NOT NULL COMMENT '歌曲名称',
  `song_writer` varchar(255) DEFAULT NULL COMMENT '歌曲作者',
  `chart_constant` float DEFAULT NULL COMMENT '谱面定数',
  `status` int NOT NULL COMMENT '0-保存但未发布 1-发布但不是认定谱面 2-认定谱面',
  `play_time` int NOT NULL COMMENT '所有玩家的游玩次数',
  `uploader_id` varchar(255) NOT NULL COMMENT '上传者信息',
  `song_cover` varchar(255) DEFAULT NULL COMMENT '歌曲封面图的路径',
  `default_background` varchar(255) DEFAULT NULL COMMENT '歌曲的默认背景',
  `notes_count` int NOT NULL COMMENT '歌曲的note总数',
  `loading_text` varchar(255) DEFAULT NULL COMMENT '加载文字',
  `loaded_text` varchar(255) DEFAULT NULL COMMENT '加载完的文字',
  `upload_date` datetime NOT NULL COMMENT '上传时间',
  `song_length` int DEFAULT NULL COMMENT '歌曲长度（毫秒）',
  `song_url` varchar(255) DEFAULT NULL COMMENT '歌曲音频的url',
  `b_p_m` float DEFAULT NULL COMMENT 'BPM',
  `first_beat_delay` int DEFAULT NULL COMMENT '第一拍延迟',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 14 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `track`
--

DROP TABLE IF EXISTS `track`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `track` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `song_id` int NOT NULL COMMENT '歌曲id',
  `type` int NOT NULL COMMENT 'o-虚轨 1-实轨',
  `key_x` varchar(10) NOT NULL COMMENT '轨道对应的按键',
  `start_timing` int NOT NULL COMMENT '轨道的出现时间',
  `end_timing` int NOT NULL COMMENT '轨道的消失时间',
  `position_x` float NOT NULL COMMENT '轨道横坐标',
  `width` float NOT NULL COMMENT '宽度',
  `r` int NOT NULL COMMENT 'R',
  `g` int NOT NULL COMMENT 'G',
  `b` int NOT NULL COMMENT 'B',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 382 DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `user` (
  `user_id` varchar(255) NOT NULL COMMENT '用户id',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `potential` float NOT NULL COMMENT '潜力值',
  `is_admin` int NOT NULL COMMENT '0-用户 1-管理员',
  `icon` varchar(255) DEFAULT NULL COMMENT '头像',
  `version` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */
;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */
;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */
;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */
;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */
;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */
;
-- Dump completed on 2026-01-30 13:54:02