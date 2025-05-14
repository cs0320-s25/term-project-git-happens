import { Dispatch, SetStateAction } from "react";
import "../../../styles/game.css";
import { IngredientImage } from "../Game";

interface OrderProps {
  orderItems: IngredientImage[];
  setOrderItems: Dispatch<SetStateAction<IngredientImage[]>>;
}

export function Order(props: OrderProps) {
  return (
    <section className="order-container" aria-label="Order preview" role="list">
      <div className="order-section">
        {props.orderItems.map((ing, i) => (
          <img
            key={ing.imgName}
            src={ing.imgStr}
            alt={ing.imgName}
            role="listitem"
            style={{
              zIndex: props.orderItems.length - i,
              position: "relative",
            }}
          />
        ))}
      </div>
    </section>
  );
}
