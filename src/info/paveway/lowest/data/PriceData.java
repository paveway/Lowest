package info.paveway.lowest.data;

import java.io.Serializable;

/**
 * 最低価格
 * 価格データ保持クラス
 *
 * @version 1.0 新規作成
 *
 */
@SuppressWarnings("serial")
public class PriceData implements Serializable {

    /** 価格ID */
    private int mPriceId;

    /** 品物ID */
    private int mGoodsId;

    /** 店名 */
    private String mShopName;

    /** 数量 */
    private double mQuantity;

    /** 価格 */
    private long mPrice;

    /**
     * コンストラクタ
     */
    public PriceData() {
        super();
    }

    /**
     * 価格IDを設定する。
     *
     * @param priceId 価格ID
     */
    public void setPriceId(int priceId) {
        mPriceId = priceId;
    }

    /**
     * 価格IDを返却する。
     *
     * @return 価格ID
     */
    public int getPriceId() {
        return mPriceId;
    }

    /**
     * 品物IDを設定する。
     *
     * @param goodsId 品物ID
     */
    public void setGoodsId(int goodsId) {
        mGoodsId = goodsId;
    }

    /**
     * 品物IDを返却する。
     *
     * @return 品物ID
     */
    public int getGoodsId() {
        return mGoodsId;
    }

    /**
     * 店名を設定する。
     *
     * @param shopId 店名
     */
    public void setShopName(String shopName) {
    	mShopName = shopName;
    }

    /**
     * 店名を返却する。
     *
     * @return 店名
     */
    public String getShopName() {
        return mShopName;
    }

    /**
     * 数量を設定する。
     *
     * @param quantity 数量
     */
    public void setQuantity(double quantity) {
        mQuantity = quantity;
    }

    /**
     * 数量を返却する。
     *
     * @return 数量
     */
    public double getQuantity() {
        return mQuantity;
    }

    /**
     * 価格を設定する。
     *
     * @param price 価格
     */
    public void setPrice(long price) {
        mPrice = price;
    }

    /**
     * 価格を返却する。
     *
     * @return 価格
     */
    public double getPrice() {
        return mPrice;
    }

    /**
     * 単価を返却する。
     *
     * @return 単価
     */
    public double getUnitPrice() {
    	if (0 != mQuantity) {
    		return mPrice / mQuantity;
    	} else {
    		return 0;
    	}
    }
}
