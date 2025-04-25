package edu.brown.cs.student.apiserver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import spark.Spark;

// Code structure mostly from 2/6 live code

/**
 * Abstract base class for endpoint tests. This class sets up and tears down the {@link Spark} test
 * server before and after each test execution, ensuring a consistent test environment.
 */
public abstract class BaseEndpointTest {

  /** Starts the Spark test server before any tests are run. */
  @BeforeAll
  public static void setupBeforeEverything() {
    SparkTestServer.startServer();
  }

  /**
   * Sets up the test environment before each test case. Registers API endpoints and initializes the
   * Spark server.
   */
  @BeforeEach
  public void setup() {

    // Register endpoints with the server
    // Spark.get("loadcsv", new LoadEndpointHandler(lazyParser));

    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * Cleans up the test environment after each test case. Unregisters API endpoints and stops the
   * Spark server.
   */
  @AfterEach
  public void teardown() {
    // Spark.unmap("loadcsv");
    Spark.awaitStop();
  }

  /**
   * Helper method for making GET requests to the {@link Spark} API.
   *
   * @param apiCall The API endpoint to request (e.g., "loadcsv").
   * @return An HttpURLConnection object representing the connection to the API endpoint.
   * @throws IOException If an I/O exception occurs while making the request.
   */
  protected static HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }
}
