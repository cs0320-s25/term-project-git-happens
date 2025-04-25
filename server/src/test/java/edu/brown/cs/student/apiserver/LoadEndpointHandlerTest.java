// package edu.brown.cs.student.apiserver;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
//
// import com.squareup.moshi.JsonAdapter;
// import com.squareup.moshi.Moshi;
// import com.squareup.moshi.Types;
// import java.io.IOException;
// import java.net.HttpURLConnection;
// import java.util.Map;
// import okio.Buffer;
// import org.junit.jupiter.api.Test;
//
// public class LoadEndpointHandlerTest extends BaseEndpointTest {
//
//  @Test
//  public void testLoadCSVSuccess() throws IOException {
//    HttpURLConnection connection =
//        tryRequest("loadcsv?filepath=data/census/RI-Town-Income-Dataset.csv&has_header=true");
//
//    assertEquals(200, connection.getResponseCode());
//
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<Map<String, Object>> adapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//    Map<String, Object> response =
//        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
//
//    assertEquals("data/census/RI-Town-Income-Dataset.csv", response.get("filepath"));
//    assertEquals("true", response.get("has_header"));
//    assertEquals("success", response.get("response"));
//    connection.disconnect();
//  }
//
//  @Test
//  public void testLoadCSVFailure_whenNoFilepath() throws IOException {
//    HttpURLConnection connection = tryRequest("loadcsv?has_header=true");
//
//    assertEquals(200, connection.getResponseCode());
//
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<Map<String, Object>> adapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//    Map<String, Object> response =
//        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
//
//    assertEquals("filepath", response.get("error_arg"));
//    assertEquals("error_bad_request", response.get("response"));
//    assertEquals("null_parameter", response.get("error_cause"));
//    connection.disconnect();
//  }
//
//  @Test
//  public void testLoadCSVFailure_whenIllegalFilepath() throws IOException {
//    HttpURLConnection connection = tryRequest("loadcsv?filepath=illegal&has_header=true");
//
//    assertEquals(200, connection.getResponseCode());
//
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<Map<String, Object>> adapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//    Map<String, Object> response =
//        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
//
//    assertEquals("illegal", response.get("filepath"));
//    assertEquals("error_bad_request", response.get("response"));
//    assertEquals("filepath_must_start_with_data", response.get("error_cause"));
//    connection.disconnect();
//  }
//
//  @Test
//  public void testLoadCSVFailure_whenFileNotFound() throws IOException {
//    HttpURLConnection connection =
// tryRequest("loadcsv?filepath=data/nonexistent&has_header=true");
//
//    assertEquals(200, connection.getResponseCode());
//
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<Map<String, Object>> adapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//    Map<String, Object> response =
//        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
//
//    assertEquals("data/nonexistent", response.get("filepath"));
//    assertEquals("error_datasource", response.get("response"));
//    assertEquals("file_not_found", response.get("error_cause"));
//    connection.disconnect();
//  }
//
//  @Test
//  public void testLoadCSVFailure_whenNoHasHeader() throws IOException {
//    HttpURLConnection connection =
//        tryRequest("loadcsv?filepath=data/census/RI-Town-Income-Dataset.csv");
//
//    assertEquals(200, connection.getResponseCode());
//
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<Map<String, Object>> adapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//    Map<String, Object> response =
//        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
//
//    assertEquals("data/census/RI-Town-Income-Dataset.csv", response.get("filepath"));
//    assertEquals("error_bad_request", response.get("response"));
//    assertEquals("null_parameter", response.get("error_cause"));
//    assertEquals("has_header", response.get("error_arg"));
//    connection.disconnect();
//  }
//
//  @Test
//  public void testLoadCSVFailure_whenInvalidHasHeader() throws IOException {
//    HttpURLConnection connection =
//        tryRequest("loadcsv?filepath=data/census/RI-Town-Income-Dataset.csv&has_header=maybe");
//
//    assertEquals(200, connection.getResponseCode());
//
//    Moshi moshi = new Moshi.Builder().build();
//    JsonAdapter<Map<String, Object>> adapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//    Map<String, Object> response =
//        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
//
//    assertEquals("data/census/RI-Town-Income-Dataset.csv", response.get("filepath"));
//    assertEquals("maybe", response.get("has_header"));
//    assertEquals("error_bad_request", response.get("response"));
//    assertEquals("parameter_not_bool", response.get("error_cause"));
//    assertEquals("has_header", response.get("error_arg"));
//    connection.disconnect();
//  }
// }
