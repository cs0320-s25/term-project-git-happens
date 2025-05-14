import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../../styles/game.css";
import { parseCommand } from "./commandParser";
import { IngredientImage } from "../../Game";
import type { CommitData, BranchData } from "../../../App";
import type { BranchType } from "../../Game";
import { gitAdd } from "../../../../datasource/gitAdd";
import { gitBranch } from "../../../../datasource/gitBranch";
import { gitCheckout } from "../../../../datasource/gitCheckout";
import { gitCommit, GitCommitParams } from "../../../../datasource/gitCommit";
import { gitLog } from "../../../../datasource/gitLog";
import { gitMerge } from "../../../../datasource/gitMerge";
import { gitPull } from "../../../../datasource/gitPull";
import { gitPush } from "../../../../datasource/gitPush";
import { gitReset } from "../../../../datasource/gitReset";
import { gitRm } from "../../../../datasource/gitRm";
import { gitStash } from "../../../../datasource/gitStash";
import { gitStatus } from "../../../../datasource/gitStatus";
import {
  BackendCommit,
  convertBackendCommit,
} from "../../../../datasource/fetcherUtil";

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
  showPopup: boolean;
  setShowPopup: Dispatch<SetStateAction<boolean>>;
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
  const [midMerge, setMidMerge] = useState<boolean>(false);
  const [mergeLocalId, setMergeLocalId] = useState<string>("");
  const [mergeIncomingId, setMergeIncomingId] = useState<string>("");

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

  function makeFileMapJson(useCommitted: boolean): string {
    const returnMap: { [key: string]: IngredientImage[] } = {};
    if (useCommitted) {
      returnMap["file1"] = props.plate1Items;
      returnMap["file2"] = props.plate2Items;
      returnMap["file3"] = props.plate3Items;
    } else {
      returnMap["file1"] = props.workstation1Items;
      returnMap["file2"] = props.workstation2Items;
      returnMap["file3"] = props.workstation3Items;
    }
    return JSON.stringify(returnMap);
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
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitAdd({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          file_map_json: makeFileMapJson(false),
        }).then((response) => {
          // no message on success, only error message
          if (!response[0]) {
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        props.setPlate1Items(props.workstation1Items);
        props.setPlate2Items(props.workstation2Items);
        props.setPlate3Items(props.workstation3Items);

        break;
      case "commit success":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        const gitCommitParams: GitCommitParams = {
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          commit_message: message!,
        };
        if (midMerge) {
          gitCommitParams.incoming_commit_id = mergeIncomingId;
          gitCommitParams.incoming_commit_id = mergeIncomingId;
          gitCommitParams.local_commit_id = mergeLocalId;
        }
        gitCommit(gitCommitParams).then((response) => {
          if (response[0]) {
            // success
            const newBackendCommit: BackendCommit = response[1].new_commit!;
            const newFrontendCommit: CommitData =
              convertBackendCommit(newBackendCommit);
            const updatedCommits = [
              ...props.branchData.commits,
              newFrontendCommit,
            ];
            props.setBranchData({
              ...props.branchData,
              commits: updatedCommits,
            });
            const terminalCommitMessage: string =
              "[" + props.currentBranch + "] " + message;
            const numFilesChanged: string = response[1].num_files_changed!;
            const filesChangedMessage: string =
              numFilesChanged == "1"
                ? "1 file changed"
                : numFilesChanged + " files changed";
            setTerminalHistory((prev) => [...prev, terminalCommitMessage]);
            setTerminalHistory((prev) => [...prev, filesChangedMessage]);
            if (midMerge) {
              setTerminalHistory((prev) => [...prev, "Merge Completed"]);
              setMidMerge(false);
            }
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        break;
      case "push":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitPush({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
        }).then((response) => {
          if (response[0]) {
            // success
            setTerminalHistory((prev) => [...prev, response[1].message!]);
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
            if (response[1].message !== undefined) {
              const message: string = response[1].message;
              setTerminalHistory((prev) => [...prev, message]);
            }
          }
        });
        break;
      case "branch all":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitBranch({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_request: "-a",
          current_branch_id: props.currentBranch,
        }).then((response) => {
          if (response[0]) {
            // success
            response[1].local_branch_names!.forEach((branchName) =>
              setTerminalHistory((prev) => [...prev, branchName])
            );
            response[1].remote_branch_names!.forEach((branchName) =>
              setTerminalHistory((prev) => [...prev, branchName])
            );
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        break;
      case "branch remote":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitBranch({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_request: "-r",
          current_branch_id: props.currentBranch,
        }).then((response) => {
          if (response[0]) {
            // success
            response[1].remote_branch_names!.forEach((branchName) =>
              setTerminalHistory((prev) => [...prev, branchName])
            );
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        break;
      case "branch delete":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitBranch({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_request: "-a",
          current_branch_id: props.currentBranch,
          delete_branch_id: message,
        }).then((response) => {
          if (response[0]) {
            // success
            setTerminalHistory((prev) => [
              ...prev,
              "Deleted branch " + response[1].delete_branch_id,
            ]);
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        break;
      case "branch local":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitBranch({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_request: "",
          current_branch_id: props.currentBranch,
        }).then((response) => {
          if (response[0]) {
            // success
            response[1].local_branch_names!.forEach((branchName) =>
              setTerminalHistory((prev) => [...prev, branchName])
            );
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        break;
      case "branch create":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        gitBranch({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_request: message!,
          current_branch_id: props.currentBranch,
          file_map_json: makeFileMapJson(false),
        }).then((response) => {
          if (response[0]) {
            // success
            const newBranchName: string = response[1].new_branch_id!;
            props.setNewBranch(() => newBranchName);
            const updatedBranches = [
              ...props.branchData.branches,
              { name: newBranchName },
            ];
            props.setBranchData({
              ...props.branchData,
              branches: updatedBranches,
            });
            setTerminalHistory((prev) => [
              ...prev,
              "Branch successfully created: " + newBranchName,
            ]);
          } else {
            // error
            setTerminalHistory((prev) => [
              ...prev,
              response[1].error_response!,
            ]);
          }
        });
        break;
      case "log":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitLog
        break;
      case "merge":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitMerge
        break;
      case "pull":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitPull
        break;
      case "reset hard":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitReset hard
        break;
      case "reset soft":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitReset soft
        break;
      case "rm":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitRm
        break;
      case "stash pop":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitStashPop
        break;
      case "stash list":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitStashList
        break;
      case "stash":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitStash
        break;
      case "stash pop index":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
        // TODO: call gitStashPop with specific index
        break;
      case "status":
        setTerminalHistory((prev) => [...prev, terminalResponse]);
        setCommandHistory((prev) => [...prev, command]);
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
