package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.serializer.ServerSerializerRegistrationsService;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.Globals.*;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class ServerMain extends SimpleApplication implements ConnectionListener{
    private Server myServer;   
    private final Node NODE_GAME = new Node("NODE_GAME");
    private Game game = new Game(this,NODE_GAME);
    
    public ServerMain(){
        game.setEnabled(false);
        stateManager.attach(game);
    }
    
    public static void main(String[] args) {
        ServerMain app = new ServerMain();
        Globals.initialiseSerializables();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {        
        // create and start the server :
        try {
            myServer = Network.createServer(Globals.NAME, Globals.VERSION, Globals.DEFAULT_PORT, Globals.DEFAULT_PORT);
            myServer.getServices().removeService(myServer.getServices().getService(ServerSerializerRegistrationsService.class));
            myServer.start();
        } catch (IOException ex) { }
        
        // add connection Listener :
        myServer.addConnectionListener(this);
        
        // add message listenter : 
        myServer.addMessageListener(new ServerListener(),
                                    TimeMessage.class,
                                    StartGameMessage.class);
    }
    
    // to ensure to close the net connection cleanly :
    @Override
    public void destroy() {
        try {
            myServer.close();
        } catch (Exception ex) { }
        super.destroy();
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void connectionAdded(Server server, HostedConnection client) {
        System.out.println("Server knows that client #" + client.getId() + " is ready.");
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection client) {
        System.out.println("Server knows that client #" + client.getId() + " has left.");
    }
    
    public class ServerListener implements MessageListener<HostedConnection>{

        @Override
        public void messageReceived(HostedConnection source, Message m) {
            if (m instanceof TimeMessage){
//                System.out.println("ask for time");
//                TimeMessage giveTime = new TimeMessage(38);
//                myServer.broadcast(giveTime);
            }else if (m instanceof StartGameMessage){
                System.out.println("ask for starting a new game");
                Future result = ServerMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ServerMain.this.game.setEnabled(true);
                        return true;
                    }
                });
                
                // send player informations :
                
                //send a message to start the game for all clients :
                StartGameMessage turnGameOn = new StartGameMessage();
                myServer.broadcast(turnGameOn);
            }
        }        
    }
}
