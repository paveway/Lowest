package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.CategoryData;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
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
        builder.setTitle("カテゴリ詳細");
        builder.setPositiveButton("変更", new DialogButtonOnClickListener());
        builder.setNeutralButton("削除", new DialogButtonOnClickListener());
        builder.setNegativeButton("キャンセル", new DialogButtonOnClickListener());
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mLogger.d("OUT(OUT)");
        return dialog;
    }

    /**************************************************************************/
    /**
     * ダイアログボタンクリックリスナークラス
     *
     */
    private class DialogButtonOnClickListener implements DialogInterface.OnClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(DialogButtonOnClickListener.class);

        /**
         * ボタンがクリックされた時に呼び出される。
         *
         * @param dialog ダイアログ
         * @param which クリックされたボタン
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mLogger.d("IN");

            // ボタンにより処理を判別する。
            switch (which) {
            // 変更ボタンの場合
            case Dialog.BUTTON_POSITIVE: {
                mLogger.d("BUTTON_POSITIVE");

                // カテゴリ編集ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                CategoryEditDialog categoryEditDialog = CategoryEditDialog.newInstance(mCategoryData);
                categoryEditDialog.show(manager, CategoryEditDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // 削除ボタンの場合
            case Dialog.BUTTON_NEUTRAL: {
                // カテゴリデータ削除確認ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                CategoryDeleteDialog categoryDeleteDialog = CategoryDeleteDialog.newInstance(mCategoryData);
                categoryDeleteDialog.show(manager, CategoryDeleteDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // キャンセルボタンの場合
            case Dialog.BUTTON_NEGATIVE:
                toast("キャンセルします");

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OUT)");
        }
    }
}
