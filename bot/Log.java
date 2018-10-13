import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Simple logging utility.
 */
public final class Log {

    private static PrintWriter out;

    public static void start(final String fileName) {
        try {
            out = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            // don't care
        }
    }

    public static void printf(final String format, final Object... args) {
        if (out != null) {
            out.printf(format, args);
            out.flush();
        }
    }

}
