/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

const args = process.argv.splice(2,2);

//-------------------User configuration-------------------------//

const userCollection = "user";
var user = {
    "userId": "logistimo",
    "serverConfigs": [
        {
            "_id": "id1",
            "name": "mysqlLogistimoServer",
            "hosts": [
                "localhost"
            ],
            "port": "3306",
            "username": "logistimo",
            "password": "logistimo",
            "schema": "logistimo",
            "type": "mysql"
        },
        {
            "_id": "id2",
            "name": "mysqlTemperatureService",
            "hosts": [
                "localhost"
            ],
            "port": "3306",
            "username": "logistimo",
            "password": "logistimo",
            "schema": "logistimo_tms",
            "type": "mysql"
        }
    ]
};

//---------------------Query configuration------------------------//

const defaultUser = 'logistimo';
const mysqlServerId = 'id1';
const mysqlAssetServerId = 'id2';
const dbname = "callisto";
const queryCollection = "queryText";

var MongoClient = require('mongodb').MongoClient;
var assert = require('assert');
var url = 'mongodb://localhost:27017/' + dbname;

MongoClient.connect(url, function (err, db) {
    assert.equal(null, err);
    dropCollection(db, function () {
        insertMysqlQueries(db, function () {
            db.close();
        })
    });
});

var dropCollection = function (db, callback) {
    var collection = db.collection(queryCollection);
    collection.drop(function (err, reply) {
        db.listCollections().toArray(function (err, result) {
            var found = false;
            result.forEach(function (document) {
                if (document.name == queryCollection) {
                    found = true;
                    console.log("Error in deleting queryText collection");
                    return;
                }
            });
            console.log("Existing queryText collection deleted successfully");
            callback();
        });
    })
};

