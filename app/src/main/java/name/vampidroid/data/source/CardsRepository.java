package name.vampidroid.data.source;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.RxPagedListBuilder;
import android.arch.persistence.db.SimpleSQLiteQuery;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import name.vampidroid.DatabaseHelper;
import name.vampidroid.data.CryptCard;
import name.vampidroid.data.LibraryCard;

/**
 * Created by fxjr on 03/01/17.
 */

public class CardsRepository {

    private static final String TAG = "CardsRepository";

    private static PagedList.Config defaultPagedListConfig = new PagedList.Config.Builder()
            .setPageSize(50)
            .setInitialLoadSizeHint(100)
            .setPrefetchDistance(30)
            .setEnablePlaceholders(true).build();

    public LiveData<PagedList<CryptCard>> getCryptCardsLiveData(final String filter) {

        return new LivePagedListBuilder<>(DatabaseHelper.getRoomDatabase().cryptCardDao().getCardsByQuery(new SimpleSQLiteQuery(DatabaseHelper.MAIN_LIST_CRYPT_QUERY + filter)),
                defaultPagedListConfig).build();
    }

    public LiveData<PagedList<LibraryCard>> getLibraryCardsLiveData(final String filter) {

        return new LivePagedListBuilder<>(DatabaseHelper.getRoomDatabase().libraryCardDao().getCardsByQuery(new SimpleSQLiteQuery(DatabaseHelper.MAIN_LIST_LIBRARY_QUERY + filter)),
                defaultPagedListConfig).build();
    }

    public Flowable<LibraryCard> getLibraryCard(final long cardId) {

        return DatabaseHelper.getRoomDatabase().libraryCardDao().getById(cardId);
    }

    public Flowable<CryptCard> getCryptCard(long cardId) {

        return DatabaseHelper.getRoomDatabase().cryptCardDao().getById(cardId);

    }
}