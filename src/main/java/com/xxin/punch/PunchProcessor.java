package com.xxin.punch;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xxin
 * @Created
 * @Date 2020/5/16 21:10
 * @Description
 */
public class PunchProcessor {
    private CloseableHttpClient client = HttpClients.createDefault();
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;
//    private static final long PERIOD_DAY = 5*60*1000 ;
    private static final int hour = 2;
    private static final int  minute = 0;
    private static int retry=3;
    private static List<Info> account = new ArrayList<Info>();

    private   void enterLogin() throws IOException {
        String url = "http://eswis.gdpu.edu.cn/login.aspx";
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse res = client.execute(get);
        res.close();
    }
    private  boolean login(String username,String password) throws IOException {
        String url = "http://eswis.gdpu.edu.cn/login.aspx";
        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("__EVENTTARGET", "logon"));
        nvps.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        nvps.add(new BasicNameValuePair("__VIEWSTATE","/wEPDwUKMTYyMDg3MzEwOA9kFgICAw9kFgQCCQ8PFgIeBFRleHQFPUVzd2lzIOmrmOagoeWtpueUn+e7vOWQiOacjeWKoeW5s+WPsCDlrabnlJ/lt6XkvZznrqHnkIbns7vnu59kZAILDw8WAh8ABU/ljZXkvY3ogZTns7vmlrnlvI865bm/5bee5biC55Wq56a65Yy65aSn5a2m5Z+O5bm/5Lic6I2v56eR5aSn5a2m5a2m55Sf5bel5L2c5aSEZGRkApHRtEp47HU3hwHc7/VDYk7HCWP1VrIoeU831p6jX5o=" ));
        nvps.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "C2EE9ABB"));
        nvps.add(new BasicNameValuePair("__EVENTVALIDATION","/wEdAAR2x90ffMPh62fEUUHFD4Tp1kNwsRYEDqnEZGvD/d7NHmTWfBqM7WrvRN2Hp35y65arCB7eRXhUFaYy1hE/nWj6nK478H4eQaeI8UwPY/TWzZwSA7XuIBUqSutXvspX48U=" ));
        nvps.add(new BasicNameValuePair("log_username",username ));
        nvps.add(new BasicNameValuePair("log_password",password));

        post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        post.setHeader("Content-type", "application/x-www-form-urlencoded");
        post.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        CloseableHttpResponse res = client.execute(post);
        HttpEntity entity = res.getEntity();
        String html = EntityUtils.toString(entity);
        System.out.println("登陆成功");
        return true;
    }
    private String enterDefault() throws IOException {
        String url = "http://eswis.gdpu.edu.cn/Default.aspx";
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse res = client.execute(get);
        HttpEntity entity = res.getEntity();
        String html = EntityUtils.toString(entity);
        int from = html.indexOf("opt_rc_jkdk.aspx");
        int end = html.indexOf("健康打卡")-2;
        html = html.substring(from,end);
        res.close();
        return html;
    }
    private void enterPunch(String url) throws IOException {
        url = "http://eswis.gdpu.edu.cn/"+url;
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse res = client.execute(get);
        HttpEntity entity = res.getEntity();
        String html = EntityUtils.toString(entity);
    }
    private void enterInput(String url) throws IOException {
        url = "http://eswis.gdpu.edu.cn/"+url;
        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("__EVENTTARGET", ""));
        nvps.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        nvps.add(new BasicNameValuePair("__VIEWSTATE","/wEPDwUKLTc2MDkyMDk0Mw9kFgJmD2QWAgIDD2QWDmYPFgIeB1Zpc2libGVoZAIBDw8WAh4EVGV4dAURMTYwMDUwMjE1MyDlrabnlJ9kZAICDw8WAh8AZ2RkAgMPZBYCAgEPFgIfAGhkAgQPZBYKAgMPZBYCAgUPEGRkFgBkAgUPFgIeCWlubmVyaHRtbAWnAjxsaSBjbGFzcz0iMCBzZWxlY3RlZCBzZWwgYWN0Ij48YSBocmVmPSJvcHRfcmNfamtkay5hc3B4P2tleT1HV1F2d3FpbmU3WWVvQ1dsJmZpZD0yMCI+5YGl5bq35omT5Y2hPC9hPjwvbGk+DQo8bGkgY2xhc3M9IjEiPjxhIGhyZWY9Im9wdF9yY19qa2RrY3guYXNweD9rZXk9R1dRdndxaW5lN1llb0NXbCZmaWQ9MjAiPuaJk+WNoeafpeivojwvYT48L2xpPg0KPGxpIGNsYXNzPSIyIj48YSBocmVmPSJvcHRfcmNfZnhzcS5hc3B4P2tleT1HV1F2d3FpbmU3WWVvQ1dsJmZpZD0yMCI+6L+U5qCh55Sz6K+3PC9hPjwvbGk+DQpkAgkPFgIfAGdkAgsPZBYGAgEPZBYCAgUPDxYCHwFlZGQCAw9kFgQCAQ88KwARAQwUKwAAZAIFDzwrABEBDBQrAABkAgUPZBYUAgEPDxYCHwFkZGQCBw8PFgIfAQUJMjAyMC81LzE2ZGQCCQ8PFgIfAQUVMTYwMDUwMjE1Mywg6ZmI5LmZ6ZGrZGQCCw8PFgIfAQULMTMwMDUxNjIzOTBkZAINDxBkZBYAZAITDxBkZBYAZAIVDxBkZBYAZAIXDxBkZBYAZAIhD2QWBgIBD2QWAgIBDxBkZBYAZAIDD2QWAgIBDxBkZBYAZAIFD2QWAgIBDxBkZBYAZAIlD2QWCmYPDxYCHwEFDOW5v+S4nOaxleWktGRkAgEPDxYCHwEFPOW5v+S4nOecgeaxleWktOW4gua9ruWNl+WMuumbt+WyremVh+m6u+WflOadkeS4reWvqDPniYcyOeWPt2RkAgIPEGRkFgBkAgcPEGRkFgBkAgoPEGRkFgBkAg0PDxYCHwFlZGQCBQ8PFgIfAQU9RXN3aXMg6auY5qCh5a2m55Sf57u85ZCI5pyN5Yqh5bmz5Y+wIOWtpueUn+W3peS9nOeuoeeQhuezu+e7n2RkAgYPDxYCHwEFT+WNleS9jeiBlOezu+aWueW8jzrlub/lt57luILnlarnprrljLrlpKflrabln47lub/kuJzoja/np5HlpKflrablrabnlJ/lt6XkvZzlpIRkZBgDBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAQUUY3RsMDAkY3BoX3JpZ2h0JGVfb2sFE2N0bDAwJGNwaF9yaWdodCRndjEPZ2QFE2N0bDAwJGNwaF9yaWdodCRndjIPZ2SA1fR1zXLkGnziHxBdk5KjhW3mtOa1cS9+o2PcNuTgoQ=="));
        nvps.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "DC47EEF4"));
        nvps.add(new BasicNameValuePair("__EVENTVALIDATION","/wEdAATl9oPlAd9RTnz8X5F9RbitCH7C2ooKcj+hye21epcYtzZfOuG+abiMM5hqqD7y7iHuGK54SO0O/aN8Wrf5s7O2pcSF0iw/Lu6pwv+vOPd4pz1VmZernuDV/AA8DLrWSes="));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_ok","on"));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$ok_submit","开始填报"));
        post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        post.setHeader("Content-type", "application/x-www-form-urlencoded");
        post.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        CloseableHttpResponse res = client.execute(post);
        HttpEntity entity = res.getEntity();
        String html = EntityUtils.toString(entity);
    }
    private String punch(String url,String atSchool,String locate,String temp) throws IOException {
        url = "http://eswis.gdpu.edu.cn/"+url;
        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("__EVENTTARGET", ""));
        nvps.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        nvps.add(new BasicNameValuePair("__LASTFOCUS", ""));
        nvps.add(new BasicNameValuePair("__VIEWSTATE", "/wEPDwUKLTc2MDkyMDk0Mw9kFgJmD2QWAgIDD2QWDmYPFgIeB1Zpc2libGVoZAIBDw8WAh4EVGV4dAURMTYwMDUwMjE1MyDlrabnlJ9kZAICDw8WAh8AZ2RkAgMPZBYCAgEPFgIfAGhkAgQPZBYKAgMPZBYCAgUPEGRkFgBkAgUPFgIeCWlubmVyaHRtbAWnAjxsaSBjbGFzcz0iMCBzZWxlY3RlZCBzZWwgYWN0Ij48YSBocmVmPSJvcHRfcmNfamtkay5hc3B4P2tleT1HV1F2d3FpbmU3WWVvQ1dsJmZpZD0yMCI+5YGl5bq35omT5Y2hPC9hPjwvbGk+DQo8bGkgY2xhc3M9IjEiPjxhIGhyZWY9Im9wdF9yY19qa2RrY3guYXNweD9rZXk9R1dRdndxaW5lN1llb0NXbCZmaWQ9MjAiPuaJk+WNoeafpeivojwvYT48L2xpPg0KPGxpIGNsYXNzPSIyIj48YSBocmVmPSJvcHRfcmNfZnhzcS5hc3B4P2tleT1HV1F2d3FpbmU3WWVvQ1dsJmZpZD0yMCI+6L+U5qCh55Sz6K+3PC9hPjwvbGk+DQpkAgkPFgIfAGdkAgsPZBYGAgEPFgIfAGgWBAIBDxAPFgIeB0NoZWNrZWRnZGRkZAIFDw8WAh8BZWRkAgMPZBYEAgEPPCsAEQEMFCsAAGQCBQ88KwARAQwUKwAAZAIFDxYCHwBnFgwCAQ8PFgIfAWRkZAIHDw8WAh8BBQkyMDIwLzUvMTZkZAIJDw8WAh8BBRUxNjAwNTAyMTUzLCDpmYjkuZnpkatkZAILDw8WAh8BBQsxMzAwNTE2MjM5MGRkAiEPZBYGAgEPZBYCAgEPEGRkFgBkAgMPZBYCAgEPEGRkFgBkAgUPZBYCAgEPEGRkFgBkAiUPZBYKZg8PFgIfAQUM5bm/5Lic5rGV5aS0ZGQCAQ8PFgIfAQU85bm/5Lic55yB5rGV5aS05biC5r2u5Y2X5Yy66Zu35bKt6ZWH6bq75Z+U5p2R5Lit5a+oM+eJhzI55Y+3ZGQCAg8QZGQWAGQCBw8QZGQWAGQCCg8QZGQWAGQCDQ8PFgIfAWVkZAIFDw8WAh8BBT1Fc3dpcyDpq5jmoKHlrabnlJ/nu7zlkIjmnI3liqHlubPlj7Ag5a2m55Sf5bel5L2c566h55CG57O757ufZGQCBg8PFgIfAQVP5Y2V5L2N6IGU57O75pa55byPOuW5v+W3nuW4gueVquemuuWMuuWkp+WtpuWfjuW5v+S4nOiNr+enkeWkp+WtpuWtpueUn+W3peS9nOWkhGRkGAMFHl9fQ29udHJvbHNSZXF1aXJlUG9zdEJhY2tLZXlfXxYIBRpjdGwwMCRjcGhfcmlnaHQkZV9oZWFsdGgkMAUaY3RsMDAkY3BoX3JpZ2h0JGVfaGVhbHRoJDEFGmN0bDAwJGNwaF9yaWdodCRlX2hlYWx0aCQyBRpjdGwwMCRjcGhfcmlnaHQkZV9oZWFsdGgkMwUaY3RsMDAkY3BoX3JpZ2h0JGVfaGVhbHRoJDQFGmN0bDAwJGNwaF9yaWdodCRlX2hlYWx0aCQ1BRpjdGwwMCRjcGhfcmlnaHQkZV9oZWFsdGgkNQUZY3RsMDAkY3BoX3JpZ2h0JGVfY2hhbmdlZAUTY3RsMDAkY3BoX3JpZ2h0JGd2MQ9nZAUTY3RsMDAkY3BoX3JpZ2h0JGd2Mg9nZHiFQwzyyAzy59qxr62ZFiwu9+LtmYH4Z0S/PTBu1BnI"));
        nvps.add(new BasicNameValuePair("__VIEWSTATEGENERATOR","DC47EEF4"));
        nvps.add(new BasicNameValuePair("__EVENTVALIDATION","/wEdAB0oJ1ZLNnMue+4XOQ+yURrLCH7C2ooKcj+hye21epcYt57zU+tJOrbkpfeI+4y+0QA5Z9oerNMkjXhVZ0NKo6l0BNuOnAvslhD1zvPfE6p8njwsBakjhQfAu8ecC7+5T+n6I++UYgK22OHU7xYrZo+AKAo7EMQ2twi8SmqnRRLHpCzoZTaRpUmlAHXi1v9rUnrcoWB+ZSaSwSyZ6Qd02q/fS475+yi9pu/K8AEne1pUQSldxvkLsgSZYXV0l/+g5CnUkowtqINm2hseYINhdouXDfiWxlld6EK/kFEymJeFqzUdaox1MfhJKAaU+2/+Xx3jFdvX4jziLBNDAqEehYqvzueLZ3ZddW59ehyg7Yp6RuigWX5Lrhqr4QVjc5zljd9VSUw93iIR+p/Vq1zpHwQug9kiUCHfLWu17Iub8ibnPiuWw5NvonImWnE6wdiOm1AlP3ZSjBpKnYeeXjunNbU4NifJrV4+PZgfSYi8dEJ8WWpnzHL1mRqbMyXodtkOCP/yWwmWBKqAn17OeRrf7PRHqmRamdqGw8vMM5Su3ukGxJDoS3W6wQtiXPuK5s6fg7f2gdyENm5/S/WZYdNiK9fGV2qApdhdm4kj1DrmuhwbL3fKVjssXtxfNcnT3/pIazX7m/cVIXJoRKqI+ZkjLSAN"));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_atschool",atSchool));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_location",locate));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_observation","无下列情况"));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_health$0","on"));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_temp",temp));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_describe",""));
        nvps.add(new BasicNameValuePair("ctl00$cph_right$e_submit","提交保存"));

        post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        post.setHeader("Content-type", "application/x-www-form-urlencoded");
        post.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        CloseableHttpResponse res = client.execute(post);
        HttpEntity entity = res.getEntity();
        String html = EntityUtils.toString(entity);
        if (html.contains(" <span id=\"ctl00_cph_right_e_msg\" class=\"msgstr\">打卡成功")){
            return "打卡成功";
        }else{
            return "打卡出错";
        }
    }

    private void timer(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        System.out.println("当前时间"+formatter.format( new Date(System.currentTimeMillis())));
        System.out.println("设置定时任务");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, PunchProcessor.hour);
        calendar.set(Calendar.MINUTE,PunchProcessor.minute);
        calendar.set(Calendar.SECOND, 0);
        Date date=calendar.getTime(); //第一次执行定时任务的时间
        if (date.before(new Date())) {
            System.out.println("设定时间已过，马上执行打卡，并设置明天任务");
            doPunch();
            date = this.addDay(date, 1);
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("----------执行定时任务----------");
                doPunch();
                System.out.println("------------任务完成------------");
            }
        };
        //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
        timer.schedule(task,date,PERIOD_DAY);
    }
    // 增加或减少天数
    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }
    public void doPunch(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PunchProcessor processor = new PunchProcessor( );
        for (Info info:account){
            try {
                processor.enterLogin();
                boolean login = processor.login(info.username,info.password);
                if (login){
                    String url = processor.enterDefault();
                    processor.enterPunch(url);
                    processor.enterInput(url);
                    String res = processor.punch(url, info.atSchool, info.locate, info.temp);
                    System.out.println("<"+formatter.format( new Date(System.currentTimeMillis()))+" >"+info.username+" "+res);
                    MailUtil.to=info.mail;
                    MailUtil.content = "当前时间"+formatter.format( new Date(System.currentTimeMillis()))+" "+info.username+" "+res;
                    MailUtil.sendComplexMail();
                    System.out.println(info.username+"开启明天打卡的任务");
                    retry=3;
                }
            }catch (Exception e){
                System.out.println("当前时间-"+formatter.format( new Date(System.currentTimeMillis()))+" "+"打卡出错"+e.getMessage());
                MailUtil.to=info.mail;
                MailUtil.content = "当前时间-"+formatter.format( new Date(System.currentTimeMillis()))+info.username+" "+"打卡出错";
                MailUtil.sendComplexMail();
                if (--retry>0){
                    System.out.println("尝试重新执行："+retry);
                    doPunch();
                }
            }
        }
    }
    public static void main(String[] args) throws IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/healthPunch?useUnicode=true&characterEncoding=UTF-8&useSSL=false", "root", "");
            String sql = "select * from info";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeQuery();
            ResultSet resultSet = statement.getResultSet();
            PunchProcessor processor = new PunchProcessor();
            while (resultSet.next()) {
                Info info = new Info();
                info.username = resultSet.getString("username");
                info.password=resultSet.getString("password");;
                info.locate=resultSet.getString("locate");
                info.temp = resultSet.getString("temp");
                info.atSchool = resultSet.getString("atSchool");
                info.mail = resultSet.getString("mail");
                System.out.println(info);
                PunchProcessor.account.add(info);
            }
            processor.timer();
        } catch (Exception e) {
            System.out.println("数据库连接失败:" + e.getMessage());
        }
    }
}
class Info{
    String locate="";
    String atSchool="否";
    String temp = "36.3";
    String username="";
    String password = "";
    String mail="xxxx@qq.com";

    @Override
    public String toString() {
        return "Info{" +
                "locate='" + locate + '\'' +
                ", atSchool='" + atSchool + '\'' +
                ", temp='" + temp + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", mail='" + mail + '\'' +
                '}';
    }
}
