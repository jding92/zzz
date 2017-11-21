package ca.ubc.cs.cpsc210.translink.model;

import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.bonuspack.overlays.Marker;

import java.util.*;

/**
 * Represents a bus stop with an id, name, location (lat/lon)
 * set of routes which stop at this stop and a list of arrivals.
 */
public class Stop implements Iterable<Arrival> {
    private Set<Route> routes = new HashSet<>();
    private int id;
    private String name;
    private LatLon locn;
    private List<Arrival> arrivals = new ArrayList<>();
    private Marker marker = null;

    /**
     * Constructs a stop with given id, name and location.
     * Set of routes and list of arrival boards are empty.
     *
     * @param id    the id of this stop (cannot by null)
     * @param name  name of this stop
     * @param locn  location of this stop
     */
    public Stop(int id, String name, LatLon locn) {
        this.id = id;
        this.name = name;
        this.locn = locn;
    }

    public String getName() {
        return name;
    }

    public LatLon getLocn() {
        return locn;
    }

    public int getID() {
        return id;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    /**
     * Add route to set of routes with stops at this stop.
     *
     * @param route  the route to add
     */
    public void addRoute(Route route) {
        if (!onRoute(route)) {
            routes.add(route);
            route.addStop(this);
        }
    }

    /**
     * Remove route from set of routes with stops at this stop
     *
     * @param route the route to remove
     */
    public void removeRoute(Route route) {
        if (onRoute(route)) {
            routes.remove(route);
            route.removeStop(this);
        }
    }

    /**
     * Determine if this stop is on a given route
     * @param route  the route
     * @return  true if this stop is on given route
     */
    public boolean onRoute(Route route) {
        return routes.contains(route);
    }

    /**
     * Add bus arrival travelling on a particular route at this stop.
     *
     * @param arrival  the bus arrival to add to stop
     */
    public void addArrival(Arrival arrival) {
        arrivals.add(arrival);
        Collections.sort(arrivals);
    }

    /**
     * Remove all arrivals from this stop
     */
    public void clearArrivals() {
        arrivals.clear();
    }

    /**
     * Two stops are equal if their ids are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stop that = (Stop) o;

        return id == that.id;

    }

    /**
     * Two stops are equal if their ids are equal.
     * Therefore hashCode only pays attention to id.
     */
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Iterator<Arrival> iterator() {
        // Do not modify the implementation of this method!
        return arrivals.iterator();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocn(LatLon locn) {
        this.locn = locn;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker;
    }
}
