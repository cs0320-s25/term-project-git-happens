package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.Map;
import spark.Route;

/**
 * Abstract class for handling API endpoints in a Spark-based application. Implements the {@link
 * Route} interface to process HTTP requests and generate responses.
 */
public abstract class AbstractEndpointHandler implements Route {
  protected Map<String, Object> responseMap;
  private final Moshi moshi = new Moshi.Builder().build();
  private final JsonAdapter<Map<String, Object>> responseMapAdapter = makeResponseMapMoshiAdapter();

  /**
   * Creates a {@link Moshi} JSON adapter for a {@link Map} with {@link String} keys and {@link
   * Object} values.
   *
   * @return A {@link JsonAdapter} for serializing and deserializing response maps.
   */
  private JsonAdapter<Map<String, Object>> makeResponseMapMoshiAdapter() {
    final Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
    return moshi.adapter(mapType);
  }

  /**
   * Marks the response as an error using the given error parameters and returns the {@code
   * responseMap} serialized to a JSON string.
   *
   * @param errorMessage The error message to include in the response.
   * @return A JSON string representing the error response.
   */
  protected String returnErrorResponse(final String errorMessage) {
    responseMap.put("response", errorMessage);
    return responseMapAdapter.toJson(responseMap);
  }

  /**
   * Marks the response as an error using the given error parameters and returns the {@code
   * responseMap} serialized to a JSON string.
   *
   * @param errorMessage The error message to include in the response.
   * @param errorCause The cause of the error.
   * @return A JSON string representing the error response.
   */
  protected String returnErrorResponse(final String errorMessage, final String errorCause) {
    responseMap.put("error_cause", errorCause);
    return returnErrorResponse(errorMessage);
  }

  /**
   * Marks the response as an error using the given error parameters and returns the {@code
   * responseMap} serialized to a JSON string.
   *
   * @param errorMessage The error message to include in the response.
   * @param errorCause The cause of the error.
   * @param errorArg Argument that caused the error.
   * @return A JSON string representing the error response.
   */
  protected String returnErrorResponse(
      final String errorMessage, final String errorCause, final String errorArg) {
    responseMap.put("error_arg", errorArg);
    responseMap.put("error_cause", errorCause);
    return returnErrorResponse(errorMessage);
  }

  /**
   * Marks the response as successful and returns the {@code responseMap} serialized to a JSON
   * string.
   *
   * @return A JSON string representing a success response.
   */
  protected String returnSuccessResponse() {
    responseMap.put("response", "success");
    return returnResponse();
  }

  /**
   * Serializes the response map to a JSON string.
   *
   * @return A JSON string representing the response map.
   */
  private String returnResponse() {
    return responseMapAdapter.toJson(responseMap);
  }
}