var insertMysqlQueries = function(db, callback) {
    var d= [];

    // KTAG, MTAG, UTAG queries
    var TYPES = {
        "KTAG" : "0",
        "MTAG" : "1",
        "UTAG" : "4"
    };
    for(var type in TYPES) {
        var q = {};
        q.userId = defaultUser;
        q.queryId = type + "_ID";
        q.query = "select ID from TAG where NAME IN(TOKEN_" + type + ") AND TYPE=" + TYPES[type];
        q.serverId = mysqlServerId;
        d.push(q);
    }

    var qdidkid = {};
    qdidkid.userId = defaultUser;
    qdidkid.queryId = "DID_KID_QUERY";
    qdidkid.query = "SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID = TOKEN_DID";
    qdidkid.serverId = mysqlServerId;
    d.push(qdidkid);

    var qktag = {};
    qktag.userId = defaultUser;
    qktag.queryId = "KTAG_QUERY";
    qktag.query = "SELECT KIOSKID FROM KIOSK_TAGS WHERE ID IN ($$enclosecsv(KTAG_ID)$$)";
    qktag.serverId = mysqlServerId;
    d.push(qktag);

    var qstate = {};
    qstate.userId = defaultUser;
    qstate.queryId = "ST_QUERY";
    qstate.query = "SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST)";
    qstate.serverId = mysqlServerId;
    d.push(qstate);

    var qdistrict = {};
    qdistrict.userId = defaultUser;
    qdistrict.queryId = "DIS_QUERY";
    qdistrict.query = "SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS)";
    qdistrict.serverId = mysqlServerId;
    d.push(qdistrict);

    var qtaluk = {};
    qtaluk.userId = defaultUser;
    qtaluk.queryId = "TALUK_QUERY";
    qtaluk.query = "SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS) AND TALUK IN (TOKEN_TALUK)";
    qtaluk.serverId = mysqlServerId;
    d.push(qtaluk);

    var qcity = {};
    qcity.userId = defaultUser;
    qcity.queryId = "CITY_QUERY";
    qcity.query = "SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND CITY IN (TOKEN_CITY)";
    qcity.serverId = mysqlServerId;
    d.push(qcity);

    var reports = ["ITC", "IR", "IAS", "AAS"];
    var reportsType = {ITC : "INV", IR : "INV", IAS : "INV", AAS : "AS"}; // INV: Inventory, AS: Assets
    var periods = ["MONTH","DAY"];
    var reportFilters = {
        INV:["DID",
            "DID_MID",
            "DID_KID",
            "DID_MID_KID",
            "DID_MTAG",
            "DID_KID_MTAG",
            "DID_KTAG",
            "DID_MID_KTAG",
            "DID_KTAG_MTAG",
            "DID_CN_ST",
            "DID_CN_ST_DIS",
            "DID_CN_ST_DIS_TALUK",
            "DID_CN_ST_CITY",
            "DID_MID_CN_ST",
            "DID_MID_CN_ST_DIS",
            "DID_MID_CN_ST_DIS_TALUK",
            "DID_MID_CN_ST_CITY",
            "DID_KTAG_CN_ST",
            "DID_KTAG_CN_ST_DIS",
            "DID_KTAG_CN_ST_DIS_TALUK",
            "DID_KTAG_CN_ST_CITY",
            "DID_MID_KTAG_CN_ST",
            "DID_MID_KTAG_CN_ST_DIS",
            "DID_MID_KTAG_CN_ST_DIS_TALUK",
            "DID_MID_KTAG_CN_ST_CITY",
            "DID_MTAG_CN_ST",
            "DID_MTAG_CN_ST_DIS",
            "DID_MTAG_CN_ST_DIS_TALUK",
            "DID_MTAG_CN_ST_CITY",
            "DID_KTAG_MTAG_CN_ST",
            "DID_KTAG_MTAG_CN_ST_DIS",
            "DID_KTAG_MTAG_CN_ST_DIS_TALUK",
            "DID_KTAG_MTAG_CN_ST_CITY"],
        AS : ["DID",
            "DID_COUNTRY_STATE",
            "DID_COUNTRY_STATE_DISTRICT",
            "DID_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_COUNTRY_STATE_CITY",
            "DID_MTYPE",
            "DID_MTYPE_COUNTRY_STATE",
            "DID_MTYPE_COUNTRY_STATE_DISTRICT",
            "DID_MTYPE_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MTYPE_COUNTRY_STATE_CITY",
            "DID_VID",
            "DID_VID_COUNTRY_STATE",
            "DID_VID_COUNTRY_STATE_DISTRICT",
            "DID_VID_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_VID_COUNTRY_STATE_CITY",
            "DID_DMODEL",
            "DID_DMODEL_COUNTRY_STATE",
            "DID_DMODEL_COUNTRY_STATE_DISTRICT",
            "DID_DMODEL_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_DMODEL_COUNTRY_STATE_CITY",
            "DID_KID",
            "DID_KTAG",
            "DID_KTAG_COUNTRY_STATE",
            "DID_KTAG_COUNTRY_STATE_DISTRICT",
            "DID_KTAG_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_KTAG_COUNTRY_STATE_CITY",
            "DID_ATYPE",
            "DID_ATYPE_COUNTRY_STATE",
            "DID_ATYPE_COUNTRY_STATE_DISTRICT",
            "DID_ATYPE_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_ATYPE_COUNTRY_STATE_CITY",
            "DID_MYEAR",
            "DID_MYEAR_COUNTRY_STATE",
            "DID_MYEAR_COUNTRY_STATE_DISTRICT",
            "DID_MYEAR_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MYEAR_COUNTRY_STATE_CITY",
            "DID_MTYPE_VID",
            "DID_MTYPE_VID_COUNTRY_STATE",
            "DID_MTYPE_VID_COUNTRY_STATE_DISTRICT",
            "DID_MTYPE_VID_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MTYPE_VID_COUNTRY_STATE_CITY",
            "DID_MTYPE_DMODEL",
            "DID_MTYPE_DMODEL_COUNTRY_STATE",
            "DID_MTYPE_DMODEL_COUNTRY_STATE_DISTRICT",
            "DID_MTYPE_DMODEL_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MTYPE_DMODEL_COUNTRY_STATE_CITY",
            "DID_MTYPE_MYEAR",
            "DID_MTYPE_MYEAR_COUNTRY_STATE",
            "DID_MTYPE_MYEAR_COUNTRY_STATE_DISTRICT",
            "DID_MTYPE_MYEAR_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MTYPE_MYEAR_COUNTRY_STATE_CITY",
            "DID_MTYPE_KID",
            "DID_MTYPE_KTAG",
            "DID_MTYPE_KTAG_COUNTRY_STATE",
            "DID_MTYPE_KTAG_COUNTRY_STATE_DISTRICT",
            "DID_MTYPE_KTAG_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MTYPE_KTAG_COUNTRY_STATE_CITY",
            "DID_ATYPE_VID",
            "DID_ATYPE_VID_COUNTRY_STATE",
            "DID_ATYPE_VID_COUNTRY_STATE_DISTRICT",
            "DID_ATYPE_VID_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_ATYPE_VID_COUNTRY_STATE_CITY",
            "DID_ATYPE_DMODEL",
            "DID_ATYPE_DMODEL_COUNTRY_STATE",
            "DID_ATYPE_DMODEL_COUNTRY_STATE_DISTRICT",
            "DID_ATYPE_DMODEL_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_ATYPE_DMODEL_COUNTRY_STATE_CITY",
            "DID_ATYPE_MYEAR",
            "DID_ATYPE_MYEAR_COUNTRY_STATE",
            "DID_ATYPE_MYEAR_COUNTRY_STATE_DISTRICT",
            "DID_ATYPE_MYEAR_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_ATYPE_MYEAR_COUNTRY_STATE_CITY",
            "DID_ATYPE_KID",
            "DID_ATYPE_KTAG",
            "DID_ATYPE_KTAG_COUNTRY_STATE",
            "DID_ATYPE_KTAG_COUNTRY_STATE_DISTRICT",
            "DID_ATYPE_KTAG_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_ATYPE_KTAG_COUNTRY_STATE_CITY",
            "DID_VID_DMODEL",
            "DID_VID_DMODEL_COUNTRY_STATE",
            "DID_VID_DMODEL_COUNTRY_STATE_DISTRICT",
            "DID_VID_DMODEL_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_VID_DMODEL_COUNTRY_STATE_CITY",
            "DID_VID_MYEAR",
            "DID_VID_MYEAR_COUNTRY_STATE",
            "DID_VID_MYEAR_COUNTRY_STATE_DISTRICT",
            "DID_VID_MYEAR_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_VID_MYEAR_COUNTRY_STATE_CITY",
            "DID_VID_KTAG",
            "DID_VID_KTAG_COUNTRY_STATE",
            "DID_VID_KTAG_COUNTRY_STATE_DISTRICT",
            "DID_VID_KTAG_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_VID_KTAG_COUNTRY_STATE_CITY",
            "DID_DMODEL_KTAG",
            "DID_DMODEL_KTAG_COUNTRY_STATE",
            "DID_DMODEL_KTAG_COUNTRY_STATE_DISTRICT",
            "DID_DMODEL_KTAG_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_DMODEL_KTAG_COUNTRY_STATE_CITY",
            "DID_DMODEL_KID",
            "DID_MYEAR_KTAG",
            "DID_MYEAR_KTAG_COUNTRY_STATE",
            "DID_MYEAR_KTAG_COUNTRY_STATE_DISTRICT",
            "DID_MYEAR_KTAG_COUNTRY_STATE_DISTRICT_TALUK",
            "DID_MYEAR_KTAG_COUNTRY_STATE_CITY",
            "DID_MYEAR_KID",
            "DID_VID_KID"]
    };


    var baseQueries = { ITC_MONTH : "SELECT SUM(CASE WHEN TR.TYPE = 'r' THEN 1 ELSE 0 END) AS rc, SUM(CASE WHEN TR.TYPE = 'i' THEN 1 ELSE 0 END) AS ic, SUM(CASE WHEN TR.TYPE = 'p' THEN 1 ELSE 0 END) AS sc, SUM(CASE WHEN TR.TYPE = 'w' THEN 1 ELSE 0 END) AS wc, SUM(CASE WHEN TR.TYPE = 't' THEN 1 ELSE 0 END) AS trc, DATE_FORMAT(T, '%Y-%m') AS t, COUNT(1) AS tc FROM TRANSACTION TR WHERE TR.T >= CONCAT('TOKEN_START_TIME', '-', '01 00:00:00') AND TR.T < CONCAT(LAST_DAY(STR_TO_DATE('TOKEN_END_TIME', '%Y-%m')), ' 23:59:59') "
        , ITC_DAY : "SELECT SUM(CASE WHEN TR.TYPE = 'r' THEN 1 ELSE 0 END) AS rc, SUM(CASE WHEN TR.TYPE = 'i' THEN 1 ELSE 0 END) AS ic, SUM(CASE WHEN TR.TYPE = 'p' THEN 1 ELSE 0 END) AS sc, SUM(CASE WHEN TR.TYPE = 'w' THEN 1 ELSE 0 END) AS wc, SUM(CASE WHEN TR.TYPE = 't' THEN 1 ELSE 0 END) AS trc, DATE_FORMAT(T, '%Y-%m-%d') AS t, COUNT(1) AS tc FROM TRANSACTION TR WHERE TR.T >= CONCAT('TOKEN_START_TIME', ' 00:00:00') AND TR.T < CONCAT('TOKEN_END_TIME', ' 23:59:59') "
        , IR_MONTH : "select SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS soc, SUM(CASE WHEN IEL.TY = 201 THEN 1 ELSE 0 END) AS lmc, SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS gmc, SUM(CASE WHEN IEL.TY = 200 THEN IEL.DR ELSE 0 END)/3600000 AS sod, SUM(CASE WHEN IEL.TY = 201 THEN IEL.DR ELSE 0 END)/3600000 AS lmd, SUM(CASE WHEN IEL.TY = 200 THEN IEL.DR ELSE 0 END)/3600000 AS gmd, DATE_FORMAT(ED, '%Y-%m') AS t from INVNTRYEVNTLOG IEL WHERE (IEL.ED >= CONCAT('TOKEN_START_TIME', '-', '01 00:00:00') AND IEL.ED < CONCAT(LAST_DAY(STR_TO_DATE('TOKEN_END_TIME', '%Y-%m')), ' 23:59:59')) "
        , IR_DAY : "select SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS soc, SUM(CASE WHEN IEL.TY = 201 THEN 1 ELSE 0 END) AS lmc, SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS gmc, SUM(CASE WHEN IEL.TY = 200 THEN IEL.DR ELSE 0 END)/3600000 AS sod, SUM(CASE WHEN IEL.TY = 201 THEN IEL.DR ELSE 0 END)/3600000 AS lmd, SUM(CASE WHEN IEL.TY = 200 THEN IEL.DR ELSE 0 END)/3600000 AS gmd, DATE_FORMAT(ED, '%Y-%m-%d') AS t from INVNTRYEVNTLOG IEL WHERE (IEL.ED >= CONCAT('TOKEN_START_TIME',' 00:00:00') AND IEL.ED < CONCAT('TOKEN_END_TIME', ' 23:59:59')) "
        , IAS_MONTH : "select SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS soec, SUM(CASE WHEN IEL.TY = 201 THEN 1 ELSE 0 END) AS lmec, SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS gmec, DATE_FORMAT(SD, '%Y-%m') AS t from INVNTRYEVNTLOG IEL WHERE (IEL.SD >= CONCAT('TOKEN_START_TIME', '-', '01 00:00:00') AND IEL.SD < CONCAT(LAST_DAY(STR_TO_DATE('TOKEN_END_TIME', '%Y-%m')), ' 23:59:59')) AND IEL.ED IS NOT NULL "
        , IAS_DAY : "select SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS soec, SUM(CASE WHEN IEL.TY = 201 THEN 1 ELSE 0 END) AS lmec, SUM(CASE WHEN IEL.TY = 200 THEN 1 ELSE 0 END) AS gmec, DATE_FORMAT(SD, '%Y-%m-%d') AS t from INVNTRYEVNTLOG IEL WHERE (IEL.SD >= CONCAT('TOKEN_START_TIME', ' 00:00:00') AND IEL.SD < CONCAT('TOKEN_END_TIME', ' 23:59:59')) AND IEL.ED IS NOT NULL "
        , AAS_MONTH : "select SUM(CASE WHEN DSL.STATUS_KEY = 'wsk' AND DSL.status_key = 0 THEN 1 ELSE 0 END) AS wkec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 1 THEN 1 ELSE 0 END) AS urec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 2 THEN 1 ELSE 0 END) AS brec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 3 THEN 1 ELSE 0 END) AS cdec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 4 THEN 1 ELSE 0 END) AS stbec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 5 THEN 1 ELSE 0 END) AS `dec`, DATE_FORMAT(from_unixtime(DSL.start_time), '%Y-%m') AS t from device_status_log DSL WHERE (from_unixtime(DSL.start_time) >= CONCAT('TOKEN_START_TIME', '-', '01 00:00:00') AND from_unixtime(DSL.start_time) < CONCAT(LAST_DAY(STR_TO_DATE('TOKEN_END_TIME', '%Y-%m')), ' 23:59:59')) "
        , AAS_DAY : "select SUM(CASE WHEN DSL.STATUS_KEY = 'wsk' AND DSL.status_key = 0 THEN 1 ELSE 0 END) AS wkec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 1 THEN 1 ELSE 0 END) AS urec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 2 THEN 1 ELSE 0 END) AS brec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 3 THEN 1 ELSE 0 END) AS cdec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 4 THEN 1 ELSE 0 END) AS stbec, SUM(CASE WHEN DSL.status_key = 'wsk' AND DSL.status_key = 5 THEN 1 ELSE 0 END) AS `dec`, DATE_FORMAT(from_unixtime(DSL.start_time), '%Y-%m-%d') AS t from device_status_log DSL WHERE (from_unixtime(DSL.start_time) >= CONCAT('TOKEN_START_TIME', ' 00:00:00') AND from_unixtime(DSL.start_time) < CONCAT('TOKEN_END_TIME', ' 23:59:59')) "
    };

    var groupQueries = {
        ITC_MONTH : " GROUP BY DATE_FORMAT(T, '%Y-%m')"
        , ITC_DAY : " GROUP BY DATE_FORMAT(T, '%Y-%m-%d')"
        , IR_MONTH : " GROUP BY DATE_FORMAT(ED, '%Y-%m')"
        , IR_DAY : " GROUP BY DATE_FORMAT(ED, '%Y-%m-%d')"
        , IAS_MONTH : " GROUP BY DATE_FORMAT(SD, '%Y-%m') "
        , IAS_DAY : " GROUP BY DATE_FORMAT(SD, '%Y-%m-%d')"
        , AAS_MONTH : " GROUP BY DATE_FORMAT(from_unixtime(DSL.start_time), '%Y-%m')"
        , AAS_DAY : "  GROUP BY DATE_FORMAT(from_unixtime(DSL.start_time), '%Y-%m-%d')"
    };

    var kidQueries = {
        ITC : " AND TR.KID IN (TOKEN_KID) "
        ,IR : " AND IEL.KID IN (TOKEN_KID) "
        ,IAS : " AND IEL.KID IN (TOKEN_KID) "
        ,AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = CONCAT('kiosk.', TOKEN_KID))) "
    };
    var didKidQueries = { ITC : " AND TR.KID IN (SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID = TOKEN_DID) ",
        IR : " AND IEL.KID IN (SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID = TOKEN_DID) ",
        IAS : " AND IEL.KID IN (SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID = TOKEN_DID) ",
        AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = 'TOKEN_DID')) "
    };
    var midQueries = {
        ITC : " AND TR.MID IN (TOKEN_MID) ",
        IR : " AND IEL.MID IN (TOKEN_MID) ",
        IAS : " AND IEL.MID IN (TOKEN_MID) "
    };
    var ktagQueries = {
        ITC : " AND TR.KID IN (SELECT KIOSKID FROM KIOSK_TAGS WHERE ID IN ($$enclosecsv(KTAG_ID)$$)) ",
        IR : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK_TAGS WHERE ID IN ($$enclosecsv(KTAG_ID)$$)) ",
        IAS : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK_TAGS WHERE ID IN ($$enclosecsv(KTAG_ID)$$)) ",
        AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = 'TOKEN_DID')) " //not supported
    };
    var mtagQueries = {
        ITC : " AND TR.MID IN (SELECT MATERIALID FROM MATERIAL_TAGS WHERE ID IN ($$enclosecsv(MTAG_ID)$$)) "
        ,IR : " AND IEL.MID IN (SELECT MATERIALID FROM MATERIAL_TAGS WHERE ID IN ($$enclosecsv(MTAG_ID)$$)) "
        ,IAS : " AND IEL.MID IN (SELECT MATERIALID FROM MATERIAL_TAGS WHERE ID IN ($$enclosecsv(MTAG_ID)$$)) "
    };

    var stQueries = {
        ITC : " AND TR.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST)) "
        ,IR : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST)) "
        ,IAS : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST)) "
        ,AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = 'TOKEN_DID')) " // not supported
    };

    var disQueries = {
        ITC : " AND TR.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS)) "
        ,IR : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS)) "
        ,IAS : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS)) "
        ,AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = 'TOKEN_DID')) " // not supported
    };
    var talukQueries = {
        ITC : " AND TR.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS) AND TALUK IN (TOKEN_TALUK)) "
        ,IR : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS) AND TALUK IN (TOKEN_TALUK)) "
        ,IAS : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND DISTRICT IN (TOKEN_DIS) AND TALUK IN (TOKEN_TALUK)) "
        ,AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = 'TOKEN_DID')) "
    };
    var cityQueries = {
        ITC : " AND TR.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND CITY IN (TOKEN_CITY)) "
        ,IR : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND CITY IN (TOKEN_CITY)) "
        ,IAS : " AND IEL.KID IN (SELECT KIOSKID FROM KIOSK WHERE STATE IN (TOKEN_ST) AND CITY IN (TOKEN_CITY)) "
        ,AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICES_ID FROM DEVICES_TAGS WHERE TAGS_ID IN(SELECT ID FROM TAGS WHERE TAGNAME = 'TOKEN_DID')) " // not supported
    };

    var mtypeQueries = {
        AAS : " AND DSL.DEVICE_ID IN (SELECT ID FROM DEVICES WHERE CASE WHEN TOKEN_MTYPE = 1 THEN ASSETTYPE_ID IN (1) WHEN TOKEN_MTYPE = 2 THEN ASSETTYPE_ID IN (2,3,5,6) END)"
    };

    var atypeQueries = {
        AAS : " AND DSL.DEVICE_ID IN (SELECT ID FROM DEVICES WHERE  ASSETTYPE_ID IN (TOKEN_ATYPE))"
    };

    var vidQueries = {
        AAS : " AND DSL.DEVICE_ID IN (SELECT ID FROM DEVICES WHERE VENDORID IN (TOKEN_VID)) "
    };

    var dmodelQueries = {
        AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICE_ID FROM DEVICE_META_DATA WHERE KY = 'dev.mdl' AND VALUE IN (TOKEN_DMODEL)) "
    };

    var myearQueries = {
        AAS : " AND DSL.DEVICE_ID IN (SELECT DEVICE_ID FROM DEVICE_META_DATA WHERE KY = 'dev.yom' AND VALUE IN (TOKEN_MYEAR)) "
    };


    for(var i =0;i<reports.length;i++){
        for(var j=0;j<periods.length;j++){
            for(var k=0;k<reportFilters[reportsType[reports[i]]].length;k++){
                var tokens = reportFilters[reportsType[reports[i]]][k].split('_');
                var qu = {};
                qu.userId = defaultUser;
                if(reportsType[reports[i]]=='INV'){
                    qu.serverId = mysqlServerId;
                }else  if(reportsType[reports[i]]=='AS'){
                    qu.serverId = mysqlAssetServerId;
                }
                qu.queryId = reports[i] + '_' + reportFilters[reportsType[reports[i]]][k] + '_' + periods[j];
                qu.query = baseQueries[reports[i]+'_'+periods[j]];
                if(tokens.indexOf("KID") == -1){
                    qu.query += didKidQueries[reports[i]];
                }
                for(var l = 1; l<tokens.length; l++){
                    if(tokens[l] == 'KID'){
                        qu.query += kidQueries[reports[i]];
                    }
                    if(tokens[l] == 'MID'){
                        qu.query += midQueries[reports[i]];
                    }
                    if(tokens[l] == 'MTYPE'){
                        qu.query += mtypeQueries[reports[i]];
                    }
                    if(tokens[l] == 'ATYPE'){
                        qu.query += atypeQueries[reports[i]];
                    }
                    if(tokens[l] == 'VID'){
                        qu.query += vidQueries[reports[i]];
                    }
                    if(tokens[l] == 'DMODEL'){
                        qu.query += dmodelQueries[reports[i]];
                    }
                    if(tokens[l] == 'MYEAR'){
                        qu.query += myearQueries[reports[i]];
                    }
                    if(tokens[l] == 'KTAG'){
                        qu.query += ktagQueries[reports[i]];
                    }
                    if(tokens[l] == 'MTAG'){
                        qu.query += mtagQueries[reports[i]];
                    }
                    if(tokens[l] == "CN"){
                        if(tokens[l+1] == "ST"){
                            l++;
                            if(tokens[l+1]=="DIS"){
                                if(tokens[l+2] == "TALUK"){
                                    qu.query += talukQueries[reports[i]];
                                    l = l+2;
                                }else{
                                    qu.query += disQueries[reports[i]];
                                    l = l+1;
                                }
                            }else if(tokens[l+1]=="CITY"){
                                qu.query += cityQueries[reports[i]];
                                l = l+1;
                            }else{
                                qu.query += stQueries[reports[i]];
                            }
                        } else{
                            console.log("problem!!");
                        }
                    }
                }
                qu.query += groupQueries[reports[i]+'_'+periods[j]];
                d.push(qu);
            }
        }
    }

    db.collection(queryCollection).insertMany(d, function (err, result) {
        assert.equal(err, null);
        console.log("Queries inserted successfully");
        callback();
    });
    if(args.indexOf('-u')!=-1){
        db.collection(userCollection).insertOne(user, function(err, result) {
            assert.equal(err, null);
            console.log("User config inserted successfully");
        });
    }
};
