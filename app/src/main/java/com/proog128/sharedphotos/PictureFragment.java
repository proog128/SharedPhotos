package com.proog128.sharedphotos;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.proog128.sharedphotos.filesystem.IPath;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PictureFragment extends Fragment implements LoaderManager.LoaderCallbacks<ImageLoader.Image> {
    private IPath path_;
    private ImageView image_;
    private ProgressBar progress_;
    private PhotoViewAttacher attacher_;
    private TextView caption_;
    private int orientation_ = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    public void setUrl(IPath path) {
        path_ = path;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        updateOrientation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_picture, container, false);

        //TextView urlView = (TextView) rootView.findViewById(R.id.url);
        //urlView.setText(path_.getContentUrl());

        caption_ = (TextView) rootView.findViewById(R.id.caption);

        image_ = (ImageView) rootView.findViewById(R.id.image);
        progress_ = (ProgressBar) rootView.findViewById(R.id.progress);
        attacher_ = new PhotoViewAttacher(image_);

        getLoaderManager().initLoader(0, null, this);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        attacher_.cleanup();
        getLoaderManager().destroyLoader(0);

        super.onDestroyView();
    }

    @Override
    public Loader<ImageLoader.Image> onCreateLoader(int id, Bundle args) {
        progress_.setVisibility(View.VISIBLE);
        return new ImageLoaderTask(getActivity(), path_);
    }

    @Override
    public void onLoadFinished(Loader<ImageLoader.Image> loader, ImageLoader.Image img) {
        progress_.setVisibility(View.GONE);

        Bitmap bmp = img.getBitmap();
        if(bmp.getWidth() > bmp.getHeight()) {
            orientation_ = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            orientation_ = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        image_.setImageBitmap(bmp);
        caption_.setText(img.getCaption());
        attacher_.update();

        updateOrientation();
    }

    @Override
    public void onLoaderReset(Loader<ImageLoader.Image> loader) {
        progress_.setVisibility(View.GONE);
        image_.setImageBitmap(null);
        caption_.setText("");
        attacher_.update();
    }

    private void updateOrientation() {
        if(getUserVisibleHint() && orientation_ != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            getActivity().setRequestedOrientation(orientation_);
        }
    }
}
