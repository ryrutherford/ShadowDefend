import bagel.Image;
import bagel.util.Point;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * PassiveTower class represents airsupport (towers that don't detect slicers, but attock randomly)
 */
public class PassiveTower extends Tower {

    //speed: how quickly the tower moves across the map
    //drop time: the time in frames left before an explosive is dropped
    private int speed, dropTime;

    public PassiveTower(String type) {
        this.setImage(new Image("res/images/" + type + ".png"));
        this.setType(type);
        switch (type) {
            case "airsupport":
                this.setPrice(500);
                this.speed = 5;
                break;
        }
        //generating a random drop time between 0 and 3 seconds (inclusive) which will correspond to an int between 0 and 180 frames
        this.dropTime = ThreadLocalRandom.current().nextInt(0, 181);
    }

    //attack method used to drop new explosives when drop time = 0, move the tower across the screen, and draw the tower's ammo on the screen
    @Override
    public void attack(List<Slicer> slicers, int timeScaleMultiplier) {

        //if the bounding is not null then the tower is on the game screen and can drop explosives
        if(this.getBounding() != null){
            //if the dropTime is 0 then we need to drop a new explosive
            if(this.dropTime <= 0){
                this.dropTime = ThreadLocalRandom.current().nextInt(0, 181);
                this.getAmmo().add(new Explosive(this.getLocation()));
            }
            //otherwise we just decrement the drop time based on the timeScaleMultiplier from ShadowDefend
            else{
                this.dropTime -= timeScaleMultiplier;
            }

            //moving the airsupport
            if (this.getDirection() == Math.PI / 2)
                this.setLocation(new Point(this.getLocation().x + timeScaleMultiplier * this.speed, this.getLocation().y));
            else
                this.setLocation(new Point(this.getLocation().x, this.getLocation().y + timeScaleMultiplier * this.speed));
        }

        //calling the drawAmmo method which will draw all of a Tower's ammo on the screen and inflict damage on nearby slicers when appropriate
        this.drawAmmo(slicers, timeScaleMultiplier);
    }
}
