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

package com.logistimo.config.models;


import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EventSummaryConfigModel implements Serializable {

  private static final String CATEGORY_TYPE_DELIMITER = "$";

  @SerializedName(value = "tag_distribution")
  private List<String> tagDistribution;

  private String tag;

  private List<Events> events;

  public List<Events> getEvents() {
    if (events == null) {
      return new ArrayList<>(0);
    }
    return events;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public List<String> getTagDistribution() {
    return tagDistribution;
  }

  public void setTagDistribution(List<String> tagDistribution) {
    this.tagDistribution = tagDistribution;
  }

  public void setEvents(List<Events> events) {
    this.events = events;
  }

  public class Events implements Serializable {

    @SerializedName(value = "event_type")
    private String type;
    private String category;
    private List<Threshold> thresholds;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getCategory() {
      return category;
    }

    public void setCategory(String category) {
      this.category = category;
    }

    public List<Threshold> getThresholds() {
      return thresholds;
    }

    public void setThresholds(
        List<Threshold> thresholds) {
      this.thresholds = thresholds;
    }
  }

  class Condition implements Serializable {

    Condition(String name, String units, String operations) {
      this.name = name;
      this.units = units;
      this.oper = operations;
    }


    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      result = 31 * result + (units != null ? units.hashCode() : 0);
      result = 31 * result + (values != null ? values.hashCode() : 0);
      result = 31 * result + (oper != null ? oper.hashCode() : 0);
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Condition condition = (Condition) o;

      return (name != null ? name.equals(condition.name) : condition.name == null) && (value != null
          ? value.equals(condition.value) : condition.value == null) && (units != null ? units
          .equals(condition.units) : condition.units == null) && (values != null ? values
          .equals(condition.values) : condition.values == null) && (oper != null ? oper
          .equals(condition.oper) : condition.oper == null);
    }

    private String name;
    private String value;
    private String units;
    private List<String> values;
    private String oper;

    public String getOper() {
      return oper;
    }

    public void setOper(String oper) {
      this.oper = oper;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getUnits() {
      return units;
    }

    public void setUnits(String units) {
      this.units = units;
    }

    public List<String> getValues() {
      return values;
    }

    public void setValues(List<String> values) {
      this.values = values;
    }

  }

  class Threshold implements Serializable {


    private String id;

    private List<Condition> conditions;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public List<Condition> getConditions() {
      return conditions;
    }

    public void setConditions(List<Condition> conditions) {
      this.conditions = conditions;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + (conditions != null ? conditions.hashCode() : 0);
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Threshold that = (Threshold) o;

      return !(id != null ? !id.equals(that.id) : that.id != null) && (conditions != null
          ? conditions
          .equals(that.conditions) : that.conditions == null);
    }
  }

  /**
   * Remove the events based on the combination of category and type
   *
   * @param category category of event
   */
  public void removeEventsByCategory(String category) {
    if (StringUtils.isBlank(category)) {
      return;
    }
    if (this.getEvents() == null) {
      return;
    }
    List<Events> eventList = new ArrayList<>();
    this.getEvents().stream()
        .filter(categoryEvents -> !categoryEvents.getCategory().equalsIgnoreCase(category)).forEach(
        eventList::add);
    this.setEvents(eventList);
  }

  /**
   * Add the list of events
   *
   * @param eventsList list of events to add to the field
   */
  public void addEvents(List<Events> eventsList) {
    this.getEvents().addAll(eventsList);
  }

  /**
   * Append the unique identifier to the id field
   */
  public void setUniqueIdentifier() {
    if (this.getEvents().isEmpty()) {
      return;
    }
    for (Events categoryEvents : this.getEvents()) {
      int counter = 0;
      final Iterator<Threshold> iterator = categoryEvents.getThresholds().iterator();
      while (iterator.hasNext()) {
        Threshold threshold = iterator.next();
        if (!threshold.conditions.isEmpty() ) {
          threshold.setId(String.valueOf(Math.abs((categoryEvents.getCategory() +
              categoryEvents.getType() +
              threshold.hashCode() + counter++).hashCode())));
        } else {
          iterator.remove();
        }
      }
    }
  }


  /**
   * Build the map with values from the template
   *
   * @param eventsList list of events
   * @return map of categories and conditions values
   */
  private Map<String, Map<String, Condition>> populateThresholdMap(List<Events> eventsList) {
    Map<String, Map<String, Condition>> map = new HashMap<>(eventsList.size());
    for (Events event : eventsList) {
      //form a key of the map by appending category,$ and type
      Threshold threshold = event.getThresholds().get(0);
      //for a nested map with conditions names and the conditions object
      Map<String, Condition> thresholdMap = new LinkedHashMap<>(threshold.getConditions().size());
      for (Condition condition : threshold.getConditions()) {
        thresholdMap.put(condition.getName(), condition);
      }
      map.put(event.getCategory() + CATEGORY_TYPE_DELIMITER + event.getType(), thresholdMap);
    }
    return map;
  }

  public void buildEvents(List<Events> templateEvents) {
    //build the map with the properties in template json
    Map<String, Map<String, Condition>> templateMap = populateThresholdMap(templateEvents);
    //Get the keys present in the template map
    Set<String> templateKeySet = new HashSet<>(templateMap.keySet());
    Iterator<Events> eventsIterator = this.getEvents().iterator();
    while (eventsIterator.hasNext()) {
      Events event = eventsIterator.next();
      String key = event.getCategory() + CATEGORY_TYPE_DELIMITER + event.getType();
      //if the configured category and events are not present in the template, remove it from the configured values
      if (!templateMap.containsKey(key)) {
        eventsIterator.remove();
        continue;
      }
      //else if the key is present, remove it from the template key set
      templateKeySet.remove(key);
      //Get the conditions map for the key
      Map<String, Condition> thresholdMap = templateMap.get(key);
      //Get a set with the conditions names
      updateThresholds(event, thresholdMap);
    }
    //Add the category/type not present
    if (!templateKeySet.isEmpty()) {
      for (String key : templateKeySet) {
        this.getEvents().add(populateEvents(key, templateMap));
      }
    }
  }

  private void updateThresholds(Events event, Map<String, Condition> templateConditionMap) {
    if (event.getThresholds() == null || event.getThresholds().isEmpty()) {
      getDefaultEvents(event, templateConditionMap);
    } else {
      buildConditions(event, templateConditionMap);
    }
  }

  private void buildConditions(Events event,
                               Map<String, Condition> templateConditionMap) {
    List<Condition> allConditions;
    for (Threshold threshold : event.getThresholds()) {
      allConditions = new ArrayList<>(templateConditionMap.size());
      for (Map.Entry<String, Condition> templateConditionEntry : templateConditionMap
          .entrySet()) {
        Iterator<Condition> dbConditionIterator = threshold.getConditions().iterator();
        boolean isNew = true;
        while (dbConditionIterator.hasNext()) {
          Condition condition = dbConditionIterator.next();
          if (templateConditionEntry.getKey().equals(condition.getName())) {
            isNew = false;
            Condition value = templateConditionEntry.getValue();
            condition.setUnits(value.getUnits() != null ? value.getUnits() : StringUtils.EMPTY);
            condition.setOper(value.getOper() != null ? value.getOper() : StringUtils.EMPTY);
            allConditions.add(condition);
            dbConditionIterator.remove();
            break;
          }
        }
        if (isNew) {
          Condition value = templateConditionEntry.getValue();
          allConditions.add(new Condition(value.getName(), value.getUnits(), value.getOper()));
        }
      }
      threshold.setConditions(allConditions);
    }
  }

  private void getDefaultEvents(Events event,
                                Map<String, Condition> templateConditionMap) {
    List<Condition> allConditions;
    allConditions = new ArrayList<>(templateConditionMap.size());
    List<Threshold> thresholdList = new ArrayList<>(1);
    allConditions
        .addAll(templateConditionMap.entrySet().stream().map(Map.Entry::getValue).collect(
            Collectors.toList()));
    Threshold threshold = new Threshold();
    threshold.setConditions(allConditions);
    thresholdList.add(threshold);
    event.setThresholds(thresholdList);
  }

  /**
   * populate the events for new thresholds
   *
   * @param key CATEGORY and TYPE
   */
  private Events populateEvents(String key, Map<String, Map<String, Condition>> templateMap) {
    Events event = new Events();
    List<Condition> conditionList = new ArrayList<>();
    String[] categoryList = key.split("\\" + CATEGORY_TYPE_DELIMITER);
    conditionList.addAll(
        templateMap.get(key).entrySet().stream().map(Map.Entry::getValue)
            .collect(Collectors.toList()));
    Threshold threshold = new Threshold();
    threshold.setConditions(conditionList);
    event.setThresholds(Collections.singletonList(threshold));
    event.setCategory(categoryList[0]);
    event.setType(categoryList[1]);
    return event;
  }


}

