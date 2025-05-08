import { Dispatch, SetStateAction, useState, useEffect } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";
import { burger_bottom, burger_top } from "../../../assets/images";

interface OrderProps {
  orderItems: IngredientImage[];
  setOrderItems: Dispatch<SetStateAction<IngredientImage[]>>;
}

export function Order(props: OrderProps) {
  return (
    <div className="order-container">
      <p>Order</p>
      <div className="order-ingredients">
        {props.orderItems.map((ing, index) => (
          <img
            key={ing.imgName + index}
            src={ing.imgStr}
            alt={ing.imgName}
            className="ingredient-image"
            style={{ zIndex: props.orderItems.length - index }} // top bun = highest z-index
          />
        ))}
      </div>
    </div>
  );
}
