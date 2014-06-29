package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.R;
import info.paveway.lowest.data.PriceData;
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
        builder.setTitle(R.string.price_detail_dialog_title);
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

        // 価格データ編集ダイアログを表示する。
        FragmentManager manager = getActivity().getSupportFragmentManager();
        PriceEditDialog priceEditDialog = PriceEditDialog.newInstance(mPriceData);
        priceEditDialog.show(manager, PriceEditDialog.class.getSimpleName());

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }

    /**
     * 削除する。
     */
    private void doDelete() {
        mLogger.d("IN");

        // 価格データ削除確認ダイアログを表示する。
        FragmentManager manager = getActivity().getSupportFragmentManager();
        PriceDeleteDialog priceDeleteDialog = PriceDeleteDialog.newInstance(mPriceData);
        priceDeleteDialog.show(manager, PriceDeleteDialog.class.getSimpleName());

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
