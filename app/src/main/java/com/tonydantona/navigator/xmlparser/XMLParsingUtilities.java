package com.tonydantona.navigator.xmlparser;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

// utility function for XML resource (res/xml) parsing
public class XMLParsingUtilities {

    // returns type of the next XML parsing event
    public static int GetNextParseEvent(XmlResourceParser xmlResourceParser) {
        try {
            return xmlResourceParser.next();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
