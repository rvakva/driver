package com.easymi.personal.activity;

import android.os.Bundle;
import android.view.View;

import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.widget.CusToolbar;
import com.easymi.component.widget.RatingBar;
import com.easymi.personal.R;

/**
 * Created by developerLzh on 2017/11/10 0010.
 */

public class StatsActivity extends RxBaseActivity {

    RatingBar ratingBar;

    @Override
    public int getLayoutId() {
        return R.layout.activity_stats;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        ratingBar = findViewById(R.id.rating_bar);

        ratingBar.setStarMark(5f);
    }

    @Override
    public void initToolBar() {
        CusToolbar cusToolbar = findViewById(R.id.cus_toolbar);
        cusToolbar.setLeftIcon(R.drawable.ic_arrow_back, view -> {
            onBackPressed();
        });
        cusToolbar.setTitle(R.string.set_statistics);
    }
}
