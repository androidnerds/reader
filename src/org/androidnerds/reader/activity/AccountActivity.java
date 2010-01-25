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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import org.androidnerds.reader.Constants;
import org.androidnerds.reader.R;
import org.androidnerds.reader.provider.AccountProvider;
import org.androidnerds.reader.util.AccountStore;
import org.androidnerds.reader.util.api.Authentication;

/**
 * This activity depends version of Android the user has installed on their phone, pre 2.0 devices
 * do not have the ability to access the account manager so authentication has to be handled 
 * manually by us. If the device is running 2.0 or higher we can use the account manager to allow
 * them to choose which Google Account on their phone they wish to use.
 */
public class AccountActivity extends Activity {
    private static final String TAG = "AccountActivity";
	
    public static final int AUTH_ID = 1;
    public static final int ACCT_ID = 2;
	
    public static final int SIGN_IN_ACCT = 2;
    public static final int AUTH_WAIT = 3;
	
    private Dialog mDialog;
    private ProgressDialog mProgressDialog;
    private EditText mEmailField;
    private EditText mPassField;
    private CheckBox mRememberField;
    private CharSequence[] mAccounts;
    private int mAccount = -1;
    private String mEmail;
    private String mPass;
    private boolean mRemember;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        AccountStore store = AccountStore.getInstance();
		
        mAccounts = store.getAccounts(this);
        mAccount = 0;
		
        AccountProvider acct = new AccountProvider(this);
        String user = acct.getMasterAccount();
		
        if (user == null) {
            onCreateDialog(SIGN_IN_ACCT);
        }
    }
	
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
        switch (id) {
        case SIGN_IN_ACCT:
            builder.setTitle("Sign in to Google Reader");
				
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.auth_dialog, (ViewGroup) findViewById(R.id.layout_root));
            mEmailField = (EditText) v.findViewById(R.id.google_username);
            mPassField = (EditText) v.findViewById(R.id.google_password);
            mRememberField = (CheckBox) v.findViewById(R.id.remember_me);
			
            builder.setView(v);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setNegativeButton("Cancel", mLoginListener);
            builder.setPositiveButton("Login", mLoginListener);
				
            mDialog = builder.create();	
            break;
        case AUTH_WAIT:
            mProgressDialog = ProgressDialog.show(this, "Please Wait", "Signing In...", true);
            return mProgressDialog;
        }
		
        mDialog.show();
        return mDialog;
    }
	
    DialogInterface.OnClickListener mLoginListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == -2) {
                close();
            } else {

                if (!mEmailField.getText().equals("") && !mPassField.getText().equals("")) {
                    mEmail = mEmailField.getText().toString();
                    mPass = mPassField.getText().toString();
                    mRemember = mRememberField.isChecked();

                    onCreateDialog(AUTH_WAIT);

                    Thread thr = new Thread(mTokenRunnable);
                    thr.start();
	        }	
            }
        }
    };

    public void close() {
        finish();
    }

    private Runnable updateUi = new Runnable() {
        public void run() {
            mProgressDialog.dismiss();
            close();
        }
    };

    private Runnable mTokenRunnable = new Runnable() {
        public void run() {
            AccountStore store = AccountStore.getInstance();
            String token = null;

            Log.d(TAG, "Account position: " + mAccount);

            if (mAccount != -1) {
                token = store.getAccountToken(AccountActivity.this.getApplicationContext(), mEmail, mPass);
            } else {
                token = store.getAccountToken(AccountActivity.this.getApplicationContext(), mAccounts[mAccount].toString(), "");
            }

            Log.d(TAG, "Authentication token is: " + token);

            AccountActivity.this.runOnUiThread(updateUi);
        }
    };

}
