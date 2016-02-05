package com.tonydantona.navigator;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.ProxySetup;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements DirectionsListFragment.OnDirectionsDrawerSelectedListener  {
//<editor-fold desc="declarations">
    public static MapView map = null;
    ArcGISTiledMapServiceLayer tileLayer;
    GraphicsLayer routeLayer, hiddenSegmentsLayer;

    // Symbol used to make route segments "invisible"
    SimpleLineSymbol segmentHider = new SimpleLineSymbol(Color.WHITE, 5);
    // Symbol used to highlight route segments
    SimpleLineSymbol segmentShower = new SimpleLineSymbol(Color.RED, 5);

    ImageView img_currLocation;
    ImageView img_getDirections;
    ImageView img_cancel;

    // Index of the currently selected route segment (-1 = no selection)
    int selectedSegmentID = -1;

    // Label showing the current direction, time, and length
    TextView directionsLabel;

    // Progress dialog to show when route is being calculated
    ProgressDialog mDialog;

    // List of the directions for the current route (used for the ListActivity)
    public static ArrayList<String> mCurrDirections = null;
    // Current route, route summary, and gps location
    Route mCurrRoute = null;
    String mRouteSummary = null;
    RouteTask mRouteTask = null;
    RouteResult mResults = null;
    // Variable to hold server exception to show to user
    Exception mException = null;
    final Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    public static DrawerLayout mDrawerLayout;

    public static Point mLocation = null;
    public LocationManager manager;
    LocationDisplayManager ldm;

    // Spatial references used for projecting points
    final SpatialReference wm = SpatialReference.create(102100);
    final SpatialReference egs = SpatialReference.create(4326);
//</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setProxyCredentials();

        setContentView(R.layout.activity_main);

        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        // Retrieve the map and initial extent from XML layout
        map = (MapView) findViewById(R.id.map);
        // Add tiled layer to MapView
        tileLayer = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        map.addLayer(tileLayer);

        // Add the route graphic layer (shows the full route)
        routeLayer = new GraphicsLayer();
        map.addLayer(routeLayer);

        // Initialize the RouteTask
        try {
            mRouteTask = RouteTask
                    .createOnlineRouteTask(
                            "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Network/USA/NAServer/Route",
                            null);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // Add the hidden segments layer (for highlighting route segments)
        hiddenSegmentsLayer = new GraphicsLayer();
        map.addLayer(hiddenSegmentsLayer);

        // Make the segmentHider symbol "invisible"
        segmentHider.setAlpha(1);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        img_cancel = (ImageView) findViewById(R.id.iv_cancel);
        img_currLocation = (ImageView) findViewById(R.id.iv_myLocation);
        img_getDirections = (ImageView) findViewById(R.id.iv_getDirections);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Get the location display manager and start reading location. Don't
        // auto-pan
        // to center our position
        ldm = map.getLocationDisplayManager();
        ldm.setLocationListener(new MyLocationListener());
        ldm.start();
        ldm.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);

        img_currLocation = (ImageView) findViewById(R.id.iv_myLocation);
        img_currLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
                map.zoomToResolution(p, 20.0);

            }
        });


        // On single clicking the directions label, start a ListActivity to show
        // the list of all directions for this route. Selecting one of those
        // items will return to the map and highlight that segment.
        directionsLabel = (TextView) findViewById(R.id.directionsLabel);
        img_getDirections = (ImageView) findViewById(R.id.iv_getDirections);
        img_getDirections.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
//                if (curDirections == null)
//                    return;

                Toast.makeText(MainActivity.this, "DirectionsLabel Clicked", Toast.LENGTH_LONG).show();
                mDrawerLayout.openDrawer(GravityCompat.END);
//
//                String segment = directionsLabel.getText().toString();

