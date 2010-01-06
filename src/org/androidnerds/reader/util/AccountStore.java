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
package org.androidnerds.reader.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import org.androidnerds.reader.Constants;

public abstract class AccountStore {
	
	private static final String TAG = "AccountStore";
	
	public static AccountStore getInstance() {
		if (Constants.PRE_ECLAIR) {
			return PreEclairAccount.Holder.sInstance;
		} else {
			return EclairAccount.Holder.sInstance;
		}
	}
	
	public abstract void getAccountToken(Context context);
	
	public abstract void authenticateAccount();
	
	private static class PreEclairAccount extends AccountStore {
		
		private static class Holder {
			private static final PreEclairAccount sInstance = new PreEclairAccount();
		}
		
		public void getAccountToken(Context context) {
			Log.d(TAG, "PreEclair token request.");
		}
		
		public void authenticateAccount() {
			
		}
		
	}
	
	private static class EclairAccount extends AccountStore {
		
		private static class Holder {
			private static final EclairAccount sInstance = new EclairAccount();
		}
		
		public void getAccountToken(Context context) {
			AccountManager manager = AccountManager.get(context);
			Account[] accts = manager.getAccounts();

			Log.d(TAG, "Number of accounts on device: " + accts.length);
		}
		
		public void authenticateAccount() {
			
		}
	}
}