package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.R;
import info.paveway.lowest.data.GoodsData;
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
 * 商品詳細ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class GoodsDetailDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(GoodsDetailDialog.class);

    /** 商品データ */
    private GoodsData mGoodsData;

    /** カテゴリ表示 */
    private TextView mCategoryNameValue;

    /** 商品名表示 */
    private TextView mGoodsNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static GoodsDetailDialog newInstance(GoodsData goodsData) {
        GoodsDetailDialog instance = new GoodsDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.GOODS_DATA, goodsData);
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
        mGoodsData = (GoodsData)getArguments().getSerializable(ExtraKey.GOODS_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_goods_detail, null);

        mCategoryNameValue = (TextView)rootView.findViewById(R.id.categoryNameValue);
        mGoodsNameValue    = (TextView)rootView.findViewById(R.id.goodsNameValue);

        mCategoryNameValue.setText(mGoodsData.getCategoryName());
        mGoodsNameValue.setText(mGoodsData.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.goods_detail_dialog_title);
        builder.setPositiveButton(R.string.dialog_update_button, new DialogButtonOnClickListener());
        builder.setNeutralButton( R.string.dialog_delete_button,  new DialogButtonOnClickListener());
        builder.setNegativeButton(R.string.dialog_close_button,  new DialogButtonOnClickListener());
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

                // 商品編集ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                GoodsEditDialog goodsEditDialog = GoodsEditDialog.newInstance(mGoodsData);
                goodsEditDialog.show(manager, GoodsEditDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // 削除ボタンの場合
            case Dialog.BUTTON_NEUTRAL: {
                mLogger.d("BUTTON_NEUTRAL");

                // 商品データ削除確認ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                GoodsDeleteDialog goodsDeleteDialog = GoodsDeleteDialog.newInstance(mGoodsData);
                goodsDeleteDialog.show(manager, GoodsDeleteDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // キャンセルボタンの場合
            case Dialog.BUTTON_NEGATIVE:
                mLogger.d("BUTTON_NEGATIVE");

                toast(R.string.error_cancel);

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OUT)");
        }
    }
}
