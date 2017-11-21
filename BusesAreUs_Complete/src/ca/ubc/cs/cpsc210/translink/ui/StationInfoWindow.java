package ca.ubc.cs.cpsc210.translink.ui;

import android.view.View;
import android.widget.Button;
import ca.ubc.cs.cpsc210.translink.R;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.views.MapView;

/**
 * StationInfoWindow displayed when station is tapped
 */
class StationInfoWindow extends MarkerInfoWindow {
    private StationSelectionListener stnSelectionListener;

    /**
     * Constructor
     *
     * @param listener   listener to handle user selection of station
     * @param mapView    the map view on which this info window will be displayed
     */
    public StationInfoWindow(StationSelectionListener listener, MapView mapView) {
        super(R.layout.bonuspack_bubble, mapView);
        stnSelectionListener = listener;

        Button btn = (Button) (mView.findViewById(R.id.bubble_moreinfo));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stop selected = (Stop) mMarkerRef.getRelatedObject();
                stnSelectionListener.onStationSelected(selected);
                StationInfoWindow.this.close();
            }
        });
    }

    @Override public void onOpen(Object item){
        super.onOpen(item);
        mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
        System.out.println("StationInfoWindow open");
    }
}
