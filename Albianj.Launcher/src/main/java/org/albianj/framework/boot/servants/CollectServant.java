package org.albianj.framework.boot.servants;


import org.albianj.framework.boot.tags.BundleSharingTag;

import java.util.Collection;
import java.util.Map;

@BundleSharingTag
public class CollectServant {
    public static CollectServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new CollectServant();
        }
    }

    protected CollectServant() {

    }

    public boolean isNullOrEmpty(Collection<?> collection) {
        return null == collection || collection.isEmpty();
    }

    public boolean isNullOrEmpty(@SuppressWarnings("rawtypes") Map map) {
        return null == map || map.isEmpty();
    }

    public boolean isNull(@SuppressWarnings("rawtypes") Map map) {
        return null == map;
    }

}
