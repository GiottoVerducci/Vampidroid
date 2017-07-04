package name.vampidroid;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import name.vampidroid.data.LibraryCard;


/**
 * Created by fxjr on 06/07/16.
 */

public class LibraryCardDetailsActivity extends AppCompatActivity {

    private static final String TAG = "LibraryCardDetailsActiv";
    private ImageView cardImage;

    // Discipline images
    private ImageView[] disciplineImageViews = new ImageView[3];
    private FloatingActionButton fab;

    private CardDetailsViewModel cardDetailsViewModel;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private CompositeDisposable subscriptions;
    private LibraryCard libraryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_card_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        //        Reference: https://plus.google.com/+AlexLockwood/posts/FJsp1N9XNLS
        supportPostponeEnterTransition();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cardImage = (ImageView) findViewById(R.id.cardImage);


        cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent showCardImage = new Intent(view.getContext(), CardImageActivity.class);
                showCardImage.putExtra("cardId", getIntent().getExtras().getLong("cardId"));
                showCardImage.putExtra("cardImageFileName", Utils.getCardFileName(libraryCard.getName(), false));
                showCardImage.putExtra("resIdFallbackCardImage", R.drawable.green_back);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(LibraryCardDetailsActivity.this, cardImage, "cardImageTransition").toBundle();
                    view.getContext().startActivity(showCardImage, bundle);
                } else
                    view.getContext().startActivity(showCardImage);


            }
        });

        setupDisciplineImagesArray();

        subscriptions = new CompositeDisposable();

        setupCardData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.dispose();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.library_details_menu, menu);

        return true;

    }

    //    Reference: https://github.com/codepath/android_guides/wiki/Shared-Element-Activity-Transition
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_share:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, libraryCard.getName());

                StringBuilder sb = new StringBuilder();

                sb.append(String.format("Name: %s %n", libraryCard.getName()));
                sb.append(String.format("Type: %s %n", libraryCard.getType()));
                sb.append(String.format("Clan: %s %n", libraryCard.getClan()));
                sb.append(String.format("PoolCost: %s %n", libraryCard.getPoolCost()));
                sb.append(String.format("BloodCost: %s %n", libraryCard.getBloodCost()));
                sb.append(String.format("Disciplines: %s %n", libraryCard.getDisciplines()));
                sb.append(String.format("Set/Rarity: %s %n", libraryCard.getSetRarity()));
                sb.append(String.format("Artist: %s %n", libraryCard.getArtist()));
                sb.append(String.format("CardText: %s %n", libraryCard.getText()));

                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_library_card_text)));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        fab.hide();
        super.onBackPressed();

    }


    //    Reference: http://stackoverflow.com/questions/19312109/execute-a-method-after-an-activity-is-visible-to-user

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

