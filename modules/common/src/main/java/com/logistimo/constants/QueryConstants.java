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

package com.logistimo.constants;

/**
 * @author Mohan Raja
 */
public class QueryConstants {
  public static final String SELECT_FROM = "SELECT FROM ";
  public static final String WHERE = " WHERE ";
  public static final String PARAMETERS = " PARAMETERS ";
  public static final String D_EQUAL = " == ";
  public static final String CONTAINS = "contains";
  public static final String LONG = "Long";
  public static final String AND = " && ";
  public static final String OR = " || ";
  public static final String NEGATION = "!";
  public static final String DOT_CONTAINS = ".contains(";

  //Param constants
  public static final String S_DID_PARAM = "sdIdParam";
  public static final String ROW_COUNT = "count(1)";
  public static final String GR_EQUAL = ">=";
  public static final String LESS_THAN = "<";
  public static final String GREATER_THAN = ">";
  public static final String LR_EQUAL = "<=";

  public static final String COUNT = "count(1)";

  public static final String STRING = "String";
  public static final String BOOLEAN = "Boolean";
  public static final String DATE = "Date";
  public static final String DATE_IMPORT = "import java.util.Date";

  public static final String ASCENDING = " asc";
  public static final String DESCENDING = " desc";
  public static final String ORDERBY = " ORDER BY";

  public static final String IN = " IN ";
  public static final String EQUALS_SIGN = " = ";
  public static final String SELECT = " SELECT ";
  public static final String FROM = " FROM ";
  public static final String LIMIT_1 = " LIMIT 1 ";

}
