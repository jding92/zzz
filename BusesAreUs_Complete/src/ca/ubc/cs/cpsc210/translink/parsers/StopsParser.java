package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.parsers.exception.StopDataMissingException;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A parser for the data returned by Translink stops query
 */
public class StopsParser {

    /**
     * Parse route and stop information from JSON response produced by Translink.
     *
     * @param jsonResponse     string encoding JSON data to be parsed
     * @return                 route parsed from Translink data
     * @throws JSONException   when JSON data does not have expected format
     * @throws StopDataMissingException when
     * <ul>
     *  <li> JSON data is not an array </li>
     *  <li> JSON data is missing Name, StopNo or location elements for any stop</li>
     * </ul>
     */

    public static void parseStops(String jsonResponse)
            throws JSONException, StopDataMissingException {
        try {
            System.out.println("Reading JSONArray");
            JSONArray stops = new JSONArray(jsonResponse);
            System.out.println("Done reading JSONArray, reading " + stops.length() + " stops");
            for (int i = 0; i < stops.length(); ++i) {
                JSONObject onestop = stops.getJSONObject(i);
                String stopName = onestop.getString("Name");
                int stopId = onestop.getInt("StopNo");
                double stopLat = onestop.getDouble("Latitude");
                double stopLon = onestop.getDouble("Longitude");
                String routes = onestop.getString("Routes");
                String[] routearray = routes.split(", *");
                Stop s = StopManager.getInstance().getStopWithId(stopId, stopName, new LatLon(stopLat, stopLon));
                for (String route : routearray) {
                    Route r = RouteManager.getInstance().getRouteWithNumber(route);
                    s.addRoute(r);
                }
            }

        } catch (JSONException e) {
            throw new StopDataMissingException("Missing required data about a stop");
        }
    }
}
