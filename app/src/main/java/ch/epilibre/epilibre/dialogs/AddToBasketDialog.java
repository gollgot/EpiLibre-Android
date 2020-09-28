package ch.epilibre.epilibre.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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

import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class AddToBasketDialog extends AppCompatDialogFragment {

    private Product product;
    private Switch switchBtnContainer;
    private TextInputLayout etWeight;
    private TextView tvProduct;
    private TextInputLayout etQuantity;

    private AddToBasketDialogListener addToBasketDialogListener;

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
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // OnClick management override on the OnResume() method
                    }
                })
                .setPositiveButton("Ajouter au panier", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // OnClick management override on the OnResume() method
                    }
                });

        switchBtnContainer = view.findViewById(R.id.dialogAddProductSwitchContainer);
        etWeight = view.findViewById(R.id.dialogAddProductEtWeight);
        tvProduct = view.findViewById(R.id.dialogAddProductTvProduct);
        etQuantity = view.findViewById(R.id.dialogAddProductEtQuantity);

        // For product buyable by piece, do not display the switch button and weight edit text, because
        // the client will not bring a container
        if(product.getUnit().equals("pc")){
            etQuantity.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER); // Only Positive number (no fractional)
            switchBtnContainer.setVisibility(View.GONE); // Remove container switch
            etWeight.setVisibility(View.GONE); // Remove container weight
            switchBtnContainer.setChecked(false); // Client doesn't have a container
        }

        // Set the product textView
        tvProduct.setText(product.getName() + " - " + product.getPrice() + " CHF / " + product.getUnit());

        // /!\ If we have a product unit of KiloGrams -> we want to use Grams for our weight / quantity
        String quantityUnit = product.getUnit().toLowerCase().equals("kg") ? "g" : product.getUnit();

        // Set the hints
        etWeight.setHint("Poids du contenant (" + quantityUnit + ")");
        etQuantity.setHint(getString(R.string.product_dialog_quantity )+ " (" + quantityUnit + ")");

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
    public void onResume() {
        super.onResume();

        // OnClick management are here, to be able to not close the dialog if there is an error
        // concerning an input
        AlertDialog dialog = (AlertDialog)getDialog();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                if(inputsAreCorrect()){
                    addToBasketDialogListener.addToBasket(product, switchBtnContainer.isChecked(), etWeight, etQuantity);
                }
            }
        });
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
            addToBasketDialogListener = (AddToBasketDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implements CustomDialogButtonListener");
        }
    }

    /**
     * Check if all dialogs inputs are correct
     * @return True if all inputs are correct false otherwise
     */
    private boolean inputsAreCorrect(){
        boolean result = true;

        String strContainerWeight = etWeight.getEditText().getText().toString();
        String strProductWeight = etQuantity.getEditText().getText().toString();

        // Reset errors
        etWeight.setError(null);
        etQuantity.setError(null);

        // Product quantity is empty
        if(TextUtils.isEmpty(strProductWeight)){
            etQuantity.setError("Quantité requise");
            result = false;
        }

        // Client have a container
        if(switchBtnContainer.isChecked()){
            // Container weight is empty
            if(TextUtils.isEmpty(strContainerWeight)){
                etWeight.setError("Poids requis");
                result = false;
            }
            // Product quantity empty
            else if(TextUtils.isEmpty(strProductWeight)){
                etQuantity.setError("Quantité requise");
                result = false;
            }
            // Container weight not empty
            else{
                double containerWeight = Double.parseDouble(strContainerWeight);
                double productWeight = Double.parseDouble(strProductWeight);
                // Product weight must be greater than the container weight
                if(productWeight <= containerWeight){
                    etQuantity.setError("Quantité de produit incorrect");
                    result = false;
                }
            }
        }

        // All are correct -> quantity must be > 0
        if(result){
            double productWeight = Double.parseDouble(strProductWeight);
            if(productWeight == 0){
                etQuantity.setError("Quantité de produit incorrect");
                result = false;
            }
        }

        return result;
    }
}
