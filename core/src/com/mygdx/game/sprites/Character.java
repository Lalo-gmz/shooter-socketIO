package com.mygdx.game.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Character extends Sprite {
    Vector2 previousPosition;
    public Character(Texture texture){
        super(texture);
        previousPosition = new Vector2(getX(), getY());
    }

    public boolean hasMove(){
        if(previousPosition.x != getX() || previousPosition.y != getY()){
            previousPosition.y = getY();
            previousPosition.x = getX();
            return true;
        }
        return false;
    }


}
