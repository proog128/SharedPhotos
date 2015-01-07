package com.proog128.sharedphotos.filesystem.dlna;

import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.filesystem.InvalidPath;
import com.proog128.sharedphotos.filesystem.PathCannotConcat;
import com.proog128.sharedphotos.filesystem.PathHasInvalidType;
import com.proog128.sharedphotos.filesystem.PathHasNoParentException;
import com.proog128.sharedphotos.filesystem.PathIsNotAFile;
import com.proog128.sharedphotos.filesystem.PathMustBeAbsolute;
import com.proog128.sharedphotos.filesystem.ThumbnailUrl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public final class Path implements IPath {

    interface IElement extends Serializable {
        public boolean equals(Object o);
        public int hashCode();
    }

    static class Element implements IElement {
        public String name;

        public Element(String name) {
            this.name = name;
        }

        public boolean equals(Object o) {
            if(this == o) return true;
            if((o != null) && (getClass() == o.getClass())) {
                Element oo = (Element)o;
                if(name.equals(oo.name)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

    static final class DeviceElement extends Element {
        public String udn;
        public ThumbnailUrl thumbnailUrl;

        public DeviceElement(String udn, String name, ThumbnailUrl thumbnailUrl) {
            super(name);
            this.udn = udn;
            this.thumbnailUrl = thumbnailUrl;
        }

        public boolean equals(Object o) {
            if(super.equals(o)) {
                DeviceElement oo = (DeviceElement)o;
                if(udn.equals(oo.udn) &&
                   thumbnailUrl.equals(thumbnailUrl)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return udn.hashCode();
        }
    }

    static final class CollectionElement extends Element {
        public String id;

        public CollectionElement(String id, String name) {
            super(name);
            this.id = id;
        }

        public boolean equals(Object o) {
            if(super.equals(o)) {
                CollectionElement oo = (CollectionElement)o;
                if(id.equals(oo.id)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return id.hashCode();
        }
    }

    static final class FileElement extends Element {
        public String id;
        public String name;
        public String contentUrl;
        public ThumbnailUrl thumbnailUrl;

        public FileElement(String id, String name, String contentUrl, ThumbnailUrl thumbnailUrl) {
            super(name);
            this.id = id;
            this.contentUrl = contentUrl;
            this.thumbnailUrl = thumbnailUrl;
        }

        public boolean equals(Object o) {
            if(super.equals(o)) {
                FileElement oo = (FileElement)o;
                if(id.equals(oo.id) &&
                   contentUrl.equals(oo.contentUrl) &&
                   thumbnailUrl.equals(oo.thumbnailUrl)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return id.hashCode();
        }
    }

    private LinkedList<IElement> elements_;

    private Path(LinkedList<IElement> elements) {
        elements_ = elements;
    }

    @Override
    public boolean isAbsolute() {
        return elements_.size() > 0 && elements_.getFirst() instanceof DeviceElement;
    }

    @Override
    public boolean isRelative() {
        return !isAbsolute();
    }

    @Override
    public boolean isFile() {
        return elements_.size() > 0 && elements_.getLast() instanceof FileElement;
    }

    public boolean isCollection() {
        return elements_.size() > 0 && elements_.getLast() instanceof CollectionElement;
    }

    @Override
    public boolean isDevice() {
        return elements_.size() == 1 && elements_.getLast() instanceof DeviceElement;
    }

    public String getCollectionId() {
        if(elements_.size() == 1) {
            return "0";
        } else {
            assert(isCollection());
            return ((CollectionElement) elements_.getLast()).id;
        }
    }

    @Override
    public IPath getParent() {
        if(elements_.size() > 0) {
            LinkedList<IElement> newList = new LinkedList<IElement>(elements_.subList(0, elements_.size() - 1));
            return new Path(newList);
        } else {
            throw new PathHasNoParentException(this);
        }
    }

    @Override
    public String getContentUrl() {
        if(elements_.size() == 0 || !(elements_.getLast() instanceof FileElement)) {
            throw new PathIsNotAFile(this);
        }
        return ((FileElement)elements_.getLast()).contentUrl;
    }

    @Override
    public ThumbnailUrl getThumbnailUrl() {
        if(elements_.size() == 0) throw new PathIsNotAFile(this);

        if(elements_.getLast() instanceof FileElement) {
            return ((FileElement)elements_.getLast()).thumbnailUrl;
        }
        if(elements_.getLast() instanceof DeviceElement) {
            return ((DeviceElement)elements_.getLast()).thumbnailUrl;
        }

        return null;
    }

    @Override
    public IPath concat(IPath other) {
        if(!(other instanceof Path)) {
            throw new PathHasInvalidType(other);
        }

        Path otherPath = (Path)other;
        if(otherPath.elements_.size() == 0) {
            return this;
        }
        if(elements_.size() == 0) {
            return otherPath;
        }

        if((elements_.getLast() instanceof FileElement) ||
           (otherPath.elements_.getFirst() instanceof DeviceElement)) {
            throw new PathCannotConcat(this, other);
        }

        LinkedList<IElement> elements = new LinkedList<IElement>();
        elements.addAll(elements_);
        elements.addAll(otherPath.elements_);
        return new Path(elements);
    }

    @Override
    public int getLength() {
        return elements_.size();
    }

    @Override
    public IPath getDevice() {
        if(!isAbsolute()) {
            throw new PathMustBeAbsolute(this);
        }
        LinkedList<IElement> elements = new LinkedList<IElement>();
        elements.add(elements_.getFirst());
        return new Path(elements);
    }

    @Override
    public IPath getFile() {
        if(!isFile()) {
            throw new PathIsNotAFile(this);
        }

        LinkedList<IElement> elements = new LinkedList<IElement>();
        elements.push(elements_.getLast());
        return new Path(elements);
    }

    @Override
    public String getLastElementName() {
        if(elements_.size() == 0) return "";

        Element e = (Element)elements_.getLast();
        return e.name;
    }

    public String getDeviceUdn() {
        if(elements_.size() == 0) {
            throw new InvalidPath(this);
        }

        DeviceElement de = (DeviceElement) elements_.getFirst();
        return de.udn;
    }

    public static Path getRoot() {
        return new Path(new LinkedList<IElement>());
    }

    public static Path fromService(String udn, String name, ThumbnailUrl thumbnailUrl) {
        LinkedList<IElement> elements = new LinkedList<IElement>();
        elements.add(new DeviceElement(udn, name, thumbnailUrl));
        return new Path(elements);
    }

    public static Path fromContainer(String id, String title) {
        LinkedList<IElement> elements = new LinkedList<IElement>();
        elements.add(new CollectionElement(id, title));
        return new Path(elements);
    }

    public static Path fromItem(String id, String title, String url, ThumbnailUrl thumbnailUrl) {
        LinkedList<IElement> elements = new LinkedList<IElement>();
        elements.add(new FileElement(id, title, url, thumbnailUrl));
        return new Path(elements);
    }

    public boolean equals(Object o) {
        if(this == o) return true;

        Path oo = (Path)o;
        if(elements_.size() == oo.elements_.size()) {
            Iterator<IElement> it = elements_.iterator();
            Iterator<IElement> oit = oo.elements_.iterator();

            while(it.hasNext() && oit.hasNext()) {
                IElement e = it.next();
                IElement oe = oit.next();
                if(!e.equals(oe)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int code = 0;
        for (IElement e : elements_) {
            code += e.hashCode();
        }
        return code;
    }

    public String toString() {
        if(elements_.size() == 0) return "";

        StringBuilder b = new StringBuilder();

        if(isAbsolute()) {
            b.append("/");
        }

        for(IElement ie : elements_) {
            Element e = (Element) ie;
            b.append(e.name);

            if(!(e instanceof FileElement)) {
                b.append("/");
            }
        }

        return b.toString();
    }
}
