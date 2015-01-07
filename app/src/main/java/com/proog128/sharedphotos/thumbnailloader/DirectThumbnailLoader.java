package com.proog128.sharedphotos.thumbnailloader;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.proog128.sharedphotos.ImageLoader;
import com.proog128.sharedphotos.filesystem.IPath;

import java.net.URL;
import java.util.concurrent.Executor;

public class DirectThumbnailLoader implements IThumbnailLoader {
    private IPath path_;
    private Executor executor_;
    private boolean fromExif_;
    private AsyncTask<IPath, IPath, Bitmap> task_;
    private Handler handler_ = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private Drawable thumbnail_;

    public DirectThumbnailLoader(IPath path, Executor executor, boolean fromExif) {
        path_ = path;
        fromExif_ = fromExif;
        executor_ = executor;
    }

    @Override
    public void loadThumbnail(final IThumbnailLoaderListener listener) {
        if(thumbnail_ != null) {
            listener.onFinished(thumbnail_, true);
        } else {
            if(task_ == null) {
                isRunning = true;
                task_ = new AsyncTask<IPath, IPath, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(IPath... params) {
                        if (isCancelled()) {
                            return null;
                        }

                        try {
                            if(fromExif_) {
                                return ImageLoader.loadExifThumbnail(new URL(params[0].getThumbnailUrl().url)).getBitmap();
                            } else {
                                return ImageLoader.load(new URL(params[0].getThumbnailUrl().url)).getBitmap();
                            }
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(final Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        if (bitmap != null) {
                            handler_.post(new Runnable() {
                                @Override
                                public void run() {
                                    thumbnail_ = new BitmapDrawable(bitmap);
                                    task_ = null;
                                    if(isRunning()) { // If not cancelled...
                                        listener.onFinished(thumbnail_, false);
                                        isRunning = false;
                                    }
                                }
                            });
                        }
                    }
                };
                task_.executeOnExecutor(executor_, path_);
            }
        }
    }

    @Override
    public void cancel() {
        if(task_ != null) {
            task_.cancel(true);
            task_ = null;
        }
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
