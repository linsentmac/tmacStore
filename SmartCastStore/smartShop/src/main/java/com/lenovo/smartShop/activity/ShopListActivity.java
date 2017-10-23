package com.lenovo.smartShop.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lenovo.smartShop.R;
import com.lenovo.smartShop.adapter.ListViewAdapter;
import com.lenovo.smartShop.adapter.SearchAdapter;
import com.lenovo.smartShop.bean.AppListBean;
import com.lenovo.smartShop.model.DownLoadModel;
import com.lenovo.smartShop.utils.HttpUtils;
import com.lenovo.smartShop.utils.MyApplication;
import com.lenovo.smartShop.utils.OkHttpClientUtil;
import com.lenovo.smartShop.utils.StateMachine;
import com.lenovo.smartShop.utils.StatusBarUtil;
import com.lenovo.smartShop.view.DownLoadButton;
import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class ShopListActivity extends Activity {

    private RelativeLayout serachLayout;
    public static ListView listView;
    private ArrayList<AppListBean.DataBean.DatalistBean> mListAll;
    private ArrayList<String> packageList;
    private ArrayList<DownLoadModel> modelList;
    private MyApplication myApplication;
    private ImageView disConn_Img;

    private SearchAdapter searchAdapter;
    private ListViewAdapter listViewAdapter;
    private final String TAG = "SC-ShopList";
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.adjustTranslentWindow(this);
        setContentView(R.layout.activity_shop_list);
        myApplication = (MyApplication) getApplication();
        initViews();
        initEvents();
        StatusBarUtil.setWhiteTranslucent(this);
        /**  因为是单例模式进行构建leopard请求，所以请求前需要先进行域名绑定 **/
        LeopardHttp.bindServer("http://wxwusy.applinzi.com");
        packageList = new ArrayList<>();
        mListAll = new ArrayList<>();
        modelList = new ArrayList<>();
        searchAdapter = new SearchAdapter(ShopListActivity.this, mListAll, R.layout.list_item, false);
        listViewAdapter = new ListViewAdapter(ShopListActivity.this, mListAll, modelList);
        listView.setAdapter(listViewAdapter);
    }

    private void initViews() {
        serachLayout = (RelativeLayout) findViewById(R.id.search_relaLay);
        listView = (ListView) findViewById(R.id.listView);
        disConn_Img = (ImageView) findViewById(R.id.disconnect_Img);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchAdapter.notifyDataSetChanged();
        getWifiState();
    }

    private void getWifiState(){
        ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if ( wifiNetInfo.isConnected()) {// unconnect network
            disConn_Img.setVisibility(View.GONE);
            dataRequest();
            Log.d(TAG, "isConnected");
        } else {
            disConn_Img.setVisibility(View.VISIBLE);
            Log.d(TAG, "isDisConnected");
        }
    }

    private void dataRequest(){
        if(!StateMachine.getInstance().isCurrentState(DownLoadButton.STATE_DOWNLOADING)){
            Log.d(TAG, "dataRequest");
            OkHttpClientUtil.getInstance()._getAsyn(HttpUtils.listUrl, new OkHttpClientUtil.ResultCallback<AppListBean>(){

                @Override
                public void onError(Request request, Exception e) {

                }

                @Override
                public void onResponse(AppListBean response) {
                    Log.d(TAG, "response = " + response.toString());
                    disConn_Img.setVisibility(View.GONE);
                    if(mListAll.size() != 0){
                        mListAll.clear();
                        packageList.clear();
                    }
                    for(int i = 0; i < response.getData().getAllcount(); i++){
                        mListAll.add(response.getData().getDatalist().get(i));
                        packageList.add(response.getData().getDatalist().get(i).getPackageName());
                    }
                    myApplication.setDataList(mListAll);
                    //searchAdapter.notifyDataSetChanged();
                    //initData();
                    List<DownloadInfo> downloadInfoList = DownLoadManager.getManager().getDownloadList(ShopListActivity.this);

                    listViewAdapter.notifyDataSetChanged();
                    Log.d(TAG, "firstLoad = " + firstLoad);
                    if(firstLoad){
                        StateMachine.getInstance().initState(packageList, ShopListActivity.this);
                        firstLoad = false;
                    }
                }
            });
        }
    }

    private void initData(){
        List<DownloadInfo> downloadInfoList = DownLoadManager.getManager().getDownloadList(ShopListActivity.this);

        for (DownloadInfo info:downloadInfoList){
            DownLoadModel model = new DownLoadModel();
            model.setInfo(info);
            modelList.add(model);
        }
        if(modelList.size() == 0){
            for(AppListBean.DataBean.DatalistBean datalistBean : mListAll){
                //final String url = HttpUtils.commDetailUrl + "pn=" + datalistBean.getPackageName() + "&vc=" + datalistBean.getVersioncode();
                String url = "http://f2.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
                DownloadInfo info = new DownloadInfo();
                info.setUrl(url);
                info.setFileSavePath(ListViewAdapter.FILEPATH);// 自定义下载路径
                info.setFileName(datalistBean.getPackageName());
                DownLoadModel model = new DownLoadModel();
                model.setInfo(info);
                modelList.add(model);
                Log.d(TAG, "name = " + datalistBean.getPackageName());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initEvents() {
        serachLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //myApplication.setDataList(mListAll);
                Intent intent = new Intent(ShopListActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                searchAdapter.notifyDataSetChanged();
                Intent intent = new Intent(ShopListActivity.this, AppDetailInfoActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        disConn_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataRequest();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory");
        StateMachine.getInstance().releaseState();
    }


    @Override
    public void onBackPressed() {
        /*if(StateMachine.getInstance().isCurrentState(DownLoadButton.STATE_DOWNLOADING)){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
            intent.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(intent);
        }else {
            Log.d(TAG, "onBackPressed");
            super.onBackPressed();
        }*/
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }
}
