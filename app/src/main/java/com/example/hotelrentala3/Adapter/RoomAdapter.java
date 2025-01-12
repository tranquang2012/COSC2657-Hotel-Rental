package com.example.hotelrentala3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelrentala3.Model.Room;
import com.example.hotelrentala3.R;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;

    public RoomAdapter(List<Room> roomList) {
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.hotelName.setText(room.getHotelName());
        holder.location.setText(room.getLocation());
        holder.type.setText("Type: " + room.getType());
        holder.capacity.setText("Capacity: " + room.getCapacity());
        holder.price.setText("Price: $" + room.getPrice());
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView hotelName, location, type, capacity, price;

        public RoomViewHolder(View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotel_name);
            location = itemView.findViewById(R.id.location);
            type = itemView.findViewById(R.id.type);
            capacity = itemView.findViewById(R.id.capacity);
            price = itemView.findViewById(R.id.price);
        }
    }
}

