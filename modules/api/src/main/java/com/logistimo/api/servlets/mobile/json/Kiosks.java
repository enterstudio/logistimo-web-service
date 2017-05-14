package com.logistimo.api.servlets.mobile.json;

import com.google.gson.annotations.Expose;

import com.logistimo.entities.entity.IKiosk;

import com.logistimo.pagination.Results;

import java.util.List;

public class Kiosks extends JsonOutput {

  @Expose
  List<IKiosk> kiosks = null;
  @Expose
  String cursor = null;

  @SuppressWarnings("unchecked")
  public Kiosks(String version, boolean status, String message, Results results, int totalKiosks) {
    super(version, status, message);
    this.kiosks = results.getResults();
    this.cursor = results.getCursor();
  }

	/*
        private void initAAData() {
		if ( kiosks == null || kiosks.isEmpty() )
			return;
		aaData = new ArrayList<List<String>>();
		Iterator<Kiosk> it = kiosks.iterator();
		while ( it.hasNext() ) {
			Kiosk k = it.next();
			List<String> kioskData = new ArrayList<String>();
			kioskData.add( k.getKioskId().toString() );
			kioskData.add( k.getName() );
			aaData.add( kioskData );
		}
	}
	*/
}
