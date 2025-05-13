package edu.brown.cs.student.main.server.storage;

import static edu.brown.cs.student.main.server.storage.FirestoreConstants.*;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public record BranchRef(CollectionReference branch) {

  /** Returns a reference to the head document of this branch. */
  public DocumentReference head() {
    return branch.document(DOC_HEAD);
  }

  /** Returns a reference to the local file map document of this branch. */
  public DocumentReference localFileMap() {
    return branch.document(DOC_LOCAL_FILE_MAP);
  }

  /** Returns a reference to the remote file map document of this branch. */
  public DocumentReference remoteFileMap() {
    return branch.document(DOC_REMOTE_FILE_MAP);
  }

  /** Returns a reference to the parent branch document. */
  public DocumentReference parentBranch() {
    return branch.document(DOC_PARENT_BRANCH);
  }

  /** Returns a reference to the staged commits document. */
  public DocumentReference stagedCommits() {
    return branch.document(DOC_STAGED_COMMITS);
  }

  /** Returns a reference to the pushed commits document. */
  public DocumentReference pushedCommits() {
    return branch.document(DOC_PUSHED_COMMITS);
  }

  /** Returns a reference to the document holding changes added (i.e., staged but not committed). */
  public DocumentReference addChanges() {
    return branch.document(DOC_ADD_CHANGES);
  }

  /**
   * Retrieves the list of pushed commit maps for this branch.
   *
   * @return list of pushed commits; empty if none
   */
  public List<Map<String, Object>> getPushedCommitsMap()
      throws ExecutionException, InterruptedException {
    final Object data = pushedCommits().get().get().get(FIELD_COMMITS);
    return data == null ? new ArrayList<>() : (List<Map<String, Object>>) data;
  }

  /**
   * Retrieves the list of staged commit maps for this branch.
   *
   * @return list of staged commits; empty if none
   */
  public List<Map<String, Object>> getStagedCommitsMap()
      throws ExecutionException, InterruptedException {
    final Object data = stagedCommits().get().get().get(FIELD_COMMITS);
    return data == null ? new ArrayList<>() : (List<Map<String, Object>>) data;
  }

  /**
   * Retrieves a specific field from a named document in this branch as a string.
   *
   * @param documentName the document to fetch
   * @param fieldName the field name to retrieve
   * @return the string value of the field
   */
  public String getSnapshotFieldString(final String documentName, final String fieldName)
      throws ExecutionException, InterruptedException {
    return branch.document(documentName).get().get().getString(fieldName);
  }

  /**
   * Retrieves the full data map of the head document.
   *
   * @return a map representing the head commit data
   */
  public Map<String, Object> getHeadData() throws ExecutionException, InterruptedException {
    return head().get().get().getData();
  }
}
