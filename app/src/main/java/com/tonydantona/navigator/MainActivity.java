package com.tonydantona.navigator;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.ProxySetup;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DirectionsListFragment.OnDirectionsDrawerSelectedListener  {
    //<editor-fold desc="declarations">
    // Main mMapView view
    public static MapView mMapView = null;
    ArcGISTiledMapServiceLayer mTileLayer;
    static final String TILE_LAYER_URL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
    GraphicsLayer mRouteLayer, mHiddenSegmentsLayer;

    // UPS route data references (route as imported from data source)
    Rte mOdoRoute;
    List<Stp> mAllStops;
    List<Stp> mUpcomingStops;
    static int mAllStopsIndex = 0;

    // Number of upcoming stops to display (includes next and on-deck)
    final int mUpComingStopCount = 2;

    // Navigation symbols
    static final int LINE_WEIGHT = 4;
    static final int LINE_ALPHA = 128; // 0-255, transparent-opaque
    static final int STOP_SYMBOL_SIZE = 15;

    //final SimpleMarkerSymbol currentLocSymbol = new SimpleMarkerSymbol(Color.RED, STOP_SYMBOL_SIZE + 5, SimpleMarkerSymbol.STYLE.CIRCLE);

    static final LineSymbol NEXT_ROUTE_SYMBOL = new SimpleLineSymbol(Color.GREEN, LINE_WEIGHT, SimpleLineSymbol.STYLE.SOLID).setAlpha(LINE_ALPHA);
    static final LineSymbol ONDECK_ROUTE_SYMBOL = new SimpleLineSymbol(Color.BLUE, LINE_WEIGHT, SimpleLineSymbol.STYLE.DASH).setAlpha(LINE_ALPHA);
    static final LineSymbol FOLLOWING_ROUTE_SYMBOL = new SimpleLineSymbol(Color.MAGENTA, LINE_WEIGHT, SimpleLineSymbol.STYLE.DOT).setAlpha(LINE_ALPHA);

    static final  SimpleMarkerSymbol NEXT_STOP_SYMBOL = new SimpleMarkerSymbol(Color.GREEN, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
    static final SimpleMarkerSymbol ONDECK_STOP_SYMBOL = new SimpleMarkerSymbol(Color.BLUE, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
    static final SimpleMarkerSymbol FOLLOWING_STOP_SYMBOL = new SimpleMarkerSymbol(Color.MAGENTA, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);

    // Drawer layout for directions list
    public static DrawerLayout mDrawerLayout;

    // List of the directions for the current route (used for the ListActivity)
    public static ArrayList<String> mCurrDirections = null;

    // Symbol used to make route segments "invisible"
    SimpleLineSymbol mSegmentHider = new SimpleLineSymbol(Color.WHITE, 5);
    // Symbol used to highlight route segments
    SimpleLineSymbol segmentShower = new SimpleLineSymbol(Color.RED, 5);

    ImageView mImgCurrLocation;
    ImageView mImgGetDirections;
    ImageView mImgCancel;

    // Index of the currently selected route segment (-1 = no selection)
    int mSelectedSegmentID = -1;

    // Label showing the current direction, time, and length
    TextView mTxtViewDirectionsLabel;

    // RouteTask solving references
    Thread[] mThreads = null;
    String mRouteSummary = null;
    static final String ROUTE_TASK_URL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Network/USA/NAServer/Route";
    RouteResult[] mRouteResults = null;
    // Variable to hold server exception to show to user
    Exception mException = null;
    // Update handler
    final Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    // GPS location
    public static Point mLocation = null;
    public LocationManager mLocationManager;

    LocationDisplayManager mLocationDisplayManager;

    // Spatial references used for projecting points
    final SpatialReference mWmSpatialReference = SpatialReference.create(102100);
    final SpatialReference mEgsSpatialReference = SpatialReference.create(4326);

    // Debug log tag
    String TAG;

    enum ResultStatus {
        failed,
        passed
    }

    ResultStatus RouteResultStatus = ResultStatus.passed;

//</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set credentials for UPS network proxy
        setProxyCredentials();

        // Set debug tag
        TAG = getString(R.string.app_name);

        // Retrieve main activity view from XML layout
        setContentView(R.layout.activity_main);

        setupViewEvents();
        setupMapView();
        InitializeLocation();
        LoadRouteData();
    }

    private void InitializeLocation() {
        // Set GPS location service
        mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        // Get the location display manager and start reading location. Don't auto-pan to center our position
        mLocationDisplayManager = mMapView.getLocationDisplayManager();
        mLocationDisplayManager.setLocationListener(new MyLocationListener());
        mLocationDisplayManager.start();
        mLocationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);
    }

    private void setupMapView() {
        // Retrieve the mMapView and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);

        // Add tiled layer to MapView
        mTileLayer = new ArcGISTiledMapServiceLayer(TILE_LAYER_URL);
        mMapView.addLayer(mTileLayer);
        // Add the route graphic layer (shows the full route)
        mRouteLayer = new GraphicsLayer();
        mMapView.addLayer(mRouteLayer);
        // Add the hidden segments layer (for highlighting route segments)
        mHiddenSegmentsLayer = new GraphicsLayer();
        mMapView.addLayer(mHiddenSegmentsLayer);

        // Make the mSegmentHider symbol "invisible"
        mSegmentHider.setAlpha(1);
    }

    private void setupViewEvents()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mImgCancel = (ImageView) findViewById(R.id.iv_cancel);
        mImgCurrLocation = (ImageView) findViewById(R.id.iv_myLocation);
        mImgGetDirections = (ImageView) findViewById(R.id.iv_getDirections);
        mTxtViewDirectionsLabel = (TextView) findViewById(R.id.directionsLabel);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mImgGetDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGetDirectionsClick(v);
            }
        });

        mImgCurrLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCurrLocationClick(v);
            }
        });

    }

    private void LoadRouteData() {
        // Retrieve and parse data
        // Current data source (XMLs): \\bldg-app-pri.MDHUN.us.ups.com\UPSData\PFS\PASSYSTEM\PASDataExport\2016_02_05
        RouteParser routeParser = new XMLRouteParser(MainActivity.this);
        mOdoRoute = routeParser.parseRoute();
        // Create working copy of ODO stop list (editable by user)
        mAllStops = new ArrayList<>();
        for(Stp stp : mOdoRoute.getStps()) {
            mAllStops.add(stp);
        }
    }

    private void handleGetDirectionsClick(View v)
    {
        // clear graphics
        clearAllGraphics();

        // set upcoming stops
        mUpcomingStops = new ArrayList<>();
        for(int i = 0; i < mUpComingStopCount; i++) {
            if(mAllStopsIndex + i < mAllStops.size()) {
                mUpcomingStops.add(i, mAllStops.get(mAllStopsIndex + i));
            }
        }

        // set RouteTask mThreads and results
        mThreads = new Thread[mUpcomingStops.size() + 1];
        mRouteResults = new RouteResult[mUpcomingStops.size() + 1];

        // set point references
        ArrayList<Point> allPoints = new ArrayList<>();
        Point p1;
        Point p2;

        // next stop
        // p1 is current location
        ArrayList<Point> nextPoints = new ArrayList<>();
        p1 = mLocation;
        nextPoints.add(p1);
        allPoints.add(p1);
        p2 = new Point(mUpcomingStops.get(0).getNAPLong(), mUpcomingStops.get(0).getNAPLat());
        nextPoints.add(p2);
        allPoints.add(p2);
        updateRouteResult(0, nextPoints);

        // on-deck and following stops
        int upcomingStopCounter = 1;
        while(upcomingStopCounter < mUpcomingStops.size()) {
            ArrayList<Point> followingPoints = new ArrayList<>();
            p1 = p2;
            followingPoints.add(p1);
            p2 = new Point(mUpcomingStops.get(upcomingStopCounter).getNAPLong(), mUpcomingStops.get(upcomingStopCounter).getNAPLat());
            followingPoints.add(p2);
            allPoints.add(p2);
            updateRouteResult(upcomingStopCounter, followingPoints);
            upcomingStopCounter++;
        }

        // all upcoming stops (for mMapView zoom)
        updateRouteResult(mUpcomingStops.size(), allPoints);

        // wait for mThreads to complete
        try {
            for (Thread thread : mThreads) {
                thread.join();
            }
        } catch(InterruptedException e) {
            mException = e;
            Log.e(TAG, mException.toString());
            RouteResultStatus = ResultStatus.failed;
        }

        if (RouteResultStatus == ResultStatus.failed) {
            Toast.makeText(MainActivity.this, mException.toString(), Toast.LENGTH_LONG).show();
            RouteResultStatus = ResultStatus.passed;
            return;
        }

        // update mMapView with route results
        mHandler.post(mUpdateResults);
    }

    private void handleCurrLocationClick(View v)
    {
        Point p = (Point) GeometryEngine.project(mLocation, mEgsSpatialReference, mWmSpatialReference);
        mMapView.zoomToResolution(p, 20.0);
    }

    private void updateRouteResult(final int leg, final List<Point> points) {
         mThreads[leg] = new Thread() {
            public void run() {
                try {
                    RouteTask routeTask = RouteTask.createOnlineRouteTask(ROUTE_TASK_URL, null);

                    // create route parameters object
                    RouteParameters routeParams = routeTask.retrieveDefaultRouteTaskParameters();

                    // Start building up routing parameters
                    NAFeaturesAsFeature rfaf = new NAFeaturesAsFeature();
                    // Convert point to EGS (decimal degrees)
                    StopGraphic[] stopPoints = new StopGraphic[points.size()];
                    for(int i = 0; i < points.size(); i++) {
                        stopPoints[i] = new StopGraphic(points.get(i));
                    }
                    rfaf.setFeatures(stopPoints);
                    rfaf.setCompressedRequest(true);
                    routeParams.setStops(rfaf);
                    // Set the routing service output SR to our mMapView service's SR
                    routeParams.setOutSpatialReference(mWmSpatialReference);

                    // Solve the route and use the results to update UI when received
                    mRouteResults[leg] = routeTask.solve(routeParams);
                } catch (Exception e) {
                    mException = e;
                    Log.e(TAG, mException.toString());
                    RouteResultStatus = ResultStatus.failed;
                }
            }
        };

        // Start the operation
        mThreads[leg].start();
    }

    private void updateUI() {
        // If no RouteTask results
        if (mRouteResults == null) {
            Toast.makeText(MainActivity.this, mException.toString(), Toast.LENGTH_LONG).show();
            Log.e(TAG, mException.toString());
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

        mCurrDirections = new ArrayList<>();

        Route nextStopRoute = mRouteResults[0].getRoutes().get(0);
        for (RouteDirection rd : nextStopRoute.getRoutingDirections()) {
            HashMap<String, Object> attribs = new HashMap<>();
            attribs.put("text", rd.getText());
            attribs.put("time", rd.getMinutes());
            attribs.put("length", rd.getLength());
            mCurrDirections.add(String.format("%s%n%.1f minutes (%.1f miles)", rd.getText(), rd.getMinutes(), rd.getLength()));
            Graphic routeGraphic = new Graphic(rd.getGeometry(), mSegmentHider, attribs);
            mHiddenSegmentsLayer.addGraphic(routeGraphic);
        }
        // Reset the selected segment
        mSelectedSegmentID = -1;

        // Get the next stop route summary and set it as our current label
        mRouteSummary = String.format("%s%n%.1f minutes (%.1f miles)", nextStopRoute.getRouteName(), nextStopRoute.getTotalMinutes(), nextStopRoute.getTotalMiles());
        mTxtViewDirectionsLabel.setText(mRouteSummary);

        // Replacing the first and last direction segments
        mCurrDirections.remove(0);
        mCurrDirections.add(0, "My Location");

        mCurrDirections.remove(mCurrDirections.size() - 1);
        mCurrDirections.add("Destination");

        // Add the full route graphics, start and destination graphic to the
        // mRouteLayer

        // update path to next stop
        updateRouteLayer(nextStopRoute, NEXT_ROUTE_SYMBOL, NEXT_STOP_SYMBOL);

        // update path to ondeck stop
        if(mRouteResults.length > 1) {
            Route ondeckStopRoute = mRouteResults[1].getRoutes().get(0);
            updateRouteLayer(ondeckStopRoute, ONDECK_ROUTE_SYMBOL, ONDECK_STOP_SYMBOL);
        }

        // update path to following stops
        if(mRouteResults.length > 2) {
            for (int i = 2; i < mRouteResults.length - 1; i++) {
                Route followingStopRoute = mRouteResults[i].getRoutes().get(0);
                updateRouteLayer(followingStopRoute, FOLLOWING_ROUTE_SYMBOL, FOLLOWING_STOP_SYMBOL);
            }
        }

        // update mMapView extent for all upcoming stops
        Route stopShowCountRoute = mRouteResults[mUpComingStopCount].getRoutes().get(0);
        Envelope extent = stopShowCountRoute.getEnvelope();
        extent.inflate(750, 750);
        mMapView.setExtent(extent, 0, true);
    }

    // helper method for updating route layer with a given route (next, on-deck, following)
    private void updateRouteLayer(Route route, Symbol routeSymbol, Symbol stopSymbol) {
        Graphic routeGraphic = new Graphic(route.getRouteGraphic().getGeometry(), routeSymbol);
        Polyline polyline = ((Polyline) route.getRouteGraphic().getGeometry());
        Graphic stopGraphic = new Graphic(polyline.getPoint(polyline.getPointCount() - 1), stopSymbol);

        mRouteLayer.addGraphics(new Graphic[]{routeGraphic, stopGraphic});
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

    public void clearAllGraphics() {
        // Removing the graphics from the layer
        mRouteLayer.removeAll();
        mHiddenSegmentsLayer.removeAll();

        // Locking the Drawer
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Removing the cancel icon
        mImgCancel.setVisibility(View.GONE);

    }

    @Override
    public void onSegmentSelected(String segment) {

    }

    private class MyLocationListener implements LocationListener {

        public MyLocationListener() {
            super();
        }

        // If location changes, update our current location. If being found for the first time, zoom to our current position with a resolution of 20

        public void onLocationChanged(Location loc) {
            if (loc == null)
                return;
            boolean zoomToMe = (mLocation == null) ? true : false;
            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            if (zoomToMe) {
                Point p = (Point) GeometryEngine.project(mLocation, mEgsSpatialReference, mWmSpatialReference);
                mMapView.zoomToResolution(p, 20.0);
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
