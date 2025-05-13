import { BaseResponse, typedFetch } from "./abstractFetch";

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
