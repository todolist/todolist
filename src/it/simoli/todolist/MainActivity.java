package it.simoli.todolist;

import it.simoli.todolist.utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements ItemViewDialogFragment.ItemViewDialogListener {

	private static final String TAG = "MainActivity";
	private static final String FILENAME = "list.json";
	private static final int EDIT_TASK_REQUEST = 0;
	private static ArrayList<ToDoRow> todoRows = null;	
	private Context context = null;
	private MyAdapter adapter = null;
	private ListView myListView = null;
	private EditText myEditText = null;
	private Button myButton = null;
	private static ToDoRow rowToEdit = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_main);

		// Get references to UI widgets
		myListView = (ListView) findViewById(R.id.myListView);
		myEditText = (EditText) findViewById(R.id.myEditText);
		myButton   = (Button)   findViewById(R.id.myButton);

		// Create the array list of to do items
		todoRows = new ArrayList<ToDoRow>();
		
		// Create the adapter
		adapter = new MyAdapter(this, todoRows);

		// Bind the array adapter to the ListView
		myListView.setAdapter(adapter);

		// Bind the event handler to the button
		myButton.setOnClickListener(buttonListener);
	}

	public static ToDoRow getRowToEdit() {
		
		return rowToEdit;
	}
	
	public static ArrayList<ToDoRow> getTodoRows() {
		
		return todoRows;
	}
	
	private OnClickListener buttonListener = new OnClickListener() {

		public void onClick(View v) {

			Log.v(TAG, "Button clicked!");
			addTask();
		}
	};

	public boolean addTask() {

		String task = myEditText.getText().toString().trim();

		if (Util.isNullOrEmpty(task)) {

			Util.showToast(context, getResources().getString(R.string.no_empty_task_allowed));
			return false;

		} else {
			
			int index = 0;
			todoRows.add(index, new ToDoRow(task));
			adapter.notifyDataSetChanged();
			saveData();
			myEditText.setText("");
			return true;
		}
	}

	public void editTask(ToDoRow row) {

		Intent intent = new Intent(MainActivity.this, EditActivity.class);
		Bundle bundle = new Bundle();
//		bundle.put
		intent.putExtras(bundle);
		MainActivity.rowToEdit = row;
		startActivityForResult(intent, EDIT_TASK_REQUEST);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		Log.v(TAG, "onDestroy called!");
		saveData();
	}

	@Override
	protected void onPause() {

		super.onPause();
		Log.v(TAG, "onPause called!");
	}

	@Override
	protected void onResume() {

		super.onResume();
		Log.v(TAG, "onResume called!");

		// Restore data from internal storage
		resumeData();
	}

	@Override
	protected void onStop() {

		super.onStop();
		Log.v(TAG, "onStop called!");
		saveData();
	}

	public void saveData() {

		try {
			JsonUtil.writeJSON(todoRows, FILENAME, context);
			
		} catch (IOException e) {

			Log.e(TAG, "saveData() failed.");
			e.printStackTrace();
		}
	}

	public void resumeData() {
		
		try {
			JsonUtil.restoreDataFromJSON(JsonUtil.readJSON(FILENAME, context), todoRows);
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDialogEditClick(DialogFragment dialog) {

		Log.v(TAG, "onDialogEditClick");
		ToDoRow row = ((ItemViewDialogFragment) dialog).getEntity();
		// start EditActivity
		editTask(row);
	}

	@Override
	public void onDialogDeleteClick(DialogFragment dialog) {

		Log.v(TAG, "onDialogDeleteClick");
		ToDoRow row = ((ItemViewDialogFragment) dialog).getEntity();
		todoRows.remove(row);
		adapter.notifyDataSetChanged();
		saveData();
		Util.showToast(context, getResources().getString(R.string.task_deleted_successfully));
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        if (requestCode == EDIT_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {

        		adapter.notifyDataSetChanged();
        		saveData();
                // A task was updated. Here we will just display
                // a toast to the user.
        		Util.showToast(context, getResources().getString(R.string.task_updated_successfully));   
            }
        }
    }
	
}
