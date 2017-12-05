/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import java.io.IOException;
//import mygame.Globals.GameBoard;

/**
 *
 * @author ThomasLeScolan
 */
public class ClientMain extends SimpleApplication implements ClientStateListener{
    private Client myClient;
    private Globals myGlobals = new Globals();
    private final Node NODE_GAME = new Node("NODE_GAME");
//    private Ask ask = new Ask();
    private Game game = new Game(this,NODE_GAME);
    
    public ClientMain(){
//        ask.setEnabled(true);
        game.setEnabled(true);
        stateManager.attach(game);
//        stateManager.attach(ask);        
    }
    
    public static void main(String[] args) {
        ClientMain app = new ClientMain();
        app.start(JmeContext.Type.Display);
    }

    @Override
    public void simpleInitApp() {        
        // create and start the client :
        try {
            myClient = Network.connectToServer(Globals.NAME, Globals.VERSION, Globals.DEFAULT_SERVER, Globals.DEFAULT_PORT);
            myClient.start();
        } catch (IOException ex) { }
        
        // add client listener :
        myClient.addClientStateListener(this);
        
        // unable camera mvt with mouse : 
        flyCam.setEnabled(false); 
        
        //node containing all the other new node on the game :
        rootNode.attachChild(NODE_GAME);
    }
    
    public Globals getMyGlobals(){
        return myGlobals;
    }
    
    public Node getGameNode(){
        return NODE_GAME;
    }    
    
    // to ensure to close the net connection cleanly :
    @Override
    public void destroy() {
        try {
            myClient.close();
        } catch (Exception ex) { }
        super.destroy();
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        // camera parameter
        cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void clientConnected(Client client) {
        System.out.println("Client #" + client.getId() + " is ready."); 
    }

    @Override
    public void clientDisconnected(Client client, DisconnectInfo info) {
        System.out.println("Client #" + client.getId() + " has left.");

    }
    
    public class ClientListener implements MessageListener<Client> {

        @Override
        public void messageReceived(Client source, Message m) {
            
        }
    }

}