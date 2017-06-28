package com.logistimo.rest.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import com.logistimo.services.utils.ConfigUtil;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;

/**
 * Created by charan on 22/06/17.
 */
public class RestConfig {

  private RestConfig() {
  }

  public static RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    restTemplate.setMessageConverters(Collections.singletonList(createGsonHttpMessageConverter()));
    return restTemplate;
  }

  private static GsonHttpMessageConverter createGsonHttpMessageConverter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Date.class,
            (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(
                json.getAsJsonPrimitive().getAsLong()))
        .registerTypeAdapter(Date.class,
            (JsonSerializer<Date>) (date, type, jsonSerializationContext) -> new JsonPrimitive(
                date.getTime()))
        .create();
    GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
    gsonConverter.setGson(gson);
    return gsonConverter;
  }


  private static ClientHttpRequestFactory getClientHttpRequestFactory() {
    int timeout = ConfigUtil.getInt("api.client.timeout", 5000);
    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(timeout)
        .setConnectionRequestTimeout(timeout)
        .setSocketTimeout(timeout)
        .build();
    CloseableHttpClient client = HttpClientBuilder
        .create()
        .setDefaultRequestConfig(config)
        .build();
    return new HttpComponentsClientHttpRequestFactory(client);
  }
}