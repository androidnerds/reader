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
package org.androidnerds.reader.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import org.androidnerds.reader.Constants;
import org.androidnerds.reader.provider.AccountProvider;

public abstract class AccountStore {
	
	private static final String TAG = "AccountStore";
	
	public static AccountStore getInstance() {
		if (Constants.PRE_ECLAIR) {
			return PreEclairAccount.Holder.sInstance;
		} else {
			return EclairAccount.Holder.sInstance;
		}
	}
	
	public abstract void getAccountToken(Context context, String account);
	
	public abstract void authenticateAccount();
	
	public abstract String[] getAccounts(Context context);
	
	private static class PreEclairAccount extends AccountStore {
		
		private static class Holder {
			private static final PreEclairAccount sInstance = new PreEclairAccount();
		}
		
		public void getAccountToken(Context context, String account) {
			Log.d(TAG, "PreEclair token request.");
		}
		
		public void authenticateAccount() {
			
		}
		
		public String[] getAccounts(Context context) {
			AccountProvider provider = new AccountProvider(context);
			String user = provider.getMasterAccount();
			
			if (user == null) {
				return null;
			} else {
				return new String[] { user };
			}
		}
		
	}
	
	private static class EclairAccount extends AccountStore {
		
		private static class Holder {
			private static final EclairAccount sInstance = new EclairAccount();
		}
		
		public void getAccountToken(Context context, String account) {
			AccountManager manager = AccountManager.get(context);
			Account[] accts = manager.getAccounts();

			Log.d(TAG, "Number of accounts on device: " + accts.length);
		}
		
		public void authenticateAccount() {
			
		}
		
		public String[] getAccounts(Context context) {
			AccountManager manager = AccountManager.get(context);
			Account[] accts = manager.getAccounts();
			String[] names = new String[accts.length];
			
			for (int i = 0; i < accts.length; i++) {
				Log.d(TAG, "Account: " + accts[i]);
				names[i] = accts[i].name;
			}
			
			return names;
		}
	}
}