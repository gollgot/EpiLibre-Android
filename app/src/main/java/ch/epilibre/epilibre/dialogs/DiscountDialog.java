package ch.epilibre.epilibre.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Models.Discount;
import ch.epilibre.epilibre.Models.DiscountLine;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.activities.MainActivity;

public class DiscountDialog extends AppCompatDialogFragment {

    private MainActivity mainActivity;
    private DiscountDialogListener discountDialogListener;
    private double totalPrice;
    private Spinner spinnerDiscount;
    ArrayList<Discount> discounts;
    ArrayList<BasketLine> basketLines;


    /**
     * Constructor for create the discount select dialog. Also define here all the available discounts
     * @param mainActivity The context
     * @param totalPrice The total price without discount
     * @param basketLines All basketLines in the basket
     */
    public DiscountDialog(MainActivity mainActivity, double totalPrice, ArrayList<BasketLine> basketLines){
        this.mainActivity = mainActivity;
        this.totalPrice = totalPrice;
        this.basketLines = basketLines;
        this.discounts = new ArrayList<>();
        discounts.add(new Discount(Utils.PHD_STUDENT_DISCOUNT_PERCENT, "Rabais doctorant"));
        discounts.add(new Discount(Utils.STUDENT_DISCOUNT_PERCENT, "Rabais étudiant"));
        discounts.add(new Discount(Utils.ASSOCIATE__DISCOUNT_PERCENT, "Rabais collaborateur"));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_discount, null);

        builder.setView(view)
                .setTitle("Rabais")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing
                    }
                })
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DiscountLine discountLine = null;

                        // Create the discount line if we choose a discount other wise it will be null
                        if(spinnerDiscount.getSelectedItemPosition() > 0){
                            Discount discountChoose = discounts.get(spinnerDiscount.getSelectedItemPosition()-1);

                            // /!\ DONT APPLY DISCOUNTS ON CAUTIONS PRODUCT !!! A CAUTION IS A CAUTION /!\
                            for(BasketLine basketLine : basketLines){
                                if(basketLine.getProduct().getCategory().toLowerCase().equals("cautions")){
                                    totalPrice -= basketLine.getProduct().getPrice();
                                }
                            }

                            double discountPrice = Utils.calculateDiscount(discountChoose, totalPrice);

                            discountLine = new DiscountLine(null, 0, discountChoose, discountPrice);
                        }

                        // Call the listener with the discountLine
                        discountDialogListener.applyDiscount(discountLine);
                    }
                });

        spinnerDiscount = view.findViewById(R.id.dialogDiscountSpinnerDiscount);

        // Fill the spinner with all discount possibilities
        ArrayList<String> spinnerValues = new ArrayList<>();
        spinnerValues.add("Aucun rabais");
        for(Discount discount : discounts){
            spinnerValues.add(discount.getInfo() + " - " + discount.getPercent() + "%");
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiscount.setAdapter(dataAdapter);

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
            discountDialogListener = (DiscountDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implements DiscountDialogListener");
        }
    }

}
