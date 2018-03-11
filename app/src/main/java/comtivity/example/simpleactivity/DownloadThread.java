package comtivity.example.simpleactivity;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadThread extends Thread{
    private static bHelper bhelper=null;//用于数据库操作
    private long block;//下载区间长度
    public int id;//线程id
    public boolean isPause_thread=false;//暂停标志
    public boolean isFinish_thread=false;//完成标志
    public long downloadedLength_thread=0;//已下载的长度
    public long start;//下载起点
    public long end;//下载终点
    private String downloadLink;//下载地址
    private File file;//下载的文件
    public long fileSize;//文件大小
    private int num;//线程数量

    DownloadThread(int id,String downloadLink,File file,bHelper bhelper,int thread_num){
        this.id=id;
        this.downloadLink=downloadLink;
        this.file=file;
        this.bhelper=bhelper;
        num=thread_num;
    }

    public void run() {
        fileSize=getContentLength(downloadLink);
        block=fileSize % num == 0 ? fileSize / num : fileSize / num + 1;
        //设置开始、结束下载点
        start=(id-1)*block+downloadedLength_thread;
        if(id==3)
            end=fileSize;
        else
            end=id*block-1;
        //Log.i("DownloadThread","线程"+id+": start: "+start+"--------------end: "+end+"----------------------");
        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + start + "-" + end)
                    .url(downloadLink)
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(start);
                byte b[] = new byte[1024];
                int len;
                while (!isPause_thread && !isFinish_thread && ((len = is.read(b)) != -1)) {
                    savedFile.write(b,0,len);
                    downloadedLength_thread+=len;
                }
                response.body().close();
            }
            updateDownloading();
            isFinish_thread=true;
            isPause_thread=true;
            Log.i("DownloadThread","线程"+id+"结束-----------------------------------------");
            Log.d("DownloadThread","状态：isPause_thread: "+isPause_thread+"  isFinish_thread: "+isFinish_thread+"-------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(is!=null){
                    is.close();
                }
                if(savedFile!=null){
                    savedFile.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //更新数据库
    private synchronized void updateDownloading() {
        SQLiteDatabase db = bhelper.getWritableDatabase();
        try {
            Log.i("DownloadThread","线程"+id+"使用数据库更新中");
            String sql = "UPDATE FileDownload SET downloadedLength=? WHERE threadId=? AND downloadPath=?";
            db.execSQL(sql, new String[] { downloadedLength_thread + "", id + "", downloadLink });
        } catch(Exception e){
            e.printStackTrace();
        }
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
}
