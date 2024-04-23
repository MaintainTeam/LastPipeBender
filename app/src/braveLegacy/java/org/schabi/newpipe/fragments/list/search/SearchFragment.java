package org.schabi.newpipe.fragments.list.search;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;
import static org.schabi.newpipe.ktx.ViewUtils.animate;
import static org.schabi.newpipe.util.ExtractorHelper.showMetaInfoInTextView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.databinding.FragmentSearchBinding;
import org.schabi.newpipe.error.ErrorInfo;
import org.schabi.newpipe.error.ReCaptchaActivity;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.fragments.BackPressable;
import org.schabi.newpipe.fragments.list.BaseListFragment;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterChipDialogFragment;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterDialogFragment;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterLogic;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterOptionMenuAlikeDialogFragment;
import org.schabi.newpipe.ktx.AnimationType;
import org.schabi.newpipe.ktx.ExceptionUtils;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.settings.NewPipeSettings;
import org.schabi.newpipe.util.Constants;
import org.schabi.newpipe.util.DeviceUtils;
import org.schabi.newpipe.util.ExtractorHelper;
import org.schabi.newpipe.util.KeyboardUtil;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import icepick.State;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends BaseListFragment<SearchInfo, ListExtractor.InfoItemsPage<?>>
        implements BackPressable {
    /*//////////////////////////////////////////////////////////////////////////
    // Search
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * The suggestions will only be fetched from network if the query meet this threshold (>=).
     * (local ones will be fetched regardless of the length)
     */
    private static final int THRESHOLD_NETWORK_SUGGESTION = 1;

    /**
     * How much time have to pass without emitting a item (i.e. the user stop typing)
     * to fetch/show the suggestions, in milliseconds.
     */
    private static final int SUGGESTIONS_DEBOUNCE = 120; //ms
    private final PublishSubject<String> suggestionPublisher = PublishSubject.create();

    @State
    protected int serviceId = Constants.NO_SERVICE_ID;

    // these three represents the current search query
    @State
    String searchString;

    List<FilterItem> selectedContentFilter = new ArrayList<>();

    List<FilterItem> selectedSortFilter = new ArrayList<>();

    // these represents the last search
    @State
    String lastSearchedString;

    @State
    String searchSuggestion;

    @State
    boolean isCorrectedSearch;

    @State
    MetaInfo[] metaInfo;

    @State
    boolean wasSearchFocused = false;

    private Page nextPage;
    private boolean showLocalSuggestions = true;
    private boolean showRemoteSuggestions = true;

    private Disposable searchDisposable;
    private Disposable suggestionDisposable;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private SuggestionListAdapter suggestionListAdapter;
    private HistoryRecordManager historyRecordManager;

    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    private FragmentSearchBinding searchBinding;

    protected View searchToolbarContainer;
    private EditText searchEditText;
    private View searchClear;

    private boolean suggestionsPanelVisible = false;

    /*////////////////////////////////////////////////////////////////////////*/

    /**
     * TextWatcher to remove rich-text formatting on the search EditText when pasting content
     * from the clipboard.
     */
    private TextWatcher textWatcher;

    @State
    ArrayList<Integer> userSelectedContentFilterList;

    @State
    ArrayList<Integer> userSelectedSortFilterList = null;

    protected SearchViewModel searchViewModel;
    protected SearchFilterLogic.Factory.Variant logicVariant =
            SearchFilterLogic.Factory.Variant.SEARCH_FILTER_LOGIC_DEFAULT;


    public static SearchFragment getInstance(final int serviceId, final String searchString) {
        final SearchFragment searchFragment;
        final App app = App.getApp();


        final String searchUi = PreferenceManager.getDefaultSharedPreferences(app)
                .getString(app.getString(R.string.search_filter_ui_key),
                        app.getString(R.string.search_filter_ui_value));
        if (app.getString(R.string.search_filter_ui_option_menu_legacy_key).equals(searchUi)) {
            searchFragment = new SearchFragmentLegacy();
        } else {
            searchFragment = new SearchFragment();
        }

        searchFragment.setQuery(serviceId, searchString);

        if (!TextUtils.isEmpty(searchString)) {
            searchFragment.setSearchOnResume();
        }

        return searchFragment;
    }

    /**
     * Set wasLoading to true so when the fragment onResume is called, the initial search is done.
     */
    private void setSearchOnResume() {
        wasLoading.set(true);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fragment's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        showLocalSuggestions = NewPipeSettings.showLocalSearchSuggestions(activity, prefs);
        showRemoteSuggestions = NewPipeSettings.showRemoteSearchSuggestions(activity, prefs);

        suggestionListAdapter = new SuggestionListAdapter();
        historyRecordManager = new HistoryRecordManager(context);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        if (userSelectedContentFilterList == null) {
            userSelectedContentFilterList = new ArrayList<>();
        }

        if (userSelectedSortFilterList == null) {
            userSelectedSortFilterList = new ArrayList<>();
        }

        initViewModel();

        // observe the content/sort filter items lists
        searchViewModel.getSelectedContentFilterItemListLiveData().observe(
                getViewLifecycleOwner(), filterItems -> selectedContentFilter = filterItems);
        searchViewModel.getSelectedSortFilterItemListLiveData().observe(
                getViewLifecycleOwner(), filterItems -> selectedSortFilter = filterItems);

        // the content/sort filters ids lists are only
        // observed here to store them via Icepick
        searchViewModel.getUserSelectedContentFilterListLiveData().observe(
                getViewLifecycleOwner(), filterIds -> userSelectedContentFilterList = filterIds);
        searchViewModel.getUserSelectedSortFilterListLiveData().observe(
                getViewLifecycleOwner(), filterIds -> userSelectedSortFilterList = filterIds);

        searchViewModel.getDoSearchLiveData().observe(
                getViewLifecycleOwner(), doSearch -> {
                    if (doSearch) {
                        selectedFilters(selectedContentFilter, selectedSortFilter);
                        searchViewModel.weConsumedDoSearchLiveData();
                    }
                });

        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    protected void initViewModel() {
        searchViewModel = new ViewModelProvider(this, SearchViewModel.Companion
                .getFactory(serviceId,
                        logicVariant,
                        userSelectedContentFilterList,
                        userSelectedSortFilterList))
                .get(SearchViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull final View rootView, final Bundle savedInstanceState) {
        searchBinding = FragmentSearchBinding.bind(rootView);
        super.onViewCreated(rootView, savedInstanceState);
        showSearchOnStart();
        initSearchListeners();
    }

    @Override
    public void onStart() {
        if (DEBUG) {
            Log.d(TAG, "onStart() called");
        }
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        wasSearchFocused = searchEditText.hasFocus();

        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }
        disposables.clear();
        hideKeyboardSearch();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, "onResume() called");
        }
        super.onResume();

        if (suggestionDisposable == null || suggestionDisposable.isDisposed()) {
            initSuggestionObserver();
        }

        if (!TextUtils.isEmpty(searchString)) {
            if (wasLoading.getAndSet(false)) {
                search(searchString);
                return;
            } else if (infoListAdapter.getItemsList().isEmpty()) {
                if (savedState == null) {
                    search(searchString);
                    return;
                } else if (!isLoading.get() && !wasSearchFocused && lastPanelError == null) {
                    infoListAdapter.clearStreamItemList();
                    showEmptyState();
                }
            }
        }

        handleSearchSuggestion();

        showMetaInfoInTextView(metaInfo == null ? null : Arrays.asList(metaInfo),
                searchBinding.searchMetaInfoTextView, searchBinding.searchMetaInfoSeparator,
                disposables);

        if (TextUtils.isEmpty(searchString) || wasSearchFocused) {
            showKeyboardSearch();
            showSuggestionsPanel();
        } else {
            hideKeyboardSearch();
            hideSuggestionsPanel();
        }
        wasSearchFocused = false;
    }

    @Override
    public void onDestroyView() {
        if (DEBUG) {
            Log.d(TAG, "onDestroyView() called");
        }
        unsetSearchListeners();

        searchBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }
        disposables.clear();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ReCaptchaActivity.RECAPTCHA_REQUEST) {
            if (resultCode == Activity.RESULT_OK
                    && !TextUtils.isEmpty(searchString)) {
                search(searchString);
            } else {
                Log.e(TAG, "ReCaptcha failed");
            }
        } else {
            Log.e(TAG, "Request code from activity not supported [" + requestCode + "]");
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        searchBinding.suggestionsList.setAdapter(suggestionListAdapter);
        // animations are just strange and useless, since the suggestions keep changing too much
        searchBinding.suggestionsList.setItemAnimator(null);
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull final RecyclerView recyclerView,
                                        @NonNull final RecyclerView.ViewHolder viewHolder) {
                return getSuggestionMovementFlags(viewHolder);
            }

            @Override
            public boolean onMove(@NonNull final RecyclerView recyclerView,
                                  @NonNull final RecyclerView.ViewHolder viewHolder,
                                  @NonNull final RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
                onSuggestionItemSwiped(viewHolder);
            }
        }).attachToRecyclerView(searchBinding.suggestionsList);

        searchToolbarContainer = activity.findViewById(R.id.toolbar_search_container);
        searchEditText = searchToolbarContainer.findViewById(R.id.toolbar_search_edit_text);
        searchClear = searchToolbarContainer.findViewById(R.id.toolbar_search_clear);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // State Saving
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void writeTo(final Queue<Object> objectsToSave) {
        super.writeTo(objectsToSave);
        objectsToSave.add(nextPage);
    }

    @Override
    public void readFrom(@NonNull final Queue<Object> savedObjects) throws Exception {
        super.readFrom(savedObjects);
        nextPage = (Page) savedObjects.poll();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle bundle) {
        searchString = searchEditText != null
                ? searchEditText.getText().toString()
                : searchString;

        super.onSaveInstanceState(bundle);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init's
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void reloadContent() {
        if (!TextUtils.isEmpty(searchString)
                || (searchEditText != null && !TextUtils.isEmpty(searchEditText.getText()))) {
            search(!TextUtils.isEmpty(searchString)
                    ? searchString
                    : searchEditText.getText().toString());
        } else {
            if (searchEditText != null) {
                searchEditText.setText("");
                showKeyboardSearch();
            }
            hideErrorPanel();
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        createMenu(menu, inflater);
    }

    protected void createMenu(@NonNull final Menu menu,
                              @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            hideKeyboardSearch();
            showSelectFiltersDialog();
            return false;
        }
        return true;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Search
    //////////////////////////////////////////////////////////////////////////*/

    private void showSearchOnStart() {
        if (DEBUG) {
            Log.d(TAG, "showSearchOnStart() called, searchQuery → "
                    + searchString
                    + ", lastSearchedQuery → "
                    + lastSearchedString);
        }
        searchEditText.setText(searchString);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            searchEditText.setHintTextColor(searchEditText.getTextColors().withAlpha(128));
        }

        if (TextUtils.isEmpty(searchString) || TextUtils.isEmpty(searchEditText.getText())) {
            searchToolbarContainer.setTranslationX(100);
            searchToolbarContainer.setAlpha(0.0f);
            searchToolbarContainer.setVisibility(View.VISIBLE);
            searchToolbarContainer.animate()
                    .translationX(0)
                    .alpha(1.0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator()).start();
        } else {
            searchToolbarContainer.setTranslationX(0);
            searchToolbarContainer.setAlpha(1.0f);
            searchToolbarContainer.setVisibility(View.VISIBLE);
        }
    }

    private void initSearchListeners() {
        if (DEBUG) {
            Log.d(TAG, "initSearchListeners() called");
        }
        searchClear.setOnClickListener(v -> {
            if (DEBUG) {
                Log.d(TAG, "onClick() called with: v = [" + v + "]");
            }
            if (TextUtils.isEmpty(searchEditText.getText())) {
                NavigationHelper.gotoMainFragment(getFM());
                return;
            }

            searchBinding.correctSuggestion.setVisibility(View.GONE);

            searchEditText.setText("");
            suggestionListAdapter.submitList(null);
            showKeyboardSearch();
        });

        TooltipCompat.setTooltipText(searchClear, getString(R.string.clear));

        searchEditText.setOnClickListener(v -> {
            if (DEBUG) {
                Log.d(TAG, "onClick() called with: v = [" + v + "]");
            }
            if ((showLocalSuggestions || showRemoteSuggestions) && !isErrorPanelVisible()) {
                showSuggestionsPanel();
            }
            if (DeviceUtils.isTv(getContext())) {
                showKeyboardSearch();
            }
        });

        searchEditText.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (DEBUG) {
                Log.d(TAG, "onFocusChange() called with: "
                        + "v = [" + v + "], hasFocus = [" + hasFocus + "]");
            }
            if ((showLocalSuggestions || showRemoteSuggestions)
                    && hasFocus && !isErrorPanelVisible()) {
                showSuggestionsPanel();
            }
        });

        suggestionListAdapter.setListener(new SuggestionListAdapter.OnSuggestionItemSelected() {
            @Override
            public void onSuggestionItemSelected(final SuggestionItem item) {
                search(item.query);
                searchEditText.setText(item.query);
            }

            @Override
            public void onSuggestionItemInserted(final SuggestionItem item) {
                searchEditText.setText(item.query);
                searchEditText.setSelection(searchEditText.getText().length());
            }

            @Override
            public void onSuggestionItemLongClick(final SuggestionItem item) {
                if (item.fromHistory) {
                    showDeleteSuggestionDialog(item);
                }
            }
        });

        if (textWatcher != null) {
            searchEditText.removeTextChangedListener(textWatcher);
        }
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                                          final int count, final int after) {
                // Do nothing, old text is already clean
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                                      final int before, final int count) {
                // Changes are handled in afterTextChanged; CharSequence cannot be changed here.
            }

            @Override
            public void afterTextChanged(final Editable s) {
                // Remove rich text formatting
                for (final CharacterStyle span : s.getSpans(0, s.length(), CharacterStyle.class)) {
                    s.removeSpan(span);
                }

                final String newText = searchEditText.getText().toString();
                suggestionPublisher.onNext(newText);
            }
        };
        searchEditText.addTextChangedListener(textWatcher);
        searchEditText.setOnEditorActionListener(
                (TextView v, int actionId, KeyEvent event) -> {
                    if (DEBUG) {
                        Log.d(TAG, "onEditorAction() called with: v = [" + v + "], "
                                + "actionId = [" + actionId + "], event = [" + event + "]");
                    }
                    if (actionId == EditorInfo.IME_ACTION_PREVIOUS) {
                        hideKeyboardSearch();
                    } else if (event != null
                            && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            || event.getAction() == EditorInfo.IME_ACTION_SEARCH)) {
                        search(searchEditText.getText().toString());
                        return true;
                    }
                    return false;
                });

        if (suggestionDisposable == null || suggestionDisposable.isDisposed()) {
            initSuggestionObserver();
        }
    }

    private void unsetSearchListeners() {
        if (DEBUG) {
            Log.d(TAG, "unsetSearchListeners() called");
        }
        searchClear.setOnClickListener(null);
        searchClear.setOnLongClickListener(null);
        searchEditText.setOnClickListener(null);
        searchEditText.setOnFocusChangeListener(null);
        searchEditText.setOnEditorActionListener(null);

        if (textWatcher != null) {
            searchEditText.removeTextChangedListener(textWatcher);
        }
        textWatcher = null;
    }

    private void showSuggestionsPanel() {
        if (DEBUG) {
            Log.d(TAG, "showSuggestionsPanel() called");
        }
        suggestionsPanelVisible = true;
        animate(searchBinding.suggestionsPanel, true, 200,
                AnimationType.LIGHT_SLIDE_AND_ALPHA);
    }

    private void hideSuggestionsPanel() {
        if (DEBUG) {
            Log.d(TAG, "hideSuggestionsPanel() called");
        }
        suggestionsPanelVisible = false;
        animate(searchBinding.suggestionsPanel, false, 200,
                AnimationType.LIGHT_SLIDE_AND_ALPHA);
    }

    private void showKeyboardSearch() {
        if (DEBUG) {
            Log.d(TAG, "showKeyboardSearch() called");
        }
        KeyboardUtil.showKeyboard(activity, searchEditText);
    }

    protected void hideKeyboardSearch() {
        if (DEBUG) {
            Log.d(TAG, "hideKeyboardSearch() called");
        }

        KeyboardUtil.hideKeyboard(activity, searchEditText);
    }

    private void showDeleteSuggestionDialog(final SuggestionItem item) {
        if (activity == null || historyRecordManager == null || searchEditText == null) {
            return;
        }
        final String query = item.query;
        new AlertDialog.Builder(activity)
                .setTitle(query)
                .setMessage(R.string.delete_item_search_history)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    final Disposable onDelete = historyRecordManager.deleteSearchHistory(query)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    howManyDeleted -> suggestionPublisher
                                            .onNext(searchEditText.getText().toString()),
                                    throwable -> showSnackBarError(new ErrorInfo(throwable,
                                            UserAction.DELETE_FROM_HISTORY,
                                            "Deleting item failed")));
                    disposables.add(onDelete);
                })
                .show();
    }

    @Override
    public boolean onBackPressed() {
        if (suggestionsPanelVisible
                && !infoListAdapter.getItemsList().isEmpty()
                && !isLoading.get()) {
            hideSuggestionsPanel();
            hideKeyboardSearch();
            searchEditText.setText(lastSearchedString);
            return true;
        }
        return false;
    }


    private Observable<List<SuggestionItem>> getLocalSuggestionsObservable(
            final String query, final int similarQueryLimit) {
        return historyRecordManager
                .getRelatedSearches(query, similarQueryLimit, 25)
                .toObservable()
                .map(searchHistoryEntries ->
                    searchHistoryEntries.stream()
                            .map(entry -> new SuggestionItem(true, entry))
                            .collect(Collectors.toList()));
    }

    private Observable<List<SuggestionItem>> getRemoteSuggestionsObservable(final String query) {
        return ExtractorHelper
                .suggestionsFor(serviceId, query)
                .toObservable()
                .map(strings -> {
                    final List<SuggestionItem> result = new ArrayList<>();
                    for (final String entry : strings) {
                        result.add(new SuggestionItem(false, entry));
                    }
                    return result;
                });
    }

    private void initSuggestionObserver() {
        if (DEBUG) {
            Log.d(TAG, "initSuggestionObserver() called");
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }

        suggestionDisposable = suggestionPublisher
                .debounce(SUGGESTIONS_DEBOUNCE, TimeUnit.MILLISECONDS)
                .startWithItem(searchString == null ? "" : searchString)
                .switchMap(query -> {
                    // Only show remote suggestions if they are enabled in settings and
                    // the query length is at least THRESHOLD_NETWORK_SUGGESTION
                    final boolean shallShowRemoteSuggestionsNow = showRemoteSuggestions
                            && query.length() >= THRESHOLD_NETWORK_SUGGESTION;

                    if (showLocalSuggestions && shallShowRemoteSuggestionsNow) {
                        return Observable.zip(
                                getLocalSuggestionsObservable(query, 3),
                                getRemoteSuggestionsObservable(query),
                                (local, remote) -> {
                                    remote.removeIf(remoteItem -> local.stream().anyMatch(
                                            localItem -> localItem.equals(remoteItem)));
                                    local.addAll(remote);
                                    return local;
                                })
                                .materialize();
                    } else if (showLocalSuggestions) {
                        return getLocalSuggestionsObservable(query, 25)
                                .materialize();
                    } else if (shallShowRemoteSuggestionsNow) {
                        return getRemoteSuggestionsObservable(query)
                                .materialize();
                    } else {
                        return Single.fromCallable(Collections::<SuggestionItem>emptyList)
                                .toObservable()
                                .materialize();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listNotification -> {
                            if (listNotification.isOnNext()) {
                                if (listNotification.getValue() != null) {
                                    handleSuggestions(listNotification.getValue());
                                }
                            } else if (listNotification.isOnError()
                                    && listNotification.getError() != null
                                    && !ExceptionUtils.isInterruptedCaused(
                                            listNotification.getError())) {
                                showSnackBarError(new ErrorInfo(listNotification.getError(),
                                        UserAction.GET_SUGGESTIONS, searchString, serviceId));
                            }
                        }, throwable -> showSnackBarError(new ErrorInfo(
                            throwable, UserAction.GET_SUGGESTIONS, searchString, serviceId)));
    }

    @Override
    protected void doInitialLoadLogic() {
        // no-op
    }

    private void search(final String theSearchString) {
        if (DEBUG) {
            Log.d(TAG, "search() called with: query = [" + theSearchString + "]");
        }
        if (theSearchString.isEmpty()) {
            return;
        }

        try {
            final StreamingService streamingService = NewPipe.getServiceByUrl(theSearchString);
            if (streamingService != null) {
                showLoading();
                disposables.add(Observable
                        .fromCallable(() -> NavigationHelper.getIntentByLink(activity,
                                streamingService, theSearchString))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(intent -> {
                            getFM().popBackStackImmediate();
                            activity.startActivity(intent);
                        }, throwable -> showTextError(getString(R.string.unsupported_url))));
                return;
            }
        } catch (final Exception ignored) {
            // Exception occurred, it's not a url
        }

        lastSearchedString = this.searchString;
        this.searchString = theSearchString;
        infoListAdapter.clearStreamItemList();
        hideSuggestionsPanel();
        showMetaInfoInTextView(null, searchBinding.searchMetaInfoTextView,
                searchBinding.searchMetaInfoSeparator, disposables);
        hideKeyboardSearch();

        disposables.add(historyRecordManager.onSearched(serviceId, theSearchString)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ignored -> { },
                        throwable -> showSnackBarError(new ErrorInfo(throwable, UserAction.SEARCHED,
                                theSearchString, serviceId))
                ));
        suggestionPublisher.onNext(theSearchString);
        startLoading(false);
    }

    @Override
    public void startLoading(final boolean forceLoad) {
        super.startLoading(forceLoad);
        disposables.clear();
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        searchDisposable = ExtractorHelper.searchFor(serviceId,
                searchString,
                selectedContentFilter,
                selectedSortFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((searchResult, throwable) -> isLoading.set(false))
                .subscribe(this::handleResult, this::onItemError);
    }

    @Override
    protected void loadMoreItems() {
        if (!Page.isValid(nextPage)) {
            return;
        }
        isLoading.set(true);
        showListFooter(true);
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        searchDisposable = ExtractorHelper.getMoreSearchItems(
                serviceId,
                searchString,
                selectedContentFilter,
                selectedSortFilter,
                nextPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((nextItemsResult, throwable) -> isLoading.set(false))
                .subscribe(this::handleNextItems, this::onItemError);
    }

    @Override
    protected boolean hasMoreItems() {
        return Page.isValid(nextPage);
    }

    @Override
    protected void onItemSelected(final InfoItem selectedItem) {
        super.onItemSelected(selectedItem);
        hideKeyboardSearch();
    }

    private void onItemError(final Throwable exception) {
        if (exception instanceof SearchExtractor.NothingFoundException) {
            infoListAdapter.clearStreamItemList();
            showEmptyState();
        } else {
            showError(new ErrorInfo(exception, UserAction.SEARCHED, searchString, serviceId));
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public void selectedFilters(@NonNull final List<FilterItem> theSelectedContentFilter,
                                @NonNull final List<FilterItem> theSelectedSortFilter) {

        selectedContentFilter = theSelectedContentFilter;
        selectedSortFilter = theSelectedSortFilter;

        if (!TextUtils.isEmpty(searchString)) {
            search(searchString);
        }
    }

    private void setQuery(final int theServiceId,
                          final String theSearchString) {
        serviceId = theServiceId;
        searchString = theSearchString;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Suggestion Results
    //////////////////////////////////////////////////////////////////////////*/

    public void handleSuggestions(@NonNull final List<SuggestionItem> suggestions) {
        if (DEBUG) {
            Log.d(TAG, "handleSuggestions() called with: suggestions = [" + suggestions + "]");
        }
        suggestionListAdapter.submitList(suggestions,
                () -> searchBinding.suggestionsList.scrollToPosition(0));

        if (suggestionsPanelVisible && isErrorPanelVisible()) {
            hideLoading();
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void hideLoading() {
        super.hideLoading();
        showListFooter(false);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Search Results
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void handleResult(@NonNull final SearchInfo result) {
        final List<Throwable> exceptions = result.getErrors();
        if (!exceptions.isEmpty()
                && !(exceptions.size() == 1
                && exceptions.get(0) instanceof SearchExtractor.NothingFoundException)) {
            showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                    searchString, serviceId));
        }

        searchSuggestion = result.getSearchSuggestion();
        isCorrectedSearch = result.isCorrectedSearch();

        // List<MetaInfo> cannot be bundled without creating some containers
        metaInfo = result.getMetaInfo().toArray(new MetaInfo[0]);
        showMetaInfoInTextView(result.getMetaInfo(), searchBinding.searchMetaInfoTextView,
                searchBinding.searchMetaInfoSeparator, disposables);

        handleSearchSuggestion();

        lastSearchedString = searchString;
        nextPage = result.getNextPage();

        if (infoListAdapter.getItemsList().isEmpty()) {
            if (!result.getRelatedItems().isEmpty()) {
                infoListAdapter.addInfoItemList(result.getRelatedItems());
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
                return;
            }
        }

        super.handleResult(result);
    }

    private void handleSearchSuggestion() {
        if (TextUtils.isEmpty(searchSuggestion)) {
            searchBinding.correctSuggestion.setVisibility(View.GONE);
        } else {
            final String helperText = getString(isCorrectedSearch
                    ? R.string.search_showing_result_for
                    : R.string.did_you_mean);

            final String highlightedSearchSuggestion =
                    "<b><i>" + Html.escapeHtml(searchSuggestion) + "</i></b>";
            final String text = String.format(helperText, highlightedSearchSuggestion);
            searchBinding.correctSuggestion.setText(HtmlCompat.fromHtml(text,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            searchBinding.correctSuggestion.setOnClickListener(v -> {
                searchBinding.correctSuggestion.setVisibility(View.GONE);
                search(searchSuggestion);
                searchEditText.setText(searchSuggestion);
            });

            searchBinding.correctSuggestion.setOnLongClickListener(v -> {
                searchEditText.setText(searchSuggestion);
                searchEditText.setSelection(searchSuggestion.length());
                showKeyboardSearch();
                return true;
            });

            searchBinding.correctSuggestion.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void handleNextItems(final ListExtractor.InfoItemsPage<?> result) {
        showListFooter(false);
        infoListAdapter.addInfoItemList(result.getItems());
        nextPage = result.getNextPage();

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                    "\"" + searchString + "\" → pageUrl: " + nextPage.getUrl() + ", "
                            + "pageIds: " + nextPage.getIds() + ", "
                            + "pageCookies: " + nextPage.getCookies(),
                    serviceId));
        }
        super.handleNextItems(result);
    }

    @Override
    public void handleError() {
        super.handleError();
        hideSuggestionsPanel();
        hideKeyboardSearch();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Suggestion item touch helper
    //////////////////////////////////////////////////////////////////////////*/

    public int getSuggestionMovementFlags(@NonNull final RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return 0;
        }

        final SuggestionItem item = suggestionListAdapter.getCurrentList().get(position);
        return item.fromHistory ? makeMovementFlags(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
    }

    public void onSuggestionItemSwiped(@NonNull final RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getBindingAdapterPosition();
        final String query = suggestionListAdapter.getCurrentList().get(position).query;
        final Disposable onDelete = historyRecordManager.deleteSearchHistory(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        howManyDeleted -> suggestionPublisher
                                .onNext(searchEditText.getText().toString()),
                        throwable -> showSnackBarError(new ErrorInfo(throwable,
                                UserAction.DELETE_FROM_HISTORY, "Deleting item failed")));
        disposables.add(onDelete);
    }

    private void showSelectFiltersDialog() {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final DialogFragment searchFilterUiDialog;

        final String searchUi = PreferenceManager.getDefaultSharedPreferences(App.getApp())
                .getString(getString(R.string.search_filter_ui_key),
                        getString(R.string.search_filter_ui_value));
        if (getString(R.string.search_filter_ui_option_menu_style_key).equals(searchUi)) {
            searchFilterUiDialog = new SearchFilterOptionMenuAlikeDialogFragment();
        } else if (getString(R.string.search_filter_ui_chip_dialog_key).equals(searchUi)) {
            searchFilterUiDialog = new SearchFilterChipDialogFragment();
        } else { // default dialog
            searchFilterUiDialog = new SearchFilterDialogFragment();
        }

        searchFilterUiDialog.show(fragmentManager, "fragment_search");
    }
}
