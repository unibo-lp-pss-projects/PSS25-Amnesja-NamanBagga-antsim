package it.unibo.antsim.agent;

import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;

/*
* This class is basically an ant in the simulation, whit continous coordinates and orientation
*/
public class Ant {
    private WorldPosition position;
    private double angle; // It rappresents the orientation in radiant
    private final double speed;
    private AntState state;
    private boolean carryingFood;

    public Ant(WorldPosition initialPosition, double initialAngle, double speed) {
        this.position = Objects.requireNonNull(initialPosition, "Initial position cannot be null");
        this.angle = normalizeAngle(initialAngle);
        this.speed = speed;
        this.state = AntState.WANDERING;
        this.carryingFood = false;
    }

    // Getters
    public WorldPosition getPosition() { return position; }
    public double getAngle() { return angle; }
    public double getSpeed() { return speed; }
    public AntState getState() { return state; }
    public boolean isCarryingFood() { return carryingFood; }

    // Setters
    public void setState(AntState state) {
        this.state = Objects.requireNonNull(state);
    }
    public void setCarryingFood(boolean carryingFood){
        this.carryingFood = carryingFood;
    }

    // This method set manually the orientation angle of the ant
    public void setAngle(double angle){
        this.angle = normalizeAngle(angle);
    }

    /*
    * This method manages the ant movement.
    * If an ant goes forward to a obstacle, it bounce changing direction
    */
    public void move(double tick, World world){
        Objects.requireNonNull(world);

        // Movement calculation
        double dx = speed * Math.cos(angle) * tick;
        double dy = speed * Math.sin(angle) * tick;
        WorldPosition nextPos = new WorldPosition(position.x() + dx, position.y() + dy);

        // Collision controls
        if(!world.isBlockedAt(nextPos)){
            this.position = nextPos;
        }{
            // In case it's a collision, in poor words we do a u-turn
            this.angle = normalizeAngle(this.angle + Math.PI + (Math.random() - 0.5));
        }
    }



    private double normalizeAngle(double ang){
        double normalized = ang%(2*Math.PI);
        if(normalized<0){
            normalized += 2*Math.PI;
        }
        return normalized;
    }
}
