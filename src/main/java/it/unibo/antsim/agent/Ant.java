package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;
import java.util.random.RandomGenerator;

/*
 * This class is basically an ant in the simulation, whit continous coordinates and orientation
 */
public class Ant {
    private WorldPosition position;
    private double angle; // It rappresents the orientation in radiant
    private final double speed;
    private AntState state;
    private final AntRole role;
    private boolean carryingFood;
    private CellIndex prevCell = null;
    private int stepsSinceLastDeposit = 0;
    private static final int DEPOSIT_EVERY_N_STEPS = 3;

    public Ant(WorldPosition initialPosition, double initialAngle, double speed, AntRole role) {
        this.position = Objects.requireNonNull(initialPosition, "Initial position cannot be null");
        this.angle = normalizeAngle(initialAngle);
        this.speed = speed;
        this.state = AntState.WANDERING;
        this.role = Objects.requireNonNull(role, "Role can't be null");
        this.carryingFood = false;
    }

    // Getters
    public WorldPosition getPosition() { return position; }
    public CellIndex getPrevCell() { return prevCell; }
    public double getAngle() { return angle; }
    public double getSpeed() { return speed; }
    public AntState getState() { return state; }
    public AntRole getRole(){ return role; }
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
        this.prevCell = world.convertToCellIndex(this.position);
        // Movement calculation
        double dx = speed * Math.cos(angle) * tick;
        double dy = speed * Math.sin(angle) * tick;
        WorldPosition nextPos = new WorldPosition(position.x() + dx, position.y() + dy);

        // Collision controls
        if(!world.isBlockedAt(nextPos)){
            this.position = nextPos;
        }else{
            // try loop instead of bounce
            for(int i=0; i<8;i++){
                double tryAngle = normalizeAngle(this.angle + Math.PI + (Math.random() - 0.5) * Math.PI);
                double testX = position.x() + speed * tick * Math.cos(tryAngle);
                double testY = position.y() + speed * tick * Math.sin(tryAngle);
                if(!world.isBlockedAt(new WorldPosition(testX, testY))){
                    this.angle = tryAngle;
                    this.position = new WorldPosition(testX, testY);
                    break;
                }
            }
        }
    }

    /**
     * Switches ant state after picking up food
     */
    public void pickFood(){
        if(carryingFood){
            throw new IllegalStateException("Ant is already carrying food");
        }
        carryingFood = true;
        state = AntState.RETURNING_TO_NEST;
        this.angle = normalizeAngle(this.angle + Math.PI); // Reverse direction
    }

    /**
     * Switches ant state after dropping food at the nest?
     */
    public void dropFood(){
        if(!carryingFood){
            throw new IllegalStateException("Ant is not carrying food");
        }
        carryingFood = false;
        state = AntState.WANDERING;
        this.angle = normalizeAngle(this.angle + Math.PI); // Reverse direction
    }
    // This method normalizes the angle to be in the range [0, 2π)
    private double normalizeAngle(double ang){
        double normalized = ang%(2*Math.PI);
        if(normalized<0){
            normalized += 2*Math.PI;
        }
        return normalized;
    }
}
