package edu.brown.cs.student.main.server.storage;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

// Need to sort out the structure of the database to make this properly
// probably: "session" -> session_name -> {level: number}, {document: however we are storing
// documents}
public class FirebaseUtilities implements StorageInterface {

  // holds all randomly generated commit ids
  private final List<String> commitIds = new ArrayList<>();
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");
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
        new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

    FirebaseApp.initializeApp(options);
  }

  // *************************** USED AS HELPER METHODS ***************************

  private void deleteDocument(DocumentReference doc) {
    // for each subcollection, run deleteCollection()
    Iterable<CollectionReference> collections = doc.listCollections();
    for (CollectionReference collection : collections) {
      deleteCollection(collection);
    }
    // then delete the document
    doc.delete();
  }

  // recursively removes all the documents and collections inside a collection
  // https://firebase.google.com/docs/firestore/manage-data/delete-data#collections
  private void deleteCollection(CollectionReference collection) {
    try {

      // get all documents in the collection
      ApiFuture<QuerySnapshot> future = collection.get();
      List<QueryDocumentSnapshot> documents = future.get().getDocuments();

      // delete each document
      for (QueryDocumentSnapshot doc : documents) {
        doc.getReference().delete();
      }

      // NOTE: the query to documents may be arbitrarily large. A more robust
      // solution would involve batching the collection.get() call.
    } catch (Exception e) {
      System.err.println("Error deleting collection : " + e.getMessage());
    }
  }

  /**
   * Method that generates a 6 character ID to be used for saved commits and stashes
   *
   * @return - 6-character string
   */
  private String generateCommitId() {
    String alphaNum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
    Random random = new Random();
    StringBuilder commitId = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      commitId.append(alphaNum.charAt(random.nextInt(alphaNum.length())));
    }
    return commitId.toString();
  }
  // ********************************** GAME SPECIFIC METHODS ************************************

  /**
   * Method that creates origin/main branch on the remote repository if it has not already been
   * created and adds the first commit to setup the original state of files for every user. Then, a
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

      Firestore db = FirestoreClient.getFirestore();
      db.collection("sessions").document(session_id).set(Map.of(), SetOptions.merge());
      // create remote repository if this is the first user to log into the session
      if (db.collection("sessions")
          .document(session_id)
          .collection("remote_store")
          .get()
          .get()
          .getDocuments()
          .isEmpty()) {

        // generate unique commit id
        String initialCommitId = this.generateCommitId();
        while (this.commitIds.contains(initialCommitId)) {
          initialCommitId = this.generateCommitId();
        }
        commitIds.add(initialCommitId);

      // create initial commit
      Map<String, Object> initialCommit = new HashMap<>();
      initialCommit.put("file_map_json", file_map_json);
      initialCommit.put("commit_id", initialCommitId);
      initialCommit.put("author", "game");
      initialCommit.put("date_time", formatter.format((ZonedDateTime.now())));
      initialCommit.put("commit_message", "Initial commit");

        // setup branch info
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection("main")
            .document("parent_branch")
            .set(Collections.singletonMap("parent_branch_id", null));
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection("main")
            .document("remote_file_map_json")
            .set(Collections.singletonMap("remote_file_map_json", file_map_json));
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection("main")
            .document("head")
            .set(initialCommit);

        // setup pushed remote commits
        List<Map<String, Object>> remoteCommits = new ArrayList<>();
        remoteCommits.add(initialCommit);
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection("main")
            .document("pushed_commits")
            .set(Collections.singletonMap("commits", remoteCommits));
      }
      // setup user's local branch info
      String mainFileMapJson =
          db.collection("sessions")
              .document(session_id)
              .collection("remote_store")
              .document("branches")
              .collection("main")
              .document("remote_file_map_json")
              .get()
              .get()
              .getString("remote_file_map_json");
      Map<String, Object> mainHead =
          db.collection("sessions")
              .document(session_id)
              .collection("remote_store")
              .document("branches")
              .collection("main")
              .document("head")
              .get()
              .get()
              .getData();

      // set local file map
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("branches")
          .collection("main")
          .document("local_file_map_json")
          .set(Collections.singletonMap("local_file_map_json", mainFileMapJson));

      // set parent branch
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("branches")
          .collection("main")
          .document("parent_branch")
          .set(Collections.singletonMap("parent_branch_id", null));

      // set head
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("branches")
          .collection("main")
          .document("head")
          .set(Collections.singletonMap("head", mainHead));

      // set pushed commits to match main
      List<Map<String, Object>> mainCommits =
          (List<Map<String, Object>>)
              db.collection("sessions")
                  .document(session_id)
                  .collection("remote_store")
                  .document("branches")
                  .collection("main")
                  .document("pushed_commits")
                  .get()
                  .get()
                  .get("commits");
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("branches")
          .collection("main")
          .document("pushed_commits")
          .set(Collections.singletonMap("commits", mainCommits));

      // set staged commits to empty list
      List<Map<String, Object>> localStagedCommits = new ArrayList<>();
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("branches")
          .collection("main")
          .document("staged_commits")
          .set(Collections.singletonMap("commits", localStagedCommits));

      // set changes to map file map json to null, as there are no local changes yet
      Map<String, Object> localChanges = new HashMap<>();
      localChanges.put("file_map_json", null);
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("branches")
          .collection("main")
          .document("add_changes")
          .set(localChanges);

      // set stashes to empty list of maps
      List<Map<String, Object>> stashes = new ArrayList<>();
      db.collection("sessions")
          .document(session_id)
          .collection("local_store")
          .document("users")
          .collection(user_id)
          .document("stashes")
          .set(Collections.singletonMap("stashes", stashes));
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
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public void addStash(String session_id, String user_id, String file_map_json)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || file_map_json == null) {
      throw new IllegalArgumentException(
          "addStash: session_id, user_id, and file_map_json cannot be null");
    }

    Firestore db = FirestoreClient.getFirestore();
    // Make sure session document exists (safe no-op if it already does)
    db.collection("sessions").document(session_id).set(Map.of(), SetOptions.merge());
    // Find collection of stashes
    DocumentReference stashesRef =
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("stashes");
    List<Map<String, Object>> stashes =
        (List<Map<String, Object>>) stashesRef.get().get().get("stashes");
    // generate a unique id for stash
    String stash_id = generateCommitId();
    while (commitIds.contains(stash_id)) {
      stash_id = generateCommitId();
    }
    commitIds.add(stash_id);

    // create new stash map
    Map<String, Object> stash = new HashMap<>();
    stash.put("file_map_json", file_map_json);
    stash.put("stash_id", stash_id);

    // add stash to stash list and update local store
    stashes.add(stash);
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("stashes")
        .set(Collections.singletonMap("stashes", stashes));
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
    Firestore db = FirestoreClient.getFirestore();
    // Make sure session document exists (safe no-op if it already does)
    db.collection("sessions").document(session_id).set(Map.of(), SetOptions.merge());

    // take opportunity to update current branch's local state
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(current_branch_id)
        .document("local_file_map_json")
        .set(Collections.singletonMap("local_file_map_json", file_map_json));

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
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(new_branch_id)
            .document("head")
            .set(Collections.singletonMap("head", head));

        // set local branch's parent branch
        String parentId =
            db.collection("sessions")
                .document(session_id)
                .collection("remote_store")
                .document("branches")
                .collection(new_branch_id)
                .document("parent_branch")
                .get()
                .get()
                .getString("parent_branch_id");
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(current_branch_id)
            .document("parent_branch")
            .set(Collections.singletonMap("parent_branch_id", parentId));

        // set local branch's stored local file map
        String remoteFileMapJson = db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection(new_branch_id)
            .document("parent_branch")
            .get()
            .get()
            .getString("remote_file_map_json");
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(current_branch_id)
            .document("local_file_map_json")
            .set(Collections.singletonMap("local_file_map_json", remoteFileMapJson));

        // set local branch's staged commits to reflect remote branch's
        List<Map<String, Object>> stagedCommits = new ArrayList<>();
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(new_branch_id)
            .document("staged_commits")
            .set(Collections.singletonMap("commits", stagedCommits));

        // set local branch's pushed commits to reflect remote branch's
        List<Map<String, Object>> pushedCommits =
            (List<Map<String, Object>>)
                db.collection("sessions")
                    .document(session_id)
                    .collection("remote_store")
                    .document("branches")
                    .collection(new_branch_id)
                    .document("pushed_commits")
                    .get()
                    .get()
                    .get("commits");
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(new_branch_id)
            .document("pushed_commits")
            .set(Collections.singletonMap("commits", pushedCommits));
        return;
      }
    }
    //if branch does not exist create a new one locally and remotely

    // set new branch's head
    Map<String, Object> head = this.getLatestLocalCommit(session_id, user_id, current_branch_id);
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(new_branch_id)
        .document("head")
        .set(Collections.singletonMap("head", head));

    // set new branch's parent branch
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(new_branch_id)
        .document("parent_branch")
        .set(Collections.singletonMap("parent_branch_id", current_branch_id));

    // set new branch's stored local file map
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(new_branch_id)
        .document("local_file_map_json")
        .set(Collections.singletonMap("local_file_map_json", file_map_json));

    // set new branch's staged commits to reflect current branch's
    List<Map<String, Object>> stagedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("local_store")
                .document("users")
                .collection(user_id)
                .document("branches")
                .collection(current_branch_id)
                .document("staged_commits")
                .get()
                .get()
                .get("commits");
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(new_branch_id)
        .document("staged_commits")
        .set(Collections.singletonMap("commits", stagedCommits));

    // set new branch's pushed commits to reflect current branch's
    List<Map<String, Object>> pushedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("local_store")
                .document("users")
                .collection(user_id)
                .document("branches")
                .collection(current_branch_id)
                .document("pushed_commits")
                .get()
                .get()
                .get("commits");
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(new_branch_id)
        .document("pushed_commits")
        .set(Collections.singletonMap("commits", pushedCommits));

    // add branch to remote repository for convenience
    db.collection("sessions")
        .document(session_id)
        .collection("remote_store")
        .document("branches")
        .collection(new_branch_id)
        .document("parent_branch")
        .set(Collections.singletonMap("parent_branch_id", current_branch_id));
    db.collection("sessions")
        .document(session_id)
        .collection("remote_store")
        .document("branches")
        .collection(new_branch_id)
        .document("head")
        .set(Collections.singletonMap("head", head));
    db.collection("sessions")
        .document(session_id)
        .collection("remote_store")
        .document("branches")
        .collection(new_branch_id)
        .document("local_file_map_json")
        .set(Collections.singletonMap("local_file_map_json", file_map_json));
    db.collection("sessions")
        .document(session_id)
        .collection("remote_store")
        .document("branches")
        .collection(new_branch_id)
        .document("pushed_commits")
        .set(Collections.singletonMap("commits", pushedCommits));
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
    Firestore db = FirestoreClient.getFirestore();
    // check that branch exists
    if (!this.getAllRemoteBranches(session_id).contains(branch_id)) {
      throw new IllegalArgumentException("deleteBranch: branch_id does not exist");
    }
    // delete local copy of branch
    CollectionReference localBranchRef =
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(branch_id);
    deleteCollection(localBranchRef);
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
    List<String> branchIds = new ArrayList<>();
    Firestore db = FirestoreClient.getFirestore();
    // get all subcollections in remote branches document
    Iterable<CollectionReference> branches =
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .listCollections();

    // add branch_id for each collection
    for (CollectionReference branch : branches) {
      branchIds.add(branch.getId());
    }
    return branchIds;
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
    List<String> branchIds = new ArrayList<>();
    Firestore db = FirestoreClient.getFirestore();
    // get all subcollections in user's local branches document
    Iterable<CollectionReference> branches =
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .listCollections();

    // add branch_id for each collection
    for (CollectionReference branch : branches) {
      branchIds.add(branch.getId());
    }
    return branchIds;
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

    Firestore db = FirestoreClient.getFirestore();
    // set changes document to new version of file map
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("add_changes")
        .set(Collections.singletonMap("file_map_json", file_map_json));
    // take opportunity to update local file map in branch info
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("local_file_map_json")
        .set(Collections.singletonMap("local_file_map_json", file_map_json));
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
    // retrieve head stored in local branch
    Firestore db = FirestoreClient.getFirestore();
    Map<String, Object> latestCommit =
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(branch_id)
            .document("head")
            .get()
            .get()
            .getData();
    // return last added commit
    return latestCommit;
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
    // retrieve head stored in local branch
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> stagedCommits = (List<Map<String, Object>>)
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(branch_id)
            .document("staged_commits")
            .get()
            .get()
            .get("commits");
    // return staged commits
    return stagedCommits;
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
    // retrieve head stored in local branch
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> pushedCommits = (List<Map<String, Object>>)
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(branch_id)
            .document("pushed_commits")
            .get()
            .get()
            .get("commits");
    // return staged commits
    return pushedCommits;
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
    // retrieve head stored in remote branch
    Firestore db = FirestoreClient.getFirestore();
    Map<String, Object> latestCommit =
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection(branch_id)
            .document("head")
            .get()
            .get()
            .getData();
    // return last added commit
    return latestCommit;
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
  public List<Map<String, Object>> getRemotePushedCommits(
      String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getRemotePushedCommits: session_id and branch_id cannot be null");
    }
    // retrieve head stored in local branch
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> pushedCommits = (List<Map<String, Object>>)
        db.collection("sessions")
            .document(session_id)
            .collection("remote_store")
            .document("branches")
            .collection(branch_id)
            .document("pushed_commits")
            .get()
            .get()
            .get("commits");
    // return staged commits
    return pushedCommits;
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
    Firestore db = FirestoreClient.getFirestore();
    String fileMapJson =
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(branch_id)
            .document("add_changes")
            .get()
            .get()
            .getString("file_map_json");
    return fileMapJson;
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
    Firestore db = FirestoreClient.getFirestore();
    String changedFileMapJson = this.getLatestLocalChanges(session_id, user_id, branch_id);

    // generate new commit id
    String commitId = generateCommitId();
    while (commitIds.contains(commitId)) {
      commitId = generateCommitId();
    }
    commitIds.add(commitId);

    // create new commit
    Map<String, Object> newCommit = new HashMap<>();
    newCommit.put("file_map_json", changedFileMapJson);
    newCommit.put("commit_id", commitId);
    newCommit.put("author", user_id);
    newCommit.put("date_time", formatter.format(ZonedDateTime.now()));
    newCommit.put("commit_message", commit_message);

    // add new commit to local staged commits then update local store
    CollectionReference localBranchRef =
        db.collection("sessions")
            .document(session_id)
            .collection("local_store")
            .document("users")
            .collection(user_id)
            .document("branches")
            .collection(branch_id);
    // update branch's staged commits
    List<Map<String, Object>> stagedCommits =
        (List<Map<String, Object>>)
            localBranchRef.document("staged_commits").get().get().get("commits");
    stagedCommits.add(newCommit);
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("staged_commits")
        .set(Collections.singletonMap("commits", stagedCommits));
    // update branch's head
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("head")
        .set(newCommit);
    // clear changes since they have all been committed
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("add_changes")
        .set(Collections.singletonMap("file_map_json", null));
    return commitId;
  }

  /**
   * Method for push command, which moves all local staged commits to local pushed commits and
   * remote pushed commits, then clears the staged commits list. The most recently staged commit
   * will now be the last commit in the pushed-commits list.
   *
   * @param session_id - unique session id
   * @param branch_id - branch id for currently checked out branch
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public void pushCommit(String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("pushCommit: session_id, branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    // get all staged commits
    List<Map<String, Object>> stagedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("local_store")
                .document("branches")
                .collection(branch_id)
                .document("staged_commits")
                .get()
                .get()
                .get("commits");
    // get all local pushed commits
    List<Map<String, Object>> localPushedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("local_store")
                .document("branches")
                .collection(branch_id)
                .document("pushed_commits")
                .get()
                .get()
                .get("commits");
    // get all remote pushed commits
    List<Map<String, Object>> remotePushedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("remote_store")
                .document("branches")
                .collection(branch_id)
                .document("pushed_commits")
                .get()
                .get()
                .get("commits");

    // add each staged commit to local and remote pushed commits, with the most recent commit being
    // added last
    for (Map<String, Object> commit : stagedCommits) {
      localPushedCommits.add(commit);
      remotePushedCommits.add(commit);
    }

    // update local store with new pushed commits
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("branches")
        .collection(branch_id)
        .document("pushed_commits")
        .set(Collections.singletonMap("commits", localPushedCommits));
    // update remote store with new pushed commits
    db.collection("sessions")
        .document(session_id)
        .collection("remote_store")
        .document("branches")
        .collection(branch_id)
        .document("pushed_commits")
        .set(Collections.singletonMap("commits", remotePushedCommits));

    // clear staged commits, as they have all now been pushed
    List<Map<String, Object>> clearedCommits = new ArrayList<>();
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("branches")
        .collection(branch_id)
        .document("staged_commits")
        .set(Collections.singletonMap("commits", clearedCommits));
  }

  /**
   * Method for returning a map of all unstaged and pushed commits in a user's local repository.
   *
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @return a map that contains a list of unstaged commits and a list of pushed commits
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
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> pushedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("local_store")
                .document("users")
                .collection(user_id)
                .document("branches")
                .collection(branch_id)
                .document("pushed_commits")
                .get()
                .get()
                .get("commits");
    List<Map<String, Object>> stagedCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("local_store")
                .document("users")
                .collection(user_id)
                .document("branches")
                .collection(branch_id)
                .document("staged_commits")
                .get()
                .get()
                .get("commits");

    localCommits.put("pushed_commits", pushedCommits);
    localCommits.put("staged_commits", stagedCommits);
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
  public List<Map<String, Object>> getAllRemoteCommits(
      String session_id, String branch_id)
      throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException(
          "getAllRemoteCommits: session_id, and branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> remoteCommits =
        (List<Map<String, Object>>)
            db.collection("sessions")
                .document(session_id)
                .collection("remote_store")
                .document("branches")
                .collection(branch_id)
                .document("pushed_commits")
                .get()
                .get()
                .get("commits");
    return remoteCommits;
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
    for (Map<String, Object> commit : allLocalCommits.get("staged_commits")) {
      if (commit.get("commit_id").equals(commit_id)) {
        foundCommit = commit;
        break;
      }
    }
    // if commit can't be found in staged commits, search through pushed commits
    if (foundCommit == null) {
      for (Map<String, Object> commit : allLocalCommits.get("pushed_commits")) {
        if (commit.get("commit_id").equals(commit_id)) {
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
   * @param session_id
   * @param user_id
   * @param branch_id
   * @throws IllegalArgumentException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Override
  public void pullRemoteCommits(String session_id, String user_id, String branch_id)
  throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException("pullRemoteCommits: session_id, user_id, and branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> remoteCommits = this.getAllRemoteCommits(session_id, branch_id);

    //set local branch's pushed commits to match remote branch's history
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("pushed_commits")
        .set(Collections.singletonMap("commits", remoteCommits));
  }

  /**
   * Method that resets local commit history to inputted commits list for the given local branch.
   * Staged changes and staged commits are deleted, used for git reset.
   * @param session_id - unique session id
   * @param user_id - unique user id
   * @param branch_id - id of currently checked out branch
   * @param commits - list of commit map data, representing new local commit history
   * @throws IllegalArgumentException - if any parameters are null
   * @throws ExecutionException - for firebase methods
   * @throws InterruptedException - for firebase methods
   */
  @Override
  public void resetLocalCommits(
      String session_id, String user_id, String branch_id, List<Map<String, Object>> commits)
  throws IllegalArgumentException, ExecutionException, InterruptedException {
    if (session_id == null || user_id == null || branch_id == null) {
      throw new IllegalArgumentException("resetLocalCommits: session_id, user_id, branch_id, and commits cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    Map<String, Object> head = commits.get(commits.size()-1);

    // replace local commits with reset commits list
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("pushed_commits")
        .set(Collections.singletonMap("commits", commits));

    // set head to last commit in list (commit you reset to)
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("head")
        .set(head);

    // clear any staged changes
    Map<String, Object> changes = new HashMap<>();
    changes.put("file_map_json", null);
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("add_changes")
        .set(changes);

    // clear any staged commits
    List<Map<String, Object>> stagedCommits = new ArrayList<>();
    db.collection("sessions")
        .document(session_id)
        .collection("local_store")
        .document("users")
        .collection(user_id)
        .document("branches")
        .collection(branch_id)
        .document("staged_commits")
        .set(Collections.singletonMap("commits", stagedCommits));



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
        newBranch.put("branch_id", branch);
        newBranch.put("remote_branch_id", "origin/" + branch);
        newBranches.add(newBranch);
      }
    }
    fetchedChanges.put("new_branches", newBranches);

    // check if remote head has been updated
    Map<String, Object> latestLocalCommit =
        this.getLatestLocalCommit(session_id, user_id, branch_id);
    Map<String, Object> latestRemoteCommit = this.getLatestRemoteCommit(session_id, branch_id);
    if (!latestLocalCommit.get("commit_id").equals(latestRemoteCommit.get("commit_id"))) {
      commitUpdates.put("old_commit_id", latestLocalCommit.get("commit_id"));
      commitUpdates.put("new_commit_id", latestRemoteCommit.get("commit_id"));
    }
    fetchedChanges.put("commit_updates", commitUpdates);
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
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference dataRef = db.collection("sessions");
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
    Firestore db = FirestoreClient.getFirestore();
    DocumentReference docRef = db.collection("sessions").document(session_id);
    deleteDocument(docRef);
  }
}
