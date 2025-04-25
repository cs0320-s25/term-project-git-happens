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
}
