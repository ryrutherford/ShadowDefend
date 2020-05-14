import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PassiveTower extends Tower {

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

    @Override
    public void attack(List<Slicer> slicers, int timeScaleMultiplier) {

        if(this.getBounding() != null){
            //if the dropTime is 0 then we need to drop a new explosive
            if(this.dropTime == 0){
                this.dropTime = ThreadLocalRandom.current().nextInt(0, 181);
                this.getAmmo().add(new Explosive(this.getLocation()));
            }
            else{
                this.dropTime -= timeScaleMultiplier;
            }

            //moving the airsupport
            if (this.getDirection() == Math.PI / 2)
                this.setLocation(new Point(this.getLocation().x + timeScaleMultiplier * this.speed, this.getLocation().y));
            else
                this.setLocation(new Point(this.getLocation().x, this.getLocation().y + timeScaleMultiplier * this.speed));
        }

        Iterator<Ammo> itr = this.getAmmo().iterator();
        while(itr.hasNext()){
            Ammo a = itr.next();
            a.getImage().draw(a.getLocation().x, a.getLocation().y);

            boolean remove = a.damageSlicers(slicers, timeScaleMultiplier);
            if(remove){
                itr.remove();
            }
        }
    }
}
