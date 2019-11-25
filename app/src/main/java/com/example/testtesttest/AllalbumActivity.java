package com.example.testtesttest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AllalbumActivity extends AppCompatActivity {
    public static Context mContext;
    private RecyclerView gridImage;
    private RecyclerView sideBar;
    private ArrayList<String> imageBitmapList = new ArrayList<>();
    private ArrayList<imageFolder> imageFolderList = new ArrayList<>();
    private GridImageAdapter gridAdapter;
    private SideImageAdapter sideAdapter;
    private int folderSelectState;
    File loadPath;

    public void setFolderSelectState(int i) {folderSelectState = i;}

    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allalbum);

        folderSelectState = 1;


        BottomBar bottomBar = (BottomBar)findViewById(R.id.bottomBar);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_Home:
                        Toast.makeText(getApplicationContext(), Integer.toString(tabId), Toast.LENGTH_LONG).show();
                        break;
                    case R.id.tab_photo:
                        Toast.makeText(getApplicationContext(), Integer.toString(tabId + 1), Toast.LENGTH_LONG).show();
                        Intent in = new Intent(AllalbumActivity.this, BubblingPhotoActivity.class);
                        startActivity(in);
                        break;
                    case R.id.tab_folder:
                        Toast.makeText(getApplicationContext(), Integer.toString(tabId + 2), Toast.LENGTH_LONG).show();
                        Intent in2 = new Intent(AllalbumActivity.this, BubblingFolderActivity.class);
                        startActivity(in2);
                        break;
                }
            }
        });
        gridImage = findViewById(R.id.gridImage_recycler) ;
        gridImage.setHasFixedSize(true);
        gridImage.setLayoutManager(new GridLayoutManager(this, 3)) ;
        gridAdapter = new GridImageAdapter(imageBitmapList, this) ;

        gridImage.setAdapter(gridAdapter) ;

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        sideBar = findViewById(R.id.sidebar_recycler) ;
        sideBar.setLayoutManager(new LinearLayoutManager(this)) ;

        // 리사이클러뷰에 SideImageAdapter 객체 지정.
        sideAdapter = new SideImageAdapter(imageFolderList, this) ;
        sideBar.setAdapter(sideAdapter) ;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        setImageFolderList();
        setImageBitmapList();
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setImageFolderList() {
        imageFolderList.clear();
        imageFolderList.addAll(getPicturePaths());
        imageFolderList.add(0,imageFolderList.get(0));

        sideAdapter.notifyDataSetChanged();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setImageBitmapList(){
        imageBitmapList.clear();
        if (folderSelectState==0) {
            imageFolderList.subList(1,imageFolderList.size()).stream()
                    .forEach(i->{
                        File a = new File(i.getPath());
                        Arrays.asList(a.listFiles())
                                .stream()
                                .map(f->f.getAbsolutePath())
                                .forEach(p->imageBitmapList.add(p));
                    });
        }
        else {
            File target = new File(imageFolderList.get(folderSelectState).getPath());
            Arrays.asList(target.listFiles())
                    .stream()
                    .map(f->f.getAbsolutePath())
                    .forEach(p->imageBitmapList.add(p));
        }
        gridAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_accending_name) {

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //이름은 이렇게 바꿔요
            toolbar.setTitle("ss");
            return true;
        }
        else if (id == R.id.action_accending_date) {

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //이름은 이렇게 바꿔요
            toolbar.setTitle("ss");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



//////////////////-->폴더 검색
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ArrayList<imageFolder> getPicturePaths() {
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesUri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();

                do {
                    imageFolder folds = new imageFolder();
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    //String date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    //String folderpaths =  datapath.replace(name,"");
                    String folderPaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                    folderPaths = folderPaths + folder + "/";
                    if (!picPaths.contains(folderPaths)) {
                        picPaths.add(folderPaths);
                        folds.setPath(folderPaths);
                        folds.setFolderName(folder);
                        folds.setFirstPic(datapath);
                        folds.addPics();
                        picFolders.add(folds);
                    } else {
                        for (int i = 0; i < picFolders.size(); i++) {
                            if (picFolders.get(i).getPath().equals(folderPaths)) {
                                picFolders.get(i).setFirstPic(datapath);
                                picFolders.get(i).addPics();
                            }
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < picFolders.size(); i++) {
            Log.d("picture folders", picFolders.get(i).getFolderName() + " and path = " + picFolders.get(i).getFirstPic() + " " + picFolders.get(i).getNumberOfPics());
        }
        return picFolders;
    }
}