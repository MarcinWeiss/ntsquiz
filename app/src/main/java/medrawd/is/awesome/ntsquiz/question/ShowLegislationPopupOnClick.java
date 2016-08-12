package medrawd.is.awesome.ntsquiz.question;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.View;

import java.util.Arrays;

import medrawd.is.awesome.ntsquiz.legislation.Document;


public class ShowLegislationPopupOnClick implements View.OnClickListener {
    Context context;
    String[] legislationAddress;

    public ShowLegislationPopupOnClick(Context context, String[] legislationAddress) {
        this.context = context;
        this.legislationAddress = legislationAddress;
    }

    @Override
    public void onClick(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set title
        alertDialogBuilder.setTitle("Powiązany przepis");
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

        String message = builder.toString();

        // set dialog message
        alertDialogBuilder
                .setMessage(Html.fromHtml(message))
                .setCancelable(false)
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
}
