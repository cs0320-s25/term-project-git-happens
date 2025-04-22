// MAINTAINED FOR REFERENCE FOR SHAPE OF FETCH REQUEST

// /**
//  * Fetches and processes a CSV file based on the provided parameters.
//  *
//  * @param csvName - The name of the CSV file to fetch.
//  * @param hasHeader - A boolean indicating whether the CSV file has a header row.
//  * @param searchTerm - A string to search for within the CSV file. If empty, the entire CSV is viewed.
//  * @param columnIdentifier - The column to search within.
//  * @returns A promise that resolves to a tuple containing a string message and a boolean indicating success.
//  *          If the CSV is loaded successfully and a search term is provided, the result of the search is returned.
//  *          If the CSV is loaded successfully and no search term is provided, the entire CSV is viewed.
//  *          If the CSV fails to load, the error message and a boolean indicating failure are returned.
//  */
// export async function fetchCsv(
//     csvName: string,
//     hasHeader: boolean,
//     searchTerm: string,
//     columnIdentifier: string
//   ): Promise<[string, boolean]> {
//     const loadStatus: [string, boolean] = await loadCsv(csvName, hasHeader);
//     if (loadStatus[1]) {
//       if (searchTerm) {
//         return searchCsv(hasHeader, searchTerm, columnIdentifier);
//       } else {
//         return viewCsv(hasHeader);
//       }
//     } else {
//         // return the error coming from loadCsv
//         return loadStatus;
//     }
//   }
  
//   /**
//    * Loads a CSV file based on the provided parameters.
//    *
//    * @param csvName - The name of the CSV file to load.
//    * @param hasHeader - A boolean indicating whether the CSV file has a header row.
//    * @returns A promise that resolves to a tuple containing a string message and a boolean indicating success.
//    *          If the CSV is loaded successfully, the message is "success" and the boolean is true.
//    *          If the CSV fails to load, the error message and a boolean indicating failure are returned.
//    */
//   async function loadCsv(
//     csvName: string,
//     hasHeader: boolean
//   ): Promise<[string, boolean]> {
//     const hasHeaderString: string = hasHeader ? "true" : "false";
//     const loadUrl: string =
//       "http://localhost:3232/loadcsv?filepath=" +
//       csvName +
//       "&has_header=" +
//       hasHeaderString;
//     try {
//       const response = await fetch(loadUrl);
//       const responseObject = await response.json();
//       if (responseObject.response == "success") {
//         return ["success", true];
//       }
//       else {
//         // server error
//         console.log(responseObject);
//         const errorArg = responseObject.error_arg ? " " + responseObject.error_arg : "";
//         const errorResponse = responseObject.response + ". " + responseObject.error_cause + "." + errorArg;
//         return [errorResponse, false];
//       }
//     } catch (error) {
//       console.log(error);
//       return ["File Load Error", false];
//     }
//   }
  