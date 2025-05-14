import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../../styles/game.css";
import { parseCommand } from "./commandParser";
import { IngredientImage } from "../../Game";
import type { CommitData, BranchData, fileCommit } from "../../../App";
import type { BranchType } from "../../Game";
import { gitAdd } from "../../../../datasource/gitAdd";
import { gitBranch } from "../../../../datasource/gitBranch";
import { gitCheckout } from "../../../../datasource/gitCheckout";
import { gitCommit, GitCommitParams } from "../../../../datasource/gitCommit";
import { gitLog } from "../../../../datasource/gitLog";
import { FileConflicts, gitMerge } from "../../../../datasource/gitMerge";
import { gitPull } from "../../../../datasource/gitPull";
import { gitPush } from "../../../../datasource/gitPush";
import { gitReset } from "../../../../datasource/gitReset";
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

  function makeFileMapJson(useStaged: boolean): string {
    const returnMap: { [key: string]: IngredientImage[] } = {};
    if (useStaged) {
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

  function setupWorkspace(fileInfo: fileCommit[]) {
    fileInfo.forEach((f) => {
      if (f.fileName === "file1") {
        props.setWorkstation1Items(() => f.fileContents);
        props.setPlate1Items(() => f.fileContents);
      }
      if (f.fileName === "file2") {
        props.setWorkstation2Items(() => f.fileContents);
        props.setPlate2Items(() => f.fileContents);
      }
      if (f.fileName === "file3") {
        props.setWorkstation3Items(() => f.fileContents);
        props.setPlate3Items(() => f.fileContents);
      }
    });
  }

  function addToTerminal(text: string) {
    setTerminalHistory((prev) => [...prev, text]);
  }

  function formatGitLog(commits: BackendCommit[]) {
    commits.forEach((c) => {
      addToTerminal("commit " + c.commit_id);
      addToTerminal("Author: " + c.author);
      addToTerminal("Date: " + c.date_time);
      addToTerminal(c.commit_message);
    });
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
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitAdd({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          file_map_json: makeFileMapJson(false),
        }).then((response) => {
          // no message on success, only error message
          if (!response[0]) {
            addToTerminal(response[1].error_response!);
          }
        });
        props.setPlate1Items(props.workstation1Items);
        props.setPlate2Items(props.workstation2Items);
        props.setPlate3Items(props.workstation3Items);

        break;
      case "commit success":
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);
        const gitCommitParams: GitCommitParams = {
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          commit_message: message!,
        };
        if (midMerge) {
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
            addToTerminal(terminalCommitMessage);
            addToTerminal(filesChangedMessage);
            if (midMerge) {
              addToTerminal("Merge Completed");
              setMidMerge(false);
            }
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "push":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitPush({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
        }).then((response) => {
          if (response[0]) {
            // success
            addToTerminal(response[1].message!);
          } else {
            // error
            addToTerminal(response[1].error_response!);
            if (response[1].message !== undefined) {
              const pushMessage: string = response[1].message;
              addToTerminal(pushMessage);
            }
          }
        });
        break;
      case "branch all":
        addToTerminal(command);
        addToTerminal(terminalResponse);
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
              addToTerminal(branchName)
            );
            response[1].remote_branch_names!.forEach((branchName) =>
              addToTerminal(branchName)
            );
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "branch remote":
        addToTerminal(command);
        addToTerminal(terminalResponse);
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
              addToTerminal(branchName)
            );
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "branch delete":
        addToTerminal(command);
        addToTerminal(terminalResponse);
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
            // remove branch from branchData and branchTypes
            const updatedBranches = props.branchData.branches.filter(
              (branch) => branch.name !== message
            );
            props.setBranchData({
              ...props.branchData,
              branches: updatedBranches,
            });
            const updatedBranchTypes = props.branchTypes.filter(
              (b) => b.branchName !== message
            );
            props.setBranchTypes(() => updatedBranchTypes);
            addToTerminal("Deleted branch " + response[1].delete_branch_id);
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "branch local":
        addToTerminal(command);
        addToTerminal(terminalResponse);
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
              addToTerminal(branchName)
            );
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "branch create":
        addToTerminal(command);
        addToTerminal(terminalResponse);
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
            props.setBranchTypes((prev) => [
              ...prev,
              { branchName: newBranchName, branchType: "burger" },
            ]);
            props.setShowPopup(() => true);
            addToTerminal("Branch successfully created: " + newBranchName);
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "log":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitLog({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          verbose: "false",
        }).then((response) => {
          if (response[0]) {
            // success
            formatGitLog(response[1].commits!);
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "merge":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitMerge({
          session_id: props.sessionID,
          user_id: props.userID,
          current_branch_id: props.currentBranch,
          merge_branch_id: message!,
          file_map_json: makeFileMapJson(false),
        }).then((response) => {
          if (response[0]) {
            // success
            const returnedMessage: string = response[1].message!;
            addToTerminal(response[1].message!);
            if (returnedMessage !== "Already up to date") {
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
              setupWorkspace(newFrontendCommit.contents);
            }
          } else {
            if (response[1].differences_detected == "true") {
              const mergeMessage =
                response[1].error_response +
                " " +
                response[1].files_with_differences;
              addToTerminal(mergeMessage);
              addToTerminal(response[1].instructions!);
            } else {
              if (
                response[1].local_commit_id != undefined &&
                response[1].incoming_commit_id != undefined
              ) {
                setMergeLocalId(response[1].local_commit_id);
                setMergeIncomingId(response[1].incoming_commit_id);
                setMidMerge(true);
              }
              // TODO: handle file conflicts
              const conflicts: FileConflicts = JSON.parse(
                response[1].file_conflicts!
              );
              addToTerminal(response[1].error_response!);
            }
          }
        });
        break;
      case "pull":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitPull({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          file_map_json: makeFileMapJson(false),
        }).then((response) => {
          if (response[0]) {
            // success
            const returnedMessage: string = response[1].message!;
            addToTerminal(response[1].message!);
            if (returnedMessage !== "Already up to date") {
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
              setupWorkspace(newFrontendCommit.contents);
            }
          } else {
            if (
              response[1].local_commit_id != undefined &&
              response[1].incoming_commit_id != undefined
            ) {
              setMergeLocalId(response[1].local_commit_id);
              setMergeIncomingId(response[1].incoming_commit_id);
              setMidMerge(true);
            }
            // TODO: handle file conflicts
            const conflicts: FileConflicts = JSON.parse(
              response[1].file_conflicts!
            );
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      case "reset":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitReset({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          reset_commit_id: message!,
        }).then((response) => {
          if (response[0]) {
            const fileInfo: fileCommit[] = JSON.parse(
              response[1].file_map_json!
            );
            setupWorkspace(fileInfo);
            addToTerminal(response[1].message!);
            // success
          } else {
            // error
            addToTerminal(response[1].error_response!);
            if (response[1].message != undefined) {
              addToTerminal(response[1].message);
            }
          }
        });
        break;
      case "stash pop":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        // TODO: call gitStashPop
        break;
      case "stash list":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        // TODO: call gitStashList
        break;
      case "stash":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        // TODO: call gitStash
        break;
      case "stash pop index":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        // TODO: call gitStashPop with specific index
        break;
      case "status":
        addToTerminal(command);
        addToTerminal(terminalResponse);
        setCommandHistory((prev) => [...prev, command]);

        gitStatus({
          session_id: props.sessionID,
          user_id: props.userID,
          branch_id: props.currentBranch,
          file_map_json: makeFileMapJson(false),
        }).then((response) => {
          if (response[0]) {
            // success
            addToTerminal(response[1].branch_message!);
            addToTerminal(response[1].staged_changes_message!);
            if (response[1].staged_changes != undefined) {
              response[1].staged_changes.forEach((change) =>
                addToTerminal(change)
              );
            }
            addToTerminal(response[1].unstaged_changes_message!);
            if (response[1].unstaged_changes != undefined) {
              response[1].unstaged_changes.forEach((change) =>
                addToTerminal(change)
              );
            }
          } else {
            // error
            addToTerminal(response[1].error_response!);
          }
        });
        break;
      default: // error commandstrs
        addToTerminal(terminalResponse);
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
