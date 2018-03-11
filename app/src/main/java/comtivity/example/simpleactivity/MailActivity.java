package comtivity.example.simpleactivity;

import android.content.DialogInterface;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//发送邮件活动
public class MailActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText reciveMailText,mailThemeText,mailContainText,senderName,senderMailText,senderMailPassword;
    private Button reSet,send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_mail);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        reciveMailText = (EditText) findViewById(R.id.receiver_mail_text);
        mailThemeText = (EditText) findViewById(R.id.mail_theme_text);
        mailContainText = (EditText) findViewById(R.id.mail_contain_text);
        senderName = (EditText) findViewById(R.id.sender_name_text);
        senderMailText = (EditText) findViewById(R.id.sender_mail_text);
        senderMailPassword = (EditText) findViewById(R.id.sender_password_text);
        reSet = (Button) findViewById(R.id.reset_bt);
        send = (Button) findViewById(R.id.send_bt);

        reSet.setOnClickListener(this);
        send.setOnClickListener(this);

       //提示用户使用注意事项
        AlertDialog.Builder dialog = new AlertDialog.Builder(MailActivity.this);
        dialog.setTitle("使用提示");
        dialog.setMessage("请确保您的邮箱已开通SMTP服务。");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();

    }

    public void onClick(View v) {
        switch (v.getId()){
            //重置所有输入
            case R.id.reset_bt:
                reciveMailText.setText("");
                mailThemeText.setText("");
                mailContainText.setText("");
                senderName.setText("");
                senderMailText.setText("");
                senderMailPassword.setText("");
                break;
            //发送邮件
            case R.id.send_bt:
                new sendMessage(reciveMailText.getText().toString(),mailThemeText.getText().toString(),mailContainText.getText().toString(),
                        senderName.getText().toString(),senderMailText.getText().toString(),senderMailPassword.getText().toString()).start();
                Toast.makeText(MailActivity.this,"已发送",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
    //设置返回事件
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
           //toolbar返回事件
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
