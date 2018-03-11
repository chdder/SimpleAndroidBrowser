package comtivity.example.simpleactivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
//搜索框活动
public class SearchActivity extends AppCompatActivity {
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
    //超时时间
    private long timeout = 6000;
    //超时处理
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        //Toolbar的使用
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //地址输入栏
        url = (AutoCompleteTextView ) findViewById(R.id.et_url_search);

        //地址栏自动联想
        editor = getSharedPreferences("Keyword",MODE_PRIVATE).edit();
        editor.putString("name","百度");
        editor.apply();
        //设置适配器
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getWord());
        //注意设置适配器
        url.setAdapter(adapter);

        //WebView进度条
        webViewProgress = (ProgressBar) findViewById(R.id.progressBar_web_search);

        //Webview的使用
        webView = (WebView) findViewById(R.id.web_view_search);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        //默认显示页面
        webView.loadUrl("https://www.baidu.com/");
        //设置页面自适应大小
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        //设置WebView进度条显示
        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int newProgress) {
                //url.setText(webView.getUrl().toString());
                if(newProgress==100){
                    webViewProgress.setVisibility(View.GONE);//加载完网页进度条消失
                }
                else{
                    webViewProgress.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    webViewProgress.setProgress(newProgress);//设置进度值
                }
            }
        });
        //设置连接超时
        webView.setWebViewClient(new WebViewClient() {
            /*
             * 创建一个WebViewClient,重写onPageStarted和onPageFinished
             * onPageStarted中启动一个计时器,到达设置时间后利用handle发送消息给activity执行超时后的动作.
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                timer = new Timer();
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(webView.getProgress()<100) {
                                        Toast.makeText(SearchActivity.this, "连接超时，请尝试刷新", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            timer.cancel();
                            timer.purge();
                    }
                };
                timer.schedule(tt, timeout);
            }

            /**
             * onPageFinished指页面加载完成,完成后取消计时器
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                timer.cancel();
                timer.purge();
            }
        });

        //搜索按钮
        Search = (Button) findViewById(R.id.bt_search_search);
        //添加监听事件
        Search.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String url_name = url.getText().toString();
                if(judgeSame(getWord(),url_name)) {
                    //在一次使用不退出的情况下，可以直接用add增加访问历史
                    adapter.add(url_name);
                    //如果退出再进入，历史会消失，所以要写入存储历史的文件之中，这样历史会永久保存
                    //获取历史记录的数量，防止键值重复
                    editor.putString(getWord().length + 1 + "", url_name);
                    editor.apply();
                }
                webView.loadUrl("https://m.baidu.com/from=1017188c/s?word="
                        +url.getText().toString()
                        +"&sa=tb&ts=6761128&t_kt=0&ie=utf-8&rsv_t=04c0AXp%252FnDlX1uXvwxgI2l6KK7O9%252B0Y311ZMNu%252Ffbb6z5AU%252Fhhw6Zf73yB%252FA%252FlA&tn=ntc&rsv_pq=11595599411768285232&ss=100&rqlang=zh&rsv_sug4=9912&inputT=3590&oq=%E5%AE%89%E5%8D%93%E7%9A%84listView%E5%A6%82%E4%BD%95%E5%88%86%E4%B8%A4%E8%A1%8C%E6%98%BE%E7%A4%BA%E7%9A%84");
            }

        });
    }

    //给toolbar的菜单设置点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    //获取历史记录转换为字符串数组
    private String[] getWord(){
        pref = getSharedPreferences("Keyword",MODE_PRIVATE);
        Map<String,?> map = pref.getAll();
        StringBuilder temp = new StringBuilder();

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            temp.append(entry.getValue()+" ");
        }
        return temp.toString().split(" ");
    }

    //判断是否有相同的记录
    private boolean judgeSame(String[] str,String subString){
        for(int i=0;i<str.length;i++){
            if(str[i].equals(subString))
                return false;
        }
        return true;
    }

    //释放内存应用，避免内存泄露
    protected void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
    }
}
