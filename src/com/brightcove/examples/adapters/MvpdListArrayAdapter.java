package com.brightcove.examples.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.brightcove.auth.model.IProvider;
import com.brightcove.examples.R;
import com.brightcove.utils.DownloadImageTask;

import java.util.List;

/**
 * ArrayAdapter class for rendering the row items in the Mvpd selector
 *
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @since 1.0
 */
public class MvpdListArrayAdapter extends ArrayAdapter<IProvider> {
    // ResourceId for the row item view container/wrapper
    private int rowItemResourceId;

    /**
     * Constructs a new MvpdListArrayAdapter for rendering the row items for the Mvpd selector
     * @param context the view context for the ArrayAdapter
     * @param rowItemResourceId the resourceId of the row item view container/wrapper
     * @param providers the array of provider items to render
     * @since 1.0
     */
    public MvpdListArrayAdapter(Context context, int rowItemResourceId, List<IProvider> providers) {
        super(context, rowItemResourceId, providers);
        this.rowItemResourceId = rowItemResourceId;
    }

    /**
     * Updates and returns the row item view container/wrapper at the provided position for the list view
     * @param position the row position to update
     * @param convertView the current/previous view container/wrapper for the row item. If null, then a new view will be created
     * @param parent the parent view container
     * @return the row item view container/wrapper
     * @since 1.0
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout mvpdItemView;

        IProvider provider = getItem(position);

        if (convertView == null) {
            mvpdItemView = new LinearLayout(getContext());
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(rowItemResourceId, mvpdItemView, true);
        } else {
            mvpdItemView = (LinearLayout) convertView;
        }

        ImageView mvpdIcon = (ImageView) mvpdItemView.findViewById(R.id.mvpd_logo);
        new DownloadImageTask(mvpdIcon).execute(provider.getLogo());

        TextView mvpdNameTv = (TextView) mvpdItemView.findViewById(R.id.mvpd_name);
        mvpdNameTv.setText(provider.getName());

        return mvpdItemView;
    }
}
