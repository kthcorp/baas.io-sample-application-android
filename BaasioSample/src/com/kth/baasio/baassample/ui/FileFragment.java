
package com.kth.baasio.baassample.ui;

import static com.kth.common.utils.LogUtils.LOGE;
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
import com.kth.baasio.baassample.utils.FileUtils;
import com.kth.baasio.baassample.utils.actionmodecompat.ActionMode;
import com.kth.baasio.baassample.utils.actionmodecompat.ActionMode.Callback;
import com.kth.baasio.baassample.view.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.kth.baasio.baassample.view.pulltorefresh.PullToRefreshListView;
import com.kth.baasio.callback.ApiResponseProgressCallback;
import com.kth.baasio.callback.ClientProgressAsyncTask;

import org.usergrid.android.client.callbacks.ApiResponseCallback;
import org.usergrid.java.client.entities.Entity;
import org.usergrid.java.client.response.ApiResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileFragment extends SherlockFragment implements OptionsItemSelectedListener,
        OnRefreshListener, Callback {

    private static final String TAG = makeLogTag(FileFragment.class);

    private static final String KEY_PATH = "path";

    private static final String KEY_SIZE = "size";

    private final static int REQUEST_FILE_FOR_CREATE = 1;

    private final static int REQUEST_FILE_FOR_UPDATE = 2;

    private ViewGroup mRootView;

    private ImageFetcher mImageFetcher;

    private PullToRefreshListView mPullToRefreshList;

    private ListView mListView;

    private FileListAdapter mListAdapter;

    private TextView mTextCurrentPath;

    private TextView mTextQuota;

    private ArrayList<Entity> mEntityList;

    private String mNextCursor;

    private static final String ROOT_PATH = "public/";

    private String mCurrentPath = ROOT_PATH;

    private ClientProgressAsyncTask<ApiResponse> mFileAsyncTask = null;

    private ActionMode mActionMode;

    private View mLongClickedView;

    private Integer mLongClickedPosition;

    private Integer mPositionForUpdate;

    public FileFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageFetcher = EtcUtils.getImageFetcher(getActivity());

        mEntityList = new ArrayList<Entity>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_file, null);
        mTextCurrentPath = (TextView)mRootView.findViewById(R.id.textPath);
        mTextQuota = (TextView)mRootView.findViewById(R.id.textQuota);

        mPullToRefreshList = (PullToRefreshListView)mRootView.findViewById(R.id.list);
        mPullToRefreshList.setOnRefreshListener(this);
        mListView = mPullToRefreshList.getRefreshableView();
        mListAdapter = new FileListAdapter(getActivity(), mImageFetcher);

        mListView.setAdapter(mListAdapter);

        Baasio.getInstance().getQuotaInformationAsync(new ApiResponseCallback() {

            @Override
            public void onException(Exception e) {
                Toast.makeText(getActivity(), "getQuotaInformationAsync:" + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(ApiResponse response) {
                if (response != null) {
                    if (TextUtils.isEmpty(response.getError())) {
                        // Map<String, JsonNode> properties =
                        // response.getProperties();
                        HashMap<String, Object> map = (HashMap<String, Object>)response.getData();
                        if (map != null) {
                            Integer size = (Integer)map.get("size");
                            if (size != null) {
                                mTextQuota.setText(size + " bytes");
                            }
                        }

                        mTextQuota.setVisibility(View.VISIBLE);
                    } else {
                        mTextQuota.setVisibility(View.GONE);
                        Toast.makeText(getActivity(),
                                "getQuotaInformationAsync:" + response.getErrorDescription(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        getFileList(null, ROOT_PATH);

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void getFileList(final String cursor, String path) {
        StringBuilder builder = new StringBuilder();

        String queryString;
        if (!TextUtils.isEmpty(cursor)) {
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

        Map<String, Object> params = null;
        if (!TextUtils.isEmpty(cursor)) {
            params = new HashMap<String, Object>();
            params.put("cursor", cursor);
        } else {
            if (!TextUtils.isEmpty(path)) {
                mTextCurrentPath.setText(path);
                mCurrentPath = path;
            } else {
                mTextCurrentPath.setText(ROOT_PATH);
                mCurrentPath = ROOT_PATH;
            }
        }

        Baasio.getInstance().queryEntitiesRequestAsync(new ApiResponseCallback() {

            @Override
            public void onResponse(ApiResponse response) {
                if (TextUtils.isEmpty(cursor)) {
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
                if (response != null) {
                    if (TextUtils.isEmpty(response.getError())) {
                        if (TextUtils.isEmpty(cursor)) {
                            mEntityList.clear();

                            if (!mCurrentPath.equals(ROOT_PATH)) {
                                Entity entity = new Entity();
                                entity.setProperty(KEY_PATH, "..");
                                entity.setProperty(KEY_SIZE, "0");
                                mEntityList.add(entity);
                            }
                        }

                        mEntityList.addAll(response.getEntities());

                        mListAdapter.notifyDataSetChanged();

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (mPullToRefreshList.isRefreshing()) {
                                    mPullToRefreshList.onRefreshComplete();
                                }
                            }
                        });

                        mNextCursor = response.getCursor();
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
                    } else {
                        if (!TextUtils.isEmpty(response.getErrorDescription())) {
                            Toast.makeText(
                                    getActivity(),
                                    "queryEntitiesRequestAsync onResponse:"
                                            + response.getErrorDescription(), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getActivity(),
                                    "queryEntitiesRequestAsync onResponse:" + response.getError(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onException(Exception e) {
                Toast.makeText(getActivity(), "getFileList:" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }, "files/" + path + queryString);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Integer position = mPositionForUpdate;

        mPositionForUpdate = null;

        if (resultCode == Activity.RESULT_OK) {
            Uri contentUri = data.getData();
            if (contentUri == null) {
                return;
            }

            String srcFilePath;
            try {
                srcFilePath = FileUtils.getPath(getActivity(), contentUri);
            } catch (URISyntaxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return;
            }

            if (requestCode == REQUEST_FILE_FOR_CREATE) {
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        DialogUtils.showProgressDialog(getActivity(), "upload_progress",
                                "업로드 중입니다.", ProgressDialog.STYLE_HORIZONTAL);

                        DialogUtils.setProgress(getActivity(), "upload_progress", 0);
                    }
                });

                mFileAsyncTask = Baasio.getInstance().createFileAsync(srcFilePath, mCurrentPath,
                        null, true, new ApiResponseProgressCallback() {

                            @Override
                            public void onProgress(long total, long current) {
                                float progress = (float)((double)current / (double)total);

                                DialogUtils.setProgress(getActivity(), "upload_progress",
                                        (int)(progress * 100.f));
                            }

                            @Override
                            public void onException(Exception e) {
                                DialogUtils
                                        .dissmissProgressDialog(getActivity(), "upload_progress");

                                Toast.makeText(getActivity(),
                                        "createFileAsync onException:" + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onResponse(ApiResponse response) {
                                DialogUtils
                                        .dissmissProgressDialog(getActivity(), "upload_progress");

                                if (response != null) {
                                    Toast.makeText(getActivity(),
                                            "createFileAsync onResponse:" + response.toString(),
                                            Toast.LENGTH_LONG).show();
                                    LOGE(TAG, response.toString());

                                    if (TextUtils.isEmpty(response.getError())) {
                                        List<Entity> entities = response.getEntities();
                                        for (Entity entity : entities) {
                                            String uploadedFilePath = EtcUtils.getStringFromEntity(
                                                    entity, "path");
                                            if (!uploadedFilePath.endsWith("/")) {
                                                Entity firstEntity = mEntityList.get(0);
                                                if (firstEntity != null) {
                                                    String path = EtcUtils.getStringFromEntity(
                                                            firstEntity, KEY_PATH);

                                                    if (!path.equals("..")) {
                                                        mEntityList.add(0, entity);
                                                    } else {
                                                        mEntityList.add(1, entity);
                                                    }
                                                } else {
                                                    mEntityList.add(0, entity);
                                                }
                                            }
                                        }

                                        mListAdapter.notifyDataSetChanged();
                                    } else {
                                        if (!TextUtils.isEmpty(response.getErrorDescription())) {
                                            Toast.makeText(
                                                    getActivity(),
                                                    "createFileAsync onResponse:"
                                                            + response.getErrorDescription(),
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(
                                                    getActivity(),
                                                    "createFileAsync onResponse:"
                                                            + response.getError(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                        });
            } else if (requestCode == REQUEST_FILE_FOR_UPDATE) {
                if (position == null) {
                    return;
                }

                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        DialogUtils.showProgressDialog(getActivity(), "upload_progress",
                                "업로드 중입니다.", ProgressDialog.STYLE_HORIZONTAL);

                        DialogUtils.setProgress(getActivity(), "upload_progress", 0);
                    }
                });

                Entity entity = mEntityList.get(position);
                if (entity != null) {
                    String uuid = entity.getUuid().toString();
                    mFileAsyncTask = Baasio.getInstance().updateFileAsync(uuid, srcFilePath, true,
                            new ApiResponseProgressCallback() {

                                @Override
                                public void onProgress(long total, long current) {
                                    float progress = (float)((double)current / (double)total);

                                    DialogUtils.setProgress(getActivity(), "upload_progress",
                                            (int)(progress * 100.f));
                                }

                                @Override
                                public void onException(Exception e) {
                                    DialogUtils.dissmissProgressDialog(getActivity(),
                                            "upload_progress");

                                    Toast.makeText(getActivity(),
                                            "updateFileAsync onException:" + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onResponse(ApiResponse response) {
                                    DialogUtils.dissmissProgressDialog(getActivity(),
                                            "upload_progress");

                                    if (response != null) {
                                        Toast.makeText(
                                                getActivity(),
                                                "updateFileAsync onResponse:" + response.toString(),
                                                Toast.LENGTH_LONG).show();
                                        LOGE(TAG, response.toString());

                                        if (TextUtils.isEmpty(response.getError())) {
                                            List<Entity> entities = response.getEntities();
                                            for (Entity entity : entities) {
                                                String path = EtcUtils.getStringFromEntity(entity,
                                                        "path");
                                                if (!path.endsWith("/")) {
                                                    mEntityList.add(0, entity);
                                                }
                                            }

                                            mListAdapter.notifyDataSetChanged();
                                        } else {
                                            if (!TextUtils.isEmpty(response.getErrorDescription())) {
                                                Toast.makeText(
                                                        getActivity(),
                                                        "updateFileAsync onResponse:"
                                                                + response.getErrorDescription(),
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(
                                                        getActivity(),
                                                        "updateFileAsync onResponse:"
                                                                + response.getError(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    }
                                }
                            });
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.kth.Baasio.baassample.ui.BaaSActivity.OptionsItemSelectedListener#
     * onParentOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
     */
    @Override
    public boolean onParentOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_file_create: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_FILE_FOR_CREATE);
                break;
            }
            case R.id.menu_folder_create: {
                EntityDialogFragment folderDialog = DialogUtils.showEntityDialog(getActivity(),
                        "create_folder", EntityDialogFragment.CREATE_FOLDER);

                folderDialog.setEntityDialogResultListener(new EntityDialogResultListener() {

                    @Override
                    public boolean onPositiveButtonSelected(int mode, Bundle data) {
                        String body = data.getString("body");

                        if (!body.endsWith("/")) {
                            body = body + "/";
                        }

                        final String folderPath = body;

                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.showProgressDialog(getActivity(),
                                        "create_folder_progress", "폴더 생성 중입니다.");
                            }
                        });

                        Baasio.getInstance().createFolderAsync(mCurrentPath, body,
                                new ApiResponseCallback() {
                                    @Override
                                    public void onException(Exception e) {
                                        DialogUtils.dissmissProgressDialog(getActivity(),
                                                "create_folder_progress");

                                        Toast.makeText(getActivity(),
                                                "createFileAsync onException:" + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onResponse(ApiResponse response) {
                                        DialogUtils.dissmissProgressDialog(getActivity(),
                                                "create_folder_progress");

                                        if (response != null) {
                                            Toast.makeText(
                                                    getActivity(),
                                                    "createFileAsync onResponse:"
                                                            + response.toString(),
                                                    Toast.LENGTH_LONG).show();
                                            LOGE(TAG, response.toString());

                                            if (TextUtils.isEmpty(response.getError())) {
                                                List<Entity> entities = response.getEntities();
                                                for (Entity entity : entities) {
                                                    String uploadedFolderPath = EtcUtils
                                                            .getStringFromEntity(entity, "path");
                                                    LOGE(TAG, "folderPath:" + folderPath);
                                                    if (uploadedFolderPath.endsWith(folderPath)) {
                                                        Entity firstEntity = mEntityList.get(0);
                                                        if (firstEntity != null) {
                                                            String path = EtcUtils
                                                                    .getStringFromEntity(
                                                                            firstEntity, KEY_PATH);

                                                            if (!path.equals("..")) {
                                                                mEntityList.add(0, entity);
                                                            } else {
                                                                mEntityList.add(1, entity);
                                                            }
                                                        } else {
                                                            mEntityList.add(0, entity);
                                                        }
                                                    }
                                                }

                                                mListAdapter.notifyDataSetChanged();
                                            } else {
                                                if (!TextUtils.isEmpty(response
                                                        .getErrorDescription())) {
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "createFileAsync onResponse:"
                                                                    + response
                                                                            .getErrorDescription(),
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "createFileAsync onResponse:"
                                                                    + response.getError(),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    }
                                });
                        return false;
                    }
                });
                break;
            }
        }
        return false;
    }

    public class EntityViewHolder {
        public LinearLayout mRoot;

        public ImageView mFileType;

        public ImageView mFolderType;

        public TextView mName;

        public TextView mSize;

        public LinearLayout mLowerLayout;

        public TextView mCreatedTime;

        public TextView mModifiedTime;
    }

    private class FileListAdapter extends BaseAdapter {
        private final String TAG = makeLogTag(FileListAdapter.class);

        private Context mContext;

        private LayoutInflater mInflater;

        private ImageFetcher mImageFetcher;

        public FileListAdapter(Context context, ImageFetcher imageFetcher) {
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
                convertView = mInflater.inflate(R.layout.listview_item_filelist, parent, false);

                view = new EntityViewHolder();
                view.mRoot = (LinearLayout)convertView.findViewById(R.id.layoutRoot);
                view.mFileType = (ImageView)convertView.findViewById(R.id.imageFileType);
                view.mFolderType = (ImageView)convertView.findViewById(R.id.imageFolderType);

                view.mName = (TextView)convertView.findViewById(R.id.textName);
                view.mSize = (TextView)convertView.findViewById(R.id.textSize);

                view.mLowerLayout = (LinearLayout)convertView.findViewById(R.id.layoutLower);
                view.mCreatedTime = (TextView)convertView.findViewById(R.id.textCreatedTime);
                view.mModifiedTime = (TextView)convertView.findViewById(R.id.textModifiedTime);

                if (view != null) {
                    convertView.setTag(view);
                }
            } else {
                view = (EntityViewHolder)convertView.getTag();
            }

            final Entity entity = mEntityList.get(position);

            if (entity != null) {
                String path = EtcUtils.getStringFromEntity(entity, KEY_PATH);

                String[] splittedPath = path.split("\\/");

                if (path.endsWith("/")) {
                    view.mFileType.setVisibility(View.GONE);
                    view.mFolderType.setVisibility(View.VISIBLE);

                    view.mSize.setVisibility(View.GONE);

                    view.mLowerLayout.setVisibility(View.VISIBLE);
                } else if (path.equalsIgnoreCase("..")) {
                    view.mFileType.setVisibility(View.GONE);
                    view.mFolderType.setVisibility(View.VISIBLE);

                    view.mSize.setVisibility(View.GONE);

                    view.mLowerLayout.setVisibility(View.GONE);
                } else {
                    view.mFileType.setVisibility(View.VISIBLE);
                    view.mFolderType.setVisibility(View.GONE);

                    view.mSize.setVisibility(View.VISIBLE);
                    view.mSize.setText(EtcUtils.getLongFromEntity(entity, KEY_SIZE, -1) + " bytes");

                    view.mLowerLayout.setVisibility(View.VISIBLE);
                }

                if (splittedPath.length > 0) {
                    String name = splittedPath[splittedPath.length - 1];

                    String result = "";
                    try {
                        result = URLDecoder.decode(name, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    view.mName.setText(result);
                }

                view.mCreatedTime.setText(EtcUtils.getSimpleDateString(EtcUtils.getLongFromEntity(
                        entity, "created", -1)));
                view.mModifiedTime.setText(EtcUtils.getSimpleDateString(EtcUtils.getLongFromEntity(
                        entity, "modified", -1)));

                view.mRoot.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onItemClicked(entity);
                    }
                });

                view.mRoot.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {
                        if (mActionMode != null) {
                            // CAB already displayed, ignore
                            return true;
                        }

                        mLongClickedView = view;
                        mLongClickedPosition = position;

                        mActionMode = ActionMode.start(getActivity(), FileFragment.this);
                        EtcUtils.setActivatedCompat(mLongClickedView, true);
                        return true;
                    }
                });
            }
            return convertView;
        }
    }

    private void onItemClicked(Entity entity) {
        String path = EtcUtils.getStringFromEntity(entity, KEY_PATH);

        if (path.endsWith("/")) {
            getFileList(null, path);
        } else if (path.equalsIgnoreCase("..")) {
            goToUpDir();
        } else {
            // false면 file
            downloadFile(entity);
        }
    }

    public void downloadFile(Entity entity) {
        String remotePath = EtcUtils.getStringFromEntity(entity, KEY_PATH);

        String localPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Download/";
        // String localPath =
        // Environment.getExternalStorageDirectory().getAbsolutePath() +
        // "/Android/data/"
        // + getActivity().getPackageName() + "/download";

        new Handler().post(new Runnable() {

            @Override
            public void run() {
                DialogUtils.showProgressDialog(getActivity(), "download_progress", "다운로드 중입니다.",
                        ProgressDialog.STYLE_HORIZONTAL);

                DialogUtils.setProgress(getActivity(), "download_progress", 0);
            }
        });

        mFileAsyncTask = Baasio.getInstance().getFileAsync(remotePath, localPath, null,
                new ApiResponseProgressCallback() {

                    @Override
                    public void onProgress(long total, long current) {
                        float progress = (float)((double)current / (double)total);
                        LOGE(TAG, "getFileAsync onProgress:" + progress);

                        DialogUtils.setProgress(getActivity(), "download_progress",
                                (int)(progress * 100.f));
                    }

                    @Override
                    public void onException(Exception e) {
                        DialogUtils.dissmissProgressDialog(getActivity(), "download_progress");

                        Toast.makeText(getActivity(), "getFileAsync onException:" + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        LOGE(TAG, "getFileAsync onException:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(ApiResponse response) {
                        DialogUtils.dissmissProgressDialog(getActivity(), "download_progress");
                        if (response != null) {
                            Toast.makeText(getActivity(),
                                    "getFileAsync onResponse:" + response.toString(),
                                    Toast.LENGTH_LONG).show();

                            LOGE(TAG, response.toString());
                        }
                    }

                });
    }

    @Override
    public void onRefresh() {
        getFileList(null, mCurrentPath);
    }

    @Override
    public void onUpdate() {
        // TODO Auto-generated method stub
    }

    private void goToUpDir() {
        if (mCurrentPath.equalsIgnoreCase(ROOT_PATH)) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        String[] splittedPath = mCurrentPath.split("\\/");
        for (int i = 0; i < splittedPath.length - 1; i++) {
            if (builder.length() != 0) {
                builder.append("/");
            }
            builder.append(splittedPath[i]);
        }

        if (builder.length() != 0) {
            builder.append("/");
        }
        getFileList(null, builder.toString());
    }

    /*
     * (non-Javadoc)
     * @see
     * com.kth.baasio.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onCreateActionMode
     * (com.kth.baasio.baassample.utils.actionmodecompat.ActionMode,
     * android.view.Menu)
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (mLongClickedView == null) {
            return true;
        }

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextmenu_fragment_file, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.kth.baasio.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onPrepareActionMode
     * (com.kth.baasio.baassample.utils.actionmodecompat.ActionMode,
     * android.view.Menu)
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.kth.baasio.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onActionItemClicked
     * (com.kth.baasio.baassample.utils.actionmodecompat.ActionMode,
     * android.view.MenuItem)
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
        boolean handled = false;
        switch (item.getItemId()) {
            case R.id.menu_file_update: {
                Entity entity = mEntityList.get(mLongClickedPosition);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_FILE_FOR_UPDATE);

                mPositionForUpdate = mLongClickedPosition;

                handled = true;
                break;
            }
            case R.id.menu_file_delete: {
                final int position = mLongClickedPosition;

                Entity entity = mEntityList.get(mLongClickedPosition);

                Baasio.getInstance().deleteFileAsync(entity.getUuid().toString(),
                        new ApiResponseCallback() {

                            @Override
                            public void onException(Exception e) {
                                Toast.makeText(getActivity(), "deleteFileAsync:" + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onResponse(ApiResponse response) {
                                if (response != null) {
                                    if (response.getError() == null
                                            || response.getError().length() <= 0) {
                                        mEntityList.remove(position);

                                        mListAdapter.notifyDataSetChanged();
                                    } else {
                                        if (response.getErrorDescription() != null) {
                                            Toast.makeText(
                                                    getActivity(),
                                                    "deleteFileAsync onResponse:"
                                                            + response.getErrorDescription(),
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(
                                                    getActivity(),
                                                    "deleteFileAsync onResponse:"
                                                            + response.getError(),
                                                    Toast.LENGTH_LONG).show();
                                        }

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
     * @see
     * com.kth.baasio.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onDestroyActionMode
     * (com.kth.baasio.baassample.utils.actionmodecompat.ActionMode)
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
