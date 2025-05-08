import { createUrl, formatErrorResponse } from "./fetcherUtil";

export async function createSession(
  sessionId: string,
  userId: string,
  currentState: string
): Promise<[boolean, string | null]> {
  const url: string = createUrl(
    "createsession",
    ["session_id", "user_id", "file_map_json"],
    [sessionId, userId, currentState]
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
    return [false, "createSession request error"];
  }
}
