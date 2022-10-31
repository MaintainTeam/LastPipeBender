// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.util.ServiceHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.view.ContextThemeWrapper;

public class SearchFilterDialogGenerator extends BaseSearchFilterUiDialogGenerator {
    private final GridLayout gridLayout;
    private final Map<Spinner, AdapterView.OnItemSelectedListener> spinners = new HashMap<>();

    public SearchFilterDialogGenerator(final StreamingService service,
                                       final ViewGroup root,
                                       final Context context,
                                       final SearchFilterLogic.Callback callback) {
        super(service.getSearchQHFactory(), callback, context);
        this.gridLayout = createGridLayout();
        root.addView(gridLayout);
    }

    @Override
    public void onResume() {
        for (final Map.Entry<Spinner, AdapterView.OnItemSelectedListener> spinner
                : spinners.entrySet()) {
            spinner.getKey().setOnItemSelectedListener(spinner.getValue());
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        for (final Map.Entry<Spinner, AdapterView.OnItemSelectedListener> spinner
                : spinners.entrySet()) {
            spinner.getKey().setOnItemSelectedListener(null);
        }
        super.onPause();
    }

    @Override
    protected void createTitle(final String name,
                               final List<View> titleViewElements) {
        final TextView titleView = createTitleText(name);
        final View separatorLine = createSeparatorLine();
        final View separatorLine2 = createSeparatorLine();

        gridLayout.addView(separatorLine);
        gridLayout.addView(titleView);
        gridLayout.addView(separatorLine2);

        titleViewElements.add(titleView);
        titleViewElements.add(separatorLine);
        titleViewElements.add(separatorLine2);
    }

    @Override
    protected void createFilterGroup(final FilterGroup filterGroup,
                                     final UiWrapperMapDelegate wrapperDelegate,
                                     final UiSelectorDelegate selectorDelegate) {
        final GridLayout.LayoutParams layoutParams = getLayoutParamsViews();
        boolean doSpanDataOverMultipleCells = false;
        final UiItemWrapperViews viewsWrapper = new UiItemWrapperViews(
                filterGroup.getIdentifier(), filterGroup.getIdentifier());

        if (filterGroup.getNameId() != null) {
            final TextView filterLabel = new TextView(context);

            filterLabel.setId(filterGroup.getIdentifier());
            filterLabel.setText(
                    ServiceHelper.getTranslatedFilterString(filterGroup.getNameId(), context));
            filterLabel.setGravity(Gravity.CENTER_VERTICAL);
            setDefaultMargin(layoutParams);
            setZeroPadding(filterLabel);

            filterLabel.setLayoutParams(layoutParams);
            gridLayout.addView(filterLabel);
            viewsWrapper.add(filterLabel);
        } else {
            doSpanDataOverMultipleCells = true;
        }

        if (filterGroup.isOnlyOneCheckable()) {

            final Spinner filterDataSpinner = new Spinner(context, Spinner.MODE_DROPDOWN);

            final GridLayout.LayoutParams spinnerLp =
                    clipFreeRightColumnLayoutParams(doSpanDataOverMultipleCells);
            setDefaultMargin(spinnerLp);
            filterDataSpinner.setLayoutParams(spinnerLp);
            setZeroPadding(filterDataSpinner);

            createUiElementsForSingleSelectableItemsFilterGroup(
                    filterGroup, wrapperDelegate, selectorDelegate, filterDataSpinner);

            viewsWrapper.add(filterDataSpinner);
            gridLayout.addView(filterDataSpinner);

        } else { // multiple items in FilterGroup selectable
            final ChipGroup chipGroup = new ChipGroup(context);
            viewsWrapper.add(chipGroup);
            gridLayout.addView(chipGroup);
            chipGroup.setLayoutParams(
                    clipFreeRightColumnLayoutParams(doSpanDataOverMultipleCells));
            chipGroup.setSingleLine(false);

            createUiElementsForMultipleSelectableItemsFilterGroup(
                    filterGroup, wrapperDelegate, selectorDelegate, chipGroup);
        }

        wrapperDelegate.put(filterGroup.getIdentifier(), viewsWrapper);
    }

    private void createUiElementsForSingleSelectableItemsFilterGroup(
            final FilterGroup filterGroup,
            final UiWrapperMapDelegate wrapperDelegate,
            final UiSelectorDelegate selectorDelegate,
            final Spinner filterDataSpinner) {
        filterDataSpinner.setAdapter(new SearchFilterDialogSpinnerAdapter(
                context, filterGroup, wrapperDelegate, filterDataSpinner));

        final AdapterView.OnItemSelectedListener listener;
        listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view,
                                       final int position, final long id) {
                if (view != null) {
                    selectorDelegate.selectFilter(view.getId());
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // we are only interested onItemSelected() -> no implementation here
            }
        };

        filterDataSpinner.setOnItemSelectedListener(listener);
        spinners.put(filterDataSpinner, listener);
    }

