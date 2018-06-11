package com.example.szabo.organizer;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Event> items;

    public EventAdapter(Context context, ArrayList<Event> items) {
        this.context=context;
        this.items = items;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mEventType;
        public TextView mLocation;
        public TextView mEventDate;
        public ImageView mEventImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mEventType = itemView.findViewById(R.id.eventType);
            mLocation = itemView.findViewById(R.id.location);
            mEventDate = itemView.findViewById(R.id.eventDate);
            mEventImage = itemView.findViewById(R.id.eventImage);
        }
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, final int position) {
        final Event item = items.get(position);
        if (!item.getEventPicture().equals("null")) {
            holder.mEventImage.setVisibility(View.VISIBLE);
            Glide
                    .with(context)
                    .load(item.getEventPicture())
                    .into(holder.mEventImage);
        }
        else
        {
            holder.mEventImage.setVisibility(View.GONE);
        }
        holder.mEventType.setText(item.getEventType());
        LatLng selectedLocation = item.getLocation();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(selectedLocation.latitude, selectedLocation.longitude, 1);
            String location = "";
            if (addresses.size() > 0) {
                if (addresses.get(0).getCountryName() != null) {
                    String country = addresses.get(0).getCountryName();
                    location = country;
                }
                if (addresses.get(0).getAdminArea() != null) {
                    String state = addresses.get(0).getAdminArea();
                    location = location + ", " + state;
                }
                if (addresses.get(0).getLocality() != null) {
                    String city = addresses.get(0).getLocality();
                    location = location + ", " + city;
                }
                if (addresses.get(0).getPostalCode() != null) {
                    String zip = addresses.get(0).getPostalCode();
                    location = location + ", " + zip;
                }
            }
            holder.mLocation.setText(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Date startingDate = item.getStartingDate();
        String displayText = String.valueOf(startingDate.getYear());
        int month = startingDate.getMonth() + 1;
        if (month/10 == 0)
        {
            displayText = displayText + ".0" + month;
        }
        else
        {
            displayText = displayText + "." + month;
        }
        if (startingDate.getDate()/10 == 0)
        {
            displayText = displayText + ".0" + startingDate.getDate();
        }
        else
        {
            displayText = displayText + "." + startingDate.getDate();
        }
        if (startingDate.getHours()/10 == 0)
        {
            displayText = displayText + ". 0" + startingDate.getHours();
        }
        else
        {
            displayText = displayText + ". " + startingDate.getHours();
        }
        if (startingDate.getMinutes()/10 == 0)
        {
            displayText = displayText + ":0" + startingDate.getMinutes();
        }
        else
        {
            displayText = displayText + ":" + startingDate.getMinutes();
        }
        holder.mEventDate.setText(displayText);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(context, EventDetailsActivity.class);
                eventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                eventIntent.putExtra("eventId", item.getEventId());
                context.startActivity(eventIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
