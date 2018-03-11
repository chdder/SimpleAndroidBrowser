package comtivity.example.simpleactivity;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.DialogPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //滑动菜单
    private DrawerLayout mDrawerLayout;
    //WebView控件
    private WebView webView;
    //搜索按钮
    private Button Search;
    //地址栏
    private AutoCompleteTextView url;
    //WebView进度条
    private ProgressBar webViewProgress;
    //地址栏联想的适配器
    private ArrayAdapter<String> adapter;
    //存储访问历史记录
    private SharedPreferences pref;
    //负责写入历史记录
    private SharedPreferences.Editor editor;
    //负责存储收藏的网页链接
    private SharedPreferences collectUrl;
    //负责写入收藏记录
    private SharedPreferences.Editor collectEditor;
    //返回上一页
    private Button forwButton;
    //前往下一页
    private Button nextButton;
    //退出
    private Button exitButton;
    //收藏
    private Button collectButton;
    //超时时间
    private long timeout = 6000;
    //超时处理
    private Handler mHandler;
    private Timer timer;
    //从收藏夹获取收藏的链接
    private String getString=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent_from_clooect =getIntent();
        getString = intent_from_clooect.getStringExtra("collect_URL");

        //超时处理
        mHandler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        Toast.makeText(MainActivity.this,"连接超时，请尝试刷新",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
        //Toolbar的使用
        setContentView(R.layout.brower_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //滑动菜单
        mDrawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_list_white_24dp);
        }
        //滑动菜单具体内容
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        //设置菜单的点击事件
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(MenuItem item){
                switch (item.getItemId()){
                    case R.id.nav_collection:
                        Intent intent_one = new Intent(MainActivity.this,CollectActivity.class);
                        startActivity(intent_one);
                        break;
                    case R.id.dele_all_history:
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("提示");
                        dialog.setMessage("是否要清除所有浏览记录？");
                        dialog.setCancelable(true);
                        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editor.clear();
                                editor.commit();
                                Toast.makeText(MainActivity.this,"已清除所有浏览历史",Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.setNegativeButton("否", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog,int which){
                            }
                        });
                        dialog.show();

                        break;
                    case R.id.send_mail:
                        Intent intent_two = new Intent(MainActivity.this,MailActivity.class);
                        startActivity(intent_two);
                        break;
                    case R.id.search_test:
                        Intent intent_three = new Intent(MainActivity.this,SearchActivity.class);
                        startActivity(intent_three);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //地址输入栏
        url = (AutoCompleteTextView ) findViewById(R.id.et_url);

        //地址栏自动联想
        editor = getSharedPreferences("history",MODE_PRIVATE).edit();
        editor.putString("name","https://www.baidu.com");
        editor.apply();
        //设置适配器
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getHistory());
        //注意设置适配器
        url.setAdapter(adapter);

        //WebView进度条
        webViewProgress = (ProgressBar) findViewById(R.id.progressBar_web);

        //Webview的使用
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        //默认显示页面
        if(getString!=null) {
            webView.loadUrl(getString);
        }
        else{
            webView.loadUrl("https://www.baidu.com/");
        }
        //设置页面自适应大小
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        //设置WebView进度条显示
        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int newProgress) {
                url.setText(webView.getUrl().toString());
                if(newProgress==100){
                    webViewProgress.setVisibility(View.GONE);//加载完网页进度条消失
                }
                else{
                    webViewProgress.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    webViewProgress.setProgress(newProgress);//设置进度值
                }
            }
        });
        //设置下载监听
        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                final String u=url+"/"+contentDisposition.split("=")[1];
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("选择下载方式")//以列表方式显示alertdialog
                        .setItems(R.array.items_irdc_dialog, new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface arg0, int whichcountry) {
                                CharSequence strDialogMsg = getString(R.string.mychoose);
                                //从资源文件中得到选项数组
                                final String[] aryShop = getResources().getStringArray(R.array.items_irdc_dialog);
                                final String chosen = aryShop[whichcountry];
                                //创建一个alertDialog
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle(strDialogMsg)
                                        .setMessage(aryShop[whichcountry])
                                        .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                if(chosen.equals("单线程断点续传(建议选项)")){
                                                    Intent intent_threee = new Intent(MainActivity.this,DownloadTwoActivity.class);
                                                    intent_threee.putExtra("DownloadURL",u.toString());
                                                    startActivity(intent_threee);
                                                }
                                                else if(chosen.equals("多线程断点续传")){
                                                    Intent intent_threee = new Intent(MainActivity.this,DownloadActivity.class);
                                                    intent_threee.putExtra("DownloadURL",u.toString());
                                                    startActivity(intent_threee);
                                                }
                                            }
                                        }).show();
                            }
                        }).setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface d, int arg1) {
                        d.dismiss(); //关闭取得焦点的对话框
                    }
                }).show();
            }
        });

        //开启 Application Caches 功能
        webView.getSettings().setAppCacheEnabled(true);

        //优先使用缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //设置支持缩放
        webView.getSettings().setBuiltInZoomControls(true);

        //设置连接超时
        webView.setWebViewClient(new WebViewClient() {
             //创建一个WebViewClient,重写onPageStarted和onPageFinished
             //onPageStarted中启动一个计时器,到达设置时间后判断进度条
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                timer = new Timer();
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                            //切换回主线程进行提醒
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(webView.getProgress()<100) {
                                        Toast.makeText(MainActivity.this, "连接超时，请尝试刷新", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        timer.cancel();
                        timer.purge();
                    }
                };
                timer.schedule(tt, timeout);
            }
             //onPageFinished指页面加载完成,完成后取消计时器
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                timer.cancel();
                timer.purge();
            }
        });
        //WebView的按钮
        forwButton = (Button) findViewById(R.id.go_for);
        forwButton.setOnClickListener(this);
        nextButton = (Button) findViewById(R.id.go_next);
        nextButton.setOnClickListener(this);
        exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(this);
        collectButton = (Button) findViewById(R.id.collect);
        collectButton.setOnClickListener(this);

        //搜索按钮
        Search = (Button) findViewById(R.id.bt_search);
        //添加监听事件
        Search.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String url_name = url.getText().toString();
                if(judgeSame(getHistory(),url_name)) {
                    //在一次使用不退出的情况下，可以直接用add增加访问历史
                    adapter.add(url_name);
                    //如果退出再进入，历史会消失，所以要写入存储历史的文件之中，这样历史会永久保存
                    //获取历史记录的数量，防止键值重复
                    editor.putString(getHistory().length + 1 + "", url_name);
                    editor.apply();
                }
                if((!url_name.startsWith("https://")) && (!url_name.startsWith("http://")))
                    webView.loadUrl("http://"+url.getText().toString());
                else
                    webView.loadUrl(url_name);
            }

        });

        //收藏网页
        collectEditor = getSharedPreferences("collection",MODE_PRIVATE).edit();

        //悬浮刷新按钮
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                webView.loadUrl(webView.getUrl().toString());
                Toast.makeText(MainActivity.this,"正在刷新...",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //给toolbar的菜单设置点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //滑动菜单选项
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    //获取历史记录转换为字符串数组
    private String[] getHistory(){
        pref = getSharedPreferences("history",MODE_PRIVATE);
        Map<String,?> map = pref.getAll();
        StringBuilder temp = new StringBuilder();

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            temp.append(entry.getValue()+" ");
        }
        return temp.toString().split(" ");
    }

    //获取收藏记录转换为字符串数组
    private String[] getCollection(){
        collectUrl = getSharedPreferences("collection",MODE_PRIVATE);
        Map<String,?> map = collectUrl.getAll();
        String ssr[] = new String[map.size()];
        int i=0;
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            ssr[i]=entry.getValue().toString();
            i++;
        }
        return ssr;
    }

    //判断是否有相同的记录
    private boolean judgeSame(String[] str,String subString){
        for(int i=0;i<str.length;i++){
            if(str[i].equals(subString))
                return false;
        }
        return true;
    }

    //给WebView设置监听按钮
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.collect:
                if(judgeSame(getCollection(),webView.getTitle()+" "+webView.getUrl().toString())) {
                    collectEditor.putString(getCollection().length + 1 + "",webView.getTitle().toString()+" "+webView.getUrl().toString());
                    //Log.d("storge",getCollection().length + 1 + "-------------------------------");
                    collectEditor.apply();
                    Toast.makeText(MainActivity.this,"已收藏当前网页到收藏夹",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"你已收藏过该网站",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.go_for:
                if(webView.canGoBack()){
                    webView.goBack();
                }
                else {
                    Toast.makeText(MainActivity.this, "没有上一页了", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.go_next:
                if(webView.canGoForward()){
                    webView.goForward();
                }
                else {
                    Toast.makeText(MainActivity.this, "没有下一页了", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exit:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("提示");
                dialog.setMessage("是否要退出浏览器？");
                dialog.setCancelable(true);
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int which){
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
    }
    //释放内存应用，避免内存泄露
    protected void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
    }
}
