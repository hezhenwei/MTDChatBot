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

    private String m_strLogPrefix = "[MykesTool:ChatBot]";
    private String m_strBotName = "myke";
    private String m_strMsgPrefix = "[red][[[yellow]"+m_strBotName+"的小仆ff[red]]:[white] ";
    private NetClient m_nc;
    private Player m_playerNew;
    private int m_nDebug = 0;
    private int m_nLessPeopleActive = 1;
    private long m_nLastChatTime = 0;
    private MTDChatEngineThread m_threadEngine = new MTDChatEngineThread();

    private void GetAndDisplayReply()
    {
        MTDChatEngineThread.ChatReply chat = m_threadEngine.GetNextReply();
        if( chat != null)
        {
            if( chat.nHideReply == 0) {
                Call.sendMessage(m_strMsgPrefix + chat.strReply); // say to all
            }
            Log.info(m_strMsgPrefix + chat.strReply); // log this so we can trace back

            m_nLastChatTime = System.currentTimeMillis();
        }
    }

    private void AskAsync(Player player, String strMsg) {

        try {
            int nBotNamePos = -1;
            String strCallName = "@" + m_strBotName;
            nBotNamePos = strMsg.indexOf(strCallName);

            //Log.info(text);
            //Log.info(nBotNamePos);
            if (nBotNamePos == 0) {
                String strAsk = strMsg.substring(strCallName.length());
                m_threadEngine.AskAsync(player, strAsk, 0);
            } // if has "@myke" prefix
            else if (Groups.player.size() <= m_nLessPeopleActive) {
                String strAsk = strMsg;
                m_threadEngine.AskAsync(player, strAsk, 0);

            } else if (0 == strMsg.indexOf("无聊") || 0 == strMsg.indexOf("有点无聊") || 0 == strMsg.indexOf("好无聊")) {

                String strAsk = strMsg;
                m_threadEngine.AskAsync(player, strAsk, 0);
            } else {
                // otherwise, only see what he will reply, but not show them.
                String strAsk = strMsg;
                m_threadEngine.AskAsync(player, strAsk, 1);
            }
            Time.runTask(5*60f, () -> GetAndDisplayReply());
        }catch ( Exception e)
        {
            e.printStackTrace();
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
        AskAsync(player, strStartMsg);
    }

    //called when game initializes
    @Override
    public void init() {

        //add a chat filter that changes the contents of all messages
        //in this case, all instances of "heck" are censored
        Vars.netServer.admins.addChatFilter((player, text) -> {

            // if has some reply messages, show them.
            GetAndDisplayReply();
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

            AskAsync(player,text);

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
                AskAsync(event.player, "hi");
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
                StartChat(player);
            }
        });


        Vars.netServer.admins.addActionFilter(action -> {
            // get and show it anytime.
            // seems it make the server slow.
            // GetAndDisplayReply();
            return true;
        });

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