package info.paveway.lowest.data;

import java.io.Serializable;

/**
 * 最低価格記録アプリ
 * データ抽象基底クラス
 *
 * @version 1.0 新規作成
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractBaseData implements Serializable {

    /** ID */
    protected long mId;

    /** 名称 */
    protected String mName;

    /** 更新日 */
    protected long mUpdateTime;

    /**
     * コンストラクタ
     */
    public AbstractBaseData() {
        super();
    }

    /**
     * IDを設定する。
     *
     * @param id ID
     */
    public void setId(long id) {
        mId = id;
    }

    /**
     * IDを返却する。
     *
     * @return ID
     */
    public long getId() {
        return mId;
    }

    /**
     * 名称を設定する。
     *
     * @param name 名称
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * 名称を返却する。
     *
     * @return 名称
     */
    public String getName() {
        return mName;
    }

    /**
     * 更新日時を設定する。
     *
     * @param updateTime 更新日時
     */
    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }

    /**
     * 更新日時を返却する。
     *
     * @return 更新日時
     */
    public long getUpdateTime() {
        return mUpdateTime;
    }
}
