import { BaseResponse, typedFetch } from "./abstractFetch";

// TODO: we seem to have implemented git rm to be exactly the same as git add -A
// This is a problem since git rm literally does not work when taking no parameters
// need to do some changes to both the backend and the front end to make this work,
// so this handler is currently unused

export interface GitRmParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  file_map_json: string;
}

export interface GitRmResponse extends BaseResponse {
  action?: string;
}

const allowedKeys: (keyof GitRmResponse)[] = ["error_response", "action"];

export async function gitRm(
  params: GitRmParams
): Promise<[boolean, GitRmResponse]> {
  return typedFetch("gitrm", params, allowedKeys);
}
