package it.unibo.antsim.world;

import java.util.Objects;

/*
* This class manages the grid of bidimensional cells securing the edge controls*/
public class Grid {
    private int rows;
    private int columns;
    private final Cell[][] cells;

    public Grid(int rows, int columns){
        if(rows <= 0 || columns <= 0){
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        this.rows = rows;
        this.columns = columns;
        this.cells = new Cell[rows][columns];

        for(int i=0; i<rows; i++){
            for(int j=0; j<columns; j++){
                cells[i][j] = new Cell(new CellContent.Empty());
            }
        }
    }

    // Getters
    public int getRows(){
        return this.rows;
    }
    public int getColumns(){
        return this.columns;
    }

    /*
    * This method verify if an index in inside the grid
    */
    public boolean isInside(CellIndex index){
        return index.row() >= 0 && index.row() < rows && index.column() >= 0 && index.column() < columns;
    }

    /*
    * This method returns the cell at the specified index
    */
    public Cell getCellAt(CellIndex index){
        if(!isInside(index)){
            throw new IndexOutOfBoundsException("Index is outside the grid");
        }
        return cells[index.row()][index.column()];
    }

    /*
    * This method modifies the content of the cell at the specified index
    */
    public void setCellContent(CellIndex index, CellContent content){
        Objects.requireNonNull(content);
        getCellAt(index).setCellContent(content);
    }
}
