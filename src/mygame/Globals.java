/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
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
    
    // abstract message :
    public static abstract class MyAbstractMessage extends AbstractMessage{
    
    }   
}

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
    private final float POS_PLAYER[][] = {{-POSNEG_BETWEEN_COORD,POSNEG_BETWEEN_COORD},{0,POSNEG_BETWEEN_COORD},{POSNEG_BETWEEN_COORD,POSNEG_BETWEEN_COORD},{-POSNEG_BETWEEN_COORD,0},{0,0},{POSNEG_BETWEEN_COORD,0},{-POSNEG_BETWEEN_COORD,-POSNEG_BETWEEN_COORD},{0,-POSNEG_BETWEEN_COORD},{POSNEG_BETWEEN_COORD,-POSNEG_BETWEEN_COORD}};
    private int TAB_POS_PLAYER_LENGTH = 8; // size of the prvious tab is decreased every time we add a player (to avoid 2 player on 1 start position) 
    
//    // Player control for 3 players :
//    private final KeyTrigger PLAYER_KEY[][] = {{new KeyTrigger(KeyInput.KEY_I), new KeyTrigger(KeyInput.KEY_K), new KeyTrigger(KeyInput.KEY_L), new KeyTrigger(KeyInput.KEY_J)},
//                                        {new KeyTrigger(KeyInput.KEY_Z), new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_Q)},
//                                        {new KeyTrigger(KeyInput.KEY_T), new KeyTrigger(KeyInput.KEY_G), new KeyTrigger(KeyInput.KEY_H), new KeyTrigger(KeyInput.KEY_F)}};
    
    // list containing all the disks :
//    private ArrayList<Disk> diskStore = new ArrayList<Disk>();
    
    private final Application myApp;
    private final Node NODE_GAME;
    private boolean needCleaning = false;
    
    public Game(Application app, Node gameNode){
        myApp = app;
        NODE_GAME = gameNode;
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
        if (needCleaning) {
            // detach all disk from the game when enabled :
            NODE_GAME.detachAllChildren();
            
            //reset variables :
            TAB_POS_PLAYER_LENGTH = 8;
            TIME = 30f;
            needCleaning = false;            
        }
        
        //create the gameboard
        GameBoard gb = new GameBoard();
        NODE_GAME.attachChild(gb.getGameBoard());                 
        
//        // create all neg and pos disk :
//        boolean isNeg = true;
//        for(int i=0; i<5 ;i++){
//            for (int j=0; j<5 ;j++){
//                if(!((i>0 && i<4)&&(j>0 && j<4))){
//                    if (isNeg){
//                        NDisk disk = new NDisk(POS_TAB[i], POS_TAB[j]);
//                        disk.setEnabled(true); 
//                        myApp.getStateManager().attach(disk);
//                        diskStore.add(disk);
//                    }else{
//                        PDisk disk = new PDisk(POS_TAB[i], POS_TAB[j]); 
//                        disk.setEnabled(true); 
//                        myApp.getStateManager().attach(disk);
//                        diskStore.add(disk);
//                    }
//                }
//                isNeg = !isNeg;                
//            }     
//        }
        
//        // create players :
//        for (int i = 0; i < NUMBER_OF_PLAYER ;i++){
//            this.newPlayer(i);
//        } 
        
        // create the HUD
//        initHUD();
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
//        BitmapFont myFont = myApp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");                        
//        timeAndScore = new BitmapText(myFont, false);
//        timeAndScore.setSize(myFont.getCharSet().getRenderedSize() * 4);
//        timeAndScore.setColor(ColorRGBA.White);
//        timeAndScore.setText("TIME : " + TIME);
//        timeAndScore.setLocalTranslation(5, 700, 0);
//        myApp.getGuiNode().attachChild(timeAndScore); 
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
//        for(int i = 0; i < diskStore.size(); i++){
//            for(int other = i + 1; other < diskStore.size(); other++){
//                diskStore.get(i).diskCollision(diskStore.get(other), tpf);
//            }            
//        }
        
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