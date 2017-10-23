package com.lenovo.smartShop.adapter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lenovo.smartShop.R;
import com.lenovo.smartShop.bean.AppListBean;
import com.lenovo.smartShop.model.DownLoadModel;
import com.lenovo.smartShop.utils.DataFormatConvert;
//import com.lenovo.smartShop.utils.DownLoadManager;
import com.lenovo.smartShop.utils.HttpUtils;
import com.lenovo.smartShop.utils.SCPackageManager;
import com.lenovo.smartShop.utils.StateMachine;
import com.lenovo.smartShop.view.DownLoadButton;
import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.interfaces.IDownloadProgress;
import com.yuan.leopardkit.download.DownLoadManager;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tmac on 17-10-13.
 */

public class ListViewAdapter extends BaseAdapter {

    public static final String TAG = "SC-ListAdapter";
    private Context context;
    private List<AppListBean.DataBean.DatalistBean> data;
    private List<DownLoadModel> modelList;
    public static final String FILEPATH = Environment.getExternalStorageDirectory() + "/SmartShop/";

    public ListViewAdapter(Context context, List<AppListBean.DataBean.DatalistBean> data, List<DownLoadModel> modelList) {
        this.context = context;
        this.data = data;
        this.modelList = modelList;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();

            holder.tv_item = convertView.findViewById(R.id.tv_item);
            holder.tv_item_download = convertView.findViewById(R.id.tv_item_download);
            holder.tv_item_size = convertView.findViewById(R.id.tv_item_size);
            holder.tv_item_desc = convertView.findViewById(R.id.tv_item_desc);
            holder.iv_item = convertView.findViewById(R.id.iv_item);
            holder.btn_item = convertView.findViewById(R.id.btn_item);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_item.setText(data.get(position).getName());
        holder.tv_item_download.setText(data.get(position).getDownloadCount());
        holder.tv_item_size.setText(DataFormatConvert.formatData(data.get(position).getSize()));
        holder.tv_item_desc.setText(data.get(position).getTypeName());
        Glide.with(context).load(data.get(position).getIconAddr()).diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.iv_item);

        final String packageName = data.get(position).getPackageName();
        final String mUrl = HttpUtils.commDetailUrl + "pn=" + packageName + "&vc=" + data.get(position).getVersioncode();
        final int downLoadState = StateMachine.getInstance().getDownLoadState(packageName);

        // 按钮状态
        switch (downLoadState) {
            case DownLoadButton.STATE_NORMAL:
                Log.d(TAG, "Normal");
                holder.btn_item.setState(packageName, DownLoadButton.STATE_NORMAL);
                break;
            case DownLoadButton.STATE_DOWNLOADING:
                Log.d(TAG, "Downloading");
                holder.btn_item.setState(packageName, DownLoadButton.STATE_DOWNLOADING);
                break;
            case DownLoadButton.STATE_INSTALL:
                Log.d(TAG, "Install");
                holder.btn_item.setState(packageName, DownLoadButton.STATE_INSTALL);
                break;
            case DownLoadButton.STATE_COMPLETE:
                Log.d(TAG, "Complete");
                holder.btn_item.setState(packageName, DownLoadButton.STATE_COMPLETE);
                break;
        }
        Log.d(TAG, "position = " + position);
        //final DownloadInfo info = modelList.get(position).getInfo();


        // 点击事件
        holder.btn_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downLoadState == DownLoadButton.STATE_NORMAL) {
                    //开始下载
                    holder.btn_item.setState(packageName, DownLoadButton.STATE_DOWNLOADING);
                    Log.d(TAG, "mContext = " + context);
                    com.lenovo.smartShop.utils.DownLoadManager.getInstance(context).appInfoRequest(mUrl, context, holder.btn_item, position, false);
                    //DownLoadManager.getManager().restartTask(info);
                } else if (downLoadState == DownLoadButton.STATE_COMPLETE) {
                    Log.d(TAG, "open application");
                    SCPackageManager.startActivityByPn(packageName);
                } else if (downLoadState == DownLoadButton.STATE_INSTALL) {
                    com.lenovo.smartShop.utils.DownLoadManager.getInstance(context).installApk(new File(SCPackageManager.PATH, packageName));
                } else if (downLoadState == DownLoadButton.STATE_WAITTING){

                }
            }
        });

        return convertView;
    }

    private class ViewHolder {
        TextView tv_item, tv_item_download, tv_item_size, tv_item_desc;
        ImageView iv_item;
        DownLoadButton btn_item;
    }
}
