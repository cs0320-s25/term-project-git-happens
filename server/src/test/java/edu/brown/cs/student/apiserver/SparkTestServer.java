package edu.brown.cs.student.apiserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import spark.Spark;

/**
 * Utility class for managing the {@link Spark} test server. Ensures that the server starts only
 * once during test execution.
 */
public class SparkTestServer {
  private static boolean started = false;

  /**
   * Starts the {@link Spark} test server if it has not been started already. Sets the server to run
   * on an ephemeral port and suppresses logging output.
   */
  public static void startServer() {
    if (!started) {
      Spark.port(0);
      Logger.getLogger("").setLevel(Level.WARNING);
      Spark.init();
      started = true;
    }
  }
}
