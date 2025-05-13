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
        <img src={logo} className="logo" />
        <div className="level-header">
          <button onClick={props.onPrev} disabled={props.isFirst}>
            &lt;
          </button>
          <h1 style={{ margin: 0 }}>
            Level {props.level} {props.completed ? "✅" : ""}
          </h1>
          <button onClick={props.onNext} disabled={props.isLast}>
            &gt;
          </button>
        </div>
      </center>
      <p>{props.instructions}</p>
      <div className="order-container" style={{ position: "relative" }}>
        {props.completed && (
          <img src={checkmark} alt="Completed" className="checkmark-img" />
        )}
        <Order
          orderItems={props.orderItems}
          setOrderItems={props.setOrderItems}
        />
      </div>
    </div>
  );
}
