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
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.util.DeviceUtils;
import org.schabi.newpipe.util.ServiceHelper;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;

public class SearchFilterDialogGenerator extends BaseSearchFilterUiDialogGenerator {
    private final GridLayout globalLayout;

    public SearchFilterDialogGenerator(
            @NonNull final SearchFilterLogic logic,
            @NonNull final ViewGroup root,
            @NonNull final Context context) {
        super(logic, context);
        this.globalLayout = createGridLayout();
        root.addView(globalLayout);
    }

    @Override
    protected void createTitle(@NonNull final String name,
                               @NonNull final List<View> titleViewElements) {
        final TextView titleView = createTitleText(name);
        final View separatorLine = createSeparatorLine();
        final View separatorLine2 = createSeparatorLine();

        globalLayout.addView(separatorLine);
        globalLayout.addView(titleView);
        globalLayout.addView(separatorLine2);

        titleViewElements.add(titleView);
        titleViewElements.add(separatorLine);
        titleViewElements.add(separatorLine2);
    }

    @Override
    protected void createFilterGroup(@NonNull final FilterGroup filterGroup,
                                     @NonNull final UiWrapperMapDelegate wrapperDelegate,
                                     @NonNull final UiSelectorDelegate selectorDelegate) {
        final GridLayout.LayoutParams layoutParams = getLayoutParamsViews();
        boolean doSpanDataOverMultipleCells = false;
        final UiItemWrapperViews viewsWrapper = new UiItemWrapperViews(
                filterGroup.getIdentifier());

        if (filterGroup.getNameId() != null) {
            final TextView filterLabel = new TextView(context);

            filterLabel.setId(filterGroup.getIdentifier());
            filterLabel.setText(
                    ServiceHelper.getTranslatedFilterString(filterGroup.getNameId(), context));
            filterLabel.setGravity(Gravity.CENTER_VERTICAL);
            setDefaultMargin(layoutParams);
            setZeroPadding(filterLabel);

            filterLabel.setLayoutParams(layoutParams);
            globalLayout.addView(filterLabel);
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
            globalLayout.addView(filterDataSpinner);

        } else { // multiple items in FilterGroup selectable
            final ChipGroup chipGroup = new ChipGroup(context);
            viewsWrapper.add(chipGroup);
            globalLayout.addView(chipGroup);
            chipGroup.setLayoutParams(
                    clipFreeRightColumnLayoutParams(doSpanDataOverMultipleCells));
            chipGroup.setSingleLine(false);

            createUiElementsForMultipleSelectableItemsFilterGroup(
                    filterGroup, wrapperDelegate, selectorDelegate, chipGroup);
        }

        wrapperDelegate.put(filterGroup.getIdentifier(), viewsWrapper);
    }

    private void createUiElementsForSingleSelectableItemsFilterGroup(
            @NonNull final FilterGroup filterGroup,
            @NonNull final UiWrapperMapDelegate wrapperDelegate,
            @NonNull final UiSelectorDelegate selectorDelegate,
            @NonNull final Spinner filterDataSpinner) {
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
    }

    private void createUiElementsForMultipleSelectableItemsFilterGroup(
            @NonNull final FilterGroup filterGroup,
            @NonNull final UiWrapperMapDelegate wrapperDelegate,
            @NonNull final UiSelectorDelegate selectorDelegate,
            @NonNull final ChipGroup chipGroup) {
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
            wrapperDelegate.put(item.getIdentifier(), new UiItemWrapperChip(
                    item, chip, chipGroup));
        }
    }

    @NonNull
    private View createSeparatorLine() {
        return createSeparatorLine(clipFreeRightColumnLayoutParams(true));
    }

    @NonNull
    private TextView createTitleText(final String name) {
        final TextView title = createTitleText(name,
                clipFreeRightColumnLayoutParams(true));
        title.setGravity(Gravity.CENTER);
        return title;
    }

    @NonNull
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

    @NonNull
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

    @NonNull
    private GridLayout.LayoutParams getLayoutParamsViews() {
        final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.setGravity(Gravity.TOP);
        setDefaultMargin(layoutParams);
        return layoutParams;
    }

    @NonNull
    private GridLayout.LayoutParams setDefaultMargin(
            @NonNull final GridLayout.LayoutParams layoutParams) {
        layoutParams.setMargins(
                DeviceUtils.dpToPx(4, context),
                DeviceUtils.dpToPx(2, context),
                DeviceUtils.dpToPx(4, context),
                DeviceUtils.dpToPx(2, context)
        );
        return layoutParams;
    }

    @NonNull
    private View setZeroPadding(@NonNull final View view) {
        view.setPadding(0, 0, 0, 0);
        return view;
    }

    public static class UiItemWrapperChip extends BaseUiItemWrapper {

        @NonNull
        private final ChipGroup chipGroup;

        public UiItemWrapperChip(@NonNull final FilterItem item,
                                 @NonNull final View view,
                                 @NonNull final ChipGroup chipGroup) {
            super(item, view);
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
