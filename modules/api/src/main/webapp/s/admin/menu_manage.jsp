<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%--
  ~ Copyright © 2017 Logistimo.
  ~
  ~ This file is part of Logistimo.
  ~
  ~ Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
  ~ low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
  ~
  ~ This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
  ~ Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
  ~ later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  ~ warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
  ~ for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
  ~ <http://www.gnu.org/licenses/>.
  ~
  ~ You can be released from the requirements of the license by purchasing a commercial license. To know more about
  ~ the commercial license, please contact us at opensource@logistimo.com
  --%>

<%
	/*
		menuitems - list of tabs on the page
		Default view is home. Just pass the 'view' parameter to set the menu to that view.
	*/
	// Get the tab view parameters
	String selectedItem = request.getParameter("view");
	
	ArrayList<String> menuitems = new ArrayList<String>();
	menuitems.add( "Domains" );
	menuitems.add( "Data" );
	menuitems.add( "System Configuration" );
	
	// Get the request parameter 'view' and cycle through the array
	if ("".equalsIgnoreCase(selectedItem)) {
		selectedItem = "Domains";
	}
%>
			<div id="sgmenu" class="yui-navset">
				 <ul class="yui-nav">
				 <%
				 	String selectedItemDisplay;
				 	for (String m : menuitems) {
				 		String normalizedMenuItem = m.toLowerCase().replace( ' ', '_' );
				 		selectedItemDisplay = "";
				 		if (normalizedMenuItem.equals(selectedItem)) {
				 			selectedItemDisplay = "selected";
				 		}
				 		String linkString = "/s/admin/" + normalizedMenuItem + ".jsp";
				 			
				 		out.print("<li class=\""+selectedItemDisplay+"\"><a href=\""+ linkString+ "\"><em>" + m + "</em></a></li>" );
				 	}
				 %>
 				</ul>
 			</div>