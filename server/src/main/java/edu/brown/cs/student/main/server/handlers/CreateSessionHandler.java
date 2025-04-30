package edu.brown.cs.student.main.server.handlers;

import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.List;
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
    responseMap = new HashMap<>();
    // request must contain "session_name"

    final String sessionName = request.queryParams("session_name");
    if (sessionName == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "session_name");
    } else {
      responseMap.put("session_name", sessionName);
    }

    // TODO: check if session already exists
    List<String> existingSessionNames = storage.getAllSessions();
    //     if (// check if session exists) {
    //       return returnErrorResponse("error_bad_request", "session_name_already_in_use");
    //     }

    try {
      // make session
    } catch (Exception e) {
      return returnErrorResponse("error_bad_request", "session_creation_failed");
    }

    return returnSuccessResponse();
  }
}
