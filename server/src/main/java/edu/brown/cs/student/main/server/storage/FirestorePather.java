package edu.brown.cs.student.main.server.storage;

import static edu.brown.cs.student.main.server.storage.FirestoreConstants.*;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A utility class that helps construct and access Firestore paths for various collections and
 * documents in the Firebase backend for Git Happens.
 */
public class FirestorePather {

  private final Firestore db;

  /**
   * Constructs a FirestorePather using the given Firestore instance.
   *
   * @param db the Firestore instance to use for building paths
   */
  public FirestorePather(Firestore db) {
    this.db = db;
  }

  /**
   * Safely initializes a session document if it doesn't already exist by writing an empty map using
   * merge semantics (i.e., does not overwrite existing data).
   *
   * @param session_id the ID of the session to initialize
   */
  public void safeSessionInitialize(final String session_id) {
    db.collection(COLLECTION_SESSIONS).document(session_id).set(Map.of(), SetOptions.merge());
  }

  /**
   * Returns the DocumentReference to the session document.
   *
   * @param session_id the ID of the session
   * @return a reference to the session document
   */
  private DocumentReference getSessionRef(final String session_id) {
    return db.collection(COLLECTION_SESSIONS).document(session_id);
  }

  /**
   * Returns the CollectionReference for the local_store collection within a session.
   *
   * @param session_id the ID of the session
   * @return a reference to the local_store collection
   */
  private CollectionReference getLocalStoreRef(final String session_id) {
    return getSessionRef(session_id).collection(COLLECTION_LOCAL_STORE);
  }

  /**
   * Returns the CollectionReference for the remote_store collection within a session.
   *
   * @param session_id the ID of the session
   * @return a reference to the remote_store collection
   */
  public CollectionReference getRemoteStoreRef(final String session_id) {
    return getSessionRef(session_id).collection(COLLECTION_REMOTE_STORE);
  }

  /**
   * Returns the CollectionReference for a user's collection in the local_store.
   *
   * @param session_id the session ID
   * @param user_id the user ID
   * @return a reference to the user's collection
   */
  private CollectionReference getLocalUserRef(final String session_id, final String user_id) {
    return getLocalStoreRef(session_id).document(DOC_USERS).collection(user_id);
  }

  /**
   * Returns the DocumentReference to the branches holder in the remote_store.
   *
   * @param session_id the session ID
   * @return a reference to the "branches" document in remote_store
   */
  private DocumentReference getRemoteBranchHolder(final String session_id) {
    return getRemoteStoreRef(session_id).document(DOC_BRANCHES);
  }

  /**
   * Returns the DocumentReference to the branches holder for a specific user in local_store.
   *
   * @param session_id the session ID
   * @param user_id the user ID
   * @return a reference to the "branches" document in the user's local store
   */
  private DocumentReference getLocalBranchHolder(final String session_id, final String user_id) {
    return getLocalUserRef(session_id, user_id).document(DOC_BRANCHES);
  }

  /**
   * Returns the BranchRef for a specific branch in the remote store.
   *
   * @param session_id the session ID
   * @param branch_id the branch ID
   * @return a reference to the collection representing the branch
   */
  public BranchRef getRemoteBranch(final String session_id, final String branch_id) {
    return new BranchRef(getRemoteBranchHolder(session_id).collection(branch_id));
  }

  /**
   * Returns the BranchRef for a specific branch in the user's local store.
   *
   * @param session_id the session ID
   * @param user_id the user ID
   * @param branch_id the branch ID
   * @return a reference to the collection representing the branch
   */
  public BranchRef getLocalBranch(
      final String session_id, final String user_id, final String branch_id) {
    return new BranchRef(getLocalBranchHolder(session_id, user_id).collection(branch_id));
  }

  /**
   * Returns the DocumentReference to the stashes document for a given user in a session.
   *
   * @param session_id the session ID
   * @param user_id the user ID
   * @return a reference to the stashes document
   */
  public DocumentReference getStashes(final String session_id, final String user_id) {
    return getLocalUserRef(session_id, user_id).document(DOC_STASHES);
  }

  /**
   * Returns the list of all stashes for a given user in a session.
   *
   * @param session_id the session ID
   * @param user_id the user ID
   * @return a list of stashes
   */
  public List<Map<String, Object>> getStashList(final String session_id, final String user_id)
      throws ExecutionException, InterruptedException {
    return (List<Map<String, Object>>)
        getStashes(session_id, user_id).get().get().get(FIELD_STASHES);
  }

  /**
   * Retrieves a list of all remote branch IDs for a given session.
   *
   * @param session_id the session ID
   * @return a list of branch IDs
   */
  public List<String> getAllRemoteBranches(final String session_id) {
    // get all subcollections in remote branches document
    Iterable<CollectionReference> branches = getRemoteBranchHolder(session_id).listCollections();
    // add branch_id for each collection
    List<String> branchIds = new ArrayList<>();
    for (CollectionReference branch : branches) {
      branchIds.add(branch.getId());
    }
    return branchIds;
  }

  /**
   * Retrieves a list of all local branch IDs for a given user in a session.
   *
   * @param session_id the session ID
   * @param user_id the user ID
   * @return a list of branch IDs
   */
  public List<String> getAllLocalBranches(final String session_id, final String user_id) {
    // get all subcollections in local branches document
    Iterable<CollectionReference> branches =
        getLocalBranchHolder(session_id, user_id).listCollections();
    // add branch_id for each collection
    List<String> branchIds = new ArrayList<>();
    for (CollectionReference branch : branches) {
      branchIds.add(branch.getId());
    }
    return branchIds;
  }
}
