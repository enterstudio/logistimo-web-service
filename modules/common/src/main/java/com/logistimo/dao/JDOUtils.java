package com.logistimo.dao;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import com.googlecode.charts4j.Slice;

import org.apache.commons.codec.binary.Hex;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.entity.IUploaded;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.PageParams;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;
import com.logistimo.utils.QueryUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 21/05/15.
 */
public class JDOUtils {

  private static final XLog xLogger = XLog.getLog(JDOUtils.class);
  private static final String DOT = ".";

  private static Map<Class, Class> _factory = new HashMap<Class, Class>();


  public static Class getImplClass(Class clazz) throws DaoException {
    Class instance = _factory.get(clazz);
    if (instance == null) {
      synchronized (clazz) {
        if (instance == null) {
          String
              implClazzName =
              clazz.getPackage().getName() + DOT + clazz.getSimpleName().substring(1);
          try {
            instance = Class.forName(implClazzName);
            _factory.put(clazz, instance);
          } catch (ClassNotFoundException e) {
            xLogger.severe("Error Loading JDO class {0}", implClazzName, e);
            throw new DaoException(e);
          }
        }
      }
    }
    return instance;
  }

  public static <T> T getObjectById(Class<T> clazz, Object key, boolean isReports) {
    PersistenceManager
        pm =
        isReports ? PMF.getReportsPM().getPersistenceManager() : PMF.get().getPersistenceManager();
    try {
      return getObjectById(clazz, key, pm);
    } finally {
      pm.close();
    }
  }

  public static <T> T getObjectById(Class<T> clazz, Object key) {
    return getObjectById(clazz, key, false);
  }

  public static <T> T getObjectById(Class<T> clazz, Object key, PersistenceManager pm) {
    return (T) pm.getObjectById(getImplClass(clazz), key);
  }


  public static <T> T createInstance(Class<T> clazz) {
    try {
      return (T) getImplClass(clazz).newInstance();
    } catch (Exception e) {
      xLogger.severe("Error creating instance for JDO class {0}", clazz.getName(), e);
      throw new DaoException(e);
    }
  }

  public static String createAccountKey(Long vId, Long cId, int year) {
    return vId + DOT + cId + DOT + year;
  }

  public static Long createAssetStatusKey(Long assetId, int mpId, String sId, int type) {
    return Hashing.murmur3_128()
        .hashString(assetId + "MPID" + mpId + "SID" + sId + type, Charsets.UTF_16LE).asLong();

  }

  public static String createUploadedKey(String filename, String version, String locale) {
    if (version != null && !version.isEmpty() && locale != null && !locale.isEmpty()) {
      return filename + IUploaded.SEPARATOR + version + IUploaded.SEPARATOR + locale;
    }
    return filename;
  }

  public static String createKioskLinkId(Long kioskId, String type, Long linkedKioskId) {
    return kioskId + DOT + type + DOT + linkedKioskId;
  }

  public static String createMessageLogKey(String jobId, String address) {
    if (jobId == null && address == null) {
      return null;
    }
    String key = jobId;
    if (address != null) {
      key += DOT + address;
    }
    return key;
  }

  public static String createSliceKey(Long domainId, Date d, String objectType, String objectId,
                                      String dt, String dv, String tz) {
    MessageDigest md = null;
    if (d != null && domainId != null && objectType != null && objectId != null && dt != null
        && dv != null) {
      try {
        Calendar cal = GregorianCalendar.getInstance();
        if (tz != null) {
          cal.setTimeZone(TimeZone.getTimeZone(tz));
        }
        cal.setTime(d);
        String timeStr = String.valueOf(cal.get(Calendar.DATE)) +
            String.valueOf(cal.get(Calendar.MONTH)) +
            String.valueOf(cal.get(Calendar.YEAR));
        md = MessageDigest.getInstance("MD5");
        md.update(timeStr.getBytes()); // date
        md.update(domainId.toString().getBytes()); // domain Id
        md.update(objectType.getBytes()); // object type
        md.update(objectId.getBytes()); // object Id
        md.update(dt.getBytes()); // dim. type
        md.update(dv.getBytes()); // dim. value
      } catch (NoSuchAlgorithmException e) {
        XLog.getLog(Slice.class).warn("Exception: No such algorithm: {0}", e.getMessage());
      }
    }
    if (md != null) {
      byte[] bytes = md.digest();
      if (bytes != null) {
        return new String(Hex.encodeHex(bytes));
      }
    }
    return null;
  }

