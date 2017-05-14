<%@page contentType="text/html; charset=UTF-8" language="java"%>
<%@ page import="com.logistimo.services.blobstore.BlobstoreService" %>
<%@page import="com.logistimo.entity.Uploaded" %>
<%@ page import="com.logistimo.AppFactory" %>
<%@ page import="com.logistimo.entity.IUploaded" %>
<%
    BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
%>

<html>
<head>
<script type="text/javascript">
function validateForm()
{
	var alertStr = "Please enter the ";
	var isValid = true;
	var x="";
	x = document.getElementById("jadfile").value;
	if(x==null || x=="") {
		alertStr = alertStr + "Jad File ";
		isValid = isValid && false;
	}
	
	x = document.getElementById("jarfile").value;
	if(x==null || x=="") {
		alertStr = alertStr + "Jar File ";
		isValid = isValid && false;
	}
		
	x = document.forms["apppublish"]["version"].value;
	
	if (x==null || x=="") {
	  alertStr = alertStr + "Version ";
	  isValid = isValid && false;
	}
		
	x=document.forms["apppublish"]["locale"].value;
	if( x== null || x == "" ) {
		alertStr = alertStr + "Locale ";
		isValid = isValid && false;
	}
	
	x = x=document.forms["apppublish"]["description"].value;
	if( x == null || x == "" ) {
		alertStr = alertStr + "Description ";
		isValid = isValid && false;
	}
	
	x = x=document.forms["apppublish"]["userid"].value;
	if( x == null || x == "" ) {
		alertStr = alertStr + "User ID";
		isValid = isValid && false;
	}
	
	if( !isValid )
	alert(alertStr);
	return isValid;
}

function check(val) {
	var id = val.id;
	var ext = val.value;
	ext = ext.substring(ext.length-3,ext.length);
	ext = ext.toLowerCase();
	if(id == "jadfile" && ext == "jad") {
	    return true;
	}
	if( id == "jarfile" && ext =="jar" ) {
		return true;
	}
	alert("Please upload correct " + id);
	val.value=null;
	val.focus();
    return false; 
}
</script>
</head>
<body>
<p>&nbsp;</p>
<!--  <form name="apppublish"  method="post" action="/apppublish"> <!-- enctype="multipart/form-data"> -->
<form name = "apppublish" action="<%= blobstoreService.createUploadUrl("/dev/publishapp") %>"  method="post"  enctype="multipart/form-data" onsubmit="return validateForm()">
<p><b>Please use this form to upload the jad and jar files</b></p>
<table border="0" cellpadding="0" width="100%">
<tr>
<th width="5%">Jad File</th>
<td><input type="file" id="jadfile" name="<%= IUploaded.JADFILE %>" accept="text/vnd.sun.j2me.app-descriptor" onchange="check(this)" size="25"/></td>
</tr>
<tr>
<th width="5%">Jar File</th>
<td><input type="file" id="jarfile" name="<%= IUploaded.JARFILE %>" accept="application/java-archive" onchange="check(this)" size="25"/></td>
</tr>
<tr>
<th width="5%">Version</th>
<td><input type="text" name="version" size="25"/></td>
</tr>
<tr>
<th width="5%">Locale</th>
<td><input type="text" name="locale" size="25"/></td>
<tr>
<th width="5%">Description</th>
<td><input type="text" name="description" size="25"/></td>
</tr>
<tr>
<th width="5%">User ID</th>
<td><input type="text" name="userid" size="25"/></td>
</tr>
</table>

<input type="submit" value="Submit"/>
</form>
</body>
</html>
