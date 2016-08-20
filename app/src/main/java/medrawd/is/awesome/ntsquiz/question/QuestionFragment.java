package medrawd.is.awesome.ntsquiz.question;

import android.content.Context;
import android.content.res.ColorStateList;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

import medrawd.is.awesome.ntsquiz.R;

public class QuestionFragment extends Fragment implements GestureOverlayView.OnGesturePerformedListener {
    public static final String SELECTED_ANSWER = "selectedAnswer";
    public static final String TEST = "test";
    public static final String TAG = QuestionFragment.class.getSimpleName();
    private static final String QUESTION_INDEX = "index";
    private static final String QUESTION_DISPLAY_INDEX = "displayIndex";
    private static final String QUESTIONS_NUMBER = "number";
    private QuestionFragmentInteractionListener mListener;
    private TextView mQuestionTextView;
    private RadioGroup mAnswerRadioButtons;
    private Question mQuestion;
    private RadioButton mAradioButton;
    private RadioButton mBradioButton;
    private RadioButton mCradioButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private ImageButton mHintButton;
    private Integer mSelectedAnswer;
    private int mQuestionIndex;
    private int mQuestionsNumber;
    private boolean mIsTest;
    private TextView mQuestionNumberView;
    private int mQuestionDisplayIndex;
    private Button mEndButton;

    public QuestionFragment() {
    }

