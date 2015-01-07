package com.proog128.sharedphotos.filesystem;

public interface IListTaskListener {
    public void onProgressChanged(int progress);
    public void onSuccess(IPath parent, IPath path[], int totalMatches);
    public void onError(String msg);
}
