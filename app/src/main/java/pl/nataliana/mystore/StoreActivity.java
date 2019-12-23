package pl.nataliana.mystore;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import pl.nataliana.mystore.data.ProductContract.ProductEntry;

public class StoreActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private final static String MSG_PRODUCTO_NUEVO = "Hello! Please take a look at our new products!";

    private Uri currentProductUri;

    private String currentPhotoUri = "no image";

    private TextView nameProd;
    private TextView priceProd;
    private TextView providerProd;
    private TextView salesProd;
    private EditText stockProd;
    private ImageView imageProd;
    private ImageView requestItem;
    private TextView amountProd;

    private int amount = 0;
    private int clicks = 0;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        nameProd = (TextView) findViewById(R.id.tv_name_product);
        priceProd = (TextView) findViewById(R.id.textview_text_price);
        providerProd = (TextView) findViewById(R.id.textview_text_provider);
        salesProd = (TextView) findViewById(R.id.sales_product);
        stockProd = (EditText) findViewById(R.id.quantity);
        amountProd = (TextView) findViewById(R.id.amount_product);
        Button buttonRestar = (Button) findViewById(R.id.restar_stock);
        Button buttonSumar = (Button) findViewById(R.id.sumar_stock);
        imageProd = (ImageView) findViewById(R.id.image_product);
        requestItem = (ImageView) findViewById(R.id.request_item);


        final Intent intent = getIntent();
        currentProductUri = intent.getData();

        getLoaderManager().initLoader(0, null, this);

        buttonRestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountProd.setText(getString(R.string.order_quantity));
                RestarStock();
            }
        });

        buttonSumar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountProd.setText(getString(R.string.order_quantity));
                SumarStock();
            }
        });

        requestItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRequestMerchandise();
            }
        });
    }

    private void ActualiseProductStock() {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_sale_msg,
                (ViewGroup) findViewById(R.id.container_toast_sale));

        //Show toast reminding about writing to provider
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CLIP_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
        String amount = stockProd.getText().toString();

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, amount);

        if (currentProductUri != null) {
            int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);

            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.error_stock, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.stock_updated, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void RestarStock() {
        amount = Integer.parseInt(stockProd.getText().toString());
        if (amount >= 10) {
            int restaStock = amount - 10;
            String actuazliseStock = String.valueOf(restaStock);
            stockProd.setText(actuazliseStock);
            clicks++;

            if (clicks % 2 == 1) {
                snackbar = Snackbar
                        .make(getCurrentFocus(), R.string.updated_stock,
                                Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.colorStock));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
            }
        }
        if (amount < 10) {
            int restaStock = amount - amount;
            String actuazliseStock = String.valueOf(restaStock);
            stockProd.setText(actuazliseStock);
            clicks++;

            if (clicks % 2 == 1) {
                snackbar = Snackbar
                        .make(getCurrentFocus(), R.string.updated_stock,
                                Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.colorStock));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
            }
            if (amount == 0) {
                Toast.makeText(this, R.string.stock_empty, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SumarStock() {
        amount = Integer.parseInt(stockProd.getText().toString());
        int some = amount + 10;
        String suma = String.valueOf(some);
        stockProd.setText(suma);
        clicks++;

        if (clicks % 2 == 0) {
            snackbar = Snackbar
                    .make(getCurrentFocus(), R.string.updated_stock,
                            Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.colorStock));
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            snackbar.setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            })
                    .show();
        }
    }

    private void saleRequest() {
        String product = nameProd.getText().toString();
        String provider = providerProd.getText().toString();
        String providerWebsite = "www." + provider + ".pl";
        String[] source = {providerWebsite};
        String stockRequest = stockProd.getText().toString();

        StringBuilder builder = new StringBuilder();
        builder.append("Hello " + provider + " :\n");
        builder.append("I need to order the following products:\n");
        builder.append("PRODUCT: " + product + "\n");
        builder.append("AMOUNT: " + stockRequest + "\n");
        builder.append("\nThank you,  best regards!");
        String request = builder.toString();

        Intent intentSale = new Intent(Intent.ACTION_SEND);
        intentSale.setData(Uri.parse("mailto:"));
        intentSale.setType("text/plain");
        intentSale.putExtra(Intent.EXTRA_EMAIL, source);
        intentSale.putExtra(Intent.EXTRA_SUBJECT, "Merchandise Request ");
        intentSale.putExtra(Intent.EXTRA_TEXT, request);
        startActivity(Intent.createChooser(intentSale, "Merchandise Request"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sale_product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_product_atributes:
                ModifyProductFields();
                finish();
                return true;
            case R.id.saved_stock:
                ActualiseProductStock();
                return true;
            case R.id.action_share:
                ShareProduct();
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Share Product.
    private void ShareProduct() {
        String product = nameProd.getText().toString();
        String provider = providerProd.getText().toString();
        String price = priceProd.getText().toString();

        StringBuilder builderNewProduct = new StringBuilder();
        builderNewProduct.append(MSG_PRODUCTO_NUEVO + " \n\n"
                + product + "\n" + provider + "\n" + price);

        String some = builderNewProduct.toString();

        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("text/plain");
        intentShare.putExtra(Intent.EXTRA_SUBJECT, "New Product");
        intentShare.putExtra(Intent.EXTRA_TEXT, some);
        startActivity(Intent.createChooser(intentShare, "Share New Product"));
    }

    // Modify product
    private void ModifyProductFields() {
        Intent intent = new Intent(StoreActivity.this, NewProductActivity.class);
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,
                ContentUris.parseId(this.currentProductUri));
        intent.setData(currentProductUri);
        startActivity(intent);
        finish();
    }

    private void showDialogRequestMerchandise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.request_goods);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saleRequest();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("REQUEST SALE");
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_IMAGE_PRODUCT,
                ProductEntry.COLUMN_NAME_PRODUCT,
                ProductEntry.COLUMN_PRICE_PRODUCT,
                ProductEntry.COLUMN_PROVIDER_PRODUCT,
                ProductEntry.COLUMN_QUANTITY_PRODUCT,
                ProductEntry.COLUMN_PRODUCT_SALES};

        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE_PRODUCT);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE_PRODUCT);
            int providerColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY_PRODUCT);
            int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String provider = cursor.getString(providerColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            currentPhotoUri = cursor.getString(imageColumnIndex);
            double pvp = cursor.getDouble(salesColumnIndex);

            nameProd.setText(name);
            priceProd.setText(String.valueOf(price) + " €");
            providerProd.setText(provider.toUpperCase());
            salesProd.setText(String.valueOf(pvp) + " €");
            stockProd.setText(String.valueOf(quantity));

            Picasso.with(this).load(currentPhotoUri)
                    .placeholder(R.drawable.new_image)
                    .fit()
                    .into(imageProd);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameProd.setText("");
        priceProd.setText(String.valueOf(""));
        providerProd.setText("");
        salesProd.setText("");
        amountProd.setText(R.string.stock);
    }

    public void notifyImageChange(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_notify_image_change,
                (ViewGroup) findViewById(R.id.content_toast));
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CLIP_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
