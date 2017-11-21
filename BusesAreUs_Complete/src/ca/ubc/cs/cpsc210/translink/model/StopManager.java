package ca.ubc.cs.cpsc210.translink.model;

import ca.ubc.cs.cpsc210.translink.model.exception.StopException;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import ca.ubc.cs.cpsc210.translink.util.SphericalGeometry;


import java.util.*;

/**
 * Manages all bus stops.
 *
 * Singleton pattern applied to ensure only a single instance of this class that
 * is globally accessible throughout application.
 */
public class StopManager implements Iterable<Stop> {
    public static final int RADIUS = 10000;
    private static StopManager instance;
    private Set<Stop> stops;
    private Map<Integer, Stop> stopMap;
    private Stop selectedStop;

    /**
     * Constructs stop manager with empty set of stops and null as the selected stop
     */
    private StopManager() {
        this.stops = new HashSet<Stop>();
        this.stopMap = new HashMap<>();
        this.selectedStop = null;
    }

    /**
     * Gets one and only instance of this class
     *
     * @return  instance of class
     */
    public static StopManager getInstance() {
        // Do not modify the implementation of this method!
        if(instance == null) {
            instance = new StopManager();
        }

        return instance;
    }

    public Stop getSelected() {
        return selectedStop;
    }

    /**
     * Get stop with given id, creating it if necessary
     *
     * @param id  the id of this stop
     *
     * @return  stop with given id
     */
    public Stop getStopWithId(int id) {
        Stop s = stopMap.get(id);
        if (s != null) {
            return s;
        }
        s = new Stop(id, "Bogus stop", null);
        stops.add(s);
        stopMap.put(id, s);
        return s;
    }

    /**
     * Get stop with given id, creating it if necessary, using the given name and latlon
     *
     * @param id  the id of this stop
     *
     * @return  stop with given id
     */
    public Stop getStopWithId(int id, String name, LatLon locn) {
        Stop s = stopMap.get(id);
        if (s != null) {
            s.setName(name);
            s.setLocn(locn);
            return s;
        }

        s = new Stop(id, name, locn);
        stops.add(s);
        stopMap.put(id, s);
        return s;
    }

    /**
     * Set the stop selected by user
     *
     * @param selected   stop selected by user
     * @throws StopException when stop manager doesn't contain selected stop
     */
    public void setSelected(Stop selected) throws StopException {
        if (stopMap.containsKey(selected.getID())) {
            selectedStop = selected;
        } else {
            throw new StopException("No such stop: " + selected.getID() + " " + selected.getName());
        }
    }

    /**
     * Clear selected stop (selected stop is null)
     */
    public void clearSelectedStop() {
        selectedStop = null;
    }

    /**
     * Get number of stops managed
     *
     * @return  number of stops added to manager
     */
    public int getNumStops() {
        return stops.size();
    }

    /**
     * Remove all stops from stop manager
     */
    public void clearStops() {
        stops.clear();
        stopMap.clear();
        clearSelectedStop();
    }

    /**
     * Find nearest stop to given point.  Returns null if no stop is closer than RADIUS metres.
     *
     * @param pt  point to which nearest stop is sought
     * @return    stop closest to pt but less than 10,000m away; null if no stop is within RADIUS metres of pt
     */
    public Stop findNearestTo(LatLon pt) {
        Stop nearest = null;
        double nearestDistance = RADIUS;
        for (Stop s : stops) {
            double distance = SphericalGeometry.distanceBetween(s.getLocn(), pt);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = s;
            }
        }
        return nearest;
    }

    @Override
    public Iterator<Stop> iterator() {
        // Do not modify the implementation of this method!
        return stops.iterator();
    }

    public void clearMarkers() {
        for (Stop s : stops)
            s.setMarker(null);
    }
}
