/*
 * Copyright 2015 Jason J. (iamovrhere)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ovrhere.android.currencyconverter.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/** Provides safe and reliable methods to launch market intents.
 * @author Jason J.
 * @version 0.1.0-20140929
 */
public class MarketIntentUtil {
	/** The package for market intent. */
	final static private String MARKET_PACKAGE =
			"com.android.vending";
	
	/** The stub for the http launch of app. */
	final static private String HTTP_APP_STUB = 
			"http://play.google.com/store/apps/details?id=%s";
	/** The stub for the http launch of developer list. */
	final static private String HTTP_DEVELOPER_STUB = 
			"http://play.google.com/store/search?q=pub:%s";
	/** The stub for the http launch of search result. */
	final static private String HTTP_SEARCH_STUB = 
			"http://play.google.com/store/search?q=%s&c=apps";
	
	/** The stub for the market launch of app. */
	final static private String MARKET_APP_STUB = 
			"market://details?id=%s";
	/** The stub for the market launch of developer list. */
	final static private String MARKET_DEVELOPER_STUB = 
			"market://search?q=pub:%s";
	/** The stub for the market launch of search result. */
	final static private String MARKET_SEARCH_STUB = 
			"market://search?q=%s&c=apps";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Launches the app in the store
	 * @param context The context to launch from/app package to launch from
	 */
	static public void launchApp(Context context){
		String appName = context.getPackageName();
		robustLaunch(context, 
				String.format(HTTP_APP_STUB, appName), 
				String.format(MARKET_APP_STUB, appName));
	}
	
	/** Launches the app in the store
	 * @param context The context to launch from
	 * @param appName The app name to launch 
	 */
	static public void launchApp(Context context, String appName){
		try {
			appName =  URLEncoder.encode(appName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//do nothing
		}
		robustLaunch(context, 
				String.format(HTTP_APP_STUB, appName), 
				String.format(MARKET_APP_STUB, appName));
	}
	
	
	/** Launches a the developer page. 
	 * @param context The context to launch from
	 * @param developerName The developer's name (raw)
	 */
	static public void launchDeveloper(Context context, String developerName){
		try {
			developerName =  URLEncoder.encode(developerName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//do nothing
		}
		robustLaunch(context, 
				String.format(HTTP_DEVELOPER_STUB, developerName), 
				String.format(MARKET_DEVELOPER_STUB, developerName));
	}
	
	/** Launches a search query into the market. 
	 * @param context The context to launch from
	 * @param searchQuery The search query
	 */
	static public void launchSearch(Context context, String searchQuery){
		try {
			searchQuery =  URLEncoder.encode(searchQuery, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//do nothing
		}
		robustLaunch(context, 
				String.format(HTTP_SEARCH_STUB, searchQuery), 
				String.format(MARKET_SEARCH_STUB, searchQuery));
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// private Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Launches the market intent robustly, even if the user does not 
	 * have it installed.
	 * @param context The context to launch from
	 * @param httpTarget The http target prepared from constants
	 * @param marketTarget The market target prepared from constants
	 */
	static private void robustLaunch(Context context, String httpTarget, 
			String marketTarget){
		/* As learnt from using google maps api (see above): 
		 * If you try to launch the play intent without having play 
		 * (due to be older device or just not installed) it will fail. 
		 * Badly.
		 * 
		 * Thus method of trying:
		 * -Launch using the HTTP request setting package to vending (as they do)
		 * -Launch intent using market stub 
		 * -Launch via web intent
		 */
		Uri httpUri = Uri.parse(httpTarget);
		Uri marketUri = Uri.parse(marketTarget);
		
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, httpUri);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setPackage(MARKET_PACKAGE);
			
			//attempt to launch
			context.startActivity(intent);
			
		} catch (ActivityNotFoundException e) {
			//It failed, try the older market method?
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.setPackage(MARKET_PACKAGE);
				
				//attempt to launch, again.
				context.startActivity(intent);
				
			} catch (ActivityNotFoundException f) {
				//Still don't have it; maybe they don't have it installed?
				//Try launching by browser instead.
				
				Intent intent = new Intent(Intent.ACTION_VIEW, httpUri);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				context.startActivity(intent);
			}
		}
	}
}
