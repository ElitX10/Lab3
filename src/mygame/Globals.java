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
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author ThomasLeScolan
 */
public class Globals {
    // variable for setting the server and the clients :
    public static final String NAME = "Lab3";
    public static final String DEFAULT_SERVER = "localhost";
    public static final int VERSION = 1;
    public static final int DEFAULT_PORT = 6143;
    
// register all message types there are
    public static void initialiseSerializables() {
        Serializer.registerClass(TimeMessage.class);
        Serializer.registerClass(StartGameMessage.class);
    }   
    
    // abstract message :
    public static abstract class MyAbstractMessage extends AbstractMessage{
    
    } 
    
    // time message :
    @Serializable
    public static class TimeMessage extends MyAbstractMessage{
        private float TIME;
        
        public TimeMessage() {
        }
        
        public TimeMessage(float time){
            this.TIME = time;
        }        
        
        public float getTime(){
            return TIME;
        }
    }
    
    @Serializable
    public static class StartGameMessage extends MyAbstractMessage{
        public StartGameMessage(){
            
        }
    }
    
}

//-------------------------------------------------GAME--------------------------------------------------------------------------------------------------------------------------------------------------------

class Game extends BaseAppState {
    // player number
    private final int NUMBER_OF_PLAYER = 3;
    
    // time informations : 
    private float TIME = 30f;
    DecimalFormat df = new DecimalFormat("0.0 s"); //time format
    private BitmapText timeAndScore;
    
    // thickness of the sides of the frame
    static final float FRAME_THICKNESS = 24f; 
    // width (and height) of the free area inside the frame, where disks move
    static final float FREE_AREA_WIDTH = 492f; 
    // total outer width (and height) of the frame
    static final float FRAME_SIZE = FREE_AREA_WIDTH + 2f * FRAME_THICKNESS; 

    // next three constants define initial positions for disks
    static final float PLAYER_COORD = FREE_AREA_WIDTH / 6;
    static final float POSNEG_MAX_COORD = FREE_AREA_WIDTH / 3;
    static final float POSNEG_BETWEEN_COORD = PLAYER_COORD;
    
    // radius :
    static final float PLAYER_R = 20f; // radius of a player's disk
    static final float POSDISK_R = 16f; // radius of a positive disk
    static final float NEGDISK_R = 16f; // radius of a negative disk
    
    // posible position for disk + / - :
    private final float POS_TAB[] = {-POSNEG_MAX_COORD, -POSNEG_BETWEEN_COORD, 0, POSNEG_BETWEEN_COORD, POSNEG_MAX_COORD};
    
    // posible position for player disk :
//    private final float POS_PLAYER[][] = {{-POSNEG_BETWEEN_COORD,POSNEG_BETWEEN_COORD},{0,POSNEG_BETWEEN_COORD},{POSNEG_BETWEEN_COORD,POSNEG_BETWEEN_COORD},{-POSNEG_BETWEEN_COORD,0},{0,0},{POSNEG_BETWEEN_COORD,0},{-POSNEG_BETWEEN_COORD,-POSNEG_BETWEEN_COORD},{0,-POSNEG_BETWEEN_COORD},{POSNEG_BETWEEN_COORD,-POSNEG_BETWEEN_COORD}};
//    private int TAB_POS_PLAYER_LENGTH = 8; // size of the prvious tab is decreased every time we add a player (to avoid 2 player on 1 start position) 
    
//    // Player control for 3 players :
//    private final KeyTrigger PLAYER_KEY[][] = {{new KeyTrigger(KeyInput.KEY_I), new KeyTrigger(KeyInput.KEY_K), new KeyTrigger(KeyInput.KEY_L), new KeyTrigger(KeyInput.KEY_J)},
//                                        {new KeyTrigger(KeyInput.KEY_Z), new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_Q)},
//                                        {new KeyTrigger(KeyInput.KEY_T), new KeyTrigger(KeyInput.KEY_G), new KeyTrigger(KeyInput.KEY_H), new KeyTrigger(KeyInput.KEY_F)}};
    
    // list containing all the disks :
    private ArrayList<Disk> diskStore = new ArrayList<Disk>();
    
    private final SimpleApplication myApp;
    private final Node NODE_GAME;
    private boolean needCleaning = false;
    
    public Game(SimpleApplication app, Node gameNode){
        myApp = app;
        NODE_GAME = gameNode;
    }
    
