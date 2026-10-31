package com.fongmi.android.ok.impl;

import com.fongmi.android.ok.bean.Site;

public interface SiteCallback {

    void setSite(Site item);

    void onChanged();
}
