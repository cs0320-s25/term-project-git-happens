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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

// Need to sort out the structure of the database to make this properly
// probably: "session" -> session_name -> {level: number}, {document: however we are storing documents}
public class FirebaseUtilities implements StorageInterface {
  //holds all randomly generated commit ids
  private final List<String> commitIds = new ArrayList<>();
  //stores the local version of each branch's files for the user, used to check for changes against what is stored in firebase (the most current pushed changes of the project)
  // when switching branches or pushing local changes
  private final Map<String, Map<String, Object>> localState = new HashMap<>();
  // user's local stashes
  private final Map<String, Object> stashes = new LinkedHashMap<>();

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

  //****************************NOT USED, CONSIDER DELETING**************************
  @Override
  public List<Map<String, Object>> getCollection(String user, String collection_id)
      throws InterruptedException, ExecutionException, IllegalArgumentException {
    if (user == null || collection_id == null) {
      throw new IllegalArgumentException("getCollection: user and/or collection_id cannot be null");
    }

    // gets all documents in the collection 'collection_id' for user 'user'

    Firestore db = FirestoreClient.getFirestore();
    // 1: Make the data payload to add to your collection
    CollectionReference dataRef = db.collection("users").document(user).collection(collection_id);

    // 2: Get pin documents
    QuerySnapshot dataQuery = dataRef.get().get();

    // 3: Get data from document queries
    List<Map<String, Object>> data = new ArrayList<>();
    for (QueryDocumentSnapshot doc : dataQuery.getDocuments()) {
      data.add(doc.getData());
    }

    return data;
  }

  @Override
  public List<Map<String, Object>> getCompleteCollection()
      throws InterruptedException, ExecutionException, IllegalArgumentException {

    Firestore db = FirestoreClient.getFirestore();

    // Get all pin documents for all users
    List<Map<String, Object>> data = new ArrayList<>();
    List<QueryDocumentSnapshot> docs = db.collectionGroup("pins").get().get().getDocuments();
    for (QueryDocumentSnapshot doc : docs) {
      data.add(doc.getData());
    }

    return data;
  }

  @Override
  public void addDocument(
      String user, String collection_id, String doc_id, Map<String, Object> data)
      throws IllegalArgumentException {
    if (user == null || collection_id == null || doc_id == null || data == null) {
      throw new IllegalArgumentException(
          "addDocument: user, collection_id, doc_id, or data cannot be null");
    }
    // adds a new document 'doc_name' to collection 'collection_id' for user 'user'
    // with data payload 'data'.

    Firestore db = FirestoreClient.getFirestore();
    CollectionReference collection =
        db.collection("users").document(user).collection(collection_id);
    DocumentReference docRef = collection.document(doc_id);
    docRef.set(data);
  }

  // clears the collections inside of a specific user.
  @Override
  public void clearUser(String user) throws IllegalArgumentException {
    if (user == null) {
      throw new IllegalArgumentException("removeUser: user cannot be null");
    }
    try {
      // removes all data for user 'user'
      Firestore db = FirestoreClient.getFirestore();
      // 1: Get a ref to the user document
      DocumentReference userDoc = db.collection("users").document(user);
      // 2: Delete the user document
      deleteDocument(userDoc);
    } catch (Exception e) {
      System.err.println("Error removing user : " + user);
      System.err.println(e.getMessage());
    }
  }

  //*************************** USED AS HELPER METHODS ***************************

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
  //********************************** GAME SPECIFIC METHODS ************************************

  @Override
  public Map<String, Map<String, Object>> getLocalState() {
    Map<String, Map<String, Object>> localStateCopy = localState;
    return localStateCopy;
  }

  @Override
  public void updateLocalState(String branch_id, Map<String, Object> branchLocalState) {
    localState.put(branch_id, branchLocalState);
  }


  public void addStash(String file_map_json) throws ExecutionException, InterruptedException {
    if (file_map_json == null) {
      throw new IllegalArgumentException("addStash: file_map_json  cannot be null");
    }
    //generate a unique id for stash
    String stash_id = generateCommitId();
    while (commitIds.contains(stash_id)) {
      stash_id = generateCommitId();
    }
    commitIds.add(stash_id);
    stashes.put(stash_id, file_map_json);
  }

