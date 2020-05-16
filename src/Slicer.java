import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Slicer implements Attackable{
    /*
    health: the remaining health of the slicer
    speed: the speed at which the slicer moves (in px/frame)
    locationIndex: the index of the slicer's location in the path list of the Path object in the ShadowDefend class
    spawnDelayF: the spawn delay in frames of the slicer
    reward: the cash reward for eliminating the slicer
    penalty: the penalty (in lives) for the slicer finishing the path without dying
    wave: the wave the slicer belongs to
    width: the actual width of the slicer image (excluding white space) used to make its bounding box
    height: the actual height of the slicer image (excluding white space) used to make its bounding box
    location: the location of the slicer
    type: the type of slicer (slicer, superslicer, megaslicer, apexslicer)
    children: a list of all the children of the slicer
    bounding: the bounding box of the slicer
     */
    private double health, speed;
    private int locationIndex, spawnDelayF, reward, penalty, wave, width, height;
    private Point location;
    private final Image image;
    private final String type;
    private List<Slicer> children;
    private Rectangle bounding;

    public Slicer(String type, int wave, int spawnDelayF) {
        this.type = type;
        switch(type){
            case "slicer":
                this.speed = 2.0;
                this.health = 1.0;
                this.reward = 2;
                this.penalty = 1;
                this.width = 48;
                this.height = 58;
                this.children = Collections.<Slicer>emptyList();
                break;
            case "superslicer":
                this.speed = 1.5;
                this.health = 1.0;
                this.reward = 15;
                this.width = 48;
                this.height = 58;
                this.children = new ArrayList<Slicer>(2);
                for(int i = 0; i < 2; i++)
                    this.children.add(new Slicer("slicer", wave, 0));
                for(Slicer s: this.children)
                    this.penalty += s.getPenalty();
                break;
            case "megaslicer":
                this.speed = 1.5;
                this.health = 2.0;
                this.reward = 10;
                this.width = 24;
                this.height = 30;
                this.children = new ArrayList<Slicer>(2);
                for(int i = 0; i < 2; i++)
                    this.children.add(new Slicer("superslicer", wave,0));
                for(Slicer s: this.children)
                    this.penalty += s.getPenalty();
                break;
            case "apexslicer":
                this.speed = 0.75;
                this.health = 25.0;
                this.reward = 150;
                this.width = 48;
                this.height = 58;
                this.children = new ArrayList<Slicer>(4);
                for(int i = 0; i < 4; i++)
                    this.children.add(new Slicer("megaslicer", wave,0));
                for(Slicer s: this.children)
                    this.penalty += s.getPenalty();
                break;
        }
        this.spawnDelayF = spawnDelayF;
        this.image = new Image("res/images/" + type + ".png");
        this.wave = wave;
    }

    //a method to deduct health from a slicer after a collision with ammo
    @Override
    public void deductHealth(int deduction){
        this.health -= deduction;

        //if the deduction forced the health to hit 0 or below =>
        //the locationIndex is set to -1 so the slicer disappears off the screen
        //if the slicer has children then they will each be placed at the location of their parent slicer (slightly spread apart)
        if(this.health <= 0){
            this.health = 0;
            int i = 0;
            for(Slicer s: children){
                if(this.locationIndex - i >= 0) {
                    s.setLocationIndex(this.locationIndex - i);
                }
                else{
                    s.setLocationIndex(this.locationIndex);
                }
                i+=100;
            }
            this.locationIndex = -1;
        }
    }

    //a method to draw a slicer on the screen
    public void drawSlicer(int timescaleMultiplier, Path path){
        //if the locationIndex is not -1 then the slicer is somewhere on the path
        if(this.locationIndex != - 1){
            //if the locationIndex is a valid pathIndex then we get the slicers location, bounding, and draw it on the screen
            if(locationIndex < path.getPathLength()) {
                this.location = path.getPath().get(locationIndex);
                this.bounding = new Rectangle(this.location.x - this.width/2, this.location.y - this.height/2, this.width, this.height);

                //drawing the slicer
                this.image.draw(location.x,
                        location.y,
                        new DrawOptions().setRotation(path.getPathAngle().get(locationIndex)));

                //we update the location index based on the speed and timeScaleMultiplier
                //I multiply by 4 because each point in the path is 0.25px apart and we want to work in 1px increments
                this.locationIndex += timescaleMultiplier*(int)(this.speed*4);
            }
            //if the locationIndex is invalid then we set the locationIndex to -1 and spawnDelayF to -1
            else{
                this.locationIndex = - 1;

                //a spawnDelayF of -1 indicates that the slicer's penalty needs to be deducted from the user's lives
                this.spawnDelayF = -1;
            }
        }

    }

    //Getters

    public double getHealth() {
        return health;
    }

    public int getReward() {
        return reward;
    }

    public int getPenalty() {
        return penalty;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public Point getLocation() {
        return location;
    }

    public List<Slicer> getChildren() {
        return children;
    }

    public int getSpawnDelayF(){
        return this.spawnDelayF;
    }

    public Rectangle getBounding() { return this.bounding; }

    //Setters

    public void setHealth(double health) {
        this.health = health;
    }

    public void setLocationIndex(int locationIndex) {
        this.locationIndex = locationIndex;
    }
}
