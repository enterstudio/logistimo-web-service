<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.api.util.KioskDataSimulator"%>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.constants.Constants" %>
<%@page import="com.logistimo.entities.entity.IKiosk" %>
<%@page import="com.logistimo.entities.service.EntitiesService" %>
<%@page import="com.logistimo.entities.service.EntitiesServiceImpl" %>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.Services" %>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.GregorianCalendar" %>
<%@page import="java.util.List" %>

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

<jsp:include page="../pageheader.jsp" >
<jsp:param name="mode" value="manage" />
	</jsp:include>
<jsp:include page="menu_manage.jsp" >
<jsp:param name="view" value="data" />
	</jsp:include>
<script type="text/javascript" src="../../js/sg.js"></script>

<%
	// Get the desired subview
	String selectedItem = request.getParameter("subview");
	
	ArrayList<String> subMenuItems = new ArrayList<String>();
	subMenuItems.add("Kiosk Simulator");
	
	if ("".equalsIgnoreCase(selectedItem) || selectedItem == null) {
		selectedItem = "Kiosk Simulator";
	}
%>
<div id="sgSubMenu">
	<p>
	<%
		String selectedItemDisplay;
		for (String sel : subMenuItems) {
			selectedItemDisplay = "";
			if (sel.equalsIgnoreCase(selectedItem)) {
				selectedItemDisplay = "selected";
			}
			String url = "data.jsp?view=data&subview=" + sel;
			out.print( "<a class=\""+selectedItemDisplay+"\" href=\"" + url + "\">" + sel + "</a>" );
		}
	 %>
	</p>
</div>
<div class="yui-g" style="border-bottom:1px solid #E6E6E6;">
<h2 class="addresourceheader">Simulate Transaction Data</h2>
</div>
<%
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	EntitiesService as = Services.getService(EntitiesServiceImpl.class);
	
	// Get the domain Id
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
		
	// Get all kiosks
	List<IKiosk> kiosks = null;
	kiosks = as.getAllKiosks( domainId, null, null ).getResults();
	
	// Get default start date
	Calendar cal = GregorianCalendar.getInstance();
	cal.setTime( new Date() );
	cal.add( Calendar.MONTH, -3 ); // 3 months back
	SimpleDateFormat start = new SimpleDateFormat( Constants.DATE_FORMAT );
	String strDate = start.format( cal.getTime() );	
%>
<div id="doc3">
	<p>
		Simulate transaction data (i.e. issues and receipts) according to a set of parameters that you can specify below. In addition, optimal order quantities will also be generated as per the run frequency specified below.
		Please fill out the form and click 'Generate data' to simulate transactions.
	</p>
	<br></br>
	<b>NOTE: Data can be generated for a maximum of 365 days and up to 10 materials per kiosk.</b>
	
	<form name="generatedata" method="post" action="/s/data" class="sgForm">
	<table id="addresource" width="100%">
	<tr>
		<th class="mandatory">Entity</th>
		<td>
			<select name="kioskid"/>
				<!-- <option value="">All kiosks</option>  -->
				<%
					for ( IKiosk k : kiosks ) { %>
						<option value="<%= k.getKioskId() %>"><%= k.getName() %></option>
				 <% } %>
			</select>
		</td>
	</tr>
	<tr><th>Transaction start date</th><td><input type="text" name="startdate" value="<%= strDate %>" /> (<i>dd/mm/yyyy</i>)</td></tr>
	<tr><th>Duration</th><td><input type="text" name="duration" value="<%= String.valueOf( KioskDataSimulator.DAYS ) %>"/> day(s)</td></tr>
	<tr><th>Issue periodicity</th>
		<td>
			<select name="issueperiodicity">
				<option value="<%= KioskDataSimulator.PERIODICITY_DAILY %>" selected>Daily</option>
				<option value="<%= KioskDataSimulator.PERIODICITY_WEEKLY %>">Weekly</option>				
			</select>
		</td>
	</tr>
	<tr><th>Initial stock</th><td><input type="text" name="stockonhand" value="<%= String.valueOf( KioskDataSimulator.INITIAL_STOCK ) %>"/> unit(s)</td></tr>
	<!-- <tr><th>Order periodicity</th><td><input type="text" name="orderperiodicity" value="<%= String.valueOf( KioskDataSimulator.ORDER_PERIODICITY ) %>"/> day(s)</td></tr>  -->
	<tr><th>Issue quantities</th><td><b>Mean</b>: <input type="text" name="issuemean" value="<%= String.valueOf( KioskDataSimulator.ISSUE_MEAN ) %>"/> unit(s), <b>Standard Deviation</b>: <input type="text" name="issuestddev" value="<%= String.valueOf( KioskDataSimulator.ISSUE_STDDEV ) %>"/></td></tr>
	<tr><th>Receipt quantities</th><td><b>Mean</b>: <input type="text" name="receiptmean" value="<%= String.valueOf( KioskDataSimulator.RECEIPT_MEAN ) %>"/> unit(s), <b>Standard Deviation</b>: <input type="text" name="receiptstddev" value="<%= String.valueOf( KioskDataSimulator.RECEIPT_STDDEV ) %>"/></td></tr>
	<tr><th>Zero-stock days<br></br>(after every stock-out)</th><td><b>Low</b>: <input type="text" name="zerostockdayslow" value="<%= String.valueOf( KioskDataSimulator.ZEROSTOCK_LOW ) %>"/> day(s), <b>High</b>: <input type="text" name="zerostockdayshigh" value="<%= String.valueOf( KioskDataSimulator.ZEROSTOCK_HIGH ) %>"/> day(s)</td></tr>		
	<tr><td colspan="2">&nbsp;</td></tr>
	</table>
	<input type="hidden" name="action" value="simulatetransdata" />
	<input type="hidden" name="requesttype" value="schedule" />
	<input type="hidden" name="type" value="data" />
	<input type="hidden" name="userid" value="<%= userId %>" />
	<input type="hidden" name="domainid" value="<%= domainId %>"/>
	<input type="submit" class="submit" value="Generate data now" />
	</form>
</div>

<jsp:include page="../pagefooter.html" />