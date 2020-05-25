import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import java.util.List;

/**
 * Abstract Ammo class extended by the Projectile and Explosive classes
 * Defines damageSlicers method which will be used by the Projectile and Explosive classes to damageSlicers in their own way
 */
public abstract class Ammo {
    //location: the current location of the ammo
    //bounding: the bounding box of the ammo
    //image: the image of the ammo
    //damage: the damage that the ammo inflicts on slicers that are hit by it
    private Point location;
    private Rectangle bounding;
    private Image image;
    private int damage;

    /**
     * @param slicers: the list of slicers in the current wave, used to decide which slicers to be targetted/attacked
     * @param timeScaleMultiplier: the timeScaleMultiplier from ShadowDefend used to affect the speed at which ammo moves or detonates
     * @return a boolean indicating whether the ammo should be removed (hit a target or exploded etc.)
     */
    public abstract boolean damageSlicers(List<Slicer> slicers, int timeScaleMultiplier);

    //Getters and Setters

    /**
     * @return the Point on the screen of the ammo
     */
    public Point getLocation() { return location; }

    /**
     * @param location: the new location of the ammo
     * @param r: the new bounding box of the ammo must be passed because a new location will result in a new bounding
     */
    public void setLocation(Point location, Rectangle r){
        this.location = location;
        this.setBounding(r);
    }

    /**
     * @return the Rectangle corresponding to the bounding box of the ammo
     */
    public Rectangle getBounding() { return bounding; }

    /**
     * @param bounding: the new bounding box of the ammo
     */
    public void setBounding(Rectangle bounding) { this.bounding = bounding; }

    /**
     * @return the Image of this ammo
     */
    public Image getImage() { return image; }

    /**
     * @param image: the new Image of this ammo
     */
    public void setImage(Image image) { this.image = image; }

    /**
     * @return the amount of damage this ammo causes to slicers
     */
    public int getDamage() { return damage; }

    /**
     * @param damage the new amount of damage this ammo causes
     */
    public void setDamage(int damage) { this.damage = damage; }
}
