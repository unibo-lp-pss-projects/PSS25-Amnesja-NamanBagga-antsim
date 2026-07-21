package it.unibo.antsim.pheromone;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;

public class PheromoneMap implements PheromoneField {
    private final int rows;
    private final int cols;
    private final double cellWidth;
    private final double cellHeight;
    private final double maxSaturation;
    private final EvaporationModel evaporationModel;

    private final double[][] foodPheromones;
    private final double[][] homePheromones;

    public PheromoneMap(int rows, int cols, double cellWidth, double cellHeight, double maxSaturation, EvaporationModel evaporationModel){
        if(rows<=0 || cols<=0 || cellWidth<=0 || cellHeight<=0 || maxSaturation<=0){
            throw new IllegalArgumentException("Rows, cols, cellWidth, cellHeight and maxSaturation must be positive");
        }
        this.rows = rows;
        this.cols = cols;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.maxSaturation = maxSaturation;
        this.evaporationModel = Objects.requireNonNull(evaporationModel);
        this.foodPheromones = new double[rows][cols];
        this.homePheromones = new double[rows][cols];
    }

    @Override
    public void deposit(CellIndex index, PheromoneType type, double intensity){
        if(index.row()>=0 && index.row()<rows && index.column()>=0 && index.column()<cols){
            double[][] grid = (type==PheromoneType.HOME) ? homePheromones : foodPheromones;
            grid[index.row()][index.column()] = Math.min(maxSaturation, grid[index.row()][index.column()] + intensity);
        }
    }

    @Override
    public void evaporate(double dt){
        for(int r=0; r<rows; r++){
            for(int c=0; c<cols; c++){
                foodPheromones[r][c] = evaporationModel.decay(foodPheromones[r][c], dt);
                homePheromones[r][c] = evaporationModel.decay(homePheromones[r][c], dt);
            }
        }
    }

    @Override
    public double level(WorldPosition pos, PheromoneType type){
        double centerX = (pos.x()/cellHeight)-0.5;
        double centerY = (pos.y()/cellWidth)-0.5;

        int c0 = (int) Math.floor(centerX);
        int r0 = (int) Math.floor(centerY);

        double a = centerX - c0;
        double b = centerY - r0;

        double v00 = safeRead(r0, c0, type);
        double v10 = safeRead(r0, c0+1, type);
        double v01 = safeRead(r0+1, c0, type);
        double v11 = safeRead(r0+1, c0+1, type);

        return (1-a)*(1-b)*v00 + a*(1-b)*v10 + (1-a)*b*v01 + a*b*v11;
    }

    private double safeRead(int r, int c, PheromoneType type){
        int clampedRow = Math.clamp(r, 0, rows-1);
        int clampedCol = Math.clamp(c, 0, cols-1);
        return (type==PheromoneType.HOME)?homePheromones[clampedRow][clampedCol]:foodPheromones[clampedRow][clampedCol];
    }
}
