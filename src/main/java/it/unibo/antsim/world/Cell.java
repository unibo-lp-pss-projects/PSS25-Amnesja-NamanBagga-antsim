package it.unibo.antsim.world;

import java.util.Objects;

/*
* This class rappresents a mutable cell of the grid that host a specific CellContet*/
public class Cell {
    private CellContent content;

    public Cell(CellContent content) {
        this.content = Objects.requireNonNull(content);
    }

    // Getters and Setters
    public CellContent getCellContent(){
        return this.content;
    }

    public void setCellContent(CellContent content){
        this.content = Objects.requireNonNull(content);
    }

    /*
    * This method allows to consume a certain amount of food from the cell, if it contains food.
    * If the food is completely consumed, the cell content is set to Empty
    */
    public void consumeFood(int amount){
        if(this.content instanceof CellContent.Food foodContent){
            CellContent.Food updatedFood = foodContent.consume(amount);
            if(updatedFood.isEmpty()){
                this.content = new CellContent.Empty();
            }else{
                this.content = updatedFood;
            }
        }
    }
}
