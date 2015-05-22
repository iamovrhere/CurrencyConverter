package com.ovrhere.android.currencyconverter.model.currencyrequest;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.ovrhere.android.currencyconverter.model.requests.AbstractSimpleHttpRequest;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiCurrencyRequest;

public class TestYahooApiCurrencyRequest extends AndroidTestCase {

	@Before
	public void setUp() throws Exception {
	}

	

	@Test(timeout=100) //should finish almost instantly, as the host should fail.
	public void testUnknownHost() {
		AbstractSimpleHttpRequest.OnRequestEventListener fiveSecondSleeper = 
				new AbstractSimpleHttpRequest.OnRequestEventListener(){
			@Override public void onComplete() {}			
			@Override public void onException(Exception e) {
				if (e instanceof UnknownHostException == false){
					fail("Was only expecting an unknown host, not: " + e);
				}
			}			
			@Override public void onResponseCode(int responseCode) {				
			}			
			@Override public void onStart(InputStream in) {
				try {
					Thread.sleep(5000); //sleep 5 seconds
				} catch (InterruptedException e) {} 				
			}
		};
		
		new BrokenAPI(new String[]{}, new String[]{}, fiveSecondSleeper)
			.run();
		
	}

	@Test(timeout=100)
	public void testSetRequestTimeout1() {		
		final String[] source = new String[] {"USD"};
		final String[] dest = new String[] {"BBD"};
		
		AbstractSimpleHttpRequest.OnRequestEventListener fiveSecondSleeper = 
			new AbstractSimpleHttpRequest.OnRequestEventListener(){
				@Override public void onComplete() {}			
				@Override public void onException(Exception e) {
					if (e instanceof SocketTimeoutException == false) {
						fail("Not expecting an error aside from 'SocketTimeoutException' " + e);
					}
				}			
				@Override public void onResponseCode(int responseCode) {}			
				@Override public void onStart(InputStream in) {
					try {
						Thread.sleep(5000); //sleep 5 seconds
					} catch (InterruptedException e) {} 				
				}
			};
		YahooApiCurrencyRequest request1 = new YahooApiCurrencyRequest(source, dest, fiveSecondSleeper);
		request1.setRequestTimeout(10);
		request1.run();		
	}
	
	@Test
	public void testSetRequestTimeout2() {		
		final String[] source = new String[] {"USD"};
		final String[] dest = new String[] {"BBD"};
		
		AbstractSimpleHttpRequest.OnRequestEventListener fiveSecondSleeper = 
			new AbstractSimpleHttpRequest.OnRequestEventListener(){
				@Override public void onComplete() {}			
				@Override public void onException(Exception e) {
					fail("Not expecting error: " + e);					
				}			
				@Override public void onResponseCode(int responseCode) {
					if (responseCode != 200){
						fail("Unexpected testing error: " + responseCode);
					}
				}			
				@Override public void onStart(InputStream in) {
					try {
						Thread.sleep(5000); //sleep 5 seconds
					} catch (InterruptedException e) {} 				
				}
			};
		YahooApiCurrencyRequest request1 = new YahooApiCurrencyRequest(source, dest, fiveSecondSleeper);
		request1.setRequestTimeout(20000);
		
		
		long start = System.currentTimeMillis();
		request1.run();
		long time = System.currentTimeMillis() -  start;
		
		assertTrue("Cannot take less than 5 seconds to complete", time >= 5000);
	}

	@Test
	public void testUriBuilding() {
		final String[] source = new String[] {"USD", "GBP", "CAD", "BBD"};
		final String[] dest = new String[] {"USD", "GBP", "CAD", "BBD"};
		final int EXPECTED_COUNT = 6; // 3 + 2 + 1
		
		YahooApiCurrencyRequest request1 = new YahooApiCurrencyRequest(source, dest, null);
		request1.setJsonFormat(true); //
		Uri builtUri1 = request1.getUriRequest();
		
		assertTrue("Should contain json query", builtUri1.toString().contains("format=json"));
		
		Matcher matches  = Pattern.compile("%22[A-Z]{6}%22").matcher(builtUri1.toString());
		int pairCount = 0;
		while(matches.find()){
			pairCount++;
		}
		assertEquals("Should contain minimal combinations (" +builtUri1.toString() +")", 
				EXPECTED_COUNT, pairCount);
		
		YahooApiCurrencyRequest request2 = new YahooApiCurrencyRequest(source, dest, null);
		request2.setJsonFormat(false); //no json
		Uri builtUri2 = request2.getUriRequest();
		
		assertFalse("Should not contain json query", builtUri2.toString().contains("format=json"));
	}
	
	
	//for testing the event listener
	private static class BrokenAPI extends YahooApiCurrencyRequest {
		
		public BrokenAPI(String[] strings, String[] strings2,
				OnRequestEventListener onRequestEventListener) {
			super(strings, strings2, onRequestEventListener);
		}

		@Override
		protected Uri getUriRequest() {
			return Uri.parse("http://verylikely.notadomianinfactiwouldbeterrifedifitwere");
		}
		
	}

}
