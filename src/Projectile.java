import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import bagel.util.Vector2;

import java.util.List;

/**
 * Projectile class represents ammo for ActiveTowers
 */
public class Projectile extends Ammo{

    //direction: the direction of the projectile
    //target: the slicer that the projectile is targetting
    //parentRange: the rectangle that represents the range of the tower that shot this projectile
    //speed: how fast the projectile moves (px/frame)
    private double direction = 0;
    private Slicer target;
    private Rectangle parentRange;
    private int speed;

    public Projectile(Point location, String type, Rectangle range) {
        this.setImage(new Image("res/images/" + type + "_projectile.png"));
        this.setLocation(location, this.getImage().getBoundingBoxAt(location));
        this.parentRange = range;
        this.speed = 10;
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
        //if the target is null and there is a slicer in the slicers list, we initialize the target with the first slicer in the list
        if(this.target == null && slicers.size() > 0){
            this.target = slicers.get(0);
        }

        //if the projectile has left the tower's range then it must disappear so we return true
        if(this.parentRange.intersects(this.getLocation()) == false){
            return true;
        }

        //if the target is not null we check to see if the projectile has intersected with it
        if(this.target != null){
            if(this.target.getBounding().intersects(this.getLocation())){
                if(this.target.getHealth() > 0) {
                    this.target.deductHealth(this.getDamage());
                }
                return true;
            }

            //the vector to the target
            Vector2 toTarget = new Vector2(this.target.getLocation().x, this.target.getLocation().y);
            //the vector to the ammo
            Vector2 toAmmo = new Vector2(this.getLocation().x, this.getLocation().y);

            //the vector pointing from the ammo to the target
            Vector2 betweenPoints = toTarget.sub(toAmmo);

            //unit vector pointing east
            Vector2 unitEast = new Vector2(1, 0);
            //unit vector for the betweenPoints vector
            Vector2 unitDir = betweenPoints.normalised();

            //calculating the direction
            this.direction = unitDir.y < 0 ? -Math.acos(unitEast.dot(unitDir)) : Math.acos(unitEast.dot(unitDir));

            //the vector to the next point for the ammo
            Vector2 path = toAmmo.add(unitDir.mul(this.speed*timeScaleMultiplier));

            this.setLocation(path.asPoint(), this.getImage().getBoundingBoxAt(path.asPoint()));
        }
        //otherwise it means there were no slicers in range
        return false;
    }
}
