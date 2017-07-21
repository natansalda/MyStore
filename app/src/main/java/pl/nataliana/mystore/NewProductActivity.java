package pl.nataliana.mystore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import pl.nataliana.mystore.data.ProductContract.ProductEntry;

public class NewProductActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;
    private ImageView imageViewP;
    private EditText editTextNameP;
    private EditText editTextPriceP;
    private EditText editTextProvider;
    private EditText editTextQuantity;
    public static final int PHOTO_REQUEST_CODE = 20;
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;
    private String currentPhotoUri = "no image";
    private int rowsDeleted = 0;
    private boolean productChange = false;

    // Check for field changes.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productChange = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Binding the views.
        ScrollView scrollview = (ScrollView) findViewById(R.id.scrollview);
        ImageView imageViewP = (ImageView) findViewById(R.id.product_image);
        editTextNameP = (EditText) findViewById(R.id.textview_product_name);
        editTextPriceP = (EditText) findViewById(R.id.textview_text_price);
        editTextProvider = (EditText) findViewById(R.id.textview_text_provider);
        editTextQuantity = (EditText) findViewById(R.id.amount);
        TextView textImage = (TextView) findViewById(R.id.text_image_product);
        final TextView instruction_img = (TextView) findViewById(R.id.instruction_img);
        View viewSeparate = (View) findViewById(R.id.view_separate);

        scrollview.fullScroll(ScrollView.FOCUS_UP);

        imageViewP.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                productChange = true;
                instruction_img.setVisibility(View.GONE);
                return false;
            }
        });
        editTextNameP.setOnTouchListener(mTouchListener);
        editTextPriceP.setOnTouchListener(mTouchListener);
        editTextProvider.setOnTouchListener(mTouchListener);
        editTextQuantity.setOnTouchListener(mTouchListener);

        // Photo update listener
        imageViewP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizeProductImg(v);
            }
        });

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null) {
            setTitle(getString(R.string.title_activity_new_product));
            textImage.setVisibility(View.VISIBLE);
            viewSeparate.setVisibility(View.GONE);
            invalidateOptionsMenu();

        } else {
            setTitle(getString(R.string.title_activity_modify_product));
            textImage.setVisibility(View.GONE);
            instruction_img.setVisibility(View.GONE);
            editTextNameP.setHintTextColor(getResources().getColor(R.color.colorHint));
            viewSeparate.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    //Perform the removal of the product in the database.
    private void deleteProduct() {
        // Only perform the delete if the product exists.
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NewProductActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void cancelChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.continue_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!productChange) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        cancelChangesDialog(discardButtonClickListener);
    }

    public void actualizeProductImg(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                chooseProductImg();
            } else {
                String[] permisionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permisionRequest, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            chooseProductImg();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            chooseProductImg();
        } else {
            Toast.makeText(this, R.string.permision, Toast.LENGTH_LONG).show();
        }
    }

    private void chooseProductImg() {
        Intent selectImg = new Intent(Intent.ACTION_PICK);
        File imgDirectory = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = imgDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);

        selectImg.setDataAndType(data, "image/*");

        startActivityForResult(selectImg, PHOTO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
            }
            Uri mProductPhotoUri = data.getData();
            currentPhotoUri = mProductPhotoUri.toString();

            Picasso.with(this).load(mProductPhotoUri)
                    .placeholder(R.drawable.new_image)
                    .fit()
                    .into(imageViewP);
        }
    }

    private void AddNewProduct() {
        String name = editTextNameP.getText().toString();
        String price = editTextPriceP.getText().toString();
        String provider = editTextProvider.getText().toString();
        String amount = editTextQuantity.getText().toString();

        if (name.isEmpty() || price.isEmpty() || provider.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, R.string.missing, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_IMAGE_PRODUCT, currentPhotoUri);
        values.put(ProductEntry.COLUMN_NAME_PRODUCT, name);
        values.put(ProductEntry.COLUMN_PRICE_PRODUCT, price);
        values.put(ProductEntry.COLUMN_PROVIDER_PRODUCT, provider);
        values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, amount);
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, 0.0);

        if (currentProductUri == null) {

            Uri insertedRow = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (insertedRow == null) {
                Toast.makeText(this, R.string.error_save_changes, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.added, Toast.LENGTH_LONG).show();
            }
        } else {
            int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);

            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.error_save_changes, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.modified, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_product_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product);
            menuItem.setVisible(false);
        }

        if (currentProductUri != null) {
            MenuItem menuItem = menu.findItem(R.id.saved_product);
            menuItem.setIcon(R.drawable.ic_done_white_18dp);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saved_product:
                AddNewProduct();
                finish();
                return true;
            case R.id.delete_product:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
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

            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String provider = cursor.getString(providerColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            currentPhotoUri = cursor.getString(imageColumnIndex);

            editTextNameP.setText(name);
            editTextPriceP.setText(String.valueOf(price));
            editTextProvider.setText(provider);
            editTextQuantity.setText(String.valueOf(quantity));

            Picasso.with(this).load(currentPhotoUri)
                    .placeholder(R.drawable.new_image)
                    .fit()
                    .into(imageViewP);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        editTextNameP.setText("");
        editTextProvider.setText("");
        editTextQuantity.setText("");
        editTextPriceP.setText("");
    }
}
