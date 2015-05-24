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
package com.ovrhere.android.currencyconverter.model.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/** The basic outline to a JSON parser. Requires GSON library. 
 * Compatible 2.3.3 and up.
 * 
 * @author Jason J.
 * @version 0.2.0-20150523
 * @param <R1> The return data type for {@link #parseJsonStream(Reader)}.
 */
abstract public class AbstractJsonParser <R1>{
	/** The tag for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String LOGTAG = AbstractJsonParser.class
			.getSimpleName();	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The pull parser for this object. */
	protected JsonReader mJsonReader = null;
	
	/** Returns the current state of {@link #mJsonReader}; may be null or valid.  */
	final protected JsonReader getJsonReader() {
		return mJsonReader;
	}
	
	/** Parses the JSON stream and returns it described return type.
	 * Note that this method initializes and safely closes the reader.
	 * @param reader The input reading
	 * @return R1, the parsed data.
	 * @throws IOException Re-thrown exception from reader
	 */
	public R1 parseJsonStream(Reader reader) throws IOException {
		mJsonReader =  new JsonReader(reader);
		try {
			return parseJsonToReturnData();
		} finally {
			try {
				mJsonReader.close();
			} catch (IOException e) {}
		}
	}
	
	/** Convenience method to call {@link #parseJsonStream(Reader)}.
	 * @param in The input stream
	 * @return R1, the parsed data. 
	 * @throws IOException Re-thrown exception from reader 
	 */
	public R1 parseJsonStream(InputStream in) throws IOException {
		InputStreamReader reader = new InputStreamReader(in);
		return parseJsonStream(reader);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Abstract methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Parses the JSON to a R1 using the prepared JsonReader  {@link #mJsonReader}.
	 * 
	 * Perhaps consider the use of {@link Thread#yield()} for throttling large
	 * data?
	 * @return The parsed group of values 
	 * @throws IOException Re-thrown exception
	 * */
	abstract protected R1 parseJsonToReturnData() throws IOException;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Checks to see if the next element of the reader is {@link JsonToken#NULL}.
	 * @return <code>true</code> if not NULL, <code>false</code> if 
	 * if null.
	 * @throws IOException Re-thrown exception from peek	 */
	final protected boolean nextNotNull() throws IOException{
		return  mJsonReader.peek() != JsonToken.NULL;	
	}
	
}
