package ca.ubc.cs.cpsc210.translink.ui;

import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.util.LatLon;

/**
 * Handles changes in user location
 */
public interface LocationListener {

    /**
     * Called when the user's location has changed
     *
     * @param nearest  station that is nearest to user (null if no station within StationManager.RADIUS metres)
     */
    void onLocationChanged(Stop nearest, LatLon locn);
    void onMapScroll(LatLon centre);
}
