package com.ovrhere.android.currencyconverter.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.text.format.DateUtils;

/** Used for formatting {@link Date}s and timestamps created by {@link Timestamp}
 * into readable data. Restructured to use {@link DateUtils}.
 * @author Jason J.
 * @version 0.2.0-20141106
 */
public class DateFormatter {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = DateFormatter.class
			.getSimpleName();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End private constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The key to describe minute units (e.g. 1 "minute"). 
	 * Expects formatted R.plurals. */ 
	@Deprecated
	final static public String MINUTE_UNIT = CLASS_NAME + ".MINUTE_UNIT";
	/** The key to describe the hour units (e.g. 1 "hour").  
	 * Expects R.plurals.*/
	@Deprecated
	final static public String HOUR_UNIT = CLASS_NAME + ".HOUR_UNIT";
	/** The key to describe the day unit (e.g. 1 "day").  
	 * Expects formatted R.plurals.*/
	@Deprecated
	final static public String DAY_UNIT = CLASS_NAME + ".DAY_UNIT";

	/** The key to describe the today unit. Overrides 
	 * {@link #HOUR_UNIT}.  Expects formatted R.string. 
	 * (e.g. "Today at %d" )	 */
	@Deprecated
	final static public String TODAY_UNIT = CLASS_NAME + ".TODAY_UNIT";
	/** The key to describe the yesterday unit. Overrides 
	 * {@link #HOUR_UNIT}. Expects formatted R.string 	 */
	@Deprecated
	final static public String YESTERDAY_UNIT = 
			CLASS_NAME + ".YESTERDAY_UNIT";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Takes date and gives a relative date & time given in local time. 
	 * @param context The current context for accessing resources
	 * @param date The given date to process
	 * @return The time broken down into simpler relative date 
	 */
	static public String dateToRelativeDate(Context context, Date date){
		long millis = date.getTime();
		String time = DateUtils.getRelativeDateTimeString(context, millis, 
						DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 
						0).toString();
		int commaIndex = time.lastIndexOf(",");
		if (commaIndex > 0){
			//remove the time e.g. ", 1:33pm " if found
			return time.substring(0, commaIndex);
		} 
		return time;
	}
	
	/** 
	 * 
	 * Parses timestamp as provided by the (custom) {@link Timestamp} object
	 * and calls  {@link #dateToRelativeDate(Context, HashMap, Date)}
	 * @param context The current context for accessing resources
	 * @param timestamp The timestamp. 
	 * Expects the form  "yyyy-MM-dd HH:mm:ss.SSSZ", 
	 * i.e. 1969-12-31 16:00:00.000-0800
	 * @return The timestamp broken down into simpler relative date 
	 * @throws ParseException If the timestamp supplied does not match the form
	 * given
	 * @deprecated Please use {@link #timestampToRelativeDate(Context, String)}
	 */
	static public String timestampToRelativeDate(Context context, 
			String timestamp) throws ParseException {
		Date then = Timestamp.parse(timestamp);
		return dateToRelativeDate(context, then);
	}
	
	/** 
	 * Takes date and gives a relative date & time given in local time. 
	 * @param context The current context for accessing resources
	 * @param timeUnits Ignored. 
	 * @param date The given date to process
	 * @return The time broken down into simpler relative date
	 * @deprecated Please use {@link #dateToRelativeDate(Context, Date)} 
	 */
	@Deprecated
	static public String dateToRelativeDate(Context context,
			HashMap<String, Integer> timeUnits, Date date) {
		return dateToRelativeDate(context, date);
	}
	
	/** 
	 * 
	 * Parses timestamp as provided by the (custom) {@link Timestamp} object
	 * and calls  {@link #dateToRelativeDate(Context, HashMap, Date)}
	 * @param context The current context for accessing resources
	 * @param timeUnits ignored.
	 * @param timestamp The timestamp. 
	 * Expects the form  "yyyy-MM-dd HH:mm:ss.SSSZ", 
	 * i.e. 1969-12-31 16:00:00.000-0800
	 * @return The timestamp broken down into simpler relative date 
	 * @throws ParseException If the timestamp supplied does not match the form
	 * given
	 * @deprecated Please use {@link #timestampToRelativeDate(Context, String)}
	 */
	@Deprecated
	static public String timestampToRelativeDate(Context context,
			HashMap<String, Integer> timeUnits, String timestamp) throws ParseException {
		Date then = Timestamp.parse(timestamp);
		return dateToRelativeDate(context, timeUnits, then);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Private utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	//removed
}
