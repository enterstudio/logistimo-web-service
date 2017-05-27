<%@page contentType="application/json; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.config.models.ReportsConfig" %>
<%@page import="com.logistimo.pagination.PageParams" %>
<%@page import="com.logistimo.reports.generators.ReportData"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.api.security.SecurityMgr" %>
<%@page import="com.logistimo.domains.service.impl.DomainsServiceImpl"%>
<%@ page import="com.logistimo.inventory.service.impl.InventoryManagementServiceImpl"%>
<%@ page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
<%@ page import="com.logistimo.api.util.SearchUtil"%>
<%@ page import="com.logistimo.api.util.SessionMgr"%>
<%@ page import="com.logistimo.utils.StringUtil"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Locale"%><%@ page import="com.logistimo.services.Services"%><%@ page import="com.logistimo.users.entity.IUserAccount"%><%@ page import="com.logistimo.users.service.UsersService"%><%@ page import="com.logistimo.users.service.impl.UsersServiceImpl"%><%@ page import="com.logistimo.entities.service.EntitiesService"%><%@ page import="com.logistimo.entities.service.EntitiesServiceImpl"%><%@ page import="com.logistimo.materials.service.MaterialCatalogService"%><%@ page import="com.logistimo.reports.ReportsConstants"%><%@ page import="com.logistimo.auth.SecurityConstants"%><%@ page import="com.logistimo.domains.service.DomainsService"%><%@ page import="com.logistimo.domains.entity.IDomain"%><%@ page import="com.logistimo.domains.entity.IDomainLink"%><%@ page import="com.logistimo.inventory.service.InventoryManagementService"%><%@ page import="com.logistimo.entities.entity.IKiosk"%><%@ page import="com.logistimo.entities.entity.IUserToKiosk"%><%@ page import="com.logistimo.materials.entity.IMaterial"%><%@ page import="com.logistimo.entities.entity.IPoolGroup"%><%@ page import="com.logistimo.entities.entity.IKioskLink"%>
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
	// Request parameters
	String type = request.getParameter( "type" );
	String search = request.getParameter( "search" ); // passed in by TextboxList Autocomplete, if used
	boolean isTokenInput = ( request.getParameter( "q") != null );
	if ( isTokenInput )
		search = request.getParameter( "q" );
	if ( search != null && search.trim().isEmpty() )
		search = null;
	if ( search != null )
		search = search.trim();
	String role = request.getParameter( "role" ); // for getting user list only; get all users of given role
	String offsetStr = request.getParameter( "o" );
	String sizeStr = request.getParameter( "s" );
	String cursor = request.getParameter( "c" );
	// Get type-specific parameters
	String kioskIdStr = request.getParameter( "kioskid" ); // required for type kiosklinks
	String linkType = request.getParameter( "linktype" ); // required for type kiosklinks
	// User Id (for userkiosks)
	String userIdPassedIn = request.getParameter( "userid" ); // required for userkiosks
	// Check if the referrer URL is public demand board; if so, check permissions
	String referrer = request.getHeader( "Referer" );
	boolean accessDenied = true;
	boolean isPublicDemandBoard = false;
	Long domainId = null;
	Locale locale = null;
	String userId = null;
	if ( referrer != null && referrer.contains( "/pub/demand" ) ) {
		isPublicDemandBoard = true;
		String domainIdStr = request.getParameter( "pdbdomainid" );
		if ( domainIdStr != null && !domainIdStr.isEmpty() ) {
			domainId = Long.valueOf( domainIdStr );
			DomainConfig dc = DomainConfig.getInstance( domainId );
			locale = dc.getLocale();
			accessDenied = !dc.isDemandBoardPublic();
		}
	}
	if ( isPublicDemandBoard && accessDenied ) {
		System.out.println( "Access denied" );
		return;
	}
	if ( cursor != null && cursor.isEmpty() )
		cursor = null;
	// Logged in user details
	String loggedInUserRole = null;
	if ( !isPublicDemandBoard ) {
		SecureUserDetails sUser = SecurityMgr.getUserDetails( request.getSession() );
		userId = sUser.getUsername();
		locale = sUser.getLocale();
		loggedInUserRole = sUser.getRole();
		domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	}
	// Get the pagination params
	int offset = 1;
	if ( offsetStr != null && !offsetStr.isEmpty() )
		offset = Integer.parseInt( offsetStr );
	int size = PageParams.DEFAULT_SIZE;
	if ( sizeStr != null && !sizeStr.isEmpty() )
		size = Integer.parseInt( sizeStr );
	PageParams pageParams = new PageParams( cursor,offset-1, size );
	// Get the lists
	UsersService as = null;
	EntitiesService es = null;
	List items = null;
	try {
		if ( "kiosks".equals( type ) ) {
			as = Services.getService( UsersServiceImpl.class );
			es = Services.getService( EntitiesServiceImpl.class );
			IUserAccount u = as.getUserAccount( userId );
			if ( search != null )
				items = SearchUtil.findKiosks( domainId, search, pageParams, u ).getResults();
			else
				items = es.getKiosks( u, domainId, null, pageParams ).getResults();
		} else if ( "userkiosks".equals( type ) ) {
			as = Services.getService( UsersServiceImpl.class );
			es = Services.getService( EntitiesServiceImpl.class );
			IUserAccount u = as.getUserAccount( userIdPassedIn );
			// Bug fix: When setting primary entity in the edit kiosk owner form, the entities for the user were being shown erratically or not shown at all.
			// This bug is fixed by adding the search != null block below.
			if ( search != null )
				items = SearchUtil.findKiosks( domainId, search, pageParams, u ).getResults();
			else
				items = es.getKiosksForUser( u, null, pageParams ).getResults();
		} else if ( "materials".equals( type ) ) {
			MaterialCatalogService mcs = Services.getService( MaterialCatalogServiceImpl.class );
			if ( search != null )
				items = SearchUtil.findMaterials( domainId, search, pageParams ).getResults();
			else
				items = mcs.getAllMaterials( domainId, null, pageParams ).getResults();
		} else if ( "users".equals( type ) ) {
			as = Services.getService( UsersServiceImpl.class );
			IUserAccount u = as.getUserAccount( userId );
			if ( role != null && !role.isEmpty() ) // get active users of a given role
				items = as.getUsers( domainId, role, true, search, pageParams ).getResults();
			else // get users visible to the logged in user
				items = as.getUsers( domainId, u, true, search, pageParams ).getResults();
		} else if ( "superusers".equals( type ) ) {
			as = Services.getService( UsersServiceImpl.class );
			items = as.getSuperusers();
		} else if ( "poolgroups".equals( type ) ) {
			es = Services.getService( EntitiesServiceImpl.class );
			items = es.findAllPoolGroups( domainId, 0, 1000 ); // TODO: pagination
		} else if ( "kiosklinks".equals( type ) ) {
			es = Services.getService( EntitiesServiceImpl.class );
			Long kioskId = Long.valueOf( kioskIdStr );
			if ( search != null )
				items = SearchUtil.findLinkedKiosks( kioskId, linkType, search, pageParams ).getResults();
			else
				items = es.getKioskLinks( kioskId, linkType, null, null, pageParams ).getResults();
		} else if ( ReportsConstants.FILTER_COUNTRY.equals( type ) || ReportsConstants.FILTER_STATE.equals( type ) ||
					ReportsConstants.FILTER_DISTRICT.equals( type ) || ReportsConstants.FILTER_TALUK.equals( type ) ||
					ReportsConstants.FILTER_CITY.equals( type ) || ReportsConstants.FILTER_PINCODE.equals( type ) ) {
			ReportsConfig rconfig = ReportsConfig.getInstance( domainId );
			items = rconfig.getFilterValues( type );
		} else if ( "domains".equals( type ) && SecurityConstants.ROLE_SUPERUSER.equals( loggedInUserRole ) ) {
			DomainsService domainsService = Services.getService( DomainsServiceImpl.class );
			if ( search != null )
				items = SearchUtil.findDomains( search, pageParams ).getResults();
			else
				items = domainsService.getAllDomains( pageParams ).getResults();
		} else if ( "ktag".equals( type ) ) {
			DomainConfig dc = DomainConfig.getInstance( domainId );
			items = StringUtil.getList( dc.getKioskTags() );
		} else if ( "mtag".equals( type ) ) {
			DomainConfig dc = DomainConfig.getInstance( domainId );
			items = StringUtil.getList( dc.getMaterialTags() );
		} else if ( "linkeddomains".equals( type ) && SecurityConstants.ROLE_SUPERUSER.equals( loggedInUserRole ) ) {
			String domainIdStr = request.getParameter( "domainid" );
			String linkTypeStr = request.getParameter( "linktype" );
			DomainsService ds = Services.getService( DomainsServiceImpl.class );
			List<IDomain> itemsFromDb = null;
			if ( search != null )
				itemsFromDb = SearchUtil.findDomains( search, pageParams ).getResults();
			else
				itemsFromDb = ds.getAllDomains( pageParams ).getResults();
			// Ensure that the existing linked domains are not part of this list
			if ( itemsFromDb != null && !itemsFromDb.isEmpty() && domainIdStr != null && linkTypeStr != null ) {
				domainId = Long.valueOf( domainIdStr );
				items = new ArrayList<IDomain>( itemsFromDb.size() );
				items.addAll( itemsFromDb );
				// Remove current domain from the list, if any
				Iterator<IDomain> itDomains = items.iterator();
				while ( itDomains.hasNext() ) {
					Long dId = itDomains.next().getId();
					if ( dId.equals( domainId ) || ds.hasDomainLinks( dId, IDomainLink.TYPE_PARENT ) ) // if it the current domain or has a parent, then remove from the list (NOTE: at this time, one domain has only one child)
						itDomains.remove();
				}
			}
		} else if ( "vendors".equals( type ) ) {
			DomainConfig dc = DomainConfig.getInstance( domainId );
			items = dc.getAssetConfig().getVendorIds();
		}
	} catch ( Exception e ) {
		System.out.println( "Exception " + e.getClass().getName() + " when getting list of type " + type + ": " + e.getMessage() );
	}
