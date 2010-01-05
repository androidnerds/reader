/*
 * Copyright (C) 2007-2010 Michael Novak <mike@androidnerds.org>, Josh Guilfoyle <jasta@devtcg.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
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