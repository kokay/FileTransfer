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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.MenuItem;
import android.view.View;
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
    public static final int PHOTO_FRAGMENT = 0;
    public static final int VIDEO_FRAGMENT = 1;
    public static final int AUDIO_FRAGMENT = 2;
    public static final int DOCUMENT_FRAGMENT = 3;
    private final int[] selectedTabImages = {
            R.drawable.photo_selected,
            R.drawable.video_selected,
            R.drawable.audio_selected,
            R.drawable.document_selected,
    };
    private final int[] unselectedTabImages = {
            R.drawable.photo_unselected,
            R.drawable.video_unselected,
            R.drawable.audio_unselected,
            R.drawable.document_unselected,
    };
    private final String[] titles = {
            "Photo",
            "Video",
            "Audio",
            "Document"
    };

    private static LruCache<Long, Bitmap> memoryCache;
    public static List<FileInfo> fileList = new ArrayList<>();

    private List<Fragment> fileFragments = new ArrayList<>();

    private FloatingActionButton waitForDeviceConnectionButton;
    private Toolbar toolbar;
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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(titles[PHOTO_FRAGMENT]);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(PHOTO_FRAGMENT).setIcon(selectedTabImages[PHOTO_FRAGMENT]);
        tabLayout.getTabAt(VIDEO_FRAGMENT).setIcon(unselectedTabImages[VIDEO_FRAGMENT]);
        tabLayout.getTabAt(AUDIO_FRAGMENT).setIcon(unselectedTabImages[AUDIO_FRAGMENT]);
        tabLayout.getTabAt(DOCUMENT_FRAGMENT).setIcon(unselectedTabImages[DOCUMENT_FRAGMENT]);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setIcon(selectedTabImages[tab.getPosition()]);
                toolbar.setTitle(titles[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(unselectedTabImages[tab.getPosition()]);
                toolbar.setTitle(titles[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        waitForDeviceConnectionButton = (FloatingActionButton) findViewById(R.id.wait_connections_button);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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