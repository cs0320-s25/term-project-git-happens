import { BaseResponse, typedFetch } from "./abstractFetch";

export interface GitPushParams {
  session_id: string;
  user_id: string;
  branch_id: string;
}

export interface GitPushResponse extends BaseResponse {
  message?: string;
  old_head_id?: string;
  new_head_id?: string;
  action?: string;
}

const allowedKeys: (keyof GitPushResponse)[] = [
  "error_response",
  "message",
  "old_head_id",
  "new_head_id",
  "action",
];

export async function gitPush(
  params: GitPushParams
): Promise<[boolean, GitPushResponse]> {
  return typedFetch("gitpush", params, allowedKeys);
}
