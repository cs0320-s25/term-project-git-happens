import { Dispatch, SetStateAction, useState, useEffect, useRef } from "react";
import "../../../styles/game.css";
import { Order } from "../order/Order";
import { IngredientImage } from "../Game";

interface LevelProps {
  level: number;
  instructions: string;
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
      <div className="level-header">
        <button onClick={props.onPrev} disabled={props.isFirst}>
          &lt;
        </button>
        <h1 style={{ margin: 0 }}>Level {props.level}</h1>
        <button onClick={props.onNext} disabled={props.isLast}>
          &gt;
        </button>
      </div>
      <p>{props.instructions}</p>
      <Order
        orderItems={props.orderItems}
        setOrderItems={props.setOrderItems}
      />
    </div>
  );
}

export default Level;
