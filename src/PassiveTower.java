import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public class PassiveTower implements Attacker {

    private final Image image;
    private String type;
    private int price;
    private Point location;
    private Rectangle bounding;
    double direction;

    public PassiveTower(String type){
        this.image = new Image("res/images/" + type + ".png");
        this.type = type;
        switch(type){
            case "airsupport":
                this.price = 500;
                break;
        }
    }
    @Override
    public boolean attack() {
        return false;
    }

    @Override
    public Rectangle getBounding() {
        return null;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public int getPrice() {
        return this.price;
    }

    @Override
    public Point getLocation() {
        return this.location;
    }

    @Override
    public void setLocation(Point Location) {
        this.location = location;
        this.bounding = this.image.getBoundingBoxAt(location);
    }
}
