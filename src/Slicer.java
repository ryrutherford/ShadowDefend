import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Slicer implements Attackable{
    private double health, speed;
    private int locationIndex, spawnDelayF, reward, penalty, wave;
    private Point location;
    private final Image image;
    private final String type;
    private List<Slicer> children;
    private Rectangle bounding;

    public Slicer(@NotNull String type, int wave, int spawnDelayF) {
        this.type = type;
        switch(type){
            case "slicer":
                this.speed = 2.0;
                this.health = 1.0;
                this.reward = 2;
                this.penalty = 1;
                this.children = Collections.<Slicer>emptyList();
                break;
            case "superslicer":
                this.speed = 1.5;
                this.health = 1.0;
                this.reward = 15;
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

    @Override
    public void deductHealth(int deduction){
        this.health -= deduction;

        //if the deduction forced the health to hit 0 or below =>
        //the locationIndex is set to -1 so the slicer disappears off the screen
        //if the slicer has children then they will each be placed at the location of their parent slicer (slighlty spread apart)
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

    public void drawSlicer(int timescaleMultiplier, Path path){
        if(this.locationIndex != - 1){
            if(locationIndex < path.getPathLength()) {
                this.location = path.getPath().get(locationIndex);
                this.bounding = this.image.getBoundingBoxAt(this.location);

                //drawing the slicer
                this.image.draw(location.x,
                        location.y,
                        new DrawOptions().setRotation(path.getPathAngle().get(locationIndex)));
                this.locationIndex += timescaleMultiplier*(int)(this.speed*4);
            }
            else{
                this.locationIndex = - 1;
            }
        }

    }

    //Getters

    public double getHealth() {
        return health;
    }

    public double getSpeed() {
        return speed;
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

    public int getWave(){
        return this.wave;
    }

    public int getSpawnDelayF(){
        return this.spawnDelayF;
    }

    public Image getImage(){
        return this.image;
    }

    public Rectangle getBounding() { return this.bounding; }

    //Setters

    public void setHealth(double health) {
        this.health = health;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void setLocationIndex(int locationIndex) {
        this.locationIndex = locationIndex;
    }
}
