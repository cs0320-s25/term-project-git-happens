const baseUrl: string = "http://localhost:3232/";

export function createUrl(endpoint: string, parameterNames: string[], parameters: string[]): string {
    if (parameterNames.length !== parameters.length) {
      throw new Error("Parameter names and values count do not match.");
    }
  
    const queryParams = parameterNames
      .map((name, index) => `${encodeURIComponent(name)}=${encodeURIComponent(parameters[index])}`)
      .join("&");
  
    const url = `${baseUrl}${endpoint}${queryParams ? `?${queryParams}` : ""}`;
    return url;
  }

export function formatErrorResponse(responseObject: any): string {
    const errorArg = responseObject.error_arg
    ? " " + responseObject.error_arg
    : "";
  const errorResponse =
    responseObject.response +
    ". " +
    responseObject.error_cause +
    ". " +
    errorArg;
return errorResponse
}