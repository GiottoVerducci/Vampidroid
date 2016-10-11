package name.vampidroid.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import name.vampidroid.R;


/**
 * Created by fxjr on 26/09/16.
 */

public class CardFilters extends LinearLayout {

    private static final String TAG = "CardFilters";
    private View cryptDisciplinesHeader;
    private View cryptDisciplinesLayout;
    private View clansHeader;
    private View clansLayout;
    private View cardTypesHeader;
    private View cardTypesLayout;
    private View libraryDisciplinesHeader;
    private View libraryDisciplinesLayout;

    OnCardFiltersChangeListener cardFiltersChangeListener;

    int numberOfGroupFiltersApplied;
    int numberOfCapacityFiltersApplied;
    int numberOfCryptDisciplineFiltersApplied;
    int numberOfLibraryDisciplineFiltersApplied;
    int numberOfClansFiltersApplied;
    int numberOfCardTypesFiltersApplied;

    private SparseArray<Parcelable> container;
    BadgeTextView cryptDisciplinesBagde;
    BadgeTextView clansBagde;
    BadgeTextView cardTypesBagde;
    BadgeTextView libraryDisciplinesBagde;

    public interface OnCardFiltersChangeListener {

        void onGroupsChanged(int group, boolean isChecked);

        void onCryptDisciplineChanged(String discipline, boolean isBasic, boolean isChecked);

        void onClansChanged(String clan, boolean isChecked);

        void onCardTypeChanged(String cardType, boolean isChecked);

        void onLibraryDisciplineChanged(String discipline, boolean isChecked);

        void onCapacitiesChanged(int minCapacity, int maxCapacity);
    }



    public CardFilters(Context context) {
        super(context);
        init();
    }