    private void createUiElementsForMultipleSelectableItemsFilterGroup(
            final FilterGroup filterGroup,
            final UiWrapperMapDelegate wrapperDelegate,
            final UiSelectorDelegate selectorDelegate,
            final ChipGroup chipGroup) {
        for (final FilterItem item : filterGroup.getFilterItems()) {
            final Chip chip = new Chip(new ContextThemeWrapper(
                    context, R.style.Theme_MaterialComponents_Light));
            chip.setText(ServiceHelper.getTranslatedFilterString(item.getNameId(), context));
            chip.setId(item.getIdentifier());
            chip.setCheckable(true);
            final View.OnClickListener listener;
            listener = view -> selectorDelegate.selectFilter(view.getId());

            chip.setOnClickListener(listener);
            chipGroup.addView(chip);
            viewListeners.put(chip, listener);
            wrapperDelegate.put(item.getIdentifier(), new UiItemWrapperChip(
                    item, chip, filterGroup.getIdentifier(), chipGroup));
        }
    }

    private View createSeparatorLine() {
        return createSeparatorLine(clipFreeRightColumnLayoutParams(true));
    }

    private TextView createTitleText(final String name) {
        final TextView title = createTitleText(name,
                clipFreeRightColumnLayoutParams(true));
        title.setGravity(Gravity.CENTER);
        return title;
    }

    private GridLayout createGridLayout() {
        final GridLayout layout = new GridLayout(context);

        layout.setColumnCount(2);

        final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setDefaultMargin(layoutParams);
        layout.setLayoutParams(layoutParams);

        return layout;
    }

    private GridLayout.LayoutParams clipFreeRightColumnLayoutParams(final boolean doColumnSpan) {
        final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        // https://stackoverflow.com/questions/37744672/gridlayout-children-are-being-clipped
        layoutParams.width = 0;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.setGravity(Gravity.FILL_HORIZONTAL | Gravity.CENTER_VERTICAL);
        setDefaultMargin(layoutParams);

        if (doColumnSpan) {
            layoutParams.columnSpec = GridLayout.spec(0, 2, 1.0f);
        }

        return layoutParams;
    }

    private GridLayout.LayoutParams getLayoutParamsViews() {
        final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.setGravity(Gravity.TOP);
        setDefaultMargin(layoutParams);
        return layoutParams;
    }

    private GridLayout.LayoutParams setDefaultMargin(final GridLayout.LayoutParams layoutParams) {
        layoutParams.setMargins(
                (int) dipToPixels(context, 4),
                (int) dipToPixels(context, 2),
                (int) dipToPixels(context, 4),
                (int) dipToPixels(context, 2)
        );
        return layoutParams;
    }

    private View setZeroPadding(final View view) {
        view.setPadding(0, 0, 0, 0);
        return view;
    }

    public static class UiItemWrapperChip extends BaseUiItemWrapper {

        private final ChipGroup chipGroup;

        public UiItemWrapperChip(final FilterItem item, final View view,
                                 final int groupId, final ChipGroup chipGroup) {
            super(item, groupId, view);
            this.chipGroup = chipGroup;
        }

        @Override
        public boolean isChecked() {
            return ((Chip) view).isChecked();
        }

        @Override
        public void setChecked(final boolean checked) {
            view.setSelected(checked);
            ((Chip) view).setChecked(checked);

            if (checked) {
                chipGroup.check(view.getId());
            }
        }
    }
}
