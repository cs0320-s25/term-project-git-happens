package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class GitAddHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitAddHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    try {
      responseMap = new HashMap<>();
      // session id
      String sessionId = request.queryParams("session_id");
      // user id
      String userId = request.queryParams("user_id");
      // name of currently checked out branch
      String branchId = request.queryParams("branch_id");
      // Map<String, List<Object>> map of all current filename : file entries, assuming user always
      // using add -A
      String fileMapJson = request.queryParams("file_map_json");

      if (sessionId == null) {
        return returnErrorResponse("error_bad_request", "null parameter", "session_id");
      } else {
        responseMap.put("session_id", sessionId);
      }
      if (userId == null) {
        return returnErrorResponse("error_bad_request", "null parameter", "user_id");
      } else {
        responseMap.put("user_id", userId);
      }
      if (branchId == null) {
        return returnErrorResponse("error_bad_request", "null parameter", "branch_id");
      } else {
        responseMap.put("branch_id", branchId);
      }
      if (fileMapJson == null) {
        return returnErrorResponse("error_bad_request", "null parameter", "file_map_json");
      } else {
        responseMap.put("file_map_json", fileMapJson);
      }
      storage.addChange(sessionId, userId, branchId, fileMapJson);
      responseMap.put("action", "add -A");
    } catch (Exception e) {
      return returnErrorResponse("error_database", "add_failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
