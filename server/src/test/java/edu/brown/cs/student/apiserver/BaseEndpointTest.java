package edu.brown.cs.student.apiserver;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.handlers.CheckSolutionHandler;
import edu.brown.cs.student.main.server.handlers.CreateSessionHandler;
import edu.brown.cs.student.main.server.handlers.DeleteSessionHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitAddHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitBranchHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitCheckoutHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitCommitHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitLogHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitMergeHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitPullHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitPushHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitResetHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitRmHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitStashHandler;
import edu.brown.cs.student.main.server.handlers.gitHandlers.GitStatusHandler;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.MockStorage;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import okio.BufferedSource;
import okio.Okio;
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

  private static MockStorage firebaseUtils;
  /** Starts the Spark test server before any tests are run. */
  @BeforeAll
  public static void setupBeforeEverything() {
    firebaseUtils = new MockStorage();
    SparkTestServer.startServer();
  }

  /**
   * Sets up the test environment before each test case. Registers API endpoints and initializes the
   * Spark server.
   */
  @BeforeEach
  public void setup() {

    // Register endpoints with the server

    try {

      // Setting up the handlers for the endpoints
      Spark.get("createsession", new CreateSessionHandler(firebaseUtils));
      Spark.get("deletesession", new DeleteSessionHandler(firebaseUtils));
      Spark.get("checksolution", new CheckSolutionHandler(firebaseUtils));
      Spark.get("gitadd", new GitAddHandler(firebaseUtils));
      Spark.get("gitbranch", new GitBranchHandler(firebaseUtils));
      Spark.get("gitcheckout", new GitCheckoutHandler(firebaseUtils));
      Spark.get("gitcommit", new GitCommitHandler(firebaseUtils));
      Spark.get("gitlog", new GitLogHandler(firebaseUtils));
      Spark.get("gitmerge", new GitMergeHandler(firebaseUtils));
      Spark.get("gitpull", new GitPullHandler(firebaseUtils));
      Spark.get("gitpush", new GitPushHandler(firebaseUtils));
      Spark.get("gitreset", new GitResetHandler(firebaseUtils));
      Spark.get("gitrm", new GitRmHandler(firebaseUtils));
      Spark.get("gitstash", new GitStashHandler(firebaseUtils));
      Spark.get("gitstatus", new GitStatusHandler(firebaseUtils));

      Spark.notFound(
          (request, response) -> {
            response.status(404); // Not Found
            System.out.println("ERROR");
            return "404 Not Found - The requested endpoint does not exist.";
          });
      Spark.init();
      Spark.awaitInitialization();

      System.out.println("Test server started.");
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(
          "Error: Could not initialize Firebase. Likely due to firebase_config.json not being found. Exiting.");
      System.exit(1);
    }
  }

  /**
   * Cleans up the test environment after each test case. Unregisters API endpoints and stops the
   * Spark server.
   */
  @AfterEach
  public void teardown() {
    Spark.unmap("createsesion");
    Spark.unmap("deletesession");
    Spark.unmap("checksolution");
    Spark.unmap("gitadd");
    Spark.unmap("gitbranch");
    Spark.unmap("gitcheckout");
    Spark.unmap("gitcommit");
    Spark.unmap("gitlog");
    Spark.unmap("gitmerge");
    Spark.unmap("gitpull");
    Spark.unmap("gitpush");
    Spark.unmap("gitreset");
    Spark.unmap("gitrm");
    Spark.unmap("gitstash");
    Spark.unmap("gitstatus");
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

  protected static String serializeFileMap(Map<String, List<MockFileObject>> fileMap) {
    Moshi moshi = new Moshi.Builder().build();
    Type type =
        Types.newParameterizedType(
            Map.class, String.class, Types.newParameterizedType(List.class, MockFileObject.class));
    JsonAdapter<Map<String, List<MockFileObject>>> adapter = moshi.adapter(type);
    return adapter.toJson(fileMap);
  }

  protected static Map<String, List<MockFileObject>> deserializeFileMap(String fileMapJson) {
    try {
      // Initializes Moshi
      Moshi moshi = new Moshi.Builder().build();

      // Initializes an adapter to a parametrized List class then uses it to parse the JSON.
      Type type =
          Types.newParameterizedType(
              Map.class,
              String.class,
              Types.newParameterizedType(List.class, MockFileObject.class));

      JsonAdapter<Map<String, List<MockFileObject>>> adapter = moshi.adapter(type);

      Map<String, List<MockFileObject>> fileMap = adapter.fromJson(fileMapJson);

      return fileMap;
    } catch (Exception e) {
      // for debugging purposes
      e.printStackTrace();
      throw new RuntimeException("In deserializeFileMap: " + e.getMessage());
    }
  }

  /**
   * Returns a deserialized response map from the given connection.
   *
   * @param connection - HttpURLConnection returned by a request
   * @return - a responsse map of strings to objects
   * @throws IOException - if there is a problem reading input stream
   */
  protected static Map<String, Object> deserializeResponse(HttpURLConnection connection)
      throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    // Read from connection input stream
    BufferedSource source = Okio.buffer(Okio.source(connection.getInputStream()));

    // Parse JSON into a Map<String, Object>
    Map<String, Object> response = (Map<String, Object>) moshi.adapter(type).fromJson(source);
    return response;
  }
}
