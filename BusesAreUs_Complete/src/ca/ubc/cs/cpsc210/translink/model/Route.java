package ca.ubc.cs.cpsc210.translink.model;

import android.graphics.Color;

import java.util.*;
import java.util.List;

/**
 * Represents a bus route with a route number and list of stops.
 * <p/>
 * Invariants:
 * - no duplicates in list of stops
 * - iterator iterates over stops in the order in which they were added to the route
 */
public class Route implements Iterable<Stop> {
    private List<Stop> stops;
    private String number;
    private String name;
    private List<RoutePattern> patterns = new ArrayList<>();
    private int colour;

    private static final int allColours[] =
            {0xBF00FFFF, 0xBF0000FF, 0xBF00FF00, 0xBFFF00FF, 0xBFFF0000, 0xBFFFFF00};
    private static int nextColour = 0; //Stores index of nextColour

    /**
     * Constructs a route with given number.
     * List of stops is empty.
     *
     * @param number the route number
     */
    public Route(String number) {
        this.number = number;
        this.stops = new ArrayList<>();
        this.colour = nextColour();
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPattern(RoutePattern pattern) {
        if (!patterns.contains(pattern)) {
            patterns.add(pattern);
        }
    }

    /**
     * Get colour specified by route resource data
     *
     * @return colour in which to plot this route
     */
    public int getColour() {
        return colour;
    }

    /**
     * Add stop to route.
     *
     * @param stn the stop to add to this route
     */
    public void addStop(Stop stn) {
        if (!hasStop(stn))
            stops.add(stn);
        stn.addRoute(this);
    }

    /**
     * Remove stop from route
     *
     * @param stn the stop to remove from this route
     */
    public void removeStop(Stop stn) {
        if (hasStop(stn)) {
            stops.remove(stn);
            stn.removeRoute(this);
        }
    }

    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Determines if this route has a stop at a given stop
     *
     * @param stn the stop
     * @return true if route has a stop at given stop
     */
    public boolean hasStop(Stop stn) {
        return stops.contains(stn);
    }

    /**
     * Two routes are equal if their numbers are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route stops = (Route) o;

        return number.equals(stops.number);

    }

    /**
     * Two routes are equal if their numbers are equal.
     * Therefore hashCode only pays attention to number.
     */
    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public Iterator<Stop> iterator() {
        // Do not modify the implementation of this method!
        return stops.iterator();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Route " + number;
    }

    private static int nextColour() {
        int ans = allColours[nextColour];
        nextColour = (nextColour + 1) % allColours.length;
        return ans;
    }


    public RoutePattern getPattern(String patternName) {
        for (RoutePattern pattern : patterns) {
            String name = pattern.getName();
            if (name != null && name.equals(patternName)) {
                return pattern;
            }
        }
        return new RoutePattern(patternName, "", "", "", this);//fixme??
    }

    public List<RoutePattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }
}
