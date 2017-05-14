package com.logistimo.proto.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

public class Properties {
  private Hashtable propTable = new Hashtable();

  public static Properties loadProperties(String filePath) throws IOException {
    Properties result = new Properties();
    InputStream stream = result.getClass().getResourceAsStream(filePath);
    // Read contents as string
    String contents = StringUtil.read(stream, "UTF-8");
    // Close stream
    stream.close();
    // Get Properties map
    String[] lines = StringUtil.split(contents, "\r\n");
    for (int i = 0; i < lines.length; i++) {
      String[] kv = StringUtil.split(lines[i], "=");
      if (kv.length == 1) {
        result.setProperty(kv[0], "");
      }
      if (kv.length == 2) {
        result.setProperty(kv[0], kv[1]);
      }
    }
    return result;
  }

  public void setProperty(String key, String val) {
    this.propTable.put(key, val);
  }

  public String getProperty(String key) {
    return (String) this.propTable.get(key);
  }

  public int getPropertyCount() {
    return this.propTable.size();
  }

  public Enumeration getEnumeratedNames() {
    return this.propTable.keys();
  }

  public String[] getPropertyNames() {
    String[] result = new String[this.propTable.size()];
    int c = 0;
    for (Enumeration e = this.propTable.keys(); e.hasMoreElements(); ) {
      result[c] = ((String) e.nextElement());
      c++;
    }

    return result;
  }
}