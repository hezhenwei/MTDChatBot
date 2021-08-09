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

    private String AskAndGetReplyContent(String strAsk)
    {
        String strEncodedAsk = URLEncoder.encode(strAsk, StandardCharsets.UTF_8);
        String strURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + strEncodedAsk;
        String strJsonReply = doGet(strURL);
        //Log.info(m_strLogPrefix+strAsk);
        //Log.info(m_strLogPrefix+strEncodedAsk);
        String strFormattedContent = "";

        if( m_nDebug == 1) {
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
        return strFormattedContent;
    }

    private String m_strLogPrefix = "[MykesTool:ChatBot]";
    private String m_strBotName = "myke";
    private String m_strMsgPrefix = "[red][[[yellow]"+m_strBotName+"的小仆ff[red]]:[white] ";
    private NetClient m_nc;
    private Player m_playerNew;
    private int m_nDebug = 0;
    private int m_nLessPeopleActive = 1;
    private long m_nLastChatTime = 0;
    private void DelayReply(Player player, String strMsg) {

        int nBotNamePos = -1;
        String strCallName = "@" + m_strBotName;
        nBotNamePos = strMsg.indexOf(strCallName);

        //Log.info(text);
        //Log.info(nBotNamePos);
        if (nBotNamePos == 0) {
            String strAsk = strMsg.substring(strCallName.length());
            String strFormattedReply = AskAndGetReplyContent(strAsk);
            Call.sendMessage(m_strMsgPrefix + strFormattedReply); // say to all
            Log.info(m_strMsgPrefix + strFormattedReply); // log this so we can trace back

            m_nLastChatTime = System.currentTimeMillis( );
        } // if has "@myke" prefix
        else  if( Groups.player.size()<= m_nLessPeopleActive)
        {
            String strAsk = strMsg;
            String strFormattedReply = AskAndGetReplyContent(strAsk);
            Call.sendMessage(m_strMsgPrefix + strFormattedReply); // say to all
            Log.info(m_strMsgPrefix + strFormattedReply); // log this so we can trace back
            m_nLastChatTime = System.currentTimeMillis( );

        } else if(0 == strMsg.indexOf("无聊") || 0 == strMsg.indexOf("有点无聊") || 0 == strMsg.indexOf("好无聊")) {

            String strAsk = strMsg;
            String strFormattedReply = AskAndGetReplyContent(strAsk);
            Call.sendMessage(m_strMsgPrefix + strFormattedReply); // say to all
            Log.info(m_strMsgPrefix + strFormattedReply); // log this so we can trace back
            m_nLastChatTime = System.currentTimeMillis( );
        }
        else {
            // otherwise, only see what he will reply, but not show them.
            String strAsk = strMsg.substring(strCallName.length());
            String strFormattedReply = AskAndGetReplyContent(strAsk);
            //Call.sendMessage(m_strMsgPrefix + strFormattedReply); // say to all
            Log.info(m_strMsgPrefix + strFormattedReply); // log this so we can trace back

            //m_nLastChatTime = System.currentTimeMillis( );

        }
    }

    String[] m_ArrayPossibleStart = {"天气如何？", "讲个笑话", "最近有什么新闻","."};
    private void StartChat(Player player)
    {
        //int nPlayer = Mathf.random(Groups.player.size()-1);
        //Player player = Groups.player.index(nPlayer);
        int nStartMsg = Mathf.random(m_ArrayPossibleStart.length -1);
        String strStartMsg = m_ArrayPossibleStart[nStartMsg];
        Call.sendMessage(m_strMsgPrefix + "一个人的话，可以和机器人对话："+strStartMsg); // say to all
        DelayReply(player, strStartMsg);
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

        Events.on(PlayerLeave.class, event -> {
            if(m_nDebug==1)
            {
                Log.info("[Myke's ChatBot] Player Left. player "+Groups.player.size()+":"+m_nLessPeopleActive);
            }
            // when a player leave and some one still in....
            if( Groups.player.size() <= m_nLessPeopleActive && Groups.player.size() > 0)
            {
                // not * 60 is 1 sec
                int nPlayer = Mathf.random(Groups.player.size()-1);
                Player player = Groups.player.index(nPlayer);
                Time.run(Mathf.random(10) * 60f, () -> StartChat(player));
            }
        });

        /*
        Vars.netServer.admins.addActionFilter(action -> {

            // when user do any action
            long nNow = System.currentTimeMillis( );
            long nDiff = nNow - m_nLastChatTime;
            //Log.info("ndeiff." +  nDiff);
            if( nDiff > (120+Mathf.random(480)) * 1000)
            {
                if( Groups.player.size() <= m_nLessPeopleActive && Groups.player.size() > 0) {
                    m_nLastChatTime = System.currentTimeMillis( );

                    Time.run(Mathf.random(10) * 60f, () -> StartChat(action.player));
                }
            }
            return true;
        });
        */

    }


    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("cbdebug", "turn on/off debug info.", args -> {
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
        handler.register("cbnact", "allow limit less then n peoplez.", args -> {
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
