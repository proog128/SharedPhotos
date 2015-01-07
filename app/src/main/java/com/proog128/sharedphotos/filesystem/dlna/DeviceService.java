package com.proog128.sharedphotos.filesystem.dlna;

import com.proog128.sharedphotos.filesystem.IDeviceService;
import com.proog128.sharedphotos.filesystem.IDeviceServiceListener;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DeviceService extends DefaultRegistryListener implements IDeviceService {
    private Registry registry_ = null;
    private ControlPoint controlPoint_ = null;

    private IDeviceServiceListener listener_ = null;
    private Object listenerMutex_ = new Object();

    private Map<Service, DlnaDevice> dlnaDevices_ = new HashMap<Service, DlnaDevice>();

    public DeviceService(Registry registry, ControlPoint controlPoint) {
        registry_ = registry;
        controlPoint_ = controlPoint;
    }

    @Override
    public void start(IDeviceServiceListener listener) {
        listener_ = listener;
        registry_.addListener(this);

        Collection<Device> devices = registry_.getDevices();
        for(Device device : devices) {
            Service contentDirectory = findContentDirectory(device);

            if(contentDirectory != null) {
                addDlnaDevice(contentDirectory);
            }
        }
    }

    @Override
    public void stop() {
        registry_.removeListener(this);

        synchronized (listenerMutex_) {
            listener_ = null;
        }
    }

    @Override
    public void deviceAdded(Registry registry, Device device) {
        Service contentDirectory = findContentDirectory(device);
        if(contentDirectory != null) {
            addDlnaDevice(contentDirectory);
        }
    }

    @Override
    public void deviceRemoved(Registry registry, Device device) {
        Service contentDirectory = findContentDirectory(device);
        if(contentDirectory != null) {
            removeDlnaDevice(contentDirectory);
        }
    }

    public static Service findContentDirectory(Device device) {
        Service[] services = device.findServices();
        for(Service service : services) {
            if(service.getServiceType().toFriendlyString().equals("schemas-upnp-org:ContentDirectory:1")) {
                return service;
            }
        }
        return null;
    }

    private synchronized void addDlnaDevice(Service s) {
        if(!dlnaDevices_.containsKey(s)) {
            dlnaDevices_.put(s, new DlnaDevice(s, controlPoint_));
            synchronized (listenerMutex_) {
                if(listener_ != null) {
                    listener_.onDeviceAdded(dlnaDevices_.get(s));
                }
            }
        }
    }

    private synchronized void removeDlnaDevice(Service s) {
        DlnaDevice d = dlnaDevices_.get(s);
        dlnaDevices_.remove(d);

        synchronized (listenerMutex_) {
            if(listenerMutex_ != null) {
                listener_.onDeviceRemoved(d);
            }
        }
    }
}
