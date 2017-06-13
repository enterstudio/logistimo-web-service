/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ACCOUNT` (
  `KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CID` bigint(20) DEFAULT NULL,
  `CNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `PY` decimal(16,4) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `VID` bigint(20) DEFAULT NULL,
  `VNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `Y` int(11) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `ACCOUNT#1` (`VID`,`Y`,`CNM`),
  KEY `ACCOUNT#3` (`CID`,`Y`,`VNM`),
  KEY `ACCOUNT#2` (`VID`,`Y`,`PY`),
  KEY `ACCOUNT#4` (`CID`,`Y`,`PY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ACTIVITY` (
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ACTION` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDATE` datetime(3) DEFAULT NULL,
  `DOMAINID` bigint(20) DEFAULT NULL,
  `FIELD` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MESSAGEID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NEWVALUE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OBJECTID` varchar(255) DEFAULT NULL,
  `OBJECTTYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PREVVALUE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TAG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ALOG` (
  `KY` bigint(20) NOT NULL AUTO_INCREMENT,
  `DID` bigint(20) DEFAULT NULL,
  `IP` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UA` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KY`),
  KEY `ALOG` (`DID`,`TY`,`T`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSET` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `CB` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `LID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `UON` datetime DEFAULT NULL,
  `UB` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `VID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` int(11) NOT NULL,
  `nsId` varchar(50) NOT NULL,
  `model` varchar(255) DEFAULT NULL,
  `yom` int(4) unsigned DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2027 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSETATTRIBUTE` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ATT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `VAL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ASSET_STATUS_ID` bigint(20) DEFAULT NULL,
  `ATTRIBUTES_INTEGER_IDX` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4569 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSETRELATION` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ASSETID` bigint(20) NOT NULL,
  `RELATEDASSETID` bigint(20) NOT NULL,
  `TYPE` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=589 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSETSTATUS` (
  `ID` bigint(20) NOT NULL,
  `ABNSTATUS` int(11) DEFAULT NULL,
  `ASSETID` bigint(20) DEFAULT NULL,
  `MPID` int(11) DEFAULT NULL,
  `SID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `STATUS` int(11) DEFAULT NULL,
  `TMP` float DEFAULT NULL,
  `TS` datetime DEFAULT NULL,
  `TYPE` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSET_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`),
  KEY `ASSET_DOMAINS_N49` (`ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSET_MAINTAINERS` (
  `ID` bigint(20) NOT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID`,`IDX`),
  KEY `ASSET_MAINTAINERS_N49` (`ID`),
  KEY `ASSET_MAINTAINERS_N50` (`USERID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSET_OWNERS` (
  `ID` bigint(20) NOT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID`,`IDX`),
  KEY `ASSET_OWNERS_N50` (`ID`),
  KEY `ASSET_OWNERS_N49` (`USERID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BBOARD` (
  `KY` bigint(20) NOT NULL AUTO_INCREMENT,
  `CTY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `EID` int(11) DEFAULT NULL,
  `EKY` bigint(20) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MS` varchar(2048) DEFAULT NULL,
  `MURL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `ST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BBOARD_DOMAINS` (
  `KY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KY_OID`,`IDX`),
  KEY `BBOARD_DOMAINS_N49` (`KY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BBOARD_TGS` (
  `KY_OID` bigint(20) NOT NULL,
  `TAG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KY_OID`,`IDX`),
  KEY `BBOARD_TGS_N49` (`KY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONFIG` (
  `KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CONF` longblob,
  `DID` bigint(20) DEFAULT NULL,
  `LASTUPD` datetime DEFAULT NULL,
  `PREVCONF` longblob,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONVERSATION` (
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CREATEDATE` datetime DEFAULT NULL,
  `DOMAINID` bigint(20) DEFAULT NULL,
  `OBJECTID` varchar(255) DEFAULT NULL,
  `OBJECTTYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UPDATEDATE` datetime DEFAULT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONVERSATIONTAG` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CONVERSATION_ID_OID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TAG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `CONVERSATIONTAG_N49` (`CONVERSATION_ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DASHBOARD` (
  `DBID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CONF` varchar(2048) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDON` datetime DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `DEF` bit(1) DEFAULT NULL,
  `DESC` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`DBID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEMANDITEM` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AST` int(11) DEFAULT NULL,
  `CR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DCT` decimal(16,4) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `MS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OID` bigint(20) DEFAULT NULL,
  `OQ` decimal(16,4) DEFAULT NULL,
  `ORDER_ID_OID` bigint(20) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `ST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TX` decimal(16,4) DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UP` decimal(16,4) DEFAULT NULL,
  `ROQ` decimal(16,4) DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `RSN` varchar(255) DEFAULT NULL,
  `TTO` bigint(20) DEFAULT NULL,
  `FQ` decimal(16,4) DEFAULT NULL,
  `DQ` decimal(16,4) DEFAULT NULL,
  `SQ` decimal(16,4) DEFAULT NULL,
  `ISQ` decimal(16,4) DEFAULT NULL,
  `SDRSN` varchar(255) DEFAULT NULL,
  `MST` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `DEMANDITEM_N49` (`ORDER_ID_OID`),
  KEY `DEMANDITEM#6` (`MID`,`T`),
  KEY `DEMANDITEM#4` (`KID`,`T`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEMANDITEMBATCH` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BEXP` datetime DEFAULT NULL,
  `BID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `BMFDT` datetime DEFAULT NULL,
  `BMFNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DMDITM_ID_OID` bigint(20) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `DEMANDITEMBATCH_N49` (`DMDITM_ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEMANDITEMBATCH_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`),
  KEY `DEMANDITEMBATCH_DOMAINS_N49` (`ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEMANDITEM_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`),
  KEY `DEMANDITEM_DOMAINS_N49` (`ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEMANDITEM_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `DEMANDITEM_KTAGS_N49` (`ID`),
  KEY `DEMANDITEM_KTAGS_N50` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEMANDITEM_MTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `DEMANDITEM_MTAGS_N49` (`ID`),
  KEY `DEMANDITEM_MTAGS_N50` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DOMAIN` (
  `DID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATEDON` datetime DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `HASCHILD` bit(1) DEFAULT NULL,
  `HASPARENT` bit(1) DEFAULT NULL,
  `ISACTIVE` bit(1) NOT NULL,
  `UB` varchar(255) DEFAULT NULL,
  `UO` datetime DEFAULT NULL,
  `NNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OWNERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RPTENABLED` bit(1) DEFAULT NULL,
  PRIMARY KEY (`DID`),
  KEY `DOMAIN#1` (`OWNERID`,`NNM`)
) ENGINE=InnoDB AUTO_INCREMENT=1343728 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DOMAINLINK` (
  `KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `LDID` bigint(20) DEFAULT NULL,
  `NDNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NLDNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TY` int(11) DEFAULT NULL,
  PRIMARY KEY (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DOMAINPERMISSION` (
  `DID` bigint(20) NOT NULL,
  `CC` bit(1) DEFAULT NULL,
  `CE` bit(1) DEFAULT NULL,
  `CM` bit(1) DEFAULT NULL,
  `CV` bit(1) DEFAULT NULL,
  `EA` bit(1) DEFAULT NULL,
  `EE` bit(1) DEFAULT NULL,
  `EGA` bit(1) DEFAULT NULL,
  `EGE` bit(1) DEFAULT NULL,
  `EGR` bit(1) DEFAULT NULL,
  `EGV` bit(1) DEFAULT NULL,
  `ERV` bit(1) DEFAULT NULL,
  `ERA` bit(1) DEFAULT NULL,
  `ERE` bit(1) DEFAULT NULL,
  `ERR` bit(1) DEFAULT NULL,
  `ER` bit(1) DEFAULT NULL,
  `EV` bit(1) DEFAULT NULL,
  `IA` bit(1) DEFAULT NULL,
  `IE` bit(1) DEFAULT NULL,
  `IR` bit(1) DEFAULT NULL,
  `IV` bit(1) DEFAULT NULL,
  `MA` bit(1) DEFAULT NULL,
  `ME` bit(1) DEFAULT NULL,
  `MR` bit(1) DEFAULT NULL,
  `MV` bit(1) DEFAULT NULL,
  `UA` bit(1) DEFAULT NULL,
  `UE` bit(1) DEFAULT NULL,
  `UR` bit(1) DEFAULT NULL,
  `UV` bit(1) DEFAULT NULL,
  `AV` bit(1) DEFAULT NULL,
  `AE` bit(1) DEFAULT NULL,
  `AA` bit(1) DEFAULT NULL,
  `AR` bit(1) DEFAULT NULL,
  PRIMARY KEY (`DID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DOWNLOADED` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `DD` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `L` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `V` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EVENT` (
  `KY` bigint(20) NOT NULL AUTO_INCREMENT,
  `ETAMILLIS` bigint(20) DEFAULT NULL,
  `ID` int(11) DEFAULT NULL,
  `MSG` varchar(400) DEFAULT NULL,
  `OID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OTY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PRMS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RT` bit(1) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `ST` int(11) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `UIDS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EVENT_DOMAINS` (
  `KY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KY_OID`,`IDX`),
  KEY `EVENT_DOMAINS_N49` (`KY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HANDLINGUNIT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CB` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DESCRIPTION` varchar(400) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LASTUPDATED` datetime DEFAULT NULL,
  `NNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `QUANTITY` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `TIMESTAMP` datetime DEFAULT NULL,
  `UB` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `VOLUME` decimal(16,4) DEFAULT NULL,
  `WEIGHT` decimal(16,4) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HANDLINGUNITCONTENT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CNTID` bigint(20) DEFAULT NULL,
  `HU_ID_OID` bigint(20) DEFAULT NULL,
  `QUANTITY` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `TY` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HUC_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HU_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVALLOCATION` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `TYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TYPEID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UON` datetime DEFAULT NULL,
  `MST` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVELOGS_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVELOGS_KTAGS_N49` (`KEY`),
  KEY `INVELOGS_KTAGS_N50` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVELOGS_MTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVELOGS_MTAGS_N49` (`KEY`),
  KEY `INVELOGS_MTAGS_N50` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVENTORYMINMAXLOG` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CR` decimal(16,4) DEFAULT NULL,
  `INVID` bigint(20) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `MAX` decimal(16,4) DEFAULT NULL,
  `MIN` decimal(16,4) DEFAULT NULL,
  `SRC` int(11) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TYPE` int(11) DEFAULT NULL,
  `USER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MAXDUR` decimal(16,4) DEFAULT NULL,
  `MINDUR` decimal(16,4) DEFAULT NULL,
  `freq` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `INVT` (`INVID`,`T`)
) ENGINE=InnoDB AUTO_INCREMENT=23281 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVENTORYPREDICTIONSLOG` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `CR` float DEFAULT NULL,
  `IKEY` bigint(20) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `OP` decimal(16,4) DEFAULT NULL,
  `PDOS` decimal(16,4) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  PRIMARY KEY (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRY` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `Q` decimal(16,4) DEFAULT NULL,
  `B` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CRD` decimal(16,4) DEFAULT NULL,
  `CRMNL` decimal(16,4) DEFAULT NULL,
  `IMDL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `KNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LDTDMD` decimal(16,4) DEFAULT NULL,
  `LDTM` decimal(16,4) DEFAULT NULL,
  `LSEV` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `MAX` decimal(16,4) DEFAULT NULL,
  `MNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OMSG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ORDP` decimal(16,4) DEFAULT NULL,
  `PRC` decimal(16,4) DEFAULT NULL,
  `REORD` decimal(16,4) DEFAULT NULL,
  `RVPDMD` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SFSTK` decimal(16,4) DEFAULT NULL,
  `STDV` decimal(16,4) DEFAULT NULL,
  `STK` decimal(16,4) DEFAULT NULL,
  `SVCLVL` float DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TDQ` datetime DEFAULT NULL,
  `TPS` datetime DEFAULT NULL,
  `TMAX` float DEFAULT NULL,
  `TMIN` float DEFAULT NULL,
  `TX` decimal(16,4) DEFAULT NULL,
  `MAXT` datetime DEFAULT NULL,
  `REORDT` datetime DEFAULT NULL,
  `CRMNLT` datetime DEFAULT NULL,
  `PRCT` datetime DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `UB` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `SID` int(11) DEFAULT NULL,
  `PDOS` decimal(16,4) DEFAULT NULL,
  `MAXDUR` decimal(16,4) DEFAULT '0.0000',
  `MINDUR` decimal(16,4) DEFAULT '0.0000',
  `TSTK` decimal(16,4) DEFAULT NULL,
  `ATPSTK` decimal(16,4) DEFAULT NULL,
  `ASTK` decimal(16,4) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `INVNTRY#4` (`KID`,`MID`,`KNM`),
  KEY `INVNTRY#3` (`MID`,`KNM`),
  KEY `INVNTRY#1` (`KID`,`MNM`)
) ENGINE=InnoDB AUTO_INCREMENT=390177 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYBATCH` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `BEXP` datetime DEFAULT NULL,
  `BID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `BMFDT` datetime DEFAULT NULL,
  `BMFNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `KNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `VLD` bit(1) DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `ASTK` decimal(16,4) DEFAULT NULL,
  `ATPSTK` decimal(16,4) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `INVNTRYBATCH#3` (`MID`,`KID`,`VLD`,`BEXP`),
  KEY `INVNTRYBATCH#1` (`MID`,`VLD`,`BID`,`BEXP`),
  KEY `INVNTRYBATCH#2` (`MID`,`VLD`,`BEXP`)
) ENGINE=InnoDB AUTO_INCREMENT=3287 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYBATCH_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `INVNTRYBATCH_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYBATCH_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVNTRYBATCH_KTAGS_N50` (`KEY`),
  KEY `INVNTRYBATCH_KTAGS_N49` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYBATCH_MTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVNTRYBATCH_MTAGS_N49` (`ID`),
  KEY `INVNTRYBATCH_MTAGS_N50` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYEVENTLOG_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `INVNTRYEVENTLOG_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYEVNTLOG` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `AST` int(11) DEFAULT NULL,
  `DR` bigint(20) DEFAULT NULL,
  `ED` datetime DEFAULT NULL,
  `INVID` bigint(20) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `SD` datetime DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `TY` int(11) DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `INVNTRYEVNTLOG#5` (`KID`,`MID`,`TY`,`SD`),
  KEY `INVNTRYEVNTLOG#2` (`KID`,`TY`,`ED`,`SD`)
) ENGINE=InnoDB AUTO_INCREMENT=35396 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYITEM` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `AST` int(11) DEFAULT NULL,
  `ASTT` datetime DEFAULT NULL,
  `BST` int(11) DEFAULT NULL,
  `BSTT` datetime DEFAULT NULL,
  `BV` double DEFAULT NULL,
  `CNST` int(11) DEFAULT NULL,
  `CNSTT` datetime DEFAULT NULL,
  `CONFIGST` int(11) DEFAULT NULL,
  `DST` int(11) DEFAULT NULL,
  `DSTT` datetime DEFAULT NULL,
  `INVNTRY_KEY_OID` bigint(20) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `KNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KCITY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KCNTRY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KDIS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `MNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NABS` int(11) DEFAULT NULL,
  `NHGH` int(11) DEFAULT NULL,
  `NLOW` int(11) DEFAULT NULL,
  `RGST` int(11) DEFAULT NULL,
  `SST` int(11) DEFAULT NULL,
  `SSTT` datetime DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SIDNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ST` int(11) DEFAULT NULL,
  `STT` datetime DEFAULT NULL,
  `SVID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SYSERRS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TMAX` float DEFAULT NULL,
  `TMIN` float DEFAULT NULL,
  `TMP` float DEFAULT NULL,
  `TMPSTATUS` int(11) DEFAULT NULL,
  `TRID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TUT` datetime DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `INVNTRYITEM_N49` (`INVNTRY_KEY_OID`),
  KEY `INVNTRYITEM#6` (`SID`,`ST`,`STT`),
  KEY `INVNTRYITEM#7` (`ST`,`STT`),
  KEY `INVNTRYITEM#8` (`SVID`,`ST`,`STT`),
  KEY `INVNTRYITEM#16` (`KST`,`ST`,`STT`),
  KEY `INVNTRYITEM#9` (`KID`,`ST`,`STT`),
  KEY `INVNTRYITEM#12` (`BST`,`CNST`),
  KEY `INVNTRYITEM#18` (`CNST`),
  KEY `INVNTRYITEM#17` (`KDIS`,`ST`,`STT`),
  CONSTRAINT `INVNTRYITEM_FK1` FOREIGN KEY (`INVNTRY_KEY_OID`) REFERENCES `INVNTRY` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYITEM_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `INVNTRYITEM_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYITEM_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVNTRYITEM_KTAGS_N49` (`ID`),
  KEY `INVNTRYITEM_KTAGS_N50` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYITEM_MTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVNTRYITEM_MTAGS_N49` (`ID`),
  KEY `INVNTRYITEM_MTAGS_N50` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYLOG` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `BID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `BS` float DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `S` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `INVNTRYLOGINDEX` (`KID`,`MID`,`T`)
) ENGINE=InnoDB AUTO_INCREMENT=475260 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRYLOG_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `INVNTRYLOG_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRY_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `INVNTRY_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRY_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVNTRY_KTAGS_N50` (`ID`),
  KEY `INVNTRY_KTAGS_N49` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVNTRY_MTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `INVNTRY_MTAGS_N50` (`ID`),
  KEY `INVNTRY_MTAGS_N49` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INV_ALLOC_TAG` (
  `ID_OID` bigint(20) NOT NULL,
  `TAG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `JOBSTATUS` (
  `JID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DESC` varchar(255) DEFAULT NULL,
  `DID` bigint(20) NOT NULL,
  `TY` varchar(255) NOT NULL,
  `STY` varchar(255) DEFAULT NULL,
  `ST` int(11) NOT NULL,
  `RSN` varchar(255) DEFAULT NULL,
  `STT` datetime(3) NOT NULL,
  `UT` datetime(3) DEFAULT NULL,
  `NR` int(11) DEFAULT NULL,
  `ON` varchar(255) DEFAULT NULL,
  `OFL` varchar(255) DEFAULT NULL,
  `CRBY` varchar(255) DEFAULT NULL,
  `META` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`JID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KIOSK` (
  `KIOSKID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BE` bit(1) DEFAULT NULL,
  `CID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CITY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `COUNTRY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CURRENCY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DISTRICT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `GACC` double DEFAULT NULL,
  `GERR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `INVMODEL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LASTUPDATED` datetime DEFAULT NULL,
  `LATITUDE` double NOT NULL,
  `LONGITUDE` double NOT NULL,
  `NNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NLNK` int(11) DEFAULT NULL,
  `OPTIMIZATIONON` bit(1) NOT NULL,
  `ORDERINGMODE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PINCODE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PRMS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RGDBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RTE` bit(1) DEFAULT NULL,
  `RTEV` bit(1) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SERVICELEVEL` int(11) NOT NULL,
  `STATE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `STREET` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TALUK` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TAX` decimal(16,4) DEFAULT NULL,
  `TIMESTAMP` datetime DEFAULT NULL,
  `TXID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UB` varchar(255) DEFAULT NULL,
  `VERTICAL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `CPERM` int(1) DEFAULT '0',
  `VPERM` int(1) DEFAULT '0',
  `IAT` datetime DEFAULT NULL,
  `OAT` datetime DEFAULT NULL,
  `AT` datetime DEFAULT NULL,
  PRIMARY KEY (`KIOSKID`)
) ENGINE=InnoDB AUTO_INCREMENT=1344727 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KIOSKLINK` (
  `KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDON` datetime DEFAULT NULL,
  `CRL` decimal(16,4) DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KIOSKID` bigint(20) DEFAULT NULL,
  `LINKTYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LINKEDKIOSKID` bigint(20) DEFAULT NULL,
  `LKNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RI` int(11) DEFAULT NULL,
  `RTG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `KIOSKLINK#2` (`KIOSKID`,`LINKTYPE`,`RI`),
  KEY `KIOSKLINK#3` (`KIOSKID`,`LINKTYPE`,`RTG`,`RI`),
  KEY `KIOSKLINK#1` (`KIOSKID`,`LINKTYPE`,`LKNM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KIOSKLINK_DOMAINS` (
  `KEY_OID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `KIOSKLINK_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KIOSKTOPOOLGROUP` (
  `KIOSKTOPOOLGROUPID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DID` bigint(20) DEFAULT NULL,
  `KIOSKID` bigint(20) DEFAULT NULL,
  `POOLGROUPID` bigint(20) DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`KIOSKTOPOOLGROUPID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KIOSK_DOMAINS` (
  `KIOSKID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KIOSKID_OID`,`IDX`),
  KEY `KIOSK_DOMAINS_N49` (`KIOSKID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KIOSK_TAGS` (
  `KIOSKID` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KIOSKID`,`IDX`),
  KEY `KIOSK_TAGS_N49` (`ID`),
  KEY `KIOSK_TAGS_N50` (`KIOSKID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MATERIAL` (
  `MATERIALID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CB` varchar(255) DEFAULT NULL,
  `UB` varchar(255) DEFAULT NULL,
  `BM` bit(1) DEFAULT NULL,
  `CID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CURRENCY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DISPINFO` bit(1) DEFAULT NULL,
  `DTY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IDTYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IDVALUE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IMAGEPATH` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `INFO` varchar(400) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LASTUPDATED` datetime DEFAULT NULL,
  `MBM` bit(1) DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RETAILPRICE` decimal(16,4) DEFAULT NULL,
  `RETAILERPRICE` decimal(16,4) DEFAULT NULL,
  `SALEPRICE` decimal(16,4) DEFAULT NULL,
  `SCODE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SEASONAL` bit(1) DEFAULT NULL,
  `SNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TIMESTAMP` datetime DEFAULT NULL,
  `TMAX` float DEFAULT NULL,
  `TMIN` float DEFAULT NULL,
  `TMP` bit(1) DEFAULT NULL,
  `UNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `VERTICAL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MATERIALID`)
) ENGINE=InnoDB AUTO_INCREMENT=3345736 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MATERIAL_DOMAINS` (
  `MATERIALID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`MATERIALID_OID`,`IDX`),
  KEY `MATERIAL_DOMAINS_N49` (`MATERIALID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MATERIAL_TAGS` (
  `MATERIALID` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`MATERIALID`,`IDX`),
  KEY `MATERIAL_TAGS_N49` (`ID`),
  KEY `MATERIAL_TAGS_N50` (`MATERIALID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MEDIA` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DOMAINKEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MEDIATYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAMESPACEID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SERVINGURL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UPLOADTIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MESSAGE` (
  `MESSAGEID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CONVERSATIONID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDATE` datetime DEFAULT NULL,
  `MESSAGE` varchar(2048) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UPDATEDATE` datetime DEFAULT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`MESSAGEID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MESSAGELOG` (
  `KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ADR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `AST` int(11) DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `DT` datetime DEFAULT NULL,
  `MSG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SUID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NOTIF` int(11) DEFAULT '0',
  `EVENTTYPE` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `MESSAGELOG#1` (`SUID`,`T`),
  KEY `MESSAGELOG#2` (`DID`,`T`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MNLTRANSACTION` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `CCR` decimal(16,4) DEFAULT NULL,
  `COQ` decimal(16,4) DEFAULT NULL,
  `DQ` decimal(16,4) DEFAULT NULL,
  `IQ` decimal(16,4) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `MCR` decimal(16,4) DEFAULT NULL,
  `MOQ` decimal(16,4) DEFAULT NULL,
  `OLDTGS` mediumblob,
  `OS` decimal(16,4) DEFAULT NULL,
  `RP` datetime DEFAULT NULL,
  `RQ` decimal(16,4) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SOD` int(11) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `VID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `MNLTRANSACTION#2` (`KID`,`RP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MNLTRANSACTION_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `MNLTRANSACTION_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MNLTRANSACTION_OTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `MNLTRANSACTION_OTAGS_N50` (`KEY`),
  KEY `MNLTRANSACTION_OTAGS_N49` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MULTIPARTMSG` (
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CCODE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OPTIMIZERLOG` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `DID` bigint(20) DEFAULT NULL,
  `E` datetime DEFAULT NULL,
  `INIDS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MSG` varchar(2048) DEFAULT NULL,
  `N` int(11) NOT NULL,
  `S` datetime DEFAULT NULL,
  `TY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `OPTIMIZERLOG` (`TY`,`S`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ORDER` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AST` int(11) DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `CFTE` datetime DEFAULT NULL,
  `CFTS` datetime DEFAULT NULL,
  `CR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DLT` bigint(20) DEFAULT NULL,
  `DSC` decimal(16,4) DEFAULT NULL,
  `DTYP` int(11) DEFAULT NULL,
  `EFTS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `FLDS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `GACC` double DEFAULT NULL,
  `GERR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `LN` double DEFAULT NULL,
  `LT` double DEFAULT NULL,
  `MS` varchar(2048) DEFAULT NULL,
  `PD` decimal(16,4) DEFAULT NULL,
  `PDS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PH` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `POPT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PT` bigint(20) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SKID` bigint(20) DEFAULT NULL,
  `ST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `STON` datetime DEFAULT NULL,
  `TP` decimal(16,4) DEFAULT NULL,
  `TX` decimal(16,4) DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UON` datetime DEFAULT NULL,
  `UUID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `RID` varchar(50) DEFAULT NULL,
  `OTY` int(1) DEFAULT '1',
  `EDD` datetime DEFAULT NULL,
  `EAD` datetime DEFAULT NULL,
  `NOI` int(11) DEFAULT NULL,
  `CDRSN` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `ORDER#8` (`SKID`,`ST`,`CON`),
  KEY `ORDER#10` (`UID`,`CON`),
  KEY `ORDER#7` (`SKID`,`CON`),
  KEY `ORDER#6` (`KID`,`ST`,`CON`),
  KEY `ORDER#5` (`KID`,`CON`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ORDER_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`),
  KEY `ORDER_DOMAINS_N49` (`ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ORDER_JOB_STATUS` (
  `ID` bigint(20) NOT NULL,
  `STATUS` varchar(20) DEFAULT NULL,
  `SDATE` datetime DEFAULT NULL,
  `REMARKS` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ORDER_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `ORDER_KTAGS_N50` (`ID`),
  KEY `ORDER_KTAGS_N49` (`KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ORDER_TAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `ORDER_TAGS_N49` (`KEY`),
  KEY `ORDER_TAGS_N50` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PAYMENT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AMT` bigint(20) NOT NULL,
  `CON` datetime DEFAULT NULL,
  `CUR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `FRM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RID` bigint(20) NOT NULL,
  `ST` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TO` mediumblob,
  `UON` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `POOLGROUP` (
  `GROUPID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CB` varchar(255) DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OWNERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TIMESTAMP` datetime DEFAULT NULL,
  `UB` varchar(255) DEFAULT NULL,
  `UO` datetime DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`GROUPID`),
  KEY `POOLGROUP` (`DID`,`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHARDEDCOUNTER` (
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `C` int(11) DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `NM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SHNO` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `SHARDEDCOUNTER` (`DID`,`NM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHIPMENT` (
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `AFD` datetime DEFAULT NULL,
  `CBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `EAD` datetime DEFAULT NULL,
  `GEOACC` double DEFAULT NULL,
  `GEOERR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `LN` double DEFAULT NULL,
  `LT` double DEFAULT NULL,
  `NOI` int(11) DEFAULT NULL,
  `ORDERID` bigint(20) DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SKID` bigint(20) DEFAULT NULL,
  `STATUS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TRACKINGID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TRANSPORTER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RSN` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UON` datetime DEFAULT NULL,
  `CDRSN` varchar(255) DEFAULT NULL,
  `PS` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHIPMENTITEM` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `DQ` decimal(16,4) DEFAULT NULL,
  `FQ` decimal(16,4) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `RSN` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UON` datetime DEFAULT NULL,
  `FDRSN` varchar(255) DEFAULT NULL,
  `FMST` varchar(255) DEFAULT NULL,
  `SMST` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHIPMENTITEMBATCH` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BEXP` datetime DEFAULT NULL,
  `BID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `BMFDT` datetime DEFAULT NULL,
  `BMFNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CON` datetime DEFAULT NULL,
  `DQ` decimal(16,4) DEFAULT NULL,
  `FQ` decimal(16,4) DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `RSN` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SIID` bigint(20) DEFAULT NULL,
  `UBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UON` datetime DEFAULT NULL,
  `FMST` varchar(255) DEFAULT NULL,
  `SMST` varchar(255) DEFAULT NULL,
  `FDRSN` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHIPMENTITEMBATCH_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHIPMENTITEM_DOMAINS` (
  `ID_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHIPMENT_DOMAINS` (
  `ID_OID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`ID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TAG` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TYPE` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=367 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TASK` (
  `TASKID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATEDBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDON` datetime DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `DURATION` int(11) NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `QUEUE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `REASON` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RETRIES` int(11) NOT NULL,
  `SCHEDULETIME` datetime DEFAULT NULL,
  `STATUS` int(11) NOT NULL,
  `UPDATEDON` datetime DEFAULT NULL,
  PRIMARY KEY (`TASKID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TASKLOG` (
  `KY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `D` datetime DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `VERSION` bigint(20) NOT NULL,
  PRIMARY KEY (`KY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRANSACTION` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `AST` int(11) DEFAULT NULL,
  `BEXP` datetime DEFAULT NULL,
  `BID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `BMFDT` datetime DEFAULT NULL,
  `BMFNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CS` decimal(16,4) DEFAULT NULL,
  `CSB` decimal(16,4) DEFAULT NULL,
  `DUID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `GACC` double DEFAULT NULL,
  `GERR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `KID` bigint(20) DEFAULT NULL,
  `LKID` bigint(20) DEFAULT NULL,
  `LN` double DEFAULT NULL,
  `LT` double DEFAULT NULL,
  `LTID` bigint(20) DEFAULT NULL,
  `MID` bigint(20) DEFAULT NULL,
  `OS` decimal(16,4) DEFAULT NULL,
  `OSB` decimal(16,4) DEFAULT NULL,
  `Q` decimal(16,4) DEFAULT NULL,
  `RS` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `SDF` decimal(16,4) DEFAULT NULL,
  `T` datetime(3) DEFAULT NULL,
  `TID` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TOT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SRC` int(11) NOT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `MST` varchar(255) DEFAULT NULL,
  `ALT` double DEFAULT NULL,
  `ATD` date DEFAULT NULL,
  `EATD` tinyint(1) DEFAULT NULL,
  `ET` DATETIME(3) NULL,
  PRIMARY KEY (`KEY`),
  KEY `TRANSACTION#10` (`KID`,`TYPE`,`T`),
  KEY `TRANSACTION#8` (`KID`,`T`),
  KEY `TRANSACTION#12` (`MID`,`T`),
  KEY `TRANSACTION#13` (`MID`,`TYPE`,`T`),
  KEY `TRANSACTION#14` (`UID`,`T`),
  KEY `TRANSACTION#15` (`KID`,`LKID`,`T`),
  KEY `TRANSACTION#1` (`KID`,`MID`,`TYPE`,`T`),
  KEY `TRANSACTION#17` (`KID`,`LKID`,`TYPE`,`T`),
  KEY `trans_reason` (`RS`)
) ENGINE=InnoDB AUTO_INCREMENT=1545921 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRANSACTION_DOMAINS` (
  `KEY_OID` bigint(20) NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY_OID`,`IDX`),
  KEY `TRANSACTION_DOMAINS_N49` (`KEY_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRANSACTION_KTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `TRANSACTION_KTAGS_N49` (`KEY`),
  KEY `TRANSACTION_KTAGS_N50` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRANSACTION_MTAGS` (
  `KEY` bigint(20) NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`KEY`,`IDX`),
  KEY `TRANSACTION_MTAGS_N49` (`KEY`),
  KEY `TRANSACTION_MTAGS_N50` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRANSPORTER` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `DID` bigint(20) DEFAULT NULL,
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `OID` bigint(20) DEFAULT NULL,
  `ORDER_ID_OID` bigint(20) DEFAULT NULL,
  `TY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `TRANSPORTER_N49` (`ORDER_ID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UPLOADED` (
  `ID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `BLKEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `DESC` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `FN` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `JID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `L` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ST` int(11) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `TY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `V` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `UPLOADED` (`FN`,`L`,`T`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UPLOADEDMSGLOG` (
  `KEY` bigint(20) NOT NULL AUTO_INCREMENT,
  `DID` bigint(20) DEFAULT NULL,
  `MSG` varchar(2048) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `UPLOADEDID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`KEY`),
  KEY `UPLOADEDMSGLOG` (`T`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USERACCOUNT` (
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `AGE` int(11) NOT NULL,
  `AGETYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `AST` int(11) DEFAULT NULL,
  `BIRTHDATE` datetime DEFAULT NULL,
  `CID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CITY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `COUNTRY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SDID` bigint(20) DEFAULT NULL,
  `DISTRICT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `EMAIL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ENCODEDPASSWORD` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `FIRSTNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `GENDER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IMEI` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `IPADDR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ISENABLED` bit(1) NOT NULL,
  `LANDPHONENUMBER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LANGUAGE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LASTLOGIN` datetime DEFAULT NULL,
  `LASTNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `LRE` datetime DEFAULT NULL,
  `MEMBERSINCE` datetime DEFAULT NULL,
  `MOBILEPHONENUMBER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NNAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PHONEBRAND` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PHONEMODELNUMBER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PHONESERVICEPROVIDER` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PINCODE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `PKID` bigint(20) DEFAULT NULL,
  `PREVUSRAGNT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `REGISTEREDBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ROLE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RTE` bit(1) DEFAULT NULL,
  `SIMID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `STATE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `STREET` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TALUK` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TIMEZONE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `UIPREF` bit(1) DEFAULT NULL,
  `UB` varchar(255) DEFAULT NULL,
  `UO` datetime DEFAULT NULL,
  `USRAGNT` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `V` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  `LGR` tinyint(4) DEFAULT NULL,
  `ATEXP` int(11) NOT NULL,
  `PER` varchar(5) NOT NULL DEFAULT 'd',
  `LGSRC` int(11) DEFAULT NULL,
  PRIMARY KEY (`USERID`),
  KEY `USERACCOUNT#4` (`SDID`,`LASTLOGIN`),
  KEY `USERACCOUNT#3` (`SDID`,`ROLE`,`NNAME`),
  KEY `USERACCOUNT#2` (`SDID`,`REGISTEREDBY`,`NNAME`),
  KEY `USERACCOUNT#1` (`SDID`,`NNAME`),
  KEY `USERACCOUNT#5` (`SDID`,`CID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USERACCOUNT_DOMAINS` (
  `USERID_OID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`USERID_OID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USERLOGINHISTORY` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USERID` varchar(50) DEFAULT NULL,
  `T` datetime DEFAULT NULL,
  `LGSRC` int(11) DEFAULT NULL,
  `USRAGNT` varchar(255) DEFAULT NULL,
  `IPADDR` varchar(255) DEFAULT NULL,
  `V` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=54200 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USERTOKEN` (
  `TOKEN` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `DOMAINID` bigint(20) DEFAULT NULL,
  `EXPIRES` datetime DEFAULT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`TOKEN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USERTOKIOSK` (
  `USERTOKIOSKID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DID` bigint(20) DEFAULT NULL,
  `KIOSKID` bigint(20) DEFAULT NULL,
  `KNM` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `RI` int(11) DEFAULT NULL,
  `TG` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ARCAT` datetime DEFAULT NULL,
  `ARCBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`USERTOKIOSKID`),
  KEY `USERRI` (`USERID`,`RI`),
  KEY `USERTAG` (`USERID`,`TG`,`RI`),
  KEY `USERKIOSKNAME` (`USERID`,`KNM`)
) ENGINE=InnoDB AUTO_INCREMENT=23067 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_ACC_DOMAINS` (
  `USERID_OID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `DOMAIN_ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`USERID_OID`,`IDX`),
  KEY `USER_ACC_DOMAINS_N49` (`USERID_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_TAGS` (
  `USERID` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ID` bigint(20) DEFAULT NULL,
  `IDX` int(11) NOT NULL,
  PRIMARY KEY (`USERID`,`IDX`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WIDGET` (
  `WID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AGGR` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `AGGRTY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDBY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CREATEDON` datetime DEFAULT NULL,
  `DID` bigint(20) DEFAULT NULL,
  `DESC` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `EXPENABLED` bit(1) DEFAULT NULL,
  `FREQ` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `NOP` int(11) NOT NULL,
  `SHOWLEG` bit(1) DEFAULT NULL,
  `SUBTITLE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TITLE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TYPE` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `YLABEL` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`WID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `charan` (
  `did` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
