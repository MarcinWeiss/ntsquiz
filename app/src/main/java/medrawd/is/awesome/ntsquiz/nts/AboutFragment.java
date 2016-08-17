package medrawd.is.awesome.ntsquiz.nts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import medrawd.is.awesome.ntsquiz.R;

public class AboutFragment extends Fragment {


    public AboutFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView aboutUsTextView = (TextView) view.findViewById(R.id.about_us_textview);
        aboutUsTextView.setText(Html.fromHtml(getString(R.string.about_us_html)));
        aboutUsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

}
