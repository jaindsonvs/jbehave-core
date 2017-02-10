package com.lunivore.gameoflife.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Game {

    private final int width;
    private final int height;
    private GameObserver observer;
    private Set<Cell> cells = new HashSet<Cell>();

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setObserver(GameObserver observer) {
        this.observer = observer;
        notifyObserver();
    }

    private void notifyObserver() {
        observer.gridChanged(new Grid() {
            public int getHeight() { return height; }
            public int getWidth() { return width; }
            public boolean hasLife(int column, int row) { return cells.contains(new Cell(column, row)); }
            
        });
    }

    public void toggleCellAt(int column, int row) {
        Cell toggled = new Cell(column, row);
        toggleCellAt(toggled);
        notifyObserver();
    }

	private void toggleCellAt(Cell cell) {
		if (cells.contains(cell)) {
            cells.remove(cell);
        } else {
            cells.add(cell);
        }
	}

    public void nextGeneration() {
    	int minWidth = width + 1;
    	int minHeight = height + 1;
    	int maxWidth = -1;
    	int maxHeight = -1;
    	
    	// Check boundary to verify
    	for (Cell cell : cells) {
    		minWidth = Math.min(minWidth, cell.getColumn());
    		minHeight = Math.min(minHeight, cell.getRow());
    		
    		maxWidth = Math.max(maxWidth, cell.getColumn());
    		maxHeight = Math.max(maxHeight, cell.getRow());
		}
    	
    	// Make sure we check dead cells around
    	minWidth = Math.max(0, minWidth - 1);
    	minHeight = Math.max(0, minHeight - 1);
    	maxWidth = Math.min(width, maxWidth + 1);
    	maxHeight = Math.min(height, maxHeight + 1);
    	
    	switchCellsWithinBoundary(minWidth, minHeight, maxWidth, maxHeight);
    }
    
    private void switchCellsWithinBoundary(int minWidth, int minHeight, int maxWidth, int maxHeight) {
    	Map<Cell, Integer> counter = countNeighbours(minWidth, minHeight, maxWidth, maxHeight);
    	Set<Cell> cellsToToggle = getCellsToToggle(counter);
    	toggleCells(cellsToToggle);
    	notifyObserver();
	}

	private void toggleCells(Set<Cell> cellsToToggle) {
		for (Cell cell : cellsToToggle) {
    		toggleCellAt(cell);
		}
	}

	private Set<Cell> getCellsToToggle(Map<Cell, Integer> counter) {
		Set<Cell> cellsToToggle = new HashSet<>();
    	for (Entry<Cell, Integer> entry : counter.entrySet()) {
    		Cell cell = entry.getKey();
    		Integer count = entry.getValue();
    		
    		// Reference: https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
    		// 1. Any live cell with fewer than two live neighbours dies, as if caused by under-population.
    		// 2. Any live cell with two or three live neighbours lives on to the next generation.
    		// 3. Any live cell with more than three live neighbours dies, as if by over-population.
    		// 4. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
    		
    		if (hasLiveCellAt(cell)) {
    			// Live cell
    			if (count < 2) {
    				// fewer than 2 => dies
    				cellsToToggle.add(cell);
    			} else if (count == 2 || count == 3) {
    				// exactly 2 or 3 => stay alive
    			} else {
    				// more than 3 => dies
    				cellsToToggle.add(cell);
    			}
    		} else {
    			// Dead cell
    			if (count == 3) {
    				// exactly 3 => become alive
    				cellsToToggle.add(cell);
    			}
    		}
    	}
		return cellsToToggle;
	}

	private Map<Cell, Integer> countNeighbours(int minWidth, int minHeight, int maxWidth, int maxHeight) {
		Map<Cell, Integer> count = new HashMap<Cell, Integer>();
		for (int column = minWidth; column <= maxWidth; column++) {
			for (int row = minHeight; row <= maxHeight; row++) {
				int liveNeighbourCells = 0;
				
//				for (int incColumn = -1; incColumn <= 1; incColumn++) {
//					for (int incRow = -1; incRow <= 1; incRow++) {
//						liveNeighbourCells += incIfLiveCellAt(column + incColumn, row + incRow);
//					}
//				}
				liveNeighbourCells += incIfLiveCellAt(column - 1, row - 1);
				liveNeighbourCells += incIfLiveCellAt(column, row - 1);
				liveNeighbourCells += incIfLiveCellAt(column + 1, row - 1);
				
				liveNeighbourCells += incIfLiveCellAt(column + 1, row);
				
				liveNeighbourCells += incIfLiveCellAt(column + 1, row + 1);
				liveNeighbourCells += incIfLiveCellAt(column, row + 1);
				liveNeighbourCells += incIfLiveCellAt(column - 1, row + 1);
				
				liveNeighbourCells += incIfLiveCellAt(column - 1, row);
				
				count.put(new Cell(column, row), liveNeighbourCells);
			}
		}
		return count;
	}

    private int incIfLiveCellAt(int column, int row) {
    	return hasLiveCellAt(column, row) ? 1 : 0;
    }
    
	private boolean hasLiveCellAt(int column, int row) {
    	return hasLiveCellAt(new Cell(column, row));
    }
    
	private boolean hasLiveCellAt(Cell cell) {
    	return cells.contains(cell);
    }

}
