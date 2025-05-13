import {
  createUrl,
  formatErrorResponse,
  sanitizeObject,
  toQueryParams,
} from "./fetcherUtil";

export interface BaseResponse {
  error_response?: string;
}

export async function typedFetch<
  T extends BaseResponse,
  P extends Record<string, any>
>(
  endpoint: string,
  params: P,
  allowedKeys: (keyof T)[]
): Promise<[boolean, T]> {
  const url: string = createUrl(endpoint, toQueryParams(params));

  try {
    const response = await fetch(url);
    const responseObject = await response.json();
    const sanitizedResponse: T = sanitizeObject(responseObject, allowedKeys);
    if (responseObject.response == "success") {
      return [true, sanitizedResponse];
    } else {
      // server error
      const errorResponse = formatErrorResponse(responseObject);
      sanitizedResponse.error_response = errorResponse;
      return [false, sanitizedResponse];
    }
  } catch (error) {
    console.log(error);
    return [false, { error_response: endpoint + " request error" } as T];
  }
}
