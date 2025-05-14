import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../../styles/game.css";
import { parseCommand } from "./commandParser";
import { IngredientImage } from "../../Game";
import type { CommitData, BranchData } from "../../../App";
import type { BranchType } from "../../Game";

interface TerminalProps {
  workstation1Items: IngredientImage[];
  setWorkstation1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation2Items: IngredientImage[];
  setWorkstation2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  workstation3Items: IngredientImage[];
  setWorkstation3Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate1Items: IngredientImage[];
  setPlate1Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate2Items: IngredientImage[];
  setPlate2Items: Dispatch<SetStateAction<IngredientImage[]>>;
  plate3Items: IngredientImage[];
  setPlate3Items: Dispatch<SetStateAction<IngredientImage[]>>;
  branchData: {
    commits: CommitData[];
    branches: BranchData[];
  };
  setBranchData: Dispatch<
    SetStateAction<{
      commits: CommitData[];
      branches: BranchData[];
    }>
  >;
  currentBranch: string;
  setCurrentBranch: Dispatch<SetStateAction<string>>;
  newBranch: string;
  setNewBranch: Dispatch<SetStateAction<string>>;
  branchTypes: BranchType[];
  setBranchTypes: Dispatch<SetStateAction<BranchType[]>>;
  sessionID: string;
  userID: string;
}

// can do branch stuff by changing currentBranch / setCurrentBranch

export interface Command {
  commandStr: string;
  terminalResponse: string;
  message?: string;
}

export function Terminal(props: TerminalProps) {
  const [commandHistory, setCommandHistory] = useState<string[]>([]);
  const [terminalHistory, setTerminalHistory] = useState<string[]>([]);
  const [currentInput, setCurrentInput] = useState("");
  const [historyIndex, setHistoryIndex] = useState<number | null>(null);

  const inputRef = useRef<HTMLInputElement | null>(null);
  const historyRef = useRef<HTMLDivElement | null>(null); // Ref for scrolling

  // Scroll to bottom whenever the command history updates
  useEffect(() => {
    if (historyRef.current) {
      historyRef.current.scrollTop = historyRef.current.scrollHeight;
    }
  }, [commandHistory]);

  // Focus the terminal input when the history is clicked
  function handleHistoryClick() {
    if (window.getSelection()?.toString()) {
      return; // If there's selected text, do nothing
    }

    // Otherwise, focus the input field
    if (inputRef.current) {
      inputRef.current.focus(); // Focus the input
    }
  }

  function handleCommandSubmit(command: string) {
    if (command.trim() === "") return;

    const parsed = parseCommand(command); // Parse the command here
    const commandStr = parsed.commandStr;
    const terminalResponse = parsed.terminalResponse;
    const message = parsed.message ? parsed.message : null;

    switch (commandStr) {
      case "clear":
        setTerminalHistory([]);
        setCommandHistory((prev) => [...prev, command]);
        break;
      case "add all":
        // TODO: call gitAdd handler and update props accordingly
        props.setPlate1Items(props.workstation1Items);
        props.setPlate2Items(props.workstation2Items);
        props.setPlate3Items(props.workstation3Items);
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        break;
      case "commit success":
        // TODO: call gitCommit and either update props accordingly from commit response or
        const commits = props.branchData.commits;
        const branches = props.branchData.branches;

        // or don't make a new commit, just call git log verbose and update commits
        const newCommit = {
          commit_hash: "m",
          message: message,
          branch: "main",
          parent_commits: ["l"],
          contents: ["aaaaa"],
        };
        commits.push(newCommit);

        props.setBranchData({ commits: commits, branches: branches });
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        break;
      case "push":
        // TODO: call gitPush
        break;
      case "branch all":
        // TODO: call gitBranch
        break;
      case "branch remote":
        // TODO: call gitBranch remote
        break;
      case "branch delete":
        // TODO: call gitBranch delete
        break;
      case "branch local":
        // TODO: call gitBranch local
        break;
      case "branch create":
        // TODO: call gitBranch create, make sure to store the name of the newly created branch in the prop newBranch
        break;
      case "log":
        // TODO: call gitLog
        break;
      case "merge":
        // TODO: call gitMerge
        break;
      case "pull":
        // TODO: call gitPull
        break;
      case "reset hard":
        // TODO: call gitReset hard
        break;
      case "reset soft":
        // TODO: call gitReset soft
        break;
      case "rm":
        // TODO: call gitRm
        break;
      case "stash pop":
        // TODO: call gitStashPop
        break;
      case "stash list":
        // TODO: call gitStashList
        break;
      case "stash":
        // TODO: call gitStash
        break;
      case "stash pop index":
        // TODO: call gitStashPop with specific index
        break;
      case "status":
        // TODO: call gitStatus
        break;
      default: // error commandstrs
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
    }

    setCurrentInput("");
    setHistoryIndex(null);
  }

  function handleKeyDown(ev: React.KeyboardEvent<HTMLInputElement>) {
    if (ev.key === "Enter" && !ev.shiftKey) {
      ev.preventDefault();
      handleCommandSubmit(currentInput);
    } else if (ev.key === "ArrowUp") {
      ev.preventDefault();
      setHistoryIndex((prev) => {
        const newIndex =
          prev === null ? commandHistory.length - 1 : Math.max(prev - 1, 0);
        setCurrentInput(commandHistory[newIndex] || "");
        return newIndex;
      });
    } else if (ev.key === "ArrowDown") {
      ev.preventDefault();
      setHistoryIndex((prev) => {
        if (prev === null) return null;
        const newIndex = Math.min(prev + 1, commandHistory.length);
        if (newIndex === commandHistory.length) {
          setCurrentInput("");
          return null;
        }
        setCurrentInput(commandHistory[newIndex]);
        return newIndex;
      });
    }
  }

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      const target = e.target as HTMLElement;

      const isTyping =
        target.tagName === "INPUT" ||
        target.tagName === "TEXTAREA" ||
        target.isContentEditable;

      if (isTyping) return;

      if (e.shiftKey && e.key === "T") {
        e.preventDefault(); // Prevent default action for Shift + T
        inputRef.current?.focus(); // Focus the input field
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <div className="terminal-container">
      <div
        className="terminal-history"
        ref={historyRef}
        onClick={handleHistoryClick}
      >
        {terminalHistory.map((cmd, idx) => (
          <div key={idx}>$ {cmd}</div>
        ))}
      </div>
      <input
        className="terminal-input"
        ref={inputRef}
        value={currentInput}
        onChange={(ev) => setCurrentInput(ev.target.value)}
        onKeyDown={(ev) => handleKeyDown(ev)}
        placeholder="Type your command..."
      />
    </div>
  );
}
