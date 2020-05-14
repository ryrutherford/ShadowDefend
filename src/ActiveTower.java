import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Rectangle;
import bagel.util.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActiveTower extends Tower {

    private int radius, cooldown, timeToShoot;
    private Rectangle range;

    public ActiveTower(String type){
        this.setImage(new Image("res/images/" + type + ".png"));
        this.setType(type);
        switch(type){
            case "tank":
                this.radius = 100;
                this.cooldown = 60;
                this.setPrice(250);
                break;
            case "supertank":
                this.radius = 150;
                this.cooldown = 30;
                this.setPrice(600);
                break;
        }
        this.timeToShoot = this.cooldown;
    }

    @Override
    public void attack(List<Slicer> slicers, int timeScaleMultiplier) {

        //if the range hasn't been defined yet then we define it
        if(this.range == null){
            this.range = new Rectangle(this.getLocation().x - this.radius,
                    this.getLocation().y - this.radius,
                    this.radius*2,
                    this.radius*2);
        }

        //the slicer to be targeted by the next projectile
        List<Slicer> slicerInRange = new ArrayList<Slicer>(1);

        //if the cooldown period has been reached, we check for slicers in range
        //if there are slicers in range we fire a projectile at it and restart the timeToShoot period
        if(this.timeToShoot <= 0){
            for(Slicer s: slicers){
                if(s.getBounding() != null && s.getBounding().intersects(this.range) && s.getLocationIndex() != -1){
                    slicerInRange.add(s);
                    break;
                }
            }
            if(slicerInRange.size() > 0) {
                this.getAmmo().add(new Projectile(this.getLocation(), this.getType(), this.range));
                this.timeToShoot = this.cooldown;

                Slicer target = slicerInRange.get(0);
                this.setDirection(calculateDirection(target));
            }
        }
        //if the cooldown period hasn't been reached we simply decrement the timeToShoot counter
        else{
            this.timeToShoot -= timeScaleMultiplier;
        }
        //we always draw the active ammo
        this.drawAmmo(slicerInRange, timeScaleMultiplier);
    }
}
