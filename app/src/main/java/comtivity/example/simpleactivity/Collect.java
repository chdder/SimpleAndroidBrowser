package comtivity.example.simpleactivity;

/**
 * Created by Administrator on 2017/7/1 0001.
 */

public class Collect {
    private String title;
    private String url;

    public Collect(String title,String url){
        this.title=title;
        this.url=url;
    }
    public String getTitle(){
        return title;
    }
    public String getUrl(){
        return url;
    }
}
