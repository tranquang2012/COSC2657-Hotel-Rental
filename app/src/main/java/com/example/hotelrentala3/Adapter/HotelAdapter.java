package com.example.hotelrentala3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelrentala3.Model.Hotel;
import com.example.hotelrentala3.R;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;

    public HotelAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        holder.name.setText("Name: " + hotel.getName());
        holder.latitude.setText("Latitude: " + hotel.getLatitude());
        holder.longitude.setText("Longitude: " + hotel.getLongitude());
        holder.availability.setText("Availability: " + hotel.getAvailability());
        holder.price.setText("Price: $" + hotel.getPrice());
        holder.rating.setText("Rating: " + hotel.getRating());
    }


    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView name, latitude, longitude, availability, price, rating;

        public HotelViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.hotel_name);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            availability = itemView.findViewById(R.id.availability);
            price = itemView.findViewById(R.id.price);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}

