package MykesTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import arc.util.Log;
import mindustry.gen.*;
import mindustry.*;
import org.json.JSONObject;

public class MTDChatEngineThread extends Thread{

    private static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                // 存放数据
                StringBuilder sbf = new StringBuilder();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.info("try continue");
        } catch (java.net.SocketTimeoutException e){
            e.printStackTrace();
            Log.info("try continue");
        } catch (IOException e) {
            e.printStackTrace();
            Log.info("try continue");
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("try continue");
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.info("try continue");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.info("try continue");
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.info("try continue");
                }
            }

            if( connection!= null) {
                connection.disconnect();// 关闭远程连接
            }
        }

        return result;
    }

        public int m_nDebug = 1;
        public int m_nRunning = 0;
        private String m_strLogPrefix = "[ChatEngineThread]";
        private int i = 10;
        private Object object = new Object();
        public class ChatReply {
            public Player player;
            public String strAsk;
            public String strReply;
            public int nHideReply;
            public ChatReply(Player player, String strAsk)
            {
                this.player = player;
                this.strAsk = strAsk;
                this.strReply ="";
                nHideReply = 0;
            }
        }
        private ArrayList<ChatReply> m_ListAsk = new ArrayList<ChatReply>() ;
        private ArrayList<ChatReply> m_ListReply = new ArrayList<ChatReply>() ;

        public MTDChatEngineThread()
        {
            Log.info("Start MTDChatEngine Thread");
            m_nRunning = 1;
            this.start();
        }

        // note this may be called in another thread.
    public void AskAsync(Player player, String strAsk, int nHideReply)
    {
        ChatReply chat = new ChatReply(player, strAsk);
        chat.nHideReply = nHideReply;
        synchronized (m_ListAsk) {
            m_ListAsk.add(chat);
        }
        if( m_nDebug == 1) {
            Log.info("Insert chat:"+player.toString()+":"+strAsk);
        }
        // if it's stopped because of server stop, restart it.
        if(m_nRunning == 0)
        {
            m_nRunning =1;
            this.start();
        }
    }

    // note this may be called in another thread.
    public ChatReply GetNextReply()
    {
        ChatReply chat = null;
        Log.info("trying to get reply:");
        synchronized (m_ListReply) {
            // find the one that has not get replied.
            for (ChatReply chatOne:m_ListReply) {
                Log.info("itering reply:"+chatOne.strReply);
                if( "" != chatOne.strReply) {
                    chat = chatOne;
                    Log.info("find reply:"+chatOne.strReply);
                    m_ListReply.remove(chatOne);
                    break;
                }
            }

        }
        return chat;

    }

    @Override
    public void run() {
            m_nRunning = 1;
                ChatReply chat ;
                while (1 == m_nRunning) {
                    //Log.info("Running MTDChatEngine Thread");

                    chat = null;
                    synchronized (m_ListAsk) {
                        // find the one that has not get replied.
                        for (ChatReply chatOne:m_ListAsk) {
                            if( "" == chatOne.strReply) {
                                chat = chatOne;
                                m_ListAsk.remove(chatOne);
                                break;
                            }
                        }
                    }

                    // get the content through http
                    if( chat != null) {
                        String strEncodedAsk = URLEncoder.encode(chat.strAsk, StandardCharsets.UTF_8);
                        String strURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + strEncodedAsk;
                        String strJsonReply = doGet(strURL);
                        //Log.info(m_strLogPrefix+strAsk);
                        //Log.info(m_strLogPrefix+strEncodedAsk);
                        String strFormattedContent = "";

                        if (m_nDebug == 1) {
                            Log.info(strJsonReply);
                        }
                        //String json = "{\"2\":\"efg\",\"1\":\"abc\"}";
                        //JSONObject json_test = JSONObject.fromObject(strReply);
                        final JSONObject jsonResult = new JSONObject(strJsonReply);

                        //Log.info(m_strLogPrefix+jsonResult.getInt("result"));
                        //Log.info(m_strLogPrefix+jsonResult.getString("content"));
                        if (0 == jsonResult.getInt("result")) {
                            //Groups.player.find();
                            String strContent = jsonResult.getString("content");
                            strFormattedContent = strContent.replace("{br}", "\n");

                            //Time.run(1, () -> player.sendMessage(strMsgPrefix + strFormattedContent));
                            //text = text + "\n" + strMsgPrefix + strFormattedContent;
                        } else {
                            Log.info(m_strLogPrefix + " error getting message.");
                        }
                        chat.strReply = strFormattedContent;
                        Log.info("adding chat reply "+chat);
                        synchronized (m_ListReply) {
                            Log.info("adding chat reply2 "+chat);
                            m_ListReply.add(chat);
                        }
                    }

                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO: handle exception
                    }
                    if( !Vars.net.active())
                    {
                        Log.info("Stop MTDChatEngine Thread");
                        System.out.println("Stop MTDChatEngine Thread");
                        break;
                    }
                }
            }
}
