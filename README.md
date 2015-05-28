[<img src="./screenshots/feature_graphic.png?raw=true" title="Get 'Currency Converter' on Google Play"  /> ](https://play.google.com/store/apps/details?id=com.ovrhere.android.currencyconverter)

#Currency Converter  [<span style=""><img src="./ic_launcher-web.png?raw=true" title="Get 'Currency Converter' on Google Play" height="50" width="50" style="padding-right: 15px; padding-left: 15px;height: 50px;" /> <img src="./screenshots/get_on_play_45.png?raw=true" title="Get 'Currency Converter' on Google Play" /></span>](https://play.google.com/store/apps/details?id=com.ovrhere.android.currencyconverter)

This is a demo app is intended to be a utility for converting between multiple currencies. Supports 5 currencies.


###Notes

The app supports **Android 2.3.3** and up, with enhanced support for tablet & small screens, and retro-fitted to support Material Design. It is design to small, simple, and to only fetch fresh data when open. 

It is currently an open beta release. 

Building requires the Android v7 compatibility library (build 22), the unoffical [preference fragment library (2015-05-13 build)](https://github.com/kolavar/android-support-v4-preferencefragment "android-support-v4-preferencefragment by Kolavar") & GSON library. 

Currently converts between:

* USD (United State's Dollar)
* CAD (Canadian Dollar)
* EUR (Euro)
* GBP (Great British Pound)
* JPY (Japanese Yen)

Makes use of the Yahoo currency api, via JSON and XML stream parsing. All exchange rates are stored offline for future use. Context menu support allows copying of the results.

Demonstrates the use of Unit testing, `ContentProvider`s, `SQLite`, `AsyncTaskLoader`s, `SharedPreferences`, `Animation`s, stream parsing, `Fragment`s, multiple layouts, external libraries, etc.

All graphics are done in a combination of GIMP & Inkscape.


###Screenshots

<img src="./screenshots/currconv_screenshot0.png?raw=true" title="Screenshot of Great British Pounds being converted on Lolipop"  width="250"/> <img src="./screenshots/currconv_screenshot1.png?raw=true" title="Screenshot of context menu"  width="250"/> 


<img src="./screenshots/currconv_screenshot2.png?raw=true" title="Screenshot of EUR when it cannot update."  width="250"/>  <img src="./screenshots/currconv_screenshot3.png?raw=true" title="Screenshot of smaller resolutions"  width="250"/>

<img src="./screenshots/currconv_screenshot4_tablet.png?raw=true" title="Screenshot of tablet view"  width="500"/>




#License
***
####[Apache License v2](./blob/master/LICENSE.md)

   Copyright 2015 Jason J. (iamovrhere)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
