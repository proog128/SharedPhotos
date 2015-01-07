package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IDeviceService;
import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IPath;

import java.util.HashMap;

public class CachedFilesystem implements IFilesystem {
    private Cache cache_;
    private IFilesystem fs_;

    private HashMap<IDeviceService, CachedDeviceService> cachedDeviceServices = new HashMap<IDeviceService, CachedDeviceService>();

    public CachedFilesystem(Cache cache, IFilesystem fs) {
        cache_ = cache;
        fs_ = fs;
    }

    @Override
    public IDeviceService listDevices() {
        IDeviceService ds = fs_.listDevices();
        CachedDeviceService cds = new CachedDeviceService(cache_, ds);
        cachedDeviceServices.put(ds, cds);
        return cds;
    }

    @Override
    public IDevice findDevice(IPath device) {
        IDevice d = fs_.findDevice(device);
        return d == null ? null : new CachedDevice(cache_, d);
    }

    @Override
    public IPath getRootPath() {
        return fs_.getRootPath();
    }
}
