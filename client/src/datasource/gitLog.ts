import { BaseResponse, typedFetch } from "./abstractFetch";

export interface BackendCommit {
  file_map_json: string;
  commit_id: string;
  author: string;
  date_time: string;
  commit_message: string;
  parent_commits: string[];
  branch_id: string;
}

export interface GitLogParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  verbose: string; // boolean
}

export interface GitLogResponse extends BaseResponse {
  commits?: BackendCommit[];
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
