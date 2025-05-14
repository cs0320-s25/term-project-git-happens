import { BaseResponse, typedFetch } from "./abstractFetch";

export interface CheckSolutionParams {
  session_id: string;
  user_id: string;
  solution_branch_id: string;
  solution_file_map_json: string;
}

export interface CheckSolutionResponse extends BaseResponse {
  solution_correct?: string;
}

const allowedKeys: (keyof CheckSolutionResponse)[] = [
  "error_response",
  "solution_correct",
];

export async function checkSolution(
  params: CheckSolutionParams
): Promise<[boolean, CheckSolutionResponse]> {
  return typedFetch("checksolution", params, allowedKeys);
}
