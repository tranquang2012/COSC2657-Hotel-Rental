package com.example.hotelrentala3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private final List<HistoryActivity.Booking> bookingList;
    private final OnDetailsClickListener listener;

    public interface OnDetailsClickListener {
        void onDetailsClick(HistoryActivity.Booking booking);
    }

    public BookingAdapter(List<HistoryActivity.Booking> bookingList, OnDetailsClickListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryActivity.Booking booking = bookingList.get(position);
        holder.tvHotelName.setText(booking.getFullName());
        holder.tvCheckInCheckOut.setText("Check-in: " + booking.getCheckInDate() + " | Nights: " + booking.getNumberOfNights());
        holder.btnDetails.setOnClickListener(v -> listener.onDetailsClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvCheckInCheckOut;
        Button btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvCheckInCheckOut = itemView.findViewById(R.id.tvCheckInCheckOut);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
