/**
 * Provides basic game state handling.
 */
public abstract class Bot extends AbstractSystemInputParser {
    protected Ants ants;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(long loadTime,
                      long turnTime,
                      int rows,
                      int cols,
                      int turns,
                      int viewRadius2,
                      int attackRadius2,
                      int spawnRadius2) {
        ants = new Ants(
                    loadTime,
                    turnTime,
                    rows,
                    cols,
                    turns,
                    viewRadius2,
                    attackRadius2,
                    spawnRadius2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeUpdate() {
        ants.setTurnStartTime(System.nanoTime());
        ants.clearMyAnts();
        ants.clearEnemyAnts();
        ants.clearMyHills();
        ants.clearEnemyHills();
        ants.clearFood();
        ants.getOrders().clear();
        ants.clearVision();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWater(int row, int col) {
        ants.update(Ilk.WATER, new Tile(row, col));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAnt(int row, int col, int owner) {
        ants.update(owner > 0 ? Ilk.ENEMY_ANT : Ilk.MY_ANT, new Tile(row, col));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFood(int row, int col) {
        ants.update(Ilk.FOOD, new Tile(row, col));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAnt(int row, int col, int owner) {
        ants.update(Ilk.DEAD, new Tile(row, col));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHill(int row, int col, int owner) {
        ants.updateHills(owner, new Tile(row, col));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterUpdate() {
        ants.setVision();
    }
}
