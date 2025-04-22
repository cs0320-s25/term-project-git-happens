package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import spark.Spark;

/**
 * The main API Server. Uses {@link Spark} to open the endpoints {@code "..."}
 */
public class Server {
  private static final Integer CACHE_TIMEOUT = null;
  private static final int CACHE_SIZE = 100;

  /**
   * Entry point for the program. Launches the API server on port {@code 3232}
   *
   * @param args IGNORED
   */
  public static void main(String[] args) {

    int port = 3232;
    Spark.port(port);
    /*
       Setting CORS headers to allow cross-origin requests from the client;
       this is necessary for the client to
       be able to make requests to the server.

       By setting the Access-Control-Allow-Origin header to "*", we allow requests from any origin.
       This is not a good idea in real-world applications,
       since it opens up your server to cross-origin requests
       from any website. Instead, you should set this header to the origin of your client,
       or a list of origins that you trust.

       By setting the Access-Control-Allow-Methods header to "*", we allow requests with any HTTP
       method. Again, it's generally better to be more specific here and only allow the methods
       you need, but for this demo we'll allow all methods.
    */
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET");
        });

    // Setting up the handlers for the endpoints
    //Spark.get("loadcsv", new LoadEndpointHandler(lazyParser));
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }
}
