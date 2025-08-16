package com.yt.hebrew;

import com.kosherjava.zmanim.ZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;

import java.util.Date;

public class ZmanimUtil {
    private final ZmanimCalendar zc;

    public ZmanimUtil(GeoLocation location) {
        zc = new ZmanimCalendar(location);
    }

    public Date getSunrise() {
        return zc.getSunrise();
    }

    public Date getSunset() {
        return zc.getSunset();
    }
}
