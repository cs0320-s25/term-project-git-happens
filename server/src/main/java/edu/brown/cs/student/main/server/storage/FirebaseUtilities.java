package edu.brown.cs.student.main.server.storage;

import static edu.brown.cs.student.main.server.storage.FirestoreConstants.*;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Need to sort out the structure of the database to make this properly
// probably: "session" -> session_name -> {level: number}, {document: however we are storing
// documents}
public class FirebaseUtilities implements StorageInterface {

  private final FirebaseUtilHelpers helpers = new FirebaseUtilHelpers();
  private final Firestore db;
  private final FirestorePather pather;

  public FirebaseUtilities() throws IOException {
    // Create /resources/ folder with firebase_config.json and
    // add your admin SDK from Firebase. see:
    // https://docs.google.com/document/d/10HuDtBWjkUoCaVj_A53IFm5torB_ws06fW3KYFZqKjc/edit?usp=sharing
    String workingDirectory = System.getProperty("user.dir");
    Path firebaseConfigPath =
        Paths.get(workingDirectory, "src", "main", "resources", "firebase_config.json");
    // ^-- if your /resources/firebase_config.json exists but is not found,
    // try printing workingDirectory and messing around with this path.

    FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath.toString());

    FirebaseOptions options =
        FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

    FirebaseApp.initializeApp(options);

