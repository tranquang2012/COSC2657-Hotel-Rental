package com.example.hotelrentala3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelrentala3.Model.Hotel;
import com.example.hotelrentala3.R;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private OnItemClickListener onItemClickListener;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(Hotel hotel);
    }


    public HotelAdapter(List<Hotel> hotelList, OnItemClickListener onItemClickListener) {
        this.hotelList = hotelList;
        this.onItemClickListener = onItemClickListener;
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
        holder.location.setText("Location: " + hotel.getLocation());
        holder.availability.setText("Rooms Available: " + hotel.getAvailability() + " rooms");
        holder.price.setText("Price: $" + hotel.getPrice());
        holder.rating.setText("Rating: " + hotel.getRating());
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(hotel);
            }
        });
    }


    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView name, location, availability, price, rating;

        public HotelViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.hotel_name);
            location = itemView.findViewById(R.id.location);
            availability = itemView.findViewById(R.id.availability);
            price = itemView.findViewById(R.id.price);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}

