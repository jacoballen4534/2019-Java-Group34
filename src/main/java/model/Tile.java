package model;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Tile extends GameObject{
    protected boolean solid = false;

    public Tile(int x, int y, BufferedImage image, int spriteWidth, int spriteHeight, int renderWidth, int renderHeight){
        super(x,y, image, spriteWidth, spriteHeight, renderWidth, renderHeight);
    }

    protected Rectangle getBounds() {
        if (this.solid) {
            return new Rectangle((int)this.x, (int)this.y, this.spriteWidth, this.spriteHeight);
        } else {
            return new Rectangle(0, 0, 0, 0);
        }
    }
}