%>
<% if ( items == null || items.isEmpty() ) { %>
[]
<% } else {
	%>
	[
	<%
	Iterator it = items.iterator();
	while ( it.hasNext() ) {
		Object o = it.next();
		String id = "", name = "";
		if ( o instanceof IKiosk ) {
			id = ((IKiosk)o).getKioskId().toString();
			name = ((IKiosk)o).getName();
		} else if ( o instanceof IUserToKiosk ) {
			Long kId = ((IUserToKiosk)o).getKioskId();
			id = kId.toString();
			name = "";
			try {
				name = Services.getService(EntitiesServiceImpl.class).getKiosk(kId, false).getName();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		} else if ( o instanceof IMaterial ) {
			id = ((IMaterial)o).getMaterialId().toString();
			name = ((IMaterial)o).getName();
		} else if ( o instanceof IUserAccount ) {
			id = ((IUserAccount)o).getUserId();
			name = ((IUserAccount)o).getFullName() + " [" + ((IUserAccount)o).getUserId() + "]";
		} else if ( o instanceof IPoolGroup ) {
			id = ((IPoolGroup)o).getGroupId().toString();
			name = ((IPoolGroup)o).getName();
		} else if ( o instanceof IKioskLink ) {
			try {
				id = ((IKioskLink)o).getLinkedKioskId().toString();
				name = Services.getService(EntitiesServiceImpl.class).getKiosk( ((IKioskLink)o).getLinkedKioskId(), false ).getName();
			} catch ( Exception e ) {
				System.out.println( e.getClass().getName() + " when getting linked-kiosk " + ((IKioskLink)o).getLinkedKioskId() + " for kiosk " + kioskIdStr + ": " + e.getMessage() );
			}
		} else if ( o instanceof String ) {
			id = (String) o;
			name = (String) o;
		} else if ( o instanceof IDomain ) {
			id = ((IDomain)o).getId().toString();
			name = ((IDomain)o).getName();
		}
		if ( isTokenInput ) {
		%>
		{ "id": "<%= id %>", "name" : "<%= name %>" }<%= it.hasNext() ? "," : "" %>
		<% } else { %>
		["<%= id %>","<%= name %>",null,null]<%= it.hasNext() ? "," : "" %>
		<%
		} // end if ( isTokenInput )
	}
	%>
	]
<% } // end else %>