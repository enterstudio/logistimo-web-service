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

package com.logistimo.services.mapper;

import com.logistimo.logger.XLog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class MapReduceUtil {


  private static final String xmlName = "mapreduce.xml";

  private static final XLog xLogger = XLog.getLog(MapReduceUtil.class);

  public static Configuration getXMLConfiguration(String configName) {
    Configuration configuration = new Configuration();
    try {
      InputSource
          source =
          new InputSource(
              Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlName));

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse(source);

      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();

      NodeList
          nodes =
          (NodeList) xpath
              .evaluate("//configuration[@name = '" + configName + "']/property", document,
                  XPathConstants.NODESET);
      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          NodeList children = nodes.item(i).getChildNodes();
          String name = null;
          String value = null;
          for (int j = 0; j < children.getLength(); j++) {
            Node childNode = children.item(j);
            if (childNode != null) {
              if ("name".equals(childNode.getNodeName())) {
                name = childNode.getTextContent();
              } else if ("value".equals(childNode.getNodeName())) {
                value = childNode.getTextContent();
              }
            }
          }
          if (name != null && !name.isEmpty()) {
            configuration.set(name, value);
          }
        }
      }
    } catch (Exception e) {
      xLogger.severe("Failed to process mapreduce xml", e);
    }
    return configuration;
  }

}
