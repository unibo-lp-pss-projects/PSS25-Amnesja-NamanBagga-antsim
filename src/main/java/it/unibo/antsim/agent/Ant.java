package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import java.util.Objects;

/**
 * This is an ant in the simulation with continuous coordinates and orientation angle.
 */
public class Ant {
    private WorldPosition position;
    private double angle; // It represents the orientation in radiant
    private final double speed;
    private AntState state;
    private final AntRole role;
    private boolean carryingFood;
    private CellIndex prevCell;

    /**
     * Instantiates a new Ant.
     *
     * @param initialPosition the initial position
     * @param initialAngle the initial angle
     * @param speed the speed
     * @param role the role
     */
    public Ant(final WorldPosition initialPosition, final double initialAngle, final double speed, final AntRole role) {
        this.position = Objects.requireNonNull(initialPosition, "Initial position cannot be null");
        this.angle = normalizeAngle(initialAngle);
        this.speed = speed;
        this.state = AntState.WANDERING;
        this.role = Objects.requireNonNull(role, "Role can't be null");
        this.carryingFood = false;
    }

    /**
     * Gets the current position of the ant.
     *
     * @return the current world position
     */
    public WorldPosition getPosition() {
        return position;
    }

    /**
     * Gets he previous cell index occupied by the ant.
     *
     * @return the previous cell index
     */
    public CellIndex getPrevCell() {
        return prevCell;
    }

    /**
     * Gets the current orientation angle in radians.
     *
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Gets the speed of the ant.
     *
     * @return the speed value
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the current state of the ant.
     *
     * @return the current state
     */
    public AntState getState() {
        return state;
    }

    /**
     * Gets the current role of the ant.
     *
     * @return the current role
     */
    public AntRole getRole() {
        return role;
    }

    /**
     * Check if the ant is carrying food.
     *
     * @return true if the ant is carrying food
     */
    public boolean isCarryingFood() {
        return carryingFood;
    }

    /**
     * Sets the state of the ant.
     *
     * @param state the ant state
     */
    public void setState(final AntState state) {
        this.state = Objects.requireNonNull(state);
    }

    /**
     * Manually sets the orientation angle of the ant.
     *
     * @param angle the new angle in radians
     */
    public void setAngle(final double angle) {
        this.angle = normalizeAngle(angle);
    }

    /**
     * Managers ant movement, collisions with obstacles by trying alternate direction.
     *
     * @param tick the time step duration
     * @param world the world instance
     */
    public void move(final double tick, final World world) {
        Objects.requireNonNull(world);
        this.prevCell = world.convertToCellIndex(this.position);
        // Movement calculation
        final double dx = speed * Math.cos(angle) * tick;
        final double dy = speed * Math.sin(angle) * tick;
        final WorldPosition nextPos = new WorldPosition(position.x() + dx, position.y() + dy);

        // Collision controls
        if (!world.isBlockedAt(nextPos)) {
            this.position = nextPos;
        } else {
            // try loop instead of bounce
            for (int i = 0; i < 8; i++) {
                final double tryAngle = normalizeAngle(this.angle + Math.PI + (Math.random() - 0.5) * Math.PI);
                final double testX = position.x() + speed * tick * Math.cos(tryAngle);
                final double testY = position.y() + speed * tick * Math.sin(tryAngle);
                if (!world.isBlockedAt(new WorldPosition(testX, testY))) {
                    this.angle = tryAngle;
                    this.position = new WorldPosition(testX, testY);
                    break;
                }
            }
        }
    }

    /**
     * Switches ant state after picking up food.
     */
    public void pickFood() {
        if (carryingFood) {
            throw new IllegalStateException("Ant is already carrying food");
        }
        carryingFood = true;
        state = AntState.RETURNING_TO_NEST;
        this.angle = normalizeAngle(this.angle + Math.PI); // Reverse direction
    }

    /**
     * Switches ant state after dropping food at the nest.
     */
    public void dropFood() {
        if (!carryingFood) {
            throw new IllegalStateException("Ant is not carrying food");
        }
        carryingFood = false;
        state = AntState.WANDERING;
        this.angle = normalizeAngle(this.angle + Math.PI); // Reverse direction
    }

    /**
     * Normalizes an angle to be in this range [0, 2pi).
     *
     * @param ang the input angle in radians
     * @return the normalized angle in radians
     */
    private double normalizeAngle(final double ang) {
        double normalized = ang % (2 * Math.PI);
        if (normalized < 0) {
            normalized += 2 * Math.PI;
        }
        return normalized;
    }
}
