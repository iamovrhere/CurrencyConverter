package com.ovrhere.android.currencyconverter.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;

/** Used for formatting {@link Date}s and timestamps created by {@link Timestamp}
 * into readable data.
 * @author Jason J.
 * @version 0.1.0-20140904
 */
public class DateFormatter {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = DateFormatter.class
			.getSimpleName();
	
	/** Minute in milliseconds. */
	final static private long MINUTE_IN_MILLIS = 60000; //ms 
	/** Hours in milliseconds. */
	final static private long HOUR_IN_MILLIS = 3600000; //ms //60*60*1000
	/**A day or 24 hrs in milliseconds. */
	final static private long DAY_IN_MILLIS = 86400000; //ms //24*60*60*1000
	/**A week in milliseconds. */
	final static private long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7; //ms 
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End private constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The key to describe minute units (e.g. 1 "minute"). 
	 * Expects formatted R.plurals. */ 
	final static public String MINUTE_UNIT = CLASS_NAME + ".MINUTE_UNIT";
	/** The key to describe the hour units (e.g. 1 "hour").  
	 * Expects R.plurals.*/
	final static public String HOUR_UNIT = CLASS_NAME + ".HOUR_UNIT";
	/** The key to describe the day unit (e.g. 1 "day").  
	 * Expects formatted R.plurals.*/
	final static public String DAY_UNIT = CLASS_NAME + ".DAY_UNIT";

