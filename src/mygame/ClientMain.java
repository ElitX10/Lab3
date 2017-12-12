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
        } catch (IOException ex) {
            this.destroy();
            this.stop();
        }
        
        // add client listener :
        myClient.addClientStateListener(this);
        
        // add message listenter :
        myClient.addMessageListener(new ClientListener(),
                                    TimeMessage.class,
                                    StartGameMessage.class,
                                    PlayerPosMessage.class,
                                    EndGameMessage.class,
                                    DiskPosMessage.class,
                                    PositivDiskUpdateMessage.class,
                                    ScoreMessage.class);
        
        // unable camera mvt with mouse : 
        flyCam.setEnabled(false); 
        
        // outside the window or we give the focus to another window
        setPauseOnLostFocus(false);
        
        //node containing all the other new node on the game :
        rootNode.attachChild(NODE_GAME);
        
        setDisplayStatView(false);
        setDisplayFps(false); 
        
        game.setEnabled(false);
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P)); // enable calls
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addListener(actionListener, "Restart", "Exit");
        ask.setEnabled(true);
    }
    
    public Globals getMyGlobals(){
        return myGlobals;
    }
    
    public Node getGameNode(){
        return NODE_GAME;
    } 
    
    public Client getMyClient(){
        return myClient;
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
                        ClientMain.this.game.addClientPlayerToList(ClientMain.this.PlayerStore);
                        return true;
                    }
                });
            }else if (m instanceof PlayerPosMessage){
                final PlayerPosMessage playerPos = (PlayerPosMessage) m;
                final int myHost = source.getId();
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        if (PlayerStore.isEmpty()){
                            float[] X_Pos = playerPos.getX();
                            float[] Y_Pos = playerPos.getY();
                            int[] Hosts = playerPos.getHosts();
                            for (int i = 0; i < X_Pos.length; i ++){
                                ClientPlayer player;                            
                                if (Hosts[i] == myHost){                                
                                    player = new ClientPlayer(X_Pos[i], Y_Pos[i], ClientMain.this, NODE_GAME, true, myClient);
                                }else{
                                    player = new ClientPlayer(X_Pos[i], Y_Pos[i], ClientMain.this, NODE_GAME, false, myClient);
                                }
                                player.setEnabled(true);
                                ClientMain.this.getStateManager().attach(player); 
                                ClientMain.this.PlayerStore.add(player);
                            }

//                            // send ack :
//                            PlayerPosMessage ack = new PlayerPosMessage();
//                            ClientMain.this.myClient.send(ack);
                        }
                        
                        return true;
                    }
                });
            }else if (m instanceof EndGameMessage){
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ClientMain.this.game.setEnabled(false);
                        ClientMain.this.ask.setEnabled(true);
                        // disable all players :
                        for(int i = 0 ; i < PlayerStore.size();i++){
                            PlayerStore.get(i).setEnabled(false);
                        }
                        ClientMain.this.PlayerStore.clear();
                        Player.resetPlayerNumber();
                        ClientMain.this.inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P)); // enable calls
                        ClientMain.this.inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
                        ClientMain.this.inputManager.addListener(actionListener, "Restart", "Exit");
                        return true;
                    }
                });
            }else if(m instanceof DiskPosMessage){
                final DiskPosMessage DiskPos = (DiskPosMessage) m;
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        float[] X_Pos = DiskPos.getX();
                        float[] Y_Pos = DiskPos.getY();
                        float[] X_Speed = DiskPos.getX_SPEED();
                        float[] Y_Speed = DiskPos.getY_SPEED();

                        for (int i = 0; i < X_Pos.length; i ++){                    
                            if (i < game.getDiskStore().size()){                                
                                ClientMain.this.game.getDiskStore().get(i).setXPos(X_Pos[i]);
                                ClientMain.this.game.getDiskStore().get(i).setYPos(Y_Pos[i]);
                                ClientMain.this.game.getDiskStore().get(i).setXSpeed(X_Speed[i]);                                
                                ClientMain.this.game.getDiskStore().get(i).setYSpeed(Y_Speed[i]);
                            }
                        }
                        return true;
                    }
                });
            }else if(m instanceof PositivDiskUpdateMessage){
                final PositivDiskUpdateMessage diskUpdateMess = (PositivDiskUpdateMessage) m;
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        PDisk diskToUpdate = (PDisk) ClientMain.this.game.getDiskStore().get(diskUpdateMess.getIndex());
                        diskToUpdate.updateDisk(diskUpdateMess.getPoint());
                        return true;
                    }
                });
            }else if(m instanceof ScoreMessage){
                final ScoreMessage updateMess = (ScoreMessage) m;
                Future result = ClientMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        int id = updateMess.getId();
                        int newScore = updateMess.getPoint();
                        PlayerStore.get(id - 1).setScore(newScore);
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
    private final KeyTrigger RIGHT = new KeyTrigger(KeyInput.KEY_D);
    private final KeyTrigger LEFT = new KeyTrigger(KeyInput.KEY_Q);
    private final SimpleApplication myApp;
    private final Client myClient;
    
    public ClientPlayer(float X_pos, float Y_pos, SimpleApplication app, Node NodeGame, boolean isControl, Client Client) {
        super(X_pos, Y_pos, app, NodeGame);
        this.keyTrigger = isControl;
        myApp = app;
        myClient = Client;
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
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
        if (keyTrigger){
            myApp.getInputManager().deleteMapping("UP");
            myApp.getInputManager().deleteMapping("DOWN");
            myApp.getInputManager().deleteMapping("LEFT");
            myApp.getInputManager().deleteMapping("RIGHT"); 
        }
    }
    
    private AnalogListener analogListener = new AnalogListener() {
        float timePressed = 0;
        final float delay = 0.15f;
        float X_Pressed = 0;
        float Y_Pressed = 0;
        
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isEnabled()){
                timePressed += tpf;
                // add velocity when input are pressed :
                if (name.equals("UP")){
                    Y_SPEED += SPEED_ACCELERATION * tpf;
                    Y_Pressed += tpf;
                }
                if (name.equals("DOWN")){
                    Y_SPEED -= SPEED_ACCELERATION * tpf; 
                    Y_Pressed -= tpf;
                }
                if (name.equals("LEFT")){
                    X_SPEED -= SPEED_ACCELERATION * tpf;
                    X_Pressed -= tpf;
                }
                if (name.equals("RIGHT")){
                    X_SPEED += SPEED_ACCELERATION * tpf;
                    X_Pressed += tpf;
                }
            }
            sendInput();
        }
        
        private void sendInput(){
            if(timePressed >= delay){
                // send message :
                InputMessage input = new InputMessage(X_Pressed, Y_Pressed);
                myClient.send(input);
                // reset values :
                timePressed = 0;
                X_Pressed = 0;
                Y_Pressed = 0;
            }
        }
    };
    
    
    
}
