import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitLogParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  verbose: string; // boolean
}

export interface GitLogResponse extends BaseResponse {
  commits?: Map<string, any>[];
  action?: string;
}

const allowedKeys: (keyof GitLogResponse)[] = [
  "error_response",
  "commits",
  "action",
];

export async function gitLog(
  params: GitLogParams
): Promise<[boolean, GitLogResponse]> {
  return typedFetch("gitlog", params, allowedKeys);
}
