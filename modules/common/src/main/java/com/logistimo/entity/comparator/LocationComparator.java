package com.logistimo.entity.comparator;

import com.logistimo.entity.ILocation;

import java.util.Comparator;

/**
 * Created by kumargaurav on 16/07/17.
 */
public class LocationComparator implements Comparator<ILocation> {


    @Override
    public int compare(ILocation o1, ILocation o2) {
        if (o1.getCountry() != null && o2.getCountry() != null) {
            if (!o1.getCountry().equalsIgnoreCase(o2.getCountry())) {
                return 1;
            }
        }
        if (o1.getState() != null && o2.getState() != null) {
            if (!o1.getState().equalsIgnoreCase(o2.getState())) {
                return 1;
            }
        }
        if (o1.getDistrict() != null && o2.getDistrict() != null) {
            if (!o1.getDistrict().equalsIgnoreCase(o2.getDistrict())) {
                return 1;
            }
        }
        if (o1.getCity() != null && o2.getCity() != null) {
            if (!o1.getCity().equalsIgnoreCase(o2.getCity())) {
                return 1;
            }
        }
        return 0;
    }
}
