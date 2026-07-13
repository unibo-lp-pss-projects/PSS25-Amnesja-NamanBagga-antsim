package it.unibo.antsim.world;
/*
* WorldPosition rappresents an continuous 2D position in the simulation
*/
public record WorldPosition(double x, double y) {

    /*
    * Calculates the distance from another position
    */
    public double distanceToAnotherPosition(WorldPosition other){
        return Math.hypot(this.x - other.x, this.y - other.y);
    }

    /*
    * Adds a vector(dx, dy) to the current position, returning a new position*/
    public WorldPosition add(double x, double y){
        return new WorldPosition(this.x + x, this.y + y);
    }
}
