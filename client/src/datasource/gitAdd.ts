import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitAddParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  file_map_json: string;
}

export interface GitAddResponse extends BaseResponse {
  action?: string;
}

const allowedKeys: (keyof GitAddResponse)[] = ["error_response", "action"];

export async function gitAdd(
  params: GitAddParams
): Promise<[boolean, GitAddResponse]> {
  return typedFetch("gitadd", params, allowedKeys);
}
//   const url: string = createUrl("gitadd", toQueryParams(params));

//   try {
//     const response = await fetch(url);
//     const responseObject = await response.json();
//     if (responseObject.response == "success") {
//       return [true, {}];
//     } else {
//       // server error
//       console.log(responseObject);
//     const errorResponse = formatErrorResponse(responseObject)
//       return [false, {error_response: errorResponse}];
//     }
//   } catch (error) {
//     console.log(error);
//     return [false, {error_response: "git add request error"}];
//   }
// }