    public ArrayList<Disk> getDiskStore(){
        return diskStore;
    }
    
    @Override
    protected void initialize(Application app) {
//        myApp = (Main) app;
        
//        //light :        
//        DirectionalLight sun = new DirectionalLight();
//        sun.setColor(ColorRGBA.White);
//        sun.setDirection(new Vector3f(0, 0, -1));
//        myApp.getRootNode().addLight(sun);
//        final int SHADOWMAP_SIZE=1024;
//        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(myApp.getAssetManager(), SHADOWMAP_SIZE, 3);
//        dlsr.setLight(sun);
//        dlsr.setShadowIntensity(0f);
//        myApp.getViewPort().addProcessor(dlsr);
//        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(myApp.getAssetManager(), SHADOWMAP_SIZE, 3);
//        dlsf.setLight(sun);
//        dlsf.setEnabled(true);
//        FilterPostProcessor fpp = new FilterPostProcessor(myApp.getAssetManager());
//        fpp.addFilter(dlsf);
//        myApp.getViewPort().addProcessor(fpp);       
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        System.out.println("game : on");
        if (needCleaning) {
            // detach all disk from the game when enabled :
            NODE_GAME.detachAllChildren();
            
            //reset variables :
//            TAB_POS_PLAYER_LENGTH = 8;
            TIME = 30f;
            needCleaning = false;            
        }
        
        //create the gameboard
        GameBoard gb = new GameBoard();
        NODE_GAME.attachChild(gb.getGameBoard());                 
        
        // create all neg and pos disk :
        boolean isNeg = true;
        for(int i=0; i<5 ;i++){
            for (int j=0; j<5 ;j++){
                if(!((i>0 && i<4)&&(j>0 && j<4))){
                    if (isNeg){
                        NDisk disk = new NDisk(POS_TAB[i], POS_TAB[j], myApp, NODE_GAME);
                        disk.setEnabled(true); 
                        myApp.getStateManager().attach(disk);
                        diskStore.add(disk);
                    }else{
                        PDisk disk = new PDisk(POS_TAB[i], POS_TAB[j], myApp, NODE_GAME); 
                        disk.setEnabled(true); 
                        myApp.getStateManager().attach(disk);
                        diskStore.add(disk);
                    }
                }
                isNeg = !isNeg;                
            }     
        }
        
//        // create players :
//        for (int i = 0; i < NUMBER_OF_PLAYER ;i++){
//            this.newPlayer(i);
//        } 
        
        // create the HUD
        initHUD();
    }

//    private void newPlayer(int i){
//        // random index :
//        int randomIndex = (int)(Math.random() * (TAB_POS_PLAYER_LENGTH + 1));
//        
//        // new player in a random position :
//        Player player = new Player(POS_PLAYER[randomIndex][0], POS_PLAYER[randomIndex][1], PLAYER_KEY[i][0], PLAYER_KEY[i][1], PLAYER_KEY[i][2], PLAYER_KEY[i][3]);
//        player.setEnabled(true);
//        myApp.getStateManager().attach(player); 
//        diskStore.add(player);
//        
//        //update the array with the position :
//        float sav[] = {POS_PLAYER[TAB_POS_PLAYER_LENGTH][0],POS_PLAYER[TAB_POS_PLAYER_LENGTH][1]};
//        POS_PLAYER[TAB_POS_PLAYER_LENGTH][0] = POS_PLAYER[randomIndex][0];
//        POS_PLAYER[TAB_POS_PLAYER_LENGTH][1] = POS_PLAYER[randomIndex][1];
//        POS_PLAYER[randomIndex][0] = sav[0];
//        POS_PLAYER[randomIndex][1] = sav[1];
//        TAB_POS_PLAYER_LENGTH --;
//    }
    
    private void initHUD(){
        BitmapFont myFont = myApp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");                        
        timeAndScore = new BitmapText(myFont, false);
        timeAndScore.setSize(myFont.getCharSet().getRenderedSize() * 4);
        timeAndScore.setColor(ColorRGBA.White);
        timeAndScore.setText("TIME : " + TIME);
        timeAndScore.setLocalTranslation(5, 700, 0);
        myApp.getGuiNode().attachChild(timeAndScore); 
    }
    
    @Override
    protected void onDisable() {
        needCleaning = true;
        
        //enable all disk :
//        for(Disk d: diskStore){
//            d.setEnabled(false);
//        }
//        diskStore.clear();
    } 
    
