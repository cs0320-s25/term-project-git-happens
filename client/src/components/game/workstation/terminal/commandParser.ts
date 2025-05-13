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
      return gitCommand(splitCommand, normalCommand);

    default:
      return {
        commandStr: "unknown",
        terminalResponse: `Unknown command "${normalCommand}"`,
      };
  }
}

export function gitCommand(
  splitCommand: string[],
  normalCommand: string
): Command {
  const command = splitCommand?.[1] ?? null;
  const tag = splitCommand?.[2] ?? null;

  var message;
  if (splitCommand?.[3]) {
    var msg_split = normalCommand.slice(
      splitCommand?.[0].length + command.length + tag.length + 3
    ); // Message = slcie of normalCommand excluding command + git command + tag (includes spaces/quotes)
    message = checkQuotes(msg_split);
  }

  // return {
  //   commandStr: "stash null",
  //   terminalResponse: `Command: ${command}, Tag: ${tag}, Message: ${message}`,
  // };

  switch (command) {
    case "add":
      // const arg2 = splitCommand?.[2] ?? null;
      switch (tag) {
        case "-A":
          if (message) {
            // Return error if additional command provided
            return {
              commandStr: "add null",
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
          // var commit_msg = checkQuotes(message, false)
          if (message) {
            return {
              // Successful commit
              commandStr: "test",
              terminalResponse: `Commit: ${message}`,
              message: `${message}`,
            };
          }
          return {
            // No commit msg or msg not in quotes
            commandStr: "commit null",
            terminalResponse:
              "Error: Make sure you provide a commit message in quotes.",
          };
        case null:
          return {
            commandStr: "commit null",
            terminalResponse:
              "Error: Try commit '-m' followed by a commit message in quotes.",
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
              commandStr: "branch null",
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
              commandStr: "branch null",
              terminalResponse: "Error: Unexpected command.",
            };
          }
          return {
            commandStr: "branch remote",
            terminalResponse: "Fetching remote branches.",
          };
        case "-d": // delete
          if (message) {
            var branchname = checkQuotes(message);
            return {
              // Successful branch delete call
              commandStr: "branch delete",
              terminalResponse: `Deleting Branch: ${message}`,
              message: `${message}`,
            };
          }
          return {
            // No branchname specified
            commandStr: "branch null",
            terminalResponse: "Error: Branch name required.",
          };
        case null: // View all local branches
          return {
            commandStr: "branch local",
            terminalResponse: "Fetching local branches.",
          };
        default: // Create new branch branchname, check if already exists
          if (!message) {
            var branchname = checkQuotes(tag);
            return {
              commandStr: "branch create",
              terminalResponse: `Creating Branch: ${branchname}`,
              message: `${branchname}`,
            };
          }
          return {
            commandStr: "branch null",
            terminalResponse: "Error: Branch name required.",
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
          commandStr: "log null",
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
          var branchname = checkQuotes(tag); // Removes quotes from branchname
          if (!message) {
            // Checks that additional spaces are NOT included in branchname specification
            return {
              commandStr: "merge",
              terminalResponse: `Attempting Merge: ${branchname}`,
              message: `${branchname}`,
            };
          }
          return {
            commandStr: "merge null",
            terminalResponse: "Error: Try specifying a branch to merge with.",
          };
      }

    case "pull":
      if (tag) {
        // Return error if additional command provided
        return {
          commandStr: "pull null",
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
            terminalResponse:
              "Error: Try specifying the type of reset with either '--soft' or '--hard'.",
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
          var filename = checkQuotes(tag); // Removes quotes from filename
          if (!message) {
            // Checks that additional spaces are NOT included in filename specification
            return {
              commandStr: "rm",
              terminalResponse: `Attempting to Remove: ${filename}`,
              message: `${filename}`,
            };
          }
          return {
            commandStr: "rm null",
            terminalResponse: "Error: Try specifying a file to remove.",
          };
      }

    case "stash":
      switch (tag) {
        case "pop": // return to previous stash and remove from stash list
          return {
            // CAN USER SPECIFY COMMIT TO POP?
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
        default: // Check if popping specific index
          var pop_tag = tag.split("{")[1].split("}"); // Gets value inside brackets
          if (
            // Checks that index to pop is specified and is a valid number
            tag.slice(0, 5) === "pop@{" &&
            tag.slice(-1) === "}" &&
            !isNaN(parseFloat(pop_tag[0])) &&
            isFinite(Number(pop_tag[0]))
          ) {
            return {
              // Pop index
              commandStr: "stash pop",
              terminalResponse: `Popping index: ${pop_tag[0]}`,
              message: `${pop_tag[0]}`,
            };
          }
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

function checkQuotes(msg_split: string) {
  if (msg_split) {
    // Checks for quotes and removes if applicable, returns null if message has multiple words w/o quotes
    if (msg_split[0] === "'" && msg_split[msg_split.length - 1] === "'") {
      return msg_split.split("'")[1];
    } else if (
      msg_split[0] === '"' &&
      msg_split[msg_split.length - 1] === '"'
    ) {
      return msg_split.split('"')[1];
    } else if (msg_split.split(" ").length === 1) {
      return msg_split;
    }
  }
  return null;
}
