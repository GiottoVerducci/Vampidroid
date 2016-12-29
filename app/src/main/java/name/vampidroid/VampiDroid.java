package name.vampidroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

import name.vampidroid.fragments.SettingsFragment;
import name.vampidroid.ui.widget.CardFilters;
import name.vampidroid.ui.widget.PersistentSearchBar;
import name.vampidroid.utils.FilterStateQueryConverter;


public class VampiDroid extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "MainActivity";

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private PersistentSearchBar persistentSearchBar;
    private Toolbar toolbar;
    DrawerLayout drawerLayout;

    FilterState filterState = new FilterState();

    boolean restoring = false;
    CardFilters cardFilters;
    private ViewPagerAdapter viewPagerAdapter;

    CursorRecyclerAdapter cryptCardsAdapter;
    CursorRecyclerAdapter libraryCardsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate... ");


        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);


        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
                appbar.setExpanded(true);

                persistentSearchBar.editSearchBarText();

            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        persistentSearchBar = new PersistentSearchBar(this);

        setupPersistentSearchBar();


        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setCustomView(persistentSearchBar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

//		toolbar.setContentInsetsAbsolute(0, 0);

        // Reference: http://stackoverflow.com/questions/26433409/android-lollipop-appcompat-actionbar-custom-view-doesnt-take-up-whole-screen-w
        ViewGroup.LayoutParams lp = persistentSearchBar.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        persistentSearchBar.setLayoutParams(lp);


        // TODO: 11/06/16 Check how to make those initializations off the main thread.
        setupSearchFilterNavigation();

        if (savedInstanceState != null) {
            filterState = savedInstanceState.getParcelable("filtermodel");
        } else {
            filterState = new FilterState();
        }

        filterCryptCards();
        filterLibraryCards();
    }


    private void setupSearchFilterNavigation() {

        cardFilters = (CardFilters) findViewById(R.id.cardFilters);

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if (tab.getPosition() == 0) {
					cardFilters.showCryptFilters();
				} else {
					cardFilters.showLibraryFilters();
				}

                updateSearchSettingsButtonState();
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});


        cardFilters.setOnCardFiltersChangeListener(new CardFilters.OnCardFiltersChangeListener() {
            @Override
            public void onGroupsChanged(int group, boolean isChecked) {
                if (!restoring) {
                    filterState.setGroup(group, isChecked);
                    updateSearchSettingsButtonState();
                    filterCryptCards();
                }
            }

            @Override
            public void onCryptDisciplineChanged(String discipline, boolean isBasic, boolean isChecked) {
                if (!restoring) {
                    filterState.setDiscipline(discipline, isBasic, isChecked);
                    updateSearchSettingsButtonState();
                    filterCryptCards();
                }

            }

            @Override
            public void onClansChanged(String clan, boolean isChecked) {
                if (!restoring) {
                    filterState.setClan(clan, isChecked);
                    updateSearchSettingsButtonState();
                    filterCryptCards();
                }
            }

            @Override
            public void onCardTypeChanged(String cardType, boolean isChecked) {
                if (!restoring) {
                    filterState.setCardType(cardType, isChecked);
                    updateSearchSettingsButtonState();
                    filterLibraryCards();
                }
            }

            @Override
            public void onLibraryDisciplineChanged(String discipline, boolean isChecked) {
                if (!restoring) {
                    filterState.setLibraryDiscipline(discipline, isChecked);
                    updateSearchSettingsButtonState();
                    filterLibraryCards();
                }
            }

            @Override
            public void onCapacitiesChanged(int minCapacity, int maxCapacity) {
                if (!restoring) {
                    filterState.setCapacityMin(minCapacity);
                    filterState.setCapacityMax(maxCapacity);
                    updateSearchSettingsButtonState();
                    filterCryptCards();
                }
            }

            @Override
            public void onReset() {
                filterState.reset();
                updateSearchSettingsButtonState();
                filterCryptCards();
                filterLibraryCards();
            }


        });

    }

    void updateSearchSettingsButtonState() {

        boolean haveChanges = false;

        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                haveChanges = cardFilters.getNumberOfCryptFiltersApplied() > 0;
                break;
            case 1:
                haveChanges = cardFilters.getNumberOfLibraryFiltersApplied() > 0;
                break;
        }
        persistentSearchBar.updateChangesIndicator(haveChanges, ContextCompat.getColor(VampiDroid.this, R.color.colorAccent));

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        Log.d(TAG, "onRestoreInstanceState() called with: " + "savedInstanceState =");

        restoring = true;

        super.onRestoreInstanceState(savedInstanceState);

