// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public abstract class BaseSearchFilterUiDialogGenerator extends BaseSearchFilterUiGenerator {
    private static final float FONT_SIZE_TITLE_ITEMS_IN_DIP = 20f;

    protected final Map<View, View.OnClickListener> viewListeners = new HashMap<>();

    protected BaseSearchFilterUiDialogGenerator(
            final SearchQueryHandlerFactory linkHandlerFactory,
            final Callback callback,
            final Context context) {
        super(linkHandlerFactory, callback, context);
    }

    protected abstract void createTitle(String name, List<View> titleViewElements);

    protected abstract void createFilterGroup(FilterGroup filterGroup,
                                              UiWrapperMapDelegate wrapperDelegate,
                                              UiSelectorDelegate selectorDelegate);

    @Override
    protected ICreateUiForFiltersWorker createContentFilterWorker() {
        return new BaseCreateSearchFilterUI.CreateContentFilterUI(this, context);
    }

    @Override
    protected ICreateUiForFiltersWorker createSortFilterWorker() {
        return new BaseCreateSearchFilterUI.CreateSortFilterUI(this, context);
    }

    @Override
    public void onResume() {
        for (final Map.Entry<View, View.OnClickListener> view : viewListeners.entrySet()) {
            view.getKey().setOnClickListener(view.getValue());
        }
    }

    @Override
    public void onPause() {
        for (final Map.Entry<View, View.OnClickListener> view : viewListeners.entrySet()) {
            view.getKey().setOnClickListener(null);
        }
    }

    protected View createSeparatorLine(final ViewGroup.LayoutParams layoutParams) {
        final View separatorLine = new View(context);
        separatorLine.setBackgroundColor(getSeparatorLineColorFromTheme());
        layoutParams.height = 1; // always set the separator to the height of 1
        separatorLine.setLayoutParams(layoutParams);
        return separatorLine;
    }

    protected TextView createTitleText(final String name,
                                       final ViewGroup.LayoutParams layoutParams) {
        final TextView title = new TextView(context);
        title.setText(name);
        title.setTextSize(COMPLEX_UNIT_DIP, FONT_SIZE_TITLE_ITEMS_IN_DIP);
        title.setLayoutParams(layoutParams);
        return title;
    }
}