	/** The key to describe the today unit. Overrides 
	 * {@link #HOUR_UNIT}.  Expects formatted R.string. 
	 * (e.g. "Today at %d" )	 */
	final static public String TODAY_UNIT = CLASS_NAME + ".TODAY_UNIT";
	/** The key to describe the yesterday unit. Overrides 
	 * {@link #HOUR_UNIT}. Expects formatted R.string 	 */
	final static public String YESTERDAY_UNIT = 
			CLASS_NAME + ".YESTERDAY_UNIT";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Takes date and gives a relative date & time given in local time. 
	 * @param context The current context for accessing resources
	 * @param timeUnits The hashmap of time unit <b>plurals resource ids</b> 
	 * used to break down and output time. It is suggested that strings
	 * contain %d for inputs (e.g. %d hours ago). Note that:
	 * <ul>
	 * <li>Times &lt; an hour ago will be parsed with minute unit, 
	 * if provided.<li>
	 * <li>Time &lt; a day but &gt; an hour, the hours unit will be used
	 * UNLESS a today/yesterday unit is provided.</li>
	 * <li>Time &lt; week but &gt; a day, the days units will be used</li>
	 * <li>Time &gt; 1 week ago, only the date & time are given
	 * in yyyy-MM-dd hh:mm form.</li>
	 * <li>{@link #TODAY_UNIT} & {@link #YESTERDAY_UNIT} override any hour & 
	 * minute units. Such that: "5 hours ago" becomes "Today @ 8:00 am"</li>
	 * <li>If no units are given for given time frame(s), 
	 * the date & time will revert to next available form; 
	 * minutes -> hours -> days -> weeks and, ultimately, 
	 * in yyyy-MM-dd hh:mm form</li>
	 * </ul>
	 * @param date The given date to process
	 * @return The time broken down into simpler relative date 
	 */
	static public String dateToRelativeDate(Context context,
			HashMap<String, Integer> timeUnits, Date date) {
		Date then =  new Date(date.getTime());
		Date now = new Date();
		Calendar calNow = Calendar.getInstance(TimeZone.getDefault());
		Calendar calThen = Calendar.getInstance(TimeZone.getDefault());
		calThen.setTime(then);
		
		long timeSince = now.getTime() - then.getTime();
		//Log.d(CLASS_NAME, "then: "+ then.getTime()+" now: "+  now.getTime() );
		Resources r =context.getResources();
		return processTimeSince(r, timeUnits, calNow, calThen, timeSince);
	}
	
	/** Parses timestamp as provided by the (custom) {@link Timestamp} object
	 * and calls  {@link #dateToRelativeDate(Context, HashMap, Date)}
	 * @param context The current context for accessing resources
	 * @param timeUnits The hashmap of time unit <b>plurals resource ids</b> 
	 * used to break down and output time. It is suggested that strings
	 * contain %d for inputs (e.g. %d hours ago). Note that:
	 * <ul>
	 * <li>Times &lt; an hour ago will be parsed with minute unit, 
	 * if provided.<li>
	 * <li>Time &lt; a day but &gt; an hour, the hours unit will be used
	 * UNLESS a today/yesterday unit is provided.</li>
	 * <li>Time &lt; week but &gt; a day, the days units will be used</li>
	 * <li>Time &gt; 1 week ago, only the date & time are given
	 * in yyyy-MM-dd hh:mm form.</li>
	 * <li>{@link #TODAY_UNIT} & {@link #YESTERDAY_UNIT} override any hour & 
	 * minute units. Such that: "5 hours ago" becomes "Today @ 8:00 am"</li>
	 * <li>If no units are given for given time frame(s), 
	 * the date & time will revert to next available form; 
	 * minutes -> hours -> days -> weeks and, ultimately, 
	 * in yyyy-MM-dd hh:mm form</li>
	 * </ul>
	 * @param timestamp The timestamp. 
	 * Expects the form  "yyyy-MM-dd HH:mm:ss.SSSZ", 
	 * i.e. 1969-12-31 16:00:00.000-0800
	 * @return The timestamp broken down into simpler relative date 
	 * @throws ParseException If the timestamp supplied does not match the form
	 * given
	 */
	static public String timestampToRelativeDate(Context context,
			HashMap<String, Integer> timeUnits, String timestamp) throws ParseException {
		Date then = Timestamp.parse(timestamp);
		return dateToRelativeDate(context, timeUnits, then);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Private utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Processes the time since, using the calender and decides which semantics
	 * of date to reutrn
	 * @param res The resource accessor
	 * @param timeUnits The time units plurals.
	 * @param now The calendar for now
	 * @param then The calender for the time stamp
	 * @param timeSince The time since then.
	 * @return The broken down time.
	 */
	static private String processTimeSince(Resources res,
			HashMap<String, Integer> timeUnits,
			Calendar now, Calendar then, long timeSince) {
		if ( timeSince > 0){
			if (timeSince < HOUR_IN_MILLIS && 
					timeUnits.containsKey(MINUTE_UNIT) ) {
				//within an hour; minutes ago
				return formatMinutes(res, timeUnits, timeSince);
			}  
			//no elses, but fall through
			if (timeSince < DAY_IN_MILLIS ) {
				//within 24 hrs
				String returnValue = formatHours(res, timeUnits, now, then, timeSince);
				if (returnValue != null){
					return returnValue;
				}
			} 
			if (timeSince < WEEK_IN_MILLIS && timeUnits.containsKey(DAY_UNIT)) {
				//within week; days ago
				return formatDays(res, timeUnits, timeSince);
			}
		}
		//TODO restructure using DateFormat
		//beyond a week
		return ""+
				then.get(Calendar.YEAR)+"-"+(then.get(Calendar.MONTH)+1 )+"-"+
				then.get(Calendar.DAY_OF_MONTH)+" "+get12Time(then);
	}

	/** Formats items less than &lt; 1 week, the weeks into given days. 	 
	 * @param res The resource accessor
	 * @param timeUnits The list of resources
	 * @param timeSince The time since timestamp
	 * @return The formatted date in hours
	 */
	private static String formatDays(Resources res,
			HashMap<String, Integer> timeUnits, long timeSince) {
		int weeks = (int) (timeSince/DAY_IN_MILLIS);
		return res.getQuantityString(timeUnits.get(DAY_UNIT).intValue(), 
				weeks, weeks);
	}
	/** Formats items &lt; one day into either Today, Yesterday or hours.
	 * @param res The resource accessor
	 * @param timeUnits The list of resources
	 * @param now The calender for now to compare to then
	 * @param then The time stamp object
	 * @param timeSince The time since timestamp
	 * @return The formatted date in hours.
	 */
	private static String formatHours(Resources res,
			HashMap<String, Integer> timeUnits, Calendar now, Calendar then,
			long timeSince) {
		if (	now.get(Calendar.DAY_OF_MONTH) == 
				then.get(Calendar.DAY_OF_MONTH) &&
				timeUnits.containsKey(TODAY_UNIT)) {
			//today
			return formatTimeString(res, timeUnits.get(TODAY_UNIT), then);
		} else if (timeUnits.containsKey(YESTERDAY_UNIT)){
			//yesterday
			return formatTimeString(res, timeUnits.get(YESTERDAY_UNIT), then);
		} else if (timeUnits.containsKey(HOUR_UNIT)){
			//hours ago
			int hours = (int) (timeSince/HOUR_IN_MILLIS);
			return res.getQuantityString(timeUnits.get(HOUR_UNIT).intValue(), 
					hours, hours);
		}
		return null;
	}
	/** Formats the string with a time string. */
	private static String formatTimeString(Resources res,
			int string , Calendar then) {
		return res.getString(string,
				get12Time(then));
	}
	/** Gets 12 hr time from given calendar item.
	 * @param calendar
	 * @return The time 
	 */
	private static String get12Time(Calendar calendar) {
		return calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+" "+
		(calendar.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");
	}

	/** Formats minutes into readable form.
	 * @param res The resource accessor
	 * @param timeUnits The list of resources
	 * @param timeSince The time since timestamp
	 * @return The formatted date
	 */
	private static String formatMinutes(Resources res, 
			HashMap<String, Integer> timeUnits, long timeSince) {
		int minutes = (int) (timeSince/MINUTE_IN_MILLIS);
		return res.getQuantityString(timeUnits.get(MINUTE_UNIT).intValue(), 
				minutes, minutes);
	}
}
