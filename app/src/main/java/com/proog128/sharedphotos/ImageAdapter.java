package com.proog128.sharedphotos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.thumbnailloader.IThumbnailLoader;
import com.proog128.sharedphotos.thumbnailloader.IThumbnailLoaderListener;
import com.proog128.sharedphotos.thumbnailloader.ThumbnailLoaderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageAdapter extends BaseAdapter {
    private Context context_;
    private ArrayList<Item> items_ = new ArrayList<Item>();

    private LayoutInflater inflater_;

    private static final Executor EXECUTOR = new ThreadPoolExecutor(3, 3, 1,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(128),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public ImageAdapter(Context context) {
        context_ = context;
        inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        items_.clear();
        notifyDataSetChanged();
    }

    public void addAll(Collection<IPath> paths, IPath parent, IFilesystem fs) {
        items_.ensureCapacity(paths.size());
        for(IPath p : paths) {
            IThumbnailLoader thumbnailLoader = ThumbnailLoaderFactory.make(parent.concat(p), EXECUTOR, context_.getResources(), fs);
            items_.add(new Item(p, thumbnailLoader));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items_.size();
    }

    @Override
    public IPath getItem(int position) {
        return items_.get(position).getPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThumbnailView image;
        TextView text;
        Item item = items_.get(position);
        if (convertView == null) {
            convertView = inflater_.inflate(R.layout.row_layout, null);
        }

        image = (ThumbnailView) convertView.findViewById(R.id.image);
        text = (TextView) convertView.findViewById(R.id.text);

        if(image.item_ != item) {
            image.lazySetItem(item);

            if (!item.getPath().isFile()) {
                text.setVisibility(View.VISIBLE);
                text.setText(item.getPath().getLastElementName());
            } else {
                text.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    public static class ThumbnailView extends ImageView implements IThumbnailLoaderListener {
        private Item item_;
        private static final int BACKGROUND_COLOR = Color.rgb(30, 30, 30);

        public ThumbnailView(Context context) {
            super(context);
            init(context);
        }

        public ThumbnailView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init(context);
        }

        private void init(Context context) {
            setLayoutParams(new GridView.LayoutParams(
                    (int) context.getResources().getDimension(R.dimen.width),
                    (int) context.getResources().getDimension(R.dimen.height)));
            setBackgroundColor(BACKGROUND_COLOR);
        }

        public void lazySetItem(Item item) {
            if(item_ != null) {
                item_.getThumbnailLoader().cancel();
            }

            item_ = item;

            setImageResource(android.R.color.transparent);
            setBackgroundColor(BACKGROUND_COLOR);

            if(item_ != null) {
                item_.getThumbnailLoader().loadThumbnail(this);
            }
        }

        @Override
        public void onFinished(Drawable drawable, boolean fromCache) {
            if(item_.getPath().isDevice()) {
                setScaleType(ScaleType.FIT_CENTER);
            } else {
                setScaleType(ScaleType.CENTER_CROP);
            }
            if(!fromCache) {
                TransitionDrawable tdImage =
                        new TransitionDrawable(new Drawable[]{
                                new ColorDrawable(Color.TRANSPARENT),
                                drawable
                        });
                TransitionDrawable tdBackground =
                        new TransitionDrawable(new Drawable[]{
                                new ColorDrawable(BACKGROUND_COLOR),
                                new ColorDrawable(Color.TRANSPARENT)
                        });
                tdImage.setCrossFadeEnabled(true);
                tdBackground.setCrossFadeEnabled(true);
                setImageDrawable(tdImage);
                setBackgroundDrawable(tdBackground);
                tdImage.startTransition(250);
                tdBackground.startTransition(250);

            } else {
                setBackgroundColor(Color.TRANSPARENT);
                setImageDrawable(drawable);
            }
        }
    }

    private static class Item {
        private IPath path_;
        private IThumbnailLoader thumbnailLoader_;

        public Item(IPath path, IThumbnailLoader thumbnailLoader) {
            path_ = path;
            thumbnailLoader_ = thumbnailLoader;
        }

        public IPath getPath() {
            return path_;
        }

        synchronized public IThumbnailLoader getThumbnailLoader() {
            return thumbnailLoader_;
        }
    }
}
