import { BaseResponse, typedFetch } from "./abstractFetch";

export interface CreateSessionParameters {
  session_id: string;
  user_id: string;
  file_map_json: string;
}

export interface CreateSessionResponse extends BaseResponse {
  action?: string;
}

const allowedKeys: (keyof CreateSessionResponse)[] = [
  "error_response",
  "action",
];

export async function createSession(
  params: CreateSessionParameters
): Promise<[boolean, CreateSessionResponse]> {
  return typedFetch("createsession", params, allowedKeys);
}
