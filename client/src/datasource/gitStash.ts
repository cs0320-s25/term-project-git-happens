import { ConflictEntry, IngredientImage } from "../components/game/Game";
import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitStashParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  stash_request: string;
  file_map_json: string;
  stash_index?: string;
}

export interface GitStashResponse extends BaseResponse {
  reset_file_map_json?: string;
  message?: string;
  action?: string;
  stashes?: Record<string, any>[]; // Map<string, any>[];
  merged_files?: Record<string, IngredientImage[]>;
  file_conflicts?: Record<string, ConflictEntry>;
}

const allowedKeys: (keyof GitStashResponse)[] = [
  "error_response",
  "reset_file_map_json",
  "message",
  "action",
  "stashes",
  "merged_files",
  "file_conflicts",
];

export async function gitStash(
  params: GitStashParams
): Promise<[boolean, GitStashResponse]> {
  return typedFetch("gitstash", params, allowedKeys);
}
