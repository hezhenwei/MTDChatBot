package MykesTool;

import arc.*;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.NetClient;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.net.Net;
import mindustry.net.NetConnection;
import mindustry.world.blocks.storage.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;

public class MTDChatBotPlugin extends Plugin{


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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if( connection!= null) {
                connection.disconnect();// 关闭远程连接
            }
        }

        return result;
    }

    private String m_strLogPrefix = "[MykesTool:ChatBot]";
    private String m_strBotName = "myke";
    private NetClient m_nc;
    private Player m_playerNew;
    private int m_nDebug = 0;
    private int m_nLessPeopleActive = 1;
    private long m_nLastChatTime = 0;
    private void DelayReply(Player player, String strMsg){

        int nBotNamePos = -1;
        String strCallName = "@" + m_strBotName;
        String strMsgPrefix = "[red][[[yellow]"+m_strBotName+"的小仆ff[red]]:[white] ";
        nBotNamePos = strMsg.indexOf(strCallName);
        //Log.info(text);
        //Log.info(nBotNamePos);
        if (nBotNamePos == 0) {
            String strAsk = strMsg.substring(strCallName.length());
            String strEncodedAsk = URLEncoder.encode(strAsk, StandardCharsets.UTF_8);
            String strURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + strEncodedAsk;
            String strReply = doGet(strURL);
            //Log.info(m_strLogPrefix+strAsk);
            //Log.info(m_strLogPrefix+strEncodedAsk);

            if( m_nDebug == 1) {
                Log.info(strReply);
            }
            //String json = "{\"2\":\"efg\",\"1\":\"abc\"}";
            //JSONObject json_test = JSONObject.fromObject(strReply);
            final JSONObject jsonResult = new JSONObject(strReply);

            //Log.info(m_strLogPrefix+jsonResult.getInt("result"));
            //Log.info(m_strLogPrefix+jsonResult.getString("content"));
            if (0 == jsonResult.getInt("result")) {
                //Groups.player.find();
                String strContent = jsonResult.getString("content");
                String strFormattedContent = strContent.replace("{br}", "\n");
                //player.sendMessage(strMsgPrefix + strFormattedContent); // say to asker.
                Call.sendMessage(strMsgPrefix + strFormattedContent); // say to all
                Log.info(strMsgPrefix + strFormattedContent); // log this so we can trace back

                //Time.run(1, () -> player.sendMessage(strMsgPrefix + strFormattedContent));
                //text = text + "\n" + strMsgPrefix + strFormattedContent;
            } else {
                Log.info(m_strLogPrefix + " error getting message.");
            }
            m_nLastChatTime = System.currentTimeMillis( );
        } // if has "@myke" prefix
        else
        {
            if( Groups.player.size()<= m_nLessPeopleActive)
            {
                String strAsk = strMsg;
                String strEncodedAsk = URLEncoder.encode(strAsk, StandardCharsets.UTF_8);
                String strURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + strEncodedAsk;
                String strReply = doGet(strURL);
                //Log.info(m_strLogPrefix+strAsk);
                //Log.info(m_strLogPrefix+strEncodedAsk);

                if( m_nDebug == 1) {
                    Log.info(strReply);
                }
                //String json = "{\"2\":\"efg\",\"1\":\"abc\"}";
                //JSONObject json_test = JSONObject.fromObject(strReply);
                final JSONObject jsonResult = new JSONObject(strReply);

                //Log.info(m_strLogPrefix+jsonResult.getInt("result"));
                //Log.info(m_strLogPrefix+jsonResult.getString("content"));
                if (0 == jsonResult.getInt("result")) {
                    //Groups.player.find();
                    String strContent = jsonResult.getString("content");
                    String strFormattedContent = strContent.replace("{br}", "\n");
                    //player.sendMessage(strMsgPrefix + strFormattedContent); // say to asker.
                    Call.sendMessage(strMsgPrefix + strFormattedContent); // say to all
                    Log.info(strMsgPrefix + strFormattedContent); // log this so we can trace back

                    //Time.run(1, () -> player.sendMessage(strMsgPrefix + strFormattedContent));
                    //text = text + "\n" + strMsgPrefix + strFormattedContent;
                } else {
                    Log.info(m_strLogPrefix + " error getting message.");
                }
                m_nLastChatTime = System.currentTimeMillis( );

            }

        }
    }


    //called when game initializes
    @Override
    public void init() {


        //add a chat filter that changes the contents of all messages
        //in this case, all instances of "heck" are censored
        Vars.netServer.admins.addChatFilter((player, text) -> {

            /*
            if(m_playerNew == null || m_nc == null) {
                m_playerNew = Player.create();
                m_playerNew.name = m_strBotName;
                m_playerNew.admin = true;
                m_playerNew.id = 123;
                m_playerNew.id(123);
                Groups.player.add(m_playerNew);

                //m_pNew.locale = packet.locale;
                m_playerNew.color.set(Color.yellow);
                Vars.netServer.sendWorldData(player);
            }

            player.sendMessage("test", m_playerNew);
            */

            Time.run(0, () -> DelayReply(player,text));

            //player.sendMessage("try to do something for v008");
            return text;
        });

        //listen for a user join event
        Events.on(PlayerJoin.class, event -> {
            if(m_nDebug==1)
            {
                Log.info("[Myke's ChatBot] Player joined. player "+Groups.player.size()+":"+m_nLessPeopleActive);
            }
            if( Groups.player.size() <= m_nLessPeopleActive)
            {
                // not * 60 is 1 sec
                // if has very few people, say hi hiddenly
                Time.run(Mathf.random(10) * 60f, () -> DelayReply(event.player, "hi"));
            }

        });

        Vars.netServer.admins.addActionFilter(action -> {

            // when user do any action
            long nNow = System.currentTimeMillis( );
            long nDiff = nNow - m_nLastChatTime;
            //Log.info("ndeiff." +  nDiff);
            if( nDiff > (30+Mathf.random(30)) * 1000)
            {
                if( Groups.player.size() <= m_nLessPeopleActive && Groups.player.size() > 0) {
                    m_nLastChatTime = System.currentTimeMillis( );
                    int nPlayer = Mathf.random(Groups.player.size()-1);
                    Player player = Groups.player.index(nPlayer);
                    Time.run(Mathf.random(10) * 60f, () -> DelayReply(player, "随便说点什么吧。"));
                }
            }
            return true;
        });


    }


    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("chatbotdebug", "turn on/off debug info.", args -> {
            if( m_nDebug == 0) {
                m_nDebug = 1;
                Log.info("ChatBotDebugInfo turned on");
            }
            else
            {
                m_nDebug = 0;
                Log.info("ChatBotDebugInfo turned off");

            }
            /*
            // test what is time.run // * 60 means a sec
            Time.run(0, () -> Call.sendMessage("call in 0"));
            Time.run(10, () -> Call.sendMessage("call in 10"));
            Time.run(100, () -> Call.sendMessage("call in 100"));
            Time.run(1000, () -> Call.sendMessage("call in 1000"));
            */

        });
        handler.register("chatbotLessPeopleActive", "allow limit.", args -> {
            if( args.length >= 1) {
                m_nLessPeopleActive = Integer.getInteger(args[0]);
                Log.info("LessPeopleActive set to " + m_nLessPeopleActive);
            }
            else
            {
                Log.info("No args. Guess you want to toggle. ");
                if( m_nLessPeopleActive == 0)
                {
                    m_nLessPeopleActive = 2;
                    Log.info("Toggle LessPeopleActive to " + m_nLessPeopleActive);

                }else
                {
                    m_nLessPeopleActive = 0;
                    Log.info("Toggle LessPeopleActive to " + m_nLessPeopleActive);

                }
            }

        });
    }

}
