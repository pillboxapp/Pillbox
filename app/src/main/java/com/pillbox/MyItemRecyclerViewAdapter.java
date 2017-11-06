package com.pillbox;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pillbox.DailyViewFragment.OnListFragmentInteractionListener;
import com.pillbox.DailyViewContent.DailyViewRow;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display an item and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<DailyViewRow> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyItemRecyclerViewAdapter(List<DailyViewRow> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mPillNameView.setText(mValues.get(position).pillName);
        holder.mDateView.setText(mValues.get(position).date);

        holder.mStatusView.setImageResource(getStatusImageResource(mValues.get(position).statusName));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    private int getStatusImageResource(Globals.Status status) {
        switch (status) {
            case SKIPPED: return R.drawable.red_circle;
            case TAKEN: return R.drawable.green_circle;
            case UPCOMING: return R.drawable.grey_circle;
            case TIME_TO_TAKE: return R.drawable.yellow_circle;
        }
        return R.drawable.grey_circle;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mPillNameView;
        final TextView mDateView;
        final ImageView mStatusView;
        DailyViewRow mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mPillNameView = view.findViewById(R.id.pill_name);
            mDateView = view.findViewById(R.id.date);
            mStatusView = view.findViewById(R.id.status_icon);
        }
    }
}
