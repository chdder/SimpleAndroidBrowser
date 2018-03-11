package comtivity.example.simpleactivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
//下载服务类
public class MyService extends Service {
    private DownloadMission downloadmission;
    private String durl=null;
    private downloadBinder dbinder=new downloadBinder();
    private ProgressBar progressbar;
    private Notification getNotice(String title,int progress){
        Intent intent=new Intent(this,DownloadTwoActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
            progressbar.setProgress(progress);
            progressbar.setContentDescription(progress+"%");
        }
        return builder.build();
    }

    private NotificationManager getManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private DownloadNotice downloadnotice=new DownloadNotice() {
        @Override
        public void noticeProgress(int progress) {
            getManager().notify(1,getNotice("Downloading",progress));
        }

        @Override
        public void noticeSuccess() {
            downloadmission=null;
            stopForeground(true);
            getManager().notify(1,getNotice("Download Success",-1));
            Toast.makeText(MyService.this,"下载成功",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void noticeFail() {
            downloadmission=null;
            stopForeground(true);
            getManager().notify(1,getNotice("Download Failed",-1));
            Toast.makeText(MyService.this,"下载失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void noticePause() {
            downloadmission=null;
            stopForeground(true);
            getManager().notify(1,getNotice("Download Paused",-1));
            Toast.makeText(MyService.this,"已暂停",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void noticeCancel() {
            downloadmission=null;
            stopForeground(true);
            Toast.makeText(MyService.this,"已取消下载",Toast.LENGTH_SHORT).show();
        }
    };
    class downloadBinder extends Binder {
        public void startDownload(String url,ProgressBar progress){
            if(downloadmission==null){
                //Log.d("MyService","在下载");
                progressbar=progress;
                durl=url;
                downloadmission=new DownloadMission(downloadnotice);
                downloadmission.execute(durl);
                startForeground(1,getNotice("Downloading",0));
                Toast.makeText(MyService.this,"正在开始下载...",Toast.LENGTH_SHORT).show();
            }
        }
        public void stopDownload(){
            if(downloadmission!=null){
                downloadmission.pausedownload();
            }
        }
        public void cancelDownload(){
            if(downloadmission!=null){
                downloadmission.canceldownload();
                progressbar.setProgress(0);
                //Log.d("MyService","不暂停直接取消");
            }
            else{
                if(durl!=null){
                    String fileName = durl.substring(durl.lastIndexOf("/"));
                    //Log.d("MyService",fileName+"------------------------------------------------------------------");
                    String directory = Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    progressbar.setProgress(0);
                    getManager().cancel(1);
                    stopForeground(true);

                    Toast.makeText(MyService.this, "已取消下载", Toast.LENGTH_SHORT).show();
                    //Log.d("MyService","暂停再取消");
                }
            }
        }
    }
    public MyService(){}

    @Override
    public IBinder onBind(Intent intent) {
        return dbinder;
    }
}
