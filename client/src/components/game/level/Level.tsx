import { Dispatch, SetStateAction, useState } from "react";
import "../../../styles/game.css";
import { Order } from "../order/Order";
import { IngredientImage } from "../Game";
import { logo, checkmark } from "../../../assets/images";

interface LevelProps {
  level: number;
  instructions: string;
  completed: boolean;
  orderItems: IngredientImage[];
  setOrderItems: Dispatch<SetStateAction<IngredientImage[]>>;
  onPrev: () => void;
  onNext: () => void;
  isFirst: boolean;
  isLast: boolean;
}

export function Level(props: LevelProps) {
  // State to manage the visibility of the info modal
  const [showModal, setShowModal] = useState<boolean>(false);

  // Function to close the modal
  const closeModal = () => setShowModal(false);

  return (
    <div className="instructions-order-container">
      <center>
        <img src={logo} className="logo" alt="Game logo" />
        <div className="level-header">
          <button
            onClick={props.onPrev}
            disabled={props.isFirst}
            aria-label="Go to previous level"
          >
            &lt;
          </button>
          <h1 id="level-heading" style={{ margin: 0 }} aria-live="polite">
            Level {props.level} {props.completed ? "✅" : ""}
          </h1>
          <button
            onClick={props.onNext}
            disabled={props.isLast}
            aria-label="Go to next level"
          >
            &gt;
          </button>
          {/* Info Button to open the modal */}
          <button
            onClick={() => setShowModal(true)}
            aria-label="Hotkeys Information"
            className="info-button"
          >
            ℹ️
          </button>
        </div>
      </center>
      <p className="level-instructions">{props.instructions}</p>
      <section
        className="order-container"
        style={{ position: "relative" }}
        aria-labelledby="level-heading"
      >
        {props.completed && (
          <img src={checkmark} alt="Completed" className="checkmark-img" />
        )}
        <Order
          orderItems={props.orderItems}
          setOrderItems={props.setOrderItems}
        />
      </section>

      {/* Info Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Hotkeys Information</h2>
            <ul>
              <li>
                <strong>Shift + I:</strong> Select Ingredients
              </li>
              <li>
                <strong>Space/Enter (when selecting ingredient):</strong> Add
                item
              </li>
              <li>
                <strong>Left/Right (when selecting ingredient):</strong>{" "}
                Navigate between ingredients
              </li>
              <li>
                <strong>Shift + B:</strong> Open Branch Sidebar
              </li>
              <li>
                <strong>Shift + N (when sidebar open):</strong> Select Branch
                Nodes
              </li>
              <li>
                <strong>Left/Right/Up/Down (when selecting nodes):</strong>{" "}
                Navigate between nodes
              </li>
              <li>
                <strong>Shift + T:</strong> Select Terminal
              </li>
              <li>
                <strong>Shift + 1/2/3:</strong> Select Workstation 1/2/3
              </li>
              <li>
                <strong>Space/Enter (when selecting plate ingredient):</strong>{" "}
                Select item
              </li>
              <li>
                <strong>
                  Shift + Up/Down (when plate ingredient is selected):
                </strong>{" "}
                Move ingredient up/down
              </li>
              <li>
                <strong>
                  Delete/Backspace (when plate ingredient is selected):
                </strong>{" "}
                Remove item
              </li>
            </ul>
            <button onClick={closeModal} className="close-modal-btn">
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
