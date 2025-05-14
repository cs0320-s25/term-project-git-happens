import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitBranchRequestParams {
  session_id: string;
  user_id: string;
  branch_request: string;
  current_branch_id: string;
  delete_branch_id?: string | null; // used when branch_request === "-d"
  file_map_json?: string; // used when creating a new branch
}

export interface GitBranchResponse extends BaseResponse {
  remote_branch_names?: string[];
  local_branch_names?: string[];
  delete_branch_id?: string;
  new_branch_id?: string;
  action?: string;
}

const allowedKeys: (keyof GitBranchResponse)[] = [
  "error_response",
  "remote_branch_names",
  "local_branch_names",
  "delete_branch_id",
  "new_branch_id",
  "action",
];

export async function gitBranch(
  params: GitBranchRequestParams
): Promise<[boolean, GitBranchResponse]> {
  return typedFetch("gitbranch", params, allowedKeys);
}
// const url: string = createUrl("gitbranch", toQueryParams(params));

// try {
//   const response = await fetch(url);
//   const responseObject = await response.json();
//   const sanitizedResponse: GitBranchResponse = sanitizeObject(responseObject, allowedKeys);
//   if (responseObject.response == "success") {
//     return [true, sanitizedResponse]
//   } else {
//     // server error
//     const errorResponse = formatErrorResponse(responseObject)
//     sanitizedResponse.error_response = errorResponse
//     return [false, sanitizedResponse];
//   }
// } catch (error) {
//   console.log(error);
//   return [false, {error_response: "git branch request error"}];
// }
