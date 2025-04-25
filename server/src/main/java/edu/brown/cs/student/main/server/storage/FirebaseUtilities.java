package edu.brown.cs.student.main.server.storage;

import com.google.api.core.ApiFuture;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Need to sort out the structure of the database to make this properly
// probably: "session" -> session_name -> {level: number}, {document: however we are storing documents}
public class FirebaseUtilities implements StorageInterface {

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
    // adds a new document 'doc_name' to colleciton 'collection_id' for user 'user'
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
}
