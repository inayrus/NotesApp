package com.example.finalappproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NoteDatabase extends SQLiteOpenHelper {

    // turn NoteDatabase in a Singleton: only one instance is allowed to exist
    private static NoteDatabase instance;

    public static NoteDatabase getInstance(Context context) {
        if (NoteDatabase.instance == null) {
            NoteDatabase.instance = new NoteDatabase(context);
        }
        return NoteDatabase.instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Notes(_id INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT, Content TEXT, Tags TEXT, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);");

        // an example entry
        db.execSQL("INSERT INTO Notes(Title, Content, Tags) VALUES('Test Title!'," +
                "'Test content goes like blablablablablablabalbalablabalbalbalbalablal', 'tag1,tag2')");
        db.execSQL("INSERT INTO Notes(Title, Content, Tags) VALUES('Title 2'," +
                "'Test content goes like blablablablablablabalbalablabalbalbalbalablal', 'tag1.1,tag2.1')");
    }

    // a constructor
    private NoteDatabase(Context context) {
        super(context, "Notes", null, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // deletes a table and creates a new one
        db.execSQL("DROP TABLE IF EXISTS " + "Notes");
        onCreate(db);
    }

    // select method
    public Cursor selectAll() {
        // open database
        SQLiteDatabase db = getWritableDatabase();

        // select all, return the cursor
        return db.rawQuery("SELECT * FROM Notes",null);
    }

    // insert method
    public void insert(Note note) {
        SQLiteDatabase db = getWritableDatabase();

        // create object that binds values to SQLite columns
        ContentValues newRow = new ContentValues();

//        // get the tags
//        String stringTags = note.getUpdatedStringTags();

        // put entry values in the right columns
        newRow.put("Title", note.getTitle());
        newRow.put("Content", note.getContent());
//        newRow.put("Tags", stringTags);

        // insert the column in database
        db.insert("Notes", null, newRow);
    }

    // update method
    public void update(long id, Note note) {
        SQLiteDatabase db = getWritableDatabase();

        // create object that binds values to SQLite columns
        ContentValues updatedRow = new ContentValues();

        // put entry values in the right columns
        updatedRow.put("Title", note.getTitle());
        updatedRow.put("Content", note.getContent());
//        updatedRow.put("Tags", note.getUpdatedStringTags());

        db.update("Notes", updatedRow,
                "_id = ?", new String[] { String.valueOf(id) });
    }

    //delete method
    public void delete(long id) {
        // open database
        SQLiteDatabase db = getWritableDatabase();

        // delete the entry
        db.delete("Notes", "_id = ?", new String[] { String.valueOf(id) });
    }
}