    @Override
    public void update(float tpf) {  
        // collision between disks :
        for(int i = 0; i < diskStore.size(); i++){
            for(int other = i + 1; other < diskStore.size(); other++){
                diskStore.get(i).diskCollision(diskStore.get(other), tpf);
            }            
        }
        
        // time update :
        TIME -= tpf;
        if (TIME <= 0){
            TIME = 0;
        }
        
        // hud update :
//        String text = "TIME : " + df.format(TIME);
//        for(int i = 16; i < 16 + NUMBER_OF_PLAYER; i++){
//            text += "\nPlayer " + diskStore.get(i).getID() + " : " + diskStore.get(i).POINT;
//        }
//        timeAndScore.setText(text);
    }    
    
    public float getTime(){
        return TIME;
    }    
    
    class GameBoard {
        private final Node NODE_GAME_BOARD = new Node("NODE_GAME_BOARD"); 
        protected float BOX_SIZE_FREE_AREA;
        protected float BOX_SIZE_FRAME;
        protected float BOX_THICKNESS_FRAME;
        
        GameBoard(){
            this.BOX_THICKNESS_FRAME = FRAME_THICKNESS/2;
            this.BOX_SIZE_FREE_AREA = FREE_AREA_WIDTH/2;
            this.BOX_SIZE_FRAME = FRAME_SIZE/2;
            
            //create the floor :
            Box ground = new Box(BOX_SIZE_FREE_AREA,BOX_SIZE_FREE_AREA,1);
            Geometry geom_ground = new Geometry("geom_ground", ground);
            Material mat_ground = new Material(myApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat_ground.setColor("Color", ColorRGBA.White);
            geom_ground.setMaterial(mat_ground);  
            NODE_GAME_BOARD.attachChild(geom_ground);
            NODE_GAME_BOARD.setLocalTranslation(0, 0, -BOX_THICKNESS_FRAME*2);
            
            //create the frame :                        
            Box frame_part1 = new Box(BOX_SIZE_FRAME,BOX_THICKNESS_FRAME,BOX_THICKNESS_FRAME);
            Box frame_part2 = new Box(BOX_SIZE_FRAME,BOX_THICKNESS_FRAME,BOX_THICKNESS_FRAME);
            Box frame_part3 = new Box(BOX_THICKNESS_FRAME,BOX_SIZE_FRAME,BOX_THICKNESS_FRAME);
            Box frame_part4 = new Box(BOX_THICKNESS_FRAME,BOX_SIZE_FRAME,BOX_THICKNESS_FRAME);
            Geometry geom_frame_part1 = new Geometry("geom_frame_part1", frame_part1); 
            Geometry geom_frame_part2 = new Geometry("geom_frame_part1", frame_part2);
            Geometry geom_frame_part3 = new Geometry("geom_frame_part1", frame_part3);
            Geometry geom_frame_part4 = new Geometry("geom_frame_part1", frame_part4);
            Material mat_frame = new Material(myApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat_frame.setColor("Color", ColorRGBA.Brown);
            geom_frame_part1.setMaterial(mat_frame);
            geom_frame_part2.setMaterial(mat_frame);
            geom_frame_part3.setMaterial(mat_frame);
            geom_frame_part4.setMaterial(mat_frame);
            geom_frame_part1.setLocalTranslation(0, -BOX_SIZE_FREE_AREA - BOX_THICKNESS_FRAME, BOX_THICKNESS_FRAME);
            geom_frame_part2.setLocalTranslation(0,BOX_SIZE_FREE_AREA + BOX_THICKNESS_FRAME, BOX_THICKNESS_FRAME);
            geom_frame_part3.setLocalTranslation(BOX_SIZE_FREE_AREA + BOX_THICKNESS_FRAME, 0, BOX_THICKNESS_FRAME);
            geom_frame_part4.setLocalTranslation(-BOX_SIZE_FREE_AREA - BOX_THICKNESS_FRAME, 0, BOX_THICKNESS_FRAME);
            NODE_GAME_BOARD.attachChild(geom_frame_part1);
            NODE_GAME_BOARD.attachChild(geom_frame_part2);
            NODE_GAME_BOARD.attachChild(geom_frame_part3);
            NODE_GAME_BOARD.attachChild(geom_frame_part4); 
            
            // allow shadow on the frame :
            geom_frame_part1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            geom_frame_part2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            geom_frame_part3.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            geom_frame_part4.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        }    
        
        public Node getGameBoard(){
            return NODE_GAME_BOARD;
        }
    }
}

//-------------------------------------------------DISK--------------------------------------------------------------------------------------------------------------------------------------------------------
abstract class Disk extends BaseAppState {
    private final float RADIUS;
    private float X_POS;
    private float Y_POS;
    private final float Z_POS;
    protected float X_SPEED;
    protected float Y_SPEED;
    private final float SIZE;
    private final double MASS;
    public String TYPE;    
    protected ColorRGBA COLOR = ColorRGBA.White;
    protected int POINT;
    protected Material mat_disk;
    protected final int INIT_SPEED_VALUE = 10;
    private final float FRICTION = 0.10f;
    
