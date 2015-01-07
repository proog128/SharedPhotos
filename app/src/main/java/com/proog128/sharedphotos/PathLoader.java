package com.proog128.sharedphotos;

import android.content.Context;
import android.content.Loader;
import android.os.Handler;
import android.os.Looper;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IDeviceService;
import com.proog128.sharedphotos.filesystem.IDeviceServiceListener;
import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IListTask;
import com.proog128.sharedphotos.filesystem.IListTaskListener;
import com.proog128.sharedphotos.filesystem.IPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PathLoader extends Loader<LoaderResult> {
    private Handler handler_ = new Handler(Looper.getMainLooper());

    private Context applicationContext_;
    private IFilesystem fs_;
    private IPath path_;
    private boolean ignoreCollections_ = false;

    private PathObserver observer_;

    public PathLoader(Context context, IFilesystem fs, IPath path, boolean ignoreCollections) {
        super(context);

        applicationContext_ = context;
        fs_ = fs;
        path_ = path;
        ignoreCollections_ = ignoreCollections;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if(observer_ != null) {
            // Loader was stopped and is now restarted. Send all results.
            observer_.send();
        } else {
            // Loader was reset and is now started. Create a new observer to monitor devices.
            observer_ = new PathObserver(ignoreCollections_);
            observer_.start(fs_);

        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();

        if(observer_ != null) {
            observer_.send();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        super.onReset();

        if(observer_ != null) {
            observer_.stop();
            observer_ = null;
        }
    }

    private class PathObserver implements IDeviceServiceListener, IListTaskListener {
        private List<IPath> paths_ = new ArrayList<IPath>();
        private LoaderError error;
        private String errorText;

        private IDeviceService deviceService_;
        private IListTask listTask_;
        private Timer timeoutTimer_;

        private Object mutex_ = new Object();

        private boolean ignoreCollections_ = false;

        public PathObserver(boolean ignoreCollections) {
            ignoreCollections_ = ignoreCollections;
        }

        public void start(IFilesystem fs) {
            timeoutTimer_ = new Timer();
            timeoutTimer_.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (mutex_) {
                        error = LoaderError.Timeout;
                        errorText = "Could not open " + path_.toString() + " (Timeout).";

                        stop();
                    }

                    sendContentChanged();
                }
            }, 10000);

            IDevice device = fs.findDevice(path_.getDevice());

            synchronized (mutex_) {
                if(device == null) {
                    deviceService_ = fs.listDevices();
                    deviceService_.start(this);
                } else {
                    onDeviceAdded(device);
                }
            }
        }

        public void stop() {
            synchronized (mutex_) {
                if (listTask_ != null) {
                    listTask_.stop();
                    listTask_ = null;
                }
                if (deviceService_ != null) {
                    deviceService_.stop();
                    deviceService_ = null;
                }
            }
        }

        @Override
        public void onDeviceAdded(IDevice device) {
            if(device.getPath().equals(path_.getDevice())) {
                synchronized (mutex_) {
                    if (deviceService_ != null) {
                        deviceService_.stop();
                        deviceService_ = null;
                    }

                    if (listTask_ != null) {
                        listTask_.stop();
                    }

                    listTask_ = device.list(path_, 0);
                    listTask_.start(this);
                }
            }
        }

        @Override
        public void onDeviceRemoved(IDevice device) {
        }

        public void send() {
            synchronized (mutex_) {
                LoaderResult result = new LoaderResult(path_, new ArrayList<IPath>(paths_), error, errorText);
                deliverResult(result);
            }
        }

        private void sendContentChanged() {
            handler_.post(new Runnable() {
                @Override
                public void run() {
                    onContentChanged(); // causes onForceLoad if loader is running
                }
            });
        }

        @Override
        public void onProgressChanged(int progress) {
        }

        @Override
        public void onSuccess(IPath parent, IPath[] paths, int totalMatches) {
            synchronized (mutex_) {
                timeoutTimer_.cancel();
                for (IPath path : paths) {
                    if(ignoreCollections_ && path.isCollection())
                        continue;
                    paths_.add(path);
                }
                error = LoaderError.Success;
            }
            sendContentChanged();
        }

        @Override
        public void onError(final String msg) {
            synchronized (mutex_) {
                timeoutTimer_.cancel();
                error = LoaderError.GenericError;
                errorText = msg;
            }
            sendContentChanged();
        }
    }
}
