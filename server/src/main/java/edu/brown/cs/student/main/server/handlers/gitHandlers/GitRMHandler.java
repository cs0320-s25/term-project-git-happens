package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
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

    final String branchName = request.queryParams("branch_name");
    final String fileName = request.queryParams("file_name");

    try {

      // Todo: remove fileName file from both "local" and "git" branchName

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
