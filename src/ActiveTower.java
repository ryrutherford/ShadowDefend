import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ActiveTower implements Attacker {

    private final Image image;
    private int radius, cooldown, price;
    double direction;
    private Point location;
    private String type;
    private Rectangle bounding;
    private List<Projectile> projectiles;

    public ActiveTower(String type){
        this.image = new Image("res/images/" + type + ".png");
        this.type = type;
        this.projectiles = new ArrayList<Projectile>();
        switch(this.type){
            case "tank":
                this.radius = 100;
                this.cooldown = 1000;
                this.price = 250;
                break;
            case "supertank":
                this.radius = 150;
                this.cooldown = 500;
                this.price = 600;
                break;
        }
    }

    @Override
    public boolean attack() {
        return false;
    }

    @Override
    public Rectangle getBounding() {
        return this.bounding;
    }

    @Override
    public Image getImage(){
        return this.image;
    }

    @Override
    public int getPrice(){
        return this.price;
    }

    @Override
    public Point getLocation() {
        return this.location;
    }

    @Override
    public void setLocation(Point location){
        this.location = location;
        this.bounding = this.image.getBoundingBoxAt(location);
    }


}
