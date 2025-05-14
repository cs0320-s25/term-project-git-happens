package edu.brown.cs.student.main.server.storage;

import static edu.brown.cs.student.main.server.storage.FirestoreConstants.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FirebaseUtilHelpers {

  // holds all randomly generated commit ids
  private final Set<String> commitIds = new HashSet<>();

  private final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");

  /**
   * Deletes the given document and all its subcollections.
   *
   * @param doc the document to delete
   */
  public void deleteDocument(DocumentReference doc) {
    // for each subcollection, run deleteCollection()
    Iterable<CollectionReference> collections = doc.listCollections();
    for (CollectionReference collection : collections) {
      deleteCollection(collection);
    }
    // then delete the document
    doc.delete();
  }

  /**
   * Recursively deletes all documents within the given collection. See <a
   * href="https://firebase.google.com/docs/firestore/manage-data/delete-data#collections">Firebase
   * documentation</a>
   *
   * @param collection the collection to delete
   */
  public void deleteCollection(CollectionReference collection) {
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

  /**
   * Method that generates a unique 6 character ID to be used for saved commits and stashes
   *
   * @return - 6-character string that has not been used before
   */
  public String generateUniqueCommitId() {
    String generatedId = generateCommitId();
    while (this.commitIds.contains(generatedId)) {
      generatedId = generateCommitId();
    }
    commitIds.add(generatedId);
    return generatedId;
  }

  /**
   * Generates a map representing a commit with metadata.
   *
   * @param fileMap the file map JSON string
   * @param commitId the commit ID
   * @param author the author of the commit
   * @param commitMessage the commit message
   * @param parentCommitIds the commit ID of the parent(s) (could be 2 in case of merge)
   * @return a map containing commit metadata
   */
  public Map<String, Object> createCommit(
      final String fileMap,
      final String commitId,
      final String author,
      final String commitMessage,
      final List<String> parentCommitIds) {
    Map<String, Object> commit = new HashMap<>();
    commit.put(FIELD_FILE_MAP_JSON, fileMap);
    commit.put(FIELD_COMMIT_ID, commitId);
    commit.put(FIELD_AUTHOR, author);
    commit.put(FIELD_DATE_TIME, formatter.format((ZonedDateTime.now())));
    commit.put(FIELD_COMMIT_MESSAGE, commitMessage);
    commit.put(FIELD_PARENT_COMMIT_IDS, parentCommitIds);
    return commit;
  }
}
