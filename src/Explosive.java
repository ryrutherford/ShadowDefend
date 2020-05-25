import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import java.util.List;

/**
 * Explosive class represents ammo for Passive Towers
 */
public class Explosive extends Ammo{

    //the radius corresponds to the range of the explosive in either direction
    //the timeToDetonate corresponds to how much time is left (in frames) before the explosive explodes and causes damage to slicers
    private int radius, timeToDetonate;

    /**
     * @param location: the Point on the screen at which the explosive was dropped
     */
    public Explosive(Point location){
        this.radius = 200;
        this.setLocation(location, new Rectangle(location.x - 200, location.y - 200, 2*this.radius, 2*this.radius));
        this.setImage(new Image("res/images/explosive.png"));
        this.setDamage(500);
        //120 frames is 2 seconds
        this.timeToDetonate = 120;
    }

    /**
     * method that damages all slicers in the explosives radius when timeToDetonate is 0
     * returns true when an explosive detonates, false otherwise
     * @param slicers: a list of slicers in the current wave, used to decide which slicers will be impacted by the explosion
     * @param timeScaleMultiplier: the timeScaleMultiplier from ShadowDefend used to affect the speed at which the timeToDetonate decreases
     * @return a boolean indicating whether the explosive exploded (true) or not (false)
     */
    @Override
    public boolean damageSlicers(List<Slicer> slicers, int timeScaleMultiplier){
        //if the timeToDetonate is 0 then the explosive can damage all slicers within its radius
        if(this.timeToDetonate == 0){
            for(Slicer s: slicers){
                //if the slicer is active (index is not -1) and it intersects with the explosive we deduct health from it
                if(s.getBounding() != null && s.getLocationIndex() != - 1 && s.getBounding().intersects(this.getBounding()) && s.getHealth() > 0){
                    //deducting the health from the slicer
                    s.deductHealth(this.getDamage());
                }
            }
            return true;
        }
        //otherwise we decrement the timeToDetonate based on the timeScaleMultiplier
        else{
            this.timeToDetonate -= timeScaleMultiplier;
            return false;
        }
    }
}
