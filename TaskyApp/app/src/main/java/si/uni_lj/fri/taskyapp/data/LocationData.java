/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.location.Location;

import si.uni_lj.fri.taskyapp.global.SensorsHelper;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class LocationData {
    private double lat;
    private double lng;
    private double altitude;
    private float accuracy;
    private String address;

    public LocationData(Context ctx, Location l) {
        super();
        if (l != null) {
            this.lat = l.getLatitude();
            this.lng = l.getLongitude();
            this.altitude = l.getAltitude();
            this.accuracy = l.getAccuracy();
            this.address = SensorsHelper.getLocationAddress(ctx, lat, lng);
        }
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Location getLocationObject() {
        Location location = new Location("location");
        location.setLongitude(lng);
        location.setLatitude(lat);
        location.setAccuracy(accuracy);
        location.setAltitude(altitude);
        return location;
    }

    public float getDistanceTo(LocationData ld) {
        return this.getLocationObject().distanceTo(ld.getLocationObject());
    }

    public String getPrettyLocationString() {
        if (address != null) {
            return address;
        } else {
            return String.format("%.3f, %.3f", lat, lng);
        }
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", altitude=" + altitude +
                ", accuracy=" + accuracy +
                '}';
    }
}
