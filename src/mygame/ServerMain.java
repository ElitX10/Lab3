package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
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
import java.util.ArrayList;
import java.util.Collections;
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
    private final Game game = new Game(this,NODE_GAME);
    
    // list containing all players :
    private ArrayList<ServerPlayer> PlayerStore = new ArrayList<ServerPlayer>();
    
    // list containing the index (in PlayerStore) of players that leave during the game :
    private final ArrayList<Integer> indexOfLeaver = new ArrayList<Integer>();
    
    // posible position for player disk :
    private final float POS_PLAYER[][] = {{-Game.POSNEG_BETWEEN_COORD,Game.POSNEG_BETWEEN_COORD},
                                        {0,Game.POSNEG_BETWEEN_COORD},
                                        {Game.POSNEG_BETWEEN_COORD,Game.POSNEG_BETWEEN_COORD},
                                        {-Game.POSNEG_BETWEEN_COORD,0},
                                        {0,0},
                                        {Game.POSNEG_BETWEEN_COORD,0},
                                        {-Game.POSNEG_BETWEEN_COORD,-Game.POSNEG_BETWEEN_COORD},
                                        {0,-Game.POSNEG_BETWEEN_COORD},
                                        {Game.POSNEG_BETWEEN_COORD,-Game.POSNEG_BETWEEN_COORD}};
    private int TAB_POS_PLAYER_LENGTH = 8; // size of the prvious tab is decreased every time we add a player (to avoid 2 player on 1 start position) 

    // array with players position :
    private float X_Player[];
    private float Y_Player[];
    private int Host_Player[];
    
    //array with all disks' position :
    private float X_Disks[];
    private float Y_Disks[];
    private float XSpeed_Disks[];
    private float YSpeed_Disks[];
    
    private final int sendTimeDelay = 3;
    private float timeDelay = 0;
    
    private final float sendGlobalUpdateDelay = 0.01f;
    private float GlobalUpdateDelay = 0;
    
    private final int sendScoreAndPositivStateDelay = 1;
    private float ScoreAndPositivStateDelay = 0;
    
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
                                    StartGameMessage.class,
                                    InputMessage.class/*,
        PlayerPosMessage.class*/);
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
        
        // send time information : 
        if (game.isEnabled()){
            // send time to clients :
            timeDelay += tpf;
            if (timeDelay >= sendTimeDelay){
                TimeMessage tMess = new TimeMessage(game.getTime());
                myServer.broadcast(tMess);
                timeDelay = 0 ;
            } 
            // end of the game :
            if(game.getTime() == 0){
                game.setEnabled(false);
                EndGameMessage endMess = new EndGameMessage();
                myServer.broadcast(endMess);
                Player.resetPlayerNumber();
                this.removeInGameLeaver();
            }            
            GlobalUpdateDelay += tpf;           
            if (GlobalUpdateDelay >= sendGlobalUpdateDelay){
                DiskPositions();
                DiskPosMessage diskPosUpdate = new DiskPosMessage(X_Disks,Y_Disks,XSpeed_Disks,
                                                                  YSpeed_Disks);
                myServer.broadcast(diskPosUpdate);
                GlobalUpdateDelay = 0;
            }
            
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void connectionAdded(Server server, HostedConnection client) {
        System.out.println("Server knows that client #" + client.getId() + " is ready.");
        // kick players if the game is running, otherwise create a new player disk
        if (game.isEnabled() || myServer.getConnections().size() > 9){
            client.close("Game is running or full ! Please come later ;)");
        } else {
            //TODO : ADD NEW PLAYER HERE :
            PlayerStore.add(new ServerPlayer(0, 0, this, NODE_GAME, client.getId()));
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection client) {
        System.out.println("Server knows that client #" + client.getId() + " has left.");
        int index = getIndexOfPlayer(client.getId());
        System.out.println("index in list : "+index);
        if (!game.isEnabled()){
            
            if (index < 10){
                PlayerStore.remove(index);
            }
        }else {
            //check if the player was in the game (and that he is not a player that have been kiked)
            if(index < 10){
                indexOfLeaver.add(index);
                Collections.sort(indexOfLeaver);
                // don't allow the leaver to win :
                PlayerStore.get(index).setScore(-999);
            }
        }              
    }
    
    private void removeInGameLeaver(){
        System.out.println("size : " + indexOfLeaver.size());
        for (int i = indexOfLeaver.size() - 1; i >= 0; i--){
            int j = indexOfLeaver.get(i);
            System.out.println("j : " + j);
            PlayerStore.remove(j);
        }
        indexOfLeaver.clear();
    }
    
    // return the index in PlayerStore of the player corresponding to a host :
    public int getIndexOfPlayer(int clientHost){
        for (int i =0; i < PlayerStore.size(); i++){
            if(PlayerStore.get(i).getHost() == clientHost){
                return i;
            }
        }        
        return 10;
    }

    private void resetPlayerScore() {
        for (int i =0; i < PlayerStore.size(); i++){
            PlayerStore.get(i).setScore(0);
//            PlayerStore.get(i).resetState();
        }
    }
    
    public class ServerListener implements MessageListener<HostedConnection>{

        @Override
        public void messageReceived(HostedConnection source, Message m) {
            final HostedConnection mySource = source;
            if (m instanceof TimeMessage){
//                System.out.println("ask for time");
//                TimeMessage giveTime = new TimeMessage(38);
//                myServer.broadcast(giveTime);
            }else if (m instanceof StartGameMessage){
                System.out.println("ask for starting a new game");
                
                Future result = ServerMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {                        
                        // turn on the game :
                        ServerMain.this.game.setEnabled(true);
                        // add players to the list of disks in game :
                        game.addServerPlayerToList(PlayerStore);
                        // set id for players :
                        ServerMain.this.setNewID();
                        // reset score : 
                        ServerMain.this.resetPlayerScore();
                        // assigne position for each player : 
                        ServerMain.this.setRandomPosition();
                        // enable all players :
                        for(int i =0; i < PlayerStore.size(); i++){
                            PlayerStore.get(i).setEnabled(true);
                            stateManager.attach(PlayerStore.get(i));
                        }
                        // reset the array size : 
                        ServerMain.this.TAB_POS_PLAYER_LENGTH = 8;
                        // send player informations :
        //                System.out.println(X_Player[0]);
                        PlayerPosMessage initPos = new PlayerPosMessage(X_Player, Y_Player, Host_Player);
                        myServer.broadcast(initPos);

//                        // testing : 
//                        for (int i =0; i < PlayerStore.size(); i++){
//                            System.out.println("player : " + PlayerStore.get(i).id + " controlled by : " + PlayerStore.get(i).getHost());
//                        }  
                        //send a message to start the game for all clients :
                        StartGameMessage turnGameOn = new StartGameMessage();
                        myServer.broadcast(turnGameOn);
                        
//                        ServerMain.this.checkInitialSetUp(initPos);
                        return true;
                    }
                });                 
            }else if(m instanceof InputMessage){
                final InputMessage input = (InputMessage) m;
                Future result = ServerMain.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        int index = getIndexOfPlayer(mySource.getId());
                        PlayerStore.get(index).newInput(input.getXTimer(), input.getYTimer());
//                        System.out.println("player : " + PlayerStore.get(index).id + " X : " + PlayerStore.get(index).getXPos() + " Y : " + PlayerStore.get(index).getYPos());                        
                        return true;
                    }
                });
            }//else if(m instanceof PlayerPosMessage){
