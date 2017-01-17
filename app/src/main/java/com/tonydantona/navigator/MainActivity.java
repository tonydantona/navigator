package com.tonydantona.navigator;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
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
import com.tonydantona.navigator.datatypes.Address;
import com.tonydantona.navigator.datatypes.NavRoute;
import com.tonydantona.navigator.datatypes.NavStop;
import com.tonydantona.navigator.xmlparser.XMLRouteParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements DirectionsListFragment.OnDirectionsDrawerSelectedListener, LocationServices.ILocationServices {
    //<editor-fold desc="declarations">
    // Main mMapView view
    public static MapView mMapView = null;
    ArcGISTiledMapServiceLayer mTileLayer;
    static final String TILE_LAYER_URL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
    //    static final String TILE_LAYER_URL = "http://wksp0004e94f:6080/arcgis/rest/services/USNatMap/USStreetsCache/MapServer";
    GraphicsLayer mRouteLayer;
    GraphicsLayer mHiddenSegmentsLayer;
    GraphicsLayer mStopsLayer;
    GraphicsLayer mCurrentLocationLayer;

    // UPS route data references (route as imported from data source)
    NavRoute mOdoRoute;
    List<NavStop> mUnCompletedStops;

    // Number of stops to route
    final int mNumStopsToRoute = 1;

    // Number of stops to show
    static final int mNumStopsToShow = 1;

    // Navigation symbols
    static final int LINE_WEIGHT = 4;
    static final int LINE_ALPHA = 128; // 0-255, transparent-opaque
    static final int STOP_SYMBOL_SIZE = 15;

    static final LineSymbol NEXT_ROUTE_SYMBOL = new SimpleLineSymbol(Color.GREEN, LINE_WEIGHT, SimpleLineSymbol.STYLE.SOLID).setAlpha(LINE_ALPHA);
    static final LineSymbol ONDECK_ROUTE_SYMBOL = new SimpleLineSymbol(Color.BLUE, LINE_WEIGHT, SimpleLineSymbol.STYLE.DASH).setAlpha(LINE_ALPHA);
    static final LineSymbol FOLLOWING_ROUTE_SYMBOL = new SimpleLineSymbol(Color.MAGENTA, LINE_WEIGHT, SimpleLineSymbol.STYLE.DOT).setAlpha(LINE_ALPHA);

    static final SimpleMarkerSymbol CURRENT_LOCATION_SYMBOL = new SimpleMarkerSymbol(Color.BLUE, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.TRIANGLE);
    static final SimpleMarkerSymbol NEXT_STOP_SYMBOL = new SimpleMarkerSymbol(Color.RED, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
    static final SimpleMarkerSymbol ONDECK_STOP_SYMBOL = new SimpleMarkerSymbol(Color.GREEN, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
    static final SimpleMarkerSymbol FOLLOWING_STOP_SYMBOL = new SimpleMarkerSymbol(Color.MAGENTA, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
    static final SimpleMarkerSymbol DELIVERY_STOP_SYMBOL = new SimpleMarkerSymbol(Color.BLUE, STOP_SYMBOL_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);

    // Drawer layout for directions list
    public static  DrawerLayout mDrawerLayout;

    // List of the directions for the current route (used for the ListActivity)
    public static ArrayList<String> mCurrDirections = null;

    // Symbol used to make route segments "invisible"
    SimpleLineSymbol mSegmentHider = new SimpleLineSymbol(Color.WHITE, 5);

    ImageView mImgCurrLocation;
    ImageView mImgGetDirections;
    ImageView mImgCancel;
    ImageView mImgCompleteStop;

    // Index of the currently selected route segment (-1 = no selection)
    int mSelectedSegmentID = -1;

    // Label showing the current direction, time, and length
    TextView mTxtViewDirectionsLabel;

    // Label showing the destination address
    TextView mTxtViewDestinationLabel;

    // RouteTask solving references
    Thread[] mThreads = null;
    static final String ROUTE_TASK_URL = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Network/USA/NAServer/Route";
    RouteResult[] mRouteResults = null;
    Route mCurrRoute = null;
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
    private Point mCurrentLocation = null;

    // Spatial references used for projecting points
    public static final SpatialReference mWmSpatialReference = SpatialReference.create(102100);
    public static final SpatialReference mEgsSpatialReference = SpatialReference.create(4326);

    // Debug log tag
    String TAG;

    enum ResultStatus {
        failed,
        passed
    }

    ResultStatus RouteResultStatus = ResultStatus.passed;

    // declared at class level for closure (inner class) reasons
    int stopIndex;

//</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        // Set credentials for UPS network proxy
//        setProxyCredentials();

        // Retrieve main activity view from XML layout
        setContentView(R.layout.activity_main);

        TAG = getString(R.string.app_name);
        setupMapView();
        setupViewEvents();
        initializeLocation();
        loadRouteData();

        plotDeliveryStops();
    }

    private void setupViewEvents() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mImgCancel = (ImageView) findViewById(R.id.iv_cancel);
        mImgCurrLocation = (ImageView) findViewById(R.id.iv_myLocation);
        mImgGetDirections = (ImageView) findViewById(R.id.iv_getDirections);
        mTxtViewDirectionsLabel = (TextView) findViewById(R.id.directionsLabel);
        mTxtViewDestinationLabel = (TextView) findViewById(R.id.destinationLabel);
        mImgCompleteStop = (ImageView) findViewById(R.id.iv_completeStop);

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

        mImgCompleteStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              handleCompleteStopClick(v);
            }
        });
    }

    private void setupMapView() {
        // Retrieve the mMapView and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);

        // Add tiled layer to MapView
        mTileLayer = new ArcGISTiledMapServiceLayer(TILE_LAYER_URL);
        mMapView.addLayer(mTileLayer);

        // Add the stops graphic layer
        mStopsLayer = new GraphicsLayer();
        mMapView.addLayer(mStopsLayer);

        // Add the current Location layer
        mCurrentLocationLayer = new GraphicsLayer();
        mMapView.addLayer(mCurrentLocationLayer);

        // Add the route graphic layer (shows the full route)
        mRouteLayer = new GraphicsLayer();
        mMapView.addLayer(mRouteLayer);

        // Add the hidden segments layer (for highlighting route segments)
        mHiddenSegmentsLayer = new GraphicsLayer();
        mMapView.addLayer(mHiddenSegmentsLayer);

        // Make the mSegmentHider symbol "invisible"
        mSegmentHider.setAlpha(1);
    }

    private void initializeLocation() {
        try {

            // Get our current location, position and zoom
            LocationServices.getLocationManager(this);
            mCurrentLocation = new Point(LocationServices.getLocation().getLongitude(), LocationServices.getLocation().getLatitude());
            addPointToGraphicsLayer(mCurrentLocation, CURRENT_LOCATION_SYMBOL, mCurrentLocationLayer);

            Point p = (Point) GeometryEngine.project(mCurrentLocation, mEgsSpatialReference, mWmSpatialReference);
            mMapView.zoomToResolution(p, 20.0);



            // All this ESRI LocationDisplayManager does now (since I now use android location) is give me the blinking circle for curr location
//            LocationDisplayManager ldm = MainActivity.mMapView.getLocationDisplayManager();
//          this was already commented out: ldm.setLocationListener(this);
//            ldm.start();
            //Don't auto-pan to center our position
//            ldm.setAutoPanMode(LocationDisplayManager.AutoPanMode.NAVIGATION);
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRouteData() {
        // Retrieve and parse data
        RouteParser routeParser = new XMLRouteParser(MainActivity.this);
        mOdoRoute = routeParser.parseRoute();
        // Create working copy of ODO stop list (editable by user)
        mUnCompletedStops = new ArrayList<>();
        for(NavStop stop : mOdoRoute.getAllStopList()) {
            mUnCompletedStops.add(stop);
        }
    }

    private void getDirections() {
        // clear graphics
        clearAllGraphics();

        // create threads for running route tasks
        mThreads = new Thread[mNumStopsToRoute + 1];

        // create array to hold route results
        mRouteResults = new RouteResult[mNumStopsToRoute + 1];


        // create and populate a collection to hold points to be routed
        final ArrayList<Point> routeStops = new ArrayList<>();
        routeStops.add(mCurrentLocation);
        for (int i = 0; i < mNumStopsToRoute; i++) {
            routeStops.add(new Point(mUnCompletedStops.get(i).getNAPLong(), mUnCompletedStops.get(i).getNAPLat()));
        }

        // route source to destination for each point in the collection
        stopIndex = 0;
        while (stopIndex < mNumStopsToRoute) {
            updateRouteResult(stopIndex, new ArrayList<Point>() {{
                add(routeStops.get(stopIndex));
                add(routeStops.get(stopIndex + 1));
            }});
            stopIndex++;
        }

        // all upcoming stops (for mMapView zoom)
        updateRouteResult(mNumStopsToRoute, routeStops);

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

    private void handleGetDirectionsClick(View v) {
       getDirections();
    }

    private void handleCurrLocationClick(View v)
    {
        try {
            Point p = (Point) GeometryEngine.project(mCurrentLocation, mEgsSpatialReference, mWmSpatialReference);
            mMapView.zoomToResolution(p, 20.0);
        }
        catch( Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCompleteStopClick(View v) {
        if (!mUnCompletedStops.isEmpty()) {
            mUnCompletedStops.remove(0);
            clearAllGraphics();
            removePlottedStopGraphic();
            plotDeliveryStops();
            getDirections();
        }
    }

    private void plotDeliveryStops() {

        ArrayList<SimpleMarkerSymbol> stopSymbols = new ArrayList<>();
        stopSymbols.add(NEXT_STOP_SYMBOL);
        stopSymbols.add(ONDECK_STOP_SYMBOL);
        stopSymbols.add(FOLLOWING_STOP_SYMBOL);

        // create and populate a collection to hold points to be plotted
        final ArrayList<Point> stopsToDisplay = new ArrayList<>();
        for (int i = 0; i < mNumStopsToShow; i++) {
            stopsToDisplay.add(new Point(mUnCompletedStops.get(i).getNAPLong(), mUnCompletedStops.get(i).getNAPLat()));
        }

        SimpleMarkerSymbol symbolToShow;
        for (int i = 0; i < stopsToDisplay.size(); i++) {

            if ( i < mNumStopsToRoute) {
                symbolToShow = stopSymbols.get(i);
            }
            else {
                symbolToShow = DELIVERY_STOP_SYMBOL;
            }

            addPointToGraphicsLayer(stopsToDisplay.get(i), symbolToShow, mStopsLayer);

            String address = "";
            // only show the first uncompleted stop as destination
            if( i == 0) {
                NavStop navStop = mUnCompletedStops.get(i);
                Address addressType = navStop.getAddress();
                address = addressType.getStNumber() + " " + addressType.getStName() + " " + addressType.getStType() + " " + addressType.getCity();
            }

            mTxtViewDestinationLabel.setText(address);
        }
    }

    public void addPointToGraphicsLayer(Point point, Symbol stopSymbol, GraphicsLayer graphicsLayer) {

        Point p = (Point) GeometryEngine.project(point, mEgsSpatialReference, mWmSpatialReference);
        Graphic plotPoint = new Graphic(p, stopSymbol);
        graphicsLayer.addGraphic(plotPoint);
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
        // hiddenSegmentsLayer, and add the direction information to the list of directions

        mCurrDirections = new ArrayList<>();
        mCurrRoute = mRouteResults[0].getRoutes().get(0);
        for (RouteDirection rd : mCurrRoute.getRoutingDirections()) {
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

        Calendar now = Calendar.getInstance();

        Calendar eta = (Calendar) now.clone();
        // add to current time to get eta
        eta.add(Calendar.MINUTE, (int) mCurrRoute.getTotalMinutes());

        DateFormat df = new SimpleDateFormat("h:mm a");
        String etaTime = df.format(eta.getTime());

        //String distToStop = String.format("%.1f miles", mCurrRoute.getTotalMiles());


        mTxtViewDirectionsLabel.setText(String.format("%s%s%n%s%s","ETA: ", etaTime, "Dist To Stop: ", String.format("%.1f miles", mCurrRoute.getTotalMiles())));



        // Replacing the first and last direction segments
        mCurrDirections.remove(0);
        mCurrDirections.add(0, "My Location");

        mCurrDirections.remove(mCurrDirections.size() - 1);
        mCurrDirections.add("Destination");

        // update path to next stop
        updateRouteLayer(mCurrRoute, NEXT_ROUTE_SYMBOL);

        // update path to ondeck stop
        if(mRouteResults.length > 1) {
            Route ondeckStopRoute = mRouteResults[1].getRoutes().get(0);
            updateRouteLayer(ondeckStopRoute, ONDECK_ROUTE_SYMBOL);
        }

        // update path to following stops
        if(mRouteResults.length > 2) {
            for (int i = 2; i < mRouteResults.length - 1; i++) {
                Route followingStopRoute = mRouteResults[i].getRoutes().get(0);
                updateRouteLayer(followingStopRoute, FOLLOWING_ROUTE_SYMBOL);
            }
        }

        // update mMapView extent for all upcoming stops
        Route stopShowCountRoute = mRouteResults[mNumStopsToRoute].getRoutes().get(0);
        Envelope extent = stopShowCountRoute.getEnvelope();
        extent.inflate(750, 750);
        mMapView.setExtent(extent, 0, true);
    }

    // helper method for updating route layer with a given route (next, on-deck, following)
    private void updateRouteLayer(Route route, Symbol routeSymbol) {
        mRouteLayer.addGraphic(new Graphic(route.getRouteGraphic().getGeometry(), routeSymbol));
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

    public void removeCurrentLocationGraphic() {
        mCurrentLocationLayer.removeAll();
    }

    public void removePlottedStopGraphic() {
        mStopsLayer.removeAll();
    }

    @Override
    public void onSegmentSelected(String segment) {
        if (segment == null)
            return;
        // Look for the graphic that corresponds to this direction
        for (int index : mHiddenSegmentsLayer.getGraphicIDs()) {
            Graphic g = mHiddenSegmentsLayer.getGraphic(index);
            if (segment.contains((String) g.getAttributeValue("text"))) {
                // When found, hide the currently selected, show the new
                // selection
                mHiddenSegmentsLayer.updateGraphic(mSelectedSegmentID, mSegmentHider);
                mHiddenSegmentsLayer.updateGraphic(index, mSegmentHider);
                mSelectedSegmentID = index;
                // Update label with information for that direction
                mTxtViewDirectionsLabel.setText(segment);
                // Zoom to the extent of that segment
                mMapView.setExtent(mHiddenSegmentsLayer.getGraphic(mSelectedSegmentID)
                        .getGeometry(), 50);
                break;
            }
        }
    }

    @Override
    public void onLocationServicesLocationChange(Location location) {
        boolean zoomToMe = (mCurrentLocation == null);
        removeCurrentLocationGraphic();
        mCurrentLocation = new Point(location.getLongitude(), location.getLatitude());
        addPointToGraphicsLayer(mCurrentLocation, CURRENT_LOCATION_SYMBOL, mCurrentLocationLayer);
        if (zoomToMe) {
            Point p = (Point) GeometryEngine.project(mCurrentLocation, mEgsSpatialReference, mWmSpatialReference);
            mMapView.zoomToResolution(p, 20.0);
        }
    }

    @Override
    public void onLocationServicesStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onLocationServicesProviderEnabled(String provider) {
        Toast.makeText(getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationServicesProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
        buildAlertMessageNoGps();
    }

    public void buildAlertMessageNoGps() {
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
