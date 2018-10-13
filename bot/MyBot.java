import java.io.IOException;
import java.util.*;


/**
 * Starter bot implementation.
 */
public final class MyBot extends Bot {

    private Map<Tile, Tile> orders;
    private SortedSet<Tile> sortedEnemyHills;
    private SortedSet<Tile> sortedUnseenTiles;

    private Map<Route, Tile> knownRoutes;
    private List<Aim> randomDirections;

    private long timeForAStar;
    private int maxUnseenRoutesToAnalyze;
    private boolean timeLimit;

    public MyBot() {
        timeLimit = true;
    }

    /**
     * Main method executed by the game engine for starting the bot.
     *
     * @param args
     *         command line arguments
     * @throws IOException
     *         if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        MyBot bot = new MyBot();
        for (final String arg : args) {
            if (arg.equals("--bot-log")) {
                Log.start("bot.log");
            }
            if (arg.equals("--no-time-limit")) {
                bot.setTimeLimit(false);
            }
        }
        bot.readSystemInput();
    }

    public void setTimeLimit(final boolean timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public void setup(long loadTime,
                      long turnTime,
                      int rows,
                      int cols,
                      int turns,
                      int viewRadius2,
                      int attackRadius2,
                      int spawnRadius2) {
        super.setup(loadTime,
                turnTime,
                rows,
                cols,
                turns,
                viewRadius2,
                attackRadius2,
                spawnRadius2);

        orders = new HashMap<Tile, Tile>();
        sortedEnemyHills = new TreeSet<Tile>();
        sortedUnseenTiles = new TreeSet<Tile>();
        for (int row = 0; row < ants.getRows(); row++) {
            for (int col = 0; col < ants.getCols(); col++) {
                sortedUnseenTiles.add(new Tile(row, col));
            }
        }

        randomDirections = Arrays.asList(Aim.values());
        knownRoutes = new HashMap<Route, Tile>();

        timeForAStar = ants.getTurnTime() / 2;
        maxUnseenRoutesToAnalyze = 3;
    }

    @Override
    public void doTurn() {
        // clear previous orders
        orders.clear();

        // prevent stepping on our hills
        for (final Tile myHill : ants.getMyHills()) {
            orders.put(myHill, null);
        }

        // update visible tiles
        for (final Iterator<Tile> it = sortedUnseenTiles.iterator(); it.hasNext(); ) {
            final Tile next = it.next();
            if (ants.isVisible(next)) {
                it.remove();
            }
        }

        // forget conquered enemy hills
        for (Tile antLoc : ants.getMyAnts()) {
            for (final Iterator<Tile> it = sortedEnemyHills.iterator(); it.hasNext(); ) {
                final Tile hillLoc = it.next();
                if (hillLoc.equals(antLoc)) {
                    it.remove();
                }
            }
        }

        final SortedSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());

        // find close food
        final Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();
        final List<Route> foodRoutes = new ArrayList<Route>();
        final SortedSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
        for (final Tile foodLoc : sortedFood) {
            for (final Tile antLoc : sortedAnts) {
                final int distance = ants.getManhattanDistance(antLoc, foodLoc);
                final Route route = new Route(antLoc, foodLoc, distance);
                foodRoutes.add(route);
            }
        }
        Collections.sort(foodRoutes);
        for (final Route route : foodRoutes) {
            if (!foodTargets.containsKey(route.getEnd())
                    && !foodTargets.containsValue(route.getStart())) {
                if (doAStar(route)) {
                    foodTargets.put(route.getEnd(), route.getStart());
                }
            }
        }

        // add new hills to set
        for (final Tile enemyHill : ants.getEnemyHills()) {
            if (!sortedEnemyHills.contains(enemyHill)) {
                sortedEnemyHills.add(enemyHill);
            }
        }
        // attack hills
        final List<Route> hillRoutes = new ArrayList<Route>();
        for (final Tile hillLoc : sortedEnemyHills) {
            for (final Tile antLoc : sortedAnts) {
                if (!orders.containsValue(antLoc)
                        && !sortedEnemyHills.contains(antLoc)) {
                    final int distance = ants.getManhattanDistance(antLoc, hillLoc);
                    final Route route = new Route(antLoc, hillLoc, distance);
                    hillRoutes.add(route);
                }
            }
        }
        Collections.sort(hillRoutes);
        for (final Route route : hillRoutes) {
            doAStar(route);
        }

        // explore unseen areas
        for (final Tile antLoc : sortedAnts) {
            if (!orders.containsValue(antLoc)) {
                final List<Route> unseenRoutes = new ArrayList<Route>();
                for (final Tile unseenLoc : sortedUnseenTiles) {
                    final int distance = ants.getManhattanDistance(antLoc, unseenLoc);
                    final Route route = new Route(antLoc, unseenLoc, distance);
                    unseenRoutes.add(route);
                }
                Collections.sort(unseenRoutes);
                for (final Route route :
                        unseenRoutes.subList(0, Math.min(maxUnseenRoutesToAnalyze, unseenRoutes.size()))) {
                    if (doAStar(route)) {
                        break;
                    }
                }
            }
        }

        // clear my hills top
        for (final Tile hillLoc : ants.getMyHills()) {
            if (ants.getMyAnts().contains(hillLoc)
                    && !orders.containsValue(hillLoc)) {
                doRandomMove(hillLoc);
            }
        }

        Log.printf("turn: %f ms\n", (double) ants.getTimeElapsed() / 1000000);
    }

    private boolean doMoveDirection(final Tile antLoc, final Aim direction) {
        final Tile newLoc = ants.getTile(antLoc, direction);
        if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc)) {
            ants.issueOrder(antLoc, direction);
            orders.put(newLoc, antLoc);
            return true;
        } else {
            return false;
        }
    }

    private boolean doMoveLocation(final Tile antLoc, final Tile destLoc) {
        final List<Aim> directions = ants.getDirections(antLoc, destLoc);
        for (final Aim direction : directions) {
            if (doMoveDirection(antLoc, direction)) {
                return true;
            }
        }
        return false;
    }

    private boolean doRandomMove(final Tile antLoc) {
        Collections.shuffle(this.randomDirections);
        for (final Aim direction : this.randomDirections) {
            if (doMoveDirection(antLoc, direction)) {
                return true;
            }
        }
        return true;
    }

    private boolean doAStar(final Route route) {
        if (!knownRoutes.containsKey(route)) {
            if (timeLimit && ants.getTimeRemaining() <= timeForAStar) {
                return doMoveLocation(route.getStart(), route.getEnd());
            }
            final PriorityQueue<Tile> open = new PriorityQueue<Tile>(32, A_STAR_COST);
            final Map<Tile, Tile> closed = new HashMap<Tile, Tile>();
            final Tile start = route.getStart();
            final Tile goal = route.getEnd();
            Tile tile;
            open.add(start);
            while (!open.isEmpty()) {
                tile = open.poll();
                if (tile.equals(goal)) {
                    if (noUnseenTilesInPath(tile)) {
                        // certain path, register it
                        Tile t = tile;
                        while (t.getPrevious() != null) {
                            knownRoutes.put(new Route(t.getPrevious(), tile), t);
                            t = t.getPrevious();
                        }
                        Log.printf("(A* - certain path) route: %s move: %s -> %s\n", route, start, knownRoutes.get(route));
                        return doMoveLocation(start, knownRoutes.get(route));
                    } else {
                        // uncertain path (there are unseen tiles in it)
                        Tile t = tile;
                        while (!t.getPrevious().equals(start)) {
                            t = t.getPrevious();
                        }
                        Log.printf("(A* - uncertain path) route: %s move: %s -> %s\n", route, start, t);
                        return doMoveLocation(start, t);
                    }
                }
                if (timeLimit && ants.getTimeRemaining() <= timeForAStar) {
                    // time up, move towards current tile (best effort)
                    return doMoveLocation(start, tile);
                }
                closed.put(tile, tile);
                for (final Tile succ : getPassableNeighbours(tile)) {
                    final int tentativeG = tile.getG() + cost(succ);
                    if (open.contains(succ)) {
                        for (Iterator<Tile> it = open.iterator(); it.hasNext(); ) {
                            Tile next = it.next();
                            if (next.equals(succ) && tentativeG < next.getG()) {
                                it.remove();
                            }
                        }
                    }
                    if (closed.containsKey(succ) && tentativeG < closed.get(succ).getG()) {
                        closed.remove(succ);
                    }
                    if (!open.contains(succ) && !closed.containsKey(succ)) {
                        succ.setG(tentativeG);
                        succ.setH(ants.getManhattanDistance(succ, goal));
                        succ.setF(succ.getG() + succ.getH());
                        succ.setPrevious(tile);
                        open.add(succ);
                    }
                }
            }
            // no path found
            return false;
        } else {
            Log.printf("route: %s move: %s -> %s\n", route, route.getStart(), knownRoutes.get(route));
            return doMoveLocation(route.getStart(), knownRoutes.get(route));
        }
    }

    private List<Tile> getPassableNeighbours(final Tile tile) {
        final List<Tile> passable = new LinkedList<Tile>();
        for (final Tile neighbour : ants.getNeighbours(tile)) {
            if (ants.getIlk(neighbour).isPassable()) {
                passable.add(neighbour);
            }
        }
        return passable;
    }

    private int cost(final Tile tile) {
        switch (ants.getIlk(tile)) {
            case MY_ANT:
                return 3;
            case ENEMY_ANT:
                return 4;
            case LAND:
            case FOOD:
            case DEAD:
                return 1;
            default:
                throw new IllegalArgumentException(tile + " " + ants.getIlk(tile));
        }
    }

    private boolean noUnseenTilesInPath(final Tile last) {
        Tile tile = last;
        while (tile != null) {
            if (sortedUnseenTiles.contains(tile)) {
                return false;
            }
            tile = tile.getPrevious();
        }
        return true;
    }

    private static final TileAStarCostComparator A_STAR_COST = new TileAStarCostComparator();

    private static final class TileAStarCostComparator implements Comparator<Tile> {
        public int compare(final Tile o1, final Tile o2) {
            if (o1.getF() == o2.getF()) {
                return o1.getH() - o2.getH();
            }
            return o1.getF() - o2.getF();
        }
    }

}
