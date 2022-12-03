// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.util.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.view.MenuCompat;

import static android.content.ContentValues.TAG;

/**
 * The implementation of the action menu based 'dialog'.
 */
public class SearchFilterUIOptionMenu extends BaseSearchFilterUiGenerator {

    // Menu groups identifier
    private static final int MENU_GROUP_SEARCH_RESET_BUTTONS = 0;
    // give them negative ids to not conflict with the ids of the filters
    private static final int MENU_ID_SEARCH_BUTTON = -100;
    private static final int MENU_ID_RESET_BUTTON = -101;
    private Menu menu = null;
    // initialize with first group id -> next group after the search/reset buttons group
    private int newLastUsedGroupId = MENU_GROUP_SEARCH_RESET_BUTTONS + 1;
    private int firstSortFilterGroupId;

    public SearchFilterUIOptionMenu(final StreamingService service,
                                    final Callback callback,
                                    final Context context) {
        super(service.getSearchQHFactory(), callback, context);
    }

    int getLastUsedGroupIdThanIncrement() {
        return newLastUsedGroupId++;
    }

    @Override
    protected void handleIdInNonExclusiveGroup(final int filterId,
                                               final IUiItemWrapper uiItemWrapper,
                                               final List<Integer> selectedFilter) {
        uiItemWrapper.setChecked(!uiItemWrapper.isChecked()); // toggle
        super.handleIdInNonExclusiveGroup(filterId, uiItemWrapper, selectedFilter);
    }

    @SuppressLint("RestrictedApi")
    private void alwaysShowMenuItemIcon(final Menu theMenu) {
        // always show icons
        if (theMenu instanceof MenuBuilder) {
            final MenuBuilder builder = ((MenuBuilder) theMenu);
            builder.setOptionalIconsVisible(true);
        }
    }