//        filterState = savedInstanceState.getParcelable("filtermodel");

        restoring = false;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() called with: " + "outState = ");

        super.onSaveInstanceState(outState);


        outState.putParcelable("filtermodel", filterState);


    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ");

        // Update possible changes to preferences.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean prefSearchCardText = sharedPref.getBoolean(SettingsFragment.KEY_PREF_SEARCH_CARD_TEXT, false);

        persistentSearchBar.setSearchBarTextHint(prefSearchCardText ? R.string.search_bar_filter_card_name_and_card_text : R.string.search_bar_filter_card_name);
        if (prefSearchCardText != filterState.searchInsideCardText) {
            filterState.setSearchInsideCardText(prefSearchCardText);
            filterCryptCards();
            filterLibraryCards();
        }


        // Sync navigation drawer selected item.
        // Reference: http://stackoverflow.com/questions/34502848/how-to-change-selected-item-in-the-navigation-drawer-depending-on-the-activity-v?rq=1

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_cards);

        updateSearchSettingsButtonState();

    }

    private void setupPersistentSearchBar() {

        final Handler cardNameUpdateFiltersHandler = new Handler();

        final Runnable cardNameUpdateFilters = new Runnable() {
            @Override
            public void run() {

                filterCryptCards();
                filterLibraryCards();

            }
        };

        persistentSearchBar.addSearchBarTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!restoring) {

                    filterState.setName(s);

                    // This delay gives time to the user to finish typing before filtering.
                    // So we don't need to filter after every letter.
                    cardNameUpdateFiltersHandler.removeCallbacks(cardNameUpdateFilters);
                    cardNameUpdateFiltersHandler.postDelayed(cardNameUpdateFilters, 250);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        persistentSearchBar.setSearchSettingsClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                SearchSettingsFragment searchSettingsFragment = SearchSettingsFragment.newInstance();
//                searchSettingsFragment.show(getSupportFragmentManager(), "search_settings_fragment");

                // Open right navigation view.
                drawerLayout.openDrawer(GravityCompat.END);


            }
        });

        persistentSearchBar.setHamburgerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }


    void filterCryptCards() {

        Log.d(TAG, "filterCryptCards() called");
        cryptCardsAdapter.getFilter().filter(FilterStateQueryConverter.getCryptFilter(filterState));
    }

    void filterLibraryCards() {

        Log.d(TAG, "filterLibraryCards() called");
        libraryCardsAdapter.getFilter().filter(FilterStateQueryConverter.getLibraryFilter(filterState));

    }


    private void setupViewPager(ViewPager viewPager) {

        cryptCardsAdapter = setupCardsAdapter(0);
        libraryCardsAdapter = setupCardsAdapter(1);

        viewPagerAdapter = new ViewPagerAdapter(this, cryptCardsAdapter, libraryCardsAdapter);
        viewPager.setAdapter(viewPagerAdapter);

    }


    private CursorRecyclerAdapter setupCardsAdapter(int cardType) {

        CursorRecyclerAdapter cardsAdapter;
        final String initialQuery;

        if (cardType == 0) {
            cardsAdapter = new CryptCardsListViewAdapter(this, null);
            initialQuery = DatabaseHelper.ALL_FROM_CRYPT_QUERY;
        } else {
            cardsAdapter = new LibraryCardsListViewAdapter(this, null);
            initialQuery = DatabaseHelper.ALL_FROM_LIBRARY_QUERY;

        }


        cardsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            private static final String TAG = "CardsListFragment";

            @Override
            public Cursor runQuery(CharSequence constraint) {
                Log.d(TAG, "runQuery: Thread Id: " + Thread.currentThread().getId());
                return DatabaseHelper.getDatabase().rawQuery(initialQuery + constraint, null);

            }

        });


//        cardsAdapter.getFilter().filter("");

        return cardsAdapter;
    }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (persistentSearchBar.isSearchBarTextFocused()) {
            persistentSearchBar.clearSearchBarTextFocus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG, "onOptionsItemSelected: ");

        if (id == android.R.id.home) {

            drawerLayout.openDrawer(GravityCompat.START);

            return true;

        } else if (id == R.id.action_settings) {  //noinspection SimplifiableIfStatement
            return true;

        } else if (id == R.id.action_search) {

        }


        return super.onOptionsItemSelected(item);
    }

    // Reference: http://stackoverflow.com/questions/26835209/appcompat-v7-toolbar-up-back-arrow-not-working


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        boolean closeDrawer = true;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cards) {
            // Handle the camera action
        } else if (id == R.id.nav_settings) {
            Intent launch = new Intent(this, SettingsActivity.class);
            startActivity(launch);
            // Don't close the drawer when selecting settings.
            // It was causing stuttering when starting the Settings activity.
            closeDrawer = false;
        } else if (id == R.id.nav_about) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View dialogView = getLayoutInflater().inflate(R.layout.about, null);

            builder.setTitle("About")
                    .setView(dialogView)
                    .setPositiveButton("Close", null);

            builder.show();

        }


        if (closeDrawer) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }


    public void clearFilters(View v) {

        cardFilters.clearFilters();

    }
}


