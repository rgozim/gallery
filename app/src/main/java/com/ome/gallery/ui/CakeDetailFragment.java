package com.ome.gallery.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ome.gallery.R;
import com.ome.gallery.data.models.CakeModel;


/**
 * Created by rgozim on 09/06/2016.
 */

public class CakeDetailFragment extends Fragment {

    /**
     * Input arguments supplied at creation time.
     */
    private static final String ARG_CAKE_MODEL = "cakeModel";

    /**
     * Static function to create this fragment with given variables.
     *
     * @param model the selected cake model.
     * @return a new fragment with arguments set.x
     */
    public static CakeDetailFragment newInstance(CakeModel model) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_CAKE_MODEL, model);

        CakeDetailFragment fragment = new CakeDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cake_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final CakeModel cakeModel = getCakeModel();

//        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.cakeImage);
//        if (imageView != null) {
//            Uri uri = Uri.parse(cakeModel.getImageUrl());
//            imageView.setImageURI(uri);
//        }

        TextView title = view.findViewById(R.id.title);
        if (title != null) {
            title.setText(cakeModel.getTitle());
        }

        TextView description = view.findViewById(R.id.description);
        if (description != null) {
            description.setText(cakeModel.getDesc());
        }
    }

    public CakeModel getCakeModel() {
        return getArguments().getParcelable(ARG_CAKE_MODEL);
    }

}