  public static String createActiveCountsStatsStoreKey(Long domainId, Date d, String objectType,
                                                       String objectId, String dt, String dv) {
    MessageDigest md = null;
    if (d != null && domainId != null && objectType != null && objectId != null && dt != null
        && dv != null) {
      try {
        // Get the time string
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(d);
        String timeStr = String.valueOf(cal.get(Calendar.DATE)) +
            String.valueOf(cal.get(Calendar.MONTH)) +
            String.valueOf(cal.get(Calendar.YEAR));
        md = MessageDigest.getInstance("MD5");
        md.update(timeStr.getBytes()); // date
        md.update(domainId.toString().getBytes()); // domain Id
        md.update(objectType.getBytes()); // object type
        md.update(objectId.getBytes()); // object Id
        md.update(dt.getBytes()); // dim. type
        md.update(dv.getBytes()); // dim. value
      } catch (NoSuchAlgorithmException e) {
        XLog.getLog(Slice.class).warn("Exception: No such algorithm: {0}", e.getMessage());
      }
    }
    if (md != null) {
      byte[] bytes = md.digest();
      if (bytes != null) {
        return new String(Hex.encodeHex(bytes));
      }
    }
    return null;
  }

  public static String createShardedCounterKey(Long domainId, String name, int shardNo) {
    return domainId + DOT + name + DOT + shardNo;
  }

  public static String createMultipartMsgKey(String address, String id) {
    return address + DOT + id;
  }

  /**
   * Helper method to get full class name of the implemented JDO Class
   *
   * @param clazz JDO Interface
   * @return class and package name of the implemented JDO class
   */
  public static String getImplClassName(Class clazz) {
    return getImplClass(clazz).getName();
  }


  /**
   * Helper method to execute a JDO query in SQL and JQL.
   * @param qp
   * @param pm
   * @param pageParams
   * @return
   */
  public static <T> List<T> getMultiple(QueryParams qp, PersistenceManager pm,
                                        PageParams pageParams){
    List<T> results = null;
    Query q = getQuery(qp, pm, pageParams);
    try {
      results = (List<T>) getResults(qp, q);
      if(results != null){
        results = (List<T>) pm.detachCopyAll(results);
      }

    } finally {
      q.closeAll();
    }
    return results;
  }

  private static Object getResults(QueryParams qp, Query q) {
    Object results;
    if (qp.params != null && !qp.params.isEmpty()) {
      results = q.executeWithMap(qp.params);
    } else if (qp.listParams != null && !qp.listParams.isEmpty()) {
      results = q.executeWithArray(qp.listParams.toArray());
    } else {
      results = q.execute();
    }
    return results;
  }

  /**
   * Gets single result from Database
   * @param qp
   * @param pm
   * @param pageParams
   * @param <T>
   * @return
   */
  public static <T> T getSingle(QueryParams qp, PersistenceManager pm,
                                    PageParams pageParams){
    T result = null;
    Query q = getQuery(qp, pm, pageParams);
    q.setUnique(true);
    try {
      result = (T) getResults(qp,q);
      if(result != null){
        result = pm.detachCopy(result);
      }
    } finally {
      q.closeAll();
    }
    return result;
  }

  private static Query getQuery(QueryParams qp, PersistenceManager pm,
                                PageParams pageParams) {
    Query q;
    if (QueryParams.QTYPE.SQL.equals(qp.qType)) {
      String query = qp.query;
      if (query != null && pageParams != null && !query.contains(" LIMIT ")) {
        query +=
            " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams
                .getSize();
      }
      q = pm.newQuery("javax.jdo.query.SQL", query);
      q.setClass(JDOUtils.getImplClass(qp.qClazz));
    } else {
      q = pm.newQuery(qp.query);
      if (pageParams != null) {
        QueryUtil.setPageParams(q, pageParams);
      }
    }
    return q;
  }
}
