package comtivity.example.simpleactivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class bHelper extends SQLiteOpenHelper {
    public bHelper(Context context){
        super(context,"MultiDownload.db",null,1);
    }

    public void onCreate(SQLiteDatabase db){
        //创建数据库，表单的每一列从左到右分别是每一行的编号，下载路径，线程编号，该线程已经下载的长度。
        db.execSQL("CREATE TABLE FileDownload(id integer primary key autoincrement,downloadPath varchar(100),threadId INTEGER,downloadedLength INTEGER)");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
