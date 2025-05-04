package edu.brown.cs.student.main.server.storage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface StorageInterface {

  void addDocument(String user, String collection_id, String doc_id, Map<String, Object> data);

  List<Map<String, Object>> getCollection(String user, String collection_id)
      throws InterruptedException, ExecutionException;

  List<Map<String, Object>> getCompleteCollection() throws InterruptedException, ExecutionException;

  void clearUser(String user) throws InterruptedException, ExecutionException;

  void updateLocalState(String branch_id, Map<String, Object> branchLocalState);

  Map<String, Map<String, Object>> getLocalState();

  void addBranch(String session_id, String new_branch_id, String file_map_json) throws ExecutionException, InterruptedException;

  void deleteBranch(String session_id, String branch_id) throws ExecutionException, InterruptedException;

  List<String> getAllBranches(String session_id) throws ExecutionException, InterruptedException;

  void addChange(String session_id, String branch_id, String file_map_json) throws ExecutionException, InterruptedException;

  Map<String, Object> getLatestStagedCommit(String session_id, String branch_id) throws ExecutionException, InterruptedException;

  String commitChange(String session_id, String branch_id, String commit_message) throws ExecutionException, InterruptedException;

  void pushCommit(String session_id, String branch_id) throws ExecutionException, InterruptedException;

  Map<String, Object> getCommit(String session_id, String branch_id, String commit_id) throws ExecutionException, InterruptedException;

  Map<String, Object> fetch(String session_id, String branch_id) throws ExecutionException, InterruptedException;

  List<Map<String, Object>> getAllCommits(String session_id, String branch_id) throws ExecutionException, InterruptedException;

  List<String> getAllSessions() throws ExecutionException, InterruptedException;

  void deleteSession(String session_id) throws ExecutionException, InterruptedException;

}
