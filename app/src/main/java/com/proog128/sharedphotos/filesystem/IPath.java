package com.proog128.sharedphotos.filesystem;

import java.io.Serializable;

public interface IPath extends Serializable {
    ThumbnailUrl getThumbnailUrl();

    public boolean equals(Object o);
    public int hashCode();

    boolean isAbsolute();

    boolean isRelative();

    boolean isFile();

    boolean isCollection();

    boolean isDevice();

    public IPath getParent();

    public String getContentUrl();

    public IPath concat(IPath other);

    public int getLength();

    public IPath getDevice();

    public String getLastElementName();

    public IPath getFile();
}
