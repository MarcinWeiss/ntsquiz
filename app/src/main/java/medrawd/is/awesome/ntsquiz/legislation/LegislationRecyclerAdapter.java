package medrawd.is.awesome.ntsquiz.legislation;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;

import medrawd.is.awesome.ntsquiz.R;

public class LegislationRecyclerAdapter extends RecyclerView.Adapter<LegislationRecyclerAdapter.CustomViewHolder> implements SectionTitleProvider {
    private final Document document;
    private final ArrayList<String> keys;

    public LegislationRecyclerAdapter(String documentName) {
        document = Document.documents.get(documentName);
        keys = new ArrayList<>(document.getContent().keySet());
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.legislation_item, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Document paragraph = document.get(keys.get(position));
        holder.paragraph.setText(Html.fromHtml(paragraph.getParagraph()));
        holder.itemView.setTag(paragraph.getTextA());
    }

    @Override
    public int getItemCount() {
        return document.getContent().size();
    }

    @Override
    public String getSectionTitle(int position) {
        return document.get(keys.get(position)).getTextA();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView paragraph;

        public CustomViewHolder(View itemView) {
            super(itemView);
            paragraph = (TextView) itemView.findViewById(R.id.paragraph);
        }
    }
}