    this.db = FirestoreClient.getFirestore();
    this.pather = new FirestorePather(db);
  }
  // *************************** HELPER METHODS ***************************

  /**
   * Wraps a key (String) and value (Object) in a singleton map for insertion into Firestore
   *
   * @param key the field name
   * @param value the value to be inserted
   * @return the singleton map to be inserted into Firestore
   */
  private Map<String, Object> mapWrap(final String key, final Object value) {
    return Collections.singletonMap(key, value);
  }

  /**
   * Sets the referenced document to a singleton map from the key to the value in Firestore
   *
   * @param docRef the document to set
   * @param key the field name
   * @param value the value to be inserted
   */
  private void setField(final DocumentReference docRef, final String key, final Object value) {
    docRef.set(mapWrap(key, value));
  }

  /**
   * Sets the referenced document to a singleton map from the key to null in Firestore
   *
   * @param docRef the document to set
   * @param key the field name
   */
  private void clearField(final DocumentReference docRef, final String key) {
    docRef.set(mapWrap(key, null));
  }

  // ********************************** GAME SPECIFIC METHODS ************************************

  /**
   * Method that creates origin/main branch on the remote repository if it has not already been
   * created and adds the first commit to set up the original state of files for every user. Then, a
   * local repository is created for the user, which reflects the initial state of main.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id ("user1" or "user2")
   * @param file_map_json - json of filemap representing initial state of game
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public void addSession(String session_id, String user_id, String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException {

    try {
      if (session_id == null || user_id == null || file_map_json == null) {
        throw new IllegalArgumentException(
            "addSession: session_id and file_map_json cannot be null");
      }

      pather.safeSessionInitialize(session_id);
      // create remote repository if this is the first user to log into the session
      if (pather.getRemoteStoreRef(session_id).get().get().getDocuments().isEmpty()) {

        // generate unique commit id
        String initialCommitId = helpers.generateUniqueCommitId();

        // create initial commit
        Map<String, Object> initialCommit =
            helpers.createCommit(file_map_json, initialCommitId, "game", "Initial commit");

        // setup branch info
        final BranchRef remoteMainBranchReference = pather.getRemoteBranch(session_id, "main");
        clearField(remoteMainBranchReference.parentBranch(), FIELD_PARENT_BRANCH_ID);
        setField(remoteMainBranchReference.remoteFileMap(), FIELD_REMOTE_FILE_MAP, file_map_json);
        remoteMainBranchReference.head().set(initialCommit);

        // setup pushed remote commits
        List<Map<String, Object>> remoteCommits = new ArrayList<>();
        remoteCommits.add(initialCommit);
        setField(remoteMainBranchReference.pushedCommits(), FIELD_COMMITS, remoteCommits);
      }
      // setup user's local branch info
      final BranchRef remoteMainBranchReference = pather.getRemoteBranch(session_id, "main");
      String mainFileMapJson =
          remoteMainBranchReference.getSnapshotFieldString(
              DOC_REMOTE_FILE_MAP, FIELD_REMOTE_FILE_MAP);
      Map<String, Object> mainHead = remoteMainBranchReference.getHeadData();

      // set local file map
      final BranchRef localMainBranchReference = pather.getLocalBranch(session_id, user_id, "main");
      setField(localMainBranchReference.localFileMap(), FIELD_LOCAL_FILE_MAP, mainFileMapJson);

      // set parent branch
      clearField(localMainBranchReference.parentBranch(), FIELD_PARENT_BRANCH_ID);

      // set head
      localMainBranchReference.head().set(mainHead);

      // set pushed commits to match main
      List<Map<String, Object>> mainCommits = remoteMainBranchReference.getPushedCommitsMap();
      setField(localMainBranchReference.pushedCommits(), FIELD_COMMITS, mainCommits);

      // set staged commits to empty list
      localMainBranchReference
          .stagedCommits()
          .set(mapWrap(FIELD_COMMITS, new ArrayList<Map<String, Object>>()));

      // set changes to map file map json to null, as there are no local changes yet
      Map<String, Object> localChanges = new HashMap<>();
      localChanges.put(FIELD_FILE_MAP_JSON, null);
      localMainBranchReference.addChanges().set(localChanges);

      // set stashes to empty list of maps
      pather
          .getStashes(session_id, user_id)
          .set(mapWrap(FIELD_STASHES, new ArrayList<Map<String, Object>>()));
    } catch (Exception e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  /**
   * Method that adds set of changed files to stashes list in user's local store
   *
   * @param session_id - unique session_id for current game
   * @param user_id - unique user id
   * @param file_map_json - json string of map of filenames to file contents
   * @return - stash message
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public String addStash(String session_id, String user_id, String branch_id, String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || file_map_json == null || branch_id == null) {
      throw new IllegalArgumentException(
          "addStash: session_id, user_id, branch_id, and file_map_json cannot be null");
    }

    // Make sure session document exists (safe no-op if it already does)
    pather.safeSessionInitialize(session_id);
    // Find collection of stashes
    DocumentReference stashesRef = pather.getStashes(session_id, user_id);
    List<Map<String, Object>> stashes = pather.getStashList(session_id, user_id);
    // generate a unique message for stash
    Map<String, Object> latestCommit = this.getLatestLocalCommit(session_id, user_id, branch_id);
    String stashMessage = "WIP on " + branch_id + ": "
        + latestCommit.get(FIELD_COMMIT_ID)
        + " " + latestCommit.get(FIELD_COMMIT_MESSAGE);


    // create new stash map
    Map<String, Object> stash = new HashMap<>();
    stash.put(FIELD_FILE_MAP_JSON, file_map_json);
    stash.put(FIELD_STASH_MESSAGE, stashMessage);

    // add stash to stash list and update local store
    stashes.add(stash);
    stashesRef.set(mapWrap(FIELD_STASHES, stashes));

    return stashMessage;
  }

  /**
   * Method that returns the list of the user's stashes
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @return - list of stash maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public List<Map<String, Object>> getStashes(String session_id, String user_id)
  throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null) {
      throw new IllegalArgumentException("getStashes: session_id and user_id cannot be null");
    }
    // Make sure session document exists (safe no-op if it already does)
    pather.safeSessionInitialize(session_id);
    // Find collection of stashes
    return pather.getStashList(session_id, user_id);
  }

  /**
   * Method that retrieves a stash at the specified index of the stash list, then removes the stash
   * from the stash list.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param stash_index - index of stash to retrieve
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public Map<String, Object> popStash(String session_id, String user_id, int stash_index)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null) {
      throw new IllegalArgumentException("popStash: session_id and user_id cannot be null");
    }
    if (stash_index < 0) {
      throw new IllegalArgumentException("popStash: stash_index cannot be negative");
    }
    // Make sure session document exists (safe no-op if it already does)
    pather.safeSessionInitialize(session_id);
    // Find collection of stashes
    DocumentReference stashesRef = pather.getStashes(session_id, user_id);
    List<Map<String, Object>> stashes = pather.getStashList(session_id, user_id);

    if (stash_index >= stashes.size()) {
      return null;
    }
    Map<String, Object> stash = stashes.get(stash_index);
    stashes.remove(stash_index);
    stashesRef.set(mapWrap(FIELD_STASHES, stashes));
    return stash;
  }

  /**
   * Method for adding a branch to user's local and remote store, which uses the file state of
   * current branch for setting up the new branch. Both the current and new branch have the same
   * commit history and head.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param current_branch_id - branch the user currently has checked out
   * @param new_branch_id - name for the new branch
   * @param file_map_json - json string of local state of files in currently checked out branch
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public void addBranch(
      String session_id,
      String user_id,
      String current_branch_id,
      String new_branch_id,
      String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null
        || user_id == null
        || current_branch_id == null
        || new_branch_id == null
        || file_map_json == null) {
      throw new IllegalArgumentException(
          "addBranch: session_id, user_id, current_branch_id, new_branch_id, "
              + "and file_map_json cannot be null");
    }
    // Make sure session document exists (safe no-op if it already does)
    pather.safeSessionInitialize(session_id);

    final BranchRef currentLocalBranchRef =
        pather.getLocalBranch(session_id, user_id, current_branch_id);
    final BranchRef newLocalBranchRef = pather.getLocalBranch(session_id, user_id, new_branch_id);
    final BranchRef newRemoteBranchRef = pather.getRemoteBranch(session_id, new_branch_id);

    // take opportunity to update current branch's local state
    setField(currentLocalBranchRef.localFileMap(), FIELD_LOCAL_FILE_MAP, file_map_json);

    // check that branch_id isn't already in use
    List<String> allBranches = this.getAllRemoteBranches(session_id);
    if (allBranches.contains(new_branch_id)) {
      // if user has the branch locally, throw error, action not allowed
      List<String> allLocalBranches = this.getAllLocalBranches(session_id, user_id);
      if (allLocalBranches.contains(current_branch_id)) {
        throw new IllegalArgumentException(
            "addBranch: branch '" + new_branch_id + "' already exists");
      } else {
        // create a new local branch that reflects the existing remote branch

        // set local branch's head
        Map<String, Object> head = this.getLatestRemoteCommit(session_id, new_branch_id);

        newLocalBranchRef.head().set(head);

        // set local branch's parent branch
        String parentId =
            newRemoteBranchRef.getSnapshotFieldString(DOC_PARENT_BRANCH, FIELD_PARENT_BRANCH_ID);
        setField(currentLocalBranchRef.parentBranch(), FIELD_PARENT_BRANCH_ID, parentId);

        // set local branch's stored local file map
        String remoteFileMapJson =
            newRemoteBranchRef.getSnapshotFieldString(DOC_PARENT_BRANCH, FIELD_REMOTE_FILE_MAP);
        setField(currentLocalBranchRef.localFileMap(), FIELD_LOCAL_FILE_MAP, remoteFileMapJson);

        // set local branch's staged commits to an empty list
        newLocalBranchRef
            .stagedCommits()
            .set(mapWrap(FIELD_COMMITS, new ArrayList<Map<String, Object>>()));

        // set local branch's pushed commits to reflect remote branch's
        List<Map<String, Object>> pushedCommits = newRemoteBranchRef.getPushedCommitsMap();
        setField(newLocalBranchRef.pushedCommits(), FIELD_COMMITS, pushedCommits);
        return;
      }
    }
    // if branch does not exist create a new one locally and remotely

    // set new branch's head
    Map<String, Object> head = this.getLatestLocalCommit(session_id, user_id, current_branch_id);
    newLocalBranchRef.head().set(head);

    // set new branch's parent branch
    setField(newLocalBranchRef.parentBranch(), FIELD_PARENT_BRANCH_ID, current_branch_id);

    // set new branch's stored local file map
    setField(newLocalBranchRef.localFileMap(), FIELD_LOCAL_FILE_MAP, file_map_json);

    // set new branch's staged commits to reflect current branch's
    List<Map<String, Object>> stagedCommits = currentLocalBranchRef.getStagedCommitsMap();
    setField(newLocalBranchRef.stagedCommits(), FIELD_COMMITS, stagedCommits);

    // set new branch's pushed commits to reflect current branch's
    List<Map<String, Object>> pushedCommits = currentLocalBranchRef.getPushedCommitsMap();
    setField(newLocalBranchRef.pushedCommits(), FIELD_COMMITS, pushedCommits);

    // add branch to remote repository for convenience
    setField(newRemoteBranchRef.parentBranch(), FIELD_PARENT_BRANCH_ID, current_branch_id);
    newRemoteBranchRef.head().set(head);
    setField(newRemoteBranchRef.localFileMap(), FIELD_LOCAL_FILE_MAP, file_map_json);
    setField(newRemoteBranchRef.pushedCommits(), FIELD_COMMITS, pushedCommits);
  }

  /**
   * Method for deleting a branch locally for the specified user.
   *
   * @param session_id - unique session id of current game
   * @param user_id - unique user id
   * @param branch_id - name of branch to be deleted
   * @throws IllegalArgumentException - if any parameters are null
   */
  @Override
  public void deleteBranch(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "deleteBranch: session_id, user_id, and branch_id cannot be null");
    }

    // check that branch exists
    if (!this.getAllRemoteBranches(session_id).contains(branch_id)) {
      throw new IllegalArgumentException("deleteBranch: branch_id does not exist");
    }
    // delete local copy of branch
    final CollectionReference localBranchRef =
        pather.getLocalBranch(session_id, user_id, branch_id).branch();
    helpers.deleteCollection(localBranchRef);
  }

  /**
   * Method for returning a list of all branch IDs on the remote repository.
   *
   * @param session_id - unique session id for current game
   * @return - a list of remote branch names
   * @throws IllegalArgumentException - if any parameters are null
   */
  @Override
  public List<String> getAllRemoteBranches(String session_id) throws IllegalArgumentException {
    if (session_id == null) {
      throw new IllegalArgumentException("getAllBranches: session_id cannot be null");
    }
    return pather.getAllRemoteBranches(session_id);
  }

  /**
   * Method that returns all local branches on a user's local repository
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @return - a list of local branch names
   * @throws IllegalArgumentException - if any parameters are null
   */
  @Override
  public List<String> getAllLocalBranches(String session_id, String user_id)
      throws IllegalArgumentException {

    if (session_id == null || user_id == null) {
      throw new IllegalArgumentException(
          "getAllLocalBranches: session_id and user_id cannot be null");
    }
    return pather.getAllLocalBranches(session_id, user_id);
  }

  /**
   * Method for adding a changed filemap to the local working directory so changes can be committed.
   * This is used for git add, git rm, and staging changes that result from merging.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @param file_map_json - json string of all files the user would like to track changes for on Git
   * @throws IllegalArgumentException - if any parameters are null
   */
  @Override
  public void addChange(String session_id, String user_id, String branch_id, String file_map_json)
      throws IllegalArgumentException {
    if (session_id == null || user_id == null || branch_id == null || file_map_json == null) {
      throw new IllegalArgumentException(
          "addChange: session_id, user_id, branch_id, and file_map_json cannot be null");
    }

    // set changes document to new version of file map
    final BranchRef localBranchRef = pather.getLocalBranch(session_id, user_id, branch_id);
    setField(localBranchRef.addChanges(), FIELD_FILE_MAP_JSON, file_map_json);
    // take opportunity to update local file map in branch info
    setField(localBranchRef.localFileMap(), FIELD_LOCAL_FILE_MAP, file_map_json);
  }

  /**
   * Method for getting the last staged or pushed commit for a specific user on a specific local
   * branch.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @return - map of commit data representing user's most recently committed changes to files
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public Map<String, Object> getLatestLocalCommit(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getLatestLocalCommit: session_id, user_id, and branch_id cannot be null");
    }
    // retrieve head stored in local branch and return last added commit
    return pather.getLocalBranch(session_id, user_id, branch_id).getHeadData();
  }

  /**
   * Method that returns all local staged commits.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - currently checked out branch
   * @return - list of staged commit data maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public List<Map<String, Object>> getStagedCommits(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getStagedCommits: session_id, user_id, and branch_id cannot be null");
    }
    return pather.getLocalBranch(session_id, user_id, branch_id).getStagedCommitsMap();
  }

  /**
   * Method that returns all local pushed commits.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - currently checked out branch
   * @return - list of pushed commit data maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public List<Map<String, Object>> getLocalPushedCommits(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getLocalPushedCommits: session_id, user_id, and branch_id cannot be null");
    }
    return pather.getLocalBranch(session_id, user_id, branch_id).getPushedCommitsMap();
  }

  /**
   * Method that returns the head commit for a specific branch stored in the remote repository.
   *
   * @param session_id - unique session id
   * @param branch_id - name of local branch that pushes to the remote repository
   * @return - map of commit data representing the remote branch's head
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public Map<String, Object> getLatestRemoteCommit(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getLatestRemoteCommit: session_id and branch_id cannot be null");
    }
    // retrieve head stored in remote branch and return last added commit
    return pather.getRemoteBranch(session_id, branch_id).getHeadData();
  }

  /**
   * Method that returns all remote pushed commits.
   *
   * @param session_id - unique session id
   * @param branch_id - currently checked out branch
   * @return - list of staged commit data maps
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public List<Map<String, Object>> getRemotePushedCommits(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getRemotePushedCommits: session_id and branch_id cannot be null");
    }
    return pather.getRemoteBranch(session_id, branch_id).getPushedCommitsMap();
  }

  /**
   * Method that returns the file map json of the last staged changes that remain uncommitted; null
   * if there are no staged changes.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - currently checked out branch
   * @return - json string of file map containing changed files
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public String getLatestLocalChanges(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getLatestStagedCommit: session_id, user_id, and branch_id cannot be null");
    }
    String json = pather.getLocalBranch(session_id, user_id, branch_id).getSnapshotFieldString(DOC_ADD_CHANGES, FIELD_FILE_MAP_JSON);
    System.out.println(json);
    return json;
  }

  /**
   * Method for commiting most recent changes. Moves changed filemap to the local staged-commits
   * list and clears the changes document.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @param commit_message - corresponding message for commit
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public String commitChange(
      String session_id, String user_id, String branch_id, String commit_message)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "commitChange: session_id, user_id, branch_id, and commit_message cannot be null");
    }
    // get last stored changes
    String changedFileMapJson = this.getLatestLocalChanges(session_id, user_id, branch_id);

    // generate new commit id
    String commitId = helpers.generateUniqueCommitId();

    // create new commit
    Map<String, Object> newCommit =
        helpers.createCommit(changedFileMapJson, commitId, user_id, commit_message);

    // add new commit to local staged commits then update local store

    BranchRef localBranchRef = pather.getLocalBranch(session_id, user_id, branch_id);
    // update branch's staged commits
    List<Map<String, Object>> stagedCommits = localBranchRef.getStagedCommitsMap();
    stagedCommits.add(newCommit);
    setField(localBranchRef.stagedCommits(), FIELD_COMMITS, stagedCommits);
    // update branch's head
    localBranchRef.head().set(newCommit);
    // clear changes since they have all been committed
    clearField(localBranchRef.addChanges(), FIELD_FILE_MAP_JSON);
    return commitId;
  }

  /**
   * Method for push command, which moves all local staged commits to local pushed commits and
   * remote pushed commits, then clears the staged commits list. The most recently staged commit
   * will now be the last commit in the pushed-commits list.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public void pushCommit(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "pushCommit: session_id, user_id, branch_id cannot be null");
    }
    final BranchRef localBranchRef = pather.getLocalBranch(session_id, user_id, branch_id);
    // get all staged commits
    List<Map<String, Object>> stagedCommits = localBranchRef.getStagedCommitsMap();
    // get all local pushed commits
    List<Map<String, Object>> localPushedCommits = localBranchRef.getPushedCommitsMap();
    // get all remote pushed commits
    final BranchRef remoteBranchRef = pather.getRemoteBranch(session_id, branch_id);
    List<Map<String, Object>> remotePushedCommits = remoteBranchRef.getPushedCommitsMap();

    // add each staged commit to local and remote pushed commits, with the most recent commit being
    // added last
    for (Map<String, Object> commit : stagedCommits) {
      localPushedCommits.add(commit);
      remotePushedCommits.add(commit);
    }

    // update local store with new pushed commits
    setField(localBranchRef.pushedCommits(), FIELD_COMMITS, localPushedCommits);
    // update remote store with new pushed commits
    setField(remoteBranchRef.pushedCommits(), FIELD_COMMITS, remotePushedCommits);

    // clear staged commits, as they have all now been pushed
    List<Map<String, Object>> clearedCommits = new ArrayList<>();
    setField(localBranchRef.stagedCommits(), FIELD_COMMITS, clearedCommits);
  }

  /**
   * Method for returning a map of all staged and pushed commits in a user's local repository.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @return a map that contains a list of staged commits and a list of pushed commits
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public Map<String, List<Map<String, Object>>> getAllLocalCommits(
      String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getAllLocalCommits: session_id, user_id, and branch_id cannot be null");
    }
    Map<String, List<Map<String, Object>>> localCommits = new HashMap<>();
    final BranchRef localBranchRef = pather.getLocalBranch(session_id, user_id, branch_id);
    List<Map<String, Object>> pushedCommits = localBranchRef.getPushedCommitsMap();
    List<Map<String, Object>> stagedCommits = localBranchRef.getStagedCommitsMap();

    localCommits.put(FIELD_PUSHED_COMMITS, pushedCommits);
    localCommits.put(FIELD_STAGED_COMMITS, stagedCommits);
    return localCommits;
  }

  /**
   * Method that returns all pushed commits for a branch on the remote repository.
   *
   * @param session_id - unique session id
   * @param branch_id - id of currently checked out branch
   * @return - a list of pushed commits
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public List<Map<String, Object>> getAllRemoteCommits(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getAllRemoteCommits: session_id, and branch_id cannot be null");
    }
    return pather.getRemoteBranch(session_id, branch_id).getPushedCommitsMap();
  }

  /**
   * Method for returning a map of all staged commits in a user's local repository and all remote
   * commits for the current branch.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @return a list of staged commits and remote commits
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  public List<Map<String, Object>> getAllCommits(
      String session_id, String user_id, String branch_id)
      throws ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getAllCommits: session_id, user_id, and branch_id cannot be null");
    }
    final Map<String, List<Map<String, Object>>> localCommits =
        getAllLocalCommits(session_id, user_id, branch_id);
    final List<Map<String, Object>> remoteCommits = getAllRemoteCommits(session_id, branch_id);

    final List<Map<String, Object>> allCommits = new ArrayList<>();
    allCommits.addAll(localCommits.get(FIELD_STAGED_COMMITS));
    allCommits.addAll(remoteCommits);
    return allCommits;
  }

  /**
   * Method for returning the data for a specified commit on a user's local repository. Used for git
   * reset.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for branch currently checked out
   * @param commit_id - commit id to search for
   * @return null if commit_id does not exist, otherwise a map of stored commit data for specified
   *     commit
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public Map<String, Object> getCommit(
      String session_id, String user_id, String branch_id, String commit_id)
      throws ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null || commit_id == null) {
      throw new IllegalArgumentException(
          "getCommit: session_id, user_id, branch_id, and commit_id cannot be null");
    }
    // check local staged and pushed commits for desired commit
    Map<String, List<Map<String, Object>>> allLocalCommits =
        this.getAllLocalCommits(session_id, user_id, branch_id);
    Map<String, Object> foundCommit = null;
    // check staged commits for desired commit
    for (Map<String, Object> commit : allLocalCommits.get(FIELD_STAGED_COMMITS)) {
      if (commit.get(FIELD_COMMIT_ID).equals(commit_id)) {
        foundCommit = commit;
        break;
      }
    }
    // if commit can't be found in staged commits, search through pushed commits
    if (foundCommit == null) {
      for (Map<String, Object> commit : allLocalCommits.get(FIELD_PUSHED_COMMITS)) {
        if (commit.get(FIELD_COMMIT_ID).equals(commit_id)) {
          foundCommit = commit;
          break;
        }
      }
    }
    return foundCommit;
  }

  /**
   * Method that pulls full list of pushed commits from the remote branch and adds them to the local
   * commit history. Now, the local pushed commits reflect the remote pushed commits
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public void pullRemoteCommits(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "pullRemoteCommits: session_id, user_id, and branch_id cannot be null");
    }
    List<Map<String, Object>> remoteCommits = this.getAllRemoteCommits(session_id, branch_id);

    // set local branch's pushed commits to match remote branch's history
    pather
        .getLocalBranch(session_id, user_id, branch_id)
        .pushedCommits()
        .set(mapWrap(FIELD_COMMITS, remoteCommits));
  }

  /**
   * Method that resets local commit history to inputted commits list for the given local branch.
   * Staged changes and staged commits are deleted, used for git reset.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @param commits - list of commit map data, representing new local commit history
   * @throws IllegalArgumentException - if any parameters are null
   */
  @Override
  public void resetLocalCommits(
      String session_id, String user_id, String branch_id, Map<String, List<Map<String, Object>>> commits)
  throws IllegalArgumentException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "resetLocalCommits: session_id, user_id, branch_id, and commits cannot be null");
    }

    List<Map<String, Object>> stagedCommits = commits.get("staged_commits");
    List<Map<String, Object>> pushedCommits = commits.get("pushed_commits");

    final BranchRef localBranchRef = pather.getLocalBranch(session_id, user_id, branch_id);

    Map<String, Object> head;
    if (stagedCommits.isEmpty()) {
      head = pushedCommits.get(pushedCommits.size()-1);
    } else {
      head = stagedCommits.get(stagedCommits.size()-1);
    }
    // replace local pushed commits with reset commits list
    setField(localBranchRef.pushedCommits(), FIELD_COMMITS, pushedCommits);

    // replace local staged commits with reset commits list
    setField(localBranchRef.stagedCommits(), FIELD_COMMITS, stagedCommits);

    // set head to last commit in list (commit you reset to)
    localBranchRef.head().set(head);

    // clear any staged changes
    Map<String, Object> changes = new HashMap<>();
    changes.put(FIELD_FILE_MAP_JSON, null);
    localBranchRef.addChanges().set(changes);
  }

  /**
   * Method that returns any updates to the branch that are stored remotely but not on the user's
   * local repository (new branches and latest commits).
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - branch id for currently checked out branch
   * @return - a map of newly added branches info and head commit updates
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public Map<String, Object> fetch(String session_id, String user_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getCommit: session_id, user_id, and branch_id cannot be null");
    }

    Map<String, Object> fetchedChanges = new HashMap<>();
    List<Map<String, Object>> newBranches = new ArrayList<>();
    Map<String, Object> commitUpdates = new HashMap<>();
    // check if there are new branches
    List<String> allRemoteBranches = this.getAllRemoteBranches(session_id);
    List<String> allLocalBranches = this.getAllLocalBranches(session_id, user_id);
    for (String branch : allRemoteBranches) {
      if (!allLocalBranches.contains(branch)) {
        Map<String, Object> newBranch = new HashMap<>();
        newBranch.put(FIELD_BRANCH_ID, branch);
        newBranch.put(FIELD_REMOTE_BRANCH_ID, "origin/" + branch);
        newBranches.add(newBranch);
      }
    }
    fetchedChanges.put(FIELD_NEW_BRANCHES, newBranches);

    // check if remote head has been updated
    Map<String, Object> latestLocalCommit =
        this.getLatestLocalCommit(session_id, user_id, branch_id);
    Map<String, Object> latestRemoteCommit = this.getLatestRemoteCommit(session_id, branch_id);
    if (!latestLocalCommit.get(FIELD_COMMIT_ID).equals(latestRemoteCommit.get(FIELD_COMMIT_ID))) {
      commitUpdates.put(FIELD_OLD_COMMIT_ID, latestLocalCommit.get(FIELD_COMMIT_ID));
      commitUpdates.put(FIELD_NEW_COMMIT_ID, latestRemoteCommit.get(FIELD_COMMIT_ID));
    }
    fetchedChanges.put(FIELD_COMMIT_UPDATES, commitUpdates);
    // return changes
    return fetchedChanges;
  }

  /**
   * Returns a list of all stored session IDs
   *
   * @return list of session ID strings
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public List<String> getAllSessions() throws ExecutionException, InterruptedException {
    CollectionReference dataRef = db.collection(COLLECTION_SESSIONS);
    QuerySnapshot dataQuery = dataRef.get().get();

    List<String> sessions = new ArrayList<>();
    for (QueryDocumentSnapshot doc : dataQuery.getDocuments()) {
      sessions.add(doc.getId());
    }
    return sessions;
  }

  /**
   * Deletes all stored information for a session, which can be used when users finish the game so
   * session IDs can be reused.
   *
   * @param session_id - unique session id
   * @throws IllegalArgumentException - if session_id is null
   */
  @Override
  public void deleteSession(String session_id) throws IllegalArgumentException {
    if (session_id == null) {
      throw new IllegalArgumentException("deleteSession: session_id cannot be null");
    }
    DocumentReference docRef = db.collection(COLLECTION_SESSIONS).document(session_id);
    helpers.deleteDocument(docRef);
  }
}
