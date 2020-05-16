import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import bagel.util.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tower class is an abstract class that includes basic data surrounding a Tower item in ShadowDefend
 * The attack method is abstract as different types of towers have different types of implementations for attacking slicers
 */
public abstract class Tower {

    /*
    image: the tower's image
    price: the price of the tower
    direction: the direction the tower is facing
    location: the current location of the tower
    type: the type of tower (tank, supertank, airsupport)
    bounding: the bounding box of the tower (based on its image)
    ammo: a list of all the ammo that is currently active for the tower
     */
    private Image image;
    private int price;
    private double direction;
    private Point location;
    private String type;
    private Rectangle bounding;
    private List<Ammo> ammo = new ArrayList<Ammo>();

    //used by towers to attack slicers with their ammo
    public abstract void attack(List<Slicer> slicers, int timeScaleMultiplier);

    //method to draw a tower in a static position with proper direction
    public void drawTower(){
        if(this.getBounding() != null) {
            this.getImage().draw(this.location.x,
                    this.location.y,
                    new DrawOptions().setRotation(this.direction));
        }
    }

    //draws a tower's ammo on the screen and calls the damageSlicers method to inflict damage on slicers in its area
    public void drawAmmo(List<Slicer> slicers, int timeScaleMultiplier){
        Iterator<Ammo> itr = this.getAmmo().iterator();
        while(itr.hasNext()){
            Ammo a = itr.next();

            //if the ammo has hit a target, detonated (for explosives only), or reached its range (for projectiles only)
            //then damageSlicers will return true and the ammo should be removed
            boolean remove = a.damageSlicers(slicers, timeScaleMultiplier);

            if(remove == false) {
                //drawing the ammo on the screen with appropriate direction
                a.getImage().draw(a.getLocation().x, a.getLocation().y);
            }

            if(remove){
                itr.remove();
            }
        }
    }

    //a method used to calculate the direction the tower should be facing (only used for active towers)
    public double calculateDirection(Slicer target){
        //the vector to the target
        Vector2 toTarget = new Vector2(target.getLocation().x, target.getLocation().y);

        //the vector to the tower
        Vector2 toTower = new Vector2(this.getLocation().x, this.getLocation().y);

        //the vector pointing from the tower to the target
        Vector2 betweenPoints = toTarget.sub(toTower);

        //unit vector pointing east
        Vector2 unitEast = new Vector2(1, 0);
        //unit vector for the betweenPoints vector
        Vector2 unitDir = betweenPoints.normalised();

        //returning the direction, we have to add pi/2 because the tank points north with direction 0
        return unitDir.y < 0 ? -Math.acos(unitEast.dot(unitDir)) + Math.PI/2: Math.acos(unitEast.dot(unitDir)) + Math.PI/2;
    }

    public Rectangle getBounding(){ return this.bounding; }

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

    public List<Ammo> getAmmo(){ return this.ammo; }

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

    public void setAmmo(List<Ammo> ammo){
        this.ammo = ammo;
    }
}
