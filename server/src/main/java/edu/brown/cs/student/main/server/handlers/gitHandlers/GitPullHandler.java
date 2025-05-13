package edu.brown.cs.student.main.server.handlers.gitHandlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Moshi.Builder;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;

public class GitPullHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitPullHandler(final StorageInterface storage) {
    this.storage = storage;
  }

  @Override
  public Object handle(final Request request, final Response response) throws Exception {
    responseMap = new HashMap<>();
    GitDiffHelper diffHelper = new GitDiffHelper();

    // unique session id
    final String sessionId = request.queryParams("session_id");
    // unique user id
    final String userId = request.queryParams("user_id");
    // id of currently checked out branch
    final String currentBranch = request.queryParams("branch_id");
    // json of file map representing current state of project, including unstaged changes
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
    if (currentBranch == null) {
      returnErrorResponse("error_bad_request", "null parameter", "current_branch_id");
    } else {
      responseMap.put("branch_id", currentBranch);
    }

    try {

      // get current branch's latest local commit
      Map<String, Object> currentLatestLocalCommit =
          storage.getLatestLocalCommit(sessionId, userId, currentBranch);
      Map<String, List<MockFileObject>> localCommittedFileMap =
          deserializeFileMap((String) currentLatestLocalCommit.get("file_map_json"));

      // get current branch's latest remote commit
      Map<String, Object> currentLatestRemoteCommit =
          storage.getLatestRemoteCommit(sessionId, currentBranch);
      Map<String, List<MockFileObject>> remoteCommittedFileMap =
          deserializeFileMap((String) currentLatestRemoteCommit.get("file_map_json"));

      // if local head is the same as remote head, return message for terminal display
      if (currentLatestLocalCommit
          .get("commit_id")
          .equals(currentLatestRemoteCommit.get("commit_id"))) {
        responseMap.put("message", "Already up to date.");
        returnSuccessResponse();
      }

      // add any new local files to incoming filemap
      diffHelper = new GitDiffHelper();
      diffHelper.detectNewFiles(localCommittedFileMap, remoteCommittedFileMap);
      List<String> newLocalFiles = diffHelper.getNewLocalFiles();
      for (String fileName : newLocalFiles) {
        remoteCommittedFileMap.put(fileName, localCommittedFileMap.get(fileName));
      }

      // add any new incoming files to local filemap
      List<String> newIncomingFiles = diffHelper.getNewIncomingFiles();
      for (String fileName : newIncomingFiles) {
        localCommittedFileMap.put(fileName, remoteCommittedFileMap.get(fileName));
      }

      // stores the resulting merged files
      Map<String, List<MockFileObject>> mergedFileMap = new HashMap<>();

      // now that both maps have the same files, attempt to auto-merge each file and store resulting
      // List<Ingredients>
      for (String fileName : localCommittedFileMap.keySet()) {
        List<MockFileObject> committedFile = localCommittedFileMap.get(fileName);
        List<MockFileObject> incomingFile = remoteCommittedFileMap.get(fileName);
        List<MockFileObject> mergedFile =
            diffHelper.autoMergeIfPossible(fileName, committedFile, incomingFile);
        if (mergedFile != null) {
          mergedFileMap.put(fileName, mergedFile);
        }
      }
      responseMap.put("merged_files", mergedFileMap);

      // if there were conflicting files, return successfully merged files and info for conflicting
      // files
      // for terminal display
      //  file_conflicts map looks like:
      //                    {filename : {"local": List<Ingredients>, "incoming": List<Ingredients>}}

      if (!diffHelper.getFileConflicts().isEmpty()) {
        responseMap.put("file_conflicts", diffHelper.getFileConflicts());
        returnErrorResponse(
            "error_database", "Automatic merge failed; fix conflicts and then commit the results.");
      }
      //  if there were no conflicts, add and commit merged files
      else {

        // serialize merged file map
        Moshi moshi = new Builder().build();
        Type type =
            Types.newParameterizedType(
                Map.class,
                String.class,
                Types.newParameterizedType(List.class, MockFileObject.class));
        JsonAdapter<Map<String, List<MockFileObject>>> adapter = moshi.adapter(type);

        storage.pullRemoteCommits(sessionId, userId, currentBranch);
        storage.addChange(sessionId, userId, currentBranch, adapter.toJson(mergedFileMap));
        String localCommitId = (String) currentLatestLocalCommit.get("commit_id");
        String incomingCommitId = (String) currentLatestRemoteCommit.get("commit_id");
        // for updating branch map
        responseMap.put("local_commit_id", localCommitId);
        responseMap.put("incoming_commit_id", incomingCommitId);
        String commitMessage = localCommitId + " " + incomingCommitId + " merged";
        String mergeCommitId =
            storage.commitChange(sessionId, userId, currentBranch, commitMessage);
        responseMap.put("merge_commit_id", mergeCommitId);
        responseMap.put("message", commitMessage + " automatically and changes committed.");
      }

    } catch (Exception e) {
      return returnErrorResponse("error_database", "pull_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
