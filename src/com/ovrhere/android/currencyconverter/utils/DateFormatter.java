package com.ovrhere.android.currencyconverter.utils;

import java.util.Date;

import android.content.Context;
import android.text.format.DateUtils;

/** Used for formatting {@link Date}s and timestamps created by {@link Timestamp}
 * into readable data. Restructured to use {@link DateUtils}.
 * @author Jason J.
 * @version 0.3.0-20150526
 */
public class DateFormatter {
	//removed
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Convenience method, calls {@link #dateToRelativeDate(Context, long)}
	 * @param context The current context for accessing resources
	 * @param date The given date to process
	 * @return The time broken down into simpler relative date 
	 */
	static public String dateToRelativeDate(Context context, Date date){
		long millis = date.getTime();
		return dateToRelativeDate(context, millis);
	}
	
	/** Takes date and gives a relative date & time given in local time. 
	 * @param context The current context for accessing resources
	 * @param date The given date to process
	 * @return The time broken down into simpler relative date 
	 */
	static public String dateToRelativeDate(Context context, long timeInMillis){
		String time = DateUtils.getRelativeDateTimeString(context, timeInMillis, 
						DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 
						0).toString();
		int commaIndex = time.lastIndexOf(",");
		if (commaIndex > 0){
			//remove the time e.g. ", 1:33pm " if found
			return time.substring(0, commaIndex);
		} 
		return time;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Private utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	//removed
}
