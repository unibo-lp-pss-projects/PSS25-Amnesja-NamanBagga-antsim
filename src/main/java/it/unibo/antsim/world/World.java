package it.unibo.antsim.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/*
* Main aggregate of the world module that bridges the discrete grid an continuous sppace.
* Manages the lifecycle of the nest and spatial interaction between cells
*/
public class World {
    private final Grid grid;
    private final double cellWidth;
    private final double cellHeight;
    private CellIndex nestindex;

    public World(int rows, int cols, double cellWidth, double cellHeight){
        if(cellWidth <= 0 || cellHeight <= 0){
            throw new IllegalArgumentException("Cell dimensions must be positive");
        }

        this.grid = new Grid(rows, cols);
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.nestindex = null; // The nest will be positioned after
    }

    // Getters
    public int getRows(){
        return grid.getRows();
    }

    public int getColumns(){
        return grid.getColumns();
    }

    public double getWidth(){
        return grid.getColumns() * cellWidth;
    }

    public double getHeight(){
        return grid.getRows() * cellHeight;
    }

    public Grid getGrid(){
        return this.grid;
    }

    /**
     * Returns the index of the nest in the grid.
     */
    public CellIndex getNestIndex(){
        return this.nestindex;
    }

    /*
    * This method recolates the nest after deleting the previous one by making it Empty
    * and sets the new nest position in the world
    */
    public void relocateNest(CellIndex newIndex){
        if(!grid.isInside(newIndex)){
            throw new IndexOutOfBoundsException("Nest index is outside the grid");
        }

        if(this.nestindex != null){
            // Remove the old nest
            grid.setCellContent(this.nestindex, new CellContent.Empty());
        }
        this.nestindex = newIndex;
        grid.setCellContent(newIndex, new CellContent.Nest());
    }

    /*
    * This method converts a continous two dimensional coordinate into a corresponding cell index
    */
    public CellIndex convertToCellIndex(WorldPosition pos){
        int row = (int) Math.floor(pos.y() / cellHeight);
        int col = (int) Math.floor(pos.x() / cellWidth);

        // Clamp the values to ensure they are within the grid bounds
        row = Math.clamp(row, 0, grid.getRows() - 1);
        col = Math.clamp(col, 0, grid.getColumns() - 1);

        return new CellIndex(row, col);
    }

    /*
    * This method calculates the neighbors cells index which aren't obstacles
    */
    public List<CellIndex> getWalkableNeighbors(CellIndex index){
        List<CellIndex> neighbors = new ArrayList<>();
        if(!grid.isInside(index)){
            return neighbors;
        }

        for(int dRow=-1; dRow<=1; dRow++){
            for(int dCol=-1; dCol<=1; dCol++){
                if(dRow == 0 && dCol == 0){
                    continue; // Skip the current cell
                }
                CellIndex neighborIndex = new CellIndex(index.row() + dRow, index.column() + dCol);
                if(grid.isInside(neighborIndex)){
                    CellContent content = grid.getCellAt(neighborIndex).getCellContent();
                    if(!(content instanceof CellContent.Obstacle)){
                        neighbors.add(neighborIndex);
                    }
                }
            }
        }
        return neighbors;
    }

    /*
    * This method verify if a position in the world is blocked by an obstacle or if it's out of boundries
    */
    public boolean isBlockedAt(WorldPosition pos){
        int col = (int) Math.floor(pos.x() / cellWidth);
        int row = (int) Math.floor(pos.y() / cellHeight);

        // In case is out of boundries, it will be considered as blocked
        if(row < 0 || row >= grid.getRows() || col < 0 || col >= grid.getColumns()){
            return true;
        }

        return grid.getCellAt(new CellIndex(row, col)).getCellContent() instanceof CellContent.Obstacle;
    }

    /*
    * This method verify if there is food at a given position in the world
    */
    public boolean isFoodAt(WorldPosition pos){
        CellIndex index = convertToCellIndex(pos);
        return grid.getCellAt(index).getCellContent() instanceof CellContent.Food;
    }

    public boolean consumeFood(CellIndex index){
        if(!grid.isInside(index)){
            return false;
        }
        Cell cell = grid.getCellAt(index);
        if(cell.getCellContent() instanceof CellContent.Food food && food.quantity()>0){
            cell.consumeFood(1);
            return true;
        }
        return false;
    }
    /*
    * This method verify if there is a nest at a given position in the world
    */
    public boolean isNestAt(WorldPosition pos){
        CellIndex index = convertToCellIndex(pos);
        return grid.getCellAt(index).getCellContent() instanceof CellContent.Nest;
    }

    /*
    * This method searches a cell that contains food near the position in the world.
    * Check the first cell of that pos, and if there isn't food it checks the other 8*/
    public Optional<CellIndex> findFoodCellNear(WorldPosition pos){
        CellIndex center = convertToCellIndex(pos);

        // Check the cell where is the agent
        if(grid.isInside(center) && grid.getCellAt(center).getCellContent() instanceof CellContent.Food){
            return Optional.of(center);
        }

        // Check the neighbors (radius = 1):
        for(int dRow=-1; dRow<=1; dRow++){
            for(int dCol=-1; dCol<=1; dCol++){
                if(dRow == 0 && dCol == 0){
                    continue; // Skip the current cell
                }
                CellIndex neighborIndex = new CellIndex(center.row() + dRow, center.column() + dCol);
                if(grid.isInside(neighborIndex) && grid.getCellAt(neighborIndex).getCellContent() instanceof CellContent.Food){
                    return Optional.of(neighborIndex);
                }
            }
        }
        return Optional.empty();
    }

    public void forEachFoodCell(Consumer<CellIndex> action){
        for(int i=0; i<grid.getRows(); i++){
            for(int j=0;j<grid.getColumns(); j++){
                CellIndex index = new CellIndex(i, j);
                if(grid.getCellAt(index).getCellContent() instanceof CellContent.Food){
                    action.accept(index);
                }
            }
        }
    }
}
