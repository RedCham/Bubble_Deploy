package com.example.testtesttest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

public class SideImageAdapter extends RecyclerView.Adapter<SideImageAdapter.ViewHolder> {
    private int sideFolderSize = 10;
    private ArrayList<imageFolder> mData = null ;
    private Context mContext;
    // 아이템 뷰를 저장하는 뷰홀더 클래스.

    //
    private SparseBooleanArray mSelectedItems=new SparseBooleanArray(0);
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView) ;
            // 뷰 객체에 대한 참조. (hold strong reference)
            textView = itemView.findViewById(R.id.side_text);
            imageView = itemView.findViewById(R.id.sidebar_source) ;
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)
                    imageView.getLayoutParams();
            params.width = sideFolderSize;
            params.height = sideFolderSize;
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(2,2,2,2);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    SideImageAdapter(ArrayList<imageFolder> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public SideImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext() ;
        sideFolderSize = parent.getWidth();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.recyclerview_item_sidebar, parent, false) ;
        SideImageAdapter.ViewHolder vh = new SideImageAdapter.ViewHolder(view) ;
        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    // 클릭이벤트 여기야 여기

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(SideImageAdapter.ViewHolder holder, int position) {

        imageFolder folder = mData.get(position) ;
        File image = new File(folder.getFirstPic());
        String folderTitle = position==0?"All Folders":folder.getFolderName();
        holder.textView.setText(folderTitle);
        //onClickColor(holder,position);
        Glide.with(mContext)
                .load(image)
                .apply(new RequestOptions().centerCrop())
                .into(holder.imageView);
        final int t = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, String.format("%d 선택", t), Toast.LENGTH_SHORT).show();
                ((GalleryActivity)GalleryActivity.mContext).setFolderSelectState(t);
                ((GalleryActivity)GalleryActivity.mContext).setImageBitmapList();
               // toggleItemSelected(t);

            }
        });
    }

    private void onClickColor(SideImageAdapter.ViewHolder holder, int position){
        if ( mSelectedItems.get(position, false) ){
            holder.imageView.setColorFilter((R.color.white), PorterDuff.Mode.DARKEN);
        } else {
            holder.imageView.setColorFilter(null);
        }
    }

    private void toggleItemSelected(int position){
        if(mSelectedItems.get(position,false)==true){
            mSelectedItems.delete(position);
            notifyItemChanged(position);
        }else{
            mSelectedItems.put(position,true);
            notifyItemChanged(position);
        }
    }
    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }


}