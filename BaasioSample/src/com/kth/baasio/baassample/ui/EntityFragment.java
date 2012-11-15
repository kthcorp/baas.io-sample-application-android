
package com.kth.baasio.baassample.ui;

import static com.kth.common.utils.LogUtils.LOGV;
import static com.kth.common.utils.LogUtils.makeLogTag;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.Baasio;
import com.kth.baasio.baassample.R;
import com.kth.baasio.baassample.cache.ImageFetcher;
import com.kth.baasio.baassample.ui.BaasMainActivity.OptionsItemSelectedListener;
import com.kth.baasio.baassample.ui.dialog.DialogUtils;
import com.kth.baasio.baassample.ui.dialog.EntityDialogFragment;
import com.kth.baasio.baassample.ui.dialog.EntityDialogFragment.EntityDialogResultListener;
import com.kth.baasio.baassample.utils.EtcUtils;
import com.kth.baasio.baassample.utils.actionmodecompat.ActionMode;
import com.kth.baasio.baassample.utils.actionmodecompat.ActionMode.Callback;
import com.kth.baasio.baassample.view.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.kth.baasio.baassample.view.pulltorefresh.PullToRefreshListView;

import org.usergrid.android.client.callbacks.ApiResponseCallback;
import org.usergrid.java.client.entities.Entity;
import org.usergrid.java.client.response.ApiResponse;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EntityFragment extends SherlockFragment implements OptionsItemSelectedListener,
        OnRefreshListener, Callback {

    private static final String TAG = makeLogTag(EntityFragment.class);

    public static final String ENTITY_TYPE = "friend";

    public static final String ENTITY_PROPERTY_NAME = "friend_name";

    private ViewGroup mRootView;

    private ImageFetcher mImageFetcher;

    private PullToRefreshListView mPullToRefreshList;

    private ListView mFriendList;

    private EntityListAdapter mListAdapter;

    private ArrayList<Entity> mEntityList;

    private String mNextCursor;

    private ActionMode mActionMode;

    private View mLongClickedView;

    private Integer mLongClickedPosition;

    public EntityFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageFetcher = EtcUtils.getImageFetcher(getActivity());

        mEntityList = new ArrayList<Entity>();
        mListAdapter = new EntityListAdapter(getActivity(), mImageFetcher);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_entity, null);

        mPullToRefreshList = (PullToRefreshListView)mRootView.findViewById(R.id.list);
        mPullToRefreshList.setOnRefreshListener(this);

        mFriendList = mPullToRefreshList.getRefreshableView();
        mFriendList.setAdapter(mListAdapter);

        getEntities(false);

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.ui.BaaSActivity.OptionsItemSelectedListener#
     * onParentOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
     */
    @Override
    public boolean onParentOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_entity_create: {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        EntityDialogFragment entityDialog = DialogUtils.showEntityDialog(
                                getActivity(), "entity_dialog", EntityDialogFragment.CREATE_ENTITY);
                        entityDialog
                                .setEntityDialogResultListener(new EntityDialogResultListener() {

                                    @Override
                                    public boolean onPositiveButtonSelected(int mode, Bundle data) {
                                        String body = data.getString("body");
                                        return processEntity(mode, body, -1);
                                    }
                                });
                    }
                }, 100);

                break;
            }
        }
        return false;
    }

    private void getEntities(final boolean next) {
        StringBuilder builder = new StringBuilder();

        String queryString;
        if (next) {
            builder.append("select *");
            builder.append(" order by ");
            builder.append("modified");
            builder.append(" desc");

            try {
                queryString = "?ql=" + URLEncoder.encode(builder.toString(), "UTF-8") + "&cursor="
                        + mNextCursor;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                queryString = "";
            }

            mPullToRefreshList.setIsLoading(true);
        } else {
            builder.append("select *");
            builder.append(" order by ");
            builder.append("modified");
            builder.append(" desc");

            try {
                queryString = "?ql=" + URLEncoder.encode(builder.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                queryString = "";
            }
        }

        Baasio.getInstance().queryEntitiesRequestAsync(new ApiResponseCallback() {

            @Override
            public void onResponse(ApiResponse apiResponse) {
                if (!next) {
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            if (mPullToRefreshList.isRefreshing())
                                mPullToRefreshList.onRefreshComplete();
                        }
                    });
                } else {
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            mPullToRefreshList.setIsLoading(false);
                        }
                    });
                }

                if (apiResponse != null) {
                    String error = apiResponse.getError();
                    if (error == null || error.length() <= 0) {
                        if (!next) {
                            mEntityList.clear();
                        }

                        List<Entity> entities = apiResponse.getEntities();
                        mEntityList.addAll(entities);

                        mListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), apiResponse.getErrorDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    mNextCursor = apiResponse.getCursor();
                    if (mNextCursor != null) {
                        if (mPullToRefreshList != null) {
                            mPullToRefreshList.setHasMoreData(true);
                        }

                        mPullToRefreshList.setFooterVisible();
                    } else {
                        if (mPullToRefreshList != null) {
                            mPullToRefreshList.setHasMoreData(false);
                        }

                        mPullToRefreshList.setFooterGone();
                    }
                }
            }

            @Override
            public void onException(Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();

                if (!next) {
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            if (mPullToRefreshList.isRefreshing())
                                mPullToRefreshList.onRefreshComplete();
                        }
                    });
                } else {
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            mPullToRefreshList.setIsLoading(false);
                        }
                    });
                }
            }

        }, ENTITY_TYPE + queryString);
    }

    public boolean processEntity(int mode, String body, final int position) {
        if (TextUtils.isEmpty(body)) {
            return false;
        }

        if (mode == EntityDialogFragment.CREATE_ENTITY) {
            Entity entity = new Entity(ENTITY_TYPE);
            entity.setProperty(ENTITY_PROPERTY_NAME, body);

            Baasio.getInstance().createEntityAsync(entity, new ApiResponseCallback() {

                @Override
                public void onException(Exception e) {
                    Toast.makeText(getActivity(), "createEntityAsync:" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(ApiResponse response) {
                    if (response != null) {
                        if (response.getError() == null || response.getError().length() <= 0) {
                            mEntityList.addAll(0, response.getEntities());

                            mListAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(),
                                    "createEntityAsync:" + response.getErrorDescription(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        } else if (mode == EntityDialogFragment.UPDATE_ENTITY) {
            Entity entity = new Entity(ENTITY_TYPE);
            entity.setProperty(ENTITY_PROPERTY_NAME, body);
            entity.setUuid(mEntityList.get(position).getUuid());

            Baasio.getInstance().updateEntityAsync(entity, new ApiResponseCallback() {

                @Override
                public void onException(Exception e) {
                    Toast.makeText(getActivity(), "updateEntityAsync:" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(ApiResponse response) {
                    if (response != null) {
                        if (response.getError() == null || response.getError().length() <= 0) {
                            Entity newEntity = response.getFirstEntity();

                            mEntityList.remove(position);
                            mEntityList.add(0, newEntity);

                            mListAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(),
                                    "updateEntityAsync:" + response.getErrorDescription(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

        return false;
    }

    public class EntityViewHolder {
        public ViewGroup mRoot;

        public ImageView mProfile;

        public TextView mName;

        public TextView mBody;

        public TextView mCreatedTime;

        public TextView mModifiedTime;
    }

    private class EntityListAdapter extends BaseAdapter {
        private final String TAG = makeLogTag(EntityListAdapter.class);

        private Context mContext;

        private LayoutInflater mInflater;

        private ImageFetcher mImageFetcher;

        public EntityListAdapter(Context context, ImageFetcher imageFetcher) {
            super();

            mContext = context;

            mImageFetcher = imageFetcher;

            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mEntityList.size();
        }

        @Override
        public Entity getItem(int position) {
            return mEntityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            EntityViewHolder view = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listview_item_entitylist, parent, false);

                view = new EntityViewHolder();

                view.mRoot = (ViewGroup)convertView.findViewById(R.id.layoutRoot);
                view.mName = (TextView)convertView.findViewById(R.id.textName);
                view.mCreatedTime = (TextView)convertView.findViewById(R.id.textCreatedTime);
                view.mModifiedTime = (TextView)convertView.findViewById(R.id.textModifiedTime);

                if (view != null) {
                    convertView.setTag(view);
                }
            } else {
                view = (EntityViewHolder)convertView.getTag();
            }

            Entity entity = mEntityList.get(position);

            if (entity != null) {
                setStringToView(entity, view.mName, ENTITY_PROPERTY_NAME);

                String createdTime = EtcUtils.getDateString(Long.valueOf(EtcUtils
                        .getLongFromEntity(entity, "created", -1)));
                if (!TextUtils.isEmpty(createdTime)) {
                    view.mCreatedTime.setText(createdTime);
                }

                String modifiedTime = EtcUtils.getDateString(Long.valueOf(EtcUtils
                        .getLongFromEntity(entity, "modified", -1)));
                if (!TextUtils.isEmpty(modifiedTime)) {
                    view.mModifiedTime.setText(modifiedTime);
                }

                if (view.mRoot != null) {
                    view.mRoot.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View view) {
                            if (mActionMode != null) {
                                // CAB already displayed, ignore
                                return true;
                            }

                            mLongClickedView = view;
                            mLongClickedPosition = position;

                            mActionMode = ActionMode.start(getActivity(), EntityFragment.this);
                            EtcUtils.setActivatedCompat(mLongClickedView, true);
                            return true;
                        }
                    });
                }
            }
            return convertView;
        }

        private void setStringToView(Entity entity, TextView view, String value) {
            view.setText(EtcUtils.getStringFromEntity(entity, value));
        }

    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.view.pulltorefresh.PullToRefreshBase.
     * OnRefreshListener#onRefresh()
     */
    @Override
    public void onRefresh() {
        getEntities(false);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.view.pulltorefresh.PullToRefreshBase.
     * OnRefreshListener#onUpdate()
     */
    @Override
    public void onUpdate() {
        getEntities(true);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onCreateActionMode
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode,
     * android.view.Menu)
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (mLongClickedView == null) {
            return true;
        }

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextmenu_fragment_entity, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onPrepareActionMode
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode,
     * android.view.Menu)
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onActionItemClicked
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode,
     * android.view.MenuItem)
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
        boolean handled = false;
        switch (item.getItemId()) {
            case R.id.menu_entity_update: {
                final int position = mLongClickedPosition;

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Entity entity = mEntityList.get(position);
                        String body = EtcUtils.getStringFromEntity(entity, ENTITY_PROPERTY_NAME);

                        EntityDialogFragment entityDialog = DialogUtils.showEntityDialog(
                                getActivity(), "entity_dialog", EntityDialogFragment.UPDATE_ENTITY);
                        entityDialog.setBody(body);
                        entityDialog
                                .setEntityDialogResultListener(new EntityDialogResultListener() {

                                    @Override
                                    public boolean onPositiveButtonSelected(int mode, Bundle data) {
                                        String body = data.getString("body");
                                        return processEntity(mode, body, position);
                                    }
                                });
                    }
                }, 100);

                handled = true;
                break;
            }
            case R.id.menu_entity_delete: {
                final int position = mLongClickedPosition;

                Entity entity = mEntityList.get(mLongClickedPosition);

                Baasio.getInstance().deleteEntityAsync(entity, new ApiResponseCallback() {

                    @Override
                    public void onException(Exception e) {
                        Toast.makeText(getActivity(), "deleteEntityAsync:" + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(ApiResponse response) {
                        if (response != null) {
                            if (response.getError() == null || response.getError().length() <= 0) {
                                mEntityList.remove(position);

                                mListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(),
                                        "deleteEntityAsync:" + response.getErrorDescription(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
                handled = true;
                break;
            }
        }

        LOGV(TAG,
                "onActionItemClicked: position=" + mLongClickedPosition + " title="
                        + item.getTitle());
        mActionMode.finish();
        return handled;
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onDestroyActionMode
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode)
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        if (mLongClickedView != null) {
            EtcUtils.setActivatedCompat(mLongClickedView, false);
            mLongClickedPosition = null;
            mLongClickedView = null;
        }
    }

}
