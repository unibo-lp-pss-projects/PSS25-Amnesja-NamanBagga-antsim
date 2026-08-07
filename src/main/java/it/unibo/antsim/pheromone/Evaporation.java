package it.unibo.antsim.pheromone;

public record Evaporation(double decayRate) implements EvaporationModel {
    public Evaporation{
        if(decayRate<0){
            throw new IllegalArgumentException("Decay rate must be between 0 and 1");
        }
    }

    @Override
    public double decay(double currentLevel, double dt) {
        if(dt<0){
            throw new IllegalArgumentException("Time delta must be positive!");
        }
        return currentLevel * Math.exp(-decayRate * dt);
    }
}
