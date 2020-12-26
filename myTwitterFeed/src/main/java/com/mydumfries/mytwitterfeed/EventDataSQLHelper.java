package com.mydumfries.mytwitterfeed;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/** Helper to the database, manages versions and creation */
public class EventDataSQLHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "mydumfries.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "books";
	public static final String TABLE2 = "diary";
	public static final String TABLE3 = "todos";

	// Columns
	public static final String AUTHOR = "Author";
	public static final String TITLE = "Title";
	public static final String RETURNBY = "ReturnBy";
	public static final String RETURNED = "Returned";
	public static final String TIMESTAMP = "timestamp";
	public static final String RATING = "Rating";
	public static final String ISBN = "ISBN";
	public static final String LINK = "Link";
	public static final String NOTES = "Notes";
	public static final String ID = "id";
	public static final String MEMO = "Memo";
	public static final String DATE = "Date";
	public static final String FLAG = "Flag";
	public static final String TODO = "todo";

	public EventDataSQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key, " + AUTHOR + " text, " + RETURNBY
				+ " text, " + RETURNED + " text, " + NOTES + " text, "
				+ TIMESTAMP + " date default CURRENT_DATE, " + TITLE
				+ " text, " + RATING + " integer, " + ISBN + " integer, "
				+ LINK + " text, " + FLAG + " integer default 0);";
		Log.d("EventsData", "onCreate: " + sql);
		db.execSQL(sql);
		sql = "create table " + TABLE2 + "( " + BaseColumns._ID
				+ " integer primary key, " + TIMESTAMP
				+ " date default CURRENT_DATE, " + MEMO + " text, " + DATE
				+ " date default CURRENT_DATE, " + FLAG
				+ " integer default 0);";
		Log.d("EventsData", "onCreate: " + sql);
		db.execSQL(sql);
		sql = "create table " + TABLE3 + "( " + BaseColumns._ID
				+ " integer primary key, " + TIMESTAMP
				+ " date default CURRENT_DATE, " + TODO + " text, " + FLAG
				+ " integer default 0);";
		Log.d("EventsData", "onCreate: " + sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;
		String sql = null;
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add COLUMN timestamp text;";
		if (oldVersion == 2)
			sql = "";

		Log.d("EventsData", "onUpgrade	: " + sql);
		if (sql != null)
			db.execSQL(sql);
	}
}
