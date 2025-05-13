package edu.brown.cs.student.main.server.handlers.gitHandlers;

import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitStashHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitStashHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();
    GitDiffHelper gitDiffHelper = new GitDiffHelper();

    // session id
    final String sessionId = request.queryParams("session_id");
    // user id
    final String userId = request.queryParams("user_id");
    // name of currently checked out branch
    final String branchId = request.queryParams("branch_id");
    // desired command (either "" for create stash, "list" for viewing all stashes, or "pop" for
    // applying specific stash to working directory
    final String stashRequest = request.queryParams("stash_request");
    // current file map json including unstaged changes
    final String fileMapJson = request.queryParams("file_map_json");

    // parameter optional, only used with "stash pop"
    final String stashIndex;

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
    if (stashRequest == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "stash_request");
    } else {
      responseMap.put("stash_request", stashRequest);
    }
    if (fileMapJson == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "file_map_json");
    } else {
      responseMap.put("file_map_json", fileMapJson);
    }

    try {
      // stash current changes and return last local commit's filemap for updating user's workspace
      // and message for terminal display
      switch (stashRequest) {
        case "" -> {
          String stashMessage = storage.addStash(sessionId, userId, branchId, fileMapJson);
          Map<String, Object> latestLocalCommit =
              storage.getLatestLocalCommit(sessionId, userId, branchId);
          responseMap.put("reset_file_map_json", latestLocalCommit.get("file_map_json"));
          responseMap.put("message", "Saved working directory " + stashMessage);
          responseMap.put("action", "add stash");

          // list all stashes
        }
        case "list" -> {
          List<Map<String, Object>> stashes = storage.getStashes(sessionId, userId);
          responseMap.put("stashes", stashes);
          responseMap.put("action", "list stashes");

          // apply stash at inputted index
        }
        case "pop" -> {
          // check for required parameter index
          stashIndex = request.queryParams("stash_index");
          if (stashIndex == null) {
            return returnErrorResponse("error_bad_request", "null parameter", "stash_index");
          } else {
            responseMap.put("stash_index", stashIndex);
          }

          Map<String, Object> stash =
              storage.popStash(sessionId, userId, Integer.parseInt(stashIndex));

          // if stash not found, return message for terminal display
          if (stash == null) {
            returnErrorResponse(
                "error_database",
                "Error: stash@{"
                    + stashIndex
                    + "} not found. (Hint: use 'stash list' to view available stashes)");
          }

          // if stash found, attempt to merge current filemap and stashed filemap
          Map<String, List<MockFileObject>> stashedFileMap =
              deserializeFileMap((String) stash.get("file_map_json"));
          Map<String, List<MockFileObject>> currentFileMap = deserializeFileMap(fileMapJson);

          // add any new local files to incoming filemap
          gitDiffHelper.detectNewFiles(currentFileMap, stashedFileMap);
          List<String> newLocalFiles = gitDiffHelper.getNewLocalFiles();
          for (String fileName : newLocalFiles) {
            stashedFileMap.put(fileName, currentFileMap.get(fileName));
          }

          // add any new incoming files to local filemap
          List<String> newIncomingFiles = gitDiffHelper.getNewIncomingFiles();
          for (String fileName : newIncomingFiles) {
            currentFileMap.put(fileName, stashedFileMap.get(fileName));
          }

          // stores the resulting merged files
          Map<String, List<MockFileObject>> mergedFileMap = new HashMap<>();

          // now that both maps have the same files, attempt to auto-merge each file and store
          // resulting List<Ingredients>
          for (String fileName : currentFileMap.keySet()) {
            List<MockFileObject> committedFile = currentFileMap.get(fileName);
            List<MockFileObject> incomingFile = stashedFileMap.get(fileName);
            List<MockFileObject> mergedFile =
                gitDiffHelper.autoMergeIfPossible(fileName, committedFile, incomingFile);
            if (mergedFile != null) {
              mergedFileMap.put(fileName, mergedFile);
            }
          }
          responseMap.put("merged_files", mergedFileMap);

          // if there were conflicting files, return successfully merged files and info for
          // conflicting files
          // for terminal display
          //  file_conflicts map looks like:
          //                    {filename : {"local": List<Ingredients>, "incoming":
          // List<Ingredients>}}

          if (!gitDiffHelper.getFileConflicts().isEmpty()) {
            responseMap.put("file_conflicts", gitDiffHelper.getFileConflicts());
            returnErrorResponse(
                "error_database",
                "Error: Could not apply "
                    + "stash@{"
                    + stashIndex
                    + "}. Conflict detected; fix conflicts and then commit the results.");
          } else {
            responseMap.put("action", "stash pop");
            responseMap.put(
                "message",
                "Applied stash@{"
                    + stashIndex
                    + "} ("
                    + stash.get("stash_message")
                    + ") and removed from stashes list.");
          }
        }
        default -> returnErrorResponse("error_database", "Command not supported.");
      }
    } catch (Exception e) {
      return returnErrorResponse("", "");
    }

    return returnSuccessResponse();
  }
}
