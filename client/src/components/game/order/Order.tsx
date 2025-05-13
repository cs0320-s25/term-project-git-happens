import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";

interface OrderProps {
  orderItems: IngredientImage[];
  setOrderItems: Dispatch<SetStateAction<IngredientImage[]>>;
}

export function Order(props: OrderProps) {
  return (
    <div className="order-container">
      <div className="order-section">
        {props.orderItems.map((ing, i) => (
          <img
            key={ing.imgName}
            src={ing.imgStr}
            style={{
              zIndex: props.orderItems.length - i,
              position: "relative",
            }}
          />
        ))}
      </div>
    </div>
  );
}
