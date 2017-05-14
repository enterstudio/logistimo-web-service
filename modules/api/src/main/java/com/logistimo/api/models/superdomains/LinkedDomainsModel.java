package com.logistimo.api.models.superdomains;

import com.google.gson.annotations.Expose;

import com.logistimo.dao.JDOUtils;

import com.logistimo.domains.DomainLinkOptions;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

public class LinkedDomainsModel {

  private static final XLog xLogger = XLog.getLog(LinkedDomainsModel.class);

  @Expose
  private Long domainId; // domain Id
  @Expose
  private String domainName;
  @Expose
  private List<LinkedDomainModel> linkedDomains = null;
  @Expose
  private String cursor = null;
  @Expose
  private DomainLinkOptions options = null;

  public LinkedDomainsModel() {
  }

  ;

  public LinkedDomainsModel(Long domainId, String domainName, List<LinkedDomainModel> linkedDomains,
                            String cursor) {
    this.domainId = domainId;
    this.domainName = domainName;
    this.linkedDomains = linkedDomains;
    this.cursor = cursor;
  }

  @SuppressWarnings("unchecked")
  public LinkedDomainsModel(Long domainId, String domainName, Results domainLinks) {
    this.domainId = domainId;
    this.domainName = domainName;
    if (domainLinks == null) {
      return;
    }
    List<IDomainLink> links = domainLinks.getResults();
    if (links == null || links.isEmpty()) {
      return;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      linkedDomains = new ArrayList<>(links.size());
      for (IDomainLink dl : links) {
        try {
          IDomain linkedDomain = JDOUtils.getObjectById(IDomain.class, dl.getLinkedDomainId(), pm);
          linkedDomains.add(new LinkedDomainModel(dl, linkedDomain.getName()));
        } catch (Exception e) {
          xLogger.warn("{0}: Exception when getting domain {1}: {2}", e.getClass().getName(),
              dl.getLinkedDomainId(), e.getMessage(), e);
        }
      }
    } finally {
      pm.close();
    }
  }

  public long getDomainId() {
    return domainId;
  }

  public String getDomainName() {
    return domainName;
  }

  public List<LinkedDomainModel> getLinkedDomains() {
    return linkedDomains;
  }

  public String getCursor() {
    return cursor;
  }

  public int size() {
    return (linkedDomains != null ? linkedDomains.size() : 0);
  }

  public DomainLinkOptions getOptions() {
    return options;
  }

  public void setOptions(DomainLinkOptions options) {
    this.options = options;
  }

  public static class LinkedDomainModel {
    @Expose
    String key; // the unique key of this object
    @Expose
    private long ldId; // linked domain Id
    @Expose
    private String ldName; // linked domain name
    @Expose
    private int type; // type of relationship
    @Expose
    private long utcTime; // milliseconds

    public LinkedDomainModel() {
    }

    public LinkedDomainModel(String key, Long linkedDomainId, String linkedDomainName, int linkType,
                             long utcTimeMillis) {
      this.key = key;
      ldId = linkedDomainId;
      ldName = linkedDomainName;
      type = linkType;
      utcTime = utcTimeMillis;
    }

    public LinkedDomainModel(IDomainLink domainLink, String linkedDomainName) {
      key = domainLink.getKey();
      ldId = domainLink.getLinkedDomainId();
      ldName = linkedDomainName;
      type = domainLink.getType();
      utcTime = domainLink.getCreatedOn().getTime();
    }

    public String getKey() {
      return key;
    }

    public long getLinkedDomainId() {
      return ldId;
    }

    public String getLinkedDomainName() {
      return ldName;
    }

    public int getLinkType() {
      return type;
    }
  }
}
