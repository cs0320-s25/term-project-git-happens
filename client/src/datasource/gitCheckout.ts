import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitCheckoutParams {
  session_id: string;
  user_id: string;
  current_branch_id: string;
  new_branch_id: string;
  file_map_json: string;
}

export interface GitCheckoutResponse extends BaseResponse {
  files_with_differences?: Set<String>;
  message?: string;
  new_branch_id?: string;
  action?: string;
  difference_detected?: boolean;
  file_map_json?: string;
}

const allowedKeys: (keyof GitCheckoutResponse)[] = [
  "error_response",
  "files_with_differences",
  "message",
  "new_branch_id",
  "action",
  "difference_detected",
  "file_map_json",
];

export async function gitCheckout(
  params: GitCheckoutParams
): Promise<[boolean, GitCheckoutResponse]> {
  return typedFetch("gitcheckout", params, allowedKeys);
}

//   const url: string = createUrl("gitcheckout", toQueryParams(params));

//   try {
//     const response = await fetch(url);
//     const responseObject = await response.json();
//     const sanitizedResponse: GitCheckoutResponse = sanitizeObject(responseObject, allowedKeys);
//     if (responseObject.response == "success") {
//       return [true, sanitizedResponse];
//     } else {
//     const errorResponse = formatErrorResponse(responseObject);
//     sanitizedResponse.error_response = errorResponse;
//       return [false, sanitizedResponse];
//     }
//   } catch (error) {
//     console.log(error);
//     return [false, {error_response: "git checkout request error"}];
//   }
// }
