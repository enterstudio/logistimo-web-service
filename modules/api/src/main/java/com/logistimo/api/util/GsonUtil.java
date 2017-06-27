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

package com.logistimo.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.logistimo.constants.Constants;
import com.logistimo.entities.models.UserEntitiesModel;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.proto.MobileOrderModel;
import com.logistimo.proto.MobileOrdersModel;
import com.logistimo.proto.MobileTransactionsModel;
import com.logistimo.proto.RelationshipInput;
import com.logistimo.proto.RelationshipInputDeserializer;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.proto.SetupDataInput;
import com.logistimo.proto.SetupDataInputDeserializer;
import com.logistimo.proto.UpdateInventoryInput;
import com.logistimo.proto.UpdateInventoryInputDeserializer;
import com.logistimo.proto.UpdateOrderRequest;
import com.logistimo.proto.UpdateOrderStatusRequest;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by chitrachar on 21/06/16.
 *
 * Utility methods using GSON.
 */
public class GsonUtil {

  public static String authenticateOutputToJson(boolean hasNoError, String message,
                                                String expiryTime,
                                                UserEntitiesModel userEntitiesModel,
                                                Hashtable<String, Object> config,
                                                String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    String configString;
    String statusCode = hasNoError ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    IUserAccount user = userEntitiesModel.getUserAccount();
    if (user != null && hasNoError) {
      jsonObject = (JsonObject) gson.toJsonTree(user);
      jsonObject.addProperty(JsonTagsZ.DATE_FORMAT,
          LocalDateUtil.getDateTimePattern(user.getLocale(), false));
      jsonObject.add(JsonTagsZ.KIOSKS,
          gson.toJsonTree(userEntitiesModel.getKiosks()));
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);
    if (hasNoError) {
      jsonObject.addProperty(JsonTagsZ.TIMESTAMP, expiryTime);
      if (config != null) {
        configString = gson.toJson(config);
        if (configString != null && !configString.isEmpty()) {
          JsonElement element = gson.fromJson(configString, JsonElement.class);
          jsonObject.add(JsonTagsZ.CONFIGURATION, element);
        }
      }
    } else {
      if (message != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
      }

    }
    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }

