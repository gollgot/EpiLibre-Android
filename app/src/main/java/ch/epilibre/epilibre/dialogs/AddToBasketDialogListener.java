package ch.epilibre.epilibre.dialogs;

import com.google.android.material.textfield.TextInputLayout;

import ch.epilibre.epilibre.Models.Product;

public interface AddToBasketDialogListener {
    /**
     * Add to basket button clicked from the AddToBasketDialog
     * Price calculation is done here
     * @param product Product we want to add to the basket
     * @param hasContainer Client has his own container or not
     * @param etWeight the weight of the container
     * @param etQuantity The product quantity the client want to buy
     */
    void addToBasket(Product product, boolean hasContainer, TextInputLayout etWeight, TextInputLayout etQuantity);
}
