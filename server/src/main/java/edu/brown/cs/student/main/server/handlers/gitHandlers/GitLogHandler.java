package edu.brown.cs.student.main.server.handlers.gitHandlers;

import static edu.brown.cs.student.main.server.storage.FirestoreConstants.FIELD_DATE_TIME;
import static edu.brown.cs.student.main.server.storage.FirestoreConstants.FIELD_FILE_MAP_JSON;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitLogHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  // same formatter as in FirebaseUtilHelpers
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");

  public GitLogHandler(final StorageInterface storage) {
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
    // verbose - include commit contents
    final String verbose = request.queryParams("verbose");

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
    if (verbose == null) {
      return returnErrorResponse("error_bad_request", "null_parameter", "verbose");
    } else {
      responseMap.put("verbose", verbose);
    }
    if (!verbose.equalsIgnoreCase("true") && !verbose.equalsIgnoreCase("false")) {
      return returnErrorResponse("error_bad_request", "parameter_not_bool", "verbose");
      // verbose field is not true/True/false/False
      // we need to do this check because parseBoolean returns false for any non true/True inputs
    }
    final boolean verboseBool = Boolean.parseBoolean(verbose);
    responseMap.put("action", "log");

    try {
      List<Map<String, Object>> allCommits = storage.getAllCommits(sessionId, userId, branchId);
      if (!verboseBool) {
        allCommits.forEach(
            commit -> {
              commit.remove(FIELD_FILE_MAP_JSON);
            });
      }

      // sort using date in reverse order (newest commits first)
      allCommits.sort(
          Comparator.comparing(
              commit -> ZonedDateTime.parse((String) commit.get(FIELD_DATE_TIME), formatter),
              Comparator.reverseOrder()));

      responseMap.put("commits", allCommits);

    } catch (Exception e) {
      return returnErrorResponse("error_database", "git log failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
