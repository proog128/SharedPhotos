package com.proog128.sharedphotos.filesystem;

public class PathCannotConcat extends RuntimeException {
    public PathCannotConcat(IPath path, IPath other) {
        super("Cannot concatenate paths '" + path.toString() + "' and '" + other.toString() + "'.");
    }
}
