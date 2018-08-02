package com.perfectskater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String F_NAME = "f_name";
    public static final String L_NAME = "l_name";

    public static final String PRO_TABLE = "pro_table";

    public static final String CARD_NUM = "card_num";
    public static final String CVV = "cvv";
    public static final String EXP_DATE = "exp_date";

    public static final String PAY_TABLE = "pay_table";

    public static final String DATABASE_NAME = "dbRides";

    public static final int db_version = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, db_version);
    }




    public String PRO_QUERY = "CREATE TABLE " + PRO_TABLE + "(" + F_NAME + " TEXT," + L_NAME + " TEXT);";


    public String PAY_QUERY = "CREATE TABLE " + PAY_TABLE + "(" + CARD_NUM + " TEXT," + CVV + " TEXT," + EXP_DATE + " TEXT);";

    @Override
    public void onCreate(SQLiteDatabase sdb) {
        sdb.execSQL(PRO_QUERY);
        sdb.execSQL(PAY_QUERY);

    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PRO_TABLE);
        onCreate(db);
    }

    public void putProfile(DatabaseHelper dop, String fName, String lName){

            SQLiteDatabase sqLiteDatabase = dop.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(F_NAME, fName);
            cv.put(L_NAME, lName);
            sqLiteDatabase.insert(PRO_TABLE, null, cv);

        }

    public void putPayment(DatabaseHelper dop, String cardNumber, String cvv, String expDate){
        SQLiteDatabase sqLiteDatabase = dop.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CARD_NUM, cardNumber);
        cv.put(CVV, cvv);
        cv.put(EXP_DATE, expDate);
        sqLiteDatabase.insert(PAY_TABLE, null, cv);

    }



    public Cursor getProfileInformation(DatabaseHelper dop) {
        SQLiteDatabase SQ = dop.getReadableDatabase();
        String[] coloumns = {F_NAME, L_NAME,};
        Cursor CR = SQ.query(PRO_TABLE, coloumns, null, null, null, null, null);
        return CR;

    }

    public Cursor getPaymentInformation(DatabaseHelper dop) {
        SQLiteDatabase SQ = dop.getReadableDatabase();
        String[] coloumns = {CARD_NUM, CVV,EXP_DATE,};
        Cursor CR = SQ.query(PAY_TABLE, coloumns, null, null, null, null, null);
        return CR;

    }

    public int getCount(String tname) {
        String countQuery = "SELECT  * FROM " + tname;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    









}
