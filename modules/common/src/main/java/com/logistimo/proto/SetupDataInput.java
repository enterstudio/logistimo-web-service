/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author vani
 */
public class SetupDataInput extends InputMessageBean implements JsonBean {
  public static String SEPARATOR = ";";
  protected String cmd = null;
  private Hashtable user = new Hashtable();
  private Hashtable kiosk = new Hashtable();
  private String type = null;

  public SetupDataInput() {
    cmd = RestConstantsZ.ACTION_CREATEUSERKIOSK;
  }

  // Constructor with params
  public SetupDataInput(String type, String userId, String password, Hashtable user,
                        Hashtable kiosk, String version) {
    super(userId, password, version);
    this.type = type;
    this.user = user;
    this.kiosk = kiosk;
    cmd = RestConstantsZ.ACTION_CREATEUSERKIOSK;
  }

  // Accessor methods
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Hashtable getUser() {
    return this.user;
  }

  public void setUser(Hashtable user) {
    this.user = user;
  }

  public Hashtable getKiosk() {
    return this.kiosk;
  }

  public void setKiosk(Hashtable kiosk) {
    this.kiosk = kiosk;
  }


  // Load from a message string
  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("No message specified");
    }
  }

  // Convert to a set of message strings
  public Vector toMessageString() throws ProtocolException {
    return null;
  }

  // Load from JSON
  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      // Read version
      version = (String) jsonObject.get(JsonTagsZ.VERSION);
      // Load the user object from the json
      try {
        loadUserData((JSONObject) jsonObject.get(JsonTagsZ.USER));
      } catch (Exception e) {
        // ignore
      }
      // Load the entity object from the json
      try {
        loadKioskData((JSONObject) jsonObject.get(JsonTagsZ.KIOSK));
      } catch (Exception e) {
        // ignore
      }
    } catch (JSONException je) {
      throw new ProtocolException(je.getMessage());
    }
  }

  // Convert to JSON String
  // Create a JSON object in the newer (01) format
  public String toJSONString() throws ProtocolException {
    try {
      JSONObject json = new JSONObject();
      // Add version
      json.put(JsonTagsZ.VERSION, this.version);
      // Add user data
      if (user != null && !user.isEmpty()) {
        addUserData(json);
      }
      // Add kiosk data
      if (kiosk != null && !kiosk.isEmpty()) {
        addKioskData(json);
      }
      return json.toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }

  }

  private void loadUserData(JSONObject json) throws JSONException {
    // Read the json object and populate the user hashtable with the values.
    user.put(JsonTagsZ.USER_ID, json.get(JsonTagsZ.USER_ID));
    user.put(JsonTagsZ.PASSWORD, json.get(JsonTagsZ.PASSWORD));
    user.put(JsonTagsZ.ROLE, json.get(JsonTagsZ.ROLE));
    user.put(JsonTagsZ.FIRST_NAME, json.get(JsonTagsZ.FIRST_NAME));
    try {
      user.put(JsonTagsZ.LAST_NAME, json.get(JsonTagsZ.LAST_NAME)); // last name is optional
    } catch (JSONException je) {
      // do nothing
    }
    user.put(JsonTagsZ.MOBILE, json.get(JsonTagsZ.MOBILE));
    String userRole = (String) user.get(JsonTagsZ.ROLE);
    // If role is EntityManager or above email is mandatory. Otherwise it's optional
    // Currently, this API will only support users with role Entity operator and Entity manager.
    if (userRole != null && !userRole.equals("")) {
      if (userRole.equals(RestConstantsZ.ROLE_SERVICEMANAGER)) {
        user.put(JsonTagsZ.EMAIL, json.get(JsonTagsZ.EMAIL));
      } else {
        try {
          user.put(JsonTagsZ.EMAIL, json.get(JsonTagsZ.EMAIL));
        } catch (JSONException je) {
          // do nothing
        }
      }
    }
    user.put(JsonTagsZ.COUNTRY, json.get(JsonTagsZ.COUNTRY));
    user.put(JsonTagsZ.LANGUAGE, json.get(JsonTagsZ.LANGUAGE));
    user.put(JsonTagsZ.TIMEZONE, json.get(JsonTagsZ.TIMEZONE));
    // Gender is optional
    try {
      user.put(JsonTagsZ.GENDER, json.get(JsonTagsZ.GENDER));
    } catch (JSONException je) {
      // do nothing
    }
    // Age is optional
    try {
      user.put(JsonTagsZ.AGE, json.get(JsonTagsZ.AGE));
    } catch (JSONException je) {
      // do nothing
    }
    // landline number is optional
    try {
      user.put(JsonTagsZ.LANDLINE, json.get(JsonTagsZ.LANDLINE));
    } catch (JSONException je) {
      // do nothing
    }
    // state is optional
    try {
      user.put(JsonTagsZ.STATE, json.get(JsonTagsZ.STATE));
    } catch (JSONException je) {
      // do nothing
    }
    // district is optional
    try {
      user.put(JsonTagsZ.DISTRICT, json.get(JsonTagsZ.DISTRICT));
    } catch (JSONException je) {
      // do nothing
    }
    // taluk is optional
    try {
      user.put(JsonTagsZ.TALUK, json.get(JsonTagsZ.TALUK));
    } catch (JSONException je) {
      // do nothing
    }
    // city is optional
    try {
      user.put(JsonTagsZ.CITY, json.get(JsonTagsZ.CITY));
    } catch (JSONException je) {
      // do nothing
    }
    // street address is optional
    try {
      user.put(JsonTagsZ.STREET_ADDRESS, json.get(JsonTagsZ.STREET_ADDRESS));
    } catch (JSONException je) {
      // do nothing
    }
    // pin code is optional
    try {
      user.put(JsonTagsZ.PINCODE, json.get(JsonTagsZ.PINCODE));
    } catch (JSONException je) {
      // do nothing
    }
  }

  private void loadKioskData(JSONObject json) throws JSONException {
    kiosk.put(JsonTagsZ.NAME, json.get(JsonTagsZ.NAME));
    // Read the JSONArray containing the user ids of the users. Convert it into comma
    // separated string values and store it in the entity hashtable
    JSONArray usrsArray = json.getJSONArray(JsonTagsZ.USERS);
    String usr = "";
    for (int i = 0; i < usrsArray.length(); i++) {
      usr = usr + usrsArray.getString(i) + (i < usrsArray.length() ? SEPARATOR : "");
    }
    kiosk.put(JsonTagsZ.USERS, usr);

    kiosk.put(JsonTagsZ.COUNTRY, json.get(JsonTagsZ.COUNTRY));
    kiosk.put(JsonTagsZ.STATE, json.get(JsonTagsZ.STATE));
    kiosk.put(JsonTagsZ.CITY, json.get(JsonTagsZ.CITY));
    // latitude is optional
    try {
      kiosk.put(JsonTagsZ.LATITUDE, json.get(JsonTagsZ.LATITUDE));
    } catch (JSONException je) {
      // do nothing
    }
    // longitude is optional
    try {
      kiosk.put(JsonTagsZ.LONGITUDE, json.get(JsonTagsZ.LONGITUDE));
    } catch (JSONException je) {
      // do nothing
    }
    // geo-accuracy is optional
    try {
      kiosk.put(JsonTagsZ.GEO_ACCURACY, json.get(JsonTagsZ.GEO_ACCURACY));
    } catch (JSONException je) {
      // do nothing
    }
    // geo-error is optional
    try {
      kiosk.put(JsonTagsZ.GEO_ERROR_CODE, json.get(JsonTagsZ.GEO_ERROR_CODE));
    } catch (JSONException je) {
      // do nothing
    }
    // district is optional
    try {
      kiosk.put(JsonTagsZ.DISTRICT, json.get(JsonTagsZ.DISTRICT));
    } catch (JSONException je) {
      // do nothing
    }
    // taluk is optional
    try {
      kiosk.put(JsonTagsZ.TALUK, json.get(JsonTagsZ.TALUK));
    } catch (JSONException je) {
      // do nothing
    }
    // street address is optional
    try {
      kiosk.put(JsonTagsZ.STREET_ADDRESS, json.get(JsonTagsZ.STREET_ADDRESS));
    } catch (JSONException je) {
      // do nothing
    }
    // pin code is optional
    try {
      kiosk.put(JsonTagsZ.PINCODE, json.get(JsonTagsZ.PINCODE));
    } catch (JSONException je) {
      // do nothing
    }
    // Currency is optional
    try {
      kiosk.put(JsonTagsZ.CURRENCY, json.get(JsonTagsZ.CURRENCY));
    } catch (JSONException je) {
      // do nothing
    }
    // tax is optional
    try {
      kiosk.put(JsonTagsZ.TAX, json.get(JsonTagsZ.TAX));
    } catch (JSONException je) {
      // do nothing
    }
    // inventory policy
    try {
      kiosk.put(JsonTagsZ.INVENTORY_POLICY, json.get(JsonTagsZ.INVENTORY_POLICY));
    } catch (JSONException je) {
      // do nothing
    }
    // service level is optional
    try {
      kiosk.put(JsonTagsZ.SERVICE_LEVEL, json.get(JsonTagsZ.SERVICE_LEVEL));
    } catch (JSONException je) {
      // do nothing
    }
    // New entity name us optional
    try {
      kiosk.put(JsonTagsZ.NEW_NAME, json.get(JsonTagsZ.NEW_NAME));
    } catch (JSONException je) {
      // do nothing
    }
    // Route tag
    try {
      kiosk.put(JsonTagsZ.ROUTE_TAG, json.get(JsonTagsZ.ROUTE_TAG));
    } catch (JSONException je) {
      // do nothing
    }
    try {
      kiosk.put(JsonTagsZ.ADD_ALL_MATERIALS, json.get(JsonTagsZ.ADD_ALL_MATERIALS));
    } catch (JSONException je) {
      // do nothing
    }

    // Read the JSONArray containing the material names. Convert it into comma
    // separated string values and store it in the entity hashtable
    try {
      String materials = "";
      JSONArray matArray = json.getJSONArray(JsonTagsZ.MATERIALS);
      for (int i = 0; i < matArray.length(); i++) {
        materials = materials + matArray.get(i) + (i < matArray.length() ? SEPARATOR : "");
      }
      kiosk.put(JsonTagsZ.MATERIALS, materials);
    } catch (JSONException je) {
      // do nothing
    }

    // Read the initial stock ( for all materials )
    try {
      kiosk.put(JsonTagsZ.QUANTITY, json.get(JsonTagsZ.QUANTITY));
    } catch (JSONException je) {
      // do nothing
    }
    // Read the JSONArray containing the customer names. Convert it into comma
    // separated string values and store it in the entity hashtable
    try {
      String customers = "";
      JSONArray cstsArray = json.getJSONArray(JsonTagsZ.CUSTOMERS);
      for (int i = 0; i < cstsArray.length(); i++) {
        customers = customers + cstsArray.get(i) + (i < cstsArray.length() ? SEPARATOR : "");
      }
      kiosk.put(JsonTagsZ.CUSTOMERS, customers);
    } catch (JSONException je) {
      // do nothing
    }
    // Read the JSONArray containing the vendor names. Convert it into comma
    // separated string values and store it in the entity hashtable
    try {
      String vendors = "";
      JSONArray vdrsArray = json.getJSONArray(JsonTagsZ.VENDORS);
      for (int i = 0; i < vdrsArray.length(); i++) {
        vendors = vendors + vdrsArray.get(i) + (i < vdrsArray.length() ? SEPARATOR : "");
      }
      kiosk.put(JsonTagsZ.VENDORS, vendors);
    } catch (JSONException je) {
      // do nothing
    }
  }

  private void addUserData(JSONObject json) throws JSONException {
    if (this.user == null || this.user.isEmpty()) {
      throw new JSONException("No user object specified");
    }
    // Form a user json
    JSONObject u = new JSONObject();
    u.put(JsonTagsZ.USER_ID, user.get(JsonTagsZ.USER_ID));
    u.put(JsonTagsZ.PASSWORD, user.get(JsonTagsZ.PASSWORD));
    String role = (String) user.get(JsonTagsZ.ROLE);
    u.put(JsonTagsZ.ROLE, role);
    u.put(JsonTagsZ.FIRST_NAME, user.get(JsonTagsZ.FIRST_NAME));
    String value = null;
    if ((value = (String) user.get(JsonTagsZ.LAST_NAME)) != null) {
      u.put(JsonTagsZ.LAST_NAME, value);
    }
    u.put(JsonTagsZ.MOBILE, user.get(JsonTagsZ.MOBILE));

    // Email is mandatory for user with role ROLE_SERVICEMANAGER or above
    // Otherwise it's optional
    if (RestConstantsZ.ROLE_SERVICEMANAGER.equals(role)) {
      u.put(JsonTagsZ.EMAIL, user.get(JsonTagsZ.EMAIL));
    } else {
      if ((value = (String) user.get(JsonTagsZ.EMAIL)) != null) {
        u.put(JsonTagsZ.EMAIL, value);
      }
    }

    u.put(JsonTagsZ.COUNTRY, user.get(JsonTagsZ.COUNTRY));
    u.put(JsonTagsZ.LANGUAGE, user.get(JsonTagsZ.LANGUAGE));
    u.put(JsonTagsZ.TIMEZONE, user.get(JsonTagsZ.TIMEZONE));
    if ((value = (String) user.get(JsonTagsZ.GENDER)) != null) {
      u.put(JsonTagsZ.GENDER, value);
    }
    if ((value = (String) user.get(JsonTagsZ.AGE)) != null) {
      u.put(JsonTagsZ.AGE, value);
    }
    if ((value = (String) user.get(JsonTagsZ.LANDLINE)) != null) {
      u.put(JsonTagsZ.LANDLINE, value);
    }
    if ((value = (String) user.get(JsonTagsZ.STATE)) != null) {
      u.put(JsonTagsZ.STATE, value);
    }
    if ((value = (String) user.get(JsonTagsZ.DISTRICT)) != null) {
      u.put(JsonTagsZ.DISTRICT, value);
    }
    if ((value = (String) user.get(JsonTagsZ.TALUK)) != null) {
      u.put(JsonTagsZ.TALUK, value);
    }
    if ((value = (String) user.get(JsonTagsZ.CITY)) != null) {
      u.put(JsonTagsZ.CITY, value);
    }
    if ((value = (String) user.get(JsonTagsZ.STREET_ADDRESS)) != null) {
      u.put(JsonTagsZ.STREET_ADDRESS, value);
    }
    if ((value = (String) user.get(JsonTagsZ.PINCODE)) != null) {
      u.put(JsonTagsZ.PINCODE, value);
    }

    // Add to the container JSON
    json.put(JsonTagsZ.USER, u);
  }

  private void addKioskData(JSONObject json) throws JSONException {
    if (this.kiosk == null || this.kiosk.isEmpty()) {
      throw new JSONException("No Kiosk object specified");
    }
    // Form a kiosk json
    JSONObject k = new JSONObject();
    k.put(JsonTagsZ.NAME, kiosk.get(JsonTagsZ.NAME));

    //  Get the list of users separated by SEPARATOR
    String usrs = (String) kiosk.get(JsonTagsZ.USERS);
    // Tokenize the list of users
    StringTokenizer usrsSt = new StringTokenizer(usrs, SEPARATOR);
    // Create a JSONArray to hold the users
    JSONArray usrsArray = new JSONArray();
    // Add elements to the JSONArray
    while (usrsSt.hasMoreElements()) {
      usrsArray.put(usrsSt.nextElement());
    }
    k.put(JsonTagsZ.USERS, usrsArray);

    k.put(JsonTagsZ.COUNTRY, kiosk.get(JsonTagsZ.COUNTRY));
    k.put(JsonTagsZ.STATE, kiosk.get(JsonTagsZ.STATE));
    k.put(JsonTagsZ.CITY, kiosk.get(JsonTagsZ.CITY));

    String value = null;
    if ((value = (String) kiosk.get(JsonTagsZ.LATITUDE)) != null) {
      k.put(JsonTagsZ.LATITUDE, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.LONGITUDE)) != null) {
      k.put(JsonTagsZ.LONGITUDE, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.GEO_ACCURACY)) != null) {
      k.put(JsonTagsZ.GEO_ACCURACY, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.GEO_ERROR_CODE)) != null) {
      k.put(JsonTagsZ.GEO_ERROR_CODE, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.DISTRICT)) != null) {
      k.put(JsonTagsZ.DISTRICT, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.TALUK)) != null) {
      k.put(JsonTagsZ.TALUK, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.STREET_ADDRESS)) != null) {
      k.put(JsonTagsZ.STREET_ADDRESS, value);
    }
    if ((value = (String) user.get(JsonTagsZ.PINCODE)) != null) {
      k.put(JsonTagsZ.PINCODE, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.CURRENCY)) != null) {
      k.put(JsonTagsZ.CURRENCY, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.TAX)) != null) {
      k.put(JsonTagsZ.TAX, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.INVENTORY_POLICY)) != null) {
      k.put(JsonTagsZ.INVENTORY_POLICY, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.SERVICE_LEVEL)) != null) {
      k.put(JsonTagsZ.SERVICE_LEVEL, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.NEW_NAME)) != null) {
      k.put(JsonTagsZ.NEW_NAME, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.ROUTE_TAG)) != null) {
      k.put(JsonTagsZ.ROUTE_TAG, value);
    }
    if ((value = (String) kiosk.get(JsonTagsZ.ADD_ALL_MATERIALS)) != null) {
      k.put(JsonTagsZ.ADD_ALL_MATERIALS, value);
    }

    // Get a list of materials separated by SEPARATOR
    String mat = (String) kiosk.get(JsonTagsZ.MATERIALS);
    if (mat != null && !mat.equals("")) {
      // Tokenize the list of materials
      StringTokenizer matSt = new StringTokenizer(mat, SEPARATOR);
      // Create a JSONArray to hold the materials
      JSONArray matArray = new JSONArray();
      // Add elements to the JSONArray
      while (matSt.hasMoreElements()) {
        matArray.put(matSt.nextElement());
      }
      k.put(JsonTagsZ.MATERIALS, matArray);
    }

    // Get the initial stock ( of all materials )
    if ((value = (String) kiosk.get(JsonTagsZ.QUANTITY)) != null) {
      k.put(JsonTagsZ.QUANTITY, value);
    }

    // Get a list of customer names separated by SEPARATOR
    String csts = (String) kiosk.get(JsonTagsZ.CUSTOMERS);
    if (csts != null && !csts.equals("")) {
      // Tokenize the list of customers
      StringTokenizer cstsSt = new StringTokenizer(csts, SEPARATOR);
      // Create a JSONArray to hold the customers
      JSONArray cstsArray = new JSONArray();
      // Add elements to the JSONArray
      while (cstsSt.hasMoreElements()) {
        cstsArray.put(cstsSt.nextElement());
      }
      k.put(JsonTagsZ.CUSTOMERS, cstsArray);
    }
    // Get a list of vendor names separated by SEPARATOR
    String vdrs = (String) kiosk.get(JsonTagsZ.VENDORS);
    if (vdrs != null && !vdrs.equals("")) {
      // Tokenize the list of vendors
      StringTokenizer vdrsSt = new StringTokenizer(vdrs, SEPARATOR);
      // Create a JSONArray to hold the vendors
      JSONArray vdrsArray = new JSONArray();
      // Add elements to the JSONArray
      while (vdrsSt.hasMoreElements()) {
        vdrsArray.put(vdrsSt.nextElement());
      }
      k.put(JsonTagsZ.VENDORS, vdrsArray);
    }
    // Add to the container JSON
    json.put(JsonTagsZ.KIOSK, k);
  }
}
