import { CommitData } from "../components/App";
import { IngredientImage } from "../components/game/Game";
import { fileCommit } from "../components/App";

export interface BackendCommit {
  file_map_json: string;
  commit_id: string;
  author: string;
  date_time: string;
  commit_message: string;
  parent_commits: string[];
  branch_id: string;
}

const baseUrl: string = "http://localhost:3232/";

export function createUrl(
  endpoint: string,
  parameters: { [key: string]: string | undefined }
): string {
  const queryParams = Object.entries(parameters)
    .filter(([, value]) => value !== undefined)
    .map(
      ([key, value]) =>
        `${encodeURIComponent(key)}=${encodeURIComponent(value!)}`
    )
    .join("&");

  return `${baseUrl}${endpoint}${queryParams ? `?${queryParams}` : ""}`;
}

export function toQueryParams<T extends Record<string, any>>(
  obj: T
): { [key: string]: string | undefined } {
  const queryParams: { [key: string]: string | undefined } = {};

  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      const value = obj[key];
      queryParams[key] =
        value !== undefined && value !== null ? String(value) : undefined;
    }
  }

  return queryParams;
}

export function formatErrorResponse(responseObject: any): string {
  const errorArg = responseObject.error_arg
    ? " " + responseObject.error_arg
    : "";
  const errorResponse =
    responseObject.response +
    ". " +
    responseObject.error_cause +
    ". " +
    errorArg;
  return errorResponse;
}

export function sanitizeObject<T>(data: any, allowedKeys: (keyof T)[]): T {
  const sanitized: Partial<T> = {};
  for (const key of allowedKeys) {
    if (key in data) {
      sanitized[key] = data[key];
    }
  }
  return sanitized as T;
}

export function convertBackendCommit(backendCommit: BackendCommit): CommitData {
  const files: Record<string, IngredientImage[]> = JSON.parse(backendCommit.file_map_json); 
  const contents: fileCommit[] = Object.entries(files).map(
    ([fileName, fileContents]) => ({
      fileName,
      fileContents,
    })
  );

  return {
    commit_hash: backendCommit.commit_id,
    message: backendCommit.commit_message,
    branch: backendCommit.branch_id,
    parent_commits: backendCommit.parent_commits,
    contents,
  };
}
