import { Command } from "./Terminal";

export function normalizeCommand(command: string): string {
  const parts: string[] = [];
  let current = "";
  let inQuotes = false;
  let quoteChar: string | null = null;

  for (let i = 0; i < command.length; i++) {
    const char = command[i];
    const prevChar = command[i - 1];

    // Detect quote start or end
    if ((char === '"' || char === "'") && prevChar !== "\\") {
      if (inQuotes && char === quoteChar) {
        inQuotes = false;
        quoteChar = null;
      } else if (!inQuotes) {
        inQuotes = true;
        quoteChar = char;
      }
      current += char;
    } else if (/\s/.test(char) && !inQuotes) {
      if (current !== "") {
        parts.push(current);
        current = "";
      }
    } else {
      current += char;
    }
  }

  if (current !== "") {
    parts.push(current);
  }

  return parts.join(" ");
}

export function parseCommand(command: string): Command {
  const normalCommand = normalizeCommand(command);
  const splitCommand = normalCommand.split(" ");
  let commandKeyword = splitCommand[0];

  switch (commandKeyword) {
    case "clear":
      return {
        commandStr: "clear",
        terminalResponse: "Clearing terminal history...",
      };

    case "help":
      return {
        commandStr: "help",
        terminalResponse: "Available commands: clear, help",
      };

    case "git":
      return gitCommand(splitCommand);

    default:
      return {
        commandStr: "unknown",
        terminalResponse: `Unknown command "${normalCommand}"`,
      };
  }
}

export function gitCommand(splitCommand: string[]): Command {
  const command = splitCommand?.[1] ?? null;
  const tag = splitCommand?.[2] ?? null;
  const message = splitCommand?.[3] ?? null; // IF MULTIPLE WORDS (SPACE) AND DOES NOT HAVE QUOTES RETURN ERROR, IF HAS QUOTES REMOVE QUOTES AND RETURN WITHIN QUOTES
  // FOR GIT COMMIT: COMMIT MSG MUST BE IN QUOTES
  // BRANCH MUST BE ONE WORD EVEN IF IN QUOTES
  // FOR OTHERS (GIT RM?) DOES NOT NEED QUOTES FOR FILENAME
  // LOOK UP/TEST DIFFERENT REQUIREMENTS

  switch (command) {
    case "add":
      // const arg2 = splitCommand?.[2] ?? null;
      switch (tag) {
        case "-A":
          return {
            commandStr: "add all",
            terminalResponse: "Adding all",
          };
        case null:
          return {
            commandStr: "add null",
            terminalResponse: "Error: Command not available",
          };
        default:
          return {
            commandStr: "add null",
            terminalResponse: "Error: Command not available",
          };
      }

    case "commit":
      switch (tag) {
        case "-m":
          if (checkQuotes(message)) {
            return { // Successful commit
              commandStr: "test",
              terminalResponse: `Commit: ${checkQuotes(message)}`,
              message: `${checkQuotes(message)}`,
            };
          }
          return { // No commit msg or msg not in quotes
            commandStr: "commit null",
            terminalResponse: "Error: Command not available",
          };
        case null:
          return {
            commandStr: "commit null",
            terminalResponse: "Error: Command not available",
          };
        default:
          return {
            commandStr: "commit null",
            terminalResponse: "Error: Command not available",
          };
      }

    case "push":
      return {
        commandStr: "push",
        terminalResponse: "Pushing local repository",
      };

    case "branch":
      switch (tag) {
        case "-a": // view all branches
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case "-r": // view remote branches
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case "-d": // delete
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case null: // all local branches
          return {
            commandStr: "",
            terminalResponse: "",
          };
        default: // branchname
          return {
            commandStr: "",
            terminalResponse: "",
          };
      }

    case "diff":
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

    case "log":
      return {
        commandStr: "log",
        terminalResponse: "Fetching Log",
      };

    case "merge":
      switch (tag) {
        case null: // error
          return {
            commandStr: "",
            terminalResponse: "",
          };
        default: // if arg3 = branch name, merge to branch
          return {
            commandStr: "",
            terminalResponse: "",
          };
      }

    case "pull":
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

    case "reset":
      switch (tag) {
        case null: // error
          return {
            commandStr: "",
            terminalResponse: "",
          };
        default: // if arg3 = branch name, merge to branch
          return {
            commandStr: "",
            terminalResponse: "",
          };
      }

    case "rm":
      switch (tag) {
        case "--hard": // hard reset
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case "--soft": // soft reset
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case null: // error
          return {
            commandStr: "",
            terminalResponse: "",
          };
        default: // error
          return {
            commandStr: "",
            terminalResponse: "",
          };
      }

    case "stash":
      switch (tag) {
        case "pop": // return to previous stash and remove from stash list
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case "list": // return stashes
          return {
            commandStr: "",
            terminalResponse: "",
          };
        case null: // stash w no message
          return {
            commandStr: "",
            terminalResponse: "",
          };
        default: // stash message
          return {
            commandStr: "",
            terminalResponse: "",
          };
      }

    case "status":
      return {
        commandStr: "status",
        terminalResponse: "Fetching Status",
      };

    case null:
      return {
        commandStr: "null arg1",
        terminalResponse: "null arg1",
      };

    default:
      return {
        commandStr: "unknown",
        // terminalResponse: `Unknown argument "${tag}"`,
        terminalResponse: `Command: "${command}" Tag: "${tag}" Message: "${message}"`,
      };
  }
}

function checkQuotes(message: string) {
  // If message is in quotes "" or '' return message without quotes, otherwise return null
  if (message) {
    if (message[0] === "'" && message[message.length - 1] === "'") {
      return message.split("'")[1];
    } else if (message[0] === '"' && message[message.length - 1] === '"') {
      return message.split('"')[1];
    }
  }
  return null;
}
