package net.softminds.goodroad.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import net.softminds.goodroad.R;

public class ManualActivity extends AppCompatActivity {

    private ViewFlipper mViewFlipper;
    private Context mContext;
    private float initialX;
    private TextView mTvPaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        mContext = this;
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.view_flipper);
        mTvPaging = (TextView) this.findViewById(R.id.tv_paging);
        mTvPaging.setText("1/" + mViewFlipper.getChildCount());
    }

    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalX = touchevent.getX();
                if (initialX >= finalX) {
                    if (mViewFlipper.getDisplayedChild() == 19)
                        break;

                    mTvPaging.setText((mViewFlipper.getDisplayedChild() + 2) + "/" + mViewFlipper.getChildCount());

                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.in_from_right));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.out_to_left));

                    mViewFlipper.showNext();
                } else {
                    if (mViewFlipper.getDisplayedChild() == 0)
                        break;

                    mTvPaging.setText(mViewFlipper.getDisplayedChild() + "/" + mViewFlipper.getChildCount());

                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.in_from_left));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.out_to_right));

                    mViewFlipper.showPrevious();
                }
                break;
        }
        return false;
    }
}
