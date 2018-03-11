package comtivity.example.simpleactivity;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//下载服务类
public class DownloadServices extends IntentService {
    public bHelper bhelper=null;//用于数据库操作
    private DownloadThread threads[];//下载线程组
    private String url=null;//下载链接
    public static long fileSize;//要下载的文件大小
    private File file_service;//要下载的文件
    public static TextProgressBar pro;//自定义进度条

    public DownloadServices() { super("DownloadServices"); }

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        url=intent.getStringExtra("LINK");
        work(getApplicationContext(), 3, url);
        download();
        update( new DownloadListener() {
            public void onDownload(long downloaded_size) {
                final int result = (int) (downloaded_size * 100 / DownloadServices.fileSize);
                pro.getNum(getThreadNum());
                pro.setProgress(result);
                if(result==100){
                    pro.getNum(0);
                    pro.setProgress(result);
                    Toast.makeText(getApplicationContext(),"下载完成",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void work(Context context,int thread_num,String urls){

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String fileName = urls.substring(urls.lastIndexOf("/"));

        //创建文件
        file_service = new File(filePath + fileName);
        bhelper=new bHelper(context);
        threads=new DownloadThread[thread_num];
        url=urls;
        fileSize = getContentLength(url);

        //初始化线程
        for(int i=0;i<threads.length;i++){
            threads[i]=new DownloadThread(i+1,url,file_service,bhelper,thread_num);
        }

        //如果该文件长度为0，说明是第一次下载
        if (file_service.length() == 0) {
            deleteDownloading();
            makeDownloading();
        }

        //设置每个线程的已下载长度
        setThreadDownloadedLength();
    }

    //获取下载文件长度
    public long getContentLength(String urls){
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urls)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long conLength = response.body().contentLength();
                response.close();
                return conLength;
            }
        }catch(IOException e){}
        return 0;
    }

    //删除下载记录
    private void deleteDownloading() {
        SQLiteDatabase db = bhelper.getWritableDatabase();
        String sql = "DELETE FROM FileDownload WHERE downloadPath=?";
        db.execSQL(sql, new Object[] { url });
        db.close();
    }

    //创建每个线程的下载记录
    private void makeDownloading(){
        SQLiteDatabase db = bhelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (DownloadThread thread : threads) {
                String sql = "INSERT INTO FileDownload(downloadPath,threadId,downloadedLength) values(?,?,?)";
                db.execSQL(sql, new Object[] { url, thread.id, 0 });
            }
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    //设置每个线程的已下载长度
    private void setThreadDownloadedLength(){
        SQLiteDatabase db = bhelper.getReadableDatabase();
        String sql = "SELECT threadId,downloadedLength FROM FileDownload WHERE downloadPath=?";
        Cursor cursor = db.rawQuery(sql, new String[] { url });
        int i=0;
        if(cursor.moveToFirst()){
            do{
                Log.i("DownloadServices","取出："+cursor.getLong(1)+"--------------------------------------");
                threads[i].downloadedLength_thread=cursor.getLong(1);
                threads[i].start+=threads[i].downloadedLength_thread;
                i++;
            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    //开始下载
    public void download(){
        //启动线程
        for(int i=0;i<threads.length;i++){
            threads[i].start();
        }
    }

    public void update(DownloadListener listener) {
        //更新下载进度
        while (!isAllCompleted()) {
            if(listener!=null) {
                listener.onDownload(getDownloadedSize());
            }
        }
    }
    //获取正在下载的线程数
    public int getThreadNum(){
        int num=0;
        for(int i=0;i<threads.length;i++){
            if(!threads[i].isFinish_thread){
                num++;
            }
        }
        return num;
    }
    //判断是否所有线程都下载完毕
    public boolean isAllCompleted(){
        for(int i=0;i<threads.length;i++){
            if(!threads[i].isFinish_thread)
                return false;
        }
        return true;
    }


    class DownloadBinder extends Binder {
        //暂停下载
        public void setPause(){
            for(int i=0;i<threads.length;i++) {
                threads[i].isPause_thread = true;
                threads[i].isFinish_thread=true;
            }
        }
        //关闭数据库对象
        public void closeHelper(){
            bhelper.close();
        }
    }
    private DownloadBinder mbinder=new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }



    //获取目前总下载量
    private long getDownloadedSize(){
        long sum = 0;
        for (int i = 0; i < threads.length; i++) {
            sum += threads[i].downloadedLength_thread;
            Log.d("getDownloadedSize","线程"+(i+1)+"下载进度:"+threads[i].downloadedLength_thread);
        }
        Log.d("getDownloadedSize",sum+"-------------------------------------------------");
        return sum;
    }

    public void onDestroy(){
        super.onDestroy();
    }
}
