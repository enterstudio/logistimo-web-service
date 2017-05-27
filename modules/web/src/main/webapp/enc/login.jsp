<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.logistimo.config.models.LanguageConfig"%>
<%@page import="com.logistimo.config.models.ConfigurationException"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../s/pageheader.jsp">
	<jsp:param name="view" value="login" />
</jsp:include>
<%
	String status = request.getParameter( "status" );
	String lang = request.getParameter( "lang" );
	String redirectUrl = request.getParameter( "rurl" ); // URL to redirect to after login
	if ( lang == null || lang.isEmpty() )
		lang = "en";
	// Get the language configuration
	LanguageConfig lc = null;
	try {
		lc = LanguageConfig.getInstance();
	} catch ( ConfigurationException e ) {
		e.printStackTrace();
	}
	String prefix = ( SecurityMgr.isDevServer() ? "http://" : "https://" );
	String host = request.getServerName();
	if ( host.contains( "samaanguru.appspot.com" ) )
		host = "logistimo-web.appspot.com";
	String html5appurl = prefix + host + ( SecurityMgr.isDevServer() ? ":" + request.getServerPort() : "" ) + "/m/index.html";
%>
<!--  Interpret user's locale choice -->
<c:if test="${param['lang'] != null}">
	<fmt:setLocale value="${param['lang']}" scope="page"/>
</c:if>
<fmt:bundle basename="Messages">
<div id="addresource" class="sgForm" style="text-align:right;">
	<%
		// Get configured languages names
		List<String> langNames = null;
		if ( lc != null )
			langNames = lc.getNames();
	%>
	<b>Language</b>: <select name="locale" id="localeselect" onchange="location.href='login.jsp?lang='+this.options[this.selectedIndex].value;">
		<% if ( langNames != null ) { 
				Iterator<String> lit = langNames.iterator();
				while ( lit.hasNext() ) {
					String name = lit.next();
					String code = lc.getCode( name );
			%>
					<option value="<%= code %>" <%= code.equals( lang ) ? "selected" : "" %>><%= name %></option>
			<%  } // end while 
			   } // end if (langNames...)
			%>	
	</select>
</div>
<div id="doc3">
<!-- Message/title style class -->
<c:set var="msgClass" value="sgHeading"/>
<fmt:message key="login.welcome" var="message" />
<fmt:message key="login.error" var="errorMessage" />
<fmt:message key="logout.message" var="logoutMessage" />
<fmt:message key="login.browsercheck" var="browserCheckMessage" />
<!--  Check for login error - invalid user name/password -->
<c:if test="${param['status'] == '1'}">
	<c:set var="message" value="${errorMessage}" />
	<c:set var="msgClass" value="sgError" />
</c:if>
<!--  Check for login error - already logged in as another user from this browser -->
<c:if test="${param['status'] == '2'}">
	<c:set var="message" value="You are already logged in as another user from this browser. Please logout and retry." />
	<c:set var="msgClass" value="sgError" />
</c:if>
<!--  Check for login error - account is disabled -->
<c:if test="${param['status'] == '3'}">
	<c:set var="message" value="Your account is disabled." />
	<c:set var="msgClass" value="sgError" />
</c:if>
<!--  Check for login error - access denied -->
<c:if test="${param['status'] == '4'}">
	<c:set var="message" value="Access denied. You are not authorized to use this application." />
	<c:set var="msgClass" value="sgError" />
</c:if>
<!--  Check for login error - system error -->
<c:if test="${param['status'] == '5'}">
	<c:set var="message" value="A system error was encountered. Please retry after sometime, or contact your system administrator." />
	<c:set var="msgClass" value="sgError" />
</c:if>
<!--  Valid logout state -->
<c:if test="${param['status'] == '0'}">
	<c:set var="message" value="${logoutMessage}" />
</c:if>
<h1 class="${msgClass}">${message}</h1>
<br>
<script type="text/javascript">
// Function to set focus on the user name field
window.onload = function() {
	if ( document.loginForm )
		document.loginForm.username.focus();
}

// Check if the browser is Internet Explorer
function checkIE()
{
  return ( navigator.appName == 'Microsoft Internet Explorer' );
}

// Display info. if IE is the browser
if ( checkIE() )
	alert( "${browserCheckMessage}" );
</script>
<% if ( status == null || !status.equals( "4" ) ) {  // access not denied %>
<table>
<tr>
	<td width="80%">
	<fmt:message key="login.prompt" />
	<br>
		<form id="login" name="loginForm" class="sgForm" action="/enc/authenticate" method="POST">
		  	<table id="addresource" width="100%" align="center">
		  	  <tr>
				<th><fmt:message key="user.id" />:</th>
				<td><input type="text" id="username" name="username" onload="this.focus()"/></td>
			  </tr>
			  <tr>
				<th><fmt:message key="login.password" />:</th>
				<td><input type="password" id="addnewkiosk" name="password"/></td>
			  </tr>
			  <tr >
			   	<td></td><td><button type="submit" id="loginbutton" name="submit"><fmt:message key="login" /></button></td>
			  </tr>
			</table>
			<input type="hidden" name="action" value="li" />
			<% if ( redirectUrl != null && !redirectUrl.isEmpty() ) { %>
			<input type="hidden" name="rurl" value="<%= redirectUrl %>" />
			<% } %>
		</form>
	</td>
</tr>
</table>
<br/><br/><fmt:message key="login.useoperatorconsole1" /> <a href="<%= html5appurl %>"><fmt:message key="login.useoperatorconsole2" /></a>.
<% } else { 
	%>
	<fmt:message key="login.loginfrom" /> <a href="<%= html5appurl %>"><fmt:message key="login.useoperatorconsole2" /></a>.
<% } %>
<br></br>
</div>
</fmt:bundle>
<jsp:include page="../s/pagefooter.html" />
