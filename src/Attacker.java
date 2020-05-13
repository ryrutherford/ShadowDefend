import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

public interface Attacker {
    public boolean attack();
    public Rectangle getBounding();
    public Image getImage();
    public int getPrice();
    public Point getLocation();
    public void setLocation(Point Location);
}
