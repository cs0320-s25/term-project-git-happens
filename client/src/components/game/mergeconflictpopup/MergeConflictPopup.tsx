import React, {
  Dispatch,
  SetStateAction,
  useState,
  useEffect,
  useRef,
} from "react";
import { ConflictEntry, IngredientImage, FileContents } from "../Game";
import { plate } from "../../../assets/images"; // Assuming you have this for plates

interface MergeConflictPopupProps {
  fileConflicts: {
    [key: string]: ConflictEntry;
  };
  setShowMergePopup: Dispatch<SetStateAction<boolean>>; // To close the popup
  desiredMergeContents: FileContents;
  setDesiredMergeContents: Dispatch<SetStateAction<FileContents>>;
  setMergePopupDone: Dispatch<SetStateAction<boolean>>;
}

export function MergeConflictPopup({
  fileConflicts,
  setShowMergePopup,
  desiredMergeContents,
  setDesiredMergeContents,
  setMergePopupDone,
}: MergeConflictPopupProps) {
  const popupRef = useRef<HTMLDivElement>(null);

  function normalizeFileConflicts(fileConflicts: {
    [key: string]: ConflictEntry;
  }): {
    [key: string]: ConflictEntry;
  } {
    const normalized = { ...fileConflicts };

    ["file1", "file2", "file3"].forEach((fileKey) => {
      if (!normalized[fileKey]) {
        normalized[fileKey] = { incoming: [], local: [] };
      }
    });

    return normalized;
  }

  const normalizedFileConflicts = normalizeFileConflicts(fileConflicts);

  const [selectedImage, setSelectedImage] = useState<{
    workstation: number;
    imgName: string;
  } | null>(null);

  const handleClosePopup = () => {
    setShowMergePopup(false);
    setMergePopupDone(true);
  };

  function generateIngredientName(baseName: string, fileName: string): string {
    let count = desiredMergeContents[fileName].length;
    return `${baseName}_${count + 1}`;
  }

  const handleDragStart = (
    e: React.DragEvent,
    ing: IngredientImage,
    source: "incoming" | "local" | "desired",
    fileName: string
  ) => {
    e.dataTransfer.setData("ingredient", JSON.stringify(ing));
    e.dataTransfer.setData("source", source);
    e.dataTransfer.setData("fileName", fileName);
  };

  const handleDrop = (
    e: React.DragEvent,
    target: "incoming" | "local" | "desired"
  ) => {
    const ing = JSON.parse(
      e.dataTransfer.getData("ingredient")
    ) as IngredientImage;
    const source = e.dataTransfer.getData("source") as
      | "incoming"
      | "local"
      | "desired";
    const fileName = e.dataTransfer.getData("fileName");

    if (target === "desired" && source !== "desired") {
      setDesiredMergeContents((prev) => ({
        ...prev,
        [fileName]: [
          {
            imgStr: ing.imgStr,
            imgName: generateIngredientName(ing.imgStr, fileName),
          },
          ...prev[fileName],
        ],
      }));
    } else if (source === "desired" && target !== "desired") {
      setDesiredMergeContents((prev) => ({
        ...prev,
        [fileName]: prev[fileName].filter(
          (item) => item.imgName !== ing.imgName
        ),
      }));
    }
  };

  const handleKeyDown = (
    e: React.KeyboardEvent,
    ing: IngredientImage,
    source: "incoming" | "local" | "desired",
    fileName: string
  ) => {
    if (e.key === "Enter" || e.key === " " || e.key === "Spacebar") {
      if (source === "desired") {
        // Remove the image from desired
        setDesiredMergeContents((prev) => ({
          ...prev,
          [fileName]: prev[fileName].filter(
            (item) => item.imgName !== ing.imgName
          ),
        }));
      } else {
        // Add the image to desired
        setDesiredMergeContents((prev) => ({
          ...prev,
          [fileName]: [
            {
              imgStr: ing.imgStr,
              imgName: generateIngredientName(ing.imgStr, fileName),
            },
            ...prev[fileName],
          ],
        }));
      }
    }
  };

  const trapFocus = (e: KeyboardEvent) => {
    const focusableElements = popupRef.current?.querySelectorAll(
      'a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const firstFocusableElement = focusableElements?.[0] as HTMLElement;
    const lastFocusableElement = focusableElements?.[
      focusableElements.length - 1
    ] as HTMLElement;

    if (e.key === "Tab") {
      if (e.shiftKey) {
        // If Shift + Tab is pressed, focus the previous element
        if (document.activeElement === firstFocusableElement) {
          lastFocusableElement?.focus();
          e.preventDefault();
        }
      } else {
        // If Tab is pressed, focus the next element
        if (document.activeElement === lastFocusableElement) {
          firstFocusableElement?.focus();
          e.preventDefault();
        }
      }
    }
  };

  useEffect(() => {
    // Disable focus on elements outside the popup
    document.body.style.overflow = "hidden";
    document
      .querySelectorAll("body > *:not(.merge-conflict-popup)")
      .forEach((el) => {
        (el as HTMLElement).setAttribute("aria-hidden", "true");
      });

    // Trap focus within the popup
    window.addEventListener("keydown", trapFocus);

    // Cleanup on component unmount
    return () => {
      document.body.style.overflow = "auto";
      document
        .querySelectorAll("body > *:not(.merge-conflict-popup)")
        .forEach((el) => {
          (el as HTMLElement).removeAttribute("aria-hidden");
        });
      window.removeEventListener("keydown", trapFocus);
    };
  }, []);

  return (
    <div className="merge-conflict-popup" ref={popupRef}>
      <div className="conflict-container">
        <div className="popup-header">
          <h2>Resolve Merge Conflict</h2>
          <button onClick={handleClosePopup} className="close-popup-btn">
            Accept Desired Files
          </button>
        </div>
        <div className="local-incoming-container">
          {["file1", "file2", "file3"].map((fileName) => {
            const conflict = normalizedFileConflicts[fileName];
            return (
              <div key={fileName} className="file-conflict-section">
                <div className="conflict-section">
                  <h3>Incoming ({fileName})</h3>
                  <div
                    className="conflict-items"
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={(e) => handleDrop(e, "incoming")}
                  >
                    {conflict.incoming.map((ing, i) => (
                      <img
                        key={ing.imgName}
                        src={ing.imgStr}
                        alt={ing.imgName}
                        className={`ingredient-img ${
                          selectedImage?.imgName === ing.imgName
                            ? "selected"
                            : ""
                        }`}
                        draggable
                        tabIndex={0}
                        onDragStart={(e) =>
                          handleDragStart(e, ing, "incoming", fileName)
                        }
                        onKeyDown={(e) =>
                          handleKeyDown(e, ing, "incoming", fileName)
                        }
                        style={{
                          zIndex: conflict.incoming.length - i,
                          position: "relative",
                          cursor: "pointer",
                        }}
                      />
                    ))}
                  </div>
                  <img src={plate} className="workstation-plate" alt="Plate" />
                </div>
                <div className="conflict-section">
                  <h3>Local ({fileName})</h3>
                  <div
                    className="conflict-items"
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={(e) => handleDrop(e, "local")}
                  >
                    {conflict.local.map((ing, i) => (
                      <img
                        key={ing.imgName}
                        src={ing.imgStr}
                        alt={ing.imgName}
                        className={`ingredient-img ${
                          selectedImage?.imgName === ing.imgName
                            ? "selected"
                            : ""
                        }`}
                        draggable
                        tabIndex={0}
                        onDragStart={(e) =>
                          handleDragStart(e, ing, "local", fileName)
                        }
                        onKeyDown={(e) =>
                          handleKeyDown(e, ing, "local", fileName)
                        }
                        style={{
                          zIndex: conflict.local.length - i,
                          position: "relative",
                          cursor: "pointer",
                        }}
                      />
                    ))}
                  </div>
                  <img src={plate} className="workstation-plate" alt="Plate" />
                </div>
                <div className="conflict-section">
                  <h3>Desired ({fileName})</h3>
                  <div
                    className="conflict-items"
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={(e) => handleDrop(e, "desired")}
                  >
                    {desiredMergeContents[fileName].map((ing, i) => (
                      <img
                        key={ing.imgName}
                        src={ing.imgStr}
                        alt={ing.imgName}
                        className={`ingredient-img ${
                          selectedImage?.imgName === ing.imgName
                            ? "selected"
                            : ""
                        }`}
                        draggable
                        tabIndex={0}
                        onDragStart={(e) =>
                          handleDragStart(e, ing, "desired", fileName)
                        }
                        onKeyDown={(e) =>
                          handleKeyDown(e, ing, "desired", fileName)
                        }
                        style={{
                          zIndex: desiredMergeContents[fileName].length - i,
                          position: "relative",
                          cursor: "pointer",
                        }}
                      />
                    ))}
                  </div>
                  <img src={plate} className="workstation-plate" alt="Plate" />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
