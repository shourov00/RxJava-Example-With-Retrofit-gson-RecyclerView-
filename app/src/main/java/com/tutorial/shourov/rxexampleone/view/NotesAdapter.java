package com.tutorial.shourov.rxexampleone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tutorial.shourov.rxexampleone.R;
import com.tutorial.shourov.rxexampleone.network.model.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shourov on 01,December,2018
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

    private List<Note> mNoteList;
    private Context mContext;

    public NotesAdapter(List<Note> noteList, Context context) {
        mNoteList = noteList;
        mContext = context;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_note_list, viewGroup, false);

        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder noteHolder, int position) {
        Note note = mNoteList.get(position);

        noteHolder.note.setText(note.getNote());

        //displaying dot color html font
        noteHolder.dot.setText(Html.fromHtml("&#8226;"));

        //generates colors for dot
        noteHolder.dot.setTextColor(generateDotColor("400"));

        // Formatting and displaying timestamp
        noteHolder.timestamp.setText(formatDate(note.getTimeStamp()));
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private String formatDate(String timeStamp) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(timeStamp);
            SimpleDateFormat newDate = new SimpleDateFormat("MMM d,");
            return newDate.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private int generateDotColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = mContext.getResources().getIdentifier("mdcolor_" + typeColor, "array", 
                mContext.getPackageName());
        if(arrayId != 0){
            TypedArray colors = mContext.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index,Color.GRAY);
            colors.recycle();
        }

        return returnColor;
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public class NoteHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dot)
        TextView dot;

        @BindView(R.id.timestamp)
        TextView timestamp;

        @BindView(R.id.note)
        TextView note;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
