package comtivity.example.simpleactivity;

/**
 * Created by Administrator on 2017/4/20 0020.
 */

public interface DownloadNotice {
    void noticeProgress(int progress);//下载进度
    void noticeSuccess();//下载成功
    void noticeFail();//下载失败
    void noticePause();//暂停下载
    void noticeCancel();//取消下载
}
