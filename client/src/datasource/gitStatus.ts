import { BaseResponse, typedFetch } from "./abstractFetch";
export interface GitStatusParams {
  session_id: string;
  user_id: string;
  branch_id: string;
  file_map_json: string;
}

export interface GitStatusResponse extends BaseResponse {
  branch_message?: string;
  staged_changes_message?: string;
  changed_files?: string[];
  unstaged_changes_message?: string;
  unstaged_changes?: Set<string>;
}

const allowedKeys: (keyof GitStatusResponse)[] = [
  "error_response",
  "branch_message",
  "staged_changes_message",
  "changed_files",
  "unstaged_changes_message",
  "unstaged_changes",
]

export async function gitStatus(params: GitStatusParams
): Promise<[boolean, GitStatusResponse]> {
  return typedFetch("gitstatus", params, allowedKeys)
} 
