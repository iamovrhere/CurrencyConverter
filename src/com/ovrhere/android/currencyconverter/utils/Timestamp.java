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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A utility class for handling string timestamps. Typically used with a database.
 * @author Jason J.
 * @version 0.1.0-20140616
 */
public class Timestamp {
	/** The default date format to use based upon:
	 * -http://developer.android.com/reference/java/text/SimpleDateFormat.html 	 */
	final static private String TIMESTAMP_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";
	/** The UTC/GMT time zone format. */	
	final static private SimpleDateFormat UTC_FORMAT = 
			new SimpleDateFormat(TIMESTAMP_DATE_FORMAT, Locale.US);
	/** The user's local time zone format. */
	final static private SimpleDateFormat USER_FORMAT = 
			new SimpleDateFormat(TIMESTAMP_DATE_FORMAT, Locale.US);
	
	static {
		UTC_FORMAT.setTimeZone( TimeZone.getTimeZone("UTC"));
		USER_FORMAT.setTimeZone( TimeZone.getDefault());
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parses the timestamp and returns in milliseconds the time since the timestamp.
	 * If negative, the timestamp occurs in the furture. If positive, occurs
	 * in past. If 0, current time.
	 * @param timestamp
	 * @return The time elapsed in milliseconds.
	 * @throws ParseException See {@link #parse(String)}.
	 */
	static public long timeSince(String timestamp) throws ParseException{
		Date then = parse(timestamp); 
		return new Date().getTime() - then.getTime();
	}
	
	/**
	 * Convenience method. Calls {@link #parse(String)} and compares results.
	 * @param timestamp1
	 * @param timestamp2
	 * @return an int < 0 if this Date is less than the specified Date, 
	 * 0 if they are equal, and an int > 0 if this Date is greater.
	 * @throws ParseException See {@link #parse(String)}.
	 * @see Date#compareTo(Date)
	 */
	static public int compare(String timestamp1, String timestamp2) 
			throws ParseException{
		return parse(timestamp1).compareTo(parse(timestamp2));
	}
	
	/**
	 * Attempts to parse the given timestamp and return the corresponding date.
	 * @param timestamp The timestamp to parse. Expects the form 
	 * {@value #TIMESTAMP_DATE_FORMAT}, i.e. 
	 * 1969-12-31 16:00:00.000-0800
	 * @return The date represented by the timestamp if the time has parsed.
	 * @throws ParseException If an error occurs during parsing, such as 
	 * poor form.
	 */
	static public Date parse(String timestamp) throws ParseException{
		return 
			new SimpleDateFormat(TIMESTAMP_DATE_FORMAT, Locale.US).parse(timestamp);
	}
	
	/** Converts date to UTC timestamp.
	 * @param date The date to convert.
	 * @return The UTC timestamp.
	 */
	static public String dateToUtc(Date date){
		return writeTimeStamp(UTC_FORMAT, date);
	}
	
	/** @return A timestamp in the form of {@value #TIMESTAMP_DATE_FORMAT}
	 * in GMT/UTC time.  */
	static public String getUtc(){
		return writeTimeStamp(UTC_FORMAT, new Date());
	}
	
	/** @return A timestamp in the form of {@value #TIMESTAMP_DATE_FORMAT}
	 * in the user's local time.  */
	static public String get(){
		return writeTimeStamp(USER_FORMAT, new Date());
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility Methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** @param simpledateformat The simple date format object.
	 * @param date The date to use on the timestamp.
	 * @return The timestamp in the form of {@link #TIMESTAMP_DATE_FORMAT} 
	 * @see SimpleDateFormat
	 */
	static private String writeTimeStamp(SimpleDateFormat simpledateformat, 
			Date date){
		return 	simpledateformat.format(date);
	}
	
}
