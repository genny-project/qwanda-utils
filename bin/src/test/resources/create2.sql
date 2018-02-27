-- MySQL dump 10.13  Distrib 5.7.19, for osx10.12 (x86_64)
--
-- Host: 127.0.0.1    Database: gennydb
-- ------------------------------------------------------
-- Server version	5.5.5-10.2.9-MariaDB-10.2.9+maria~jessie

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `answer`
--

DROP TABLE IF EXISTS `answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `answer` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ask` tinyblob DEFAULT NULL,
  `askId` bigint(20) DEFAULT NULL,
  `attributecode` varchar(255) NOT NULL,
  `created` datetime DEFAULT NULL,
  `expired` bit(1) DEFAULT NULL,
  `refused` bit(1) DEFAULT NULL,
  `sourcecode` varchar(255) DEFAULT NULL,
  `targetcode` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `value` varchar(255) NOT NULL,
  `weight` double DEFAULT NULL,
  `attribute_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs5doqavr9yy08b1rqe4ndnt4n` (`attribute_id`),
  CONSTRAINT `FKs5doqavr9yy08b1rqe4ndnt4n` FOREIGN KEY (`attribute_id`) REFERENCES `attribute` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `answerlinks`
--

DROP TABLE IF EXISTS `answerlinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `answerlinks` (
  `askId` bigint(20) DEFAULT NULL,
  `attributeCode` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `expired` bit(1) DEFAULT NULL,
  `refused` bit(1) DEFAULT NULL,
  `sourceCode` varchar(255) DEFAULT NULL,
  `targetCode` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `valueBoolean` bit(1) DEFAULT NULL,
  `valueDateTime` datetime DEFAULT NULL,
  `valueDouble` double DEFAULT NULL,
  `valueInteger` int(11) DEFAULT NULL,
  `valueLong` bigint(20) DEFAULT NULL,
  `valueString` varchar(255) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `attribute_id` bigint(20) NOT NULL,
  `ask_id` bigint(20) NOT NULL,
  `TARGET_ID` bigint(20) NOT NULL,
  `SOURCE_ID` bigint(20) NOT NULL,
  `answerlist_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ask_id`,`attribute_id`,`SOURCE_ID`,`TARGET_ID`),
  KEY `IDX_MYIDX1` (`targetCode`,`sourceCode`,`attributeCode`),
  KEY `FKkogssga01sxqugj97qmo71ced` (`attribute_id`),
  KEY `FK9d7jg67ojpi3vtxsmt0iutr8b` (`TARGET_ID`),
  KEY `FKqd07ywjch02ubs8q5wmhn2jps` (`SOURCE_ID`),
  KEY `FK43yxp4wpvufotw5q45aqv34ar` (`answerlist_id`),
  CONSTRAINT `FK43yxp4wpvufotw5q45aqv34ar` FOREIGN KEY (`answerlist_id`) REFERENCES `ask` (`id`),
  CONSTRAINT `FK9d7jg67ojpi3vtxsmt0iutr8b` FOREIGN KEY (`TARGET_ID`) REFERENCES `baseentity` (`id`),
  CONSTRAINT `FKdtja5taqioea4i9pxof3aknce` FOREIGN KEY (`ask_id`) REFERENCES `ask` (`id`),
  CONSTRAINT `FKkogssga01sxqugj97qmo71ced` FOREIGN KEY (`attribute_id`) REFERENCES `attribute` (`id`),
  CONSTRAINT `FKqd07ywjch02ubs8q5wmhn2jps` FOREIGN KEY (`SOURCE_ID`) REFERENCES `baseentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ask`
--

DROP TABLE IF EXISTS `ask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ask` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `sourceCode` varchar(255) DEFAULT NULL,
  `targetCode` varchar(255) DEFAULT NULL,
  `question_id` bigint(20) NOT NULL,
  `source_id` bigint(20) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3gb9ght32cv27ajqs6yticbmi` (`question_id`),
  KEY `FKqmov3weo6lliv48d5o75xep85` (`source_id`),
  KEY `FKhrlbp0q8umpl991bll1jcbpwm` (`target_id`),
  CONSTRAINT `FK3gb9ght32cv27ajqs6yticbmi` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  CONSTRAINT `FKhrlbp0q8umpl991bll1jcbpwm` FOREIGN KEY (`target_id`) REFERENCES `baseentity` (`id`),
  CONSTRAINT `FKqmov3weo6lliv48d5o75xep85` FOREIGN KEY (`source_id`) REFERENCES `baseentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attribute`
--

DROP TABLE IF EXISTS `attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `className` varchar(255) DEFAULT NULL,
  `validation_list` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1774shfid1uaopl9tu8am19fq` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `baseentity`
--

