package ca.ubc.cs.cpsc210.translink.model;

import java.util.*;

/**
 * Manages all routes.
 *
 * Singleton pattern applied to ensure only a single instance of this class that
 * is globally accessible throughout application.
 */
public class RouteManager implements Iterable<Route> {
    private static RouteManager instance;
    private Set<Route> routes;
    private Map<String, Route> routeMap;

    /**
     * Constructs Route manager with empty set of stops and null as the selected stop
     */
    private RouteManager() {
        this.routeMap = new HashMap<>();
        this.routes = new HashSet<Route>();
    }

    /**
     * Gets one and only instance of this class
     *
     * @return  instance of class
     */
    public static RouteManager getInstance() {
        // Do not modify the implementation of this method!
        if(instance == null) {
            instance = new RouteManager();
        }

        return instance;
    }

    /**
     * Get route with given number, creating it if necessary
     *
     * @param number  the number of this route
     *
     * @return  route with given number
     */
    public Route getRouteWithNumber(String number) {
        Route r = routeMap.get(number);
        if (r != null) {
            return r;
        }

        r = new Route(number);
        routes.add(r);
        routeMap.put(number, r);
        return r;
    }

    /**
     * Get route with given number, creating it if necessary, using the given name and number
     *
     * @param number  the number of this route
     *
     * @return  route with given number and name
     */
    public Route getRouteWithNumber(String number, String name) {
        Route r = routeMap.get(number);
        if (r != null) {
            r.setName(name);
            return r;
        }

        r = new Route(number);
        r.setName(name);
        routes.add(r);
        routeMap.put(number, r);
        return r;
    }

    /**
     * Get number of routes managed
     *
     * @return  number of routes added to manager
     */
    public int getNumRoutes() {
        return routes.size();
    }

    @Override
    public Iterator<Route> iterator() {
        // Do not modify the implementation of this method!
        return routes.iterator();
    }
}
