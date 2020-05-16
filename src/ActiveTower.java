import bagel.Image;
import bagel.util.Rectangle;
import bagel.util.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * ActiveTower class represents tanks and supertanks (towers that detect enemies)
 */
public class ActiveTower extends Tower {

    //radius: the shooting range of the tower in either direction (2*radius = length and width of this.range)
    //cooldown: the time in frames that the tower must wait between consecutive shots
    //timeToShoot: the time remaining before the next shot can be fired (initially = to cooldown)
    //range: a rectangle that represents the shooting range of the tower
    private int radius, cooldown, timeToShoot;
    private Rectangle range;

    public ActiveTower(String type){
        this.setImage(new Image("res/images/" + type + ".png"));
        this.setType(type);
        switch(type){
            case "tank":
                this.radius = 100;
                this.cooldown = 60;
                this.setPrice(250);
                break;
            case "supertank":
                this.radius = 150;
                this.cooldown = 30;
                this.setPrice(600);
                break;
        }
        this.timeToShoot = this.cooldown;
    }

    @Override
    public void attack(List<Slicer> slicers, int timeScaleMultiplier) {

        //if the range hasn't been defined yet then we define it
        if(this.range == null){
            this.range = new Rectangle(this.getLocation().x - this.radius,
                    this.getLocation().y - this.radius,
                    this.radius*2,
                    this.radius*2);
        }

        //the slicer to be targeted by the next projectile will be stored in this list
        List<Slicer> slicerInRange = new ArrayList<Slicer>(1);

        //if the cooldown period has been reached, we check for slicers in range
        //if there are slicers in range we fire a projectile at it and restart the timeToShoot period
        if(this.timeToShoot <= 0){

            //the distance variable tracks the shortest distance between a valid target and the tower
            double distance = Double.MAX_VALUE;
            //the targettedSlicer variable tracks the slicer to be targetted by this tower
            Slicer targettedSlicer = null;

            for(Slicer s: slicers){
                //only slicers currently on the path (bounding box != null), in range, and not eliminated or finished the path (locationIndex != -1) can be shot at
                if(s.getBounding() != null && s.getBounding().intersects(this.range) && s.getHealth() > 0 && s.getLocationIndex() != -1){

                    //toSlicer is the vector to the location of the slicer
                    Vector2 toSlicer = new Vector2(s.getLocation().x, s.getLocation().y);
                    //toTower is the vector to the location of the tower
                    Vector2 toTower = new Vector2(this.getLocation().x, this.getLocation().y);

                    //betweenPoints is the vector pointing from the tower to the slicer
                    Vector2 betweenPoints = toSlicer.sub(toTower);

                    //if the length of the betweenPoints vector is shorter than the current shortest distance we update the distance and targettedSlicer vars
                    if(distance > betweenPoints.length()){
                        distance = betweenPoints.length();
                        targettedSlicer = s;
                    }
                }
            }
            //if the targettedSlicer is not null then there is a valid slicer to be targetted
            //this slicer will be the valid slicer closest to the tower
            //I decided to use this methodology instead of targetting the first valid slicer because it proved to be more effective at eliminating slicers
            if(targettedSlicer != null){
                slicerInRange.add(targettedSlicer);
            }
            //if there is a slicer in range: we will add a new projectile to the ammo list, reset the timeToShoot and set the direction of the tank based on this targe
            if(slicerInRange.size() > 0) {
                this.getAmmo().add(new Projectile(this.getLocation(), this.getType()));
                this.timeToShoot = this.cooldown;

                Slicer target = slicerInRange.get(0);
                this.setDirection(calculateDirection(target));
            }
        }
        //if the cooldown period hasn't been reached we simply decrement the timeToShoot counter
        else{
            this.timeToShoot -= timeScaleMultiplier;
        }
        //the drawAmmo method is always called
        this.drawAmmo(slicerInRange, timeScaleMultiplier);
    }
}
