package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.OnUpdateListener;
import info.paveway.lowest.R;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.CategoryTable;
import info.paveway.util.StringUtil;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * 最低価格記録アプリ
 * カテゴリ編集ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class CategoryEditDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(CategoryEditDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** カテゴリデータ */
    private CategoryData mCategoryData;

    /** カテゴリ名入力 */
    private EditText mCategoryNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static CategoryEditDialog newInstance(CategoryData categoryData) {
        CategoryEditDialog instance = new CategoryEditDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.CATEGORY_DATA, categoryData);
        instance.setArguments(args);
        return instance;
    }

    /**
     * アクティビティにアタッチした時に呼び出される。
     *
     * @param activity アクティビティ
     */
    @Override
    public void onAttach(Activity activity) {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onAttach(activity);

        // リスナーを取得する。
        try {
            mOnUpdateListener = (OnUpdateListener)activity;
        } catch (ClassCastException e) {
            mLogger.e(e);
            throw new ClassCastException(activity.toString() + " must implement OnUpdateListener");
        }

        mLogger.d("OUT(OK)");
    }

    /**
     * 生成した時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     * @return ダイアログ
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogger.d("IN");

        // 引数を取得する。
        mCategoryData = (CategoryData)getArguments().getSerializable(ExtraKey.CATEGORY_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_category_edit, null);

        mCategoryNameValue = (EditText)rootView.findViewById(R.id.categoryNameValue);
        mCategoryNameValue.setText(mCategoryData.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.category_edit_dialog_title);
        builder.setPositiveButton(R.string.dialog_regist_button, null);
        builder.setNegativeButton(R.string.dialog_cancel_button, null);
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // ボタン押下でダイアログが閉じないようにリスナーを設定する。
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doResist();
                    }
                });

                ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doCancel();
                    }
                });
            }
        });

        mLogger.d("OUT(OUT)");
        return dialog;
    }

    /**
     * 登録する。
     */
    private void doResist() {
        mLogger.d("IN");

        // カテゴリ名を取得する。
        String categoryName = mCategoryNameValue.getText().toString();

        // カテゴリ名が未入力の場合
        if (StringUtil.isNullOrEmpty(categoryName)) {
            // 終了する。
            toast(R.string.error_input_category_name);
            mLogger.d("OUT(NG)");
            return;
        }

        ContentResolver resolver = getActivity().getContentResolver();

        {
            // 登録済みか確認する。
            String selection = CategoryTable.NAME + " = ?";
            String[] selectionArgs = {categoryName};
            Cursor c = resolver.query(LowestProvider.CATEGORY_CONTENT_URI, null, selection, selectionArgs, null);
            boolean existFlg = false;
            try {
                // カーソルがある場合
                if (null != c) {
                    // データがある場合
                    if (c.moveToFirst()) {
                        // 登録済みとする。
                        existFlg = true;
                    }
                }
            } finally {
                if (null != c) {
                    c.close();
                }
            }

            // 登録済みの場合
            if (existFlg) {
                // 終了する。
                toast(R.string.error_registed);
                mLogger.d("OUT(NG)");
                return;
            }
        }

        {
            long categoryId = mCategoryData.getId();
            long updateTime = new Date().getTime();
            ContentValues values = new ContentValues();
            values.put(CategoryTable.NAME, categoryName);
            values.put(CategoryTable.UPDATE_TIME, updateTime);

            // 新規登録の場合
            if (0 == categoryId) {
                Uri result = resolver.insert(LowestProvider.CATEGORY_CONTENT_URI, values);
                if (null == result) {
                    toast(R.string.error_regist);
                    mLogger.d("OUT(NG)");
                    return;
                }

            // 更新の場合
            } else {
                String selection = CategoryTable.ID + " = ?";
                String[] selectionArgs = {String.valueOf(categoryId)};
                int result = resolver.update(LowestProvider.CATEGORY_CONTENT_URI, values, selection, selectionArgs);
                if (0 == result) {
                    toast(R.string.error_update);
                    mLogger.d("OUT(NG)");
                    return;
                }
            }
        }

        // 更新を通知する。
        mOnUpdateListener.onUpdate();

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }

    /**
     * キャンセルする。
     */
    private void doCancel() {
        mLogger.d("IN");

        toast(R.string.error_cancel);

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }
}
