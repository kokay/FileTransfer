package com.kokayapp.filetransfer.SendFiles;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FileSelectionActivity extends AppCompatActivity {
    public static List<FileInfo> fileList = new ArrayList<>();
    private List<Fragment> fileFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selection);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentListPagerAdapter(getSupportFragmentManager(), fileFragments));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Button button = (Button) findViewById(R.id.send_file_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClientSelectionActivity.class);
                startActivity(intent);
            }
        });
    }

    public static class ThumbnailImageWorkerTask extends AsyncTask<Long, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private long id = -1;
        private int type = -1;
        private Context context;

        public ThumbnailImageWorkerTask(Context context, ImageView imageView, int type) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
            this.context = context;
            this.type = type;
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            id = params[0];

            switch (type) {
                case 0 :
                    return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                                        id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                case 1 :
                    return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                                        id, MediaStore.Video.Thumbnails.MINI_KIND, null);
                case 2 :
                    Cursor cursor = context.getContentResolver().query(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                            MediaStore.Audio.Albums._ID + " =? ",
                            new String[] {String.valueOf(id)}, null
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
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewWeakReference != null && bitmap != null) {
                final ImageView imageView = imageViewWeakReference.get();
                final ThumbnailImageWorkerTask loadThumnailTask = getThumbnailImageWorkerTask(imageView);
                if (this == loadThumnailTask && imageView != null) {
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
}