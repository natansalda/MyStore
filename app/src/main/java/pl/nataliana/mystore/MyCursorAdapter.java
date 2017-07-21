package pl.nataliana.mystore;

import android.widget.CursorAdapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pl.nataliana.mystore.data.ProductContract.ProductEntry;

import com.squareup.picasso.Picasso;

public class MyCursorAdapter extends CursorAdapter {

    public MyCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_list, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Bind Views.
        ImageView imageViewProduct = (ImageView) view.findViewById(R.id.product_foto);
        TextView textViewNameP = (TextView) view.findViewById(R.id.product_name);
        TextView textViewPriceP = (TextView) view.findViewById(R.id.price);
        TextView textViewProviderP = (TextView) view.findViewById(R.id.provider_product);
        TextView textViewAmountP = (TextView) view.findViewById(R.id.amount);
        final TextView totalProductSale = (TextView) view.findViewById(R.id.text_view_sale);
        ImageView buyButtonProduct = (ImageView) view.findViewById(R.id.but_button);

        // Find the proper columns
        int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE_PRODUCT);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT);
        final int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE_PRODUCT);
        int providerColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_PRODUCT);
        int amountColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY_PRODUCT);
        int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

        // Read the attributes of the product from the Cursor for the current product.
        int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        Uri productPicture = Uri.parse(cursor.getString(pictureColumnIndex));
        final String productName = cursor.getString(nameColumnIndex);
        final double precioProduct = priceColumnIndex;
        final double priceP = cursor.getDouble(priceColumnIndex);
        String productPrice = "Price: " + cursor.getString(priceColumnIndex) + " €";
        String productProvider = cursor.getString(providerColumnIndex);
        final int amount = cursor.getInt(amountColumnIndex);
        String productQuantity = "Stock\n" + cursor.getString(amountColumnIndex);
        final double sumProductSale = cursor.getDouble(salesColumnIndex);
        String profitFromProduct = "Profit: " + cursor.getString(salesColumnIndex) +
                " €";

        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        // Update TextViews with attributes for the current product
        textViewNameP.setText(productName);
        textViewPriceP.setText(productPrice);
        textViewProviderP.setText(productProvider);
        textViewAmountP.setText(productQuantity);
        totalProductSale.setText(profitFromProduct);

        // Picasso used to update the product photo.
        Picasso.with(context).load(productPicture)
                .placeholder(R.drawable.new_image)

                .fit()
                .into(imageViewProduct);

        // Listen for the product sale button.
        buyButtonProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (amount > 0) {
                    int stock = amount;
                    double p = priceP;
                    double sumaTot = sumProductSale + p;
                    values.put(ProductEntry.COLUMN_PRODUCT_SALES, sumaTot);
                    values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, --stock);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentProductUri, null);
                } else {
                    // Inform the user when the store is empty.
                    Toast.makeText(context, R.string.empty_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
