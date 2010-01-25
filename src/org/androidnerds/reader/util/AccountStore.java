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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.androidnerds.reader.Constants;
import org.androidnerds.reader.activity.AccountActivity;
import org.androidnerds.reader.provider.AccountProvider;
import org.androidnerds.reader.util.api.Authentication;

public abstract class AccountStore {
	
    private static final String TAG = "AccountStore";
	
    public static AccountStore getInstance() {
        if (Constants.PRE_ECLAIR) {
            return PreEclairAccount.Holder.sInstance;
        } else {
            return EclairAccount.Holder.sInstance;
	}
    }
		
    public abstract String getAccountToken(Context context, String account, String pass);
	
    public abstract String[] getAccounts(Context context);
	
    private static class PreEclairAccount extends AccountStore {
		
        private static class Holder {
            private static final PreEclairAccount sInstance = new PreEclairAccount();
        }
		
        public String getAccountToken(Context context, String account, String pass) {
            Log.d(TAG, "PreEclair token request.");
			
            return Authentication.getAuthToken(account, pass);
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
		
        public String getAccountToken(Context context, String account, String pass) {
            AccountProvider provider = new AccountProvider(context);
            String token = provider.getAuthToken(provider.getMasterAccount());

            return token;
        }

        public String[] getAccounts(Context context) {
            AccountManager manager = AccountManager.get(context);
            Account[] accts = manager.getAccountsByType("com.google");
            String[] names = new String[accts.length];
			
            for (int i = 0; i < accts.length; i++) {
                Log.d(TAG, "Account: " + accts[i]);
                names[i] = accts[i].name;
            }
			
            return names;
        }
    }
}
