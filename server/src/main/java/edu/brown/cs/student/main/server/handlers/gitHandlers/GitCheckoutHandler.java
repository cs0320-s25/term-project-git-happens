package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitCheckoutHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitCheckoutHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();
    final String sessionId = request.queryParams("session_id");
    final String currentBranch = request.queryParams("current_branch_id");
    final String newBranch = request.queryParams("new_branch_id");
    final String fileMapJson = request.queryParams("file_map_json");

    if (sessionId == null || currentBranch == null || newBranch == null || fileMapJson == null) {
      String errorArg = (sessionId==null?"session_id":"") + (currentBranch==null?", current_branch_id":"")
          + (newBranch==null?", new_branch_id":"") + (fileMapJson==null?", file_map_json":"");
      return returnErrorResponse("error_bad_request", "Null parameter(s)", errorArg);
    }
    try {
      Map<String, Object> latestCommit = storage.getLocalState().get("latest_commit");
      if (latestCommit == null) {
        //TODO: check file_map_json against local storage for changes that need to be committed. if so prompt commit
      } else {
        //TODO: check if the current project state matches the last commit, if not, prompt commit
      }
      //TODO: if all changes have been committed, save local state of filemap and switch branches

    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}