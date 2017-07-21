package pl.nataliana.mystore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import pl.nataliana.mystore.data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database
     */
    private static final String DATABASE_NAME = "myStore.db";

    /**
     * Version of the database
     */
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a string containing the SQL statement to create the product table.
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_IMAGE_PRODUCT + " TEXT NOT NULL DEFAULT 'no image', "
                + ProductEntry.COLUMN_NAME_PRODUCT + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRICE_PRODUCT + " REAL NOT NULL, "
                + ProductEntry.COLUMN_PROVIDER_PRODUCT + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_QUANTITY_PRODUCT + " INTEGER DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_SALES + " REAL DEFAULT 0.0 );";

        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);
        onCreate(db);
    }
}
