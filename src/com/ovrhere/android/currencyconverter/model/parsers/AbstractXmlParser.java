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
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;



/** The basic outline for an xml parser. 
 * @author Jason J.
 * @version 0.3.0-20150523
 * @param R1 The return value to use for {@link #parseXmlStream(Reader)}
 */
abstract public class AbstractXmlParser <R1> {
	/** The tag for debugging purposes. */
	final static private String LOGTAG = AbstractXmlParser.class
			.getSimpleName();
	/** The exception message to give when the factory is not found. */
	final static private String DETAILED_EXCEPTION_PARSER_NOT_BUILT = 
			"PullParser was not built previously; check log.";	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End contants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The {@link XmlPullParser} factory for this object. */
	private XmlPullParserFactory mFactory = null;
	/** The pull parser for this object. */
	protected XmlPullParser mPullParser = null;
	
	/** @return The current {@link #mPullParser} to use. */
	final protected XmlPullParser getPullParser() {
		return mPullParser;
	}
	
	/** Initializes parser.
	 * @throws XmlPullParserException  if parser or factory fails 
	 * to be created. */
	public AbstractXmlParser() throws XmlPullParserException {
		try {
			this.mFactory = XmlPullParserFactory.newInstance();
			this.mPullParser = mFactory.newPullParser();
		} catch (XmlPullParserException e) {
			Log.e(LOGTAG, "Unexpected error creating parser: " + e);
			throw e;
		}
	}	
	
	/** Parses the xml stream and returns it described return type.
	 * @param in The input reading
	 * @return R1, the parsed data.
	 * @throws XmlPullParserException re-thrown 
	 * @throws IOException re-thrown
	 */
	public R1 parseXmlStream(Reader in) 
			throws XmlPullParserException, IOException{
		parserNullCheck();
		try {
			mPullParser.setInput(in);
		} catch (XmlPullParserException e) {
			Log.e(LOGTAG, "Parser failed to be created: "+e);
			throw e;
		}
		
		return parseXmlToReturnData();
	}
	
	/** Parses the xml stream and returns it described return type.
	 * @param in The input stream
	 * @return R1, the parsed data. 
	 * @throws XmlPullParserException re-thrown 
	 * @throws IOException re-thrown
	 */
	public R1 parseXmlStream(InputStream in) 
			throws XmlPullParserException, IOException{
		parserNullCheck();
		try {
			mPullParser.setInput(in, null);
		} catch (XmlPullParserException e) {
			Log.e(LOGTAG, "Parser failed to be created: "+e);
			throw e;
		}
		
		return parseXmlToReturnData();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Abstract methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Parses the xml to a list using prepared PullParser  {@link #mPullParser}.
	 * 
	 * Consider using {@link Thread#yield()} for throttling large data?
	 * @return The parsed group of values
	 * @throws XmlPullParserException  re-thrown from next
	 * @throws IOException  re-thrown from next */
	abstract protected R1 parseXmlToReturnData() 
			throws XmlPullParserException, IOException;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Checks if #pullParser is <code>null</code>, if so throw.
	 * @throws XmlPullParserException If the pull parser not previously initialized.	 */
	final protected void parserNullCheck() throws XmlPullParserException {
		if (mPullParser == null){ //should never happen
			Log.w(LOGTAG, "Parser not built; check error log.");
			throw new XmlPullParserException(DETAILED_EXCEPTION_PARSER_NOT_BUILT);
		}
	}	
	
	/**
	 * Reads text from the next element and proceeds to the closing tag.
	 * @return The string text read from the next element. 
	 * @throws IOException  re-thrown from next
	 * @throws XmlPullParserException re-thrown from next
	 */
	final protected String readText() 
			throws IOException, XmlPullParserException {
	    String text = "";
	    if (mPullParser.next() == XmlPullParser.TEXT) {
	        text = mPullParser.getText();
	        mPullParser.nextTag();
	    }
	    return text;
	}

	/** Skips this tag an all other tags contained within it.	
	 * @throws XmlPullParserException re-thrown from next
	 * @throws IOException re-thrown from next
	 * @throws IllegalStateException If not called on a starting tag.
	 */
	final protected void skipTag() throws XmlPullParserException, IOException {
		if (mPullParser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (mPullParser.next()) {
				case XmlPullParser.START_TAG:
				depth++;
				break;
				case XmlPullParser.END_TAG: 
				depth--;
				//break;
			}
		}
	}

}
