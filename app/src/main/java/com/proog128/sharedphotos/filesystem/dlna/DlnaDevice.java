package com.proog128.sharedphotos.filesystem.dlna;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IListTask;
import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.filesystem.InvalidPath;
import com.proog128.sharedphotos.filesystem.ThumbnailUrl;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;

import java.util.Arrays;
import java.util.Comparator;

public class DlnaDevice implements IDevice {
    private Service service_;
    private ControlPoint controlPoint_;

    private Path devicePath_;

    public DlnaDevice(Service service, ControlPoint controlPoint) {
        service_ = service;
        controlPoint_ = controlPoint;

        String udn = service.getDevice().getIdentity().getUdn().getIdentifierString();
        String name = service.getDevice().getDetails().getFriendlyName();
        String thumbnailUrl = "";

        if(service.getDevice() instanceof RemoteDevice) {
            RemoteDevice device = (RemoteDevice) service.getDevice();
            Icon[] icons = service.getDevice().getIcons();
            Arrays.sort(icons, new Comparator<Icon>() {
                @Override
                public int compare(Icon lhs, Icon rhs) {
                    int isPng0 = lhs.getMimeType().getSubtype().equals("png") ? 1 : 0;
                    int isPng1 = rhs.getMimeType().getSubtype().equals("png") ? 1 : 0;
                    int s0 = lhs.getWidth() * lhs.getHeight() * lhs.getDepth() * 10 + isPng0;
                    int s1 = rhs.getWidth() * rhs.getHeight() * rhs.getDepth() * 10 + isPng1;
                    return ((Integer) s0).compareTo(s1);
                }
            });
            if (icons.length > 0) {
                Icon ico = icons[icons.length - 1];
                thumbnailUrl = device.normalizeURI(ico.getUri()).toString();
            }
        }

        devicePath_ = Path.fromService(udn, name, new ThumbnailUrl(thumbnailUrl, false));
    }

    @Override
    public IListTask list(IPath path, int maxResults) {
        if(!path.getDevice().equals(devicePath_)) {
            throw new InvalidPath(path);
        }
        return new ListTask(controlPoint_, service_, (Path)path, maxResults, this);
    }

    @Override
    public IPath getPath() {
        return devicePath_;
    }
}
