package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitStatusHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitStatusHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();

    // TODO: request.queryParams for map of filename keys to Lists of Shapes (stored as arrays?)

    try {

      // TODO: fetch staging area, return status of working directory and staging area)

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
