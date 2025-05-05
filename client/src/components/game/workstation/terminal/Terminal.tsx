import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../../styles/game.css";
import { parseCommand } from "./commandParser";
import { IngredientImage } from "../../Game";
import type { CommitData, BranchData } from "../../../App";

interface TerminalProps {
  workstationItems: IngredientImage[];
  setWorkstationItems: Dispatch<SetStateAction<IngredientImage[]>>;
  plateItems: IngredientImage[];
  setPlateItems: Dispatch<SetStateAction<IngredientImage[]>>;
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
}

export interface Command {
  commandStr: string;
  terminalResponse: string;
}

export function Terminal(props: TerminalProps) {
  const [commandHistory, setCommandHistory] = useState<string[]>([]);
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

    switch (commandStr) {
      case "clear":
        setCommandHistory([]);
        break;
      case "add all":
        props.setPlateItems(props.workstationItems);
        setCommandHistory((prev) => [...prev, terminalResponse]);
        break;
      case "commit":
        const commits = props.branchData.commits;
        const branches = props.branchData.branches;
        const newCommit = {
          commit_hash: "m",
          message: "added commit",
          branch: "main",
          parent_commits: ["l"],
          contents: ["aaaaa"],
        };
        commits.push(newCommit);

        props.setBranchData({ commits: commits, branches: branches });
        break;
      default:
        setCommandHistory((prev) => [...prev, terminalResponse]);
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

  return (
    <div className="terminal-container">
      <div
        className="terminal-history"
        ref={historyRef}
        onClick={handleHistoryClick}
      >
        {commandHistory.map((cmd, idx) => (
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
