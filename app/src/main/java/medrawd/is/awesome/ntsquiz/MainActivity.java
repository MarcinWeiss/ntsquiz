package medrawd.is.awesome.ntsquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import medrawd.is.awesome.ntsquiz.legislation.LegislationFragment;
import medrawd.is.awesome.ntsquiz.nts.AboutFragment;
import medrawd.is.awesome.ntsquiz.question.Question;
import medrawd.is.awesome.ntsquiz.question.QuestionFragment;
import medrawd.is.awesome.ntsquiz.question.ResultsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, QuestionFragment.QuestionFragmentInteractionListener {

    public static final String TYPE_ANY = "*/*";
    public static final int TEST_SIZE = 10;

    ;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String ALL_QUESTIONS_PREFS_NAME = "allquestionsprefs";
    public static final String START_INDEX_PREF_NAME = "startIndex";
    private boolean isQuiz;
    private int questionsIndex = 0;
    private List<Integer> selectedIndices = new ArrayList<>();
    private Map<Integer, Integer> answers = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        if (shouldOpenMenuOnStart()) {
            drawer.openDrawer(Gravity.LEFT);
        }

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_questions);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.exit_popup_title)
                    .setMessage(R.string.exit_popup_message)
                    .setPositiveButton(R.string.exit_popup_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(R.string.exit_popup_negative, null)
                    .show();
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == getSupportFragmentManager().findFragmentById(R.id.content_quiz)) {
            navigateToAllQuestions();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_questions) {
            navigateToAllQuestions();
        } else if (id == R.id.nav_random_questions) {
            navigateToRandomQuestions();
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
        } else if (id == R.id.nav_about_us) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, new AboutFragment()).commit();
        } else if (id == R.id.nav_join) {
            String url = getString(R.string.join_url);
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
        } else if (id == R.id.nav_contact) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(TYPE_ANY);
            intent.putExtra(Intent.EXTRA_EMAIL, Arrays.asList(getString(R.string.contact_email)).toArray());
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToRandomQuiz() {
        questionsIndex = 0;
        isQuiz = true;
        answers.clear();
        selectedIndices.clear();
        selectedIndices.addAll(getRandomIndices());
        for (int index : selectedIndices) {
            answers.put(index, null);
        }
        showQuestion();
    }

    private void navigateToAllQuestions() {
        questionsIndex = getAllQuestionsStartIndex();
        answers.clear();
        selectedIndices.clear();
        isQuiz = false;
        showQuestion();
    }

    private int getAllQuestionsStartIndex() {
        SharedPreferences prefs = getSharedPreferences(ALL_QUESTIONS_PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(START_INDEX_PREF_NAME, 0);
    }

    private void saveAllQuestionsStartIndex(int index) {
        SharedPreferences prefs = getSharedPreferences(ALL_QUESTIONS_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(START_INDEX_PREF_NAME, index).commit();
    }

    private void navigateToRandomQuestions() {
        questionsIndex = 0;
        isQuiz = false;
        answers.clear();
        selectedIndices.clear();
        selectedIndices.addAll(getShuffledIndices());
        for (int index : selectedIndices) {
            answers.put(index, null);
        }
        showQuestion();
    }

    private void showQuestion() {
        showQuestion(null);
    }

    private void showQuestion(Direction direction) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        addChangeAnimation(direction, fragmentTransaction);
        if (!indicesAreSelected()) {
            saveAllQuestionsStartIndex(questionsIndex);
            if (answers.containsKey(questionsIndex)) {
                fragmentTransaction.replace(R.id.content_quiz, QuestionFragment.newInstance(questionsIndex, answers.get(questionsIndex), false, Question.questions.size(), questionsIndex + 1));
            } else {
                fragmentTransaction.replace(R.id.content_quiz, QuestionFragment.newInstance(questionsIndex, false, Question.questions.size(), questionsIndex + 1));
            }
        } else {
            if (null != answers.get(selectedIndices.get(questionsIndex))) {
                fragmentTransaction.replace(R.id.content_quiz, QuestionFragment.newInstance(selectedIndices.get(questionsIndex), answers.get(selectedIndices.get(questionsIndex)), isQuiz, selectedIndices.size(), questionsIndex + 1));
            } else {
                fragmentTransaction.replace(R.id.content_quiz, QuestionFragment.newInstance(selectedIndices.get(questionsIndex), isQuiz, selectedIndices.size(), questionsIndex + 1));
            }
        }
        fragmentTransaction.commit();
    }

    private void addChangeAnimation(Direction direction, FragmentTransaction fragmentTransaction) {
        if (null != direction) {
            switch (direction) {
                case LEFT:
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case RIGHT:
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
            }
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
            showQuestion(Direction.RIGHT);
        }
    }

    private int getQuestionsSize() {
        if (indicesAreSelected()) {
            return selectedIndices.size();
        } else {
            return Question.questions.size();
        }
    }

    private boolean indicesAreSelected() {
        return null != selectedIndices && selectedIndices.size() > 0;
    }

    @Override
    public void onNavigatePrev() {
        if (questionsIndex > 0) {
            questionsIndex--;
            showQuestion(Direction.LEFT);
        }
    }

    @Override
    public void onEndQuiz() {
        ArrayList<Integer> indices = new ArrayList<>(answers.keySet());
        ArrayList<Integer> answers = new ArrayList<>(this.answers.values());
        Log.i(TAG, "answers " + Arrays.toString(answers.toArray()));
        Log.i(TAG, "indices " + Arrays.toString(indices.toArray()));
        getSupportFragmentManager().beginTransaction().replace(R.id.content_quiz, ResultsFragment.newInstance(indices, answers, selectedIndices.size())).commit();
    }

    private Set<Integer> getShuffledIndices() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < Question.questions.size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        return new LinkedHashSet<>(indices);
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

    private boolean shouldOpenMenuOnStart(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("start_with_menu_open", true);
    }

    private enum Direction {LEFT, RIGHT}
}
