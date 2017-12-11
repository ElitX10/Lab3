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
import java.util.ArrayList;
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
            
            // TODO : try send less message :
            DiskPositions();
            DiskPosMessage diskPosUpdate = new DiskPosMessage(X_Disks,Y_Disks,XSpeed_Disks,
                                                              YSpeed_Disks);
            myServer.broadcast(diskPosUpdate);
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
        if (!game.isEnabled()){
            
            if (index < 10){
                PlayerStore.remove(index);
            }
        }else {
            //check if the player was in the game (and that he is not a player that have been kiked)
            if(index < 10){
                indexOfLeaver.add(index);
                // don't allow the leaver to win :
                PlayerStore.get(index).setScore(-999);
            }
        }              
    }
    
    private void removeInGameLeaver(){
        for (int i = 0; i < indexOfLeaver.size(); i++){
            int j = indexOfLeaver.get(i);
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
                        // turn on the game :
                        ServerMain.this.game.setEnabled(true);
                        // set id for players :
                        ServerMain.this.setNewID();
                        // assigne position for each player : 
                        ServerMain.this.setRandomPosition();
                        // reset the array size : 
                        ServerMain.this.TAB_POS_PLAYER_LENGTH = 8;
                        // send player informations :
        //                System.out.println(X_Player[0]);
                        PlayerPosMessage initPos = new PlayerPosMessage(X_Player, Y_Player, Host_Player);
                        myServer.broadcast(initPos);

                        // testing : 
                        for (int i =0; i < PlayerStore.size(); i++){
                            System.out.println("player : " + PlayerStore.get(i).id + " controlled by : " + PlayerStore.get(i).getHost());
                        }
                        
                        //send a message to start the game for all clients :
                        StartGameMessage turnGameOn = new StartGameMessage();
                        myServer.broadcast(turnGameOn);
                        return true;
                    }
                });
                
                
            }
        }        
    }
    
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
    
}

//-------------------------------------------------SERVER_PLAYER--------------------------------------------------------------------------------------------------------------------------------------------------------

class ServerPlayer extends Player {
    private final int HOST;
    
    public ServerPlayer(float X_pos, float Y_pos, SimpleApplication app, Node NodeGame, int host) {
        super(X_pos, Y_pos, app, NodeGame);
        this.HOST = host;
    }  
    
    public int getHost(){
        return this.HOST;
    }
    
    public void setID(int newID){
        this.id = newID;
    }    
}