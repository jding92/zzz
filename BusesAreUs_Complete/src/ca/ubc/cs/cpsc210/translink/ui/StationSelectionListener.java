package ca.ubc.cs.cpsc210.translink.ui;

import ca.ubc.cs.cpsc210.translink.model.Stop;

/**
 * Handles user selection of station on map
 */
public interface StationSelectionListener {

    /**
     * Called when user selects a station
     *
     * @param stn   station selected by user
     */
    void onStationSelected(Stop stn);
}
