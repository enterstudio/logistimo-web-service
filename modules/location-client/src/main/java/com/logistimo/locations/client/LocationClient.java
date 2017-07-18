package com.logistimo.locations.client;

import com.logistimo.entity.ILocation;
import com.logistimo.locations.model.LocationResponseModel;

/**
 * Created by kumargaurav on 13/07/17.
 */
public interface LocationClient {

    LocationResponseModel getLocationIds(ILocation location);
}
