<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.constants.Constants"%>
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
<h2 class="addresourceheader">Generate Orders</h2>
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
	///cal.add( Calendar.MONTH, -3 ); // 3 months back
	SimpleDateFormat start = new SimpleDateFormat( Constants.DATE_FORMAT );
	String strDate = start.format( cal.getTime() );	
%>
<script src="../../js/jquery-1.3.2.min.js" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">
function validateSubmit() {
	var select = document.getElementById('kioskid');
	var kid = select.options[ select.selectedIndex ].value;
	var kname = select.options[ select.selectedIndex ].text;
	var did = document.getElementById( 'domainid' ).value; 
	if ( kid == '' ) {
		alert( 'Please select an entity' );
		return;
	}
	var url = '/task/optimize?domainid=' + did + '&kioskid=' + kid;
	$.ajax( {
			url: url,
			dataType: 'html',
			success: function(o) { alert( 'Successfully submitted task for computing econmic order quantities for ' + kname ); },
			error: function(o) { alert( 'An error was encountered. ' + o.responseText ) }			
		});
}
</script>
<div id="doc3">
	<p>
		Compute quantities such as consumption rates, demand forecasts and optimal order recommendations based on transaction data (i.e. issues and receipts) for the specified entity. Optimal order quantities will be generated for all materials as per the run frequency specified below.
		Please select an Entity and click 'Compute now' to generate required computations. Computations will be as per the domain and entity configurations.
	</p>
	<br></br>
	<b>NOTE: For a given entity, optimal order quantities are computed ONLY if a relevant inventory model is selected for that entity.</b>
	<br></br>
	<b>NOTE: DO NOT click the 'Compute now' button more than once.</b>
	<br></br>
	<b>NOTE: Use this operation carefully, since it costs us CPU time.</b>
	
	<table id="addresource" width="100%" class="sgForm">
	<tr>
		<th class="mandatory">Entity</th>
		<td>
			<select id="kioskid" name="kioskid"/>
				<option value="">-- Select --</option>
				<%
					for ( IKiosk k : kiosks ) { %>
						<option value="<%= k.getKioskId() %>"><%= k.getName() %></option>
				 <% } %>
			</select>
			<input type="hidden" id="domainid" value="<%= domainId %>" />
			<input type="button" class="submit" value="Compute now" onclick="validateSubmit();"/>
		</td>
	</tr>
	</table>
	<br/>
</div>

<jsp:include page="../pagefooter.html" />