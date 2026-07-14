package it.unibo.antsim.pheromone;

public record Evaporation(double decayRate) implements EvaporationModel {
    public Evaporation{
        if(decayRate<0 || decayRate>1){
            throw new IllegalArgumentException("Decay rate must be between 0 and 1");
        }
    }

    @Override
    public double decay(double currentLevel) {
        return Math.max(0.0, currentLevel - decayRate);
    }
}
