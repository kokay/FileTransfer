package com.kokayapp.filetransfer.SendFiles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendFiles.Audio.AudioFragment;
import com.kokayapp.filetransfer.SendFiles.Document.DocumentFragment;
import com.kokayapp.filetransfer.SendFiles.Photo.PhotoFragment;
import com.kokayapp.filetransfer.SendFiles.Video.VideoFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.kokayapp.filetransfer.FileInfo.KILOBYTE;

public class FileSelectionActivity extends AppCompatActivity {
    private static LruCache<Long, Bitmap> memoryCache;
    public static List<FileInfo> fileList = new ArrayList<>();

    private List<Fragment> fileFragments = new ArrayList<>();

    private Button waitForDeviceConnectionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selection);

        fileFragments.add(PhotoFragment.newInstance());
        fileFragments.add(VideoFragment.newInstance());
        fileFragments.add(AudioFragment.newInstance());
        fileFragments.add(DocumentFragment.newInstance());

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentListPagerAdapter(getSupportFragmentManager(), fileFragments));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        waitForDeviceConnectionButton = (Button) findViewById(R.id.wait_connections_button);
        waitForDeviceConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClientSelectionActivity.class);
                startActivity(intent);
            }
        });

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / KILOBYTE);
        int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long id, Bitmap bitmap) {
                return bitmap.getByteCount() / (int) KILOBYTE;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        fileList.clear();
    }

    public void selectFile(FileInfo fileInfo) {
        if (fileList.contains(fileInfo)) fileList.remove(fileList.indexOf(fileInfo));
        else fileList.add(fileInfo);

        if (fileList.size() == 0) waitForDeviceConnectionButton.setVisibility(View.GONE);
        else waitForDeviceConnectionButton.setVisibility(View.VISIBLE);
    }

    public static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<ThumbnailImageWorkerTask> loadThumbnailTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, ThumbnailImageWorkerTask task) {
            super(res, bitmap);
            loadThumbnailTaskReference = new WeakReference<ThumbnailImageWorkerTask>(task);
        }

        public ThumbnailImageWorkerTask getThumbnailImageWorkerTask() {
            return loadThumbnailTaskReference.get();
        }
    }

    public static class ThumbnailImageWorkerTask extends AsyncTask<Long, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewWeakReference;
        private long id = -1;
        private int type = -1;
        private Context context;
        private ContentResolver contentResolver;

        public ThumbnailImageWorkerTask(Context context, ImageView imageView, int type) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
            this.context = context;
            this.contentResolver = context.getContentResolver();
            this.type = type;

        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            id = params[0];

            switch (type) {
                case 0:
                    return MediaStore.Images.Thumbnails.getThumbnail(contentResolver,
                            id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
                case 1:
                    return MediaStore.Video.Thumbnails.getThumbnail(contentResolver,
                            id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
                case 2:
                    Cursor cursor = contentResolver.query(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                            MediaStore.Audio.Albums._ID + " =? ",
                            new String[]{String.valueOf(id)}, null
                    );
                    if (cursor.moveToFirst()) {
                        return BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) bitmap = null;

            if (imageViewWeakReference != null && bitmap != null) {
                final ImageView imageView = imageViewWeakReference.get();
                final ThumbnailImageWorkerTask loadThumbnailTask = getThumbnailImageWorkerTask(imageView);
                if (this == loadThumbnailTask && imageView != null) {
                    addBitmapToMemoryCache(id, bitmap);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public static boolean cancelPotentialWork(ImageView imageView, long id) {
        final ThumbnailImageWorkerTask loadThumbnailTask = getThumbnailImageWorkerTask(imageView);

        if (loadThumbnailTask != null) {
            final long imageId = loadThumbnailTask.id;
            if (imageId == - 1|| imageId != id) {
                loadThumbnailTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }


    public static ThumbnailImageWorkerTask getThumbnailImageWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getThumbnailImageWorkerTask();
            }
        }
        return null;
    }

    public static void addBitmapToMemoryCache(long id, Bitmap bitmap) {
        if (getBitmapFromCache(id) == null) {
            memoryCache.put(id, bitmap);
        }
    }

    public static Bitmap getBitmapFromCache(long id) {
        return memoryCache.get(id);
    }
}