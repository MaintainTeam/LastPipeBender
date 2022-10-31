// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.view.View;

import org.schabi.newpipe.extractor.search.filter.FilterItem;


public abstract class BaseUiItemWrapper extends BaseItemWrapper {
    protected final View view;

    protected BaseUiItemWrapper(final FilterItem item,
                                final View view) {
        super(item);
        this.view = view;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
