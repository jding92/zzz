package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.Arrival;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.parsers.exception.ArrivalsDataMissingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A parser for the data returned by the Translink stop arrivals query
 */
public class ArrivalsParser {

    /**
     * Parse arrivals from JSON response produced by Translink query.  All parsed arrivals are
     * added to given stop assuming that corresponding JSON object has all of:
     * timeToStop, platformName, lineID and one of destinationName or towards.  If
     * any of the aforementioned elements is missing, the arrival is not added to the stop.
     *
     * @param stop             stop to which parsed arrivals are to be added
     * @param jsonResponse    the JSON response produced by Translink
     * @throws JSONException  when JSON response does not have expected format
     * @throws ArrivalsDataMissingException  when all arrivals are missing at least one of the following:
     * <ul>
     *     <li>timeToStop</li>
     *     <li>platformName</li>
     *     <li>lineId</li>
     *     <li>destinationName and towards</li>
     * </ul>
     */
    public static void parseArrivals(Stop stop, String jsonResponse)
            throws JSONException, ArrivalsDataMissingException {
        System.out.println("Arrivals data: \"" + jsonResponse + "\"");
        JSONArray ja = new JSONArray(jsonResponse);
        int numberArrivalsAdded = 0;
        for (int i = 0; i < ja.length(); ++i) {
            try {
                JSONObject jo = ja.getJSONObject(i);
                // This should be a route arrival, with RouteNo, Direction, RouteName, and Schedules fields
                String routeNo = jo.getString("RouteNo");
                JSONArray arrivals = jo.getJSONArray("Schedules");
                for (int a = 0; a < arrivals.length(); a++) {
                    JSONObject ao = arrivals.getJSONObject(a);
                    // This should be an arrival, with ExpectedCountdown, Destination, ScheduleStatus fields
                    int timeToStop = ao.getInt("ExpectedCountdown");
                    String destination = ao.getString("Destination");
                    String status = ao.getString("ScheduleStatus");
                    Arrival arrival = new Arrival(timeToStop, destination, routeNo);
                    arrival.setStatus(status);
                    stop.addArrival(arrival);
                    numberArrivalsAdded++;
                }
            } catch (JSONException e) {
                // Do nothing, in particular don't update any objects
            }
        }
        if (numberArrivalsAdded == 0) {
            throw new ArrivalsDataMissingException("All arrivals are missing some information");
        }
    }
}
