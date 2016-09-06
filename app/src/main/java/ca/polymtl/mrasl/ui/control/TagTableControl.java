package ca.polymtl.mrasl.ui.control;

import static android.view.View.OnClickListener;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ca.polymtl.mrasl.R;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.tag.Tag;
import ca.polymtl.mrasl.tag.TagAddedListener;
import ca.polymtl.mrasl.tag.TagList;

/**
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class TagTableControl implements IDisposable, TagAddedListener, OnClickListener {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = TagTableControl.class.getName();
    private static final int RESIZE = 3;

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final Context fContext;
    private final Handler fHandler;
    private final TagList fList;
    private final ImageView fView;
    private final TableLayout fTable;
    private final Map<TableRow, Tag> fRows = new HashMap<>();

    private View fSelection;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public TagTableControl(Context context, TagList list, ImageView view, TableLayout table) {
        fContext = context;
        fHandler = new Handler(context.getMainLooper());
        fList = list;
        fView = view;
        fTable = table;

        /* Connect the listener to the tag list */
        fList.addListener(this);

        /* Add the tags that are already detected */
        for (Tag tag : fList.getList()) {
            new AddRow(tag).run();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method changes the tag image that is being showed.
     *
     * @param tag The tag that has the image
     */
    private void setImage(Tag tag) {
        /* Set the view aspect ratio to the same of the image */
        ViewGroup.LayoutParams params = fView.getLayoutParams();
        params.width = tag.getImage().getWidth() * RESIZE;
        params.height = tag.getImage().getHeight() * RESIZE;

        /* Put the image into the view */
        fView.setImageBitmap(tag.getImage());
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Disconnect the listener from the tag list */
        fList.removeListener(this);
    }

    @Override
    public void onNewTag(Tag tag) {
        /* Add a new row */
        fHandler.post(new AddRow(tag));
    }

    @Override
    public void onClick(View view) {
        Tag tag = fRows.get(view);

        /* Make sure that we have a tag */
        if (tag == null) {
            Log.e(TAG, "This row isn't linked to a tag");
            return;
        }

        /* Unselect the current row if possible */
        if (fSelection != null) {
            /* Reset the color f the last selected row */
            int color = ContextCompat.getColor(fContext, R.color.colorUnselected);
            fSelection.setBackgroundColor(color);
        }

        /* Change the color of the selected item */
        int color = ContextCompat.getColor(fContext, R.color.colorSelected);
        view.setBackgroundColor(color);

        /* Update the image */
        setImage(tag);

        /* Update the selected item */
        fSelection = view;
    }

    // ---------------------------------------------------------------------------------------------
    // Anonymous classes
    // ---------------------------------------------------------------------------------------------

    /**
     * This class adds a new row based on the information of a tag.
     */
    private class AddRow implements Runnable {
        private final Tag fTag;

        public AddRow(Tag tag) {
            fTag = tag;
        }

        @Override
        public void run() {
            /* Create an empty row */
            TableRow row = (TableRow) LayoutInflater.from(fContext).inflate(R.layout.row_tag, null);

            /* Get individual cells */
            TextView ID = (TextView) row.findViewById(R.id.tag_id);
            TextView latitude = (TextView) row.findViewById(R.id.tag_latitude);
            TextView longitude = (TextView) row.findViewById(R.id.tag_longitude);

            /* Put the tag information in the cells */
            ID.setText(String.valueOf(fTag.getID()));
            latitude.setText(String.valueOf((float) fTag.getLatitude()));
            longitude.setText(String.valueOf((float) fTag.getLongitude()));

            /* Add the click listener */
            row.setOnClickListener(TagTableControl.this);

            /* Add the row into the table */
            fTable.addView(row);

            /* Link the row with the tag */
            fRows.put(row, fTag);
        }
    }

}