    // node containing the disk : 
    protected Node node_disk;
    
    // app :
    private final SimpleApplication myApp;
    
    // game node : 
    private final Node NODE_GAME;
        
    public Disk(String type, ColorRGBA color, float radius, float size, float X_pos, float Y_pos, SimpleApplication app, Node gameNode){
        myApp = app;
        NODE_GAME = gameNode;
        this.COLOR = color;
        this.RADIUS = radius;
        this.SIZE = size/2;
        this.X_POS = X_pos;
        this.Y_POS = Y_pos;
        this.TYPE = type;        
        this.Z_POS = -size/2;
        this.MASS = Math.PI * radius * radius;
    }
    
    @Override
    protected void initialize(Application app) {
        //create the cylinder :
//        myApp = (Main) app;
        Cylinder disk = new Cylinder(30, 30, RADIUS, SIZE, true);//30 sample
        Geometry geom_disk = new Geometry("geom_disk", disk);
        mat_disk = new Material(myApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat_disk.setColor("Color", COLOR);
        geom_disk.setMaterial(mat_disk);
        node_disk = new Node("node_disk");
        node_disk.attachChild(geom_disk);           
        node_disk.setLocalTranslation(X_POS, Y_POS, Z_POS); 
        NODE_GAME.attachChild(node_disk); 
    }  
    
    @Override
    protected void cleanup(Application app) {
        
    }
    
    @Override
    protected void onEnable() {
        NODE_GAME.attachChild(node_disk);
    }
    
    @Override
    protected void onDisable() {
        // stop disk when is disabled :
        this.X_SPEED = 0;
        this.Y_SPEED = 0;
    }
    
    @Override
    public void update(float tpf) {
        // update the position :
        this.X_POS += X_SPEED * tpf;
        this.Y_POS += Y_SPEED * tpf;   
         
        // move to the new position :
        node_disk.setLocalTranslation(X_POS, Y_POS , Z_POS);
        
        //simulate friction :
            this.X_SPEED -= this.X_SPEED * FRICTION * tpf;
            this.Y_SPEED -= this.Y_SPEED * FRICTION * tpf;  
        
        // check if there is a collision with the frame :    
        frameCollision();
    } 
    
    private void frameCollision(){
        // set position and new velocity to simulate collision with the frame (when collision is detected) :
        if(this.X_POS + this.RADIUS > Game.FREE_AREA_WIDTH/2){
            this.X_POS = Game.FREE_AREA_WIDTH/2 - this.RADIUS;
            this.X_SPEED = - this.X_SPEED;            
        }
        if(this.X_POS - this.RADIUS < - Game.FREE_AREA_WIDTH/2){
            this.X_POS = - Game.FREE_AREA_WIDTH/2 + this.RADIUS;
            this.X_SPEED = - this.X_SPEED; 
        }
        if(this.Y_POS + this.RADIUS > Game.FREE_AREA_WIDTH/2){
            this.Y_SPEED = - this.Y_SPEED;
            this.Y_POS = Game.FREE_AREA_WIDTH/2 - this.RADIUS;
        }
        if(this.Y_POS - this.RADIUS < - Game.FREE_AREA_WIDTH/2){
            this.Y_SPEED = - this.Y_SPEED;
            this.Y_POS = - Game.FREE_AREA_WIDTH/2 + this.RADIUS;
        }
    }
    
    public void diskCollision(Disk disk2, float tpf){
        if(this.isEnabled()){            
            // distance between the center of 2 disks :
            double distance = Math.sqrt((this.X_POS - disk2.X_POS) * (this.X_POS - disk2.X_POS) + (this.Y_POS - disk2.Y_POS) * (this.Y_POS - disk2.Y_POS));
            
            // check if there is an overlap :
            if (distance < this.RADIUS + disk2.RADIUS){                    
                // I) move backwards :
                
                // get value from the previous position (where it was no overlap and no collision) : time t(k-1) :
                float X1 = this.X_POS - this.X_SPEED * tpf;
                float Y1 = this.Y_POS - this.Y_SPEED * tpf;
                float VX1 = this.X_SPEED + this.X_SPEED * this.FRICTION * tpf; 
                float VY1 = this.Y_SPEED + this.Y_SPEED * this.FRICTION * tpf; 
                float X2 = disk2.X_POS - disk2.X_SPEED * tpf;
                float Y2 = disk2.Y_POS - disk2.Y_SPEED * tpf;
                float VX2 = disk2.X_SPEED + disk2.X_SPEED * disk2.FRICTION * tpf;
                float VY2 = disk2.Y_SPEED + disk2.Y_SPEED * disk2.FRICTION * tpf;
                
                // find the exact time of the collision (we need to solve an equation to find this time):
                // intermediate values : 
                float DX = X1 - X2;
                float DY = Y1 - Y2;
                float DVX = VX1 - VX2;
                float DVY = VY1 - VY2;
                
                // values to solve the equation : 
                float a = DVX * DVX + DVY * DVY;
                float b = 2 * (DX * DVX + DY * DVY);
                double c = (DX * DX) + (DY * DY) - (distance * distance);
                
                // solutions for the equation at²+bx+c = 0 :                  
                //double t1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a); not the good solution
                double t2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a); 
                
                //choose the right solution : 
                float collisionTime;
                if(t2 > 0 && t2 < tpf){
                    collisionTime = (float)t2;
                }else {
                    return;
                }

                // II) position and velocity where only 1 point of collision : time t :
                X1 = X1 + VX1 * collisionTime;
                Y1 = Y1 + VY1 * collisionTime;
                VX1 = VX1 - VX1 * this.FRICTION * collisionTime;
                VY1 = VY1 - VY1 * this.FRICTION * collisionTime;
                X2 = X2 + VX2 * collisionTime;
                Y2 = Y2 + VY2 * collisionTime;
                VX2 = VX2 - VX2 * disk2.FRICTION * collisionTime;
                VY2 = VY2 - VY2 * disk2.FRICTION * collisionTime; 
                
                //set the new position :
                this.node_disk.setLocalTranslation(X1, Y1 , this.Z_POS);
                disk2.node_disk.setLocalTranslation(X2 , Y2 , disk2.Z_POS);
                
                // III) compute new velocity (7 step):
                //step 1 :
                Vector3f un = new Vector3f(X2 - X1, Y2 - Y1, 0).normalize();
                Vector3f ut = new Vector3f(-un.y, un.x, 0);
                
                //step 2 :
                Vector3f v1 = new Vector3f(VX1, VY1, 0);
                Vector3f v2 = new Vector3f(VX2, VY2, 0); 
                
                //step 3 and step 4 :
                float v1n = un.dot(v1);
                float vp1t = ut.dot(v1);
                float v2n = un.dot(v2);
                float vp2t = ut.dot(v2);
                
                //step 5 : 
                double vp1n = (v1n * (this.MASS - disk2.MASS) + 2 * disk2.MASS * v2n) / (this.MASS + disk2.MASS);
                double vp2n = (v2n * (disk2.MASS - this.MASS) + 2 * this.MASS * v1n) / (this.MASS + disk2.MASS);
                
                //step 6 and step 7 :                    
                Vector3f vp1 = un.mult((float) vp1n).add(ut.mult(vp1t));
                Vector3f vp2 = un.mult((float) vp2n).add(ut.mult(vp2t));

                // IV) set position and velocity back at time t(k) :
                // set the new position :
                this.X_POS = X1 + vp1.x * (tpf - collisionTime);
                this.Y_POS = Y1 + vp1.y * (tpf - collisionTime);
                disk2.X_POS = X2 + vp2.x * (tpf - collisionTime);
                disk2.Y_POS = Y2 + vp2.y * (tpf - collisionTime);

                // set the new speed :
                this.X_SPEED = vp1.x - vp1.x * this.FRICTION * (tpf - collisionTime);
                this.Y_SPEED = vp1.y - vp1.y * this.FRICTION * (tpf - collisionTime);
                disk2.X_SPEED = vp2.x - vp2.x * disk2.FRICTION * (tpf - collisionTime);
                disk2.Y_SPEED = vp2.y - vp2.y * disk2.FRICTION * (tpf - collisionTime);

                // update the score :
                this.addToScore(disk2.reward(this));
                disk2.addToScore(this.reward(disk2));
            }            
        }       
    }
    
