// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterUIOptionMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import icepick.State;

/**
 * Fragment that hosts the action menu based filter 'dialog'.
 * <p>
 * Called ..Legacy because this was the way NewPipe had implemented the search filter dialog.
 * <p>
 * The new UI's are handled by {@link SearchFragment} and implemented by
 * using {@link androidx.fragment.app.DialogFragment}.
 */
public class SearchFragmentLegacy extends SearchFragment {

    @State
    protected int countOnPrepareOptionsMenuCalls = 0;
    private SearchFilterUIOptionMenu searchFilterUi;

    @Override
    protected void initializeFilterData() {
        try {
            final StreamingService service = NewPipe.getService(serviceId);

            searchFilterUi = new SearchFilterUIOptionMenu(service, this, requireContext());
            searchFilterUi.restorePreviouslySelectedFilters(
                    userSelectedContentFilterList,
                    userSelectedSortFilterList);

            userSelectedContentFilterList = searchFilterUi.getSelectedContentFilters();
            userSelectedSortFilterList = searchFilterUi.getSelectedSortFilters();
            selectedContentFilter = searchFilterUi.getSelectedContentFilterItems();
            selectedSortFilter = searchFilterUi.getSelectedSortFiltersItems();
        } catch (final ExtractionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle bundle) {
        // get data to save its state via Icepick
        userSelectedContentFilterList = searchFilterUi.getSelectedContentFilters();
        userSelectedSortFilterList = searchFilterUi.getSelectedSortFilters();

        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void createMenu(@NonNull final Menu menu,
                              @NonNull final MenuInflater inflater) {
        searchFilterUi.createSearchUI(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return searchFilterUi.onOptionsItemSelected(item);
    }

    @Override
    protected void initViews(final View rootView,
                             final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);
        final Toolbar toolbar = (Toolbar) searchToolbarContainer.getParent();
        toolbar.setOverflowIcon(ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_sort));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // workaround: we want to hide the keyboard in case we open the options
        // menu. As somehow this method gets triggered twice but only the 2nd
        // time is relevant as the options menu is selected by the user.
        if (++countOnPrepareOptionsMenuCalls > 1) {
            hideKeyboardSearch();
        }
    }
}