    public void createSearchUI(@NonNull final Menu theMenu) {
        this.menu = theMenu;
        alwaysShowMenuItemIcon(theMenu);

        createSearchUI();

        MenuCompat.setGroupDividerEnabled(theMenu, true);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getGroupId() == MENU_GROUP_SEARCH_RESET_BUTTONS
                && item.getItemId() == MENU_ID_SEARCH_BUTTON) {
            prepareForSearch();
        } else { // all other menu groups -> reset, content filters and sort filters

            // main part for holding onto the menu -> not closing it
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            item.setActionView(new View(context));
            item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

                @Override
                public boolean onMenuItemActionExpand(final MenuItem item) {
                    if (item.getGroupId() == MENU_GROUP_SEARCH_RESET_BUTTONS
                            && item.getItemId() == MENU_ID_RESET_BUTTON) {
                        reset();
                    } else if (item.getGroupId() < firstSortFilterGroupId) { // content filters
                        final int filterId = item.getItemId();
                        selectContentFilter(filterId);
                    } else { // the sort filters
                        Log.d(TAG, "onMenuItemActionExpand: sort filters are here");
                        selectSortFilter(item.getItemId());
                    }

                    return false;
                }

                @Override
                public boolean onMenuItemActionCollapse(final MenuItem item) {
                    return false;
                }
            });
        }

        return false;
    }

    @Override
    protected ICreateUiForFiltersWorker createSortFilterWorker() {
        return new CreateSortFilterUI();
    }

    @Override
    protected ICreateUiForFiltersWorker createContentFilterWorker() {
        return new CreateContentFilterUI();
    }

    @Override
    public void onResume() {
        // Menu does not need a implementation here
    }

    @Override
    public void onPause() {
        // Menu does not need a implementation here
    }

    private static class UiItemWrapper implements IUiItemWrapper {

        private final MenuItem item;

        UiItemWrapper(final MenuItem item) {
            this.item = item;
        }

        @Override
        public void setVisible(final boolean visible) {
            item.setVisible(visible);
        }

        @Override
        public int getItemId() {
            return item.getItemId();
        }

        @Override
        public boolean isChecked() {
            return item.isChecked();
        }

        @Override
        public void setChecked(final boolean checked) {
            item.setChecked(checked);
        }
    }

    private class CreateContentFilterUI implements SearchFilterLogic.ICreateUiForFiltersWorker {

        /**
         * MenuItem's that should not be checkable.
         */
        final List<MenuItem> nonCheckableMenuItems = new ArrayList<>();

        /**
         * {@link Menu#setGroupCheckable(int, boolean, boolean)} makes all {@link MenuItem}
         * checkable.
         * <p>
         * We do not want a group header or a group divider to be checkable. Therefore this method
         * calls above mentioned method and afterwards makes all items uncheckable that are placed
         * inside {@link #nonCheckableMenuItems}.
         *
         * @param isOnlyOneCheckable is in group only one selection allowed.
         * @param groupId            which group should be affected
         */
        private void makeAllowedMenuItemInGroupCheckable(final boolean isOnlyOneCheckable,
                                                         final int groupId) {
            // this method makes all MenuItem's checkable
            menu.setGroupCheckable(groupId, true, isOnlyOneCheckable);
            // uncheckable unwanted
            for (final MenuItem uncheckableItem : nonCheckableMenuItems) {
                if (uncheckableItem != null) {
                    uncheckableItem.setCheckable(false);
                }
            }
            nonCheckableMenuItems.clear();
        }

        @Override
        public void prepare() {
            // create the search button
            menu.add(MENU_GROUP_SEARCH_RESET_BUTTONS,
                            MENU_ID_SEARCH_BUTTON,
                            0,
                            context.getString(R.string.search))
                    .setEnabled(true)
                    .setCheckable(false)
                    .setIcon(R.drawable.ic_search);

            menu.add(MENU_GROUP_SEARCH_RESET_BUTTONS,
                            MENU_ID_RESET_BUTTON,
                            0,
                            context.getString(R.string.playback_reset))
                    .setEnabled(true)
                    .setCheckable(false)
                    .setIcon(R.drawable.ic_settings_backup_restore);
        }

        @Override
        public void createFilterGroupBeforeItems(
                final FilterGroup filterGroup) {
            if (filterGroup.getName() != null) {
                createNotEnabledAndUncheckableGroupTitleMenuItem(
                        FilterContainer.ITEM_IDENTIFIER_UNKNOWN, filterGroup.getName());
            }
        }

        protected MenuItem createNotEnabledAndUncheckableGroupTitleMenuItem(final int identifier,
                                                                            final String name) {
            final MenuItem item = menu.add(
                    newLastUsedGroupId,
                    identifier,
                    0,
                    ServiceHelper.getTranslatedFilterString(name, context));
            item.setEnabled(false);

            nonCheckableMenuItems.add(item);

            return item;

        }

        @Override
        public void createFilterItem(final FilterItem filterItem,
                                     final FilterGroup filterGroup) {
            final MenuItem item = createMenuItem(filterItem);

            if (filterItem instanceof FilterItem.DividerItem) {
                final String menuDividerTitle = ">>>"
                        + ServiceHelper.getTranslatedFilterString(filterItem.getName(), context)
                        + "<<<";
                item.setTitle(menuDividerTitle);
                item.setEnabled(false);
                nonCheckableMenuItems.add(item);
            }

            addContentFilterUiWrapperToItemMap(filterItem.getIdentifier(),
                    new UiItemWrapper(item));
        }

        protected MenuItem createMenuItem(final FilterItem filterItem) {
            return menu.add(newLastUsedGroupId,
                    filterItem.getIdentifier(),
                    0,
                    ServiceHelper.getTranslatedFilterString(filterItem.getName(), context));
        }

        @Override
        public void createFilterGroupAfterItems(final FilterGroup filterGroup) {
            makeAllowedMenuItemInGroupCheckable(filterGroup.isOnlyOneCheckable(),
                    getLastUsedGroupIdThanIncrement());
        }

        @Override
        public void finish() {
            firstSortFilterGroupId = newLastUsedGroupId;
        }

        @Override
        public void filtersVisible(final boolean areFiltersVisible) {
            // no implementation here all as there is no 'sort filter' title as MenuItem
        }
    }

    private class CreateSortFilterUI extends CreateContentFilterUI {

        private void addSortFilterUiToItemMap(final int id,
                                              final MenuItem item) {
            addSortFilterUiWrapperToItemMap(id, new UiItemWrapper(item));
        }

        @Override
        public void prepare() {
            firstSortFilterGroupId = newLastUsedGroupId;
        }

        @Override
        public void createFilterGroupBeforeItems(
                final FilterGroup filterGroup) {
            if (filterGroup.getName() != null) {
                final MenuItem item = createNotEnabledAndUncheckableGroupTitleMenuItem(
                        filterGroup.getIdentifier(), filterGroup.getName());
                addSortFilterUiToItemMap(filterGroup.getIdentifier(), item);
            }
        }

        @Override
        public void createFilterItem(final FilterItem filterItem,
                                     final FilterGroup filterGroup) {
            final MenuItem item = createMenuItem(filterItem);
            addSortFilterUiToItemMap(filterItem.getIdentifier(), item);
        }

        @Override
        public void finish() {
            // no implementation here all as we do not need to clean up anything or whatever
        }
    }
}