    // functions specific for each kind of disk :
    abstract public int reward(Disk d);
    abstract void addToScore(int points);
    abstract public int getID();
}

//-------------------------------------------------NEGATIV  DISK --------------------------------------------------------------------------------------------------------------------------------------------------------

class NDisk extends Disk {    
    
    NDisk(float X_pos, float Y_pos, SimpleApplication app, Node gameNode){
        super("N", ColorRGBA.Red, Game.NEGDISK_R, Game.FRAME_THICKNESS, X_pos, Y_pos, app, gameNode);
        if (app instanceof ServerMain){
            this.X_SPEED = -INIT_SPEED_VALUE + (float)Math.random()*2*INIT_SPEED_VALUE;
            this.Y_SPEED = -INIT_SPEED_VALUE + (float)Math.random()*2*INIT_SPEED_VALUE;
        } 
        this.POINT = -3;
    }
    
    @Override
    protected void initialize(Application app) {
        super.initialize(app);       
    }
    
    @Override
    protected void cleanup(Application app) {
        
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    @Override
    public int reward(Disk d) {
        return this.POINT;
    }

    @Override
    void addToScore(int points) {
        // no need to add point to the negativ disks
    }

    @Override
    public int getID() {
        return 0;
    }
}

//-------------------------------------------------POSITIV  DISK --------------------------------------------------------------------------------------------------------------------------------------------------------

class PDisk extends Disk {
    // possible color for a positiv disk : 
    private final ColorRGBA colorTab[] = {new ColorRGBA(0,0.2f,0, 256), new ColorRGBA(0,0.3f,0,256), new ColorRGBA(0,0.4f,0,256), new ColorRGBA(0,0.6f,0,256), new ColorRGBA(0,0.8f,0,256), new ColorRGBA(0,1,0,256)};
    
