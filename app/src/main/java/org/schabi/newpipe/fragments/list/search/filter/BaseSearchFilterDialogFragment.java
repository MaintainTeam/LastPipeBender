// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import icepick.Icepick;
import icepick.State;

/**
 * Base dialog class for {@link DialogFragment} based search filter dialogs.
 */
public abstract class BaseSearchFilterDialogFragment extends DialogFragment {

    private static final String CONTENT_FILTERS = "CONTENT_FILTERS";
    private static final String SORT_FILTERS = "SORT_FILTERS";
    private static final String SERVICE_ID = "SERVICE_ID";

    @State
    public ArrayList<Integer> userSelectedContentFilterList;
    protected List<FilterItem> selectedContentFilters;
    protected List<FilterItem> selectedSortFilters;
    protected BaseSearchFilterUiGenerator dialogGenerator;
    @State
    ArrayList<Integer> userSelectedSortFilterList = null;

    protected static DialogFragment initDialogArguments(
            final DialogFragment dialogFragment,
            final int serviceId,
            final ArrayList<Integer> userSelectedContentFilter,
            final ArrayList<Integer> userSelectedSortFilter) {
        final Bundle bundle = new Bundle(1);
        bundle.putInt(SERVICE_ID, serviceId);
        bundle.putIntegerArrayList(CONTENT_FILTERS, userSelectedContentFilter);
        bundle.putIntegerArrayList(SORT_FILTERS, userSelectedSortFilter);
        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    private void initializeFilterData() {

        assert getArguments() != null;
        final int serviceId = getArguments().getInt(SERVICE_ID);
        final ArrayList<Integer> contentFilters =
                getArguments().getIntegerArrayList(CONTENT_FILTERS);
        final ArrayList<Integer> sortFilters =
                getArguments().getIntegerArrayList(SORT_FILTERS);

        final StreamingService service;
        try {
            service = NewPipe.getService(serviceId);
        } catch (final ExtractionException e) {
            throw new RuntimeException(e);
        }

        dialogGenerator = createSearchFilterDialogGenerator(service,
                (userSelectedContentFilter, userSelectedSortFilter) -> {
                    selectedContentFilters = userSelectedContentFilter;
                    selectedSortFilters = userSelectedSortFilter;
                    sendDataToParentFragment();
                });

        userSelectedContentFilterList = contentFilters;
        userSelectedSortFilterList = sortFilters;

        dialogGenerator.restorePreviouslySelectedFilters(
                userSelectedContentFilterList,
                userSelectedSortFilterList);

        dialogGenerator.createSearchUI();
    }

    protected abstract BaseSearchFilterUiGenerator createSearchFilterDialogGenerator(
            StreamingService service,
            SearchFilterLogic.Callback callback);

    /**
     * As we have different bindings we need to get this sorted in a method.
     *
     * @return the {@link Toolbar}
     */
    protected abstract Toolbar getToolbar();

    protected abstract View getRootView(LayoutInflater inflater,
                                        ViewGroup container);

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = getRootView(inflater, container);
        initializeFilterData();
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        dialogGenerator.onResume();
    }

    @Override
    public void onStop() {
        dialogGenerator.onPause();
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        initToolbar(getToolbar());
    }

    protected void initToolbar(final Toolbar toolbar) {
        toolbar.setTitle(R.string.filter);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.inflateMenu(R.menu.menu_search_filter_dialog_fragment);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setNavigationContentDescription(R.string.cancel);

        final View okButton = toolbar.findViewById(R.id.search);
        okButton.setEnabled(true);

        final View resetButton = toolbar.findViewById(R.id.reset);
        resetButton.setEnabled(true);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.search) {
                dialogGenerator.prepareForSearch();
                return true;
            } else if (item.getItemId() == R.id.reset) {
                dialogGenerator.reset();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        // get data to save its state via Icepick
        userSelectedContentFilterList = dialogGenerator.getSelectedContentFilters();
        userSelectedSortFilterList = dialogGenerator.getSelectedSortFilters();

        Icepick.saveInstanceState(this, outState);
    }

    private void sendDataToParentFragment() {
        final Listener listener = (Listener) getTargetFragment();
        if (listener != null) {
            listener.onFinishSearchFilterDialog(
                    userSelectedContentFilterList, userSelectedSortFilterList,
                    selectedContentFilters, selectedSortFilters);
        }
        dismiss();
    }

    /**
     * Listener to be implemented by the parent Fragment so it can receive data.
     */
    public interface Listener {

        void onFinishSearchFilterDialog(ArrayList<Integer> userSelectedContentFilterList,
                                        ArrayList<Integer> userSelectedSortFilterList,
                                        List<FilterItem> selectedContentFilters,
                                        List<FilterItem> selectedSortFilters);
    }
}
