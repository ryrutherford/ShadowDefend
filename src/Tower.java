import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.ArrayList;
import java.util.List;

public abstract class Tower implements Attacker {

    private Image image;
    private int price;
    private double direction;
    private Point location;
    private String type;
    private Rectangle bounding;
    private List<Ammo> ammo = new ArrayList<Ammo>();

    public void drawTower(){
        this.getImage().draw(this.location.x,
                this.location.y,
                new DrawOptions().setRotation(this.direction));
    }

    public Rectangle getBounding(){
        return this.bounding;
    }

    public Image getImage(){
        return this.image;
    }

    public int getPrice(){
        return this.price;
    }

    public Point getLocation(){
        return this.location;
    }

    public String getType(){
        return this.type;
    }

    public double getDirection(){
        return this.direction;
    }

    public List<Ammo> getAmmo(){
        return this.ammo;
    }

    public void setLocation(Point location){
        this.location = location;
        this.bounding = this.image.getBoundingBoxAt(location);
    }

    public void setDirection(double direction){
        this.direction = direction;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBounding(Rectangle bounding) {
        this.bounding = bounding;
    }
}
