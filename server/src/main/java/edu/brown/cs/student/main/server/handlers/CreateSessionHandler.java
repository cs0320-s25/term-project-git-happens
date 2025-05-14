package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import spark.Request;
import spark.Response;

public class CreateSessionHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  /** Creates a create session endpoint handler that allows users to create a new session. */
  public CreateSessionHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  /**
   * Handles HTTPrequests for creating a new session.
   *
   * @param request The request object containing HTTP request details. The request must include a
   *     {@code "session_name"} parameter.
   * @param response UNUSED. The response object used to modify the response.
   * @return A JSON-formatted success response if the session is created successfully, or an error
   *     response otherwise.
   * @throws Exception If an unexpected error occurs during session creation.
   */
  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    try {
      responseMap = new HashMap<>();

      // request must contain "session_id"
      final String sessionId = request.queryParams("session_id");
      // request must contain user_id
      final String userId = request.queryParams("user_id");
      // file map of what the user sees at the beginning of each game session
      final String originalFileMap = request.queryParams("file_map_json");
      if (sessionId == null) {
        return returnErrorResponse("error_bad_request", "null_parameter", "session_id");
      } else {
        responseMap.put("session_id", sessionId);
      }
      if (userId == null) {
        return returnErrorResponse("error_bad_request", "null_parameter", "user_id");
      } else {
        responseMap.put("user_id", userId);
      }
      if (originalFileMap == null) {
        return returnErrorResponse("error_bad_request", "null_parameter", "file_map_json");
      } else {
        responseMap.put("file_map_json", originalFileMap);
      }

      // setup main branch for the game if not already created and add local user
      storage.addSession(sessionId, userId, originalFileMap);
      responseMap.put("action", "session_created");

    } catch (Exception e) {
      return returnErrorResponse("error_database", "session_creation_failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
