package com.tonydantona.navigator.xmlparser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.tonydantona.navigator.R;
import com.tonydantona.navigator.RouteParser;
import com.tonydantona.navigator.datatypes.NavLoad;
import com.tonydantona.navigator.datatypes.NavRoute;
import com.tonydantona.navigator.datatypes.NavStop;
import com.tonydantona.navigator.datatypes.PackageBuilder;
import com.tonydantona.navigator.datatypes.StopBuilder;
import com.tonydantona.navigator.datatypes.TimeUtilities;
import com.tonydantona.navigator.datatypes.Immutables.StopType;
import com.tonydantona.navigator.datatypes.Immutables.SvcBucket;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class XMLRouteParser implements RouteParser {
    private NavRoute route;
    private Context context;
    private TimeUtilities timeUtilities;
    private final String routeTAG = "navroute.xml";
    private final String loadTAG = "navload.xml";
    private final String stopsTAG = "navstop.xml";
    private final String packagesTAG = "navpackage.xml";

    // constructor
    public XMLRouteParser(Context context) {
        super();
        this.context = context;
        route = new NavRoute();
        timeUtilities = new TimeUtilities();
    }

    // public methods
    // parse the route from res/xml files
    public NavRoute parseRoute() {
        long startTime = System.currentTimeMillis();

        parseRouteXML();
        parseLoadXML();
        parseStopsXML();
        parsePackagesXML();
        sortStopsByODO();

        long endTime = System.currentTimeMillis();
        Log.d("XMLRouteParser", String.valueOf(endTime - startTime) + "ms to parse XMLs");
        return route;
    }

    // private methods
    //<editor-fold desc="individual XML file parsers">
    private void parseRouteXML() {
        // get XML resource parser
        XmlResourceParser routeXML = context.getResources().getXml(R.xml.navroute);

        // set initial parse event and element node reference
        int routeEventType = XMLParsingUtilities.GetNextParseEvent(routeXML);
        String routeElementNode = null;
        
        while(routeEventType != XmlResourceParser.END_DOCUMENT) {
            if (routeEventType == XmlResourceParser.START_DOCUMENT) {
                // start of routes XML
                Log.d(routeTAG, "Start XML");
            } else if (routeEventType == XmlResourceParser.START_TAG) {
                // opens a node
                routeElementNode = routeXML.getName();
            } else if (routeEventType == XmlResourceParser.END_TAG) {
                // closes a node -- do nothing
            } else if (routeEventType == XmlResourceParser.TEXT && (routeElementNode != null && routeXML.getText().length() != 0)) {
                // process contents of node
                switch (routeElementNode) {
                    case "RouteID":
                        route.setRouteID(Integer.parseInt(routeXML.getText()));
                        Log.d(routeTAG, routeElementNode + ": " + routeXML.getText());
                        break;
                    case "CountryCode":
                        String countryCode = routeXML.getText();
                        route.setCountryCode(countryCode);
                        Log.d(routeTAG, routeElementNode + ": " + countryCode);
                        break;
                    case "BldgMnemonic":
                        String bldgMnemonic = routeXML.getText();
                        route.setBldgMnemonic(bldgMnemonic);
                        Log.d(routeTAG, routeElementNode + ": " + bldgMnemonic);
                        break;
                    case "SLIC":
                        String slic = routeXML.getText();
                        route.setSLIC(slic);
                        Log.d(routeTAG, routeElementNode + ": " + slic);
                        break;
                    case "RouteName":
                        String routeName = routeXML.getText();
                        route.setRouteName(routeName);
                        Log.d(routeTAG, routeElementNode + ": " + routeName);
                        break;
                    case "DeliveryDate":
                        SimpleDateFormat sdfDeliveryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date deliveryDate = sdfDeliveryDate.parse((routeXML.getText().substring(0, 9)), new ParsePosition(0));
                        route.setDeliveryDate(deliveryDate);
                        timeUtilities.setCurrentDate(deliveryDate);
                        Log.d(routeTAG, routeElementNode + ": " + sdfDeliveryDate.format(deliveryDate));
                        break;
                    case "SchStartTime":
                        //SimpleDateFormat sdfSchStartTime = new SimpleDateFormat("k:mm", Locale.getDefault());
                        //Date schStartTime = sdfSchStartTime.parse(routeXML.getText(), new ParsePosition(0));
                        //route.setSchStartTime(timeUtilities.ToClicks(schStartTime));
                        //Log.d(routeTAG, routeElementNode + ": " + timeUtilities.ToClicks(schStartTime));
                        double schStartTime = Double.parseDouble(routeXML.getText());
                        route.setSchStartTime(schStartTime);
                        Log.d(routeTAG, routeElementNode + ": " + schStartTime);
                        break;
                    case "ODOMiles":
                        double odoMiles = Double.parseDouble(routeXML.getText());
                        route.setODOMiles(odoMiles);
                        Log.d(routeTAG, routeElementNode + ": " + odoMiles);
                        break;
                    case "ODOTime":
                        double odoTime = Double.parseDouble(routeXML.getText());
                        route.setODOTime(odoTime);
                        Log.d(routeTAG, routeElementNode + ": " + odoTime);
                        break;
                    case "BreakESST":
                        double breakESST = Double.parseDouble(routeXML.getText());
                        route.setBreakESST(breakESST);
                        Log.d(routeTAG, routeElementNode + ": " + breakESST);
                        break;
                    case "BreakLSST":
                        double breakLSST = Double.parseDouble(routeXML.getText());
                        route.setBreakLSST(breakLSST);
                        Log.d(routeTAG, routeElementNode + ": " + breakLSST);
                        break;
                    case "BreakDuration":
                        double breakDuration = Double.parseDouble(routeXML.getText());
                        route.setBreakDuration(breakDuration);
                        Log.d(routeTAG, routeElementNode + ": " + breakDuration);
                        break;

                    // unused tag
                    default:
                        Log.e(routeTAG, routeElementNode + ": Unrecognized node");
                        break;
                }
            }

            routeEventType = XMLParsingUtilities.GetNextParseEvent(routeXML);
        }
    }

    private void parseLoadXML() {
        // get XML resource parser
        XmlResourceParser loadXML = context.getResources().getXml(R.xml.navload);
        
        // set initial parse event and element node reference
        int loadEventType = XMLParsingUtilities.GetNextParseEvent(loadXML);
        String loadElementNode = null;

        // load reference
        NavLoad load = new NavLoad();

        while(loadEventType != XmlResourceParser.END_DOCUMENT) {
            if (loadEventType == XmlResourceParser.START_DOCUMENT) {
                // start of load XML
                Log.d(loadTAG, "Start XML");
            } else if (loadEventType == XmlResourceParser.START_TAG) {
                // opens a node
                loadElementNode = loadXML.getName();
            } else if (loadEventType == XmlResourceParser.END_TAG) {
                // closes a node
                // if load is not null, add to route
                if(load.getLoadID() != 0) {
                    route.getLoadList().add(load);
                }

                // set new load
                load = new NavLoad();
            } else if (loadEventType == XmlResourceParser.TEXT && (loadElementNode != null && loadXML.getText().length() != 0)) {
                // process contents of node
                switch (loadElementNode) {
                    case "LoadID":
                        load.setLoadID(Integer.parseInt(loadXML.getText()));
                        Log.d(loadTAG, loadElementNode + ": " + loadXML.getText());
                        break;
                    case "LoadName":
                        load.setLoadName(loadXML.getText());
                        Log.d(loadTAG, loadElementNode + ": " + loadXML.getText());
                        break;

                    // unused tag
                    default:
                        Log.e(loadTAG, "Unrecognized node: " + loadElementNode);
                        break;
                }
            }

            loadEventType = XMLParsingUtilities.GetNextParseEvent(loadXML);
        }
    }

    private void parseStopsXML() {
        // get XML resource parser
        XmlResourceParser stopsXML = context.getResources().getXml(R.xml.navstop);

        // set initial parse event and element node reference
        int stopsEventType = XMLParsingUtilities.GetNextParseEvent(stopsXML);
        String stopsElementNode = null;

        // set initial builder class
        StopBuilder stopBuilder = new StopBuilder(route);

        while(stopsEventType != XmlResourceParser.END_DOCUMENT) {
            if (stopsEventType == XmlResourceParser.START_DOCUMENT) {
                // start of stops XML
                Log.d(stopsTAG, "Start XML");
            } else if (stopsEventType == XmlResourceParser.START_TAG) {
                // opens a node
                stopsElementNode = stopsXML.getName();
            } else if (stopsEventType == XmlResourceParser.END_TAG) {
                // closes a node
                // add stop to a load
                if(stopsXML.getName().equalsIgnoreCase("NavStop")) {
                    // set to load if not null
                    if(stopBuilder.isBuilt()) {
                        stopBuilder.setResult();

                        // set new stopBuilder
                        stopBuilder = new StopBuilder(route);
                    }
                }
            } else if (stopsEventType == XmlResourceParser.TEXT && (stopsElementNode != null && stopsXML.getText().length() != 0)) {
                // process contents of node
                switch (stopsElementNode) {
                    // all NavStop fields
                    case "StopType":
                        // set StopBuilder StopType
                        String stopTypeStr = stopsXML.getText().toUpperCase();
                        switch(stopTypeStr) {
                            case "DELIVERY":
                                stopBuilder.setStopType(StopType.DELIVERY);
                                break;
                            case "PICKUP":
                                stopBuilder.setStopType(StopType.PICKUP);
                                break;
                            case "EOW":
                                stopBuilder.setStopType(StopType.EOW);
                                break;
                            default:
                                Log.e(stopsTAG, "Unrecognized StopType: " + stopTypeStr);
                                break;
                        }
                        break;
                    case "StopID":
                        stopBuilder.setStopID(Integer.parseInt(stopsXML.getText()));
                        break;
                    case "LoadID":
                        stopBuilder.setLoadID(Integer.parseInt(stopsXML.getText()));
                        break;
                    case "ODO":
                        stopBuilder.setODO(Integer.parseInt(stopsXML.getText()));
                        break;
                    case "NAPLat":
                        stopBuilder.setNAPLat(Double.parseDouble(stopsXML.getText()));
                        break;
                    case "NAPLong":
                        stopBuilder.setNAPLong(Double.parseDouble(stopsXML.getText()));
                        break;
                    case "StNumber":
                        stopBuilder.setStNumber(stopsXML.getText());
                        break;
                    case "StPrefix":
                        stopBuilder.setStPrefix(stopsXML.getText());
                        break;
                    case "StName":
                        stopBuilder.setStName(stopsXML.getText());
                        break;
                    case "StSuffix":
                        stopBuilder.setStSuffix(stopsXML.getText());
                        break;
                    case "StType":
                        stopBuilder.setStType(stopsXML.getText());
                        break;
                    case "City":
                        stopBuilder.setCity(stopsXML.getText());
                        break;
                    case "State":
                        stopBuilder.setState(stopsXML.getText());
                        break;
                    case "PostalCode":
                        stopBuilder.setPostalCode(stopsXML.getText());
                        break;
                    case "ODOESST":
                        stopBuilder.setODOESST(Double.parseDouble(stopsXML.getText()));
                        break;
                    case "ESST":
                        stopBuilder.setESST(Double.parseDouble(stopsXML.getText()));
                        break;
                    case "LSST":
                        stopBuilder.setLSST(Double.parseDouble(stopsXML.getText()));
                        break;
                    case "SvcTime":
                        stopBuilder.setSvcTime(Double.parseDouble(stopsXML.getText()));
                        break;
                    // DeliveryStop and PickupStop fields
                    case "SPLat":
                        stopBuilder.setSPLat(Double.parseDouble(stopsXML.getText()));
                        break;
                    case "SPLong":
                        stopBuilder.setSPLong(Double.parseDouble(stopsXML.getText()));
                        break;
                    // DeliveryStop fields
                    case "SvcBucket":
                        String svcBucketStr = stopsXML.getText().toUpperCase();
                        switch(svcBucketStr) {
                            case "PREMIUM":
                                stopBuilder.setSvcBucket(SvcBucket.PREMIUM);
                                break;
                            case "STANDARD":
                                stopBuilder.setSvcBucket(SvcBucket.STANDARD);
                                break;
                            case "SAVER":
                                stopBuilder.setSvcBucket(SvcBucket.SAVER);
                                break;
                            default:
                                Log.e(stopsTAG, "Unrecognized SvcBucket: " + svcBucketStr);
                                break;
                        }
                        break;
                    case "ResCommIndicator":
                        stopBuilder.setResCommIndicator(Boolean.parseBoolean(stopsXML.getText()));
                        break;
                    // PickupStop fields
                    case "PickupPoint":
                        stopBuilder.setPickupPoint(stopsXML.getText());
                        break;
                    case "PickupShipperName":
                        stopBuilder.setShipperName(stopsXML.getText());
                        break;
                    case "PickupShipperNumber":
                        stopBuilder.setShipperNumber(stopsXML.getText());
                        break;

                    // unused tag
                    default:
                        Log.e(stopsTAG, "Unrecognized node: " + stopsElementNode);
                        break;
                }
            }

            stopsEventType = XMLParsingUtilities.GetNextParseEvent(stopsXML);
        }
    }

    private void parsePackagesXML() {
        // get XML resource parser
        XmlResourceParser packagesXML = context.getResources().getXml(R.xml.navpackage);

        // set initial parse event and element node reference
        int packagesEventType = XMLParsingUtilities.GetNextParseEvent(packagesXML);
        String packagesElementNode = null;

        // set initial package builder
        PackageBuilder packageBuilder = new PackageBuilder(route);

        while(packagesEventType != XmlResourceParser.END_DOCUMENT) {
            if (packagesEventType == XmlResourceParser.START_DOCUMENT) {
                // start of packages XML
                Log.d(packagesTAG, "Start XML");
            } else if (packagesEventType == XmlResourceParser.START_TAG) {
                // opens a node
                packagesElementNode = packagesXML.getName();
            } else if (packagesEventType == XmlResourceParser.END_TAG) {
                // closes a node
                // add package to a stop
                if (packagesXML.getName().equalsIgnoreCase("NavPackage")) {
                    // set to load if not null
                    if (packageBuilder.isBuilt()) {
                        packageBuilder.setResult();

                        // set new stopBuilder
                        packageBuilder = new PackageBuilder(route);
                    }
                }
            } else if (packagesEventType == XmlResourceParser.TEXT && (packagesElementNode != null && packagesXML.getText().length() != 0)) {
                // process contents of node
                switch (packagesElementNode) {
                    case "PkgID":
                        packageBuilder.setPackageID(Integer.parseInt(packagesXML.getText()));
                        break;
                    case "StopID":
                        packageBuilder.setStopID(Integer.parseInt(packagesXML.getText()));
                        break;
                    case "PlannedConsignee":
                        packageBuilder.setPlannedConsignee(packagesXML.getText());
                        break;
                    case "Consignee":
                        packageBuilder.setConsignee(packagesXML.getText());
                        break;
                    case "TrackingNumber":
                        packageBuilder.setTrackingNumber(packagesXML.getText());
                        break;
                    case "RightSideHIN":
                        packageBuilder.setRightSideHIN(packagesXML.getText());
                        break;

                    // unused tag
                    default:
                        Log.e(packagesTAG, "Unrecognized node: " + packagesElementNode);
                        break;
                }
            }

            packagesEventType = XMLParsingUtilities.GetNextParseEvent(packagesXML);
        }
    }
    //</editor-fold>

    private void sortStopsByODO() {
        // sort stops within each load
        for(NavLoad load : route.getLoadList()) {
            Collections.sort(load.getStopList(), new Comparator<NavStop>() {
                @Override
                public int compare(NavStop stop1, NavStop stop2) {
                    return ((Integer) stop1.getODO()).compareTo(stop2.getODO());
                }
            });
        }

        // check for no-stop loads, prevents null exception when sorting loads below
        List<NavLoad> loadList = route.getLoadList();
        for(NavLoad load : loadList) {
            if(load.getStopList().isEmpty()) {
                loadList.remove(load);
            }
        }

        // sort loads by first stop ODO of each load
        Collections.sort(route.getLoadList(), new Comparator<NavLoad>() {
            @Override
            public int compare(NavLoad load1, NavLoad load2) {
                return ((Integer) load1.getStopList().get(0).getODO()).compareTo(load2.getStopList().get(0).getODO());
            }
        });
    }
}
