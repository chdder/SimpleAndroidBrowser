package comtivity.example.simpleactivity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2017/7/1 0001.
 */
//自定义适配器以显示listview历史记录
public class CollectAdapter extends ArrayAdapter<String> {
    private int TextId;
    public CollectAdapter(Context context, int TextViewId, String[] objects){
        super(context,TextViewId,objects);
        TextId = TextViewId;
    }

    @NonNull
    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        String collect = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(TextId,parent,false);
        TextView title = (TextView) view.findViewById(R.id.collect_title);
        title.setText(collect.split(" ")[0]+"\n"+collect.split(" ")[1]);
        return view;
    }
}
