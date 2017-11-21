package ca.ubc.cs.cpsc210.translink;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import ca.ubc.cs.cpsc210.translink.parsers.ArrivalsParser;
import ca.ubc.cs.cpsc210.translink.parsers.StopsParser;
import ca.ubc.cs.cpsc210.translink.parsers.exception.ArrivalsDataMissingException;
import ca.ubc.cs.cpsc210.translink.parsers.exception.StopDataMissingException;
import ca.ubc.cs.cpsc210.translink.providers.DataProvider;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.model.exception.StopException;
import ca.ubc.cs.cpsc210.translink.providers.HttpArrivalDataProvider;
import ca.ubc.cs.cpsc210.translink.providers.HttpStopDataProvider;
import ca.ubc.cs.cpsc210.translink.ui.LocationListener;
import ca.ubc.cs.cpsc210.translink.ui.MapDisplayFragment;
import ca.ubc.cs.cpsc210.translink.ui.StationSelectionListener;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.json.JSONException;

/**
 * Main activity
 */
public class BusesAreUs extends Activity implements LocationListener, StationSelectionListener {
    private static final String TSA_TAG = "TSA_TAG";
    private static final String MAP_TAG = "Map Fragment Tag";
    private MapDisplayFragment fragment;
    private TextView nearestStopLabel;
    private Stop myNearestStop;
    public static final String TRANSLINK_API_KEY = "xNSN6Ih96XtgHJRAwoC3";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TSA_TAG, "onCreate");

        setContentView(R.layout.map_layout);
        myNearestStop = null;

        if (savedInstanceState != null) {
            Log.i(TSA_TAG, "restoring from instance state");
            fragment = (MapDisplayFragment) getFragmentManager()
                    .findFragmentByTag(MAP_TAG);
            StopManager stopManager = StopManager.getInstance();
            int stopid = savedInstanceState.getInt("nearestStop", -1);
            if (stopid != -1) {
                myNearestStop = stopManager.getStopWithId(stopid);
            }
        } else if (fragment == null) {
            Log.i(TSA_TAG, "fragment was null");

            fragment = new MapDisplayFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.map_fragment, fragment, MAP_TAG).commit();
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setHomeButtonEnabled(false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TSA_TAG, "onSaveInstanceState");

        if (myNearestStop != null) {
            outState.putInt("nearestStop", myNearestStop.getID());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nearestStopLabel = (TextView) findViewById(R.id.nearestStnLabel);
        if (myNearestStop == null) {
            nearestStopLabel.setText(R.string.out_of_range);
        } else {
            nearestStopLabel.setText(myNearestStop.getName());
        }
    }

    /**
     * Update nearest station text view when user location changes
     *
     * @param nearest station that is nearest to user (null if no station within StationManager.RADIUS metres)
     */
    @Override
    public void onLocationChanged(Stop nearest, LatLon locn) {
        if (nearest == null) {
            nearestStopLabel.setText(R.string.out_of_range);
        } else {
            myNearestStop = nearest;
            nearestStopLabel.setText(nearest.getID() + " " + nearest.getName());
        }
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            new DownloadStopDataTask().execute(locn);
//        }
//        else {
//            Toast.makeText(this, "Unable to establish network connection!", Toast.LENGTH_LONG).show();
//        }

    }

    /**
     * Handle scrolling of the map
     *
     * @param center position of the center of the map now
     */
    @Override
    public void onMapScroll(LatLon center) {
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            new DownloadStopDataTask().execute(center);
//        }
//        else {
//            Toast.makeText(this, "Unable to establish network connection!", Toast.LENGTH_LONG).show();
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                handleAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show about dialog to user
     */
    private void handleAbout() {
        Log.d(TSA_TAG, "showing about dialog");
        AlertDialog.Builder dialogBldr = new AlertDialog.Builder(this);
        dialogBldr.setTitle(R.string.about);
        dialogBldr.setView(getLayoutInflater().inflate(R.layout.about_dialog_layout, null));
        dialogBldr.setNeutralButton(R.string.ok, null);
        dialogBldr.create().show();
    }

    /**
     * Download arrivals data for station selected by user;
     * set selected station in StationManager.
     *
     * @param stn station selected by user
     */
    @Override
    public void onStationSelected(Stop stn) {
        try {
            System.out.println("Selected station is: " + stn.getName());
            StopManager.getInstance().setSelected(stn);
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadArrivalDataTask().execute(stn);
            } else {
                Toast.makeText(this, "Unable to establish network connection!", Toast.LENGTH_LONG).show();
            }
        } catch (StopException e) {
            // shouldn't ever get here, but just in case....
            Toast.makeText(this, "Station not found on managed lines", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start activity to show arrivals to user
     *
     * @param stn station for which arrival boards are to be shown
     */
    private void startArrivalBoardActivity(Stop stop) {
        Intent i = new Intent(BusesAreUs.this, ArrivalsActivity.class);
        i.putExtra(getString(R.string.stn_name_key), stop.getID());
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_from_right, android.R.anim.fade_out);
    }

    /**
     * Task that will download and parse arrivals data
     */
    private class DownloadArrivalDataTask extends AsyncTask<Stop, Integer, String> {
        private ProgressDialog progressDialog;
        private Stop stn;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(BusesAreUs.this, getString(R.string.arrivals_download_title),
                    getString(R.string.arrivals_download_msg), true, false);
        }

        @Override
        protected String doInBackground(Stop... stns) {
            stn = stns[0];
            DataProvider dataProvider = new HttpArrivalDataProvider(stn);
            String response = null;

            try {
                response = dataProvider.dataSourceToString();
            } catch (Exception e) {
                Log.d(BusesAreUs.TSA_TAG, e.getMessage(), e);
                Toast.makeText(getApplicationContext(), "Error downloading Translink data", Toast.LENGTH_LONG).show();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Toast.makeText(getApplicationContext(), R.string.api_network, Toast.LENGTH_LONG).show();
            } else if (response.equals("Error")) {
                Log.d(BusesAreUs.TSA_TAG, "No arrivals data");
                Toast.makeText(getApplicationContext(), "No arrivals information available", Toast.LENGTH_LONG).show();
            } else {
                try {
                    stn.clearArrivals();
                    ArrivalsParser.parseArrivals(stn, response);
                    startArrivalBoardActivity(stn);
                } catch (JSONException | ArrivalsDataMissingException e) {
                    Log.d(BusesAreUs.TSA_TAG, e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), R.string.api_json, Toast.LENGTH_LONG).show();
                }
            }

            progressDialog.dismiss();
        }
    }

    /**
     * Task that will download and parse arrivals data
     */
    private class DownloadStopDataTask extends AsyncTask<LatLon, Integer, String> {
        private LatLon locn;

        @Override
        protected String doInBackground(LatLon... locns) {
            locn = locns[0];
            DataProvider dataProvider = new HttpStopDataProvider(locn);
            String response = null;

            try {
                response = dataProvider.dataSourceToString();
                System.out.println("Stops - doInBackground: " + response.length());
            } catch (Exception e) {
                Log.d(BusesAreUs.TSA_TAG, e.getMessage(), e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    System.out.println("Stops - onPostExecute: " + response.length());
                    StopsParser.parseStops(response);
                    fragment.markStops();
                } catch (JSONException e) {
                    Log.d(BusesAreUs.TSA_TAG, e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), R.string.api_json, Toast.LENGTH_LONG).show();
                } catch (StopDataMissingException e) {
                    Log.d(BusesAreUs.TSA_TAG, e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), R.string.api_json_missing, Toast.LENGTH_LONG);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.api_network, Toast.LENGTH_LONG).show();
            }
        }
    }
}