//                Future result = ServerMain.this.enqueue(new Callable() {
//                    @Override
//                    public Object call() throws Exception {
//                        // server know that the client is ready
//                        PlayerStore.get(getIndexOfPlayer(mySource.getId())).playerIsReady();                        
//                        return true;
//                    }
//                });
//            }
        }        
    }
    
//    private void checkInitialSetUp(PlayerPosMessage mess){
//        final PlayerPosMessage reSendMess = mess;
//        try {
//            Thread.sleep(40);
//        }catch(InterruptedException ex){
//            Thread.currentThread().interrupt();
//        }
//        Future result = ServerMain.this.enqueue(new Callable() {
//            @Override
//            public Object call() throws Exception {                
//                for (int i =0; i < PlayerStore.size(); i++){
//                    if (!PlayerStore.get(i).getState()){
//                        myServer.broadcast(Filters.in(myServer.getConnection(PlayerStore.get(i).getHost())) ,reSendMess);
//                        checkInitialSetUp(reSendMess);
//                        System.out.println("mess resend");
//                    }                    
//                }
//
//                return true;
//            }
//        });
//    }
    
    // set new id for all player :
    private void setNewID(){
        for (int i = 0; i < PlayerStore.size(); i++){
            PlayerStore.get(i).setID(i + 1);
        }
    }
    
    private void setRandomPosition(){
        float[] X_Pos = new float[PlayerStore.size()];
        float[] Y_Pos = new float[PlayerStore.size()];
        int[] Hosts = new int[PlayerStore.size()];
        
        for (int i = 0; i<PlayerStore.size();i++ ){            
            // random index :
            int randomIndex = (int)(Math.random() * (TAB_POS_PLAYER_LENGTH + 1));
            
            // set the position for players :
            PlayerStore.get(i).setXPos(POS_PLAYER[randomIndex][0]);
            PlayerStore.get(i).setYPos(POS_PLAYER[randomIndex][1]);
            
            //TODO : also store the position in an array for sending : 
            X_Pos[i] = POS_PLAYER[randomIndex][0];
            Y_Pos[i] = POS_PLAYER[randomIndex][1];
            Hosts[i] = PlayerStore.get(i).getHost();
            
            // update the array with all posible position : 
            float sav[] = {POS_PLAYER[TAB_POS_PLAYER_LENGTH][0],POS_PLAYER[TAB_POS_PLAYER_LENGTH][1]};
            POS_PLAYER[TAB_POS_PLAYER_LENGTH][0] = POS_PLAYER[randomIndex][0];
            POS_PLAYER[TAB_POS_PLAYER_LENGTH][1] = POS_PLAYER[randomIndex][1];
            POS_PLAYER[randomIndex][0] = sav[0];
            POS_PLAYER[randomIndex][1] = sav[1];
            TAB_POS_PLAYER_LENGTH --;
        }
        X_Player = X_Pos;
        Y_Player = Y_Pos;
        Host_Player = Hosts;
    }
    
    private void DiskPositions(){
        int TotalDisksNum = game.getDiskStore().size() + PlayerStore.size();
        float[] X_Pos = new float[TotalDisksNum];
        float[] Y_Pos = new float[TotalDisksNum];
        float[] X_Speed = new float[TotalDisksNum];
        float[] Y_Speed = new float[TotalDisksNum];
        
        int playerIndex = 0;
        
        for (int i = 0; i < TotalDisksNum; i++){
            if(i < game.getDiskStore().size()){
                X_Pos[i] = game.getDiskStore().get(i).getXPos();
                Y_Pos[i] = game.getDiskStore().get(i).getYPos();
                X_Speed[i] = game.getDiskStore().get(i).getXSpeed();
                Y_Speed[i] = game.getDiskStore().get(i).getYSpeed();
            } else {
                X_Pos[i] = PlayerStore.get(playerIndex).getXPos();
                Y_Pos[i] = PlayerStore.get(playerIndex).getYPos();
                X_Speed[i] = PlayerStore.get(playerIndex).getXSpeed();
                Y_Speed[i] = PlayerStore.get(playerIndex).getYSpeed();
            }
        }
        
        X_Disks = X_Pos;
        Y_Disks = Y_Pos;
        XSpeed_Disks = X_Speed;
        YSpeed_Disks = Y_Speed;
    }
    
    public Server getMyServer(){
        return myServer;
    }
}

//-------------------------------------------------SERVER_PLAYER--------------------------------------------------------------------------------------------------------------------------------------------------------

class ServerPlayer extends Player {
    private final int HOST;
//    private boolean isReady = false;
    
    public ServerPlayer(float X_pos, float Y_pos, SimpleApplication app, Node NodeGame, int host) {
        super(X_pos, Y_pos, app, NodeGame);
        this.HOST = host;
    } 
    @Override
    protected void initialize(Application app) {
        super.initialize(app);
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    
    public int getHost(){
        return this.HOST;
    }
    
//    public void playerIsReady(){
//        isReady = true;
////        System.out.println("player " + this.id + " is ready !");
//    }
//    
//    public void resetState(){
//        isReady = false;
//    }
    
    public void setID(int newID){
        this.id = newID;
    } 
    
    public void newInput(float X, float Y){
        X_SPEED += SPEED_ACCELERATION * X;
        Y_SPEED += SPEED_ACCELERATION * Y;
        //System.out.println("X : " + X_SPEED + " Y : " + Y_SPEED);
    }

//    boolean getState() {
//        return isReady;
//    }
}