package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class DeleteSessionHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public DeleteSessionHandler(StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    responseMap = new HashMap<>();
    // id of session to delete
    final String sessionId = request.queryParams("session_id");

    if (sessionId == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "session_id");
    } else {
      responseMap.put("session_id", sessionId);
    }

    try {
      if (!storage.getAllSessions().contains(sessionId)) {
        return returnErrorResponse("error_database", "session does not exist", "session_id");
      }
      storage.deleteSession(sessionId);
      responseMap.put("action", "session deleted");
    } catch (Exception e) {
      return returnErrorResponse("error_database", "delete_session_failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
