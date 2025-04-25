// MAINTAINED AS AN EXAMPLE FOR MAKING AN ENDPOINT HANDLER

// package edu.brown.cs.student.main.server;

// import edu.brown.cs.student.main.parser.LazyParser;
// import edu.brown.cs.student.main.parser.Parser;
// import java.io.FileReader;
// import java.io.IOException;
// import java.util.HashMap;
// import java.util.List;
// import spark.Request;
// import spark.Response;

// /**
//  * Handles requests for loading CSV data into the server. Extends {@link AbstractEndpointHandler}
// to
//  * provide response formatting.
//  */
// public class LoadEndpointHandler extends AbstractEndpointHandler {

//   private final LazyParser lazyParser;

//   /**
//    * Creates a loadcsv endpoint handler that loads CSVs into the given {@link LazyParser}.
//    *
//    * @param lazyParser the parser to be loaded with CSV data
//    */
//   public LoadEndpointHandler(final LazyParser lazyParser) {
//     this.lazyParser = lazyParser;
//   }

//   /**
//    * Handles HTTPrequests for loading a CSV file.
//    *
//    * @param request The request object containing HTTP request details. The request must include
//    *     {@code "filepath} and {@code "has_header"} parameters. {@code "has_header"} must be a
//    *     string representing a boolean.
//    * @param response UNUSED. The response object used to modify the response.
//    * @return A JSON-formatted success response if the file is loaded successfully, or an error
//    *     response otherwise.
//    * @throws Exception If an unexpected error occurs during file processing.
//    */
//   @Override
//   public Object handle(Request request, Response response) throws Exception {
//     responseMap = new HashMap<>();
//     // request must contain "filepath"
//     // probably want to include "has-header"

//     final String filepath = request.queryParams("filepath");
//     if (filepath == null) {
//       return returnErrorResponse("error_bad_request", "null_parameter", "filepath");
//     } else {
//       responseMap.put("filepath", filepath);
//     }
//     if (!filepath.startsWith("data/")) {
//       return returnErrorResponse("error_bad_request", "filepath_must_start_with_data");
//     }

//     final String hasHeader = request.queryParams("has_header");

//     if (hasHeader == null) {
//       return returnErrorResponse("error_bad_request", "null_parameter", "has_header");
//     } else {
//       responseMap.put("has_header", hasHeader);
//     }

//     if (!hasHeader.equalsIgnoreCase("true") && !hasHeader.equalsIgnoreCase("false")) {
//       return returnErrorResponse("error_bad_request", "parameter_not_bool", "has_header");
//       // has-header field is not true/True/false/False
//       // we need to do this check because parseBoolean returns false for any non true/True inputs
//     }

//     lazyParser.setFilePath(filepath);
//     lazyParser.setHasHeader(Boolean.parseBoolean(hasHeader));

//     // we don't actually care much about this parser getting created here, we just need to
//     // create it here rather than later so that we get errors thrown in the load API, not later
//     final Parser<List<String>, FileReader> parser;
//     try {
//       parser = lazyParser.getParser();
//     } catch (IOException e) {
//       return returnErrorResponse("error_datasource", "file_not_found");
//     } catch (Exception e) {
//       // should not actually hit this
//       return returnErrorResponse("error_bad_request", "parser_creation_failed");
//     }

//     try {
//       // we prefer parsing the parser here because the parse method can only be called once
//       // could consider modifying Parser to return early if parse method called on pre-parsed
//       // parser instead
//       parser.parse();
//     } catch (Exception e) {
//       // this should not actually happen (the TrivialParser is very lenient)
//       return returnErrorResponse("error_bad_request", "parser_parse_failed");
//     }

//     return returnSuccessResponse();
//   }

//   /**
//    * Marks the response as an error using the given error parameters and returns the {@code
//    * responseMap} serialized to a JSON string.
//    *
//    * <p>Also unloads the {@link LazyParser} so that a subsequent search or view before a valid
// load
//    * will return an error.
//    *
//    * @param errorMessage The error message to include in the response.
//    * @return A JSON string representing the error response.
//    */
//   @Override
//   protected String returnErrorResponse(final String errorMessage) {
//     // if load returns an error, also unload the parser from whatever it had before
//     lazyParser.setFilePath(null);
//     return super.returnErrorResponse(errorMessage);
//   }
// }
