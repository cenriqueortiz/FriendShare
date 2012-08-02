/*
 *  Copyright (C) 2010-2012 C. Enrique Ortiz <enrique.ortiz@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.    
 */

package com.hyperkode.friendshare.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "friendshare.db";
    
    // Friend Status Table
    public static final String TABLE_NAME = "friendstatus";
    public static final String ID_COL = "id";
    public static final String NAME_COL = "name";
    public static final String STATUS_COL = "status";
    public static final String IMAGEURL_COL = "imageurl";
    public static final String TIMESTAMP_COL = "timestamp";

    // Columns Order
    public enum ColumnIndex {
        ID,
        NAME,
        STATUS,
        IMAGEURL,
        TIMESTAMP
    }

    /**
     * Constructor
     * @param context the application context
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called at the time to create the DB.
     * The create DB statement
     * @param the SQLite DB
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE "
                + TABLE_NAME
                + " (_id integer primary key autoincrement, " 
                + ID_COL        + " TEXT NOT NULL UNIQUE, "
                + NAME_COL      + " TEXT, "
                + STATUS_COL    + " TEXT, "
                + IMAGEURL_COL  + " TEXT, " 
                + TIMESTAMP_COL + " LONG)");        
    }

    /**
     * Invoked if a DB upgrade (version change) has been detected
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Here add any steps needed due to version upgrade
        // for example, data format conversions, old tables no longer needed, etc
    }

}
