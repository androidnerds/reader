/* Copyright (C) 2009, 2010 Android Nerds Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidnerds.reader;

import android.os.Build;

import org.androidnerds.reader.R;

public interface Constants {
	
	/**
	 * Determine what platform we are working with, if the platform is 2.0 or higher 
	 * we have the ability to use the internal account manager or whether we need
	 * to bother the user for authentication.
	 *
	 * @note this is supported for api level 4 and higher, level 3 doesn't have
	 * build information in it
	 */
	public static boolean PRE_ECLAIR = (Integer.parseInt(Build.VERSION.SDK) <= 4);
	
}