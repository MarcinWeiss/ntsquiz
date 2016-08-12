package medrawd.is.awesome.ntsquiz;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import medrawd.is.awesome.ntsquiz.legislation.LegislationFragment;
import medrawd.is.awesome.ntsquiz.question.Question;
import medrawd.is.awesome.ntsquiz.question.QuestionFragment;
import medrawd.is.awesome.ntsquiz.question.ResultsFragment;

public class QuizActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, QuestionFragment.QuestionFragmentInteractionListener {

    public static final int TEST_SIZE = 10;
    public static final String TAG = QuizActivity.class.getSimpleName();
    private boolean isQuiz;
    private int questionsIndex = 0;
    private List<Integer> quiz = new ArrayList<>();
    private Map<Integer, Integer> answers = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null == getSupportFragmentManager().findFragmentById(R.id.content_quiz)){
            navigateToAllQuestions();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pytania) {
            navigateToAllQuestions();
        } else if (id == R.id.nav_quiz) {
            navigateToRandomQuiz();
        } else if (id == R.id.nav_uobia) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("UoBiA")).commit();
        } else if (id == R.id.nav_kk) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("KK")).commit();
        } else if (id == R.id.nav_regulamin_strzelnic) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("Wzorcowy regulamin strzelnic")).commit();
        } else if (id == R.id.nav_rozporzadzenie_przewozenie) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("Rozporządzenie w sprawie przewożenia broni i amunicji środkami transportu publicznego")).commit();
        } else if (id == R.id.nav_rozporzadzenie_deponowanie) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("Rozporządzenie w sprawie deponowania broni")).commit();
        } else if (id == R.id.nav_rozporzadzenie_noszenie) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("Rozporządzenie w sprawie noszenia i przechowywania broni")).commit();
        } else if (id == R.id.nav_rozporzadzenie_egzamin) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, LegislationFragment.newInstance("Rozporządzenie w sprawie egzaminu")).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToRandomQuiz() {
        questionsIndex = 0;
        isQuiz = true;
        answers.clear();
        quiz.clear();
        quiz.addAll(getRandomIndices());
        for(int index : quiz){
            answers.put(index, null);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, QuestionFragment.newInstance(quiz.get(0), true, quiz.size(), 1)).commit();
    }

    private void navigateToAllQuestions() {
        questionsIndex = 0;
        answers.clear();
        isQuiz = false;
        showQuestion();
    }

    private void showQuestion() {
        if (answers.containsKey(questionsIndex)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, QuestionFragment.newInstance(questionsIndex, answers.get(questionsIndex), false, Question.questions.size(), questionsIndex + 1)).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, QuestionFragment.newInstance(questionsIndex, false, Question.questions.size(), questionsIndex + 1)).commit();
        }
    }

    private void showTestQuestion() {
        if (null != answers.get(quiz.get(questionsIndex))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, QuestionFragment.newInstance(quiz.get(questionsIndex), answers.get(quiz.get(questionsIndex)), true, quiz.size(), questionsIndex + 1)).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, QuestionFragment.newInstance(quiz.get(questionsIndex), true, quiz.size(), questionsIndex + 1)).commit();
        }
    }

    @Override
    public void onQuestionAnswered(int index, int answer) {
        Log.i(TAG, "onQuestionAnswered " + index + " " + answer);
        answers.put(index, answer);
    }

    @Override
    public void onNavigateNext() {
        if (questionsIndex < (getQuestionsSize() - 1)) {
            questionsIndex++;
            if (isQuiz) {
                showTestQuestion();
            } else {
                showQuestion();
            }
        }
    }

    private int getQuestionsSize() {
        if (isQuiz) {
            return quiz.size();
        } else {
            return Question.questions.size();
        }
    }

    @Override
    public void onNavigatePrev() {
        if (questionsIndex > 0) {
            questionsIndex--;
            if (!isQuiz) {
                showQuestion();
            } else {
                showTestQuestion();
            }
        }
    }

    @Override
    public void onEndQuiz() {
        ArrayList<Integer> indices = new ArrayList<>(answers.keySet());
        ArrayList<Integer> answers = new ArrayList<>(this.answers.values());
        Log.i(TAG, "answers " + Arrays.toString(answers.toArray()));
        Log.i(TAG, "indices " + Arrays.toString(indices.toArray()));
        getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, ResultsFragment.newInstance(indices, answers, quiz.size())).commit();
    }

    private Set<Integer> getRandomIndices() {
        Random rng = new Random();
        Set<Integer> generated = new LinkedHashSet<Integer>();
        while (generated.size() < TEST_SIZE) {
            Integer next = rng.nextInt(Question.questions.size() - 1);
            generated.add(next);
        }
        return generated;
    }
}
