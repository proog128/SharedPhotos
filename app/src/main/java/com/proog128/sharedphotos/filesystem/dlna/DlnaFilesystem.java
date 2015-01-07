package com.proog128.sharedphotos.filesystem.dlna;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IDeviceService;
import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.filesystem.PathHasInvalidType;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;

public class DlnaFilesystem implements IFilesystem {
    private Registry registry_ = null;
    private ControlPoint controlPoint_ = null;

    public DlnaFilesystem(Registry registry, ControlPoint controlPoint) {
        registry_ = registry;
        controlPoint_ = controlPoint;
    }

    @Override
    public IDeviceService listDevices() {
        return new DeviceService(registry_, controlPoint_);
    }

    @Override
    public IDevice findDevice(IPath path) {
        if(!(path instanceof Path)) {
            throw new PathHasInvalidType(path);
        }

        Path devicePath = (Path) path.getDevice();

        Device device = registry_.getDevice(new UDN(devicePath.getDeviceUdn()), false);
        if(device == null) {
            return null;
        }

        Service service = DeviceService.findContentDirectory(device);
        return new DlnaDevice(service, controlPoint_);
    }

    @Override
    public IPath getRootPath() {
        return Path.getRoot();
    }
}
