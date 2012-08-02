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

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hyperkode.friendshare.entities.FriendStatus;

public class DBAccess {
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBAccess(Context context) {
        dbHelper = new DBHelper(context);
    }
    
    /**
     * Open the database
     */
    public void openDB() {
        db = dbHelper.getWritableDatabase();
    }
    
    /**
     * Close the database
     */
    public void closeDB() {
        db.close();
    }
    
    /**
     * The Insert a FriendStatus into the DB
     * @param friendStatus the friends status to insert
     */
    public void insertFriendStatus(FriendStatus friendStatus) {        
        if (db != null) {
            ContentValues cv = getContentValuesForFriendStatus(friendStatus);
            long rowid = db.insertWithOnConflict(
                DBHelper.TABLE_NAME, 
                DBHelper.ID_COL, 
                cv, 
                SQLiteDatabase.CONFLICT_REPLACE);
        }
    }    

    /**
     * The Insert a FriendStatus into the DB
     * @param id the id of the status
     * @param name the name of the user
     * @param status the status text
     * @param imageurl the image URL
     */
    public void insert(String id, String name, String status, String imageurl) {
        if (db != null) {
            long timestamp = System.currentTimeMillis();
            db.execSQL("INSERT INTO friends('id', 'name', 'status', 'imageurl', 'timestamp') values ('"
            + id         + "', '"
            + name       + "', '"
            + status     + "', '" 
            + imageurl   +
            + timestamp  + "')");
        }
    }

    /**
     * Get content values for friend status
     * @param friendStatus the friend status to generate content values from
     * @return
     */
    private ContentValues getContentValuesForFriendStatus(FriendStatus friendStatus) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.ID_COL,         friendStatus.getId());
        cv.put(DBHelper.NAME_COL,       friendStatus.getName());
        cv.put(DBHelper.STATUS_COL,     friendStatus.getStatus());    
        cv.put(DBHelper.IMAGEURL_COL,   friendStatus.getImageUrl());
        cv.put(DBHelper.TIMESTAMP_COL,  friendStatus.getTimeStamp());
        return cv;
    }
       
    /**
     * Wipe out the DB
     */
    public void clearAll() {
        if (db != null) {
            db.delete(DBHelper.TABLE_NAME, null, null);
        }
    }

    /**
     * Select All the returns a Cursor
     * @return the cursor for the DB selection
     */
    public Cursor cursorSelectAll() {
        Cursor cursor = null;
        if (db != null) {
            cursor = this.db.query(
                DBHelper.TABLE_NAME, // Table Name
                new String[] { 
                        DBHelper.ID_COL, 
                        "name", 
                        "status", 
                        "imageurl", 
                        "timestamp" 
                        }, // Columns to return
                null,         // SQL WHERE
                null,         // Selection Args
                null,         // SQL GROUP BY
                null,         // SQL HAVING
                DBHelper.TIMESTAMP_COL); // SQL ORDER BY
        }
        return cursor;
    }
    

    /**
     * Select All that returns an ArrayList
     * @return the ArrayList for the DB selection
     */
    public ArrayList<FriendStatus> selectAllFriendStatus() {
        ArrayList<FriendStatus> list = null;
        Cursor cursor = null;
        if (db != null) {
            list = new ArrayList<FriendStatus>();
            cursor = cursorSelectAll();
            if (cursor.moveToFirst()) {
                do {
                    FriendStatus f = new FriendStatus();
                    f.setId       (cursor.getString((int)DBHelper.ColumnIndex.ID.ordinal()));
                    f.setName     (cursor.getString(1));
                    f.setStatus   (cursor.getString(2));
                    f.setImageUrl (cursor.getString(3));
                    f.setTimeStamp(cursor.getLong(4));
                    list.add(f);
                } while (cursor.moveToNext());
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

}
