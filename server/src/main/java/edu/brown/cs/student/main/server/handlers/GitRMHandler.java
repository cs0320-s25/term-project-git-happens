package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitRMHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitRMHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();

    // Code

    try {

      // Code

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