    // array containing 5 markers on the top of the disk :
    private final Geometry pointMarker[] = new Geometry[5];
    
    private final Application myApp;
    
    PDisk(float X_pos, float Y_pos, SimpleApplication app, Node gameNode){
        super("P", ColorRGBA.Green, Game.NEGDISK_R, Game.FRAME_THICKNESS, X_pos, Y_pos, app, gameNode);
        if (app instanceof ServerMain){
            this.X_SPEED = -INIT_SPEED_VALUE + (float)Math.random()*2*INIT_SPEED_VALUE;
            this.Y_SPEED = -INIT_SPEED_VALUE + (float)Math.random()*2*INIT_SPEED_VALUE;
        }
        this.POINT = 5;
        myApp = app;
    }
    
    
    @Override
    protected void initialize(Application app) {
//        simpleApp = (Main) app;
        super.initialize(app);
        
        // create 5 markers on the top of the disk : 
        Sphere point = new Sphere(30, 30, 2);
        for (int i = 0; i < 5; i++){            
            pointMarker[i] = new Geometry("geom_point", point);
            Material mat_point = new Material(myApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat_point.setColor("Color", ColorRGBA.White);
            pointMarker[i].setMaterial(mat_point);            
            super.node_disk.attachChild(pointMarker[i]);
            pointMarker[i].setLocalTranslation(5 * i - 10, 0, Game.FRAME_THICKNESS/2);
        }        
    }
    
    @Override
    protected void cleanup(Application app) {
        
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
    
    public void updateDisk(){
        // if there is still available point on the disk :
        if(this.POINT > 0 ){
            // decrease point for the next collision :
            this.POINT -= 1;
            
            // change the color :
            super.mat_disk.setColor("Color", colorTab[this.POINT]); 
            
            // delete one marker :
            super.node_disk.detachChild(pointMarker[this.POINT]);  
        }
    }

    @Override
    public int reward(Disk d) {
        int reward = this.POINT;
        
        // if it collide with a player disk then update the disk :
        if(d.TYPE.equals("X")){
            this.updateDisk();
        }
        
        // return the reward :
        return reward;        
    }

    @Override
    void addToScore(int points) {
        // no need to add point to the negativ disks
    }

    @Override
    public int getID() {
        return 0;
    }
}

//------------------------------------------------- PLAYER --------------------------------------------------------------------------------------------------------------------------------------------------------

abstract class Player extends Disk { 
    private int id;
    private final int SPEED_ACCELERATION = 60; 
    private static int playerNumber = 0;
    private SimpleApplication myApp;
      
    public Player(float X_pos, float Y_pos, SimpleApplication app, Node NodeGame){
        super("X", ColorRGBA.Blue, Game.PLAYER_R, Game.FRAME_THICKNESS, X_pos, Y_pos, app, NodeGame);
        this.X_SPEED = 0;
        this.Y_SPEED = 0;
        this.POINT = 0;
        
        // increase the number of player every time we create one player  
        playerNumber++;
        
        // give an id to every player to separate input later and for displaying the score on the sceen for each player :
        this.id = playerNumber;
    }
    
    @Override
    public int getID(){
        return this.id;
    }    
    
    @Override
    protected void initialize(Application app) {        
//        simpleApp = (Main) app;
        super.initialize(app);        
        
        // player marker on top of the disk :
        this.newPlayerMarker(this.id);//       
    }
    
    private void newPlayerMarker(int playerID){        
        Box box_id = new Box(1,Game.PLAYER_R/2,1);
        Material mat_id = new Material(myApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat_id.setColor("Color", ColorRGBA.Red);
        Node node_marker = new Node("node_marker");
        switch (playerID){
            case 1 :
                Geometry geom_id1 = new Geometry("geom_id", box_id);
                geom_id1.setMaterial(mat_id);
                node_marker.attachChild(geom_id1);
                break;
            case 2 :
                Geometry geom_id2 = new Geometry("geom_id", box_id);
                geom_id2.setMaterial(mat_id);
                node_marker.attachChild(geom_id2);
                Geometry geom2_id2 = new Geometry("geom_id", box_id);
                geom2_id2.setMaterial(mat_id);
                node_marker.attachChild(geom2_id2);
                geom_id2.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom2_id2.setLocalTranslation(-Game.PLAYER_R/4, 0, 0);
                break;
            case 3 :
                Geometry geom_id3 = new Geometry("geom_id", box_id);
                geom_id3.setMaterial(mat_id);
                node_marker.attachChild(geom_id3);
                Geometry geom2_id3 = new Geometry("geom_id", box_id);
                geom2_id3.setMaterial(mat_id);
                node_marker.attachChild(geom2_id3);
                Geometry geom3_id3 = new Geometry("geom_id", box_id);
                geom3_id3.setMaterial(mat_id);
                node_marker.attachChild(geom3_id3);
                geom_id3.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom2_id3.setLocalTranslation(-Game.PLAYER_R/4, 0, 0);
                break;
            case 4 :
                Geometry geom_id4 = new Geometry("geom_id", box_id);
                geom_id4.setMaterial(mat_id);
                node_marker.attachChild(geom_id4);
                Geometry geom2_id4 = new Geometry("geom_id", box_id);
                geom2_id4.setMaterial(mat_id);
                node_marker.attachChild(geom2_id4);
                Geometry geom3_id4 = new Geometry("geom_id", box_id);
                geom3_id4.setMaterial(mat_id);
                node_marker.attachChild(geom3_id4);
                geom_id4.setLocalTranslation(-Game.PLAYER_R/4, 0, 0);
                geom2_id4.setLocalTranslation(Game.PLAYER_R/16, 0, 0);
                geom3_id4.setLocalTranslation(4*Game.PLAYER_R/16, 0, 0);
                geom2_id4.rotate(0, 0, -25);
                geom3_id4.rotate(0, 0, 25);
                break;
            case 5 :
                Geometry geom_id5 = new Geometry("geom_id", box_id);
                geom_id5.setMaterial(mat_id);
                node_marker.attachChild(geom_id5);
                Geometry geom2_id5 = new Geometry("geom_id", box_id);
                geom2_id5.setMaterial(mat_id);
                node_marker.attachChild(geom2_id5);
                geom_id5.setLocalTranslation(-Game.PLAYER_R/16, 0, 0);
                geom2_id5.setLocalTranslation(2*Game.PLAYER_R/16, 0, 0);
                geom_id5.rotate(0, 0, -25);
                geom2_id5.rotate(0, 0, 25);
                break;
            case 6 :
                Geometry geom_id6 = new Geometry("geom_id", box_id);
                geom_id6.setMaterial(mat_id);
                node_marker.attachChild(geom_id6);
                Geometry geom2_id6 = new Geometry("geom_id", box_id);
                geom2_id6.setMaterial(mat_id);
                node_marker.attachChild(geom2_id6);
                Geometry geom3_id6 = new Geometry("geom_id", box_id);
                geom3_id6.setMaterial(mat_id);
                node_marker.attachChild(geom3_id6);
                geom_id6.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom2_id6.setLocalTranslation(-Game.PLAYER_R/16, 0, 0);
                geom3_id6.setLocalTranslation(-4*Game.PLAYER_R/16, 0, 0);
                geom2_id6.rotate(0, 0, 25);
                geom3_id6.rotate(0, 0, -25);
                break;
            case 7 :
                Geometry geom_id7 = new Geometry("geom_id", box_id);
                geom_id7.setMaterial(mat_id);
                node_marker.attachChild(geom_id7);
                Geometry geom2_id7= new Geometry("geom_id", box_id);
                geom2_id7.setMaterial(mat_id);
                node_marker.attachChild(geom2_id7);
                Geometry geom3_id7 = new Geometry("geom_id", box_id);
                geom3_id7.setMaterial(mat_id);
                node_marker.attachChild(geom3_id7);
                Geometry geom4_id7 = new Geometry("geom_id", box_id);
                geom4_id7.setMaterial(mat_id);
                node_marker.attachChild(geom4_id7);
                geom_id7.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom2_id7.setLocalTranslation(-3*Game.PLAYER_R/16, 0, 0);
                geom3_id7.setLocalTranslation(-7*Game.PLAYER_R/16, 0, 0);
                geom4_id7.setLocalTranslation(Game.PLAYER_R/2, 0, 0);
                geom2_id7.rotate(0, 0, 25);
                geom3_id7.rotate(0, 0, -25);
                break;
            case 8 :
                Geometry geom_id8 = new Geometry("geom_id", box_id);
                geom_id8.setMaterial(mat_id);
                node_marker.attachChild(geom_id8);
                Geometry geom2_id8= new Geometry("geom_id", box_id);
                geom2_id8.setMaterial(mat_id);
                node_marker.attachChild(geom2_id8);
                Geometry geom3_id8 = new Geometry("geom_id", box_id);
                geom3_id8.setMaterial(mat_id);
                node_marker.attachChild(geom3_id8);
                Geometry geom4_id8 = new Geometry("geom_id", box_id);
                geom4_id8.setMaterial(mat_id);
                node_marker.attachChild(geom4_id8);
                Geometry geom5_id8 = new Geometry("geom_id", box_id);
                geom5_id8.setMaterial(mat_id);
                node_marker.attachChild(geom5_id8);
                geom2_id8.setLocalTranslation(-5*Game.PLAYER_R/16, 0, 0);
                geom3_id8.setLocalTranslation(-8*Game.PLAYER_R/16, 0, 0);
                geom4_id8.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom5_id8.setLocalTranslation(Game.PLAYER_R/2, 0, 0);
                geom2_id8.rotate(0, 0, 25);
                geom3_id8.rotate(0, 0, -25);
                break;
            case 9 :
                Geometry geom_id9 = new Geometry("geom_id", box_id);
                geom_id9.setMaterial(mat_id);
                node_marker.attachChild(geom_id9);
                Geometry geom2_id9 = new Geometry("geom_id", box_id);
                geom2_id9.setMaterial(mat_id);
                node_marker.attachChild(geom2_id9);
                Geometry geom3_id9 = new Geometry("geom_id", box_id);
                geom3_id9.setMaterial(mat_id);
                node_marker.attachChild(geom3_id9);
                geom_id9.setLocalTranslation(-Game.PLAYER_R/4, 0, 0);
                geom2_id9.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom3_id9.setLocalTranslation(Game.PLAYER_R/4, 0, 0);
                geom2_id9.rotate(0, 0, 35);
                geom3_id9.rotate(0, 0, -35);
                break;
            default: break;
        }
        node_marker.setLocalTranslation(0, 0, Game.FRAME_THICKNESS/2);
        super.node_disk.attachChild(node_marker);
    }
    
    @Override
    protected void cleanup(Application app) {
        
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
                
        // reset the number of player :
        playerNumber = 0;
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
        
    @Override
    public int reward(Disk d) {
        return 0;
    }

    @Override
    void addToScore(int points) {
        this.POINT += points;
    }
}