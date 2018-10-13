/**
 * Represents a tile of the game map.
 */
public final class Tile implements Comparable<Tile> {

    private final int row;

    private final int col;

    /**
     * Creates new {@link Tile} object.
     *
     * @param row row index
     * @param col column index
     */
    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns row index.
     *
     * @return row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns column index.
     *
     * @return column index
     */
    public int getCol() {
        return col;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return row * Ants.MAX_MAP_SIZE + col;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Tile) {
            Tile tile = (Tile) o;
            result = row == tile.row && col == tile.col;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return row + " " + col;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Tile o) {
        return hashCode() - o.hashCode();
    }

    // A* stuff
    private int h;
    private int g;
    private int f;
    private Tile previous;

    /**
     * @return G cost in A* (distance from start tile to this tile)
     */
    public int getG() {
        return g;
    }

    /**
     * @param cost G cost in A*
     */
    public void setG(int cost) {
        this.g = cost;
    }

    /**
     * @return H cost in A* (heuristic distance from this tile to goal tile)
     */
    public int getH() {
        return h;
    }

    /**
     * @param cost H cost in A*
     */
    public void setH(int cost) {
        this.h = cost;
    }

    /**
     * @return F cost in A* (total distance from start tile to goal tile via this tile)
     */
    public int getF() {
        return f;
    }

    /**
     * @param cost F cost in A*
     */
    public void setF(int cost) {
        this.f = cost;
    }

    /**
     * @param tile previous tile in path computed with A*
     */
    public void setPrevious(Tile tile) {
        this.previous = tile;
    }

    /**
     * @return previous tile in path computed with A*
     */
    public Tile getPrevious() {
        return previous;
    }

}