  /**
   * Method for adding a branch, which uses the contents of current branch for use by the new branch
   * @param session_id - unique session id for current game
   * @param new_branch_id - name for the new branch
   * @param file_map_json - json string of local state of files in currently checked out branch
   * @return - map of info for the most recent pushed commit on the current branch, to be used as the
   *           basis for new branch's contents
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public void addBranch(String session_id, String new_branch_id, String file_map_json) throws ExecutionException, InterruptedException {
    if (session_id == null || new_branch_id == null || file_map_json == null) {
      throw new IllegalArgumentException("addBranch: session_id, new_branch_id, and file_map_json cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    // Make sure session document exists (safe no-op if it already does)
    db.collection("sessions").document(session_id).set(Map.of(), SetOptions.merge());

    List<String> allBranches = this.getAllBranches(session_id);
    if (allBranches.contains(new_branch_id)) {
      throw new IllegalArgumentException("addBranch: branch_id already exists");
    }
    Map<String, Object> branchLocalState = new HashMap<>();
    branchLocalState.put("file_map_json", file_map_json);
    branchLocalState.put("latest_commit", null);
    localState.put(new_branch_id, branchLocalState);
  }

  @Override
  public void deleteBranch(String session_id, String branch_id) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("deleteBranch: session_id, branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    if (!this.getAllBranches(session_id).contains(branch_id)) {
      throw new IllegalArgumentException("deleteBranch: branch_id does not exist");
    }
    DocumentReference branchRef = db.collection("sessions").document(session_id).collection("branches").document(branch_id);

    deleteDocument(branchRef);
    this.localState.remove(branch_id);
  }

  /**
   * Method for returning a list of all branch IDs for the current session.
   * @param session_id - unique session id for current game
   * @return - a list of branch names
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public List<String> getAllBranches(String session_id) throws ExecutionException, InterruptedException {
    if (session_id == null) {
      throw new IllegalArgumentException("getAllBranches: session_id cannot be null");
    }
    List<String> branchIds = new ArrayList<>();
    Firestore db = FirestoreClient.getFirestore();
    List<QueryDocumentSnapshot> branches = db.collection("sessions").document(session_id).
        collection("branches").get().get().getDocuments();

    for (QueryDocumentSnapshot doc : branches) {
      branchIds.add(doc.getId());
    }
    return branchIds;
  }

  /**
   * Method for add -A or rm command, as well as results of merging, which saves the most current
   * changes to the files in the changes collection
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for currently checked out branch
   * @param file_map_json - json string of all files the user would like to track changes for on Git
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public void addChange(String session_id, String branch_id, String file_map_json) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null || file_map_json == null) {
      throw new IllegalArgumentException("addChange: session_id, branch_id, and file_map_json cannot be null");
    }
    Map<String, Object> data = new HashMap<>();
    data.put("file_map", file_map_json);
    Firestore db = FirestoreClient.getFirestore();
    //make sure document exists
    db.collection("sessions").document(session_id).set(Map.of(), SetOptions.merge());

    CollectionReference changesCollection = db.collection("sessions").document(session_id)
        .collection("branches").document(branch_id).collection("changes");
    //add most recent changes
    changesCollection.document("change-" +
        changesCollection.get().get().getDocuments().size()).set(data);
    this.localState.get(branch_id).put("file_map_json", file_map_json);
  }

  /**
   * Method for getting the last staged commits, used for showing the difference between any of the
   * user's uncommitted files and committed files.
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for currently checked out branch
   * @return - map of commit data representing most recently committed changes to files
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public Map<String, Object> getLatestStagedCommit(String session_id, String branch_id) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("getLatestStagedCommit: session_id, branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    List<QueryDocumentSnapshot> stagedCommits = db.collection("sessions").document(session_id)
        .collection("branches").document(branch_id).collection("staged-commits")
        .get().get().getDocuments();
    //return last added commit
    return stagedCommits.get(stagedCommits.size()-1).getData();
  }

  /**
   * Method that generates a 6 character ID to be used for saved commits and stashes
   * @return - 6-character string
   */
  public String generateCommitId() {
    String alphaNum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
    Random random = new Random();
    StringBuilder commitId = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      commitId.append(alphaNum.charAt(random.nextInt(alphaNum.length())));
    }
    return commitId.toString();
  }

  /**
   * Method for commiting most recent changes. Moves most current version of the filemap to the staged-commits
   * collection and clears the former changes, which can no longer be referenced
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for currently checked out branch
   * @param commit_message - corresponding message for commit
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public String commitChange(String session_id, String branch_id, String commit_message) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("commitChange: session_id, branch_id, and commit_message cannot be null");
    }
    Map<String, Object> data = new HashMap<>();
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference changesCollection = db.collection("sessions").document(session_id)
        .collection("branches").document(branch_id).collection("changes");
    List<QueryDocumentSnapshot> changes = changesCollection.get().get().getDocuments();
    Map<String, Object> latestChange = changes.get(changes.size()-1).getData();
    data.put("commit_message", commit_message);
    data.put("date_time", ZonedDateTime.now());
    data.put("file_map", latestChange.get("file_map"));
    String commitId = generateCommitId();
    while (commitIds.contains(commitId)) {
      commitId = generateCommitId();
    }
    commitIds.add(commitId);
    data.put("commit_id", commitId);
    db.collection("sessions").document(session_id).collection("branches")
        .document(branch_id).collection("staged-commits").document(commitId).set(data);
    localState.get(branch_id).put("file_map_json", data.get("file_map"));
    localState.get(branch_id).put("latest_commit_id", commitId);
    deleteCollection(changesCollection);
    return commitId;
  }

  /**
   * Method for push command, which moves all staged commits to pushed commits collection, then clears
   * the staged commits collection. The most recently staged commit will now be the last commit in the
   * pushed-commits collection.
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for currently checked out branch
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public void pushCommit(String session_id, String branch_id) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("pushCommit: session_id, branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    //get all staged commits
    CollectionReference stagedCommitsCollection = db.collection("sessions").document(session_id).
        collection("branches").document(branch_id).collection("staged-commits");
    List<QueryDocumentSnapshot> stagedCommits = stagedCommitsCollection.get().get().getDocuments();
    //add each staged commit to pushed commits, with the most recent commit being added last
    for (QueryDocumentSnapshot stagedCommit : stagedCommits) {
      db.collection("sessions").document(session_id).collection("branches").document(branch_id)
          .collection("pushed-commits").document((String) stagedCommit.getData().get("commit_id"))
          .set(stagedCommit.getData());
    }
    //clear staged commits, as they have all now been pushed
    deleteCollection(stagedCommitsCollection);
  }

  /**
   * Method for returning the data for a specified commit.
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for branch currently checked out
   * @param commit_id - commit id to search for
   * @return null if commit_id does not exist, otherwise a map of stored commit data for specified commit
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public Map<String, Object> getCommit(String session_id, String branch_id, String commit_id) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null || commit_id == null) {
      throw new IllegalArgumentException("getCommit: session_id, branch_id, and commit_id cannot be null");
    }
    //check pushed commits for desired commit
    Firestore db = FirestoreClient.getFirestore();
    Map<String, Object> foundCommit = db.collection("sessions").document(session_id).collection("branches")
        .document(branch_id).collection("pushed-commits").document(commit_id).get().get().getData();

    //if commit can't be found in pushed commits, search through staged commits
    if (foundCommit == null) {
      foundCommit = db.collection("sessions").document(session_id).collection("branches")
          .document(branch_id).collection("staged-commits").document(commit_id).get().get().getData();
    }
    return foundCommit;
  }

  /**
   * Method for returning the most current version of the specified branch, used for git pull.
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for currently checked out branch
   * @return - a map of the most recent commit data
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public Map<String, Object> fetch(String session_id, String branch_id) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("getCommit: session_id and branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    List<QueryDocumentSnapshot> pushedCommits = db.collection("sessions").document(session_id).collection("branches")
        .document(branch_id).collection("pushed-commits").get().get().getDocuments();
    //return last added commit
    return pushedCommits.get(pushedCommits.size()-1).getData();
  }

  /**
   * Method for returning all staged and pushed commits, used for git log
   * @param session_id - unique session id for current game
   * @param branch_id - branch id for currently checked out branch
   * @return - a list of all stored commit data
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public List<Map<String, Object>> getAllCommits(String session_id, String branch_id) throws ExecutionException, InterruptedException {
    if (session_id == null || branch_id == null) {
      throw new IllegalArgumentException("getAllCommits: session_id, branch_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    List<Map<String, Object>> allCommits = new ArrayList<>();

    //add all pushed commits
    List<QueryDocumentSnapshot> pushedCommits = db.collection("sessions").document(session_id).collection("branches")
        .document(branch_id).collection("pushed-commits").get().get().getDocuments();
    for (QueryDocumentSnapshot commit : pushedCommits) {
      allCommits.add(commit.getData());
    }
    //add all staged commits
    List<QueryDocumentSnapshot> stagedCommits = db.collection("sessions").document(session_id).collection("branches")
        .document(branch_id).collection("staged-commits").get().get().getDocuments();
    for (QueryDocumentSnapshot commit : stagedCommits) {
      allCommits.add(commit.getData());
    }
    return allCommits;
  }

  /**
   * Returns a list of all stored session IDs
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
   * @param session_id - unique session id of current game
   * @throws ExecutionException - for firebase actions
   * @throws InterruptedException - for firebase actions
   */
  @Override
  public void deleteSession(String session_id) throws ExecutionException, InterruptedException {
    if (session_id == null) {
      throw new IllegalArgumentException("deleteSession: session_id cannot be null");
    }
    Firestore db = FirestoreClient.getFirestore();
    DocumentReference docRef = db.collection("sessions").document(session_id);
    deleteDocument(docRef);
  }
}
