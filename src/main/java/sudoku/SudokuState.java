package sudoku;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SudokuState {
  public SudokuCell[] cells;
  public static final int TOTAL_CELLS = 81;
  
  private SudokuState(SudokuCell[] input) {
    this.cells = input;
  }
  
  public static SudokuState fromDefinition(int[] input) {
    SudokuCell[] cells = new SudokuCell[TOTAL_CELLS];
    for(int i=0; i<TOTAL_CELLS; i++) {
      cells[i] = SudokuCell.fromDefinition(i, input[i]);
    }
    return new SudokuState(cells);
  }

  public boolean isComplete() {
    for (int i = 0; i < 81; i++) {
      if (!cells[i].done()) {
        return false;
      }
    }
    return true;
  }
  
  /** Returns the current cell as well... */
  public Set<SudokuCell> neighbors(int index) {
    List<SudokuCell> neighbors = new LinkedList<SudokuCell>();
    SudokuCell cell = cells[index];
    
    int row = cell.y();
    int col = cell.x();
        
    int sx = col/3;
    int sy = row/3;
    for(int i=0; i<9; i++) {
      neighbors.add(get(i,row));
      neighbors.add(get(col,i));
      neighbors.add(get(sx*3 + i%3, sy*3 + i/3));
    }
    
    return new HashSet<SudokuCell>(neighbors);
  }
  
  public List<Integer> legalMoves(int id) {
    SudokuCell cell = cells[id];
    if(cell.done()) return Arrays.asList(new Integer[] { cell.get() });
    
    List<Integer> out = new LinkedList<Integer>();
    int row = cell.y();
    int col = cell.x();
        
    int sx = col/3;
    int sy = row/3;
    
    // look at all neighbors and build a list of remaining legal moves
    Set<Integer> found = new HashSet<Integer>();
    for(SudokuCell neighbor : neighbors(id)) {
      if(neighbor.done())
        found.add(neighbor.get());
    }
    
    for(int i=1; i<=9; i++) {
      if(!found.contains(i) && cell.inDomain(i))
        out.add(i);
    }
    // return the legal moves as a list
    return out;
  }
  
  /**
   * MRV must calculate for each cell the number of remaining legal moves
   * @return 
   */
  public SudokuCell selectOpenMRV(){
    // a cell for each number of remaining legal moves [0..9]
    SudokuCell[] remainingVals = new SudokuCell[10];

    // count remaining moves, put in bucket
    for (int i=0; i<TOTAL_CELLS; i++) {
      int left = legalMoves(i).size();
      if(left == 1 && cells[i].done()) continue;
      remainingVals[left] = cells[i];
    }
    // find first nonempty bucket and return that
    for(int i=0; i<remainingVals.length; i++) {
      SudokuCell cur = remainingVals[i];
      if(cur != null) {
        return cur;
      }
    }
    print(System.err);
    throw new RuntimeException("did not find any open values");
  }

  public SudokuCell selectOpenVariable() {
    assert(!isComplete());
    int min = -1;
    int minLeft = 10;
    // loop over total cells, find out who is open
    for(int i=0; i<TOTAL_CELLS; i++) {
      if(cells[i].done()) continue;
      return cells[i];
    }
    throw new RuntimeException("Shouldn't ask for an open variable on a finished sudoku...");
  }

  /**
   * Create a new SudokuState by playing value at index.
   */
  public SudokuState set(int index, int value) {
    SudokuCell[] newState = new SudokuCell[TOTAL_CELLS];
    for(int i=0; i<TOTAL_CELLS; i++) {
      if(i == index) {
        newState[i] = cells[i].set(value);
      } else {
        newState[i] = cells[i].clone();
      }
    }
    return new SudokuState(newState);
  }
  
  public int count(int index) {
    return cells[index].count();
  }


  public SudokuCell get(int x, int y) {
    assert(x >= 0 && x < 9 && y >= 0 && y < 9);
    SudokuCell at = cells[x + y*9];
    assert(at != null);
    return at;
  }
  
  
  private boolean distinct(SudokuCell[] row) {
    boolean found[] = new boolean[9];
    for(int i=0; i<9; i++) {
      if(row[i].done()) {
        int index = row[i].get()-1;
        if(found[index]) {
          return false; // double thing in this set
        }
        found[index] = true;
        row[i] = null; // eliminate this from future checks
      }
    }
    
    for(SudokuCell c : row) {
      if(c == null) continue;
      //TODO check uniqueness properly of plausible results.
    }
    
    return true;
  }
  
  boolean isConsistent() {
    SudokuCell block[] = new SudokuCell[9];
    
    // check columns
    for(int x=0; x<9; x++) {
      for(int y=0; y<9; y++) {
        block[y] = get(x,y);
      }
      if(!distinct(block)) return false;
    }
    
    // check rows
    for(int y=0; y<9; y++) {
      for(int x=0; x<9; x++) {
        block[x] = get(x,y);
      }
      if(!distinct(block)) return false;
    }
    
    // check super blocks
    for(int sx=0; sx<3; sx++) {
      for(int sy=0; sy<3; sy++) {
        // fill array and check
        for(int x=0; x<3; x++) {
          for(int y=0; y<3; y++) {
            block[x+y*3] = get(sx*3+x, sy*3+y);
          }
        }
        if(!distinct(block)) return false;
      }
    }
    
    return true;
  }
  
  public static void main(String[] args) {
    
    SudokuState st = fromDefinition(new int[] {
      1,0,0,0,0,0,0,0,0,
      0,1,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0
    });
    
    assert(!st.isConsistent());
  }

  void print(PrintStream out) {
    for (int i = 0; i < cells.length; i++) {
      out.print((i % 9 == 0) ? '\n' : ' ');
      out.print(String.format("%20s",""+i+":"+cells[i]));
    }
    out.println();
  }
  
  void printSimple(PrintStream out) {
    for (int i = 0; i < cells.length; i++) {
      out.print((i % 9 == 0) ? '\n' : ' ');
      if(cells[i].done()) {
        out.print(cells[i].get());
      } else {
        out.print("?");
      }
    }
    out.println();
  }
}
