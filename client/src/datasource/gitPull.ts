import { CommitData } from "../components/App";
import { ConflictEntry } from "../components/game/Game";
import { BaseResponse, typedFetch } from "./abstractFetch";
import { BackendCommit } from "./fetcherUtil";

export interface GitPullParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  file_map_json: string;
}

export interface GitPullResponse extends BaseResponse {
  message?: string;
  files_with_differences?: Set<string>;
  merged_files?: Map<string, any[]>;
  file_conflicts?: Record<string, ConflictEntry>; // Map<string, Map<string, any[]>>;
  local_commit_id?: string;
  incoming_commit_id?: string;
  merge_commit_id?: string;
  new_commit?: BackendCommit;
}

const allowedKeys: (keyof GitPullResponse)[] = [
  "error_response",
  "message",
  "files_with_differences",
  "merged_files",
  "file_conflicts",
  "local_commit_id",
  "incoming_commit_id",
  "merge_commit_id",
  "new_commit",
];

export async function gitPull(
  params: GitPullParams
): Promise<[boolean, GitPullResponse]> {
  return typedFetch("gitpull", params, allowedKeys);
}
