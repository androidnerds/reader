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