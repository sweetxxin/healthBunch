package com.xxin.punch;




import org.apache.commons.mail.HtmlEmail;

import java.util.Date;

public class MailUtil {
     static String from="jessic_xxin@aliyun.com";
     static String to="1208668915@qq.com";
     static String auth="chen18926278337";
     static String subject="打卡通知";
     static String content="打卡成功";
     static String host="smtp.aliyun.com";

    public static boolean sendComplexMail() {
        HtmlEmail mail = new HtmlEmail();
        mail.setHostName(host);
        mail.setAuthentication(from, auth);
        mail.setSmtpPort(465);
        mail.setSSLOnConnect(true);
        mail.setSslSmtpPort("465");
        mail.setDebug(false);
        try {
            mail.setFrom(from);
            mail.addTo(to);
            mail.setCharset("UTF-8");
            mail.setSubject(subject);
            mail.setHtmlMsg("<html><body>" + content.toString() + "</body></html>");
            mail.setSentDate(new Date());
            mail.send();
            System.out.println("邮件已发送");
        } catch (Exception e) {
            System.out.printf("邮件发送失败" + e.getMessage());
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        sendComplexMail();
    }

}
