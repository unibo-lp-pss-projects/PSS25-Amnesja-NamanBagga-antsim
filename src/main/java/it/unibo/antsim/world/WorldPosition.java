package it.unibo.antsim.world;

/**
 * WorldPosition represents a continuous 2D position in the simulation.
 *
 * @param x x coordinate
 * @param y y coordinate
 */
public record WorldPosition(double x, double y) {

    /**
     * Distance to another position double.
     *
     * @param other the other position
     * @return the calculated distance from another position
     */
    public double distanceToAnotherPosition(final WorldPosition other) {
        return Math.hypot(this.x - other.x, this.y - other.y);
    }

    /**
     * Add world position.
     *
     * @param deltaX the x pos
     * @param deltaY the y pos
     * @return the new world position
     */
    public WorldPosition add(final double deltaX, final double deltaY) {
        return new WorldPosition(this.x + deltaX, this.y + deltaY);
    }
}
