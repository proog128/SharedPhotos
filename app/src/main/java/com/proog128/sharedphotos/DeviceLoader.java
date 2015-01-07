package com.proog128.sharedphotos;

import android.content.Context;
import android.content.Loader;
import android.os.Handler;
import android.os.Looper;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IDeviceService;
import com.proog128.sharedphotos.filesystem.IDeviceServiceListener;
import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IPath;

import java.util.ArrayList;
import java.util.List;

public class DeviceLoader extends Loader<LoaderResult> {
    private Handler handler_ = new Handler(Looper.getMainLooper());

    private IFilesystem fs_;

    private DeviceObserver observer_;

    public DeviceLoader(Context context, IFilesystem fs) {
        super(context);

        fs_ = fs;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if(observer_ != null) {
            // Loader was stopped and is now restarted. Send all results collected so far.
            observer_.send();
        } else {
            // Loader was reset and is now started. Create a new observer to monitor devices.
            observer_ = new DeviceObserver();
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

    private class DeviceObserver implements IDeviceServiceListener {
        private List<IPath> devices_ = new ArrayList<IPath>();

        private IDeviceService deviceService_;

        public void start(IFilesystem fs) {
            deviceService_ = fs.listDevices();
            deviceService_.start(this);
        }

        public void stop() {
            deviceService_.stop();
        }

        @Override
        public void onDeviceAdded(IDevice device) {
            synchronized (devices_) {
                devices_.add(device.getPath());
            }
            sendContentChanged();
        }

        @Override
        public void onDeviceRemoved(IDevice device) {
            synchronized (devices_) {
                devices_.remove(device.getPath());
            }
            sendContentChanged();
        }

        public void send() {
            synchronized (devices_) {
                LoaderResult result = new LoaderResult(fs_.getRootPath(), new ArrayList<IPath>(devices_), LoaderError.Success, "");
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
    }
}
