package name.vampidroid;

import android.database.Cursor;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import name.vampidroid.data.source.CardsRepository;
import name.vampidroid.data.source.PreferenceRepository;
import name.vampidroid.utils.FilterStateQueryConverter;


/**
 * Created by fxjr on 14/11/2016.
 */

public class CardsViewModel {

    private final CardsRepository cardsRepository;

    private final PublishSubject<FilterState> filterCryptCards = PublishSubject.create();

    private final PublishSubject<FilterState> filterLibraryCards = PublishSubject.create();

    private final PublishSubject<String> cryptTabTitle = PublishSubject.create();

    private final PublishSubject<String> libraryTabTitle = PublishSubject.create();

    private final Observable<Boolean> showCardsCountPreferenceObservable;

    private final PreferenceRepository preferenceRepository;

    private int cryptCardsCount;

    private int libraryCardsCount;

    public CardsViewModel(CardsRepository cardsRepository, PreferenceRepository preferenceRepository) {

        this.cardsRepository = cardsRepository;
        this.preferenceRepository = preferenceRepository;
        showCardsCountPreferenceObservable = preferenceRepository.getShowCardsCountObservable();

    }

    public Observable<Cursor> getCryptCards() {


        return filterCryptCards
                .observeOn(Schedulers.computation())
                .flatMap(new Function<FilterState, Observable<Cursor>>() {
                    @Override
                    public Observable<Cursor> apply(FilterState filterState) throws Exception {
                        filterState.setSearchInsideCardText(preferenceRepository.shouldSearchTextCard());
                        return cardsRepository.getCryptCards(FilterStateQueryConverter.getCryptFilter(filterState));
                    }
                })
                .doOnNext(new Consumer<Cursor>() {
                    @Override
                    public void accept(Cursor cursor) throws Exception {
                        cryptCardsCount = cursor.getCount();
                        cryptTabTitle.onNext(getTabTitle("Crypt", preferenceRepository.shouldShowCardsCount(), cryptCardsCount));
                    }
                });

    }

    public Observable<Cursor> getLibraryCards() {

        return filterLibraryCards
                .observeOn(Schedulers.computation())
                .flatMap(new Function<FilterState, Observable<Cursor>>() {
                    @Override
                    public Observable<Cursor> apply(FilterState filterState) throws Exception {
                        filterState.setSearchInsideCardText(preferenceRepository.shouldSearchTextCard());
                        return cardsRepository.getLibraryCards(FilterStateQueryConverter.getLibraryFilter(filterState));
                    }
                })
                .doOnNext(new Consumer<Cursor>() {
                    @Override
                    public void accept(Cursor cursor) throws Exception {
                        libraryCardsCount = cursor.getCount();
                        libraryTabTitle.onNext(getTabTitle("Library", preferenceRepository.shouldShowCardsCount(), libraryCardsCount));
                    }
                });
    }


    public Observable<Cursor> getCards(int cardType) {
        return cardType == 0 ? getCryptCards() : getLibraryCards();

    }

    public void filterCryptCards(FilterState filterState) {
        filterCryptCards.onNext(filterState);
    }

    public void filterLibraryCards(FilterState filterState) {
        filterLibraryCards.onNext(filterState);
    }

    public Observable<String> getCryptTabTitle() {

        // The crypt tab title will need to be changed when either a new data set is loaded
        // (and the preference is set to show the card count) or when the preference itself
        // is changed. Here we merge the two source observables to create a single observable.
        return cryptTabTitle
                .mergeWith(showCardsCountPreferenceObservable
                        .skip(1) // Skip first emission on subscribe
                        .map(new Function<Boolean, String>() {
                            @Override
                            public String apply(Boolean showCards) throws Exception {
                                return getTabTitle("Crypt", showCards, cryptCardsCount);
                            }
                        }));
    }


    public Observable<String> getLibraryTabTitle() {
        return libraryTabTitle
                .mergeWith(showCardsCountPreferenceObservable
                        .skip(1) // Skip first emission on subscribe
                        .map(new Function<Boolean, String>() {
                            @Override
                            public String apply(Boolean showCards) throws Exception {
                                return getTabTitle("Library", showCards, libraryCardsCount);
                            }
                        }));
    }


    /**
     * Get an observable which emits the text hint that should be used in the search text field.
     * This text hint changes according to the settings if the search should be done only in the card name
     * or in the card text too.
     * @return An observable which emits the resourceId of the string to be used as a text hint.
     */
    public Observable<Integer> getSearchTextHintObservable() {

        return preferenceRepository.getSearchTextCardObservable()
                .map(new Function<Boolean, Integer>() {
                    @Override
                    public Integer apply(Boolean searchTextCard) throws Exception {
                        return searchTextCard ? R.string.search_bar_filter_card_name_and_card_text : R.string.search_bar_filter_card_name;
                    }
                });

    }

    /**
     * This observable will emit an item (not used) to indicate that the data refresh is needed.
     * @return An observable which emits an String used only to indicate that a refresh is needed.
     */
    public Observable<String> getNeedRefreshFlag() {


        return Observable
                .combineLatest(
                        preferenceRepository.getSearchTextCardObservable(),
                        preferenceRepository.getCardsImagesFolderObservable(),
                        new BiFunction<Boolean, String, String>() {
                            @Override
                            public String apply(Boolean aBoolean, String s) throws Exception {
                                return "";
                            }
                        });

    }


    private String getTabTitle(String tabTitlePrefix, boolean includeCount, int count) {

        return (includeCount ? String.format("%s (%d)", tabTitlePrefix, count) : tabTitlePrefix);

    }
}
