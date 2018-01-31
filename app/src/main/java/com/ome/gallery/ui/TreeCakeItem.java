package com.ome.gallery.ui;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.gozisoft.treelistview.TreeNodeView;
import com.ome.gallery.R;
import com.ome.gallery.data.models.CakeModel;

/**
 * item within the list that represents a cake model.
 * With this implementation there is a direct relation between
 * a model and it's view binding.
 */
class TreeCakeItem extends TreeNodeView<TreeCakeItem> {

    private CakeModel mModel;

    public TreeCakeItem(Context context, CakeModel model) {
        super(TreeCakeItem.class, context);
        setKey(model.getTitle());
        mModel = model;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View root = getInflater().inflate(getLayout(), parent, false);
        if (root != null) {
            ViewHolder vh = new ViewHolder(root);
            root.setTag(vh);
        }
        return root;
    }

    @Override
    protected void onBindView(View view) {
        ViewHolder vh = (ViewHolder) view.getTag();
        vh.title.setText(mModel.getTitle());
        vh.desc.setText(mModel.getDesc());

        Uri uri = Uri.parse(mModel.getImageUrl());
        // vh.cakeImage.setImageURI(uri);
    }

    @Override
    public int getLayout() {
        return R.layout.adapter_cake_item;
    }


    public CakeModel getModel() {
        return mModel;
    }

    private class ViewHolder {
        // SimpleDraweeView cakeImage;
        TextView title;
        TextView desc;

        ViewHolder(View root) {
            // cakeImage = (SimpleDraweeView) root.findViewById(R.id.cakeImage);
            title = (TextView) root.findViewById(R.id.title);
            desc = (TextView) root.findViewById(R.id.description);
        }
    }
}
