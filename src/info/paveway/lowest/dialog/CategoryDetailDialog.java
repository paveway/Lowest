package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.R;
import info.paveway.lowest.data.CategoryData;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 最低価格記録アプリ
 * カテゴリ詳細ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class CategoryDetailDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(CategoryDetailDialog.class);

    /** カテゴリデータ */
    private CategoryData mCategoryData;

    /** カテゴリ名表示 */
    private TextView mCategoryNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static CategoryDetailDialog newInstance(CategoryData categoryData) {
        CategoryDetailDialog instance = new CategoryDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.CATEGORY_DATA, categoryData);
        instance.setArguments(args);
        return instance;
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
        View rootView = inflater.inflate(R.layout.dialog_category_detail, null);

        mCategoryNameValue = (TextView)rootView.findViewById(R.id.categoryNameValue);
        mCategoryNameValue.setText(mCategoryData.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.category_detail_dialog_title);
        builder.setPositiveButton(R.string.dialog_update_button, null);
        builder.setNeutralButton( R.string.dialog_delete_button, null);
        builder.setNegativeButton(R.string.dialog_close_button,  null);
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
                        doUpdate();
                    }
                });

                ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doDelete();
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
     * 変更する。
     */
    private void doUpdate() {
        mLogger.d("IN");

        // カテゴリ編集ダイアログを表示する。
        FragmentManager manager = getActivity().getSupportFragmentManager();
        CategoryEditDialog categoryEditDialog = CategoryEditDialog.newInstance(mCategoryData);
        categoryEditDialog.show(manager, CategoryEditDialog.class.getSimpleName());

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }

    /**
     * 削除する。
     */
    private void doDelete() {
        mLogger.d("IN");

        // カテゴリデータ削除確認ダイアログを表示する。
        FragmentManager manager = getActivity().getSupportFragmentManager();
        CategoryDeleteDialog categoryDeleteDialog = CategoryDeleteDialog.newInstance(mCategoryData);
        categoryDeleteDialog.show(manager, CategoryDeleteDialog.class.getSimpleName());

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
