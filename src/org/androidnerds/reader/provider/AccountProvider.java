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
package org.androidnerds.reader.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class works with the accounts database which is separate from the
 * feeds database to remain private. This database will keep track of the
 * accounts used by the Reader application and store the authToken for
 * quick access.
 *
 * @author mike novak
 *
 */
public class AccountProvider extends SQLiteOpenHelper {

	private static final String DATABASE_FILE = "accounts.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DB_CREATE = "CREATE TABLE accounts "
			+ "(_id INTEGER PRIMARY KEY, username TEXT UNIQUE, password TEXT, "
			+ "token TEXT UNIQUE, master INTEGER DEFAULT '0');";
	
	private static final String DB_TABLE = "accounts";
	
	public AccountProvider(Context context) {
		super(context, DATABASE_FILE, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//woohoo! nothing here yet.
	}
	
	/**
	 * This method returns the proper authentication string for the desired
	 * account. 
	 *
	 * @param user - the username for the requested token
	 * @return token - the respective token for the supplied user.
	 *
	 */
	public String getAuthToken(String user) {
		SQLiteDatabase db = getReadableDatabase();
		String token = null;
		
		Cursor c = db.rawQuery("SELECT token FROM accounts WHERE username = '" + user + "'", null);
		
		if (c.moveToNext()) {
			token = c.getString(0);
		}
		
		c.close();
		db.close();
		
		return token;
	}
	
	/**
	 * This method updates the token in the database for the supplied user.
	 * An authentication token can expire in which case the app will need
	 * to go and re-authenticate, get a new auth token and update the
	 * database with the new result.
	 *
	 * @param user - the user name with the stale token.
	 * @param newToken - the newly retrieved auth token.
	 * @return result - the boolean result of the operation.
	 *
	 */
	public boolean invalidateToken(String user, String newToken) {
		
		return true;
	}
	
	/**
	 * This method adds a brand new account to the database for the application
	 * to track. The boolean parameter master is actually optional, if it is not
	 * supplied this method is called with the default of false.
	 *
	 * @param user - this is the username to add
	 * @param pass - this is the password associated with the account
	 * @param master - whether this account is the primary account on the device.
	 * @return result - whether or not the operation was successful.
	 *
	 */
	public boolean addAccount(String user, String pass, String token, boolean master) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("username", user);
		values.put("password", pass);
		values.put("token", token);
		
		if (master) {
			values.put("master", 1);
		} else {
			values.put("master", 0);
		}
		
		long result = db.insert(DB_TABLE, null, values);
		
		if (result > 0) {
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * This method is just a wrapper for the master value.
	 *
	 * @see @link{org.androidnerds.reader.provider.AccountProvider#addAccount}
	 */
	public boolean addAccount(String user, String pass, String token) {
		return addAccount(user, pass, token, false);
	}
	
	/**
	 * This method allows for the removal of an account with the supplied username.
	 * The application will also remove any feeds that are synchronized with this
	 * account and any posts that have been downloaded.
	 *
	 * @param user - the username of the account for removal.
	 * @param result - the result of the entire set of operations.
	 */
	public boolean removeAccount(String user) {
		SQLiteDatabase db = getWritableDatabase();
		
		int result = db.delete(DB_TABLE, "username=?", new String[] { user });
		
		db.close();
		
		if (result == 1) {
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * This method will return the username associated with the master account on the
	 * device. The return value will be null if there's no master account stored on
	 * the device.
	 *
	 * @return user - the username associated with the master account on the device.
	 *
	 */
	public String getMasterAccount() {
		SQLiteDatabase db = getReadableDatabase();
		String user = null;
		
		Cursor c = db.rawQuery("SELECT username FROM accounts WHERE master = '1'", null);
		
		if (c.moveToNext()) {
			user = c.getString(0);
		}
		
		return user;
	}
}