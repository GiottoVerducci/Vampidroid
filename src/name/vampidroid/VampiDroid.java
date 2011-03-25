package name.vampidroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;

public class VampiDroid extends TabActivity {
	
	
	public static String DECK_NAME = "deck_name";
	public static String CARD_ID = "card_id";

	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST+1, Menu.NONE, "Search")
						.setIcon(android.R.drawable.ic_search_category_default);
		menu.add(Menu.NONE, Menu.FIRST+2, Menu.NONE, "About")
						.setIcon(android.R.drawable.ic_menu_info_details);

		return(super.onCreateOptionsMenu(menu));
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case Menu.FIRST+1:
				onSearchRequested(); 
				return(true);
			
			case Menu.FIRST+2:
				
				Intent aboutIntent = new Intent(this, About.class );
				
				startActivity(aboutIntent);
				
		}

		return(super.onOptionsItemSelected(item));
	}
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        createDatabaseFile();
        
        
        Intent intent = getIntent();
        
        
        String queryCryptDatabase;
        
        
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String query = formatQueryString(intent.getStringExtra(SearchManager.QUERY));
			queryCryptDatabase = "select _id, Name from crypt where Name like '%" + query + "%'";
		}
        else
        	queryCryptDatabase = "select _id, Name from crypt";
        
        
        // Fill crypt list.
        
        ListView listCrypt = (ListView) findViewById(R.id.ListViewCrypt);
        
        SQLiteDatabase db = getDatabase();
        Cursor c = db.rawQuery(queryCryptDatabase, null);
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.listitem, 
        		c, new String[] { "Name" }, new int[] {android.R.id.text1});
        
        
        listCrypt.setAdapter(adapter);
        
        
        
        listCrypt.setOnItemClickListener(new OnItemClickListener() {

        	public void onItemClick(AdapterView<?> parent, 
		            View v, int position, long id) {
				
				// TODO Auto-generated method stub
        		/*SQLiteDatabase db = getDatabase();
				Cursor c = db.rawQuery("select CardText from crypt where _id = " + String.valueOf(id), null );
				c.moveToFirst();
				String cardText = c.getString(0);
				c.close();
				db.close();
				*/
								
				/*AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				
				builder.setMessage(cardText);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(  DialogInterface dialog, int id) {
			                dialog.dismiss();
			           }
			       });
        
        
				AlertDialog alertDialog = builder.create(); 
				alertDialog.show();
				
				*/
        		
        		
				
				Intent cryptCardIntent = new Intent(v.getContext(), CryptDetails.class );
				
				cryptCardIntent.putExtra(DECK_NAME, "crypt");
				cryptCardIntent.putExtra(CARD_ID, id);
				
				startActivity(cryptCardIntent);
				
				
				
			}
        	
        	
		});
			
			
        
        
        //listCrypt.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, COUNTRIES));
        
        
        // Fill crypt list.
        
        String queryLibraryDatabase;
        
        
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String query = formatQueryString(intent.getStringExtra(SearchManager.QUERY));
			queryLibraryDatabase = "select _id, Name from library where Name like '%" + query + "%'";
		}
        else
        	queryLibraryDatabase = "select _id, Name from library";
        
        
        ListView listLibrary = (ListView) findViewById(R.id.ListViewLibrary);
        
        
        c = db.rawQuery(queryLibraryDatabase, null);
        
        adapter = new SimpleCursorAdapter(this, R.layout.listitem, 
        		c, new String[] { "Name" }, new int[] {android.R.id.text1});
        
        
        listLibrary.setAdapter(adapter);
        
        db.close();
        
        
        listLibrary.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, 
		            View v, int position, long id) {
				
				// TODO Auto-generated method stub
				/*SQLiteDatabase db = getDatabase();
				Cursor c = db.rawQuery("select CardText from library where _id = " + String.valueOf(id), null );
				c.moveToFirst();
				String cardText = c.getString(0);
				c.close();
				db.close();
				*/
				
				/*AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				
				builder.setMessage(cardText);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(  DialogInterface dialog, int id) {
			                dialog.dismiss();
			           }
			       });
        
        
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
				
				 */
				
				Intent libraryCardIntent = new Intent(v.getContext(), LibraryDetails.class );
				
				libraryCardIntent.putExtra(DECK_NAME, "library");
				libraryCardIntent.putExtra(CARD_ID, id);
				
				startActivity(libraryCardIntent);
								
			}
        	
        	
		});
        
        TabHost mTabHost = getTabHost();
        
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_crypt").setIndicator("Crypt").setContent(R.id.ListViewCrypt));
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_library").setIndicator("Library").setContent(R.id.ListViewLibrary));
        
        //Show only the tab which has values or the first tab if there is no data at all.
        
        // Default tab is the first one.
        mTabHost.setCurrentTab(0);
        
        if (listCrypt.getAdapter().isEmpty() && !listLibrary.getAdapter().isEmpty() )
        	mTabHost.setCurrentTab(1);
        	
    }


	private void createDatabaseFile() {
		
				
		File databasefile = new File("/sdcard/VampiDroid.db");
		
		if (databasefile.exists())
		{
			databasefile = null;
			return;
		}
			
				
		
	}


	private String formatQueryString(String stringExtra) {
		// TODO Auto-generated method stub
		return stringExtra.trim().replace("'", "''");
	}


	static SQLiteDatabase getDatabase() {
		return SQLiteDatabase.openDatabase("/sdcard/VampiDroid.db", null, SQLiteDatabase.OPEN_READONLY);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}
}