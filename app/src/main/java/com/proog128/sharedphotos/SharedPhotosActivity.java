package com.proog128.sharedphotos;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IFilesystemService;
import com.proog128.sharedphotos.filesystem.IFilesystemServiceListener;
import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.filesystem.cache.CachedFilesystemFactory;
import com.proog128.sharedphotos.filesystem.dlna.DlnaFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SharedPhotosActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<LoaderResult>,IFilesystemServiceListener, AdapterView.OnItemClickListener {
    private GridView gridView_;
    private ImageAdapter adapter_;
    private ProgressBar progress_;

    private CachedFilesystemFactory fsFactory_;
    private IFilesystemService fsService_;
    private IFilesystem fs_;

    private IPath currentPath_;
    private Map<String, Parcelable> state_ = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        getLoaderManager(); // Must be called in onCreate to create LoaderManager.

        setContentView(R.layout.activity_sharedphotos);

        gridView_ = (GridView) findViewById(R.id.gridview);
        adapter_ = new ImageAdapter(getApplicationContext());

        progress_ = (ProgressBar) findViewById(R.id.progress);

        gridView_.setAdapter(adapter_);

        gridView_.setOnItemClickListener(this);

        if(savedInstanceState != null) {
            Object path = savedInstanceState.getSerializable("path");
            if (path != null) {
                currentPath_ = (IPath) path;
            }
        }

        try {
            long httpCacheSize = 100 * 1024 * 1024; // 100 MiB
            File httpCacheDir = new File(getExternalCacheDir(), "http");
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {

        }

        DlnaFactory dlnaFactory = new DlnaFactory();
        IFilesystemService dlnaService = dlnaFactory.init(getApplicationContext(), this);
        fsFactory_ = new CachedFilesystemFactory();
        fsService_ = fsFactory_.init(dlnaService);
        fsService_.start(this);
    }

    @Override
    protected void onDestroy() {
        getLoaderManager().destroyLoader(0);
        fsService_.stop();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        float size = Float.parseFloat(prefs.getString(SettingsActivity.KEY_PREF_THUMBNAIL_SIZE, "1.0"));
        gridView_.setColumnWidth((int) (getApplicationContext().getResources().getDimension(R.dimen.width) * size));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("path", currentPath_);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_sharedphotos_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettingsDialog();
                return true;
            case R.id.action_info:
                openInfoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettingsDialog() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openInfoDialog() {
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/info.html");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(webView);
        builder.create();
        builder.show();
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
        IPath p = (IPath) args.getSerializable("path");

        gridView_.setEnabled(false);
        progress_.setVisibility(View.VISIBLE);

        if(p.equals(fs_.getRootPath())) {
            return new DeviceLoader(this, fs_);
        } else {
            return new PathLoader(this, fs_, p, false);
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult data) {
        gridView_.setAdapter(null); // Reset adapter to scroll to the top (scrollTo() does not work)
        adapter_.clear();
        gridView_.setAdapter(adapter_);

        gridView_.setEnabled(true);
        progress_.setVisibility(View.GONE);

        if(data.error == LoaderError.Success) {
            adapter_.addAll(data.paths, data.parent, fs_);
            currentPath_ = data.parent;

            if(currentPath_ != null && !currentPath_.getLastElementName().isEmpty()) {
                getSupportActionBar().setTitle(currentPath_.getLastElementName());
            } else {
                getSupportActionBar().setTitle(R.string.title_activity_sharedphotos_devices);
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), data.errorText, Toast.LENGTH_LONG);
            toast.show();

            currentPath_ = fs_.getRootPath();
            adapter_.clear();

            Bundle b = new Bundle();
            b.putSerializable("path", currentPath_);
            getLoaderManager().restartLoader(0, b, this);
        }

        if(state_.get(currentPath_.toString()) != null) {
            gridView_.onRestoreInstanceState(state_.get(currentPath_.toString()));
            state_.remove(currentPath_.toString());
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult> loader) {
        getSupportActionBar().setTitle(R.string.title_activity_sharedphotos_devices);

        adapter_.clear();
    }

    @Override
    public void onInitialized(IFilesystem filesystem) {
        fs_ = filesystem;

        IPath target = currentPath_ != null ? currentPath_ : fs_.getRootPath();
        Bundle b = new Bundle();
        b.putSerializable("path", target);
        getLoaderManager().initLoader(0, b, this);
    }

    @Override
    public void onDestroyed(IFilesystem filesystem) {
        fs_ = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        state_.put(currentPath_.toString(), gridView_.onSaveInstanceState());
        IPath path = adapter_.getItem(position);
        IPath target = currentPath_.concat(path);

        if(target.isFile()) {
            Intent intent = new Intent(this, SlideshowActivity.class);
            intent.putExtra("path", target);
            startActivity(intent);
        } else {
            fsFactory_.makeDirty(target);
            state_.remove(target.toString());
            
            Bundle b = new Bundle();
            b.putSerializable("path", target);
            getLoaderManager().restartLoader(0, b, this);
        }
    }

    @Override
    public void onBackPressed() {
        if(currentPath_ == null ||
           (fs_ != null && currentPath_.equals(fs_.getRootPath()))) {
            super.onBackPressed();
            return;
        }

        IPath target = currentPath_.getParent();

        Bundle b = new Bundle();
        b.putSerializable("path", target);
        getLoaderManager().restartLoader(0, b, this);
    }

}
