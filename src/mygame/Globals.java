/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
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
    
    class GameBoard {
        private final Node NODE_GAME_BOARD = new Node("NODE_GAME_BOARD"); 
        protected float BOX_SIZE_FREE_AREA;
        protected float BOX_SIZE_FRAME;
        protected float BOX_THICKNESS_FRAME;
        
        GameBoard(Application sapp){
            this.BOX_THICKNESS_FRAME = FRAME_THICKNESS/2;
            this.BOX_SIZE_FREE_AREA = FREE_AREA_WIDTH/2;
            this.BOX_SIZE_FRAME = FRAME_SIZE/2;
            
            //create the floor :
            Box ground = new Box(BOX_SIZE_FREE_AREA,BOX_SIZE_FREE_AREA,1);
            Geometry geom_ground = new Geometry("geom_ground", ground);
            Material mat_ground = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
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
            Material mat_frame = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
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