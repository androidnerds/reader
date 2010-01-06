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
package org.androidnerds.reader.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import org.androidnerds.reader.Constants;
import org.androidnerds.reader.R;
import org.androidnerds.reader.util.AccountStore;

/**
 * This activity depends version of Android the user has installed on their phone, pre 2.0 devices
 * do not have the ability to access the account manager so authentication has to be handled 
 * manually by us. If the device is running 2.0 or higher we can use the account manager to allow
 * them to choose which Google Account on their phone they wish to use.
 */
public class AccountActivity extends Activity {
	
	private static final String PREFS = "readerprefs";
	private static final String TAG = "AccountActivity";
	
	public static final int AUTH_ID = 1;
	public static final int ACCT_ID = 2;
	
	private Dialog mDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AccountStore store = AccountStore.getInstance();
		store.getAccountToken(this);
	}
	
	public Dialog onCreateDialog(int id) {
		return mDialog;
	}

}