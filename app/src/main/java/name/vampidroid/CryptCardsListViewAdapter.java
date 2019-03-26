package name.vampidroid;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.l4digital.fastscroll.FastScroller;

import java.util.List;
import java.util.Objects;

import name.vampidroid.data.Card;
import name.vampidroid.data.CryptCard;

/**
 * Created by francisco on 06/09/17.
 */

public class CryptCardsListViewAdapter extends PagedListAdapter<CryptCard, CryptCardsListViewAdapter.ViewHolder>
        implements FastScroller.SectionIndexer {


    public Integer cryptTextLinesCount;

    public CryptCardsListViewAdapter() {
        super(DIFF_CALLBACK);
    }


    View.OnClickListener editDeckListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Snackbar.make(v, "Clicked edit deck", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    };

    public static final DiffUtil.ItemCallback<CryptCard> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CryptCard>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull CryptCard oldCard, @NonNull CryptCard newCard) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldCard.getUid().equals(newCard.getUid());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull CryptCard oldCard, @NonNull CryptCard newCard) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldCard.getUid().equals(newCard.getUid());
                }
            };



    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.crypt_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        CryptCardsListViewAdapter.ViewHolder vh = new CryptCardsListViewAdapter.ViewHolder(v, cryptTextLinesCount);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        CryptCard cryptCard = getItem(position);

        if (cryptCard != null) {
            viewHolder.bindTo(cryptCard);
        } else {
            viewHolder.clear();
        }
    }

    @Override
    public String getSectionText(int position) {
        CryptCard card = getItem(position);
        return (card == null) ? "" : card.getName().substring(0, 1);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView txtCardClan;
        public TextView txtCardName;
        public TextView txtCardExtraInformation;
        public TextView txtCardCost;
        public TextView txtCardGroup;
        public TextView txtCardText;
        public long cardId;
        public ImageView imageViewCardImage;
        public String txtCardAdv;

        private Integer cryptTextLinesCount;

        public ViewHolder(View v, Integer cryptTextLinesCount) {
            super(v);

            this.cryptTextLinesCount = cryptTextLinesCount;
            txtCardClan = v.findViewById(R.id.txtCardClan);
            txtCardName = v.findViewById(R.id.txtCardName);
            txtCardExtraInformation = v.findViewById(R.id.txtCardExtraInformation);
            txtCardCost = v.findViewById(R.id.txtCardCost);
            txtCardGroup = v.findViewById(R.id.txtCardGroup);
            txtCardText = v.findViewById(R.id.txtCardText);
            imageViewCardImage = v.findViewById(R.id.imageViewCardImage);

            v.setOnClickListener(this);

            imageViewCardImage.setOnClickListener(this);
        }

        public void bindTo(CryptCard cryptCard) {
            String cardName = cryptCard.getName();

            cardId = cryptCard.getUid();
            txtCardClan.setText(cryptCard.getClan());
            txtCardName.setText(cardName);
            txtCardCost.setText(cryptCard.getCapacity());
            txtCardExtraInformation.setText(cryptCard.getDisciplines());
            txtCardGroup.setText(cryptCard.getGroup());

            txtCardText.setText(cryptCard.getTextWithStyle());
            txtCardText.setMaxLines(cryptTextLinesCount);
            txtCardAdv = cryptCard.getAdvanced();

            Glide
                    .with(imageViewCardImage.getContext())
                    .load(Utils.getFullCardFileName(cardName, txtCardAdv.length() > 0))
                    .fitCenter()
                    .placeholder(R.drawable.gold_back)
                    .into(imageViewCardImage);
        }



        public void clear() {
            cardId = 0;
            txtCardClan.setText("");
            txtCardName.setText("");
            txtCardCost.setText("");
            txtCardExtraInformation.setText("");
            txtCardGroup.setText("");
            txtCardText.setText("");
        }

        @Override
        public void onClick(View view) {

            Context context = view.getContext();

            Intent launch;

            if (view == imageViewCardImage) {
                String cardName = txtCardName.getText().toString();
                launch = new Intent(view.getContext(), CardImageActivity.class);
                launch.putExtra("cardImageFileName", Utils.getFullCardFileName(cardName, !txtCardAdv.isEmpty()));
                launch.putExtra("resIdFallbackCardImage", R.drawable.gold_back);
            } else {
                launch = new Intent(context, CryptCardDetailsActivity.class);
            }

            launch.putExtra("cardId", cardId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(Utils.getActivity(context), imageViewCardImage, "cardImageTransition").toBundle();
                context.startActivity(launch, bundle);
            } else {
                context.startActivity(launch);
            }
        }
    }

}