    public CardFilters(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public CardFilters(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardFilters(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View inflated = inflater.inflate(R.layout.widget_card_filters_layout, this, false);

        addView(inflated);
        initViews();

//        AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(getContext());
//        asyncLayoutInflater.inflate(R.layout.widget_card_filters_layout, this, new AsyncLayoutInflater.OnInflateFinishedListener() {
//            @Override
//            public void onInflateFinished(View view, int resid, ViewGroup parent) {
//
//                if (container != null) {
//                    CardFilters.super.dispatchRestoreInstanceState(container);
//                    container = null;
//                }
//
//                addView(view);
//                initViews();
//
//            }
//        });


    }

    void initViews() {


        cryptDisciplinesHeader = findViewById(R.id.disciplinesHeader);
        cryptDisciplinesLayout = findViewById(R.id.disciplinesLayout);
        cryptDisciplinesBagde = (BadgeTextView) findViewById(R.id.textCryptDisciplinesBadge);

        clansHeader = findViewById(R.id.clansHeader);
        clansLayout = findViewById(R.id.clansLayout);
        clansBagde = (BadgeTextView) findViewById(R.id.textClansBadge);

        cardTypesHeader = findViewById(R.id.cardTypesHeader);
        cardTypesLayout = findViewById(R.id.cardTypesLayout);
        cardTypesBagde = (BadgeTextView) findViewById(R.id.textCardTypesBadge);

        libraryDisciplinesHeader = findViewById(R.id.libraryDisciplinesHeader);
        libraryDisciplinesLayout = findViewById(R.id.libraryDisciplinesLayout);
        libraryDisciplinesBagde = (BadgeTextView) findViewById(R.id.textLibraryDisciplinesBadge);

        setupGroupsHandler();


        setupCryptDisciplinesHandler();

        setupClansHandler();

        setupCardTypesHandler();

        setupLibraryDisciplinesHandler();

        setupCapacitiesHandler();

        setupExpandLayout(cryptDisciplinesHeader, cryptDisciplinesLayout, (ImageView) findViewById(R.id.imgDisciplinesLayoutArrow));

        setupExpandLayout(clansHeader, clansLayout, (ImageView) findViewById(R.id.imgClansLayoutArrow));

        setupExpandLayout(cardTypesHeader, cardTypesLayout, (ImageView) findViewById(R.id.imgCardTypesLayoutArrow));

        setupExpandLayout(libraryDisciplinesHeader, libraryDisciplinesLayout, (ImageView) findViewById(R.id.imgLibraryDisciplinesLayoutArrow));


        cryptDisciplinesBagde.setAutoShowHide(true);
        clansBagde.setAutoShowHide(true);
        cardTypesBagde.setAutoShowHide(true);
        libraryDisciplinesBagde.setAutoShowHide(true);


    }

    private void setupCapacitiesHandler() {
        final CryptCapacitySeekBars cryptCapacitySeekBars = (CryptCapacitySeekBars) findViewById(R.id.crypt_capacity_seekbars);

        SeekBar.OnSeekBarChangeListener cryptSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

            boolean trackingTouchStarted = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged() called with: seekBar = [" + seekBar + "], progress = [" + progress + "], fromUser = [" + fromUser + "]");

                numberOfCapacityFiltersApplied = 0;
                numberOfCapacityFiltersApplied += cryptCapacitySeekBars.getMinSeekBarValue() > 1 ? 1 : 0;
                numberOfCapacityFiltersApplied += cryptCapacitySeekBars.getMaxSeekBarValue() < 11 ? 1 : 0;

                if (!fromUser && !trackingTouchStarted) {
                    // The progress was changed because of a state restore. Report this change.
                    if (cardFiltersChangeListener != null) {
                        cardFiltersChangeListener.onCapacitiesChanged(cryptCapacitySeekBars.getMinSeekBarValue(), cryptCapacitySeekBars.getMaxSeekBarValue());
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                trackingTouchStarted = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (cardFiltersChangeListener != null) {
                    cardFiltersChangeListener.onCapacitiesChanged(cryptCapacitySeekBars.getMinSeekBarValue(), cryptCapacitySeekBars.getMaxSeekBarValue());
                }
                trackingTouchStarted = false;

            }
        };

        cryptCapacitySeekBars.setOnSeekBarChangeListenerMin(cryptSeekBarChangeListener);

        cryptCapacitySeekBars.setOnSeekBarChangeListenerMax(cryptSeekBarChangeListener);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        Log.d(TAG, "dispatchRestoreInstanceState() called with: container = [" + container + "]");
        super.dispatchRestoreInstanceState(container);
        // Doesn't restore child states yet because they may not have been inflated yet.
        // Keep the state to be used later.
//        this.container = container;
    }

//    @Override
//    protected Parcelable onSaveInstanceState() {
//
//        Log.d(TAG, "onSaveInstanceState() called");
//        Bundle state = new Bundle();
//        Parcelable superState = super.onSaveInstanceState();
//        state.putParcelable("superState", superState);
//
//        state.putInt("numberOfGroupFiltersApplied", numberOfGroupFiltersApplied);
//        state.putInt("numberOfCapacityFiltersApplied", numberOfCapacityFiltersApplied);
//        state.putInt("numberOfCryptDisciplineFiltersApplied", numberOfCryptDisciplineFiltersApplied);
//
//        return state;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        Log.d(TAG, "onRestoreInstanceState() called with: state = [" + state + "]");
//
//        if (state instanceof Bundle) {
//            Log.d(TAG, "onRestoreInstanceState: Restoring from bundle");
//            Bundle bundle = (Bundle) state;
//            super.onRestoreInstanceState(bundle.getParcelable("superState"));
//
//            numberOfGroupFiltersApplied = bundle.getInt("numberOfGroupFiltersApplied");
//            numberOfCapacityFiltersApplied = bundle.getInt("numberOfCapacityFiltersApplied");
//            numberOfCryptDisciplineFiltersApplied = bundle.getInt("numberOfCryptDisciplineFiltersApplied");
//
//        } else {
//            super.onRestoreInstanceState(state);
//        }
//    }



    //    Reference: http://stackoverflow.com/questions/11358121/how-to-handle-the-checkbox-ischecked-and-unchecked-event-in-android
    private void setupGroupsHandler() {

        CompoundButton.OnCheckedChangeListener groupsClickListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {

                Log.d(TAG, "onCheckedChanged() called with: checkbox = [" + checkbox + "], isChecked = [" + isChecked + "]");

                numberOfGroupFiltersApplied += isChecked ? 1 : -1;

                if (cardFiltersChangeListener != null) {
                    cardFiltersChangeListener.onGroupsChanged(Integer.parseInt(checkbox.getText().toString()), isChecked);
                }

            }
        };

        ViewGroup groupsContainer = (ViewGroup) findViewById(R.id.relativeLayoutGroups);

        for (int i = 0; i < groupsContainer.getChildCount(); i++) {
            if (groupsContainer.getChildAt(i) instanceof CheckBox) {
                CheckBox checkbox = (CheckBox) groupsContainer.getChildAt(i);
                checkbox.setOnCheckedChangeListener(groupsClickListener);

            }
        }

    }


    private void setupCryptDisciplinesHandler() {


        ViewGroup cryptDisciplinesContainer = (ViewGroup) cryptDisciplinesLayout;

        for (int i = 0; i < cryptDisciplinesContainer.getChildCount(); i++) {
            ViewGroup cryptDisciplineRow = (ViewGroup) cryptDisciplinesContainer.getChildAt(i);

            CheckBox cryptDisciplineCheckboxBasic = (CheckBox) cryptDisciplineRow.getChildAt(1); // Basic Discipline
            CheckBox cryptDisciplineCheckboxAdv = (CheckBox) cryptDisciplineRow.getChildAt(2); // Adv Discipline

            setupCryptDisciplineHandlerHelper(cryptDisciplineCheckboxBasic, true);
            setupCryptDisciplineHandlerHelper(cryptDisciplineCheckboxAdv, false);

        }

    }

    private void setupCryptDisciplineHandlerHelper(CheckBox cryptDisciplineCheckBox, final boolean isBasic) {


        CompoundButton.OnCheckedChangeListener cryptDisciplineHandler = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {

                numberOfCryptDisciplineFiltersApplied += isChecked ? 1 : -1;
                cryptDisciplinesBagde.setNumericText(numberOfCryptDisciplineFiltersApplied);

                // TODO: 01/10/16 Change to use a tag
                String discipline = getResources().getResourceEntryName(checkbox.getId());
                discipline = discipline.substring(discipline.length() - 3);

                if (cardFiltersChangeListener != null) {
                    cardFiltersChangeListener.onCryptDisciplineChanged(discipline, isBasic, isChecked);
                }

            }
        };

        cryptDisciplineCheckBox.setOnCheckedChangeListener(cryptDisciplineHandler);
    }

