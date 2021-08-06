package MykesTool;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;
import java.io.*;
import java.nio.charset.Charset;
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
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
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
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
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

            connection.disconnect();// 关闭远程连接
        }

        return result;
    }

    private String m_strLogPrefix = "[MykesTool:ChatBot]";
    //called when game initializes
    @Override
    public void init(){
        /*
        //listen for a block selection event
        Events.on(BuildSelectEvent.class, event -> {
            if(!event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer()){
                //player is the unit controller
                Player player = event.builder.getPlayer();

                //send a message to everyone saying that this player has begun building a reactor
                Call.sendMessage("[scarlet]ALERT![] " + player.name + " has begun building a reactor at " + event.tile.x + ", " + event.tile.y);
            }
        });
        */

        //add a chat filter that changes the contents of all messages
        //in this case, all instances of "heck" are censored
        Vars.netServer.admins.addChatFilter((player, text) -> {
                int nBotNamePos = -1;
                String strCallName = "@myke";
                nBotNamePos = text.indexOf(strCallName);
                Log.info(text);
                Log.info(nBotNamePos);
                if( nBotNamePos == 0)
                {
                    String strAsk = text.substring(strCallName.length());
                    String strEncodedAsk = java.net.URLEncoder.encode(strAsk, Charset.forName("utf-8"));
                    String strURL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg="+strEncodedAsk;
                    String strReply = doGet(strURL);
                    //Log.info(m_strLogPrefix+strAsk);
                    //Log.info(m_strLogPrefix+strEncodedAsk);

                    Log.info(strReply);
                    //String json = "{\"2\":\"efg\",\"1\":\"abc\"}";
                    //JSONObject json_test = JSONObject.fromObject(strReply);
                        final JSONObject jsonResult = new JSONObject(strReply);

                        //Log.info(m_strLogPrefix+jsonResult.getInt("result"));
                        //Log.info(m_strLogPrefix+jsonResult.getString("content"));
                        if (0 == jsonResult.getInt("result")) {
                            //Groups.player.find();
                            player.sendMessage(jsonResult.getString("content"));
                        } else {
                            Log.info(m_strLogPrefix+" error getting message.");
                        }
                }
            //player.sendMessage("try to do something for v008");
            return text;
        });
        //Vars.netServer.admins.addChatFilter((player, text) -> text.replace("111", "****"));

        /*
        //add an action filter for preventing players from doing certain things
        Vars.netServer.admins.addActionFilter(action -> {
            //random example: prevent blast compound depositing
            if(action.type == ActionType.depositItem && action.item == Items.blastCompound && action.tile.block() instanceof CoreBlock){
                action.player.sendMessage("Example action filter: Prevents players from depositing blast compound into the core.");
                return false;
            }
            return true;
        });

         */
    }

    //register commands that run on the server
    /* // sample
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("reloadwordmask", "Reload word mask in config/DirtyWords.txt.", args -> {
            for(int x = 0; x < Vars.world.width(); x++){
                for(int y = 0; y < Vars.world.height(); y++){
                    //loop through and log all found reactors
                    //make sure to only log reactor centers
                    if(Vars.world.tile(x, y).block() == Blocks.thoriumReactor && Vars.world.tile(x, y).isCenter()){
                        Log.info("Reactor at @, @", x, y);
                    }
                }
            }
        });
    }
    */

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("reloadwordmask", "Reload word mask in config/DirtyWords.txt.", args -> {
            //this.reloadWords();
        });
    }

    /*
    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){

        //register a simple reply command
        handler.<Player>register("reply", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
            player.sendMessage("You said: [accent] " + args[0]);
        });

        //register a whisper command which can be used to send other players messages
        handler.<Player>register("whisper", "<player> <text...>", "Whisper text to another player.", (args, player) -> {
            //find player by name
            Player other = Groups.player.find(p -> p.name.equalsIgnoreCase(args[0]));

            //give error message with scarlet-colored text if player isn't found
            if(other == null){
                player.sendMessage("[scarlet]No player by that name found!");
                return;
            }

            //send the other player a message, using [lightgray] for gray text color and [] to reset color
            other.sendMessage("[lightgray](whisper) " + player.name + ":[] " + args[1]);
        });
    }

     */
}
