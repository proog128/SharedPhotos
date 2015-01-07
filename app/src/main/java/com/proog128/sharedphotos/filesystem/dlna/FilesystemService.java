package com.proog128.sharedphotos.filesystem.dlna;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.proog128.sharedphotos.filesystem.IFilesystemService;
import com.proog128.sharedphotos.filesystem.IFilesystemServiceListener;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;

public class FilesystemService implements IFilesystemService, ServiceConnection {
    private Context applicationContext_;
    private Activity activity_;

    private AndroidUpnpService upnpService_ = null;

    private IFilesystemServiceListener listener_ = null;
    private Object listenerMutex_ = new Object();

    private DlnaFilesystem filesystem_ = null;

    public FilesystemService(Context applicationContext, Activity activity) {
        applicationContext_ = applicationContext;
        activity_ = activity;
    }

    @Override
    public void start(IFilesystemServiceListener listener) {
        assert(listener != null);

        listener_ = listener;
        if(!applicationContext_.bindService(new Intent(activity_, AndroidUpnpServiceImpl.class/*CustomUpnpServiceImpl.class*/),
                this, Context.BIND_AUTO_CREATE)) {
            throw new RuntimeException("bindService() failed.");
        }
    }

    @Override
    public void stop() {
        synchronized (listenerMutex_) {
            listener_ = null;
        }

        applicationContext_.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        upnpService_ = (AndroidUpnpService) service;

        filesystem_ = new DlnaFilesystem(upnpService_.getRegistry(), upnpService_.getControlPoint());
        upnpService_.getControlPoint().search();

        synchronized (listenerMutex_) {
            if(listener_ != null) {
                listener_.onInitialized(filesystem_);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        upnpService_ = null;

        synchronized (listenerMutex_) {
            assert (filesystem_ != null);

            if(listener_ != null) {
                listener_.onDestroyed(filesystem_);
            }
        }

        filesystem_ = null;
    }
}
