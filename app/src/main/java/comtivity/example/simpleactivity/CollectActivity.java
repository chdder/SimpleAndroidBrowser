package comtivity.example.simpleactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Iterator;
import java.util.Map;

public class CollectActivity extends AppCompatActivity {

    //负责存储收藏的网页链接
    private SharedPreferences collectUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect_activity);

        //Toolbar的使用
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_collect);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //滚动界面
        ListView listView = (ListView) findViewById(R.id.recycler_veiw);

        //自定义适配器传入数据
        CollectAdapter collectAdapter = new CollectAdapter(CollectActivity.this,R.layout.collect_item,getCollection());
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(CollectActivity.this,android.R.layout.simple_list_item_1,getCollection());
        listView.setAdapter(collectAdapter);
        //设置点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CollectActivity.this,MainActivity.class);
                intent.putExtra("collect_URL",getCollection()[position].split(" ")[1]);
                startActivity(intent);
            }
        });
    }

    //获取收藏记录转换为字符串数组
    private String[] getCollection(){
        collectUrl = getSharedPreferences("collection",MODE_PRIVATE);
        Map<String,?> map = collectUrl.getAll();
        //StringBuilder temp = new StringBuilder();
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

    //设置返回事件
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //toolbar的返回键设置
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
