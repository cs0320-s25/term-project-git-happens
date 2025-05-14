import { BaseResponse, typedFetch } from "./abstractFetch";
import { BackendCommit } from "./fetcherUtil";

export interface GitCommitParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  commit_message: string;
  local_commit_id?: string; // included with incoming_commit_id in case of merge conflict resolution
  incoming_commit_id?: string;
}

export interface GitCommitResponse extends BaseResponse {
  commit_id?: string;
  commit_message?: string;
  num_files_changed?: string;
  action?: string;
  new_commit?: BackendCommit;
}

const allowedKeys: (keyof GitCommitResponse)[] = [
  "error_response",
  "commit_id",
  "commit_message",
  "num_files_changed",
  "action",
  "new_commit",
];

export async function gitCommit(
  params: GitCommitParams
): Promise<[boolean, GitCommitResponse]> {
  return typedFetch("gitcommit", params, allowedKeys);
}
