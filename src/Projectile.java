import bagel.Image;
import bagel.util.Point;

public class Projectile {

    private double direction;
    private Point location;
    private Image image;
    private int damage;

    public Projectile(double direction, Point location, String type) {
        this.direction = direction;
        this.location = location;
        this.image = new Image("res/images/" + type + "_projectile.png");
        switch(type){
            case "tank":
                this.damage = 1;
                break;
            case "supertank":
                this.damage = 3;
                break;
        }
    }
}
