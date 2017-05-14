package com.logistimo.api.servlets.mobile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.logistimo.api.servlets.JsonRestServlet;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;

import com.logistimo.api.models.superdomains.LinkedDomainsModel;
import com.logistimo.api.models.superdomains.LinkedDomainsModel.LinkedDomainModel;
import com.logistimo.domains.ObjectsToDomainModel;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.domains.processor.ObjectsToDomainProcessor;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DomainsServlet extends JsonRestServlet {
  private static final XLog xLogger = XLog.getLog(DomainsServlet.class);

  private static final String ACTION_GETLINKEDDOMAINS = "getlinkeddomains";
  private static final String ACTION_ADDLINKEDDOMAINS = "addlinkeddomains";
  private static final String ACTION_REMOVELINKEDDOMAINS = "removelinkeddomains";
  private static final String ACTION_ADDOBJECTSTODOMAIN = "addobjectstodomain";
  private static final String ACTION_REMOVEOBJECTSFROMDOMAIN = "removeobjectsfromdomain";

  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException,
      ServiceException {
    String action = request.getParameter("action");
    if (ACTION_GETLINKEDDOMAINS.equals(action)) {
      getLinkedDomains(request, response, backendMessages, messages);
    } else if (ACTION_ADDLINKEDDOMAINS.equals(action)) {
      addLinkedDomains(request, response, backendMessages, messages);
    } else if (ACTION_REMOVELINKEDDOMAINS.equals(action)) {
      removeLinkedDomains(request, response, backendMessages, messages);
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
  }

  @Override
  protected void processPost(HttpServletRequest request,
                             HttpServletResponse response, ResourceBundle backendMessages,
                             ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    String action = request.getParameter("action");
    if (ACTION_ADDLINKEDDOMAINS.equals(action)) {
      addLinkedDomains(request, response, backendMessages, messages);
    } else if (ACTION_REMOVELINKEDDOMAINS.equals(action)) {
      removeLinkedDomains(request, response, backendMessages, messages);
    } else if (ACTION_ADDOBJECTSTODOMAIN.equals(action)) {
      addRemoveObjectsToDomain(request, response, backendMessages, messages,
          ObjectsToDomainModel.ACTION_ADD);
    } else if (ACTION_REMOVEOBJECTSFROMDOMAIN.equals(action)) {
      addRemoveObjectsToDomain(request, response, backendMessages, messages,
          ObjectsToDomainModel.ACTION_REMOVE);
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
  }

  /**
   * Get linked domains (returns a JSON)
   */
  private void getLinkedDomains(HttpServletRequest req, HttpServletResponse resp,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered getLinkedDomains");

    String domainIdStr = req.getParameter("domainid");
    String linkTypeStr = req.getParameter("type");
    String sizeStr = req.getParameter("size");
    String cursor = req.getParameter("cursor");

    if (domainIdStr == null || domainIdStr.isEmpty()) {
      throw new IllegalArgumentException("Invalid domain ID");
    }

    Long domainId = null;
    int linkType = IDomainLink.TYPE_CHILD;
    int size = PageParams.DEFAULT_SIZE;
    try {
      if (domainIdStr != null) {
        domainId = Long.valueOf(domainIdStr);
      }
      if (linkTypeStr != null) {
        linkType = Integer.parseInt(linkTypeStr);
      }
      if (sizeStr != null) {
        size = Integer.parseInt(sizeStr);
      }
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }
    // Get the linked domains, and send back JSON
    try {
      DomainsService as = Services.getService(DomainsServiceImpl.class);
      Results results = as.getDomainLinks(domainId, linkType, new PageParams(cursor, size));
      IDomain d = as.getDomain(domainId);
      Gson gson = new GsonBuilder().create();
      sendJsonResponse(resp, 200,
          gson.toJson(new LinkedDomainsModel(domainId, d.getName(), results)));
    } catch (Exception e) {
      xLogger.severe("{0}: {1}", e.getClass().getName(), e.getMessage());
      sendJsonResponse(resp, 500, "");
    }

    xLogger.fine("Exiting getLinkedDomains");
  }

  private void addLinkedDomains(HttpServletRequest req, HttpServletResponse resp,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
                /*xLogger.fine( "Entered addLinkedDomains" );
                String data = req.getParameter( "data" );
		xLogger.fine( "data: {0}", data );
		xLogger.fine( "input stream: {0}", getJsonInput( req ) );
		try {
			// Get the JSON content
			Gson gson = new GsonBuilder().create();
			LinkedDomainsModel ldm = gson.fromJson( data, LinkedDomainsModel.class );
			int numLinks = ldm.size();
			if ( numLinks == 0 )
				throw new ServiceException( "No linked domains to add" );
			// Get the domain links list
			List<IDomainLink> domainLinks = new ArrayList<IDomainLink>( numLinks );
			Long domainId = ldm.getDomainId();
			String domainName = ldm.getDomainName();
			Date now = new Date();
			Iterator<LinkedDomainModel> it = ldm.getLinkedDomains().iterator();
			while ( it.hasNext() ) {
				LinkedDomainModel ld = it.next();
				domainLinks.add(JDOUtils.createInstance(IDomainLink.class).init( domainId, domainName, ld.getLinkedDomainId(), ld.getLinkedDomainName(), ld.getLinkType(), now ) );
			}
			// Add domain links
			DomainsService ds = Services.getService( DomainsServiceImpl.class );
			//ds.addDomainLinks( domainLinks, ldm.getOptions() );
		} catch ( Exception e ) {
			xLogger.severe( "{0}: {1}", e.getClass().getName(), e.getMessage() );
			resp.setStatus( 500 ); // error
		}
		xLogger.fine( "Exiting addLinkedDomains" );*/
    xLogger.warn("Old UI don't support adding of linked domain. Please use new UI.");
    resp.setStatus(500); // error
  }

  private void removeLinkedDomains(HttpServletRequest req, HttpServletResponse resp,
                                   ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered removeLinkedDomains");
    String data = req.getParameter("data");
    try {
      // Get the JSON content
      Gson gson = new GsonBuilder().create();
      LinkedDomainsModel ldm = gson.fromJson(data, LinkedDomainsModel.class);
      int numLinks = ldm.size();
      if (numLinks == 0) {
        throw new ServiceException("No linked domains to remove");
      }
      // Get the domain links list
      List<String> domainLinkKeys = new ArrayList<String>(numLinks);
      Iterator<LinkedDomainModel> it = ldm.getLinkedDomains().iterator();
      while (it.hasNext()) {
        domainLinkKeys.add(it.next().getKey());
      }
      // Remove domain links
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      ds.deleteDomainLinks(domainLinkKeys);
      // Send positive response
      resp.setStatus(200); // done
    } catch (Exception e) {
      xLogger.severe("{0}: {1}", e.getClass().getName(), e.getMessage());
      resp.setStatus(500); // error
    }
    xLogger.fine("Exiting removeLinkedDomains");
  }

  // Add objects of any kind to a domain. Objects can be identified by specific IDs or it could be query based
  private void addRemoveObjectsToDomain(HttpServletRequest req, HttpServletResponse resp,
                                        ResourceBundle backendMessages, ResourceBundle messages,
                                        int action) throws ServletException, IOException {
    xLogger.fine("Entered addRemoveObjectsToDomain");
    String jsonData = req.getParameter("data");
    try {
      // Get Domains Service
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      // Get the input data object
      ObjectsToDomainModel aotdm = new Gson().fromJson(jsonData, ObjectsToDomainModel.class);
      List<Long> domainIds = aotdm.getDomainIds();
      String className = aotdm.getClassName();
      Class<?> clazz = Class.forName(className);
      // Check if objects are to be added by objects Ids
      if (aotdm.hasObjectIds()) {
        List<Object> objectIds = aotdm.getObjectIds();
        if (domainIds == null || domainIds.isEmpty() || clazz == null) {
          xLogger.severe("Either domainIds or Class was not specified");
        } else if (action == ObjectsToDomainModel.ACTION_ADD) {
          ds.addObjectsToDomains(objectIds, clazz, domainIds);
        } else if (action == ObjectsToDomainModel.ACTION_REMOVE) {
          ds.removeObjectsFromDomains(objectIds, clazz, domainIds);
        }
      } else { // check if query exists
        String queryStr = aotdm.getQueryString();
        Map<String, Object> params = aotdm.getQueryParams();
        Long sourceDomainId = aotdm.getSourceDomainId();
        if (queryStr == null || queryStr.isEmpty() || clazz == null || sourceDomainId == null) {
          xLogger.severe("Query string or class or source domain Id not specified");
        } else {
          // Send the clazz and the target domain Id
          ObjectsToDomainModel
              aotdParam =
              new ObjectsToDomainModel(action, domainIds, className, null);
          String jsonParam = new Gson().toJson(aotdParam);
          // Execute a add-to-domain processor on the paged query results
          PagedExec.exec(sourceDomainId, new QueryParams(queryStr, params),
              new PageParams(null, PageParams.DEFAULT_SIZE),
              ObjectsToDomainProcessor.class.getName(), jsonParam, null);
        }
      }
    } catch (Exception e) {
      xLogger.severe("{0}: {1}", e.getClass().getName(), e.getMessage());
    }
    // NOTE: Given this command is typically run as a task, there is no error response; else, the task can retry infinitely
    xLogger.fine("Exiting addRemoveObjectsToDomain");
  }
}
