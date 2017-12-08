/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
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
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.Globals.*;

/**
 *
 * @author ThomasLeScolan
 */
public class ClientMain extends SimpleApplication implements ClientStateListener{
    private Client myClient;
    private Globals myGlobals = new Globals();
    private final Node NODE_GAME = new Node("NODE_GAME");
    private Ask ask = new Ask();
    private Game game = new Game(this,NODE_GAME);
    private boolean running = true;
    private float time; 
    private boolean start = true;
    
    // list containing all players :
    private ArrayList<ClientPlayer> PlayerStore = new ArrayList<ClientPlayer>();
    
    public ClientMain(){
        ask.setEnabled(true);
        game.setEnabled(false);
        stateManager.attach(game);
        stateManager.attach(ask);        
    }
    
    public static void main(String[] args) {
        ClientMain app = new ClientMain();
        Globals.initialiseSerializables();
        app.start(/*JmeContext.Type.Display*/);
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
        
        // add message listenter :
        myClient.addMessageListener(new ClientListener(),
                                    TimeMessage.class,
                                    StartGameMessage.class,
                                    PlayerPosMessage.class);
        
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
        
        // time :
        if (running) {
            // get the time :
            time = game.getTime();
//            System.out.println(time);
            if (time <= 0f || start) {
                game.setEnabled(false);
                inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P)); // enable calls
                inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
                inputManager.addListener(actionListener, "Restart", "Exit");
                ask.setEnabled(true);
                running = false;
                start = false;
            }
        }
        
        if (game.isEnabled()){
            String text = "";
            for(int i = 0; i < PlayerStore.size(); i++){
                text += "\nPlayer " + PlayerStore.get(i).getID() + " : " + PlayerStore.get(i).POINT;
            }
            game.setScoreHUD(text);  
        }
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void clientConnected(Client client) {
        System.out.println("Client #" + client.getId() + " is ready.");
//        TimeMessage timeMess = new TimeMessage();
//        myClient.send(timeMess);
    }

    @Override
    public void clientDisconnected(Client client, DisconnectInfo info) {
        System.out.println("Client #" + client.getId() + " has left.");
        System.out.println(info);
        ClientMain.this.stop();
    }
    
    public class ClientListener implements MessageListener<Client> {

        @Override
        public void messageReceived(Client source, Message m) {
            if (m instanceof TimeMessage){
                final TimeMessage tMess = (TimeMessage) m;
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ClientMain.this.game.setTime(tMess.getTime());
                        return true;
                    }
                }); 
//                TimeMessage TMess = (TimeMessage) m ; 
//                System.out.println("Time is : " + TMess.getTime());
            }else if (m instanceof StartGameMessage){
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ClientMain.this.ask.setEnabled(false);
                        ClientMain.this.game.setEnabled(true);
                        ClientMain.this.running = true;
                        ClientMain.this.inputManager.deleteMapping("Restart");
                        ClientMain.this.inputManager.deleteMapping("Exit");
                        return true;
                    }
                });
            }else if (m instanceof PlayerPosMessage){
                final PlayerPosMessage playerPos = (PlayerPosMessage) m;
                final int myHost = source.getId();
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        float[] X_Pos = playerPos.getX();
                        float[] Y_Pos = playerPos.getY();
                        int[] Hosts = playerPos.getHosts();
                        for (int i = 0; i < X_Pos.length; i ++){
                            ClientPlayer player;                            
                            if (Hosts[i] == myHost){                                
                                player = new ClientPlayer(X_Pos[i], Y_Pos[i], ClientMain.this, NODE_GAME, true);
                            }else{
                                player = new ClientPlayer(X_Pos[i], Y_Pos[i], ClientMain.this, NODE_GAME, false);
                            }
                            player.setEnabled(true);
                            ClientMain.this.getStateManager().attach(player); 
                            PlayerStore.add(player);
                        }
                        return true;
                    }
                });
            }
        }
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) { // on the key being pressed...
                if (name.equals("Exit")) {
                    ClientMain.this.stop(); //terminate jMonkeyEngine app
                    // System.exit(0) would also work 
                } else if (name.equals("Restart")) {
                    //SEND MESSAGE TO THE SERVER HERE !
                    StartGameMessage newGame = new StartGameMessage();
                    myClient.send(newGame);                    
                }
            }
        }
    };
    
}


//-------------------------------------------------ASK--------------------------------------------------------------------------------------------------------------------------------------------------------

class Ask extends BaseAppState {
    private SimpleApplication sapp;

    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        // create a text in the form of a bitmap, and add it to the GUI pane : 
        BitmapFont myFont = sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        BitmapText hudText = new BitmapText(myFont, false);
        hudText.setSize(myFont.getCharSet().getRenderedSize() * 5);
        hudText.setColor(ColorRGBA.Red);
        hudText.setText("PRESS P TO \nSTART A NEW\n GAME AND \nE TO EXIT");
        hudText.setLocalTranslation(5, 250, 0);
        sapp.getGuiNode().attachChild(hudText);
    }

    @Override
    protected void onDisable() {
        sapp.getGuiNode().detachAllChildren();
    }
}

//-------------------------------------------------CLIENT_PLAYER--------------------------------------------------------------------------------------------------------------------------------------------------------

class ClientPlayer extends Player{
    private boolean keyTrigger;
    private final KeyTrigger UP = new KeyTrigger(KeyInput.KEY_Z);
    private final KeyTrigger DOWN = new KeyTrigger(KeyInput.KEY_S);
    private final KeyTrigger RIGHT = new KeyTrigger(KeyInput.KEY_Q);
    private final KeyTrigger LEFT = new KeyTrigger(KeyInput.KEY_D);
    
    public ClientPlayer(float X_pos, float Y_pos, SimpleApplication app, Node NodeGame, boolean isControl) {
        super(X_pos, Y_pos, app, NodeGame);
        this.keyTrigger = isControl;
    }
    @Override
    protected void initialize(Application app) {
        super.initialize(app);
        if (keyTrigger){
            app.getInputManager().addMapping("UP", this.UP);
            app.getInputManager().addMapping("DOWN", this.DOWN);
            app.getInputManager().addMapping("LEFT", this.LEFT);
            app.getInputManager().addMapping("RIGHT", this.RIGHT);
            app.getInputManager().addListener(analogListener, "UP", "DOWN", "LEFT","RIGHT");
        }
    }
    
    private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isEnabled()){
                // add velocity when input are pressed :
                if (name.equals("UP")){
//                    Y_SPEED += SPEED_ACCELERATION * tpf;
                }
                if (name.equals("DOWN")){
//                    Y_SPEED -= SPEED_ACCELERATION * tpf; 
                }
                if (name.equals("LEFT")){
//                    X_SPEED -= SPEED_ACCELERATION * tpf;
                }
                if (name.equals("RIGHT")){
//                    X_SPEED += SPEED_ACCELERATION * tpf;
                }
            }            
        }
    };
}
