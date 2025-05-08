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
  const arg1 = splitCommand?.[1] ?? null;
  const arg2 = splitCommand?.[2] ?? null;
  const arg3 = splitCommand?.[3] ?? null;

  switch (arg1) {
    case "add":
      // const arg2 = splitCommand?.[2] ?? null;
      switch (arg2) {
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
      switch (arg2) {
        case "-m":
          return { // Todo: HANDLE COMMIT MESSAGE???
            commandStr: "commit",
            terminalResponse: "commit",
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
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

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
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

    case "pull":
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

    case "reset":
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

    case "rm":
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

    case "stash":
      return {
        commandStr: "", // Todo: Fill
        terminalResponse: "", // Todo: Fill
      };

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
        terminalResponse: `Unknown argument "${arg1}"`,
      };
  }
}
