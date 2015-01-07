package com.proog128.sharedphotos.filesystem;

import java.io.Serializable;

public final class ThumbnailUrl implements Serializable {
    public String url;
    public boolean fromExif;

    public ThumbnailUrl(String url, boolean fromExif) {
        this.url = url;
        this.fromExif = fromExif;
    }

    public boolean equals(Object o) {
        if(this == o) return true;

        ThumbnailUrl oo = (ThumbnailUrl)o;
        return url.equals(oo.url) && fromExif == oo.fromExif;
    }

    public int hashCode() {
        return url.hashCode();
    }
}
