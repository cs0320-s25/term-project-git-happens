const baseUrl: string = "http://localhost:3232/";

// export function createUrl(endpoint: string, parameterNames: string[], parameters: string[]): string {
//     if (parameterNames.length !== parameters.length) {
//       throw new Error("Parameter names and values count do not match.");
//     }

//     const queryParams = parameterNames
//       .map((name, index) => `${encodeURIComponent(name)}=${encodeURIComponent(parameters[index])}`)
//       .join("&");

//     const url = `${baseUrl}${endpoint}${queryParams ? `?${queryParams}` : ""}`;
//     return url;
//   }

export function createUrl(
  endpoint: string,
  parameters: { [key: string]: string | undefined }
): string {
  const queryParams = Object.entries(parameters)
    .filter(([, value]) => value !== undefined)
    .map(
      ([key, value]) =>
        `${encodeURIComponent(key)}=${encodeURIComponent(value!)}`
    )
    .join("&");

  return `${baseUrl}${endpoint}${queryParams ? `?${queryParams}` : ""}`;
}

export function toQueryParams<T extends Record<string, any>>(
  obj: T
): { [key: string]: string | undefined } {
  const queryParams: { [key: string]: string | undefined } = {};

  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      const value = obj[key];
      queryParams[key] =
        value !== undefined && value !== null ? String(value) : undefined;
    }
  }

  return queryParams;
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
  return errorResponse;
}

export function sanitizeObject<T>(data: any, allowedKeys: (keyof T)[]): T {
  const sanitized: Partial<T> = {};
  for (const key of allowedKeys) {
    if (key in data) {
      sanitized[key] = data[key];
    }
  }
  return sanitized as T;
}
