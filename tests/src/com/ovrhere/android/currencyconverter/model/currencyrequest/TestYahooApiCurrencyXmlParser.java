package com.ovrhere.android.currencyconverter.model.currencyrequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;

import com.ovrhere.android.currencyconverter.test.UtilityTestMethods;

public class TestYahooApiCurrencyXmlParser extends TestCase {

	//Source: http://query.yahooapis.com/v1/public/yql/?q=select * from yahoo.finance.xchange where pair in ("AUDBBD", "CADAUD", "USDJPY")&env=store://datatables.org/alltableswithkeys
	//Fetched: 2015-05-24
	private static final String TEST_XML_RATES = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	"<query xmlns:yahoo=\"http://www.yahooapis.com/v1/base.rng\" yahoo:count=\"3\" yahoo:created=\"2015-05-24T04:00:46Z\" yahoo:lang=\"en-US\"><results><rate id=\"AUDBBD\"><Name>AUD/BBD</Name><Rate>1.5645</Rate><Date>5/23/2015</Date><Time>12:55pm</Time><Ask>1.5668</Ask><Bid>1.5622</Bid></rate><rate id=\"CADAUD\"><Name>CAD/AUD</Name><Rate>1.0408</Rate><Date>5/23/2015</Date><Time>12:55pm</Time><Ask>1.0431</Ask><Bid>1.0385</Bid></rate><rate id=\"USDJPY\"><Name>USD/JPY</Name><Rate>121.5350</Rate><Date>5/23/2015</Date><Time>12:55pm</Time><Ask>121.6000</Ask><Bid>121.5350</Bid></rate></results></query><!-- total: 13 -->"+
	"<!-- pprd1-node1016-lh1.manhattan.bf1.yahoo.com -->";
	
	/*
	 *  <rate id="AUDBBD">
        <Name>AUD/BBD</Name>
        <Rate>1.5645</Rate>
        ...
    </rate>
    <rate id="CADAUD">
        <Name>CAD/AUD</Name>
        <Rate>1.0408</Rate>
        ...
    </rate>
    <rate id="USDJPY">
        <Name>USD/JPY</Name>
        <Rate>121.5350</Rate>
        ...
    </rate>
    
		Src	Dst	Rate
		AUD	BBD	1.564500
		CAD	AUD	1.040800
		USD	JPY	121.535000
				
		Flipped		
		BBD	AUD	0.639182
		AUD	CAD	0.960799
		JPY	USD	0.008228

	 */
	
	private static final ContentValues[] TEST_EXPECTED_RATES = new ContentValues[] {
		new CodeRatePair("AUDBBD",	1.5645d).toContentValues(),
		new CodeRatePair("CADAUD",	1.0408d).toContentValues(),
		new CodeRatePair("USDJPY",	121.535d).toContentValues(),

	    new CodeRatePair("BBDAUD",	0.639182d).toContentValues(),
	    new CodeRatePair("AUDCAD",	0.960799d).toContentValues(),
	    new CodeRatePair("JPYUSD",	0.008228d).toContentValues(),
	};
	
	
	
	@Test
	public void testParseXmlStreamInputStream() throws XmlPullParserException, IOException {
		// convert String into InputStream
		final InputStream fakeXmlStream = new ByteArrayInputStream(TEST_XML_RATES.getBytes());
		
		final int EXPECTED_SIZE = TEST_EXPECTED_RATES.length;
		ContentValues[] results = new YahooApiCurrencyXmlParser().parseXmlStream(fakeXmlStream);
		
		assertEquals("Did not get the expected number of results", EXPECTED_SIZE, results.length);
		
		UtilityTestMethods.validateContentValues("Mismatch found for xml", TEST_EXPECTED_RATES, results);		
	}



}
