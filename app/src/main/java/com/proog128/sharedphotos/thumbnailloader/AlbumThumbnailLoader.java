package com.proog128.sharedphotos.thumbnailloader;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.proog128.sharedphotos.R;
import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IListTask;
import com.proog128.sharedphotos.filesystem.IListTaskListener;
import com.proog128.sharedphotos.filesystem.IPath;

import java.util.concurrent.Executor;

public class AlbumThumbnailLoader implements IThumbnailLoader {
    private Drawable defaultIcon_;
    private IFilesystem fs_;
    private IPath path_;
    private Executor executor_;
    private Resources resources_;
    private boolean isRunning_ = false;

    private IListTask task_;
    private IThumbnailLoader innerTask_;
    private Handler handler_ = new Handler(Looper.getMainLooper());

    private Drawable thumbnail_;

    public AlbumThumbnailLoader(IPath path, Executor executor, Resources resources, IFilesystem fs) {
        defaultIcon_ = resources.getDrawable(R.drawable.ic_album);
        fs_ = fs;
        path_ = path;
        resources_ = resources;
        executor_ = executor;
    }

    @Override
    public void loadThumbnail(final IThumbnailLoaderListener listener) {
        if(thumbnail_ != null) {
            listener.onFinished(thumbnail_, true);
        } else {
            if(task_ == null) {
                isRunning_ = true;

                IDevice device = fs_.findDevice(path_.getDevice());
                if (device == null) {
                    listener.onFinished(defaultIcon_, true);
                    return;
                }

                task_ = device.list(path_, 1);
                task_.start(new IListTaskListener() {
                    @Override
                    public void onProgressChanged(int progress) {

                    }

                    @Override
                    public void onSuccess(IPath parent, IPath[] path, int totalMatches) {
                        if(path.length == 0) {
                            notifyDefaultIcon();
                        } else {
                            if(isRunning()) { // If not cancelled...
                                innerTask_ = ThumbnailLoaderFactory.make(parent.concat(path[0]), executor_, resources_, fs_);
                                innerTask_.loadThumbnail(new IThumbnailLoaderListener() {
                                    @Override
                                    public void onFinished(Drawable drawable, boolean fromCache) {
                                        task_ = null;
                                        innerTask_ = null;
                                        thumbnail_ = drawable;
                                        if(isRunning()) { // If not cancelled...
                                            listener.onFinished(thumbnail_, fromCache);
                                            isRunning_ = false;
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        notifyDefaultIcon();
                    }

                    public void notifyDefaultIcon() {
                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                thumbnail_ = defaultIcon_;
                                task_ = null;
                                if(isRunning()) { // If not cancelled...
                                    listener.onFinished(thumbnail_, false);
                                    isRunning_ = false;
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void cancel() {
        if(innerTask_ != null) {
            innerTask_.cancel();
            innerTask_ = null;
        }
        if(task_ != null) {
            task_.stop();
            task_ = null;
        }
        isRunning_ = false;
    }

    public boolean isRunning() {
        return isRunning_;
    }
}
