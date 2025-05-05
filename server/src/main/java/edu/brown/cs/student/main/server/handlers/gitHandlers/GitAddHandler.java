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
    //name of currently checked out branch
    String branchId = request.queryParams("branch_id");
    //Map<String, List<Object>> map of all current filename : file entries, assuming user always using add -A
    String fileMapJson = request.queryParams("file_map_json");

    if (branchId == null || fileMapJson == null) {
      return returnErrorResponse("error_bad_request", "null parameter(s)", (branchId==null?"branch_id, ":"") + (fileMapJson==null?"file_map_json":fileMapJson));
    }
    storage.addChange(branchId, fileMapJson);
    responseMap.put("branch_id", branchId);
    responseMap.put("action", "add -A");
    } catch (Exception e) {
      return returnErrorResponse("error_database", "git add failed: " + e.getMessage());
    }
    return returnSuccessResponse();
  }
}
