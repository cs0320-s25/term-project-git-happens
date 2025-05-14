import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitResetParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  reset_commit_id: string;
}

export interface GitResetResponse extends BaseResponse {
  message?: string;
  file_map_json?: string;
  action?: string;
}

const allowedKeys: (keyof GitResetResponse)[] = [
  "error_response",
  "message",
  "file_map_json",
  "action",
];

export async function gitReset(
  params: GitResetParams
): Promise<[boolean, GitResetResponse]> {
  return typedFetch("gitreset", params, allowedKeys);
}
