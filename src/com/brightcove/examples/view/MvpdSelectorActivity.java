package com.brightcove.examples.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.brightcove.auth.model.IProvider;
import com.brightcove.examples.R;
import com.brightcove.examples.adapters.MvpdListArrayAdapter;

import java.util.ArrayList;

/**
 * Mvpd Selector Activity to handle the loading of the Selector {@link android.widget.ListView}
 * with the {@link com.brightcove.examples.adapters.MvpdListArrayAdapter} to manage the display the Mvpds
 * @author Maximilian Nyman (max.nyman@anvilcreative.com)
 * @see com.brightcove.examples.adapters.MvpdListArrayAdapter
 * @see com.brightcove.auth.model.IProvider
 * @see android.widget.ListView
 * @since 1.0
 */
public class MvpdSelectorActivity extends Activity {
    // Keep the current intent, which will then be used when closing the Selector activity
    private Intent currentIntent;

    /**
     * Initiates the Selector Activity.
     * Gets the array of providers, and then instantiates the {@link android.widget.ListView}
     * with the {@link com.brightcove.examples.adapters.MvpdListArrayAdapter} to manage the display
     * the array of {@link com.brightcove.auth.model.IProvider}
     * @param savedInstanceState ignored by this subclass, and is only passed along to the super class
     * @see #getIntent()
     * @see android.content.Intent#getParcelableExtra(String)
     * @see com.brightcove.examples.adapters.MvpdListArrayAdapter
     * @see com.brightcove.auth.model.IProvider
     * @see android.widget.ListView
     * @since 1.0
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the layout for this activity
        setContentView(R.layout.mvpd_selector);
        // Get Current Intent
        currentIntent = getIntent();
        // Getting the Providers from the Intent
        ArrayList<IProvider> providers = currentIntent.getParcelableArrayListExtra("providers");

        // get references to UI elements
        ListView mvpdListView = (ListView) findViewById(R.id.mvpd_list);
        MvpdListArrayAdapter mvpdListArrayAdapter = new MvpdListArrayAdapter(this, R.layout.mvpd_selector_list_item, providers);
        mvpdListView.setAdapter(mvpdListArrayAdapter);
        mvpdListView.setOnItemClickListener(onItemClickListener);
        mvpdListArrayAdapter.notifyDataSetChanged();
    }

    /**
     * Click handler for the Mvpd Selector.
     * Gets and returns the selected {@link com.brightcove.auth.model.IProvider} back to the launching activity
     * @see com.brightcove.auth.model.IProvider
     * @since 1.0
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            IProvider provider = (IProvider) adapterView.getItemAtPosition(position);
            currentIntent.putExtra("provider", provider);
            setResult(RESULT_OK, currentIntent);
            finish();
        }
    };

}
