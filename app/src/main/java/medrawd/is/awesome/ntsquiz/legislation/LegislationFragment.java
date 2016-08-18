package medrawd.is.awesome.ntsquiz.legislation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.RecyclerViewScrollListener;
import com.futuremind.recyclerviewfastscroll.viewprovider.ScrollerViewProvider;
import com.futuremind.recyclerviewfastscroll.viewprovider.ViewBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import medrawd.is.awesome.ntsquiz.R;

public class LegislationFragment extends Fragment {
    private static final String ARG_NAME = "name";
    private static final String TAG = LegislationFragment.class.getSimpleName();

    private String name;
    private RecyclerView recyclerView;
    private FastScroller fastScroller;

    public LegislationFragment() {
        // Required empty public constructor
    }

    public static LegislationFragment newInstance(String name) {
        LegislationFragment fragment = new LegislationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_legislation, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.listView);
        recyclerView.setAdapter(new LegislationRecyclerAdapter(name));

        fastScroller = (FastScroller) view.findViewById(R.id.fastscroll);
        fastScroller.setRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
