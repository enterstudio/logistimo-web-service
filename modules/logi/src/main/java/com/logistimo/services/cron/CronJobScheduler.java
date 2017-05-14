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

package com.logistimo.services.cron;

import com.logistimo.logger.XLog;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by charan on 08/10/14.
 */
public class CronJobScheduler {

  private static final XLog _logger = XLog.getLog(CronJobScheduler.class);
  private static final Map<String, Integer> weekDays;

  static {
    weekDays = new HashMap<String, Integer>();
    weekDays.put("monday", Calendar.MONDAY);
    weekDays.put("tuesday", Calendar.TUESDAY);
    weekDays.put("wednesday", Calendar.WEDNESDAY);
    weekDays.put("thursday", Calendar.THURSDAY);
    weekDays.put("friday", Calendar.FRIDAY);
    weekDays.put("saturday", Calendar.SATURDAY);
    weekDays.put("sunday", Calendar.SUNDAY);
  }

  private Scheduler sched;

  public CronJobScheduler() {

    try {
      SchedulerFactory schedFact = new StdSchedulerFactory();
      sched = schedFact.getScheduler();
      sched.getListenerManager().addJobListener(new QuartzJobListener());
      sched.start();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          try {
            if (sched != null) {
              sched.shutdown();
            }
          } catch (SchedulerException e) {
            _logger.warn("Failed to shutdown Quartz Scheduler", e);
          }
        }
      });
    } catch (SchedulerException e) {
      _logger.severe("Failed to initialize quartz scheduler");
    }

    init("cron.xml");
  }

  public void init(String xmlName) {
    InputSource
        source =
        new InputSource(
            Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlName));
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try {
      db = dbf.newDocumentBuilder();
      Document document = db.parse(source);

      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      NodeList
          cronNodes =
          (NodeList) xpath.evaluate("//cronentries/cron", document, XPathConstants.NODESET);
      if (cronNodes != null) {
        for (int i = 0; i < cronNodes.getLength(); i++) {
          String name = null;
          String description = null;
          String schedule = null;
          String cronSched = null;
          try {
            NodeList children = cronNodes.item(i).getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
              Node childNode = children.item(j);
              if (childNode != null) {
                if ("url".equals(childNode.getNodeName())) {
                  name = childNode.getTextContent();
                } else if ("description".equals(childNode.getNodeName())) {
                  description = childNode.getTextContent();
                } else if ("schedule".equals(childNode.getNodeName())) {
                  schedule = childNode.getTextContent();
                } else if ("cronschedule".equals(childNode.getTextContent())) {
                  cronSched = childNode.getTextContent();
                }
              }
            }
            if (name != null && !name.isEmpty()) {

              JobDetail jobDetail = newJob(CronJob.class).withIdentity(description, "default")
                  .usingJobData("url", name).usingJobData("description", description)
                  .withDescription(description).build();
              TriggerBuilder builder = newTrigger().withIdentity(description);
              CronSchedule parsedSchedule = null;
              if (cronSched == null) {
                parsedSchedule = parseTime(schedule);
                if (parsedSchedule != null) {
                  builder =
                      builder.startAt(parsedSchedule.getStartTime().getTime()).withSchedule(
                          simpleSchedule()
                              .withIntervalInSeconds(parsedSchedule.getIntervalInSeconds())
                              .repeatForever());
                                    /*if (parsedSchedule.getStartTime().get(Calendar.DAY_OF_MONTH)>Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                                        String type = null;
                                        if (name.indexOf("ScheduleTransactionDataExport") > 0) {
                                            type = "ScheduleTransactionDataExport";
                                        } else if (name.indexOf("ScheduleCustomReportsExport") > 0) {
                                            type = "ScheduleCustomReportsExport";
                                        }
                                        if (type != null) {
                                            _logger.info("Trigger immediate execution of scheduler MR: " + type);
                                            Map<String, String> params = new HashMap<String, String>();
                                            params.put(IMapredService.PARAM_ENTITYKIND, "Domain");
                                            // Start the MR job
                                            AppFactory.get().getMapredService().startJob(type, params);
                                        }
                                    }*/
                } else {
                  _logger.warn(
                      "Unable to parse schedule description: {0} , taskUrl: {1}, schedule: {2}, cronSched: {3} ",
                      description, name, schedule, cronSched);
                  continue;
                }
              } else {
                builder = builder.withSchedule(cronSchedule(cronSched));
              }
              Trigger trigger = builder.forJob(jobDetail).build();
              sched.scheduleJob(jobDetail, trigger);
              _logger.info(
                  "Scheduled job for description: {0} , taskUrl: {1}, schedule: {2}, cronSched: {3} , nextRun {4}",
                  description, name, schedule, cronSched, parsedSchedule.getStartTime().getTime());
            }
          } catch (ObjectAlreadyExistsException e) {
            _logger.warn(
                "Failed to schedule job for description: {0} , taskUrl: {1}, schedule: {2}, cronSched: {3} ",
                description, name, schedule, cronSched);
          } catch (Exception e) {
            _logger.severe(
                "Failed to schedule job for description: {0} , taskUrl: {1}, schedule: {2}, cronSched: {3} ",
                description, name, schedule, cronSched);
          }
        }

      }
    } catch (Exception e) {
      _logger.severe("Failed to initialize cron jobs from xml {0}", xmlName, e);
    }

  }

  private CronSchedule parseTime(String schedule) {
    String[] splits = schedule.split(" ");
    if (splits.length == 3 && "every".equals(splits[0])) {
      String[] hMin = splits[2].split(":");
      Calendar calendar = Calendar.getInstance();
      int interval = -1;
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hMin[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(hMin[1]));
      interval = 24 * 60 * 60;
      if ("day".equals(splits[1])) {
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
          calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

      } else {
        Integer reqDay = weekDays.get(splits[1].toLowerCase());
        if (reqDay != null) {
          while (calendar.get(Calendar.DAY_OF_WEEK) != reqDay) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
          }
        } else {
          _logger.warn("Invalid day specified {0}", splits[1]);
        }
        interval = 7 * interval;
      }
      return new CronSchedule(calendar, interval);

    }
    return null;
  }

  public void shutdown() {
    if (sched != null) {
      try {
        _logger.info("Shutting down quartz scheduler");
        sched.shutdown(false);
      } catch (SchedulerException e) {
        _logger.warn("Failed to shutdown scheduler {0}", e.getMessage(), e);
      }
    }
  }

  public boolean isShuttingdown() {
    try {
      return sched == null || sched.isShutdown();
    } catch (SchedulerException e) {
      _logger.warn("Failed to shutdown scheduler {0}", e.getMessage(), e);
      return true;
    }
  }

}