//                ListView lv = RoutingListFragment.mDrawerList;
//                for (int i = 0; i < lv.getCount() - 1; i++) {
//                    String lv_segment = lv.getItemAtPosition(i).toString();
//                    if (segment.equals(lv_segment)) {
//                        lv.setSelection(i);
//                    }
//                }
                Point p1 = new Point(-76.644621, 39.515594);
                Point p2 = new Point(-76.6381856, 39.3673428);

                clearAll();

                // Adding the symbol for start point
                SimpleMarkerSymbol startSymbol = new SimpleMarkerSymbol(Color.DKGRAY, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
                Graphic gStart = new Graphic(mLocation, startSymbol);
                routeLayer.addGraphic(gStart);

                queryDirections(p1, p2);
                }
        });
    }


    private void queryDirections(final Point mLocation, final Point p) {

        // Show that the route is calculating
        //mDialog = ProgressDialog.show(MainActivity.this, "Routing Sample", "Calculating route...", true);
        // Spawn the request off in a new thread to keep UI responsive
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    // Start building up routing parameters
                    RouteParameters rp = mRouteTask.retrieveDefaultRouteTaskParameters();
                    NAFeaturesAsFeature rfaf = new NAFeaturesAsFeature();
                    // Convert point to EGS (decimal degrees)
                    StopGraphic point1 = new StopGraphic(mLocation);
                    StopGraphic point2 = new StopGraphic(p);
                    rfaf.setFeatures(new Graphic[]{point1, point2});
                    rfaf.setCompressedRequest(true);
                    rp.setStops(rfaf);
                    // Set the routing service output SR to our map service's SR
                    rp.setOutSpatialReference(wm);

                    // Solve the route and use the results to update UI when received
                    mResults = mRouteTask.solve(rp);
                    mHandler.post(mUpdateResults);
                } catch (Exception e) {
                    mException = e;
                    mHandler.post(mUpdateResults);
                }
            }
        };
        // Start the operation
        t.start();

    }

    private void updateUI() {
        if (mResults == null) {
            Toast.makeText(MainActivity.this, mException.toString(), Toast.LENGTH_LONG).show();
            mCurrDirections = null;
            return;
        }

        // Creating a fragment if it has not been created
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag("Nav Drawer") == null) {
            FragmentTransaction ft = fm.beginTransaction();
            DirectionsListFragment frag = DirectionsListFragment.newInstance();
            ft.add(frag, "Nav Drawer");
            ft.commit();
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fm.findFragmentByTag("Nav Drawer"));
            DirectionsListFragment frag = DirectionsListFragment.newInstance();
            ft.add(frag, "Nav Drawer");
            ft.commit();
        }

        // Unlock the Navigation Drawer
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // Add all the route segments with their relevant information to the
        // hiddenSegmentsLayer, and add the direction information to the list
        // of directions
        // Symbols for the route and the destination (blue line, checker flag)
        SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);
        PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(map.getContext(), getResources().getDrawable(R.drawable.ic_action_place));

        mCurrRoute = mResults.getRoutes().get(0);
        for (RouteDirection rd : mCurrRoute.getRoutingDirections()) {
            HashMap<String, Object> attribs = new HashMap<String, Object>();
            attribs.put("text", rd.getText());
            attribs.put("time", rd.getMinutes());
            attribs.put("length", rd.getLength());
            mCurrDirections.add(String.format("%s%n%.1f minutes (%.1f miles)", rd.getText(), rd.getMinutes(), rd.getLength()));
            Graphic routeGraphic = new Graphic(rd.getGeometry(), segmentHider, attribs);
            hiddenSegmentsLayer.addGraphic(routeGraphic);
        }
        // Reset the selected segment
        selectedSegmentID = -1;

        // Add the full route graphics, start and destination graphic to the
        // routeLayer
        Graphic routeGraphic = new Graphic(mCurrRoute.getRouteGraphic().getGeometry(), routeSymbol);
        Graphic endGraphic = new Graphic(((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic.getGeometry()).getPointCount() - 1), destinationSymbol);
        routeLayer.addGraphics(new Graphic[]{routeGraphic, endGraphic});
        // Get the full route summary and set it as our current label
        mRouteSummary = String.format("%s%n%.1f minutes (%.1f miles)", mCurrRoute.getRouteName(), mCurrRoute.getTotalMinutes(), mCurrRoute.getTotalMiles());

        directionsLabel.setText(mRouteSummary);
        // Zoom to the extent of the entire route with a padding
        map.setExtent(mCurrRoute.getEnvelope(), 250);

        // Replacing the first and last direction segments
        mCurrDirections.remove(0);
        mCurrDirections.add(0, "My Location");

        mCurrDirections.remove(mCurrDirections.size() - 1);
        mCurrDirections.add("Destination");

//        MyAdapter adapter = new MyAdapter(this,MainActivity.mCurrDirections);
//        mDrawerLayout.openDrawer(GravityCompat.END);
//        ListView rightDrawer = (ListView) findViewById(R.id.right_drawer);
//        rightDrawer.setAdapter(adapter);

        //ListView dirList = (ListView) findViewById(R.id.directionsList);
        //dirList.setAdapter(adapter);
    }

    private void setProxyCredentials() {
        String proxyHost = "proxy.ismd.ups.com";
        int proxyPort = 8080;
        UserCredentials proxyCredentials = new UserCredentials();
        proxyCredentials.setUserAccount(getString(R.string.proxyUserName), getString(R.string.proxyPassword));
        try {
            ProxySetup.setupProxy(proxyHost, proxyPort, proxyCredentials);
        } catch (EsriSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
	 * Clear the graphics and empty the directions list
	 */

    public void clearAll() {

        //Removing the graphics from the layer
        routeLayer.removeAll();
        hiddenSegmentsLayer.removeAll();

        mCurrDirections = new ArrayList<String>();
        mResults = null;
        mCurrRoute = null;

        //Locking the Drawer
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //Removing the cancel icon
        img_cancel.setVisibility(View.GONE);

    }

    @Override
    public void onSegmentSelected(String segment) {

    }

    private class MyLocationListener implements LocationListener {

        public MyLocationListener() {
            super();
        }

        /**
         * If location changes, update our current location. If being found for
         * the first time, zoom to our current position with a resolution of 20
         */
        public void onLocationChanged(Location loc) {
            if (loc == null)
                return;
            boolean zoomToMe = (mLocation == null) ? true : false;
            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            if (zoomToMe) {
                Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
                map.zoomToResolution(p, 20.0);
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Disabled",
                    Toast.LENGTH_SHORT).show();
            buildAlertMessageNoGps();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enable your GPS before proceeding")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
