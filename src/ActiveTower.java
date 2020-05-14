import bagel.DrawOptions;
import bagel.Image;
import java.util.ArrayList;
import java.util.List;

public class ActiveTower extends Tower {

    private int radius, cooldown;

    public ActiveTower(String type){
        this.setImage(new Image("res/images/" + type + ".png"));
        this.setType(type);
        switch(type){
            case "tank":
                this.radius = 100;
                this.cooldown = 1000;
                this.setPrice(250);
                break;
            case "supertank":
                this.radius = 150;
                this.cooldown = 500;
                this.setPrice(600);
                break;
        }
    }

    @Override
    public void attack(List<Slicer> slicers, int timeScaleMultiplier) {

    }
}
