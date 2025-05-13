import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitCommitParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  commit_message: string;
}

export interface GitCommitResponse extends BaseResponse {
  commit_id?: string;
  commit_message?: string;
  num_files_changes?: string;
  action?: string;
}

const allowedKeys: (keyof GitCommitResponse)[] = [
  "error_response",
  "commit_id",
  "commit_message",
  "num_files_changes",
  "action",
];

export async function gitCommit(
  params: GitCommitParams
): Promise<[boolean, GitCommitResponse]> {
  return typedFetch("gitcommit", params, allowedKeys);
}
