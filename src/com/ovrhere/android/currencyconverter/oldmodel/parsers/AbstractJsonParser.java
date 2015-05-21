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
package com.ovrhere.android.currencyconverter.oldmodel.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/** The basic outline to a JSON parser. Provides pause/resume features.
 * Requires GSON library. Compatible 2.3.3 and up.
 * @author Jason J.
 * @version 0.1.0-20140929
 * @param <R1>
 */
@Deprecated
abstract public class AbstractJsonParser <R1>{
	/** The tag for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String LOGTAG = AbstractJsonParser.class
			.getSimpleName();	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End contants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The boolean for determining if paused. */ 
	volatile boolean paused = false;
	
	/** The pull parser for this object. */
	protected JsonReader jsonReader = null;
	
	
	/** Pauses the thread based on value. */
	public void pause() {
		paused = true; 
	}
	/** Resumes the thread based on value. */
	public void resume(){
		paused = false;
	}

	
	/** Parses the JSON stream and returns it described return type.
	 * Note that this method initializes and closes the reader.
	 * @param reader The input reading
	 * @return R1, the parsed data.
	 * @throws IOException Re-thrown exception from reader
	 */
	public R1 parseJsonStream(Reader reader) throws IOException {
		
		checkPause();
		jsonReader =  new JsonReader(reader);
		checkPause();
		

		try {
			return parseJsonToReturnData();
		} finally {
			try {
				jsonReader.close();
			} catch (IOException e) {}
		}
	}
	
	/** Parses the JSON stream and returns it described return type.
	 * Note that this method initializes and closes the reader.
	 * @param in The input stream
	 * @return R1, the parsed data. 
	 * @throws IOException Re-thrown exception from reader 
	 */
	public R1 parseJsonStream(InputStream in) throws IOException {
		checkPause();
		InputStreamReader reader = new InputStreamReader(in);
		jsonReader =  new JsonReader(reader);
		checkPause();
		
		try {
			return parseJsonToReturnData();
		} finally {
			try {
				jsonReader.close();
			} catch (IOException e) {}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Abstract methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Parses the JSON to a R1 using the prepared JsonReader  {@link #jsonReader}.
	 * Remember to use {@link #checkPause()} for proper thread control.
	 * Additionally, consider the use of {@link Thread#yield()} for throttling.
	 * @return The parsed group of values 
	 * @throws IOException Re-thrown exception
	 * */
	abstract protected R1 parseJsonToReturnData() throws IOException;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks to see if to yield the thread based on
	 * If so, it:
	 * <ol><li>Sets priority to lowest</li>
	 * <li>Sleeps for 50 milliseconds</li>
	 * <li>Rechecks</li>
	 * <li>Repeats step 2.</li></ul>
	 * After exiting this loop it is restored to its former priority. */
	protected void checkPause(){
		final Thread t = Thread.currentThread();
		final int priority = t.getPriority();
		if (paused){
			t.setPriority(Thread.MIN_PRIORITY);
		}
		while (paused){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		t.setPriority(priority);
	}
	
	/** Checks to see if the next element of the reader is {@link JsonToken#NULL}.
	 * @return <code>true</code> if not NULL, <code>false</code> if 
	 * if null.
	 * @throws IOException Re-thrown exception from peek	 */
	final protected boolean nextNotNull() throws IOException{
		return  jsonReader.peek() != JsonToken.NULL;	
	}
	
}
