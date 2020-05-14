import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.List;

public class Explosive extends Ammo{

    private int radius, timeToDetonate;

    public Explosive(Point location){
        this.radius = 200;
        this.setLocation(location, new Rectangle(location.x - 200, location.y - 200, 2*this.radius, 2*this.radius));
        this.setImage(new Image("res/images/explosive.png"));
        this.setDamage(500);
        //120 frames is 2 seconds
        this.timeToDetonate = 120;
    }

    @Override
    public boolean damageSlicers(List<Slicer> slicers, int timeScaleMultiplier){
        if(this.timeToDetonate == 0){
            for(Slicer s: slicers){
                //if the slicer is active (index is not -1) and it intersects with the explosive we deduct health from it
                if(s.getBounding() != null && s.getLocationIndex() != - 1 && s.getBounding().intersects(this.getBounding()) && s.getHealth() > 0){
                    s.deductHealth(this.getDamage());
                }
            }
            return true;
        }
        else{
            this.timeToDetonate -= timeScaleMultiplier;
            return false;
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
