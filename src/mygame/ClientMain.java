/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.RenderManager;
import com.jme3.system.JmeContext;
import java.io.IOException;

/**
 *
 * @author ThomasLeScolan
 */
public class ClientMain extends SimpleApplication implements ClientStateListener{
    private Client myClient;
    
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
