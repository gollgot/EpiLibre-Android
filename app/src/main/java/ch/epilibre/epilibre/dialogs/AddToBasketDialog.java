package ch.epilibre.epilibre.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class AddToBasketDialog extends AppCompatDialogFragment {

    private Product product;
    private Switch switchBtnContainer;
    private TextInputLayout etWeight;
    private TextView tvProduct;
    private TextInputLayout etQuantity;

    private CustomDialogButtonListener customDialogButtonListener;

    /**
     * Constructor
     * @param product The Product we want to add to the basket
     */
    public AddToBasketDialog(Product product) {
        this.product = product;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_add_to_basket, null);

        builder.setView(view)
                .setTitle("Ajouter au panier")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setPositiveButton("Ajouter au panier", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        customDialogButtonListener.addToBasket(product, switchBtnContainer.isChecked(), etWeight, etQuantity);
                    }
                });

        switchBtnContainer = view.findViewById(R.id.dialogAddProductSwitchContainer);
        etWeight = view.findViewById(R.id.dialogAddProductEtWeight);
        tvProduct = view.findViewById(R.id.dialogAddProductTvProduct);
        etQuantity = view.findViewById(R.id.dialogAddProductEtQuantity);

        // For product buyable by piece, do not display the switch button and weight edit text, because
        // the client will not bring a container
        if(product.getUnit().equals("pc")){
            switchBtnContainer.setVisibility(View.GONE);
            etWeight.setVisibility(View.GONE);
            switchBtnContainer.setChecked(false);
        }
        
        // Set the product textView
        tvProduct.setText(product.getName() + " - " + product.getPrice() + " CHF / " + product.getUnit());

        // Set the quantity hint
        etQuantity.setHint(getString(R.string.product_dialog_quantity )+ " (" + product.getUnit() + ")");

        // Toogle the switch button to display or not the weight edit text
        switchBtnContainer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    etWeight.setVisibility(View.VISIBLE);
                }else{
                    etWeight.setVisibility(View.INVISIBLE);
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        Button positiveButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);

        // Buttons inside custom dialog use default buttons theme, so we'll change them to be like
        // normal custom dialog button (no background and primary color text)
        Utils.setDefaultDialogButtonTheme(getActivity(), positiveButton);
        Utils.setDefaultDialogButtonTheme(getActivity(), negativeButton);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            customDialogButtonListener = (CustomDialogButtonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implements CustomDialogButtonListener");
        }
    }
}