DROP TABLE IF EXISTS `baseentity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `baseentity` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `baseentity_attribute`
--

DROP TABLE IF EXISTS `baseentity_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `baseentity_attribute` (
  `attributeCode` varchar(255) DEFAULT NULL,
  `baseEntityCode` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `valueDateTime` datetime DEFAULT NULL,
  `valueDouble` double DEFAULT NULL,
  `valueInteger` int(11) DEFAULT NULL,
  `valueLong` bigint(20) DEFAULT NULL,
  `valueString` varchar(255) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `ATTRIBUTE_ID` bigint(20) NOT NULL,
  `BASEENTITY_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ATTRIBUTE_ID`,`BASEENTITY_ID`),
  KEY `FKmqrqcxsqu49b0cliy2tymjoae` (`BASEENTITY_ID`),
  CONSTRAINT `FKaedpn6csuwk6uwm5kqh73tiwd` FOREIGN KEY (`ATTRIBUTE_ID`) REFERENCES `attribute` (`id`),
  CONSTRAINT `FKmqrqcxsqu49b0cliy2tymjoae` FOREIGN KEY (`BASEENTITY_ID`) REFERENCES `baseentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `baseentity_baseentity`
--

DROP TABLE IF EXISTS `baseentity_baseentity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `baseentity_baseentity` (
  `attributeCode` varchar(255) NOT NULL,
  `sourceCode` varchar(255) NOT NULL,
  `targetCode` varchar(255) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `valueDateTime` datetime DEFAULT NULL,
  `valueDouble` double DEFAULT NULL,
  `valueInteger` int(11) DEFAULT NULL,
  `valueLong` bigint(20) DEFAULT NULL,
  `valueString` varchar(255) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `linkAttribute_id` bigint(20) NOT NULL,
  `TARGET_ID` bigint(20) NOT NULL,
  `SOURCE_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`attributeCode`,`linkAttribute_id`,`SOURCE_ID`,`sourceCode`,`TARGET_ID`,`targetCode`),
  KEY `FK5u9vnk9yve4pah0a0j5j81ksr` (`linkAttribute_id`),
  KEY `FK8s8hil7w9rkq04o2d8gqsopso` (`TARGET_ID`),
  KEY `FK5wx1avqnhbiguv2x4a2350hkj` (`SOURCE_ID`),
  CONSTRAINT `FK5u9vnk9yve4pah0a0j5j81ksr` FOREIGN KEY (`linkAttribute_id`) REFERENCES `attribute` (`id`),
  CONSTRAINT `FK5wx1avqnhbiguv2x4a2350hkj` FOREIGN KEY (`SOURCE_ID`) REFERENCES `baseentity` (`id`),
  CONSTRAINT `FK8s8hil7w9rkq04o2d8gqsopso` FOREIGN KEY (`TARGET_ID`) REFERENCES `baseentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `context`
--

DROP TABLE IF EXISTS `context`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `context` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `attribute_id` bigint(20) NOT NULL,
  `list_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcm47nxemlmm987fu8a6iw64aa` (`attribute_id`),
  KEY `FKldpyd6jx52jnidtl6yx2oas8` (`list_id`),
  CONSTRAINT `FKcm47nxemlmm987fu8a6iw64aa` FOREIGN KEY (`attribute_id`) REFERENCES `baseentity` (`id`),
  CONSTRAINT `FKldpyd6jx52jnidtl6yx2oas8` FOREIGN KEY (`list_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gps`
--

DROP TABLE IF EXISTS `gps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gps` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bearing` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `latitude` varchar(255) NOT NULL,
  `longitude` varchar(255) NOT NULL,
  `targetcode` varchar(255) DEFAULT NULL,
  `targetid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `attribute_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjyuludyqfhb5b2aqe4d18wcbj` (`code`),
  KEY `FKc8891u9mg0doemnwfxov4e1w1` (`attribute_id`),
  CONSTRAINT `FKc8891u9mg0doemnwfxov4e1w1` FOREIGN KEY (`attribute_id`) REFERENCES `attribute` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question_question`
--

DROP TABLE IF EXISTS `question_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_question` (
  `created` datetime DEFAULT NULL,
  `mandatory` bit(1) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `CHILD_ID` bigint(20) NOT NULL,
  `PARENT_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`CHILD_ID`,`PARENT_ID`),
  KEY `FKjvkrgyj3mr3xnfh1goals7tum` (`PARENT_ID`),
  CONSTRAINT `FK5uh5d5394n7fnn2buqmnuejh` FOREIGN KEY (`CHILD_ID`) REFERENCES `question` (`id`),
  CONSTRAINT `FKjvkrgyj3mr3xnfh1goals7tum` FOREIGN KEY (`PARENT_ID`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule`
--

DROP TABLE IF EXISTS `rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `rule` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjtumkuxygwbgsuggjl85sip5x` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `validation`
--

DROP TABLE IF EXISTS `validation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `validation` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `regex` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh7lrvrsckuhda9bjbaf27qm8h` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-10-24 22:43:53
