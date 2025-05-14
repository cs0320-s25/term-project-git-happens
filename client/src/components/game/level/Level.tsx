import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
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
    </div>
  );
}
