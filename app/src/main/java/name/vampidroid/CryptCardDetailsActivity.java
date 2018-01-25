package name.vampidroid;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import name.vampidroid.data.CryptCard;


/**
 * Created by fxjr on 06/07/16.
 */

public class CryptCardDetailsActivity extends AppCompatActivity {

    private static final String TAG = "CryptCardDetailsActivit";
    private ImageView cardImage;

    // Discipline images

    private ImageView[] disciplineImageViews = new ImageView[7];
    private FloatingActionButton fab;

    private CardDetailsViewModel cardDetailsViewModel;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private CompositeDisposable subscriptions;
    private CryptCard cryptCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypt_card_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        //        Reference: https://plus.google.com/+AlexLockwood/posts/FJsp1N9XNLS
        supportPostponeEnterTransition();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cardImage = findViewById(R.id.cardImage);


        cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent showCardImage = new Intent(view.getContext(), CardImageActivity.class);
                showCardImage.putExtra("cardId", getIntent().getExtras().getLong("cardId"));
                showCardImage.putExtra("cardImageFileName", Utils.getFullCardFileName(cryptCard.getName(), cryptCard.isAdvanced()));
                showCardImage.putExtra("resIdFallbackCardImage", R.drawable.gold_back);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(CryptCardDetailsActivity.this, cardImage, "cardImageTransition").toBundle();
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
        inflater.inflate(R.menu.crypt_details_menu, menu);

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

                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, cryptCard.getName());

                StringBuilder sb = new StringBuilder();

                sb.append(String.format("Name: %s %n", cryptCard.getName()));
                sb.append(String.format("Capacity: %s %n", cryptCard.getCapacity()));
                sb.append(String.format("Type: %s %n", cryptCard.getType()));
                sb.append(String.format("Group: %s %n", cryptCard.getGroup()));
                sb.append(String.format("Clan: %s %n", cryptCard.getClan()));
                sb.append(String.format("Disciplines: %s %n", cryptCard.getDisciplines()));
                sb.append(String.format("Set/Rarity: %s %n", cryptCard.getSetRarity()));
                sb.append(String.format("Artist: %s %n", cryptCard.getArtist()));
                sb.append(String.format("CardText: %s %n", cryptCard.getText()));

                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_crypt_card_text)));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called");
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

        disciplineImageViews[0] = findViewById(R.id.img_card_details_discipline1);
        disciplineImageViews[1] = findViewById(R.id.img_card_details_discipline2);
        disciplineImageViews[2] = findViewById(R.id.img_card_details_discipline3);
        disciplineImageViews[3] = findViewById(R.id.img_card_details_discipline4);
        disciplineImageViews[4] = findViewById(R.id.img_card_details_discipline5);
        disciplineImageViews[5] = findViewById(R.id.img_card_details_discipline6);
        disciplineImageViews[6] = findViewById(R.id.img_card_details_discipline7);


    }


    private void setupCardData() {

        final TextView txtCardText = findViewById(R.id.cardText);
        final TextView txtCardCapacity = findViewById(R.id.txtCardCapacity);
        final TextView txtCardSetRarity = findViewById(R.id.txtCardSetRarity);

        cardDetailsViewModel = ViewModelProviders.of(this).get(CardDetailsViewModel.class);


        subscriptions.add(
                cardDetailsViewModel.getCryptCard(getIntent().getExtras().getLong("cardId"))
                        .observeOn(AndroidSchedulers.mainThread())

                        .subscribe(new Consumer<CryptCard>() {
                            @Override
                            public void accept(CryptCard cryptCard) throws Exception {

                                CryptCardDetailsActivity.this.cryptCard = cryptCard;

                                collapsingToolbarLayout.setTitle(cryptCard.getName());
                                txtCardText.setText(cryptCard.getText());
                                txtCardCapacity.setText(cryptCard.getCapacity());
                                txtCardSetRarity.setText(cryptCard.getSetRarity());

                                final TextView txtDisciplinesLabel = findViewById(R.id.textCardDisciplines);
                                final TextView txtTextLabel = findViewById(R.id.textCardText);
                                final TextView txtCapacityLabel = findViewById(R.id.txtCapacityLabel);
                                final TextView txtSetRarityLabel = findViewById(R.id.txtSetRarityLabel);


                                String[] disciplines = cryptCard.getDisciplines().split("[^a-zA-Z]+");

                                int disciplineIndex;
                                for (disciplineIndex = 0; disciplineIndex < disciplines.length; disciplineIndex++) {

                                    Integer disciplineDrawableID = Utils.getDisciplinesDrawableResourceIdsMap().get(disciplines[disciplineIndex]);
                                    if (disciplineDrawableID != null) {
                                        Glide.with(CryptCardDetailsActivity.this)
                                                .load(disciplineDrawableID)
                                                .fitCenter()
                                                .into(disciplineImageViews[disciplineIndex]);
                                        disciplineImageViews[disciplineIndex].setVisibility(View.VISIBLE);
                                    }
                                }

                                Glide.with(CryptCardDetailsActivity.this)
                                        .load(Utils.getFullCardFileName(cryptCard.getName(), cryptCard.isAdvanced()))
                                        .asBitmap()
                                        .error(R.drawable.gold_back)
//                                        .fitCenter()
                                        .dontAnimate()
                                        .into(new BitmapImageViewTarget(cardImage) {
                                            @Override
                                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                                super.onLoadFailed(e, errorDrawable);
                                                supportStartPostponedEnterTransition();
                                            }

                                            @Override
                                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                                super.onResourceReady(bitmap, glideAnimation);
                                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                                    @Override
                                                    public void onGenerated(Palette palette) {
                                                        // Here's your generated palette
                                                        final int paletteColor = palette.getVibrantColor(ContextCompat.getColor(CryptCardDetailsActivity.this, R.color.colorAccent));
//

                                                        txtDisciplinesLabel.setTextColor(paletteColor);
                                                        txtTextLabel.setTextColor(paletteColor);
                                                        txtCapacityLabel.setTextColor(paletteColor);
                                                        txtSetRarityLabel.setTextColor(paletteColor);
//
//                                                // Reference: http://stackoverflow.com/questions/30966222/change-color-of-floating-action-button-from-appcompat-22-2-0-programmatically
//                                                // fab.setBackgroundTintList(ColorStateList.valueOf(palette.getVibrantColor(defaultColor)));
//
                                                        supportStartPostponedEnterTransition();
                                                    }
                                                });
                                            }
                                        });
                            }
                        }));



    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}
