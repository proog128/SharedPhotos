package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IListTaskListener;
import com.proog128.sharedphotos.filesystem.IPath;

public class CachedListTaskListener implements IListTaskListener {
    private IPath path_;
    private int maxResults_;
    private IListTaskListener listener_;
    private Cache cache_;

    public CachedListTaskListener(IPath path, int maxResults, IListTaskListener listener, Cache cache) {
        path_ = path;
        maxResults_ = maxResults;
        listener_ = listener;
        cache_ = cache;
    }

    @Override
    public void onProgressChanged(int progress) {
        listener_.onProgressChanged(progress);
    }

    @Override
    public void onSuccess(IPath parent, IPath[] paths, int totalMatches) {
        cache_.update(parent, maxResults_, paths, totalMatches);
        listener_.onSuccess(parent, paths, totalMatches);
    }

    @Override
    public void onError(String msg) {
        listener_.onError(msg);
    }
}
