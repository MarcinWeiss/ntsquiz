package medrawd.is.awesome.ntsquiz.question;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import medrawd.is.awesome.ntsquiz.legislation.Document;


public class ShowLegislationPopupOnClick implements View.OnClickListener {
    Context context;
    List<String[]> legislationAddresses;

    public ShowLegislationPopupOnClick(Context context, List<String[]> legislationAddresses) {
        this.context = context;
        this.legislationAddresses = legislationAddresses;
    }

    @Override
    public void onClick(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        String message = "";
        if (legislationAddresses.size() == 1) {
            // set title
            alertDialogBuilder.setTitle("Powiązany przepis");
            String[] legislationAddress = legislationAddresses.get(0);
            message = prepareLegislationMessage(legislationAddress);
        } else if (legislationAddresses.size() > 1) {
            alertDialogBuilder.setTitle("Powiązane przepisu");
            StringBuilder messageBuilder = new StringBuilder();
            for (String[] legislationAddress : legislationAddresses) {
                messageBuilder.append(prepareLegislationMessage(legislationAddress));
                messageBuilder.append("<br>");
            }
            message = messageBuilder.toString();
        }


        // set dialog message
        alertDialogBuilder
                .setMessage(Html.fromHtml(message))
                .setCancelable(true)
                .setPositiveButton("zamknij", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private String prepareLegislationMessage(String[] legislationAddress) {
        String[] justificationAddress = Arrays.copyOfRange(legislationAddress, 1, legislationAddress.length);

        StringBuilder builder = new StringBuilder();
        builder.append("<i>");
        for (String value : legislationAddress) {
            builder.append(value);
            builder.append(", ");
        }
        builder.append("</i>");
        builder.delete(builder.length() - 2, builder.length())
                .append("<br>")
                .append(Document.documents.get(legislationAddress[0]).getParagraph(justificationAddress));

        return builder.toString();
    }
}
