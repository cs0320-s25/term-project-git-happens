package edu.brown.cs.student.main.server.storage;

/**
 * A class to hold constants for use in {@link FirebaseUtilities}, {@link FirebaseUtilHelpers},
 * {@link FirestorePather}, and {@link BranchRef}.
 */
public final class FirestoreConstants {

  private FirestoreConstants() {}

  // ===== Collections =====
  public static final String COLLECTION_SESSIONS = "sessions";
  public static final String COLLECTION_LOCAL_STORE = "local_store";
  public static final String COLLECTION_REMOTE_STORE = "remote_store";

  // ===== Document Names =====
  public static final String DOC_USERS = "users";
  public static final String DOC_BRANCHES = "branches";
  public static final String DOC_STASHES = "stashes";
  public static final String DOC_PARENT_BRANCH = "parent_branch";
  public static final String DOC_HEAD = "head";
  public static final String DOC_PUSHED_COMMITS = "pushed_commits";
  public static final String DOC_STAGED_COMMITS = "staged_commits";
  public static final String DOC_ADD_CHANGES = "add_changes";
  public static final String DOC_REMOTE_FILE_MAP = "remote_file_map_json";
  public static final String DOC_LOCAL_FILE_MAP = "local_file_map_json";

  // ===== Field Names =====
  // commit fields
  public static final String FIELD_FILE_MAP_JSON = "file_map_json";
  public static final String FIELD_COMMIT_ID = "commit_id";
  public static final String FIELD_AUTHOR = "author";
  public static final String FIELD_DATE_TIME = "date_time";
  public static final String FIELD_COMMIT_MESSAGE = "commit_message";

  public static final String FIELD_PARENT_BRANCH_ID = "parent_branch_id";
  public static final String FIELD_HEAD = "head";
  public static final String FIELD_REMOTE_FILE_MAP = "remote_file_map_json";
  public static final String FIELD_LOCAL_FILE_MAP = "local_file_map_json";

  public static final String FIELD_PUSHED_COMMITS = "pushed_commits";
  public static final String FIELD_STAGED_COMMITS = "staged_commits";

  public static final String FIELD_COMMITS = "commits";

  public static final String FIELD_STASHES = "stashes";
  public static final String FIELD_STASH_MESSAGE = "stash_message";

  // fetched changes
  public static final String FIELD_OLD_COMMIT_ID = "old_commit_id";
  public static final String FIELD_NEW_COMMIT_ID = "new_commit_id";
  public static final String FIELD_COMMIT_UPDATES = "commit_updates";
  public static final String FIELD_NEW_BRANCHES = "new_branches";
  public static final String FIELD_BRANCH_ID = "branch_id";
  public static final String FIELD_REMOTE_BRANCH_ID = "remote_branch_id";
}
