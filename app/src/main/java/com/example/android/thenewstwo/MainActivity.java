package com.example.android.thenewstwo;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>> {

    public static final String LOG_TAG = MainActivity.class.getName();
    //Guardian API request with key
    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search?&show-tags=contributor&api-key=07983b11-a6bf-4fea-a82c-340445bebd85";
    private static final int NEWS_LOADER_ID = 1;
    private NewsAdapter mAdapter;
    private TextView mEmptyStateTextView;
    ListView newsappListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign list views
        newsappListView = findViewById(R.id.list);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        newsappListView.setEmptyView(mEmptyStateTextView);
        mAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsappListView.setAdapter(mAdapter);
        newsappListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            //Open article website intent
            @Override
            public void onItemClick (AdapterView < ? > adapterView, View view, int position, long l){
                News currentNews = mAdapter.getItem(position);
                Uri newsappUri = Uri.parse(currentNews.getUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsappUri);
                startActivity(websiteIntent);
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    /**
     * overide loader
     * @param i
     * @param bundle
     * @return @NewsLoader
     */
    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String numberOfArticles = sharedPreferences.getString(getString(R.string.settings_key_numof), getString(R.string.settings_dflt_numof));

        String orderBy  = sharedPreferences.getString(
                getString(R.string.settings_key_orderby),
                getString(R.string.settings_dflt_orderby)
        );

        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("numberOfArticles", numberOfArticles);
        uriBuilder.appendQueryParameter("orderby", "time");
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new NewsLoader(this, uriBuilder.toString());
    }

    /**
     * Overide onloadFinished loader
     * @param loader
     * @param news
     */
    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        mEmptyStateTextView.setText(R.string.no_articles_found);
        if (news != null && !news.isEmpty()) {
            updateUi(news);
        }
    }

    /**
     * Overide reset loader
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mAdapter.clear();
    }

    /**
     * Overide boolean onCreateOptions Menu
     * @param menu
     * @return boolean value
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     *  Overide boolean optionsItemsSelected
     * @param item
     * @return boolean value
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ethod for updating ui
     * @param newsapp
     */
    private void updateUi(List<News> newsapp) {
        mEmptyStateTextView.setVisibility(View.GONE);
        mAdapter.clear();
        mAdapter = new NewsAdapter(this, newsapp);
        newsappListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}

