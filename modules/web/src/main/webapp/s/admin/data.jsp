<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.Services" %>
<%@page import="com.logistimo.users.service.UsersService" %>
<%@page import="com.logistimo.users.service.impl.UsersServiceImpl" %>
<%@page import="java.util.ArrayList"%>

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
<!-- <h2 class="addresourceheader">Simulate Kiosk Data</h2> -->
</div>
<%
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	UsersService as = Services.getService(UsersServiceImpl.class);
	
	// Get the domain Id
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
%>
<div id="doc3">
	<p>
		<br></br>
		<b>Welcome to the Kiosk Simulator!</b>
		<br></br><br></br>
		The Kiosk Simulator helps generate random transaction data (issues and receipts) for based on
		criteria that you specify. In particular, you can start with an initial stock, and control the spread of days over 
		which data is generated as well as parameters such as number of zero-stock days. Receipts are generated
		as soon as a material goes out of stock and cross the zero-stock days (also randomly determined within a given range).
		<br></br><br></br>
		The algorithmic computations run day, wherein demand forecast (D) and optimal order quantity (Q) are computed more frequently (say, every day), while consumption rate (c), order periodicity (P) and safety-stock (s) are computed relatively less frequently (say, every 7 days).
		<!-- 
		After you have generated data (as above), you can generate optimal order quantities by running the
		inventory optimization algorithm. This data can then be reviewed in the Demand Board.
		-->
	</p>
	<br></br>
	<div class="yui-g">
	<div class="yui-u first">
		<form id="simulatedata" class="sgForm">
				<button type="button" id="generatetransdata">Generate data</button>
				&nbsp;&nbsp;&nbsp;
				<button type="button" id="generateorders">Compute</button>
		</form>
	</div>
	<div class="yui-u">
	</div>
	</div>
	<br></br>
	<p>
	<b>Some precautions to be taken while running this tool:</b>
		<br></br>
		- Do not click the "Generate Data" button twice!
		<br></br>
		- Do not generate data more than necessary - it builds up!
		<br></br>
		- Generate data only ONCE for a given kiosk-material combination in a given period!
		<br></br>
	</p>
</div>
	
</div>

<jsp:include page="../pagefooter.html" />