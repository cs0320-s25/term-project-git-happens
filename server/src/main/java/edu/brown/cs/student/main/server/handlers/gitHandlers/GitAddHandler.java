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
    //unique session name
    String sessionId = request.queryParams("session_id");
    //name of currently checked out branch
    String branchId = request.queryParams("branch_id");
    //Map<String, List<Object>> map of all current filename : file entries, assuming user always using add -A
    String fileMapJson = request.queryParams("file_map_json");

    if (sessionId == null || branchId == null || fileMapJson == null) {
      String errorArg = sessionId==null?"session_id, ":"" + branchId==null?"branch_id, ":"" + fileMapJson==null?"file_map_json":fileMapJson;
      return returnErrorResponse("error_bad_request", "null parameter(s)", errorArg);
    }
    storage.addChange(sessionId, branchId, fileMapJson);
    responseMap.put("session_id", sessionId);
    responseMap.put("branch_id", branchId);
    responseMap.put("action", "add -A");
    } catch (Exception e) {
      return returnErrorResponse("error_database", "git add failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
