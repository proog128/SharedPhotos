package com.proog128.sharedphotos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.drew.metadata.iptc.IptcDirectory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class ImageLoader {
    public static class Image {
        private Bitmap bmp_;
        private Metadata metadata_;
        private String caption_ = "";

        private static int maxTextureSize_ = getMaxTextureSize();

        public Image(Bitmap bmp, Metadata metadata) {
            Matrix matrix = new Matrix();

            int size = Math.max(bmp.getWidth(), bmp.getHeight());
            float scale = Math.min(1.0f, (float) maxTextureSize_ / size);

            matrix.preScale(scale, scale);

            if(metadata != null) {
                matrix.preConcat(rotation(metadata));
            }

            if(matrix.isIdentity()) {
                bmp_ = bmp;
            } else {
                bmp_ = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
            }

            if(metadata != null) {
                metadata_ = metadata;

                Directory d = metadata_.getDirectory(IptcDirectory.class);
                if(d != null) {
                    caption_ = d.getString(IptcDirectory.TAG_CAPTION);
                }
            }
        }

        public Bitmap getBitmap() {
            return bmp_;
        }

        private static Matrix rotation(Metadata metadata) {
            ExifIFD0Directory d = metadata.getDirectory(ExifIFD0Directory.class);
            if(d == null) return new Matrix();

            int orientation = 0;
            try {
                orientation = d.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            } catch (MetadataException e) {
                orientation = 0;
            }

            Matrix m = new Matrix();
            switch(orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    m.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    m.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    m.setScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    m.setRotate(90);
                    m.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    m.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    m.setRotate(90);
                    m.postScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    m.setRotate(270);
                    break;
            }
            return m;
        }

        public String getCaption() {
            return caption_;
        }

        private static int getMaxTextureSize() {
            // Retrieve GL_MAX_TEXTURE_SIZE (maximum allowed size of bitmap in
            // hardware-accelerated ImageView) without GLSurfaceView
            // from http://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap

            final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            int[] version = new int[2];
            egl.eglInitialize(display, version);

            int[] totalConfigurations = new int[1];
            egl.eglGetConfigs(display, null, 0, totalConfigurations);

            EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
            egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

            int[] textureWidth = new int[1];
            int[] textureHeight = new int[1];
            int maximumTextureSize = 0;

            for (int i = 0; i < totalConfigurations[0]; i++) {
                egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureWidth);
                egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_HEIGHT, textureHeight);

                int textureSize = Math.min(textureWidth[0], textureHeight[0]);

                if (maximumTextureSize < textureSize)
                    maximumTextureSize = textureSize;
            }

            egl.eglTerminate(display);

            return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
        }
    }

    public static Image load(URL url) {
        HttpURLConnection connection = null;
        try {
            Bitmap bmp = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] tmp = new byte[2097152];
            while ((nRead = input.read(tmp, 0, tmp.length)) != -1) {
                buffer.write(tmp, 0, nRead);
            }
            connection.disconnect();

            byte[] data = buffer.toByteArray();
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));

            Metadata metadata = null;
            try {
                 metadata = ImageMetadataReader.readMetadata(bis);
            } catch(ImageProcessingException e) {
            }
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

            return new Image(bmp, metadata);
        } catch (Exception e) {
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Image loadExifThumbnail(URL url) {
        HttpURLConnection connection = null;
        try {
            Bitmap bmp = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            Metadata metadata = ImageMetadataReader.readMetadata(input);

            ExifThumbnailDirectory d = metadata.getDirectory(ExifThumbnailDirectory.class);
            if(d == null) {
                return null;
            }
            if(!d.hasThumbnailData()) {
                return null;
            }

            byte[] data = d.getThumbnailData();
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

            if(bmp == null) {
                return null;
            }

            return new Image(bmp, metadata);
        } catch (Exception e) {
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
