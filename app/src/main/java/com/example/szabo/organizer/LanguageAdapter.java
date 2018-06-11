package com.example.szabo.organizer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    private ArrayList<String> languageToLoad;
    private Context context;
    private LanguagePreferences languagePreferences;
    private ProgressDialog mProgress;

    public LanguageAdapter(Context context) {
        this.context = context;
        languageToLoad = new ArrayList<String>();
        languageToLoad.add("en");
        languageToLoad.add("hu");
        languageToLoad.add("ro");
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mLanguage;

        public ViewHolder(View itemView) {
            super(itemView);
            mLanguage = (TextView) itemView.findViewById(R.id.languageField);
        }
    }

    @Override
    public LanguageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_language, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String item = languageToLoad.get(position);
        holder.mLanguage.setText(new Locale(item).getDisplayLanguage());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress = new ProgressDialog(context);
                mProgress.setMessage(context.getResources().getString(R.string.loadingLanguage));
                mProgress.show();
                if (!Locale.getDefault().getLanguage().equals(languageToLoad.get(holder.getAdapterPosition()))) {
                    Locale locale = new Locale(languageToLoad.get(holder.getAdapterPosition()));
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    languagePreferences = LanguagePreferences.getInstance();
                    languagePreferences.setContext(context);
                    languagePreferences.setLanguage(languageToLoad.get(holder.getAdapterPosition()));

                    context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
                    ((Activity)context).finish();
                    mProgress.dismiss();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return languageToLoad.size();
    }
}
