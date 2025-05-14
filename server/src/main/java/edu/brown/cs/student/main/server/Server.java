package edu.brown.cs.student.main.server;

import static spark.Spark.after;

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
import edu.brown.cs.student.main.server.storage.FirebaseUtilities;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.io.IOException;
import spark.Spark;

/** The main API Server. Uses {@link Spark} to open the endpoints {@code "..."} */
public class Server {
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
          response.header("Access-Control-Allow-Methods", "*");
        });

    final StorageInterface firebaseUtils;
    try {
      firebaseUtils = new FirebaseUtilities();

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

      System.out.println("Server started at http://localhost:" + port);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(
          "Error: Could not initialize Firebase. Likely due to firebase_config.json not being found. Exiting.");
      System.exit(1);
    }
  }
}
