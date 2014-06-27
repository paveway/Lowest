package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.PriceData;
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
 * 価格編集ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class PriceDetailDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(PriceDetailDialog.class);

    /** 価格データ */
    private PriceData mPriceData;

    /** 店名表示 */
    private TextView mShopNameValue;

    /** 数量入力 */
    private TextView mQuantityValue;

    /** 価格入力 */
    private TextView mPriceValue;

    /** メモ入力 */
    private TextView mMemoValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static PriceDetailDialog newInstance(PriceData priceData) {
        PriceDetailDialog instance = new PriceDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.PRICE_DATA, priceData);
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
        mPriceData = (PriceData)getArguments().getSerializable(ExtraKey.PRICE_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_price_detail, null);

        mShopNameValue = (TextView)rootView.findViewById(R.id.shopNameValue);
        mQuantityValue = (TextView)rootView.findViewById(R.id.quantityValue);
        mPriceValue    = (TextView)rootView.findViewById(R.id.priceValue);
        mMemoValue     = (TextView)rootView.findViewById(R.id.memoValue);

        mShopNameValue.setText(mPriceData.getShopName());
        mQuantityValue.setText(String.format("%.2f", mPriceData.getQuantity()));
        mPriceValue.setText(String.format("%d", mPriceData.getPrice()));
        mMemoValue.setText(mPriceData.getMemo());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("価格詳細");
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

                // 店データ編集ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                PriceEditDialog priceEditDialog = PriceEditDialog.newInstance(mPriceData);
                priceEditDialog.show(manager, PriceEditDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // 削除ボタンの場合
            case Dialog.BUTTON_NEUTRAL: {
                mLogger.d("BUTTON_NEUTRAL");

                // 店データ削除確認ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                PriceDeleteDialog priceDeleteDialog = PriceDeleteDialog.newInstance(mPriceData);
                priceDeleteDialog.show(manager, PriceDeleteDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // キャンセルボタンの場合
            case Dialog.BUTTON_NEGATIVE:
                mLogger.d("BUTTON_NEGATIVE");

                toast("キャンセルします");

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OUT)");
        }
    }
}
