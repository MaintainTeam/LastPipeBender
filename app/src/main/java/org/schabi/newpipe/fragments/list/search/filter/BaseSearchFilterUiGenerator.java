// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;


import android.content.Context;
import android.util.TypedValue;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

/**
 * Base for any search filter UI.
 * <p>
 * It extends SearchFilterLogic and is used as a base class to implement the UI interface
 * for content and sort filter dialogs eg. {@link SearchFilterDialogGenerator}
 * or {@link SearchFilterOptionMenuAlikeDialogGenerator}.
 */
public abstract class BaseSearchFilterUiGenerator extends SearchFilterLogic {
    protected final ICreateUiForFiltersWorker contentFilterWorker;
    protected final ICreateUiForFiltersWorker sortFilterWorker;
    protected final Context context;

    protected BaseSearchFilterUiGenerator(final SearchQueryHandlerFactory linkHandlerFactory,
                                          final Callback callback,
                                          final Context context) {
        super(linkHandlerFactory, callback);
        this.context = context;
        this.contentFilterWorker = createContentFilterWorker();
        this.sortFilterWorker = createSortFilterWorker();
    }

    /**
     * {@link ICreateUiForFiltersWorker}.
     *
     * @return the class that implements the UI for the content filters.
     */
    protected abstract ICreateUiForFiltersWorker createContentFilterWorker();

    /**
     * {@link ICreateUiForFiltersWorker}.
     *
     * @return the class that implements the UI for the sort filters.
     */
    protected abstract ICreateUiForFiltersWorker createSortFilterWorker();

    protected int getSeparatorLineColorFromTheme() {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    /**
     * Create the complete UI for the search filter dialog and make sure the initial
     * visibility of the UI elements is done.
     */
    public void createSearchUI() {
        initContentFiltersUi(contentFilterWorker);
        initSortFiltersUi(sortFilterWorker);
        doMeasurementsIfNeeded();
        // make sure that only sort filters relevant to the selected content filter are shown
        showSortFilterContainerUI();
    }

    protected void doMeasurementsIfNeeded() {
        // nothing to measure here, if you want to measure something override this method
    }

    /**
     * If UI is implemented within an fragment/activity this method has to be called from
     * its corresponding lifecyle method manually.
     */
    public abstract void onResume();

    /**
     * If UI is implemented within an fragment/activity this method has to be called from
     * its corresponding lifecyle method manually.
     */
    public abstract void onPause();

    /**
     * Helper interface used as 'function pointer'.
     */
    protected interface UiWrapperMapDelegate {
        void put(int identifier, IUiItemWrapper menuItemUiWrapper);
    }

    /**
     * Helper interface used as 'function pointer'.
     */
    protected interface UiSelectorDelegate {
        void selectFilter(int identifier);
    }
}
