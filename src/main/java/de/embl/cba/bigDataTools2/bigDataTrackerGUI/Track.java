/*
 * #%L
 * Data streaming, tracking and cropping tools
 * %%
 * Copyright (C) 2017 Christian Tischer
 *
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package de.embl.cba.bigDataTools2.bigDataTrackerGUI;

import de.embl.cba.bigDataTools2.logging.IJLazySwingLogger;
import de.embl.cba.bigDataTools2.logging.Logger;
import javafx.geometry.Point3D;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tischi on 03/12/16.
 */
public class Track {

    TrackingSettings trackingSettings;
    Map<Integer, Point3D[]> locations = new LinkedHashMap<>();
    Point3D trackStart;
    int trackID;

    Logger logger = new IJLazySwingLogger();

    Track(TrackingSettings trackingSettings, int id) {

        this.trackingSettings = trackingSettings;

//        Roi roi = trackingSettings.trackStartROI;
//        if(roi.getTypeAsString().equals("Point")) {
//            trackStart = new Point3D(
//                    roi.getPolygon().xpoints[0],
//                    roi.getPolygon().ypoints[0],
//                    roi.getZPosition()-1);
//        } else {
//            logger.error("Please use the point selection tool to mark an object.");
//            return;
//        }

        trackID =  id;

    }

    public void addLocation(int t, Point3D[] p)
    {
        locations.put(t, p);
    }

    public int getID() {
        return(trackID);
    }

    public Point3D getObjectSize() {
        return(trackingSettings.objectSize);
    }

    public void reset() {
        locations.clear();
    }

    public Map<Integer, Point3D[]> getLocations() {
        return locations;
    }

    public Point3D[] getPosition(int t) {
        return(locations.get(t));
    }

    public int getC() {
        return(trackingSettings.channel);
    }

    public int getTmin() {

        return(Collections.min(locations.keySet()));
    }

    public int getTmax()
    {
        return(Collections.max(locations.keySet()));
    }

    public int getLength() {
        return(locations.size());
    }

    public boolean isTimePresent(int time){
        return locations.containsKey(Integer.valueOf(time));
    }

}
