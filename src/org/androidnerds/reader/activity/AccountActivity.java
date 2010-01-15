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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
	
	public static final int ACCT_LIST = 1;
	public static final int SIGN_IN_ACCT = 2;
	
	private Dialog mDialog;
	private CharSequence[] mAccounts;
	private int mAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AccountStore store = AccountStore.getInstance();
		store.getAccountToken(this);
		
		mAccounts = store.getAccounts(this);
		mAccount = 0;
		
		onCreateDialog(ACCT_LIST);
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch (id) {
		case ACCT_LIST:
			builder.setTitle("Select an account");
			builder.setSingleChoiceItems(mAccounts, mAccount, mAccountListener);
			mDialog = builder.create();
			break;
		case SIGN_IN_ACCT:
			builder.setTitle("Sign in to Google Reader");
			mDialog = builder.create();	
			break;
		}
		
		mDialog.show();
		return mDialog;
	}
	
	DialogInterface.OnClickListener mAccountListener = new DialogInterface.OnClickListener() {
	
		public void onClick(DialogInterface dialog, int which) {
				mAccount = which - 1;
		}		

	};

}