package medrawd.is.awesome.ntsquiz.question;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import medrawd.is.awesome.ntsquiz.R;

public class AnsweredQuestionRecyclerAdapter extends RecyclerView.Adapter<AnsweredQuestionRecyclerAdapter.CustomViewHolder> {

    private static final String TAG = AnsweredQuestionRecyclerAdapter.class.getSimpleName();
    private final List<Question> mQuestions;
    private final List<Integer> mAnswers;
    private final Context mContext;

    public AnsweredQuestionRecyclerAdapter(List<Integer> indices, List<Integer> answers, Context mContext) {
        this.mContext = mContext;
        mQuestions = new ArrayList<>();
        for (int index : indices) {
            mQuestions.add(Question.questions.get(index));
        }
        mAnswers = answers;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.answered_question_layout, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Question question = mQuestions.get(position);
        Integer answer = mAnswers.get(position);

        holder.questionIndex.setText(String.valueOf(position + 1));
        holder.question.setText(question.getQuestion());
        if (null == answer) {
            holder.selectedAnswer.setText("BRAK ODPOWIEDZI");
        } else if (answer != question.getCorrectAnswer()) {
            Log.i(TAG, String.format("index %d", position));
            Log.i(TAG, String.format("answer %d", answer));
            Log.i(TAG, String.format("answers %s", Arrays.toString(question.getAnswers())));
            holder.selectedAnswer.setText(question.getAnswers()[answer]);
        } else {
            holder.selectedAnswerHolder.setVisibility(View.GONE);
        }
        holder.correctAnswer.setText(question.getAnswers()[question.getCorrectAnswer()]);

        holder.hintButton.setOnClickListener(new ShowLegislationPopupOnClick(mContext, question.getJustification()));
    }

    @Override
    public int getItemCount() {
        return mQuestions.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton hintButton;
        public TextView question;
        public TextView selectedAnswer;
        public TextView correctAnswer;
        public RelativeLayout selectedAnswerHolder;
        public TextView questionIndex;

        public CustomViewHolder(View itemView) {
            super(itemView);
            question = (TextView) itemView.findViewById(R.id.question);
            selectedAnswer = (TextView) itemView.findViewById(R.id.selected);
            correctAnswer = (TextView) itemView.findViewById(R.id.correct);
            selectedAnswerHolder = (RelativeLayout) itemView.findViewById(R.id.selected_holder);
            hintButton = (ImageButton) itemView.findViewById(R.id.helpButton);
            questionIndex = (TextView) itemView.findViewById(R.id.question_index);
        }
    }
}
