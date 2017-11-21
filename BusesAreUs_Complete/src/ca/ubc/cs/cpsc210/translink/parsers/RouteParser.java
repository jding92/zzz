package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A parser for the data returned by Translink route query
 */
public class RouteParser {

    /**
     * Parse route information from JSON response produced by Translink.
     *
     * @param jsonResponse     string encoding JSON data to be parsed
     * @return                 route parsed from Translink data
     * @throws JSONException   when JSON data does not have expected format
     */

    public static Route parseRoute(String jsonResponse)
            throws JSONException {
        JSONObject route = new JSONObject(jsonResponse);
        String number = route.getString("RouteNo");
        String name = route.getString("Name");
        Route r = RouteManager.getInstance().getRouteWithNumber(number, name);
        JSONArray patterns = route.getJSONArray("Patterns");
        for (int i = 0; i < patterns.length(); ++i) {
            JSONObject pattern = patterns.getJSONObject(i);
            String patternNo = pattern.getString("PatternNo");
            String destination = pattern.getString("Destination");
            String direction = pattern.getString("Direction");
            JSONObject routeMap = pattern.getJSONObject("RouteMap");
            String url = routeMap.getString("Href");
            RoutePattern rp = new RoutePattern(patternNo, destination, direction, url, r);
            r.addPattern(rp);
        }
        return r;
    }
}
