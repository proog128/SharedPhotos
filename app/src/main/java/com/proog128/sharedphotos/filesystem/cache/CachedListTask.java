package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IListTask;
import com.proog128.sharedphotos.filesystem.IListTaskListener;
import com.proog128.sharedphotos.filesystem.IPath;

public class CachedListTask implements IListTask {
    private IPath path_;
    private int maxResults_;
    private IListTask listTask_;
    private Cache cache_;

    public CachedListTask(IPath path, int maxResults, IListTask listTask, Cache cache) {
        path_ = path;
        maxResults_ = maxResults;
        listTask_ = listTask;
        cache_ = cache;
    }

    @Override
    public void start(IListTaskListener listener) {
        listener.onProgressChanged(0);

        Cache.CacheEntry e = cache_.get(path_, maxResults_);
        if(e == null) {
            listTask_.start(new CachedListTaskListener(path_, maxResults_, listener, cache_));
        } else {
            listener.onSuccess(path_, e.paths, e.totalMatches);
            listener.onProgressChanged(100);
        }
    }

    @Override
    public void stop() {
        listTask_.stop();
    }
}
