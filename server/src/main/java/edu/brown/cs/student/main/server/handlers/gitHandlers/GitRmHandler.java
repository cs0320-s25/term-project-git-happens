package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitRmHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitRmHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();

    // unique session id
    final String sessionId = request.queryParams("session_id");
    // unique user id
    final String userId = request.queryParams("user_id");
    // id of currently checked out branch
    final String branchId = request.queryParams("branch_id");
    // json of file map with file the user wishes to delete already removed
    final String fileMapJson = request.queryParams("file_map_json");

    if (sessionId == null) {
      returnErrorResponse("error_bad_request", "null parameter", "session_id");
    } else {
      responseMap.put("session_id", sessionId);
    }
    if (userId == null) {
      returnErrorResponse("error_bad_request", "null parameter", "user_id");
    } else {
      responseMap.put("user_id", userId);
    }
    if (branchId == null) {
      returnErrorResponse("error_bad_request", "null parameter", "branch_id");
    } else {
      responseMap.put("branch_id", branchId);
    }
    if (fileMapJson == null) {
      returnErrorResponse("error_bad_request", "null parameter", "file_map_json");
    } else {
      responseMap.put("file_map_json", fileMapJson);
    }

    try {
      storage.addChange(sessionId, userId, branchId, fileMapJson);
      responseMap.put("action", "rm");
    } catch (Exception e) {
      return returnErrorResponse("error_database", "rm_failed" + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
