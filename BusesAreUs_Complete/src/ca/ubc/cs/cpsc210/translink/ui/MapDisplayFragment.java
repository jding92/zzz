package ca.ubc.cs.cpsc210.translink.ui;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import ca.ubc.cs.cpsc210.translink.model.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import ca.ubc.cs.cpsc210.translink.R;
import ca.ubc.cs.cpsc210.translink.parsers.StopsParser;
import ca.ubc.cs.cpsc210.translink.parsers.exception.StopDataMissingException;
import ca.ubc.cs.cpsc210.translink.providers.AndroidFileDataProvider;
import ca.ubc.cs.cpsc210.translink.providers.DataProvider;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.json.JSONException;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a fragment used to display the map to the user
 */
public class MapDisplayFragment extends Fragment implements MapEventsReceiver, IMyLocationConsumer {
    private static final String MDF_TAG = "MDF_TAG";
    /** minimum change in distance to trigger update of user location */
    private static final float MIN_UPDATE_DISTANCE = 50.0f;
    /** zoom level for map */
    private int zoomLevel = 15;
    /** centre of map */
    private GeoPoint mapCentre = new GeoPoint(49.14812, -123.11742699999999);
    /** the map view */
    private MapView mapView;
    /** overlays used to plot tube lines */
    private List<Polyline> busRouteOverlays;
    /** overlay used to display location of user */
    private MyLocationNewOverlay locOverlay;
    /** overlay used to show station markers */
    private RadiusMarkerClusterer stnClusterer;
    /** window displayed when user selects a station */
    private StationInfoWindow stopInfoWindow;
    /** overlay that listens for user initiated events on map */
    private MapEventsOverlay eventsOverlay;
    /** overlay used to display text on a layer above the map */
    private TextOverlay textOverlay;
    /** location provider used to respond to changes in user location */
    private GpsMyLocationProvider locnProvider;
    /** station manager */
    private StopManager stopManager;
    /** marker for station that is nearest to user (null if no such station) */
    private Marker nearestStnMarker;
    /** location listener used to respond to changes in user location */
    private LocationListener locationListener;
    /** last known user location (null if not available) */
    private Location lastKnownFromInstanceState;
    /** current location **/
    private Location currentLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(MDF_TAG, "onCreate");
        eventsOverlay = new MapEventsOverlay(getActivity(), this);
        locnProvider = new GpsMyLocationProvider(getActivity());
        locnProvider.setLocationUpdateMinDistance(MIN_UPDATE_DISTANCE);
        stopManager = StopManager.getInstance();
        nearestStnMarker = null;
        busRouteOverlays = new ArrayList<>();
        newStnClusterer();
        parseStops();
        parseRouteMapText();
    }

    private float dpiFactor() {
        float x = getResources().getDisplayMetrics().density;
        return x > 2.0f ? x / 2.0f : 1.0f;
    }
    private void newStnClusterer() {
        stnClusterer = new RadiusMarkerClusterer(getActivity());
        stnClusterer.getTextPaint().setTextSize(20.0F * dpiFactor());
        int zoom =  mapView == null ? 16 : mapView.getZoomLevel();
        int radius = 1000 / zoom;

//        System.out.println("Setting radius to " + radius + " for zoom level " + zoom);
        stnClusterer.setRadius(radius);
        Drawable clusterIconD = getResources().getDrawable(R.drawable.stn_cluster);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconD).getBitmap();
        stnClusterer.setIcon(clusterIcon);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        locationListener = (LocationListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int TILE_SIZE = 256;
        Log.i(MDF_TAG, "onCreateView");

        if (savedInstanceState != null) {
            Log.i(MDF_TAG, "restoring from instance state");
            mapCentre = new GeoPoint(savedInstanceState.getDouble(getString(R.string.lat_key)),
                    savedInstanceState.getDouble(getString(R.string.lon_key)));
            zoomLevel = savedInstanceState.getInt(getString(R.string.zoom_key));
            lastKnownFromInstanceState = savedInstanceState.getParcelable(getString(R.string.locn_key));
        }
        else {
            Log.i(MDF_TAG, "savedInstanceState is null - new fragment created");
        }

        if (mapView == null) {
            System.out.println("Making new mapView");
            stopManager.clearMarkers();
            mapView = new MapView(getActivity(), TILE_SIZE);
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setClickable(true);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);
            mapView.setTilesScaledToDpi(true);
            mapView.setMapListener(new DelayedMapListener(new BusRouteListener(), 100));

            GpsMyLocationProvider mapLocnProvider = new GpsMyLocationProvider(getActivity());
            mapLocnProvider.setLocationUpdateMinDistance(MIN_UPDATE_DISTANCE);
            locOverlay = new MyLocationNewOverlay(getActivity(), mapLocnProvider, mapView);
            stopInfoWindow = new StationInfoWindow((StationSelectionListener) getActivity(), mapView);
            createTextOverlay();

            // set default view for map
            final IMapController mapController = mapView.getController();

            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    else
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    mapController.setZoom(zoomLevel);
                    mapController.setCenter(mapCentre);
                }
            });
        }

        return mapView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(MDF_TAG, "onSaveInstanceState");

        outState.putDouble(getString(R.string.lat_key), mapView.getMapCenter().getLatitude());
        outState.putDouble(getString(R.string.lon_key), mapView.getMapCenter().getLongitude());
        outState.putInt(getString(R.string.zoom_key), mapView.getZoomLevel());

        // if location has been updated, use it; otherwise use last known locn restored from instance state
        Location lastKnown = locnProvider.getLastKnownLocation();
        if(lastKnown != null) {
            outState.putParcelable(getString(R.string.locn_key), locnProvider.getLastKnownLocation());
        }
        else {
            outState.putParcelable(getString(R.string.locn_key), lastKnownFromInstanceState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(MDF_TAG, "onResume");
        locnProvider.startLocationProvider(this);
        locOverlay.enableMyLocation();
        locOverlay.enableFollowLocation();
        mapView.setBuiltInZoomControls(true);

        Location lastKnownLocation = locnProvider.getLastKnownLocation();
        if (lastKnownLocation != null) {
            Log.i(MDF_TAG, "Restored from last known location");
            handleLocationChange(lastKnownLocation);
        }
        else if(lastKnownFromInstanceState != null) {
            Log.i(MDF_TAG, "Restored from instance state");
            handleLocationChange(lastKnownFromInstanceState);
            // force location overlay to redraw location icon
            locOverlay.onLocationChanged(lastKnownFromInstanceState, null);
        }
        else {
            Log.i(MDF_TAG, "Location cannot be recovered");
        }
        updateOverlays();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(MDF_TAG, "onPause");
        locOverlay.disableMyLocation();
        locnProvider.stopLocationProvider();
        mapView.setBuiltInZoomControls(false);
    }

    /**
     * Clear overlays and add route, station, location and events overlays
     */
    private void updateOverlays() {
        OverlayManager om = mapView.getOverlayManager();
        om.clear();
        om.addAll(busRouteOverlays);
        om.add(stnClusterer);
        om.add(locOverlay);
        om.add(textOverlay);
        om.add(0, eventsOverlay);

        mapView.invalidate();
    }

    /**
     * Create text overlay to display credit to Translink
     */
    private void createTextOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());
        textOverlay = new TextOverlay(rp, getResources().getString(R.string.translink_open_data), dpiFactor());
    }

    /**
     * Parse stop data from the file and add all stops to stop manager.
     *
     */
    private void parseStops() {
        DataProvider dataProvider = new AndroidFileDataProvider(getActivity(), "stops");

        try {
            System.out.println("Starting parseStops");
            StopsParser.parseStops(dataProvider.dataSourceToString());
            System.out.println("Ending parseStops");
        } catch (IOException|StopDataMissingException|JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the route map txt file
     */
    private void parseRouteMapText() {
        String fileName = "allroutemapstxt.txt";
        String filePrefix = fileName.substring(0, fileName.lastIndexOf("."));
        System.out.println("Reading route maps");
        int count = 0;

        DataProvider dataProvider = new AndroidFileDataProvider(getActivity(), filePrefix);
            try {
                String c = dataProvider.dataSourceToString();
                if (!c.equals("")) {
                    int posn = 0;
                    while (posn < c.length()) {
                        posn = loadTextRouteMap(c, posn);
                        count ++;
                    }
                }
                System.out.println("Loaded " + count + " route maps");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    private int loadTextRouteMap(String c, int posn) {
        List<GeoPoint> elements = new ArrayList<>();
        int end;
        if (!(c.charAt(posn) == 'N')) { throw new RuntimeException("Invalid format for route map"); }
        posn++;
        end = c.indexOf(';', posn);
        String name = c.substring(posn, end);
        posn = end + 1;
        while (c.charAt(posn) != '\n') {
            end = c.indexOf(';', posn);
            String lats = c.substring(posn, end);
            posn = end + 1;
            end = c.indexOf(';', posn);
            String lons = c.substring(posn, end);
            posn = end + 1;
            double lat = Double.parseDouble(lats);
            double lon = Double.parseDouble(lons);
            elements.add(new GeoPoint(lat, lon));
        }
        storeRouteMap(name, elements);
        return posn + 1;
    }

    private void storeRouteMap(String name, List<GeoPoint> elements) {
        String routeNo, patternName;
        int dash = name.indexOf('-');
        if (dash < 0) {
            routeNo = name;
            patternName = "";
        } else {
            routeNo = name.substring(0, dash);
            patternName = name.substring(dash + 1);
        }
        Route r = RouteManager.getInstance().getRouteWithNumber(routeNo);
        RoutePattern rp = r.getPattern(name);
        if (rp == null) {
            System.out.println("Can't store routeMap " + patternName + " in route " + r.getNumber());
        } else {
            rp.setPath(elements);
        }
    }

    private GeoPoint gpFromLL(LatLon ll) {
        return new GeoPoint(ll.getLatitude(), ll.getLongitude());
    }

    private void plotRoutes() {
        updateVisibleArea();
        busRouteOverlays.clear();
        for (Route r : RouteManager.getInstance()) {
            for (RoutePattern rp : r.getPatterns()) {
                List<GeoPoint> path = rp.getPath();
                plotRoute(r, path);
            }
        }
    }
    /**
     * Plot a route onto map using the provided colour
     *
     * @param route     The route
     * @param geoPoints The segments of the route
     */
    private void plotRoute(Route route, List<GeoPoint> geoPoints) {

//      System.out.println("In plotRoute " + routeNo + " " + segments.size() + " segments");
        Polyline pl = new Polyline(getActivity());
        pl.setColor(route.getColour());
        pl.setPoints(geoPoints);
        pl.setVisible(true);
        pl.setWidth(getLineWidth(zoomLevel));
        busRouteOverlays.add(pl);
    }

    private double nwlat, nwlon, selat, selon;

    private boolean stopIsVisible(Stop s) {
        return pointIsVisible(s.getLocn());
    }

    private boolean pointIsVisible(double lat, double lon) {
        return selat <= lat && lat <= nwlat && nwlon <= lon && lon <= selon;
    }

    private boolean pointIsVisible(LatLon p) {
        return pointIsVisible(p.getLatitude(), p.getLongitude());
    }

    private void updateVisibleArea() {
        GeoPoint northwest = (GeoPoint) mapView.getProjection().fromPixels(0, 0);
        GeoPoint southeast = (GeoPoint) mapView.getProjection().fromPixels(mapView.getWidth(), mapView.getHeight());
//        System.out.println("visible is " + northwest.getLatitude() + ", " + northwest.getLongitude() + " -> "
//                + southeast.getLatitude() + " " + southeast.getLongitude());
        nwlat = northwest.getLatitude();
        nwlon = northwest.getLongitude();
        selat = southeast.getLatitude();
        selon = southeast.getLongitude();
    }
    /**
     * Mark all stations in station manager onto map.
     */
    public void markStops() {
        Drawable stnIconDrawable = getResources().getDrawable(R.drawable.stn_icon);
        updateVisibleArea();
        newStnClusterer();
        updateOverlays();
        for (Stop s : stopManager) {
            Marker m = s.getMarker();
            if (m != null) {
                m.remove(mapView);
                s.setMarker(null);
            }
            if (stopIsVisible(s)) {
//                System.out.println("Plotting station " + s.getName() + " " + s.getLocn());
                if (s.getLocn() != null) {
                    m = new Marker(mapView);
                    m.setIcon(stnIconDrawable);
                    m.setPosition(gpFromLL(s.getLocn()));

                    m.setInfoWindow(stopInfoWindow);
                    String title = s.getID() + " " + s.getName();
                    for (Route r : s.getRoutes()) {
                        title += "\n" + r.getNumber();
                    }
                    m.setTitle(title);
                    m.setRelatedObject(s);
                    s.setMarker(m);
                    stnClusterer.add(m);
                }
            }
        }
        if (currentLocation != null) {
            LatLon ll = new LatLon(currentLocation.getLatitude(), currentLocation.getLongitude());
            Stop nearest = stopManager.findNearestTo(ll);
            updateMarkerOfNearest(nearest);
        }
    }

    /**
     * Update marker of nearest station (called when user's location has changed).  If nearest is null,
     * no station is marked as the nearest station.
     *
     * @param nearest   station nearest to user's location (null if no station within StationManager.RADIUS metres)
     */
    private void updateMarkerOfNearest(Stop nearest) {
        Drawable stnIconDrawable = getResources().getDrawable(R.drawable.stn_icon);
        Drawable closestStnIconDrawable = getResources().getDrawable(R.drawable.closest_stn_icon);

        if (nearestStnMarker != null) {
            // Reset its icon to the usual one
            nearestStnMarker.setIcon(stnIconDrawable);
            nearestStnMarker = null;
        }
        if (nearest != null) {
            for (Marker m : stnClusterer.getItems()) {
                if (m.getRelatedObject() == nearest) {
                    nearestStnMarker = m;
                    break;
                }
            }
            if (nearestStnMarker != null) {
                nearestStnMarker.setIcon(closestStnIconDrawable);
            }
        }
    }

    /**
     * Centers map at given GeoPoint
     * @param center
     */
    public void centerAt(final GeoPoint center) {
        final IMapController mapController = mapView.getController();

        mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mapController.setZoom(zoomLevel);
                mapController.setCenter(center);
            }
        });
        Log.i(MDF_TAG, "Centered location : " + center);
    }

    /**
     * Find nearest station to user, update nearest station text view and update markers on user location change
     *
     * @param location   the location of the user
     */
    private void handleLocationChange(Location location) {
        System.out.println("Location changed to " + location);
        currentLocation = location;
        LatLon ll = new LatLon(location.getLatitude(), location.getLongitude());
        Stop nearest = stopManager.findNearestTo(ll);
        updateMarkerOfNearest(nearest);
        locationListener.onLocationChanged(nearest, ll);
    }


    /**
     * Get width of line used to plot tube line based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot tube line
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * dpiFactor();
        else
            return 2.0f * dpiFactor();
    }

    /**
     * Close info windows when user taps map.
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        StationInfoWindow.closeAllInfoWindowsOn(mapView);
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    /**
     * Called when user's location has changed - handle location change and repaint map
     *
     * @param location               user's location
     * @param iMyLocationProvider    location provider
     */
    @Override
    public void onLocationChanged(Location location, IMyLocationProvider iMyLocationProvider) {
        Log.i(MDF_TAG, "onLocationChanged");

        handleLocationChange(location);
        mapView.invalidate();
    }

    /**
     * Custom listener for zoom events.  Changes width of line used to plot
     * tube line based on zoom level.
     */
    private class BusRouteListener implements MapListener {

        @Override
        public boolean onScroll(ScrollEvent scrollEvent) {
            IGeoPoint center = mapView.getMapCenter();
            LatLon centre = new LatLon(center.getLatitude(), center.getLongitude());
            System.out.println("Scroll to " + centre);
            locationListener.onMapScroll(centre);
            plotRoutes();
            markStops();

            mapView.invalidate();
            return false;
        }

        @Override
        public boolean onZoom(ZoomEvent zoomEvent) {
            busRouteOverlays.clear();
            plotRoutes();
            markStops();
            updateOverlays();
            return false;
        }
    }
}
