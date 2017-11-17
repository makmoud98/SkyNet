import java.io.UnsupportedEncodingException;
import java.util.Properties;
//

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.URL;
import java.io.IOException;
import java.net.UnknownHostException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.GameRegistry;
 
@Mod(modid="skynet", name="SkyNet", version="1.1.0")
public class SkyNet {
	
	Minecraft mc = Minecraft.getMinecraft();
    boolean isEnabled = true;
    KeyBinding toggle;
    int time = 0;
    List<String> previousPlayerList = new ArrayList();
    List<String> playersToMonitor = new ArrayList();
    Socket socket;
    PrintWriter out;
    String ip;
    int port;
   
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)  {
        time = 0;
        ip = "makvps1.mooo.com";
        port = 7001;
        MinecraftForge.EVENT_BUS.register(this);
        toggle = new KeyBinding("Toggle SkyNet", Keyboard.KEY_I, "SkyNet");
        ClientRegistry.registerKeyBinding(toggle);    
    }
   
    public String filterChatColors(String s) {
        return TextFormatting.getTextWithoutFormattingCodes(s);
    }
   
    public void onPlayerLeave(String player) {
        String output = player + " has left the server";
        mc.thePlayer.addChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + output));
        if(out == null)
            return;
        for(int i = 0; i < playersToMonitor.size(); i++)
        {
            String monitoring = playersToMonitor.get(i);
            if(monitoring.toLowerCase().equals(player.toLowerCase())){
                out.println("l;"+player);
                break;
            }
        }
    }
   
    public void onPlayerJoin(String player) {
    	String output = player + " has joined the server";
        if(out == null)
            return;
        for(int i = 0; i < playersToMonitor.size(); i++)
        {
            String monitoring = playersToMonitor.get(i);
            if(monitoring.toLowerCase().equals(player.toLowerCase())){
                out.println("j;"+player);
                break;
            }
        }
    }
 
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            if(time == 100) {
                if(mc.theWorld != null) {    
                ArrayList<String> playerList = new ArrayList();
                Collection<NetworkPlayerInfo> players = mc.getConnection().getPlayerInfoMap();
                for(NetworkPlayerInfo info : players) {
                    playerList.add(filterChatColors(info.getGameProfile().getName()));
                }
                ArrayList<String> temp = (ArrayList)playerList.clone();
                playerList.removeAll(previousPlayerList);
                previousPlayerList.removeAll(temp);
                for(String player : previousPlayerList) {
                    onPlayerLeave(player);
                }
                for(String player : playerList) {
                    onPlayerJoin(player);
                }
                previousPlayerList = temp;
                }
            }
            else{
                time++;
            }
        }

    }
   
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(toggle.isKeyDown()){
            connect();
        }              
    }

    @SubscribeEvent
    public void rightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        TileEntity te = evt.getWorld().getTileEntity(evt.getPos());
        if (te instanceof TileEntitySign)
        {
            String line1 = ((TileEntitySign)te).signText[0].getUnformattedText();
            String line2 = ((TileEntitySign)te).signText[1].getUnformattedText();
            if(line1.equals("test") && !line2.equals(""))
            {
                mc.thePlayer.addChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + "monitoring " + line2));
                playersToMonitor.add(line2);
            }
        }
    }

    public void connect()
    {
        try{
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            URL url = new URL("http://makvps1.mooo.com/watchlist.txt");
            Scanner s = new Scanner(url.openStream());
            playersToMonitor.clear();
            for(int i = 0; i < playersToMonitor.size(); i++){
                String p = s.nextLine();
                if(!p.equals("")) 
                    playersToMonitor.add(p);
            }
            mc.thePlayer.addChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + "syncing watchlist + connecting to tcp server"));
        }
        catch (UnknownHostException e1) {
            mc.thePlayer.addChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + "error(1) connecting to tcp server"));
        }   
        catch (IOException e1) {
            mc.thePlayer.addChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + "error(2) connecting to tcp server"));
        }
    }

    public void send_email(String text)
    {
	    final String username = "*****@gmail.com";
	    final String password = "*****";
	
	    Properties props = new Properties();
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.host", "smtp.gmail.com");
	    props.put("mail.smtp.port", "587");
	
	    Session session = Session.getInstance(props,
	      new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(username, password);
	        }
	      });
	
	    try {
	
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress("******@gmail.com"));
	        message.setRecipients(Message.RecipientType.TO,
	            InternetAddress.parse("*******@gmail.com"));
	        message.setSubject(text);
	        message.setText("");
	
	        Transport.send(message);
	
	        System.out.println("Done");
	
	    } catch (MessagingException e) {
	        throw new RuntimeException(e);
	    }
    }
}
