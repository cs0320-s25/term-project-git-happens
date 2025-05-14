import { IngredientImage } from "../components/game/Game";
import { BaseResponse, typedFetch } from "./abstractFetch";
import { BackendCommit } from "./fetcherUtil";

export interface GitMergeParams {
  session_id: string;
  user_id: string;
  current_branch_id: string;
  merge_branch_id: string;
  file_map_json: string;
}

interface ConflictEntry {
  incoming: IngredientImage[];
  local: IngredientImage[];
}

type FileName = "file1" | "file2" | "file3";

export type FileConflicts = Record<FileName, ConflictEntry>;


export interface GitMergeResponse extends BaseResponse {
  message?: string;
  instructions?: string;
  files_with_differences?: string[] // Set<string>;
  merged_files?: Map<string, any[]>;
  file_conflicts?: string; // FileConflicts // Map<string, Map<string, any[]>>;
  local_commit_id?: string;
  incoming_commit_id?: string;
  merge_commit_id?: string;
  differences_detected?: string;
  new_commit?: BackendCommit;
}

const allowedKeys: (keyof GitMergeResponse)[] = [
  "error_response",
  "message",
  "instructions",
  "files_with_differences",
  "merged_files",
  "file_conflicts",
  "local_commit_id",
  "incoming_commit_id",
  "merge_commit_id",
  "differences_detected",
  "new_commit",
];

export async function gitMerge(
  params: GitMergeParams
): Promise<[boolean, GitMergeResponse]> {
  return typedFetch("gitmerge", params, allowedKeys);
}
