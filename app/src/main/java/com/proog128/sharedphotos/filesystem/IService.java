package com.proog128.sharedphotos.filesystem;

public interface IService<L> extends IStoppable {
    public void start(L l);
}
