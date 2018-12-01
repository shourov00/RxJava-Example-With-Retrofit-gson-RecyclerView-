package com.tutorial.shourov.rxexampleone.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.tutorial.shourov.rxexampleone.R;
import com.tutorial.shourov.rxexampleone.network.ApiClient;
import com.tutorial.shourov.rxexampleone.network.ApiService;
import com.tutorial.shourov.rxexampleone.network.model.Note;
import com.tutorial.shourov.rxexampleone.network.model.User;
import com.tutorial.shourov.rxexampleone.utils.MyDividerItemDecoration;
import com.tutorial.shourov.rxexampleone.utils.PreUtils;
import com.tutorial.shourov.rxexampleone.utils.RecyclerTouchListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerViewContainer;

    private ApiService mApiService;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private NotesAdapter mAdapter;
    private List<Note> mNoteList = new ArrayList<>();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.txt_empty_notes_view)
    TextView txtEmptyNotesView;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.fab)
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_title_home));
        setSupportActionBar(toolbar);

        fab.setOnClickListener(v -> showNoteDialog(false, null, -1));

        // white background notification bar
        whiteNotificationBar(fab);

        //getting retrofit services
        mApiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        //configure recyclerview and add adapter
        mAdapter = new NotesAdapter(mNoteList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(
                this, 16, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(
                this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //nothing happens
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionDialog(position);
            }
        }
        ));

        /**
         * Check for stored Api Key in shared preferences
         * If not present, make api call to register the user
         * This will be executed when app is installed for the first time
         * or data is cleared from settings
         * */
        if (TextUtils.isEmpty(PreUtils.getApiKey(this))) {
            registerUser();
        } else {
            fetchAllNotes();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        shimmerViewContainer.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerViewContainer.stopShimmerAnimation();
    }

    /**
     * Registering new user
     * sending unique id as device identification
     */
    private void registerUser() {
        // unique id to identify the device
        String uniqueId = UUID.randomUUID().toString();

        mCompositeDisposable.add(mApiService
                .register(uniqueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<User>() {
                    @Override
                    public void onSuccess(User user) {
                        // Storing user API Key in preferences
                        PreUtils.setApiKey(getApplicationContext(), user.getApikey());
                        Log.d(TAG, "onSuccess: Device is register succesfully" +
                                PreUtils.getApiKey(getApplicationContext()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                        showError(e);
                    }
                }));
    }

    /**
     * Fetching all notes from api
     * The received items will be in random order
     * map() operator is used to sort the items in descending order by Id
     */
    private void fetchAllNotes() {
        mCompositeDisposable.add(mApiService
                .fetchAllNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(notes -> {//sorting notes
                    Collections.sort(notes, (o1, o2) -> o2.getId() - o1.getId());
                    return notes;
                })
                .subscribeWith(new DisposableSingleObserver<List<Note>>() {
                    @Override
                    public void onSuccess(List<Note> notes) {

                        mNoteList.clear();
                        mNoteList.addAll(notes);
                        mAdapter.notifyDataSetChanged();

                        // stop animating Shimmer and hide the layout
                        shimmerViewContainer.stopShimmerAnimation();

                        toggleEmptyNotes();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                })
        );
    }

    /**
     * Creating new note
     */
    private void createNote(String note) {
        mCompositeDisposable.add(mApiService
                .createNote(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Note>() {
                    @Override
                    public void onSuccess(Note note) {
                        if (!TextUtils.isEmpty(note.getError())) {
                            //if error occures
                            Toast.makeText(MainActivity.this, note.getError(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG, "onSuccess: New note created: " +
                                note.getId() + " " +
                                note.getNote() + "" +
                                note.getTimeStamp());

                        // Add new item and notify adapter
                        mNoteList.add(0, note);
                        mAdapter.notifyItemInserted(0);

                        toggleEmptyNotes();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(e);
                    }
                })
        );
    }

    /**
     * Updating a note
     */
    private void updateNote(int noteId, String note, int position) {
        mCompositeDisposable.add(mApiService
                .updateNote(noteId, note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "Note updated");

                        Note n = mNoteList.get(position);
                        n.setNote(note);

                        // Update item and notify adapter
                        mNoteList.set(position, n);
                        mAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                        showError(e);
                    }
                })
        );

    }

    /**
     * Deleting a note
     */
    private void deleteNote(int noteId, int position) {
        mCompositeDisposable.add(mApiService
                .deleteNote(noteId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                        // Remove and notify adapter about item deletion
                        mNoteList.remove(position);
                        mAdapter.notifyItemRemoved(position);

                        Toast.makeText(MainActivity.this,
                                "Note deleted!", Toast.LENGTH_SHORT).show();

                        toggleEmptyNotes();

                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(e);
                    }
                })
        );
    }

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(boolean souldUpdate, Note note, int position) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.note_dialog, null);

        EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_note_title);
        dialogTitle.setText(!souldUpdate ? getString(R.string.lbl_new_note_title) :
                getString(R.string.lbl_edit_note_title));

        if (souldUpdate && note != null) {
            //click fab for updating
            inputNote.setText(note.getNote());
        }

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(souldUpdate ? "Update" : "Save", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Show toast message when no text is entered
            if (TextUtils.isEmpty(inputNote.getText().toString())) {
                Toast.makeText(MainActivity.this, "Enter Note!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                //if user wrote something to save
                alertDialog.dismiss();
            }

            // check if user updating note
            if (souldUpdate & note != null) {
                //update note by id
                updateNote(note.getId(), inputNote.getText().toString(), position);
            } else {
                // create new note
                createNote(inputNote.getText().toString());
            }
        });
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionDialog(int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, (dialog, which) -> {
            if (which == 0) {
                showNoteDialog(true, mNoteList.get(position), position);
            } else {
                deleteNote(mNoteList.get(position).getId(), position);
            }
        });
        builder.show();
    }

    private void toggleEmptyNotes() {
        if (mNoteList.size() > 0) {
            txtEmptyNotesView.setVisibility(View.GONE);
        } else {
            txtEmptyNotesView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Showing a Snackbar with error message
     * The error body will be in json format
     * {"error": "Error message!"}
     */
    private void showError(Throwable e) {
        String message = "";

        try {
            if (e instanceof IOException) {
                message = "No Internet Connection";
            } else if (e instanceof HttpException) {
                HttpException httpException = (HttpException) e;
                String errorBody = httpException.response().errorBody().string();
                JSONObject jsonObject = new JSONObject(errorBody);

                message = jsonObject.getString("error");

            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(message)) {
            message = "Unknown error occurred! Check LogCat.";
        }

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.GREEN);
        snackbar.show();
    }


    public void whiteNotificationBar(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = v.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            v.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    //finally handle memoryleaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mCompositeDisposable.clear();
    }
}