    private void setupCardTypesHandler() {

        setupTextCheckBoxRowHandler((ViewGroup) cardTypesLayout, new OnTextCheckBoxRowHandlerClickListener() {
            @Override
            public void onClick(TextView rowText, CheckBox rowCheckBox) {
                rowCheckBox.toggle();

                numberOfCardTypesFiltersApplied += rowCheckBox.isChecked() ? 1 : -1;

                cardTypesBagde.setNumericText(numberOfCardTypesFiltersApplied);

                if (cardFiltersChangeListener != null) {
                    cardFiltersChangeListener.onCardTypeChanged(rowText.getText().toString(), rowCheckBox.isChecked());
                }
            }
        });
    }

    private void setupClansHandler() {
        setupTextCheckBoxRowHandler((ViewGroup) clansLayout, new OnTextCheckBoxRowHandlerClickListener() {
            @Override
            public void onClick(TextView rowText, CheckBox rowCheckBox) {
                rowCheckBox.toggle();

                numberOfClansFiltersApplied += rowCheckBox.isChecked() ? 1 : -1;

                clansBagde.setNumericText(numberOfClansFiltersApplied);

                if (cardFiltersChangeListener != null) {
                    cardFiltersChangeListener.onClansChanged(rowText.getText().toString(), rowCheckBox.isChecked());
                }
            }
        });
    }

//
//    private void setupLibraryDisciplinesHandler2() {
//
//
//        ViewGroup libraryDisciplinesContainer = (ViewGroup) findViewById(R.id.libraryDisciplinesLayout);
//
//        for (int i = 0; i < libraryDisciplinesContainer.getChildCount(); i++) {
//            ViewGroup libraryDisciplineRow = (ViewGroup) libraryDisciplinesContainer.getChildAt(i);
//
//            final TextView libraryDisciplineText = (TextView) libraryDisciplineRow.getChildAt(0);
//            final CheckBox libraryDisciplineCheckbox = (CheckBox) libraryDisciplineRow.getChildAt(1);
//
//
//            libraryDisciplineRow.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    libraryDisciplineCheckbox.toggle();
//
//                    numberOfLibraryDisciplineFiltersApplied += libraryDisciplineCheckbox.isChecked() ? 1 : -1;
//
//
//                    libraryDisciplinesBagde.setNumericText(numberOfLibraryDisciplineFiltersApplied);
//
//                    if (cardFiltersChangeListener != null) {
//                        cardFiltersChangeListener.onLibraryDisciplineChanged(libraryDisciplineText.getText().toString(), libraryDisciplineCheckbox.isChecked());
//                    }
//
//                }
//            });
//
//        }
//
//    }

