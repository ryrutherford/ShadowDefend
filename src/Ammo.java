import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.List;

public abstract class Ammo {
    private Point location;
    private Rectangle bounding;
    private Image image;
    private int damage;

    public abstract boolean damageSlicers(List<Slicer> slicers, int timeScaleMultiplier);

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location, Rectangle r){
        this.location = location;
        this.setBounding(r);
    }

    public Rectangle getBounding() {
        return bounding;
    }

    public void setBounding(Rectangle bounding) {
        this.bounding = bounding;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
