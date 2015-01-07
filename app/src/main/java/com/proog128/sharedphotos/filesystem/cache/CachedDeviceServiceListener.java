package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IDeviceServiceListener;

import java.util.HashMap;

public class CachedDeviceServiceListener implements IDeviceServiceListener {
    private Cache cache_;
    private IDeviceServiceListener listener_;

    private HashMap<IDevice, CachedDevice> cachedDevices_ = new HashMap<IDevice, CachedDevice>();

    public CachedDeviceServiceListener(Cache cache, IDeviceServiceListener listener) {
        cache_ = cache;
        listener_ = listener;
    }

    @Override
    public void onDeviceAdded(IDevice d) {
        CachedDevice cd = new CachedDevice(cache_, d);
        cachedDevices_.put(d, cd);
        listener_.onDeviceAdded(cd);
    }

    @Override
    public void onDeviceRemoved(IDevice d) {
        CachedDevice cd = cachedDevices_.get(d);
        listener_.onDeviceRemoved(cd);
        cachedDevices_.remove(cd);
    }
}