    public static QuestionFragment newInstance(int questionIndex, boolean test, int questionsNumber, int questionDisplayIndex) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(QUESTION_INDEX, questionIndex);
        args.putInt(QUESTION_DISPLAY_INDEX, questionDisplayIndex);
        args.putInt(QUESTIONS_NUMBER, questionsNumber);
        args.putBoolean(TEST, test);
        fragment.setArguments(args);
        return fragment;
    }

    public static QuestionFragment newInstance(int questionIndex, int selectedAnswer, boolean test, int questionsNumber, int questionDisplayIndex) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(QUESTION_INDEX, questionIndex);
        args.putInt(QUESTION_DISPLAY_INDEX, questionDisplayIndex);
        args.putInt(QUESTIONS_NUMBER, questionsNumber);
        args.putInt(SELECTED_ANSWER, selectedAnswer);
        args.putBoolean(TEST, test);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mQuestionIndex = getArguments().getInt(QUESTION_INDEX);
            mQuestionDisplayIndex = getArguments().getInt(QUESTION_DISPLAY_INDEX);
            mQuestionsNumber = getArguments().getInt(QUESTIONS_NUMBER);
            mQuestion = Question.questions.get(Integer.valueOf(mQuestionIndex));
            mIsTest = getArguments().getBoolean(TEST);
            if (getArguments().containsKey(SELECTED_ANSWER)) {
                mSelectedAnswer = Integer.valueOf(getArguments().getInt(SELECTED_ANSWER));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        GestureOverlayView qestureOverlay = (GestureOverlayView) view.findViewById(R.id.overlay);
        qestureOverlay.addOnGesturePerformedListener(this);

        Log.i(TAG, mQuestion.question);
        mQuestionTextView = (TextView) view.findViewById(R.id.question);
        mQuestionTextView.setText(mQuestion.getQuestion());
        mAnswerRadioButtons = (RadioGroup) view.findViewById(R.id.answers);
        mAradioButton = (RadioButton) view.findViewById(R.id.answerAradioButton);
        mBradioButton = (RadioButton) view.findViewById(R.id.answerBradioButton);
        mCradioButton = (RadioButton) view.findViewById(R.id.answerCradioButton);
        mAradioButton.setText(mQuestion.getAnswers()[0]);
        mBradioButton.setText(mQuestion.getAnswers()[1]);
        mCradioButton.setText(mQuestion.getAnswers()[2]);

        mQuestionNumberView = (TextView) view.findViewById(R.id.question_number);
        mQuestionNumberView.setText(String.valueOf(mQuestionDisplayIndex) + "/" + mQuestionsNumber);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList incorrect = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, //uchecked
                            new int[]{android.R.attr.state_checked} //checked
                    },
                    new int[]{
                            Color.BLACK //disabled
                            , Color.RED //enabled
                    }
            );
            ColorStateList correct = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, //uchecked
                            new int[]{android.R.attr.state_checked} //checked
                    },
                    new int[]{
                            Color.BLACK //disabled
                            , Color.GREEN //enabled
                    }
            );
            ColorStateList normal = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, //uchecked
                            new int[]{android.R.attr.state_checked} //checked
                    },
                    new int[]{
                            Color.BLACK //disabled
                            , Color.BLACK //enabled
                    }
            );

            if (!mIsTest) {
                switch (mQuestion.getCorrectAnswer()) {
                    case 0:
                        mAradioButton.setButtonTintList(correct);
                        mBradioButton.setButtonTintList(incorrect);
                        mCradioButton.setButtonTintList(incorrect);
                        break;
                    case 1:
                        mAradioButton.setButtonTintList(incorrect);
                        mBradioButton.setButtonTintList(correct);
                        mCradioButton.setButtonTintList(incorrect);
                        break;
                    case 2:
                        mAradioButton.setButtonTintList(incorrect);
                        mBradioButton.setButtonTintList(incorrect);
                        mCradioButton.setButtonTintList(correct);
                        break;
                }
            } else {
                mAradioButton.setButtonTintList(normal);
                mBradioButton.setButtonTintList(normal);
                mCradioButton.setButtonTintList(normal);
            }
        }

        if (null != mSelectedAnswer) {
            switch (mSelectedAnswer) {
                case 0:
                    mAnswerRadioButtons.check(R.id.answerAradioButton);
                    break;
                case 1:
                    mAnswerRadioButtons.check(R.id.answerBradioButton);
                    break;
                case 2:
                    mAnswerRadioButtons.check(R.id.answerCradioButton);
                    break;
            }
        }
        mAnswerRadioButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.answerAradioButton:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            if (!mIsTest) {
                                if (mQuestion.getCorrectAnswer() == 0) {
                                    mAradioButton.setTextColor(Color.GREEN);
                                    mBradioButton.setTextColor(Color.BLACK);
                                    mCradioButton.setTextColor(Color.BLACK);
                                } else {
                                    mAradioButton.setTextColor(Color.RED);
                                    mBradioButton.setTextColor(Color.BLACK);
                                    mCradioButton.setTextColor(Color.BLACK);
                                }
                            }
                        }
                        mListener.onQuestionAnswered(mQuestionIndex, 0);
                        break;
                    case R.id.answerBradioButton:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            if (!mIsTest) {
                                if (mQuestion.getCorrectAnswer() == 1) {
                                    mAradioButton.setTextColor(Color.BLACK);
                                    mBradioButton.setTextColor(Color.GREEN);
                                    mCradioButton.setTextColor(Color.BLACK);
                                } else {
                                    mAradioButton.setTextColor(Color.BLACK);
                                    mBradioButton.setTextColor(Color.RED);
                                    mCradioButton.setTextColor(Color.BLACK);
                                }
                            }
                        }
                        mListener.onQuestionAnswered(mQuestionIndex, 1);
                        break;
                    case R.id.answerCradioButton:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            if (!mIsTest) {
                                if (mQuestion.getCorrectAnswer() == 2) {
                                    mAradioButton.setTextColor(Color.BLACK);
                                    mBradioButton.setTextColor(Color.BLACK);
                                    mCradioButton.setTextColor(Color.GREEN);
                                } else {
                                    mAradioButton.setTextColor(Color.BLACK);
                                    mBradioButton.setTextColor(Color.BLACK);
                                    mCradioButton.setTextColor(Color.RED);
                                }
                            }
                        }
                        mListener.onQuestionAnswered(mQuestionIndex, 2);
                        break;
                }

            }
        });

        mNextButton = (ImageButton) view.findViewById(R.id.nextButton);
        if (mQuestionDisplayIndex < mQuestionsNumber) {
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onNavigateNext();
                    if (!mIsTest) {
                        validateQuestion();
                    }
                }
            });
        } else {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = (ImageButton) view.findViewById(R.id.prevButton);

        if (mQuestionDisplayIndex > 1) {
            mPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onNavigatePrev();
                }
            });
        } else {
            mPrevButton.setVisibility(View.GONE);
        }

        mEndButton = (Button) view.findViewById(R.id.end_button);

        if (mQuestionDisplayIndex == mQuestionsNumber && mIsTest) {
            mEndButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEndQuiz();
                }
            });
        } else {
            mEndButton.setVisibility(View.GONE);
        }

        mHintButton = (ImageButton) view.findViewById(R.id.helpButton);
        mHintButton.setOnClickListener(new ShowLegislationPopupOnClick(getContext(), mQuestion.getJustifications()));
        return view;
    }

    private void validateQuestion() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof QuestionFragmentInteractionListener) {
            mListener = (QuestionFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement QuestionFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
        ArrayList<GestureStroke> strokeList = gesture.getStrokes();
        // prediction = lib.recognize(gesture);
        float f[] = strokeList.get(0).points;

        if (f[0] < f[f.length - 2]) {
            mListener.onNavigatePrev();
        } else if (f[0] > f[f.length - 2]) {
            mListener.onNavigateNext();
        }
    }

    public interface QuestionFragmentInteractionListener {
        void onQuestionAnswered(int index, int answer);

        void onNavigateNext();

        void onNavigatePrev();

        void onEndQuiz();
    }
}
