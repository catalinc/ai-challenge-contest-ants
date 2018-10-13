/**
 * Represents a route from one tile to another.
 */
public final class Route implements Comparable<Route> {
    private final Tile start;

    private final Tile end;

    private int distance;

    public Route(final Tile start, final Tile end, final int distance) {
        this.start = start;
        this.end = end;
        this.distance = distance;
    }

    public Route(final Tile start, final Tile end) {
        this.start = start;
        this.end = end;
    }

    public Tile getStart() {
        return start;
    }

    public Tile getEnd() {
        return end;
    }

    public int getDistance() {
        return distance;
    }

    public int compareTo(Route route) {
        return distance - route.distance;
    }

    @Override
    public int hashCode() {
        return start.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE + end.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Route) {
            Route route = (Route)o;
            result = start.equals(route.start) && end.equals(route.end);
        }
        return result;
    }

    @Override
    public String toString() {
        return start + " -> " + end + " (" + distance + ")";
    }

}