    private void setupLibraryDisciplinesHandler() {

        setupTextCheckBoxRowHandler((ViewGroup) libraryDisciplinesLayout, new OnTextCheckBoxRowHandlerClickListener() {
            @Override
            public void onClick(TextView rowText, CheckBox rowCheckBox) {
                rowCheckBox.toggle();

                numberOfLibraryDisciplineFiltersApplied += rowCheckBox.isChecked() ? 1 : -1;


                libraryDisciplinesBagde.setNumericText(numberOfLibraryDisciplineFiltersApplied);

                if (cardFiltersChangeListener != null) {
                    cardFiltersChangeListener.onLibraryDisciplineChanged(rowText.getText().toString(), rowCheckBox.isChecked());
                }
            }
        });
    }

    interface OnTextCheckBoxRowHandlerClickListener {
        void onClick(TextView rowText, CheckBox rowCheckBox);
    }


    private void setupTextCheckBoxRowHandler(ViewGroup root, final OnTextCheckBoxRowHandlerClickListener rowClickListener) {

        for (int i = 0; i < root.getChildCount(); i++) {
            ViewGroup row = (ViewGroup) root.getChildAt(i);

            final TextView rowText = (TextView) row.getChildAt(0);
            final CheckBox rowCheckbox = (CheckBox) row.getChildAt(1);

            row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    rowClickListener.onClick(rowText, rowCheckbox);
                }
            });


        }
    }


    private void setupExpandLayout(final View header, final View layoutToExpand, final ImageView imgArrow) {
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (layoutToExpand.isShown()) {
                    layoutToExpand.setVisibility(View.GONE);
                    imgArrow.setImageResource(R.drawable.ic_expand_more_black_24dp);
                } else {

                    imgArrow.setImageResource(R.drawable.ic_expand_less_black_24dp);


//                    Reference: http://stackoverflow.com/questions/19765938/show-and-hide-a-view-with-a-slide-up-down-animation
                    // Prepare the View for the animation
                    layoutToExpand.setVisibility(View.VISIBLE);
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
//						layoutToExpand.setAlpha(0.0f);
//
//						// Start the animation
//						layoutToExpand.animate()
//								.alpha(1.0f);
//					}
                }

            }
        });


    }


    public void setOnCardFiltersChangeListener(OnCardFiltersChangeListener listener) {
        cardFiltersChangeListener = listener;
    }

    public void showCryptFilters() {
        cryptDisciplinesHeader.setVisibility(VISIBLE);
        cryptDisciplinesLayout.setVisibility(VISIBLE);

        clansHeader.setVisibility(VISIBLE);
        clansLayout.setVisibility(VISIBLE);

    }

    public void showLibraryFilters() {

        cryptDisciplinesHeader.setVisibility(GONE);
        cryptDisciplinesLayout.setVisibility(GONE);

        clansHeader.setVisibility(GONE);
        clansLayout.setVisibility(GONE);
    }

    public int getNumberOfFiltersApplied() {

        return numberOfGroupFiltersApplied + numberOfCryptDisciplineFiltersApplied + numberOfClansFiltersApplied + numberOfCardTypesFiltersApplied + numberOfLibraryDisciplineFiltersApplied + numberOfCapacityFiltersApplied;

    }

}

