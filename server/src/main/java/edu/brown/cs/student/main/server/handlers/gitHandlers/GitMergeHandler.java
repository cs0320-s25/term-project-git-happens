package edu.brown.cs.student.main.server.handlers.gitHandlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.handlers.AbstractEndpointHandler;
import edu.brown.cs.student.main.server.mergeHelpers.GitDiffHelper;
import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;

public class GitMergeHandler extends AbstractEndpointHandler {

  final StorageInterface storage;

  public GitMergeHandler(final StorageInterface storage) {
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
    final String currentBranch = request.queryParams("current_branch_id");
    // id of branch to merge with
    final String mergeBranch = request.queryParams("merge_branch_id");
    // json of file map representing current state of project, including unstaged changes
    final String fileMapJson = request.queryParams("file_map_json");

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
    if (currentBranch == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "current_branch_id");
    } else {
      responseMap.put("current_branch_id", currentBranch);
    }
    if (mergeBranch == null) {
      return returnErrorResponse("error_bad_request", "null parameter", "merge_branch_id");
    } else {
      responseMap.put("merge_branch_id", mergeBranch);
    }

    try {

      // if current branch is same as merge branch, return message for terminal display
      if (currentBranch.equals(mergeBranch)) {
        responseMap.put("message", "Already up to date");
        return returnSuccessResponse();
      }

      // if user does not have merge branch stored locally, return error for terminal display
      List<String> allLocalBranches = storage.getAllLocalBranches(sessionId, userId);
      if (!allLocalBranches.contains(mergeBranch)) {
        // TODO: maybe suggest trying git fetch/offering some guidance in terminal??
        return returnErrorResponse(
            "error_database", "Merge: " + mergeBranch + " - not something we can merge.");
      }

      // get current branch's latest local commit for merging
      Map<String, Object> currentLatestLocalCommit =
          storage.getLatestLocalCommit(sessionId, userId, currentBranch);
      Map<String, List<MockFileObject>> currentCommittedFileMap =
          deserializeFileMap((String) currentLatestLocalCommit.get("file_map_json"));

      // check for uncommitted staged changes
      String changedFileMapJson = storage.getLatestLocalChanges(sessionId, userId, currentBranch);

      // if there are staged changes, check if there are any differences between the staged changes
      // and latest commit
      if (changedFileMapJson != null) {
        Map<String, List<MockFileObject>> changedFileMap = deserializeFileMap(changedFileMapJson);
        Set<String> filesWithDifferences =
            diffHelper.differenceDetected(currentCommittedFileMap, changedFileMap);
        // if there are uncommitted staged changes, return response for terminal display
        if (!filesWithDifferences.isEmpty()) {
          responseMap.put("difference_detected", true);
          responseMap.put(
              "instructions", "Please commit your changes or stash them before you merge.");
          responseMap.put("files_with_differences", filesWithDifferences);
          return returnErrorResponse(
              "error_database",
              "Your local changes to the following files would be overwritten by merge:");
        }
      }

      // check for unstaged changes in current state of project using file_map_json parameter
      Map<String, List<MockFileObject>> currentFileMap = deserializeFileMap(fileMapJson);
      diffHelper = new GitDiffHelper();
      Set<String> filesWithDifferences =
          diffHelper.differenceDetected(currentCommittedFileMap, currentFileMap);
      // if there are unstaged changes, return response for terminal display
      if (!filesWithDifferences.isEmpty()) {
        responseMap.put("difference_detected", true);
        responseMap.put(
            "instructions", "Please commit your changes or stash them before you merge.");
        responseMap.put("files_with_differences", filesWithDifferences);
        return returnErrorResponse(
            "error_database",
            "Your local changes to the following files would be overwritten by merge:");
      }

      // get latest commit from branch user wishes to merge with
      Map<String, Object> commitToMerge =
          storage.getLatestLocalCommit(sessionId, userId, currentBranch);
      Map<String, List<MockFileObject>> toMergeFileMap =
          deserializeFileMap((String) commitToMerge.get("file_map_json"));

      // add any new local files to incoming filemap
      diffHelper = new GitDiffHelper();
      diffHelper.detectNewFiles(currentCommittedFileMap, toMergeFileMap);
      List<String> newLocalFiles = diffHelper.getNewLocalFiles();
      for (String fileName : newLocalFiles) {
        toMergeFileMap.put(fileName, currentCommittedFileMap.get(fileName));
      }

      // add any new incoming files to local filemap
      List<String> newIncomingFiles = diffHelper.getNewIncomingFiles();
      for (String fileName : newIncomingFiles) {
        currentCommittedFileMap.put(fileName, toMergeFileMap.get(fileName));
      }

      // stores the resulting merged files
      Map<String, List<MockFileObject>> mergedFileMap = new HashMap<>();

      // now that both maps have the same files, attempt to auto-merge each file and store resulting
      // List<Ingredients>
      for (String fileName : currentCommittedFileMap.keySet()) {
        List<MockFileObject> committedFile = currentCommittedFileMap.get(fileName);
        List<MockFileObject> incomingFile = toMergeFileMap.get(fileName);
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
        return returnErrorResponse(
            "error_database", "Automatic merge failed; fix conflicts and then commit the results.");
      }
      //  if there were no conflicts, add and commit merged files
      else {

        Moshi moshi = new Moshi.Builder().build();
        Type type =
            Types.newParameterizedType(
                Map.class,
                String.class,
                Types.newParameterizedType(List.class, MockFileObject.class));
        JsonAdapter<Map<String, List<MockFileObject>>> adapter = moshi.adapter(type);

        storage.addChange(sessionId, userId, currentBranch, adapter.toJson(mergedFileMap));
        String localCommitId = (String) currentLatestLocalCommit.get("commit_id");
        String incomingCommitId = (String) commitToMerge.get("commit_id");
        // for updating branch map
        responseMap.put("local_commit_id", localCommitId);
        responseMap.put("incoming_commit_id", incomingCommitId);
        String commitMessage = localCommitId + " " + incomingCommitId + " merged";
        String mergeCommitId =
            storage.commitChange(
                sessionId,
                userId,
                currentBranch,
                commitMessage,
                List.of(localCommitId, incomingCommitId));
        responseMap.put("merge_commit_id", mergeCommitId);
        responseMap.put("message", commitMessage + " successfully and changes committed.");
      }

    } catch (Exception e) {
      return returnErrorResponse("error_database", "merge_failed: " + e.getMessage());
    }

    return returnSuccessResponse();
  }
}
