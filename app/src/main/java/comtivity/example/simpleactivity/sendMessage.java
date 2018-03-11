package comtivity.example.simpleactivity;

import android.util.Log;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import javax.activation.DataHandler;

//用来发送邮件的类
public class sendMessage extends Thread {
    private String recemail, mailtheme, mailtext, sendername,sendmail, senderpass;
    sendMessage(String recemail,String mailtheme,String mailtext,String sendername,String sendmail,String senderpass){
        this.recemail=recemail;
        this.mailtheme=mailtheme;
        this.mailtext=mailtext;
        this.sendername=sendername;
        this.sendmail=sendmail;
        this.senderpass=senderpass;
    }
    @Override
    public void run() {
        SimpleEmail email = new SimpleEmail();
        //这个服务器可以在网易邮箱查到，一共有3个
        email.setHostName("smtp.163.com");// 设置使用发电子邮件的邮件服务器
        try {
            //收件人
            email.addTo(recemail);
            //发件人的用户名和密码，注意密码是授权密码不是邮箱密码，授权密码是用作第三方登录验证的，需要开启smtp服务来获得
            email.setAuthentication(sendername, senderpass);//smtp认证的用户名和密码
            //发件人的邮箱和用户名
            email.setFrom(sendmail, sendername);//写的信箱要与设置使用发电子邮件的邮件服务器相对应
            email.setSubject(mailtheme);//标题
            email.setMsg(mailtext);//内容
            email.send();//发送邮件
        } catch (EmailException ex) {
            ex.printStackTrace();
        }
    }
}