//        fab.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                fab.show();
//            }
//        }, 300);

    }


    private void setupDisciplineImagesArray() {

        disciplineImageViews[0] = (ImageView) findViewById(R.id.img_card_details_discipline1);
        disciplineImageViews[1] = (ImageView) findViewById(R.id.img_card_details_discipline2);
        disciplineImageViews[2] = (ImageView) findViewById(R.id.img_card_details_discipline3);


    }


    private void setupCardData() {

        final TextView txtCardText = (TextView) findViewById(R.id.cardText);
        final TextView txtCardType = (TextView) findViewById(R.id.txtCardType);
        final TextView txtCardCost = (TextView) findViewById(R.id.txtCardCost);
        final TextView txtCardSetRarity = (TextView) findViewById(R.id.txtCardSetRarity);


        cardDetailsViewModel = ((VampiDroidApplication)getApplication()).getCardDetailsViewModel(getIntent().getExtras().getLong("cardId"));


        subscriptions.add(
                cardDetailsViewModel.getLibraryCard()
                        .flatMap(new Function<LibraryCard, Observable<Pair<Pair<BitmapDrawable, Palette>, Drawable[]>>>() {
                            @Override
                            public Observable<Pair<Pair<BitmapDrawable, Palette>, Drawable[]>> apply(LibraryCard libraryCard) throws Exception {

                                LibraryCardDetailsActivity.this.libraryCard = libraryCard;

                                return Observable.zip(
                                        Utils.loadLibraryCardImageWithPalette(libraryCard.getName()).subscribeOn(Schedulers.io()),

                                        Utils.getDisciplinesArrayObservable(LibraryCardDetailsActivity.this, libraryCard.getDisciplines()).subscribeOn(Schedulers.io()),

                                        new BiFunction<Pair<BitmapDrawable, Palette>, Drawable[], Pair<Pair<BitmapDrawable, Palette>, Drawable[]>>() {
                                            @Override
                                            public Pair<Pair<BitmapDrawable, Palette>, Drawable[]> apply(Pair<BitmapDrawable, Palette> bitmapDrawablePalettePair, Drawable[] drawables) throws Exception {
                                                return Pair.create(bitmapDrawablePalettePair, drawables);
                                            }
                                        }

                                );
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Pair<Pair<BitmapDrawable, Palette>, Drawable[]>>() {
                            @Override
                            public void accept(Pair<Pair<BitmapDrawable, Palette>, Drawable[]> data) throws Exception {

                                Pair<BitmapDrawable, Palette> bitmapDrawablePalettePair = data.first;

                                BitmapDrawable imageDrawable = bitmapDrawablePalettePair.first;
                                Palette palette = bitmapDrawablePalettePair.second;

                                collapsingToolbarLayout.setTitle(libraryCard.getName());
                                txtCardText.setText(libraryCard.getText());
                                txtCardType.setText(libraryCard.getType());
                                txtCardSetRarity.setText(libraryCard.getSetRarity());


                                cardImage.setImageDrawable(imageDrawable);

                                final TextView txtCardTypeLabel = (TextView) findViewById(R.id.txtCardTypeLabel);
                                final TextView txtDisciplinesLabel = (TextView) findViewById(R.id.txtCardDisciplinesLabel);
                                final TextView txtCardTextLabel = (TextView) findViewById(R.id.txtCardTextLabel);
                                final TextView txtCardCostLabel = (TextView) findViewById(R.id.txtCardCostLabel);
                                final TextView txtCardSetRarityLabel = (TextView) findViewById(R.id.txtCardSetRarityLabel);

                                final int defaultColor = ContextCompat.getColor(LibraryCardDetailsActivity.this, R.color.colorAccent);

                                txtCardTypeLabel.setTextColor(palette.getVibrantColor(defaultColor));
                                txtDisciplinesLabel.setTextColor(palette.getVibrantColor(defaultColor));
                                txtCardTextLabel.setTextColor(palette.getVibrantColor(defaultColor));
                                txtCardCostLabel.setTextColor(palette.getVibrantColor(defaultColor));
                                txtCardSetRarityLabel.setTextColor(palette.getVibrantColor(defaultColor));


                                if (!libraryCard.getBloodCost().isEmpty()) {
                                    txtCardCostLabel.setText("Blood Cost:");
                                    txtCardCost.setText(libraryCard.getBloodCost());
                                } else if (!libraryCard.getPoolCost().isEmpty()) {
                                    txtCardCostLabel.setText("Pool Cost:");
                                    txtCardCost.setText(libraryCard.getPoolCost());
                                } else {
                                    txtCardCostLabel.setVisibility(View.GONE);
                                    txtCardCost.setVisibility(View.GONE);
                                }





                                // Reference: http://stackoverflow.com/questions/30966222/change-color-of-floating-action-button-from-appcompat-22-2-0-programmatically
//                                fab.setBackgroundTintList(ColorStateList.valueOf(palette.getVibrantColor(defaultColor)));

                                Drawable[] drawables = data.second;

                                if (libraryCard.getDisciplines().length() > 0) {
                                    txtDisciplinesLabel.setVisibility(View.VISIBLE);
                                    for (int disIndex = 0; disIndex < drawables.length; disIndex++) {
                                        disciplineImageViews[disIndex].setImageDrawable(drawables[disIndex]);
                                        disciplineImageViews[disIndex].setVisibility(View.VISIBLE);
                                    }
                                }

                                supportStartPostponedEnterTransition();


                            }
                        }));

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

}

