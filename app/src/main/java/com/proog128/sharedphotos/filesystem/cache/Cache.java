package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IPath;

import java.util.HashMap;
import java.util.Iterator;

public class Cache {
    public class CacheEntry {
        public IPath[] paths;
        public int totalMatches;

        public CacheEntry(IPath[] paths, int totalMatches) {
            this.paths = paths;
            this.totalMatches = totalMatches;
        }
    }
    private class CacheKey {
        public IPath path;
        public int maxResults;

        public CacheKey(IPath path, int maxResults) {
            this.path = path;
            this.maxResults = maxResults;
        }

        public int hashCode() {
            return path.hashCode() + maxResults;
        }

        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof CacheKey)) return false;

            CacheKey oo = (CacheKey)o;
            return oo.path.equals(path) && oo.maxResults == maxResults;
        }
    }
    private HashMap<CacheKey, CacheEntry> cache_ = new HashMap<CacheKey, CacheEntry>();

    public void update(IPath parentPath, int maxResults, IPath[] paths, int totalMatches) {
        synchronized (cache_) {
            cache_.put(new CacheKey(parentPath, maxResults), new CacheEntry(paths, totalMatches));
        }
    }

    public CacheEntry get(IPath parentPath, int maxResults) {
        synchronized (cache_) {
            return cache_.get(new CacheKey(parentPath, maxResults));
        }
    }

    public void remove(IPath parentPath) {
        synchronized (cache_) {
            Iterator<CacheKey> it = cache_.keySet().iterator();
            while(it.hasNext()) {
                CacheKey key = it.next();
                if(key.path.equals(parentPath)) {
                    it.remove();
                }
            }
        }
    }
}
