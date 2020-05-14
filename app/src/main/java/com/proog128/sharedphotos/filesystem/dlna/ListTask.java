package com.proog128.sharedphotos.filesystem.dlna;

import com.proog128.sharedphotos.filesystem.IListTaskListener;
import com.proog128.sharedphotos.filesystem.IListTask;
import com.proog128.sharedphotos.filesystem.ThumbnailUrl;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.seamless.util.MimeType;

import java.util.ArrayList;

public class ListTask implements IListTask {
    private ControlPoint controlPoint_;
    private Service service_;
    private Path path_;
    private int maxResults_;
    private DlnaDevice device_;

    private IListTaskListener listener_ = null;
    private Object listenerMutex_ = new Object();

    public ListTask(ControlPoint controlPoint, Service service, Path path, int maxResults, DlnaDevice device) {
        controlPoint_ = controlPoint;
        service_ = service;
        path_ = path;
        maxResults_ = maxResults;
        device_ = device;
    }

    @Override
    public void start(final IListTaskListener listener) {
        listener_ = listener;

        listener_.onProgressChanged(0);

        controlPoint_.execute(new Browse(service_, path_.getCollectionId(), BrowseFlag.DIRECT_CHILDREN, "@childCount,dc:title,res", 0, (long)maxResults_, new SortCriterion(true, "dc:title")) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didlContent) {
                ArrayList<Path> newPaths = new ArrayList<Path>();

                for(Container container : didlContent.getContainers()) {
                    newPaths.add(Path.fromContainer(container.getId(), container.getTitle()));
                }
                for(Item item : didlContent.getItems()) {
                    if(item.getResources().size() < 1) {
                        continue;
                    }

                    String urlLargeImage = "";
                    ThumbnailUrl thumbnail = null;

                    // Search for a thumbnail
                    for(Res res : item.getResources()) {
                        if(res.getProtocolInfo().getAdditionalInfo().contains("JPEG_TN")) {
                            thumbnail = new ThumbnailUrl(res.getValue(), false);
                            break;
                        }
                    }

                    // Search for full-size image
                    for(Res res : item.getResources()) {
                        if(res.getProtocolInfo().getAdditionalInfo().contains("JPEG_LRG")) {
                            urlLargeImage = res.getValue();
                            break;
                        }
                    }

                    // If full-size image was not found by protocol info, fall back to the first
                    // supported resource
                    if(urlLargeImage.isEmpty()) {
                        for(Res res : item.getResources()) {
                            MimeType t = res.getProtocolInfo().getContentFormatMimeType();
                            if(t.getType().equals("image") && t.getSubtype().equals("jpeg")) {
                                urlLargeImage = item.getFirstResource().getValue();
                                break;
                            }
                        }
                    }

                    // If we still have no image, skip the item
                    if(urlLargeImage.isEmpty()) {
                        continue;
                    }

                    // If thumbnail is not found, fall back to full image and try to extract it
                    // from EXIF metadata.
                    if(thumbnail == null) {
                        thumbnail = new ThumbnailUrl(urlLargeImage, true);
                    }
                    newPaths.add(Path.fromItem(item.getId(), item.getTitle(), urlLargeImage, thumbnail));
                }

                int numberReturned = Integer.parseInt(actionInvocation.getOutput("NumberReturned").getValue().toString());
                int totalMatches = Integer.parseInt(actionInvocation.getOutput("TotalMatches").getValue().toString());

                if(numberReturned > 0 && totalMatches == 0) {
                    // Server cannot compute totalMatches --> return -1
                    totalMatches = -1;
                }

                synchronized (listenerMutex_) {
                    if(listener_ != null) {
                        listener_.onProgressChanged(100);
                        listener_.onSuccess(path_, newPaths.toArray(new Path[newPaths.size()]), totalMatches);
                    }
                }
            }

            @Override
            public void updateStatus(Status status) {
                if(status == Status.OK || status == Status.NO_CONTENT) {
                    synchronized (listenerMutex_) {
                        if(listener_ != null) {
                            listener_.onProgressChanged(100);
                        }
                    }
                }
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                synchronized (listenerMutex_) {
                    if(listener_ != null) {
                        listener_.onProgressChanged(100);
                        listener_.onError(s);
                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        synchronized (listenerMutex_) {
            listener_ = null;
        }
    }
}
