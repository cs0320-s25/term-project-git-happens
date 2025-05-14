package edu.brown.cs.student.apiserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.server.mergeHelpers.MockFileObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DeleteSessionHandlerTest  extends BaseEndpointTest {
  private final String testSessionId = "test";
  private final Map<String, List<MockFileObject>> originalFileMap = new HashMap<>();
  @Test
  void testMissingParameters() {
    try {

      HttpURLConnection connection = tryRequest("deletesession?");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_bad_request", response.get("response"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testDeleteNonexistentSession() {
    try {

      HttpURLConnection connection = tryRequest("deletesession?session_id=fake");
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("error_database", response.get("response"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  @Test
  void testDeleteExistingSession() {
    try {
      List<MockFileObject> testFile1 = new ArrayList<>();
      List<MockFileObject> testFile2 = new ArrayList<>();

      testFile1.add(new MockFileObject("ingredient1", "ingredient1"));
      testFile1.add(new MockFileObject("ingredient2", "ingredient2"));
      testFile2.add(new MockFileObject("ingredient3", "ingredient3"));
      testFile2.add(new MockFileObject("ingredient4", "ingredient4"));

      originalFileMap.put("test_file1", testFile1);
      originalFileMap.put("test_file2", testFile2);

      String fileMapJson = serializeFileMap(originalFileMap);

      HttpURLConnection connection = tryRequest("createsession?session_id=" + testSessionId
          + "&user_id=test-user1&file_map_json=" + fileMapJson);
      Map<String, Object> response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

      connection = tryRequest("deletesession?session_id=" + testSessionId);
      response = deserializeResponse(connection);
      assertEquals("success", response.get("response"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
