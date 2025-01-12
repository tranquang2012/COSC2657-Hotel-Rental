package com.example.hotelrentala3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class HotelInfoAdapter extends BaseAdapter {

    private Context context;
    private List<DocumentSnapshot> hotelDocuments;

    public HotelInfoAdapter(Context context, List<DocumentSnapshot> hotelDocuments) {
        this.context = context;
        this.hotelDocuments = hotelDocuments;
    }

    @Override
    public int getCount() {
        return hotelDocuments.size();
    }

    @Override
    public Object getItem(int position) {
        return hotelDocuments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_hotel, parent, false);
        }

        TextView textViewHotelName = convertView.findViewById(R.id.textViewHotelName);
        TextView textViewHotelPrice = convertView.findViewById(R.id.textViewHotelPrice);
        TextView textViewHotelRating = convertView.findViewById(R.id.textViewHotelRating);

        DocumentSnapshot document = hotelDocuments.get(position);

        String name = document.getString("name");
        double price = document.getDouble("price");
        double rating = document.getDouble("rating");

        textViewHotelName.setText(name != null ? name : "Unknown Hotel");
        textViewHotelPrice.setText("Price: $" + price);
        textViewHotelRating.setText("Rating: " + rating + "★");

        return convertView;
    }
}
