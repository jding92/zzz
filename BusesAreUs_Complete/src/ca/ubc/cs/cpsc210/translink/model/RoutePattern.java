package ca.ubc.cs.cpsc210.translink.model;

import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * A description of one pattern of a route
 */
public class RoutePattern {
    private String name;
    private String destination;
    private String direction;
    private String routeMapURL;
    private List<GeoPoint> path;
    private Route route;

    public RoutePattern(String name, String destination, String direction, String routeMapURL, Route route) {
        this.name = name;
        this.destination = destination;
        this.direction = direction;
        this.routeMapURL = routeMapURL;
        this.route = route;
        route.addPattern(this);
    }

    public String getName() {
        return name;
    }

    public String getDestination() {
        return destination;
    }

    public String getDirection() {
        return direction;
    }

    public String getRouteMapURL() {
        return routeMapURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoutePattern that = (RoutePattern) o;

        if (!name.equals(that.name)) return false;
        return route.equals(that.route);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + route.hashCode();
        return result;
    }

    public void setPath(List<GeoPoint> path) {
        this.path = path;
    }

    public List<GeoPoint> getPath() {
        return path;
    }
}
