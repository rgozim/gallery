package com.ome.gallery.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.gozisoft.treelistview.TreeAdapter;
import com.gozisoft.treelistview.TreeGroup;
import com.gozisoft.treelistview.TreeView;
import com.ome.gallery.R;
import com.ome.gallery.data.LoadCakesLoader;
import com.ome.gallery.data.models.CakeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rgozim on 09/06/2016.
 */

public class CakeListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<CakeModel>> {

    private ListView mList;

    /**
     * Progress layout for displaying loading.
     */
    private ViewGroup mProgress;

    /**
     * layout that displays all content, when not loading
     */
    private ViewGroup mContent;

    /**
     * The tree root that will add and remove items to our list
     */
    private TreeGroup mRoot = new TreeGroup();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cake_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgress = (ViewGroup) view.findViewById(R.id.progressContainer);
        mList = (ListView) view.findViewById(android.R.id.list);
        if (mList != null) {
            mList.setAdapter(new TreeAdapter(mRoot));
            mContent = mList;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Load data for displaying cakes.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<CakeModel>> onCreateLoader(int id, Bundle args) {
        // Show progress when creating a loader.
        showLoading(true);

        // Return the new cakes loader.
        return new LoadCakesLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<CakeModel>> loader, List<CakeModel> data) {
        // Done loading, hide progress
        showLoading(false);

        // Set the list of cakes, if not empty.
        if (data.isEmpty()) {
            return;
        }

        // A clickListener that can be used across all the items within the list.
        // When the user clicks an item in the list, the appropriate
        // View has performClick() called, which in turn triggers this listener.
        TreeCakeItem.OnClickListener clickListener = item -> {
            showCakeDetail(((TreeCakeItem) item).getModel());
            return false;
        };

        List<TreeView> cakeItems = new ArrayList<>(data.size());

        // Create row items
        for (CakeModel cakeModel : data) {
            TreeCakeItem item = new TreeCakeItem(getContext(), cakeModel);
            item.setOnClickListener(clickListener);
            cakeItems.add(item);
        }

        // Replace all items in root
        // Add to root.
        mRoot.replaceAll(cakeItems);
    }

    @Override
    public void onLoaderReset(Loader<List<CakeModel>> loader) {
        mRoot.clear();
    }

    private void showCakeDetail(CakeModel cakeModel) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack("")
                .replace(getId(), CakeDetailFragment.newInstance(cakeModel))
                .commit();
    }

    private void showLoading(boolean show) {
        final int duration = getResources()
                .getInteger(android.R.integer.config_shortAnimTime);

        ObjectAnimator fadeIn = new ObjectAnimator();
        fadeIn.setFloatValues(0.0f, 1.0f);
        fadeIn.setDuration(duration);

        ObjectAnimator fadeOut = new ObjectAnimator();
        fadeIn.setFloatValues(1.0f, 0.0f);
        fadeIn.setDuration(duration);

        if (show) {
            // Animate the content out and the spinner in.
            fadeOut.setTarget(mContent);
            fadeIn.setTarget(mProgress);
        } else {
            // Animate the spinner out and content in
            fadeOut.setTarget(mProgress);
            fadeIn.setTarget(mContent);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeOut, fadeIn);
    }

}
