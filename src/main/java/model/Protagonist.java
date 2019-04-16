package model;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Protagonist extends Character {
    private static int nextID = 0; //Unique id for all characters, this will be used for multilayer
    protected int id;
    private int lives; //Keep track of how many lives, Can pick up hearts which increase this. 0 = dead.
    //private KeyInput keyInput; //The keyboard inputs to move the character.
    private boolean buttonAlreadyDown = false; //To only update animation state on button initial press, not on hold.
    private boolean isAttacking = false; //Attempt to debounce attacking
    //The different animation states to hold the borders and which sprite from sprite sheet to use.
    private AnimationsState runningState;
    private AnimationsState idleState;
    private AnimationsState attackState;
    private AnimationsState gotHitState;

    private int currEnergy;
    private int maxEnergy;
    private HUD hud;

    private Item equippedItem;
    private Inventory inventory;

    public Protagonist(int x, int y, BufferedImage image, int spriteWidth, int spriteHeight, int renderWidth, int renderHeight, int levelWidth) {
        super(x, y, image, spriteWidth, spriteHeight, renderWidth, renderHeight, levelWidth);
        this.id = nextID++; //Give each protagonist a unique id. (Will be used for multilayer)
        //this.keyInput = keyInput;

        //Set up the bounding boxes and sprite selection for the different animation options.
        this.idleState = new AnimationsState(45,48,17, 5, 3, 0, 0);
        this.runningState = new AnimationsState(52,38,20,5, 6, 1, 1);
//        this.attackState = new AnimationsState(45,0,0,5,6,6,0);
        this.attackState = new AnimationsState(45,48,17,5,6,6,0);
        this.gotHitState = new AnimationsState(0,0,0,0,6,9,0); //Place holder till get hit sprite

        //Set health
        this.currHealth = 100;
        this.currEnergy = 100;
        this.maxHealth = this.currHealth;
        this.maxEnergy = this.currEnergy;
        this.hud = new HUD(this.id, this.maxHealth,this.maxEnergy,300,100,50,50);
        hud.setHealth(this.currHealth);
        hud.setEnergy(this.currEnergy);

        inventory = new Inventory(10);
        equippedItem = null;

        this.jfxImage = SwingFXUtils.toFXImage(this.spriteSheet.getSprite(0,0), null); //Initialise image for first animation
    }

    @Override
    protected void attack() {
        this.animationsState.copy(this.attackState); //Set the state to update the bounding boxes
        super.attack();
        Handler.attack(this);
        currEnergy -= 10;
        hud.setEnergy(currEnergy);
    }

    @Override
    void playSound() {
        System.out.println("Beep");
    }

    @Override
    protected void getHit() {
        this.animationsState.copy(this.gotHitState);
        super.getHit();
        if (this.currHealth <= 0) { //died
            this.playGotAttackedAnimation = false;
            this.playDieAnimation = true; //Can leave other play animation booleans true as die has implicit priority when checking.
        }
    }

    @Override
    void updateAnimationState() {
        //Determine what state the player is in, and update the animation accordingly.
        //IMPLICIT PRIORITY. ORDER = DIE, ATTACKING, GotHit, IDLE/RUNNING
        //After die animation last frame, fade out ...Game over
        if (this.playAttackAnimation) { //Attacking
            //Update attack animation
            this.animationsState.copy(this.attackState);
            if (this.animationsState.isLastFrame(this.currentAnimationCol)) {
                this.playAttackAnimation = false; //Once the animation has finished, set this to false to only play the animation once
                //hud.setEnergy(hud.getEnergy()-10);
                this.isAttacking = false;
            }
        }else if (this.playGotAttackedAnimation) {
            this.animationsState.copy(this.gotHitState);
            if (this.animationsState.isLastFrame(this.currentAnimationCol)) {
                this.playGotAttackedAnimation = false; //Once the animation has finished, set this to false to only play the animation once
            }
        } else if (this.playDieAnimation) {
//            this.animationsState.copy(this.dieAnimation);
            if (this.animationsState.isLastFrame(this.currentAnimationCol)) {
                Handler.removePlayer(this);
            }
        } else if (this.velocityX == 0 && this.velocityY == 0) { //Idle
            this.animationsState.copy(this.idleState);
        } else { //Running (As this is the only other option)
            this.animationsState.copy(runningState);
        }
    }


    public void tick(double cameraX, double cameraY, KeyInput keyInput) {
        //Update the velocity according to what keys are pressed.
        //If the key has just been pressed, update the animation. This leads to more responsive animations.
        if(this.playGotAttackedAnimation || this.playDieAnimation || this.playAttackAnimation) { //If the player is in an animation, disable movement
            this.velocityX = 0;
            this.velocityY = 0;
        } else {

            if (keyInput.getKeyPressed("up")){
                this.velocityY = -5;
            } else if (keyInput.getKeyPressed("down")){
                this.velocityY = 5;
            } else this.velocityY=0;

            if (keyInput.getKeyPressed("right")) {
                this.velocityX = 5;
                if (!this.buttonAlreadyDown) { //TODO: Add to up/down
                    this.updateSprite();
                    this.buttonAlreadyDown = true;
                }
            } else if (keyInput.getKeyPressed("left")) {
                this.velocityX = -5;
                if (this.buttonAlreadyDown) {
                    this.updateSprite();
                    this.buttonAlreadyDown = false;
                }
            } else this.velocityX=0;
        }

        if (keyInput.getKeyPressDebounced("attack")){
            /*if (hud.getEnergyPercent() > 0.5){
                this.attack();
            } else {
                System.out.println("Not enough energy");
            }*/
            this.attack();
        }

        if (keyInput.getKeyPressed("jump")){
            System.out.println("Jump for joy");// Using this to make it easier to custom add key bindings later
        }

        if (keyInput.getKeyPressDebounced("useItem")){
            useItem();
        }

        if (keyInput.getKeyPressDebounced("block")){
            System.out.println("An impenetrable defence");
        }

        if (keyInput.getKeyPressDebounced("useSpecial")){
            if (useSpecial()) {
                System.out.println("Azarath, metrion, zinthos!");//Outdated reference
            }
            else System.out.println("Insufficient energy");
        }

        if (keyInput.getKeyPressDebounced("cheatKey")){
            System.out.println("Wow, cheating in 2019?");
            currEnergy = maxEnergy;
            hud.setEnergy(currEnergy);
        }

        super.tick(cameraX,cameraY); //Check collisions and update x and y
        hud.tick(cameraX, cameraY); //Update health and energy displays

    }

    @Override
    public Rectangle getBounds() {
        return super.getBounds();
    }



    @Override
    public void render(GraphicsContext graphicsContext, double cameraX, double cameraY) {
        super.render(graphicsContext, cameraX, cameraY);
        hud.render(graphicsContext, cameraX, cameraY);
    }

    public HUD getHud() {
        return hud;
    }

    public boolean pickUpItem(Item item){
        if (!this.inventory.isFull()){
            this.inventory.addItem(item);
            return true;
        }
        return false;
    }

    public void useItem(){
        if (equippedItem != null) {
            System.out.println("Using an item");
            equippedItem.useItem();
        } else {
            System.out.println("You dont have an item to use");
        }
    }

    public void setEquippedItem(Item item){
        equippedItem = item;
    }

    public boolean useSpecial(){
        if (currEnergy == maxEnergy){
            currEnergy -= maxEnergy;
            hud.setEnergy(currEnergy);
            return true;
        }
        else return false;
    }
}

