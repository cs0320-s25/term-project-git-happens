import { createUrl, formatErrorResponse } from "./fetcherUtil";

export async function gitAdd(
  sessionId: string,
  userId: string,
  branchId: string,
  currentState: string
): Promise<[boolean, string | null]> {
  const url: string = createUrl(
    "gitadd",
    ["session_id", "user_id", "branch_id", "file_map_json"],
    [sessionId, userId, branchId, currentState]
  );

  try {
    const response = await fetch(url);
    const responseObject = await response.json();
    if (responseObject.response == "success") {
      return [true, null];
    } else {
      // server error
      console.log(responseObject);
    const errorResponse = formatErrorResponse(responseObject)
      return [false, errorResponse];
    }
  } catch (error) {
    console.log(error);
    return [false, "git add request error"];
  }
}
