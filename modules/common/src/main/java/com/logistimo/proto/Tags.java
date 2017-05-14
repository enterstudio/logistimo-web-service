/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import com.logistimo.proto.utils.ResourceBundle;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Arun
 */
public class Tags {

  public String NOTAGS = "{Others}";

  private Hashtable tagMap = null; // map of tag and vector(hashtable) of material data

  public Tags() {
    this(null);
  }

  public Tags(ResourceBundle bundle) {
    tagMap = new Hashtable();
    if (bundle != null) {
      NOTAGS = "{" + bundle.getString("notags") + "}";
    }
  }

  public Enumeration getTags() {
    return tagMap.keys();
  }

  public Vector getData(String tag) {
    return (Vector) tagMap.get(tag);
  }

  public void addData(String tag, Hashtable data) {
    Vector v = getData(tag);
    if (v == null) {
      v = new Vector();
      tagMap.put(tag, v);
    }
    v.addElement(data);
  }

  public Hashtable getTagMap() {
    return this.tagMap;
  }

  public boolean hasTags() {
    return !tagMap.isEmpty();
  }

  public int size() {
    return tagMap.size();
  }

  public void clear() {
    tagMap.clear();
  }

  public boolean hasOnlyNOTAGStag() { // check if only NOTAGS tag is present
    return tagMap.size() == 1 && tagMap.containsKey(NOTAGS);
  }
}
