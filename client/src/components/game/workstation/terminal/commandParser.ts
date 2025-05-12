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
  var message = splitCommand?.[3] ?? null; // FIND WAY TO SPLIT SO THAT MSG CONTAINS ALL SECTIONS AFTER SPACE?

  // var message = ""

  // for (let i = 3; i < splitCommand?.length; i++) {
  //   message = message + " " + splitCommand?.[i]
  // }

  // return {
  //   commandStr: "stash null",
  //   terminalResponse: `Message: ${message}`,
  // };

  switch (command) {
    case "add":
      // const arg2 = splitCommand?.[2] ?? null;
      switch (tag) {
        case "-A":
          if (message) {
            // Return error if additional command provided
            return {
              commandStr: "stash null",
              terminalResponse: "Error: Unexpected command.",
            };
          }
          return {
            commandStr: "add all",
            terminalResponse: "Adding all",
          };
        case null:
          return {
            commandStr: "add null",
            terminalResponse: "Error: Nothing specified, nothing added.",
          };
        default:
          return {
            commandStr: "add null",
            terminalResponse: "Error: Try using '-A' instead?",
          };
      }

    case "commit":
      switch (tag) {
        case "-m":
          var commit_msg = checkQuotes(message, false) // ALLOW FOR MESSAGE TO BE ONE WORD NO QUOTES
          if (commit_msg) {
            return {
              // Successful commit
              commandStr: "test",
              terminalResponse: `Commit: ${commit_msg}`,
              message: `${commit_msg}`,
            };
          }
          return { // No commit msg or msg not in quotes
            commandStr: "commit null",
            terminalResponse: "Error: Make sure you provide a commit message in quotes.",
          };
        case null:
          return {
            commandStr: "commit null",
            terminalResponse: "Error: Try commit '-m' followed by a commit message in quotes.",
          };
        default:
          return {
            commandStr: "commit null",
            terminalResponse:
              "Error: Try commit '-m' followed by a commit message in quotes.",
          };
      }

    case "push":
      return {
        commandStr: "push",
        terminalResponse: "Pushing local repository.",
      };

    case "branch":
      switch (tag) {
        case "-a": // view all branches
          if (message) {
            // Return error if additional command provided
            return {
              commandStr: "stash null",
              terminalResponse: "Error: Unexpected command.",
            };
          }
          return {
            commandStr: "branch all",
            terminalResponse: "Fetching all branches.", // Return all branches
          };
        case "-r": // view remote branches
          if (message) {
            // Return error if additional command provided
            return {
              commandStr: "stash null",
              terminalResponse: "Error: Unexpected command.",
            };
          }
          return {
            commandStr: "branch remote",
            terminalResponse: "Fetching remote branches.",
          };
        case "-d": // delete
          var branchname = checkQuotes(message, true)
          if (branchname) {
            return { // Successful branch delete call
              commandStr: "branch delete",
              terminalResponse: `Deleting Branch: ${branchname}`,
              message: `${branchname}`,
            };
          }
          return { // No branchname specified
            commandStr: "branch null",
            terminalResponse: "Error: Branch name required.",
          };
        case null: // View all local branches
          return {
            commandStr: "branch local",
            terminalResponse: "Fetching local branches.",
          };
        default: // Create new branch branchname, check if already exists
          var branchname = checkQuotes(message, true);
          return {
            commandStr: "branch create",
            terminalResponse: `Creating Branch: ${branchname}`,
            message: `${branchname}`,
          };
      }

    case "diff":
      return {
        commandStr: "diff",
        terminalResponse: "", // Todo: Fill
      };

    case "log":
      if (tag) {
        // Return error if additional command provided
        return {
          commandStr: "stash null",
          terminalResponse: "Error: Unexpected command.",
        };
      }
      return {
        commandStr: "log",
        terminalResponse: "Fetching Log.",
      };

    case "merge":
      switch (tag) {
        case null: // UNSURE
          return {
            commandStr: "merge null",
            terminalResponse: "Error: Try specifying a branch to merge with.",
          };
        default: // if arg2 = branch name, merge to branch
          var branchname = checkQuotes(tag, true);
          return {
            commandStr: "merge",
            terminalResponse: `Attempting Merge: ${branchname}`,
            message: `${branchname}`,
          };
      }

    case "pull":
      if (tag) {
        // Return error if additional command provided
        return {
          commandStr: "stash null",
          terminalResponse: "Error: Unexpected command.",
        };
      }
      return {
        commandStr: "pull", // Todo: Fill
        terminalResponse: "Pulling", // Todo: Fill
      };

    case "reset":
      switch (tag) {
        case "--hard": // hard reset
          return {
            commandStr: "reset hard",
            terminalResponse: "Hard Reset",
          };
        case "--soft": // soft reset
          return {
            commandStr: "reset soft",
            terminalResponse: "Soft Reset",
          };
        case null: // UNSURE
          return {
            commandStr: "reset null",
            terminalResponse: "Error: Try specifying the type of reset with either '--soft' or '--hard'.",
          };
        default: // UNSURE
          return {
            commandStr: "reset null",
            terminalResponse:
              "Error: Try specifying the type of reset with either '--soft' or '--hard'.",
          };
      }

    case "rm":
      switch (tag) {
        case null: // error
          return {
            commandStr: "rm null",
            terminalResponse: "Error: Try specifying a file to remove.",
          };
        default: // remove filename file
          var filename = checkQuotes(tag, true);
          return {
            commandStr: "rm",
            terminalResponse: `Attempting to Remove: ${filename}`,
            message: `${filename}`,
          };
      }

    case "stash":
      switch (tag) {
        case "pop": // return to previous stash and remove from stash list
          return { // CAN USER SPECIFY COMMIT TO POP?
            commandStr: "stash pop",
            terminalResponse: "Popping last stash",
          };
        case "list": // return list of stashes
          return {
            commandStr: "stash list",
            terminalResponse: "Fetching stash list",
          };
        case null: // stash
          return {
            commandStr: "stash",
            terminalResponse: "Stashing commit",
          };
        default: // error
          return {
            commandStr: "stash null",
            terminalResponse: "Error: Unexpected command.",
          };
      }

    case "status":
      return {
        commandStr: "status",
        terminalResponse: "Fetching Status",
      };

    case null:
      return {
        commandStr: "Error: null arg1",
        terminalResponse: "Error: null arg1",
      };

    default:
      return {
        commandStr: "unknown",
        // terminalResponse: `Unknown argument "${tag}"`,
        terminalResponse: `Command: "${command}" unknown.`,
      };
  }
}

function checkQuotes(message: string, returnBack: boolean) {
  // If message is in quotes "" or '' return message without quotes, otherwise either return msg back if returnBack, or else return null
  if (message) {
    if (message[0] === "'" && message[message.length - 1] === "'") {
      return message.split("'")[1];
    } else if (message[0] === '"' && message[message.length - 1] === '"') {
      return message.split('"')[1];
    } else if (returnBack) {
      return message
    }
  }
  return null;
}