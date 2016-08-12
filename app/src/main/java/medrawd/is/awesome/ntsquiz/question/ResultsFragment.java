package medrawd.is.awesome.ntsquiz.question;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import medrawd.is.awesome.ntsquiz.R;

public class ResultsFragment extends Fragment {
    public static final String TAG = ResultsFragment.class.getSimpleName();

    private static final String ARG_INDICES = "indices";
    private static final String ARG_ANSWERS = "answers";
    private static final String ARG_QUESTIONS = "numberOfQuestions";

    private ArrayList<Integer> indices;
    private ArrayList<Integer> answers;
    private int numberOfQuestions;
    private int mCorrect;

    public ResultsFragment() {
    }

    public static ResultsFragment newInstance(ArrayList<Integer> indices, ArrayList<Integer> answers, int numberOfQuestions) {
        ResultsFragment fragment = new ResultsFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_INDICES, indices);
        args.putIntegerArrayList(ARG_ANSWERS, answers);
        args.putInt(ARG_QUESTIONS, numberOfQuestions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            indices = getArguments().getIntegerArrayList(ARG_INDICES);
            answers = getArguments().getIntegerArrayList(ARG_ANSWERS);
            numberOfQuestions = getArguments().getInt(ARG_QUESTIONS);
            Log.i(TAG, "indices " + Arrays.toString(indices.toArray()));
            Log.i(TAG, "answers " + Arrays.toString(answers.toArray()));

            mCorrect = 0;
            for (int i = 0; i < indices.size(); i++) {
                int index = indices.get(i);
                Integer answer = answers.get(i);
                Log.i(TAG, "" + index + " " + answer + " " + Question.questions.get(index).getCorrectAnswer());
                if (null != answer && Question.questions.get(index).getCorrectAnswer() == answer) {
                    mCorrect++;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        TextView results = (TextView) view.findViewById(R.id.results);
        results.setText("" + mCorrect + "/" + numberOfQuestions);

        AnsweredQuestionRecyclerAdapter adapter = new AnsweredQuestionRecyclerAdapter(indices, answers, getContext());
        RecyclerView answeredQuestionsList = (RecyclerView)view.findViewById(R.id.answers);
        answeredQuestionsList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