    return jsonString;
  }


  public static String updateInventoryOutputToJson(boolean status, String message,
                                                   List materialList, Vector errors,
                                                   String formattedTime, String trackingIdStr,
                                                   String localeStr, String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    Boolean hasPartialErrors = false;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    if (errors != null && !errors.isEmpty()) {
      hasPartialErrors = true;
      statusCode = "2";
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (status || hasPartialErrors) {
      jsonObject.addProperty(JsonTagsZ.TIMESTAMP, formattedTime);
      jsonObject.addProperty(JsonTagsZ.TRACKING_ID, trackingIdStr);
      if (materialList != null && !materialList.isEmpty()) {
        String materialString = gson.toJson(materialList);
        JsonElement mElement = gson.fromJson(materialString, JsonElement.class);
        jsonObject.add(JsonTagsZ.MATERIALS, mElement);
      }
      if (hasPartialErrors) {
        String errorString = gson.toJson(errors);
        if (errorString != null && !errorString.isEmpty()) {
          JsonElement errorElement = gson.fromJson(errorString, JsonElement.class);
          jsonObject.add(JsonTagsZ.ERRORS, errorElement);
        }
      }

    } else {
      if (message != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
      }
    }
    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;

  }

  public static UpdateInventoryInput updateInventoryInputFromJson(String jsonString) {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder
        .registerTypeAdapter(UpdateInventoryInput.class, new UpdateInventoryInputDeserializer());
    Gson
        gson =
        gsonBuilder.setDateFormat(Constants.DATE_FORMAT).excludeFieldsWithoutExposeAnnotation()
            .serializeNulls().create();
    UpdateInventoryInput updInv = gson.fromJson(jsonString, UpdateInventoryInput.class);
    return updInv;
  }

  public static String getInventoryOutputToJson(boolean status, List inventoryList, String currency,
                                                String errMessage, boolean onlyStock,
                                                String localeString, String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (status) {
      jsonObject.addProperty(JsonTagsZ.CURRENCY, currency);
      if (inventoryList != null && !inventoryList.isEmpty()) {
        String inventoryString = gson.toJson(inventoryList);
        JsonElement mElement = gson.fromJson(inventoryString, JsonElement.class);
        jsonObject.add(JsonTagsZ.MATERIALS, mElement);
      }

    } else {
      if (errMessage != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, errMessage);
      }
    }

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  public static String basicOutputToJson(boolean status, String errMsg, List errMsgs, String locale,
                                         String version) {

    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (!status) {
      if (errMsg != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, errMsg);
      }
      if (errMsgs != null && !errMsgs.isEmpty()) {
        String errString = gson.toJson(errMsgs);
        JsonElement mElement = gson.fromJson(errString, JsonElement.class);
        jsonObject.add(JsonTagsZ.ERRORS, mElement);
      }
    }

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  public static String getOrdersOutputToJson(boolean status, List ordersOrMaterials, String errMsg,
                                             boolean hasOrders, String currency, String locale,
                                             String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (status) {
      if (currency != null) {
        jsonObject.addProperty(JsonTagsZ.CURRENCY, currency);
      }
      if (ordersOrMaterials != null && !ordersOrMaterials.isEmpty()) {
        String orderString = gson.toJson(ordersOrMaterials);
        JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
        jsonObject.add(JsonTagsZ.ORDERS, mElement);
      }

    } else {
      if (errMsg != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, errMsg);
      }
    }

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }


  public static String getRelationshipsOutputToJson(boolean status, String relationshipType,
                                                    List linkedKiosks, String errMsg,
                                                    String localeStr, String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (status) {
      jsonObject.addProperty(JsonTagsZ.TYPE, relationshipType);
      if (linkedKiosks != null && !linkedKiosks.isEmpty()) {
        String linkedKiosksString = gson.toJson(linkedKiosks);
        JsonElement mElement = gson.fromJson(linkedKiosksString, JsonElement.class);
        jsonObject.add(JsonTagsZ.KIOSKS, mElement);
      }

    } else {
      if (errMsg != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, errMsg);
      }

    }
    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  public static RelationshipInput relationshipInputFromJson(String jsonString) {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(RelationshipInput.class, new RelationshipInputDeserializer());
    Gson
        gson =
        gsonBuilder.setDateFormat(Constants.DATE_FORMAT).excludeFieldsWithoutExposeAnnotation()
            .serializeNulls().create();
    RelationshipInput relationshipInput = gson.fromJson(jsonString, RelationshipInput.class);
    return relationshipInput;
  }

  public static String getTransactionsOutputToJson(boolean status, String kioskid,
                                                   List transactionList, String errMessage,
                                                   String locale, String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (status) {
      jsonObject.addProperty(JsonTagsZ.KIOSK_ID, kioskid);
      if (transactionList != null && !transactionList.isEmpty()) {
        String transactionString = gson.toJson(transactionList);
        JsonElement mElement = gson.fromJson(transactionString, JsonElement.class);
        jsonObject.add(JsonTagsZ.TRANSACTIONS, mElement);
      }

    } else {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, errMessage);
    }

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  public static String orderOutputToJson(boolean status, Map<String, Object> orderData,
                                         String message, String localeStr, String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    String statusCode = status ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    if (status) {
      if (orderData != null && !orderData.isEmpty()) {
        String orderString = gson.toJson(orderData);
        JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
        jsonObject = (JsonObject) gson.toJsonTree(mElement);//orderdata
      }

    } else {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
      jsonString = gson.toJson(jsonObject);
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  /**
   * Parse order request from mobile API
   */
  public static UpdateOrderRequest updateOrderInputFromJson(String jsonString) {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.setDateFormat(Constants.DATE_FORMAT)
        .serializeNulls().create();
    return gson.fromJson(jsonString, UpdateOrderRequest.class);
  }

  public static String setupDataOutputToJson(boolean status, String uId, String kioskId,
                                             String errMsg, List dataErrors, String localeStr,
                                             String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);
    if (status) {
      jsonObject.addProperty(JsonTagsZ.USER_ID, uId);
      jsonObject.addProperty(JsonTagsZ.KIOSK_ID, kioskId);

    } else {

      if (errMsg != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, errMsg);
      }
      if (dataErrors != null && !dataErrors.isEmpty()) {
        String errorString = gson.toJson(dataErrors);
        JsonElement mElement = gson.fromJson(errorString, JsonElement.class);
        jsonObject.add(JsonTagsZ.ERRORS, mElement);
      }
    }
    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }


  public static SetupDataInput setupDataInputFromJson(String jsonString) {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(SetupDataInput.class, new SetupDataInputDeserializer());
    Gson
        gson =
        gsonBuilder.setDateFormat(Constants.DATE_FORMAT).excludeFieldsWithoutExposeAnnotation()
            .serializeNulls().create();
    SetupDataInput setupDataInput = gson.fromJson(jsonString, SetupDataInput.class);
    return setupDataInput;
  }

  public static String aggTransDataOutputToJson(boolean status, int resultsSize,
                                                List transDataVector, String errMsg, String locale,
                                                String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    JsonObject jsonObject = new JsonObject();
    String statusCode = status ? "0" : "1";
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);
    if (status) {
      jsonObject.addProperty(JsonTagsZ.NUM_RESULTS, resultsSize);
      if (transDataVector != null && !transDataVector.isEmpty()) {
        String transString = gson.toJson(transDataVector);
        JsonElement mElement = gson.fromJson(transString, JsonElement.class);
        jsonObject.add(JsonTagsZ.DATA, mElement);
      }

    } else {
      if (errMsg != null) {
        jsonObject.addProperty(JsonTagsZ.MESSAGE, errMsg);
      }
    }
    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  public static String getOrderOutputToJson(boolean status, Map<String, Object> orderData,
                                            String message, String version) {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    String jsonString = null;
    String statusCode = status ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    if (status) {
      if (orderData != null && !orderData.isEmpty()) {
        String orderString = gson.toJson(orderData);
        JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
        jsonObject = (JsonObject) gson.toJsonTree(mElement);
      }

    } else {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
      jsonString = gson.toJson(jsonObject);
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  /**
   * Construct json string from the MobileOrderModel object.
   */
  public static String buildGetOrderResponseModel(boolean status, MobileOrderModel mom,
                                                  String message, String version) {
    Gson gson = new Gson();
    String jsonString = null;
    String statusCode = status ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    if (status) {
      if (mom != null) {
        String orderString = gson.toJson(mom);
        JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
        jsonObject = (JsonObject) gson.toJsonTree(mElement);
      }
    } else {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
      jsonString = gson.toJson(jsonObject);
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  public static String buildOrderJson(boolean status, MobileOrderModel mom,
                                      String message, String version, String errorCode) {
    Gson gson = new Gson();
    String jsonString = null;
    String statusCode = status ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    if (mom != null) {
      String orderString = gson.toJson(mom);
      JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
      jsonObject = (JsonObject) gson.toJsonTree(mElement);
    }
    if (!status) {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
      jsonString = gson.toJson(jsonObject);
      jsonObject.addProperty(JsonTagsZ.ERROR_CODE, errorCode);
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    if (jsonObject != null) {
      jsonString = gson.toJson(jsonObject);
    }
    return jsonString;
  }

  /**
   *
   * @param status
   * @param mom
   * @param message
   * @param version
   * @return
   */
  public static String buildGetOrdersResponseModel(boolean status, MobileOrdersModel mom,
                                                   String message, String version) {
    Gson gson = new Gson();
    String statusCode = status ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    if (status) {
      if (mom != null) {
        String orderString = gson.toJson(mom);
        JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
        jsonObject = (JsonObject) gson.toJsonTree(mElement);
      }
    } else {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);
    return gson.toJson(jsonObject);
  }

  /**
   * Method to build the response
   *
   * @param jsonObject- response json
   * @param errorMsg    -Error message
   * @param version     -version
   * @return response String
   */
  public static String buildResponse(JsonObject jsonObject, String errorMsg, String version) {

    short statusCode = RestConstantsZ.ORDERS_SUC_STATUS_CODE;
    if (StringUtils.isNotBlank(errorMsg)) {
      jsonObject = new JsonObject();
      jsonObject.addProperty(JsonTagsZ.MESSAGE, errorMsg);
      statusCode = RestConstantsZ.ORDERS_FAIL_STATUS_CODE;
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);
    return new Gson().toJson(jsonObject);
  }

  /**
   * Parse update order status request from mobile API
   */
  public static UpdateOrderStatusRequest buildUpdateOrderStatusRequestFromJson(String jsonString) {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.setDateFormat(Constants.DATE_FORMAT)
        .serializeNulls().create();
    return gson.fromJson(jsonString, UpdateOrderStatusRequest.class);
  }

  /**
   * Builds a json string output from the mobile transactions model
   *
   * @param hasNoError - true indicating that there was no error and false indicating there was error
   * @param mtm        - model containing the transactions as expected by the mobile
   * @param message    - Error message if status is false
   * @param version    -
   * @return Json string containing version, status and either error message or transactions
   */
  public static String buildGetTransactionsResponseModel(boolean hasNoError,
                                                         MobileTransactionsModel mtm,
                                                         String message, String version) {
    Gson gson = new Gson();
    String jsonString = null;
    String statusCode = hasNoError ? "0" : "1";
    JsonObject jsonObject = new JsonObject();
    if (hasNoError) {
      if (mtm != null) {
        String transactionsString = gson.toJson(mtm);
        JsonElement mElement = gson.fromJson(transactionsString, JsonElement.class);
        jsonObject = (JsonObject) gson.toJsonTree(mElement);
      }
    } else {
      jsonObject.addProperty(JsonTagsZ.MESSAGE, message);
    }
    jsonObject.addProperty(JsonTagsZ.VERSION, version);
    jsonObject.addProperty(JsonTagsZ.STATUS, statusCode);

    jsonString = gson.toJson(jsonObject);

    return jsonString;
  }

}

