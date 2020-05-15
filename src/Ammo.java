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

    public abstract boolean damageSlicers(List<Slicer> slicers, int timeScaleMultiplier);

    //Getters and Setters

    public Point getLocation() { return location; }

    public void setLocation(Point location, Rectangle r){
        this.location = location;
        this.setBounding(r);
    }

    public Rectangle getBounding() { return bounding; }

    public void setBounding(Rectangle bounding) { this.bounding = bounding; }

    public Image getImage() { return image; }

    public void setImage(Image image) { this.image = image; }

    public int getDamage() { return damage; }

    public void setDamage(int damage) { this.damage = damage; }
}
