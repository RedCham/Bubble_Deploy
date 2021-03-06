package com.luxand.bubble.mainActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.exifinterface.media.ExifInterface;

import com.luxand.FSDK;
import com.luxand.bubble.referenceClass.DBHelper;
import com.luxand.bubble.R;
import com.luxand.bubble.referenceClass.GalleryActivity;
import com.luxand.bubble.referenceClass.SingleMediaScanner;
import com.luxand.bubble.referenceClass.dateImage;
import com.luxand.bubble.referenceClass.imageFolder;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class BubblingFaceActivity extends GalleryActivity {
    AlertDialog.Builder builder;
    private DBHelper helper;
    private SQLiteDatabase db;

    private boolean mIsFailed = false;

    EditText input11;
    EditText input21;
    EditText input12;
    EditText input22;
    EditText input13;
    EditText input23;
    EditText name;
    EditText folder;
    LinearLayout dialogView;

    public void showErrorAndClose(String error, int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(error + ": " + code)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;    //this
        setContentView(R.layout.activity_bubblingface);
        super.onCreate(savedInstanceState);
        folderSelectState = getIntent().getIntExtra("folderState",0);

        int res = FSDK.ActivateLibrary("hoynDpHEai2hNQwtcWI8fVFuDotDyliNysu37ydK1Dd9iV7wNfpJizHURKq/q+LAkdv9zqYS48mD5+RmmPNX3njok2bW712DubyBU4lSP4twqFuVvnguUGbZwqjCNQlVg8S5FAXldxXhozC7LDahJBQnFxYo5MPwUcZwY9OcRvY=");
        if (res != FSDK.FSDKE_OK) {
            mIsFailed = true;
            showErrorAndClose("FaceSDK activation failed", res);
        } else {
            FSDK.Initialize();
        }

        makeDB();

        BottomBar bottomBar = (BottomBar)findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_photo);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_Home:
                        finish();
                        break;
                    case R.id.tab_photo:

                        break;
                    case R.id.tab_next:
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        break;
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            //가운데 아이템이 활성화 되어있는 상태고 다시 눌렀을 때 선택한거 다사라짐
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if(tabId==R.id.tab_photo){
                    gridAdapter.clearSelectedItem();
                }else if(tabId==R.id.tab_next){
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        createAndSetAdapter();
        dialogView = (LinearLayout) View.inflate(BubblingFaceActivity.this, R.layout.dialog_bubblingface, null);
        builder = new AlertDialog.Builder(BubblingFaceActivity.this);
        builder.setTitle("Face Detection and Make Folder");
        if (dialogView.getParent() != null)
            ((ViewGroup)dialogView.getParent()).removeView(dialogView);
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id) {
                input11 = dialogView.findViewById(R.id.face_inputText11);
                input21 = dialogView.findViewById(R.id.face_inputText21);
                input12 = dialogView.findViewById(R.id.face_inputText12);
                input22 = dialogView.findViewById(R.id.face_inputText22);
                input13 = dialogView.findViewById(R.id.face_inputText13);
                input23 = dialogView.findViewById(R.id.face_inputText23);
                name = dialogView.findViewById(R.id.face_nameText);
                folder = dialogView.findViewById(R.id.face_folderText);

                String n = name.getText().toString();
                if (n.equals("name"))
                    n = "";

                String inputDate1 = input11.getText().toString() + "/" + input12.getText().toString() + "/" + input13.getText().toString() + " 00:00:00";
                Log.e("original date1", inputDate1);
                String inputDate2 = input21.getText().toString() + "/" + input22.getText().toString() + "/" + input23.getText().toString() + " 23:59:59";
                Log.e("original date1", inputDate2);
                String fName = folder.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = sdf.parse(inputDate1);
                    date2 = sdf.parse(inputDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date1 != null && date2 != null) {
                    String millis1 = Long.toString(date1.getTime());
                    String millis2 = Long.toString(date2.getTime());
                    //String millis1 = "1484924400000";
                    //String millis2 = "1485183599000";
                    Log.e("millis1", millis1);
                    Log.e("millis2", millis2);

                    if (gridAdapter.mSelectedItems.size() != 1)
                        Toast.makeText(getApplicationContext(), "이미지를 하나만 선택해주세요.", Toast.LENGTH_SHORT).show();
                    else {
                        int j = gridAdapter.mSelectedItems.keyAt(0);
                        try {
                            imageFolder folds = matchFacesInFolder(n, imageBitmapList.get(j).getImageAbPate(), millis1, millis2, fName);
                            if (folds != null) {
                                imageFolderList.add(folds);
                                setFolderSelectState(imageFolderList.size() - 1);
                                setImageBitmapList();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "기간이 형식과 다릅니다", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((ViewGroup)dialogView.getParent()).removeView(dialogView);
                gridAdapter.clearSelectedItem();
            }
        });
    }

    public void makeDB(){
        // SQLiteOpenHelper 클래스의 subclass인 DBHelper 클래스 객체 생성
        helper = new DBHelper(this);
        // DBHelper 객체를 이용하여 DB 생성
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = helper.getReadableDatabase();
        }
    }

    public void insertDB(String name, Uri uri, String abPath, String date, String position){
        int config=0;
        db.execSQL("INSERT INTO contacts VALUES ('"+name+"','"+uri+"','"+abPath+"','"+date+"','"+position+"','"+config+"');");
    }

    public ArrayList<String> getPaths(String date1, String date2) {
        Cursor cursor;
        ArrayList<String> paths = new ArrayList<>();
        cursor = db.rawQuery("Select path from contacts WHERE date >= " + date1 + " AND date <= " + date2, null);

        while (cursor.moveToNext()) {
            paths.add(cursor.getString(0));
        }

        DateFormat format = new SimpleDateFormat();

        return paths;
    }

    private String getTagString(String tag, ExifInterface exif) {

        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }

    private String showExif(ExifInterface exif) {
        String myAttribute = "[Exif information] \n\n";
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);

        return myAttribute;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume(){
        super.onResume();
        BottomBar bottomBar = (BottomBar)findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_photo);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.execSQL("DELETE FROM contacts");
        helper.close();

    }

    public int matchSingleFace(String file1, String file2) {

		/*
		return code
		1  :same people
		0  :different people
		-1 :first file can't detect face
		-2 :second file can't detect face
		-3 :first file can't detect facial features
		-4 :second file can't detect facial features
		-5 :first file can't make face template
		-6 :second file can't make face template
		-7 :first file can't load
		-8 :second file can't load
		 */

        FSDK.SetFaceDetectionParameters(true, true, 128);
        FSDK.SetFaceDetectionThreshold(0);

        FSDK.HImage Image = new FSDK.HImage();
        FSDK.HImage Image2 = new FSDK.HImage();

        int s = FSDK.LoadImageFromFile(Image, file1);
        int s11 = FSDK.LoadImageFromFile(Image2, file2);

        Log.e("file to FSDK", Integer.toString(s));
        Log.e("file to FSDK2", Integer.toString(s11));
        if (s != 0) return -7;
        if ( s11 != 0) return -8;

        FSDK.HImage RotatedImage = new FSDK.HImage();
        FSDK.CreateEmptyImage(RotatedImage);

        FSDK.HImage RotatedImage2 = new FSDK.HImage();
        FSDK.CreateEmptyImage(RotatedImage2);

        FSDK.RotateImage90(Image, 1, RotatedImage);
        FSDK.RotateImage90(Image2, 1, RotatedImage2);

        FSDK.TFacePosition tf = new FSDK.TFacePosition();;

        int s4 = -1;

        for (int i = 0; i < 4; i++) {
            s4 = -7;
            //tf = new FSDK.TFacePosition();
            s4 = FSDK.DetectFace(RotatedImage, tf);
            Log.e("detect face",Integer.toString(s4));
            if (s4 == 0)
                break;
            else {
                FSDK.CopyImage(RotatedImage, Image);
                FSDK.RotateImage90(Image, -1, RotatedImage);
            }
        }

        if (s4 != 0) return -1;

        FSDK.TFacePosition tf2 = new FSDK.TFacePosition();

        int s8 = -1;

        for (int i = 0; i < 4; i++) {
            s8 = -7;
            //tf2 = new FSDK.TFacePosition();
            s8 = FSDK.DetectFace(RotatedImage2, tf2);
            Log.e("detect face2",Integer.toString(s8));
            if (s8 == 0)
                break;
            else {
                FSDK.CopyImage(RotatedImage2, Image2);
                FSDK.RotateImage90(Image2, -1, RotatedImage2);
            }
        }

        if (s8 != 0) return -2;

        FSDK.FSDK_Features eye = new FSDK.FSDK_Features();
        FSDK.FSDK_FaceTemplate template = new FSDK.FSDK_FaceTemplate();

        FSDK.FSDK_Features eye2 = new FSDK.FSDK_Features();
        FSDK.FSDK_FaceTemplate template2 = new FSDK.FSDK_FaceTemplate();

        int s6 = FSDK.DetectFacialFeaturesInRegion(RotatedImage, tf, eye);
        Log.e("eye detection",Integer.toString(s6));
        if (s6 != 0) return -3;
        int s61 = FSDK.DetectFacialFeaturesInRegion(RotatedImage2, tf2, eye2);
        Log.e("eye detection2",Integer.toString(s61));
        if (s61 != 0) return -4;
        int s5 = FSDK.GetFaceTemplateUsingFeatures(RotatedImage, eye, template);
        Log.e("face template",Integer.toString(s5));
        if (s5 != 0) return -5;
        int s51 = FSDK.GetFaceTemplateUsingFeatures(RotatedImage2, eye2, template2);
        Log.e("face template2",Integer.toString(s51));
        if (s51 != 0) return -6;

        float[] fw = new float[5];
        int s7 = FSDK.MatchFaces(template, template2, fw);

        FSDK.FreeImage(Image);
        FSDK.FreeImage(RotatedImage);
        FSDK.FreeImage(Image2);
        FSDK.FreeImage(RotatedImage2);

        Log.e("Similarity", String.valueOf(fw[0]) + " " + s7);
        if(fw[0] > 0.6) return 1;
        else return 0;
    }

    public int matchMultiFace(String file1, String file2) {

		/*
		return code
		1  :same people
		0  :different people
		-1 :first file can't detect face
		-2 :second file can't detect face
		-3 :first file can't detect facial features
		-4 :second file can't detect facial features even one
		-5 :first file can't make face template
		-6 :second file can't make face template even one
		-7 :first file can't load
		-8 :second file can't load
		 */

        FSDK.SetFaceDetectionParameters(true, true, 128);
        FSDK.SetFaceDetectionThreshold(0);

        FSDK.HImage Image = new FSDK.HImage();
        FSDK.HImage Image2 = new FSDK.HImage();

        int s = FSDK.LoadImageFromFile(Image, file1);
        int s11 = FSDK.LoadImageFromFile(Image2, file2);

        Log.e("file to FSDK", Integer.toString(s));
        Log.e("file to FSDK2", Integer.toString(s11));
        if (s != 0) return -7;
        if ( s11 != 0) return -8;

        FSDK.HImage RotatedImage = new FSDK.HImage();
        FSDK.CreateEmptyImage(RotatedImage);

        FSDK.HImage RotatedImage2 = new FSDK.HImage();
        FSDK.CreateEmptyImage(RotatedImage2);

        FSDK.RotateImage90(Image, 1, RotatedImage);
        FSDK.RotateImage90(Image2, 1, RotatedImage2);

        FSDK.TFacePosition tf = new FSDK.TFacePosition();;

        int s4 = -1;

        for (int i = 0; i < 4; i++) {
            s4 = -7;
            tf = new FSDK.TFacePosition();
            s4 = FSDK.DetectFace(RotatedImage, tf);
            Log.e("detect face",Integer.toString(s4));
            if (s4 == 0)
                break;
            else {
                FSDK.CopyImage(RotatedImage, Image);
                FSDK.RotateImage90(Image, -1, RotatedImage);
            }
        }

        if (s4 != 0) return -1;

        int s8 = -1;

        FSDK.TFaces faces = new FSDK.TFaces(5);

        for (int i = 0; i < 4; i++) {
            s8 = -7;
            faces = new FSDK.TFaces(5);
            s8 = FSDK.DetectMultipleFaces(RotatedImage2, faces);
            Log.e("detect faces",Integer.toString(s8));
            if (s8 == 0)
                break;
            else {
                FSDK.CopyImage(RotatedImage2, Image2);
                FSDK.RotateImage90(Image2, -1, RotatedImage2);
            }
        }

        if (s8 != 0) return -2;

        FSDK.FSDK_Features eye = new FSDK.FSDK_Features();
        FSDK.FSDK_FaceTemplate template = new FSDK.FSDK_FaceTemplate();

        FSDK.FSDK_Features eye2 = new FSDK.FSDK_Features();
        FSDK.FSDK_FaceTemplate template2 = new FSDK.FSDK_FaceTemplate();

        int s6 = FSDK.DetectFacialFeaturesInRegion(RotatedImage, tf, eye);
        Log.e("eye detection",Integer.toString(s6));
        if (s6 != 0) return -3;

        int s5 = FSDK.GetFaceTemplateUsingFeatures(RotatedImage, eye, template);
        Log.e("face template",Integer.toString(s5));
        if (s5 != 0) return -5;

        float[] fw = new float[5];
        float f = 0;
        fw[0] = 0;

        int s611 = 0;
        int s511 = 0;

        Log.e("how much faces", String.valueOf(faces.faces.length));

        for (int i = 0; i < faces.faces.length; i++) {
            int s61 = FSDK.DetectFacialFeaturesInRegion(RotatedImage2, faces.faces[i], eye2);
            Log.e("eye detection2",Integer.toString(s61));
            if (s61 == 0) s611++;

            int s51 = FSDK.GetFaceTemplateUsingFeatures(RotatedImage2, eye2, template2);
            Log.e("face template2",Integer.toString(s51));
            if (s51 == 0) s511++;

            int s7 = FSDK.MatchFaces(template, template2, fw);
            Log.e("Similarity", String.valueOf(fw[0]));
            if (fw[0] >= f) f = fw[0];
        }

        if (s611 == 0) return -4;
        if (s511 == 0) return -6;

        FSDK.FreeImage(Image);
        FSDK.FreeImage(RotatedImage);
        FSDK.FreeImage(Image2);
        FSDK.FreeImage(RotatedImage2);

        Log.e("Similarity", String.valueOf(f));
        if(f > 0.6) return 1;
        else return 0;
    }

    public imageFolder matchFacesInFolder(String name, String file1, String date1, String date2, String folder) throws IOException {
		/*
		return code
		1  :same people
		0  :different people
		-1 :first file can't detect face
		-2 :second file can't detect face
		-3 :first file can't detect facial features
		-4 :second file can't detect facial features even one
		-5 :first file can't make face template
		-6 :second file can't make face template even one
		-7 :first file can't load
		-8 :second file can't load
		 */

        imageFolder folds = new imageFolder();
        folds.setFolderName(folder);
        Cursor cursor;
        ArrayList<dateImage> images = new ArrayList<>();
        cursor = db.rawQuery("Select * from contacts WHERE date >= " + date1 + " AND date <= " + date2, null);

        while (cursor.moveToNext()) {
            Uri uri = Uri.parse(cursor.getString(1));
            Log.e("face uri", cursor.getString(1));
            Log.e("face date", cursor.getString(3));
            dateImage d = new dateImage(uri, cursor.getString(3), cursor.getString(0), cursor.getString(2));
            images.add(d);
        }

        FSDK.SetFaceDetectionParameters(true, true, 256);
        FSDK.SetFaceDetectionThreshold(1);

        FSDK.HImage Image = new FSDK.HImage();

        int s = FSDK.LoadImageFromFile(Image, file1);
        Log.e("file to FSDK", file1);
        if (s != 0) {
            Log.e("can't detect file", String.valueOf(s));
            return null;
        }

        FSDK.HImage RotatedImage = new FSDK.HImage();
        FSDK.CreateEmptyImage(RotatedImage);
        FSDK.RotateImage90(Image, 1, RotatedImage);

        int s4 = -1;

        FSDK.TFacePosition tf = new FSDK.TFacePosition();

        FSDK.FSDK_Features eye = new FSDK.FSDK_Features();
        FSDK.FSDK_FaceTemplate template = new FSDK.FSDK_FaceTemplate();

        for (int i = 0; i < 4; i++) {
            s4 = -7;
            tf = new FSDK.TFacePosition();
            s4 = FSDK.DetectFace(RotatedImage, tf);
            Log.e("detect face", Integer.toString(s4));
            if (s4 == 0)
                break;
            else {
                FSDK.CopyImage(RotatedImage, Image);
                FSDK.RotateImage90(Image, -1, RotatedImage);
            }
        }

        if (s4 != 0) {
            Log.e("can't detect face", file1);
            return null;
        }

        int s6 = FSDK.DetectFacialFeaturesInRegion(RotatedImage, tf, eye);
        Log.e("eye detection", Integer.toString(s6));
        if (s6 != 0) {
            Log.e("can't detect eye", file1);
            return null;
        }

        int s5 = FSDK.GetFaceTemplateUsingFeatures(RotatedImage, eye, template);
        Log.e("face template", Integer.toString(s5));
        if (s5 != 0) {
            Log.e("can't make template", file1);
            return null;
        }

        if (images.size() == 0) {
            Log.e("image zero", "0");
            Toast.makeText(getApplicationContext(), "기간내에 사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            return null;
        } else if (images.size() > 30) {
            Log.e("image over", "20");
            Toast.makeText(getApplicationContext(), "기간내에 너무 많은 사진이 존재합니다.", Toast.LENGTH_SHORT).show();
            return null;
        }

        for (int i = 0; i < images.size(); i++) {
            ExifInterface exif = new ExifInterface(images.get(i).getImageAbPate());
            String n = null;
            n = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);

            if (n!= null && n.contains(name)) {
                Log.e("TAG_IMAGE_DESCRIPTION", n);
                folds.addPics(images.get(i));
                continue;
            }
            FSDK.HImage Image2 = new FSDK.HImage();
            int s11 = FSDK.LoadImageFromFile(Image2, images.get(i).getImageAbPate());
            Log.e("file to FSDK2", images.get(i).getImageAbPate());

            if (s11 != 0) {
                Log.e("can't detect file", images.get(i).getImageName());
                continue;
            }

            FSDK.HImage RotatedImage2 = new FSDK.HImage();
            FSDK.CreateEmptyImage(RotatedImage2);

            FSDK.RotateImage90(Image2, 1, RotatedImage2);

            int s8 = -1;

            FSDK.TFaces faces = new FSDK.TFaces(5);

            for (int j = 0; j < 4; j++) {
                s8 = -7;
                faces = new FSDK.TFaces(5);
                s8 = FSDK.DetectMultipleFaces(RotatedImage2, faces);
                Log.e("detect faces", Integer.toString(s8));
                if (s8 == 0)
                    break;
                else {
                    FSDK.CopyImage(RotatedImage2, Image2);
                    FSDK.RotateImage90(Image2, -1, RotatedImage2);
                }
            }

            if (s8 != 0) {
                Log.e("can't detect face", images.get(i).getImageName());
                continue;
            }

            FSDK.FSDK_Features eye2 = new FSDK.FSDK_Features();
            FSDK.FSDK_FaceTemplate template2 = new FSDK.FSDK_FaceTemplate();

            float[] fw = new float[5];
            float f = 0;
            fw[0] = 0;

            int s611 = 0;
            int s511 = 0;
            Log.e("how much faces in " + i, String.valueOf(faces.faces.length));

            for (int j = 0; j < faces.faces.length; j++) {
                int s61 = FSDK.DetectFacialFeaturesInRegion(RotatedImage2, faces.faces[j], eye2);
                Log.e("eye detection2", Integer.toString(s61));
                if (s61 == 0) s611++;

                int s51 = FSDK.GetFaceTemplateUsingFeatures(RotatedImage2, eye2, template2);
                Log.e("face template2", Integer.toString(s51));
                if (s51 == 0) s511++;

                int s7 = FSDK.MatchFaces(template, template2, fw);
                Log.e("Similarity", String.valueOf(fw[0]));
                if (fw[0] >= f) f = fw[0];
            }
            if (s611 == 0) {
                Log.e("can't detect features", images.get(i).getImageName());
            }
            if (s511 == 0) {
                Log.e("can't detect template", images.get(i).getImageName());
            }

            Log.e("Similarity " + i, String.valueOf(f));
            if (f > 0.6) {
                folds.addPics(images.get(i));
                if (n == null || n.equals("null") || n.equals("")) {
                    Log.e("exif is null", "0");
                    exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, name);
                    exif.saveAttributes();
                    new SingleMediaScanner(mContext, images.get(i).getImageAbPate());
                }
                else if (!n.contains(name)) {
                    Log.e("exif tag", n);
                    exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, n + ", " + name);
                    exif.saveAttributes();
                    new SingleMediaScanner(mContext, images.get(i).getImageAbPate());
                }
            }
            FSDK.FreeImage(Image2);
            FSDK.FreeImage(RotatedImage2);

        }
        FSDK.FreeImage(Image);
        FSDK.FreeImage(RotatedImage);

        return folds;
    }

}