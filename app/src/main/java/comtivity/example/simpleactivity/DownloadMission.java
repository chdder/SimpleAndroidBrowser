package comtivity.example.simpleactivity;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadMission extends AsyncTask<String,Integer,Integer> {
    private DownloadNotice notice;
    private boolean cancel=false;
    private boolean pause=false;
    public static final int DOWNLOAD_SUCCESS=0;
    public static final  int DOWNLOAD_FAIL=1;
    public static final int DOWNLOAD_CANCEL=2;
    public static final int DOWNLOAD_PAUSE=3;
    int lastprogress;//上一次下载进度

    public DownloadMission(DownloadNotice n){
        notice=n;
    }

    public Integer doInBackground(String... params){
        RandomAccessFile downloadFile=null;//下载存储的文件
        InputStream in=null;
        File file=null;
        long downloadedLength=0;//已下载长度
        long contentLength=0;//目标文件长度
        String Url=params[0];//要下载的链接
        int progress;//下载进度

        String fileName=Url.substring(Url.lastIndexOf("/"));//获取下载文件名字
        try{
            String filePath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file=new File(filePath+fileName);//创建目标文件

            if(file.exists()){
                downloadedLength=file.length();//如果要下载的文件已经存在，则获取已下载的文件长度
            }
            contentLength=getContentLength(Url);
            if(contentLength==0){
                return DOWNLOAD_FAIL;//获取目标文件长度为0，提示下载失败。
            }
            else if(contentLength==downloadedLength){
                return DOWNLOAD_SUCCESS;//目标文件已存在，无需下载。
            }

            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .addHeader("RANGE","bytes="+downloadedLength+"-")
                    .url(Url)
                    .build();

            Response response=client.newCall(request).execute();
            if(response!=null){
                in=response.body().byteStream();
                downloadFile=new RandomAccessFile(file,"rw");
                downloadFile.seek(downloadedLength);
                byte b[]=new byte[1024];
                int total=0;
                int len;
                while((len=in.read(b))!=-1){
                    if(cancel){
                        return DOWNLOAD_CANCEL;
                    }
                    else if(pause){
                        return DOWNLOAD_PAUSE;
                    }
                    else{
                        total+=len;
                        downloadFile.write(b,0,len);
                        progress=(int)((total+downloadedLength)*100/contentLength);
                        publishProgress(progress);//通过onProgressUpdate（）更新进度
                    }
                }
                response.body().close();
                return DOWNLOAD_SUCCESS;
            }
        }catch(Exception e){}
        finally{
            try {
                if(in!=null)
                    in.close();

                if(downloadFile!=null)
                    downloadFile.close();

                if(cancel && file!=null)
                    file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return DOWNLOAD_FAIL;
    }

    protected void onProgressUpdate(Integer... values){//更新进度
        int progress=values[0];
        if(progress>lastprogress){
            notice.noticeProgress(progress);
            lastprogress=progress;
        }
    }

    protected void onPostExecute(Integer status){
        switch(status){
            case DOWNLOAD_SUCCESS:
                notice.noticeSuccess();
                break;
            case DOWNLOAD_FAIL:
                notice.noticeFail();
                break;
            case DOWNLOAD_PAUSE:
                notice.noticePause();
                break;
            case DOWNLOAD_CANCEL:
                notice.noticeCancel();
                break;
            default:
                break;
        }
    }

    public void pausedownload(){
        pause=true;
    }
    public void canceldownload(){
        cancel=true;
    }

    private long getContentLength(String urls){//获取下载文件长度
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
