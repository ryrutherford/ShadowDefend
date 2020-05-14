import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.List;

public class Projectile extends Ammo{

    private double direction;

    public Projectile(double direction, Point location, String type) {
        this.direction = direction;
        this.setImage(new Image("res/images/" + type + "_projectile.png"));
        this.setLocation(location, this.getImage().getBoundingBoxAt(this.getLocation()));
        switch(type){
            case "tank":
                this.setDamage(1);
                break;
            case "supertank":
                this.setDamage(3);
                break;
        }
    }

    public boolean damageSlicers(List<Slicer> slicers, int timeScaleMultiplier){
        return false;
    }

}
