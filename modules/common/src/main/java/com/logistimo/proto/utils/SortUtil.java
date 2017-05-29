/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.proto.utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author satya
 */
public class SortUtil {

  private static final int INSERTIONSORT_THRESHOLD = 7;

  public static Vector sort(Vector v, ComparatorValue c) {
    Object[] a = new Object[v.size()];
    v.copyInto(a);
    sort(a, c);
    return toVector(a);
  }

  public static void sort(Object[] a, ComparatorValue c) {
    Object[] aux = new Object[a.length];
    for (int i = 0; i < aux.length; i++) {
      aux[i] = a[i]; // new String( c.getValue( a[i] ) );
    }
    mergeSort(aux, a, 0, a.length, 0, c);
  }

  public static Vector sortStrings(Vector v) {
    String[] a = new String[v.size()];
    v.copyInto(a);
    sort(a, new ComparatorValue() {
      public String getValue(Object o) {
        return (String) o;
      }
    });
    return toVector(a);
  }

  public static Vector toVector(Object[] array) {
    Vector v = new Vector();
    int size = array.length;
    for (int i = 0; i < size; i++) {
      v.addElement(array[i]);
    }
    return v;
  }

  public static Object[] toArray(Hashtable ht) {
    int size = ht.size();
    Object[] array = new Object[size];
    if (size == 0) {
      return array;
    }
    if (size == 1) {
      array[0] = ht.elements().nextElement();
      return array;
    }
    Enumeration en = ht.elements();
    for (int i = 0; i < size; i++) {
      array[i] = en.nextElement();
    }
    return array;
  }

  /**
   * Src is the source array that starts at index 0
   * Dest is the (possibly larger) array destination with a possible offset
   * low is the index in dest to start sorting
   * high is the end index in dest to end sorting
   * off is the offset to generate corresponding low, high in src
   */
  private static void mergeSort(Object[] src,
                                Object[] dest,
                                int low,
                                int high,
                                int off, ComparatorValue c) {
    int length = high - low;

    // Insertion sort on smallest arrays
    if (length < INSERTIONSORT_THRESHOLD) {
      for (int i = low; i < high; i++) {
        for (int j = i; j > low && compareTo(c.getValue(dest[j - 1]), c.getValue(dest[j])) > 0;
             j--) {
          swap(dest, j, j - 1);
        }
      }
      return;
    }

    // Recursively sort halves of dest into src
    int destLow = low;
    int destHigh = high;
    low += off;
    high += off;
    int mid = (low + high) >>> 1;
    mergeSort(dest, src, low, mid, -off, c);
    mergeSort(dest, src, mid, high, -off, c);

    // If list is already sorted, just copy from src to dest.  This is an
    // optimization that results in faster sorts for nearly ordered lists.
    if (compareTo(c.getValue(src[mid - 1]), c.getValue(src[mid])) <= 0) {
      System.arraycopy(src, low, dest, destLow, length);
      return;
    }

    // Merge sorted halves (now in src) into dest
    for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
      if (q >= high || p < mid && compareTo(c.getValue(src[p]), c.getValue(src[q])) <= 0) {
        dest[i] = src[p++];
      } else {
        dest[i] = src[q++];
      }
    }
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(Object[] x, int a, int b) {
    Object t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  public static int compareTo(String oneString1, String anotherString1) {
    String oneString = (String) oneString1;
    String anotherString = (String) anotherString1;
    int len1 = oneString.length();
    int len2 = anotherString.length();
    int n = Math.min(len1, len2);
    char v1[] = oneString.toCharArray();
    char v2[] = anotherString.toCharArray();
    int i = 0;
    int j = 0;

    if (i == j) {
      int k = i;
      int lim = n + i;
      while (k < lim) {
        char c1 = v1[k];
        char c2 = v2[k];
        if (c1 != c2) {
          return c1 - c2;
        }
        k++;
      }
    } else {
      while (n-- != 0) {
        char c1 = v1[i++];
        char c2 = v2[j++];
        if (c1 != c2) {
          return c1 - c2;
        }
      }
    }
    return len1 - len2;
  }

}
